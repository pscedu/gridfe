/* $Id$ */

/*
 * mod_fum - functional user Machiavellianism (for Kerberos)
 * Module to provide the functionality of kinit(1),
 * kx509(1), and kxlist(1) -p
 *
 * Robert Budden	<rbudden@psc.edu>, <rmb265@psu.edu>
 * Jared Yanovich	<yanovich@psc.edu>
 * Summer 2004 Pittsburgh Supercomputing Center
*/

#define _GNU_SOURCE /* wow, gross, needed for some stat function */
#include <sys/types.h>
#include <sys/stat.h>

#include <ctype.h>
#include <dirent.h>
#include <pwd.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <openssl/rsa.h>
#include <openssl/x509v3.h>
#include <openssl/pem.h>

#include <krb5.h>
#include <kx509.h>

#include <httpd/httpd.h>
#include <httpd/http_config.h>
#include <httpd/http_core.h>
#include <httpd/http_log.h>
#include <httpd/http_main.h>
#include <httpd/http_protocol.h>
#include <httpd/http_request.h>
#include <apr_strings.h>
#include <apr_env.h>

#ifdef STANDALONE
#define apr_palloc(pool, siz) malloc(siz)
#define apr_pstrdup(pool, str) strdup(str)
#define apr_env_set(name, val, pool) setenv(name, val, 1)
#endif

#define MF_VERSION "mod_fum/0.1"

#define KPD_PROXIABLE 1
#define KPD_FORWARDABLE 0
#define KPD_LIFETIME 28800 /* 8hrs. default */

#define KX509_HOSTNAME "certificate"
#define KX509_SERVNAME "kx509"

/* XXX: put in a .conf file? */
#define _PATH_X509CERT "/tmp/x509up_fum_u"
#define _PATH_KRB5CERT "/tmp/krb5cc_fum_"
#define _PATH_MFTMP "/tmp/mod-fum-tmp"

/* xxx remove */
#define _PATH_CERT_FILE "x509up_fum_u"
#define _PATH_CERT_DIR "/tmp"

#define X509_FILE_PERM 0600

#define MF_USERENV "REMOTE_USER"

struct krb5_inst {
	krb5_context	 ki_ctx;
	krb5_ccache	 ki_cache;
	krb5_principal	 ki_prin;
	krb5_creds	 ki_cred;
	int		 ki_init;
};

struct krb5_prefs {
	krb5_deltat	 kp_life;
	int		 kp_fwd;
	int		 kp_proxy;
	const char	*kp_prin;
	const char	*kp_pw;
};

static char	*mf_dstrcat(const char *, const char *);
static char	*mf_get_uid_from_ticket_cache(const char *);
static int	 mf_check_for_cred(const char *);
static int	 mf_kinit(struct krb5_inst *, struct krb5_prefs *);
static int	 mf_kinit_setup(struct krb5_inst *, struct krb5_prefs *);
static int	 mf_krb5_init(struct krb5_inst *, const char *);
static int	 mf_kx509(const char *);
static int	 mf_kxlist(const char *);
static int	 mf_kxlist_crypto(struct krb5_inst *, char *);
static int	 mf_kxlist_setup(struct krb5_inst *);
static int	 mf_user_id_from_principal(const char *, char **);
static int	 mf_valid_cred(char *);
static int	 mf_valid_user(const char *, const char *);
static void	 mf_kinit_cleanup(struct krb5_inst *);
static void	 mf_krb5_free(struct krb5_inst *);
static void	 mf_log(const char *, ...);
static void	 mod_fum_hooks(apr_pool_t *);
static void	 mf_remove_certs(char *);

int do_kx509(int, char **);
int mod_fum_main(const char *, const char *);
int mod_fum_auth(request_rec *);

static apr_pool_t	*mf_pool = NULL;
static request_rec	*mf_rec = NULL;

#ifdef APXS1
#else
/* Apache 2.x lmodule record, handlers, & hooks */
module fum_module = {
	STANDARD20_MODULE_STUFF,
	NULL,				/* create per-httpd.conf */
	NULL,				/* merge per-httpd.conf */
	NULL,				/* create pre-.htaccess */
	NULL,				/* merge pre-.htaccess */
	NULL,				/* config */
	mod_fum_hooks,			/* hooks */
};

/* Register hooks in Apache */
void
mod_fum_hooks(apr_pool_t *p)
{
# ifndef STANDALONE
	/* We need to be the first to intercept the password. */
	ap_hook_check_user_id(mod_fum_auth, NULL, NULL, APR_HOOK_MIDDLE);
	ap_add_version_component(p, MF_VERSION);
# endif /* STANDALONE */
}
#endif

/* Apache authentication hook */
int
mod_fum_auth(request_rec *r)
{
	const char *auth, *name, *type, *pass = NULL;
	char *user;
	int err;

	/* Save request record. */
	mf_pool = r->pool;
	mf_rec = r;

mf_log("auth()");

	if ((type = ap_auth_type(r)) == NULL ||
	    strcasecmp(type, "fum") != 0)
		return (DECLINED);

	if ((auth = apr_table_get(r->headers_in, "Authorization")) == NULL)
		goto failed_auth;
	if ((type = ap_getword_white(r->pool, &auth)) == NULL ||
	    strcasecmp(type, "Basic") != 0)
		goto failed_auth;
	pass = ap_pbase64decode(r->pool, auth);
	user = ap_getword(r->pool, &pass, ':');

	/* Check if previous credentials exist. */
	if (!mf_check_for_cred(user)) {
mf_log("creating new credentials");
		/* Create new certs. */
		if ((err = mod_fum_main(user, pass)) ==
		    HTTP_UNAUTHORIZED)
			goto failed_auth;
		else
			return (err);
	}

mf_log("auth() - cred");
	/*
	 * They exist, make sure the user/pass is correct;
	 * otherwise, a correct username and wrong password
	 * will work.
	 */
	if (!mf_valid_user(user, pass)) {
		mf_log("wrong user/pass combination; user: %s", user);
		goto failed_auth;
	}

mf_log("auth() - valid");
	/*
	 * Finally check if the credentials have expired
	 * or not.  If so, create new certs; if not, do
	 * nothing.  mod_fum assumes that if the Kerberos
	 * ticket (created by mod_fum) is valid, then the
	 * X.509 ticket is as well.
	 */
	if (!mf_valid_cred(user)) {
		/*
		 * This could also be a permissions problem.  If the
		 * user already has a valid kerberos certificate, Apache
		 * does not have permission to read it. Thus mod_fum
		 * creates credentials with unique names.
		 */
		mf_log("credentials expired, removing and recreating");

		/* Try to remove old certs */
		mf_remove_certs(user);

		/* Create new certs */
		if ((err = mod_fum_main(user, pass)) != HTTP_UNAUTHORIZED)
			return (err);
	} else {
		/*
		 * Without this, auth always fails with
		 * previous credentails.
		 */
		return OK;
}

failed_auth:
	name = ap_auth_name(r);
	apr_table_add(r->err_headers_out, "WWW-Authenticate",
	    apr_pstrcat(r->pool, "Basic realm=\"", name, "\"", NULL));
	return (HTTP_UNAUTHORIZED);
}

int
mod_fum_main(const char *principal, const char *password)
{
	char *tkt_cache, *uid;
	struct krb5_prefs kp;
	struct krb5_inst ki;
	int err;

	/* XXX Resolve UID from KDC with given principal (possible?) */

mf_log("main()");
	/* Read UID from /etc/passwd */
	if ((err = mf_user_id_from_principal(principal, &uid)) != 0)
		return (err);
	tkt_cache = mf_dstrcat(_PATH_KRB5CERT, uid);
	free(uid);
	if (tkt_cache == NULL) {
		mf_log("dstrcat");
		return (HTTP_INTERNAL_SERVER_ERROR);
	}

mf_log("main() - krb5");
	if ((err = mf_krb5_init(&ki, tkt_cache)) != 0)
		return (err);

	kp.kp_proxy = KPD_PROXIABLE;
	kp.kp_fwd = KPD_FORWARDABLE;
	kp.kp_life = KPD_LIFETIME;
	kp.kp_prin = principal;
	kp.kp_pw = password;

mf_log("main() - kinit");
	if ((err = mf_kinit_setup(&ki, &kp)) != 0)
		return (err);

	/* kinit -c /tmp/krb5cc_$UID */
	if ((err = mf_kinit(&ki, &kp)) != 0)
		return (err);

	mf_kinit_cleanup(&ki);
	mf_krb5_free(&ki);

mf_log("main() - kx509");
	if ((err = mf_kx509(tkt_cache)) != 0)
		return (err);

mf_log("main() - kxlist");
	/* kxlist -p */
	if ((err = mf_kxlist(tkt_cache)) != 0)
		return (err);

mf_log("success!");

#if 0
	/* Tomcat doesn't receive this. */
	if ((err = apr_env_set(MF_USERENV, principal, mf_pool)) != OK)
		return (err);
#endif

	return (0); /* HTTP_OK ? */
}

static int
mf_kxlist(const char *tkt_cache)
{
	char *uid, *name;
	struct krb5_inst ki;
	int err;

	/*
	 * Parse tkt_cache name to get UID:
	 *	/tmp/krb5cc_UID_FUBAR
	 *	/tmp/krb5cc_UID
	 */
	if ((uid = mf_get_uid_from_ticket_cache(tkt_cache)) == NULL)
		return (HTTP_INTERNAL_SERVER_ERROR);
	/* krb5 initial context setup */
	if ((err = mf_krb5_init(&ki, tkt_cache)) != 0)
		return (err);
	/* Obtain proper kx509 credentials */
	if ((err = mf_kxlist_setup(&ki)) != 0)
		return (err);

	/* Perform crypto & write certificate */
	name = mf_dstrcat(_PATH_X509CERT, uid);
mf_log("path: %s, uid: %s, ful: %s", _PATH_X509CERT, uid, name);
	if (name == NULL)
		return (HTTP_INTERNAL_SERVER_ERROR);
	err = mf_kxlist_crypto(&ki, name);
	mf_krb5_free(&ki);
	return (err);
}

/*
 * Perform crypto and write the X.509 certificate
 */
static int
mf_kxlist_crypto(struct krb5_inst *ki, char *name)
{
	const unsigned char *data;
	unsigned int klen, clen;
	X509 *cert = NULL;
	RSA *priv = NULL;
	FILE *file;

mf_log("kxlist_crypto: open %s", name);
	if ((file = fopen(name, "w")) == NULL) {
		mf_log("%s", name);
		return (HTTP_INTERNAL_SERVER_ERROR);
	}
	klen = ki->ki_cred.ticket.length;
	clen = ki->ki_cred.second_ticket.length;

	/* Decode the certificate (we want PEM format) */
	data = ki->ki_cred.second_ticket.data;
	d2i_X509(&cert, &data, clen);

	/* Extract & decode the RSA private key from the certificate */
	data = ki->ki_cred.ticket.data;
	d2i_RSAPrivateKey(&priv, &data, klen);

	if (priv == NULL) {
		mf_log("d2i_RSAPrivateKey failed");
		return (HTTP_INTERNAL_SERVER_ERROR);
	}

	/* Write the certificate, appropriately formatted */
	PEM_write_X509(file, cert);
	PEM_write_RSAPrivateKey(file, priv, NULL, NULL, 0, NULL,
	    NULL);
	(void)fclose(file);

	/* Set proper permissions */
	(void)chmod(name, X509_FILE_PERM);
	return (0);
}

/*
 * Load the kx509 credentials
 */
static int
mf_kxlist_setup(struct krb5_inst *ki)
{
	krb5_error_code err;
	krb5_creds screds;

	memset(&screds, '\0', sizeof(krb5_creds));

	/* The primary principal will be for the client */
	if ((err = krb5_cc_get_principal(ki->ki_ctx, ki->ki_cache,
	    &screds.client)) != 0) {
		mf_log("get client principal failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}

	/* Now obtain one for the server */
	if ((err = krb5_sname_to_principal(ki->ki_ctx, KX509_HOSTNAME,
	     KX509_SERVNAME, KRB5_NT_UNKNOWN, &screds.server)) != 0) {
		mf_log("get server principal failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}

	/* Retrieve the kx509 credentials, search by service name only */
	if ((err = krb5_cc_retrieve_cred(ki->ki_ctx, ki->ki_cache,
	     KRB5_TC_MATCH_SRV_NAMEONLY, &screds, &ki->ki_cred)) != 0) {
		mf_log("unable to retrieve kx509 credential (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}
	krb5_free_cred_contents(ki->ki_ctx, &screds);
	return (0);
}

/*
 * Parse the user ID from the ticket_cache name
 */
static char *
mf_get_uid_from_ticket_cache(const char *tkt)
{
	char *uid;

	/* Parse `/tmp/krb5cc_fum_5222' for `5222'. */
	if ((uid = strrchr(tkt, '_')) == NULL || !isdigit(*++uid)) {
		mf_log("invalid ticket format: %s", tkt);
		return (NULL);
	}
	return (apr_pstrdup(mf_pool, uid));
}

/*
 * Parse the principal and get the uid either from the KDC
 * (XXX if that is even possible) or just read from /etc/passwd
 */
static int
mf_user_id_from_principal(const char *prin, char **uid)
{
	struct passwd *pw;
	char *p, *s;

	/* Parse principal. */
	p = apr_pstrdup(mf_pool, prin);
	if ((s = strchr(p, '@')) != NULL)
		*s = '\0';
	if ((pw = getpwnam(p)) == NULL){
		mf_log("user not in /etc/passwd: %s", p);
		return (HTTP_UNAUTHORIZED);
	}
	if (asprintf(uid, "%d", pw->pw_uid) == -1) {
		*uid = NULL;
		mf_log("asprintf");
		return (HTTP_INTERNAL_SERVER_ERROR);
	}
	return (0);
}

static int
mf_kx509(const char *tkt_cache)
{
	char *argv[4];
	int argc, err;

	/* setup kx509 as would be called from command line */
	argv[0] = apr_pstrdup(mf_pool, "kx509");
	argv[1] = apr_pstrdup(mf_pool, "-c");
	argv[2] = apr_pstrdup(mf_pool, tkt_cache);
	argv[3] = NULL;
	argc = 3;

	if ((err = do_kx509(argc, argv)) != KX509_STATUS_GOOD) {
		mf_log("kx509 failed (%d)", err);
		return (HTTP_INTERNAL_SERVER_ERROR);
	}
	return (0);
}

static int
mf_kinit(struct krb5_inst *ki, struct krb5_prefs *kp)
{
	krb5_get_init_creds_opt opt;
	krb5_error_code err;

	/* Set default credential options? */
	krb5_get_init_creds_opt_init(&opt);

	/* Make our changes to the defaults */
	krb5_get_init_creds_opt_set_forwardable(&opt, kp->kp_fwd);
	krb5_get_init_creds_opt_set_proxiable(&opt, kp->kp_proxy);
	krb5_get_init_creds_opt_set_tkt_life(&opt, kp->kp_life);
	krb5_get_init_creds_opt_set_address_list(&opt, NULL);

	/* Create credentials from given password */
	if ((err = krb5_get_init_creds_password(ki->ki_ctx, &ki->ki_cred,
	     ki->ki_prin, (char *)kp->kp_pw, krb5_prompter_posix,
	     NULL, 0, NULL, &opt)) != 0) {
		mf_log("get initial credentials failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}
	ki->ki_init = 1;

	/* Initialize the cache file */
	if ((err = krb5_cc_initialize(ki->ki_ctx, ki->ki_cache,
	     ki->ki_prin)) != 0) {
		/*
		 * In testing, this is thrown with the error -1765328188
		 * when there are already credentials created and mod_fum
		 * does not have the proper permissions to read them!
		 */
		mf_log("initialize cache failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}

	/* Store the credential */
	if ((err = krb5_cc_store_cred(ki->ki_ctx, ki->ki_cache,
	     &ki->ki_cred)) != 0) {
		mf_log("store credentials failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}
	return (0);
}

/*
 * Handle standard krb5 initial functions
 */
static int
mf_krb5_init(struct krb5_inst *ki, const char *tkt_cache)
{
	krb5_error_code err;

	memset(&ki->ki_cred, '\0', sizeof(krb5_creds));
	ki->ki_init = 0;

	/* Initialize application context */
	if ((err = krb5_init_context(&ki->ki_ctx)) != 0) {
		mf_log("krb5_init_context failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}
	/*
	 * Don't use the default.  We need to be able to
	 * write the tkt out with different uid's for each
	 * individual user.
	 */
	/* err = krb5_cc_default(ki->ki_ctx, &ki->cache); */
	if ((err = krb5_cc_resolve(ki->ki_ctx, tkt_cache,
	     &ki->ki_cache)) != 0) {
		mf_log("default cache failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}
	return (0);
}

static void
mf_krb5_free(struct krb5_inst *ki)
{
	if (ki->ki_init)
		krb5_free_cred_contents(ki->ki_ctx, &ki->ki_cred);
	if (ki->ki_cache)
		krb5_cc_close(ki->ki_ctx, ki->ki_cache);
}

/*
 * Kinit initial setup
 */
static int
mf_kinit_setup(struct krb5_inst *ki, struct krb5_prefs *kp)
{
	krb5_error_code err;

	/*
	 * Generate a full principal name to be used for authentication.
	 */
	if ((err = krb5_sname_to_principal(ki->ki_ctx, NULL, NULL,
	     KRB5_NT_SRV_HST, &ki->ki_prin)) != 0) {
		mf_log("create principal failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}

	/*
	 * Take the principal name we were given and parse it into the
	 * appropriate form for authentication protocols.
	 */
	if ((err = krb5_parse_name(ki->ki_ctx, kp->kp_prin,
	     &ki->ki_prin)) != 0) {
		mf_log("parse_name failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}
	return (0);
}

static void
mf_kinit_cleanup(struct krb5_inst *ki)
{
	/* if (ki->ki_prin) */
		krb5_free_principal(ki->ki_ctx, ki->ki_prin);
	/* if (ki->ki_ctx) */
		krb5_free_context(ki->ki_ctx);
}

/*
 * Check for previous credentials in /tmp
 */
static int
mf_check_for_cred(const char *principal)
{
	char *uid, *cert;
	int err, found = 0;
	struct dirent *d;
	DIR *dp;

	/* Read uid from /etc/passwd */
	if ((err = mf_user_id_from_principal(principal, &uid)) != OK) {
		mf_log("error finding uid (%d)", err);
		return (0);
	}
	/* Create cert name */
	cert = mf_dstrcat(_PATH_CERT_FILE, uid);
	free(uid);
	if (cert == NULL) {
		mf_log("error creating cert string");
		return (0);
	}
	/* Check if the file exists */
	if ((dp = opendir(_PATH_CERT_DIR)) != NULL) {
		while ((d = readdir(dp)) != NULL) {
			if (strcmp(d->d_name, cert) == 0) {
				found = 1;
				break;
			}
		}
		(void)closedir(dp);
	}
	return (found);
}

/*
 * Check if the certificate has expired or not by finding the kx509
 * certificate and checking it's expiration time, compared with the
 * current time.
 *
 * XXX - This code is so similar to stuff from mod_fum_main and kxlist,
 * that there has to be a nice way to break some of it down to be more
 * modular and compact.
 */
static int
mf_valid_cred(char *principal)
{
	char *uid, *tkt_cache;
	struct krb5_inst ki;
	krb5_timestamp end;
	int err;

	if ((err = mf_user_id_from_principal(principal, &uid)) != OK) {
		mf_log("uid failed (%d)", err);
		return (0);
	}
	tkt_cache = mf_dstrcat(_PATH_KRB5CERT, uid);
	free(uid);

	if (tkt_cache == NULL)
		return (0);
	if (mf_krb5_init(&ki, tkt_cache) != OK)
		/* XXX: mf_krb5_free(&ki); ? */
		return (0);

	/* Grab the kx509 credentials */
	if (mf_kxlist_setup(&ki) != OK)
		return (0);

	/* Get the expiration time of the cert */
	end = ki.ki_cred.times.endtime;
	mf_krb5_free(&ki);

	/* Compare with our time now */
	if (time(NULL) < end)
		return (1);
	return (0);
}

/*
 * Check if the user/pass is valid (for now just simulate a Kerberos
 * authentication)
 *
 * XXX: there must be a better way to validate the authenticity of the
 * user.  If this is the best way, it should be more modular, because
 * much of this code is similar to kinit.
 */
static int
mf_valid_user(const char *principal, const char *password)
{
	krb5_get_init_creds_opt opt;
	char tkt[] = _PATH_MFTMP;
	struct krb5_prefs kp;
	struct krb5_inst ki;
	int err;

	if ((err = mf_krb5_init(&ki, tkt)) != OK) {
		mf_log("krb5_init failed (%d)", err);
		return (0);
	}
	ki.ki_init = 0;
	kp.kp_prin = principal;
	kp.kp_pw = password;

	if ((err = mf_kinit_setup(&ki, &kp)) != 0) {
		mf_log("mf_kinit_setup failed (%d)", err);
		goto cleanup;
	}

	krb5_get_init_creds_opt_init(&opt);

	/*
	 * Try and get an initial ticket.  If this succeeds,
	 * then the username/password is correct.
	 */
	if ((err = krb5_get_init_creds_password(ki.ki_ctx,
	     &ki.ki_cred, ki.ki_prin, (char *)kp.kp_pw,
	     krb5_prompter_posix, NULL, 0, NULL, &opt)) != 0)
		/* XXX: ki.ki_init = 0 ? */
		mf_log("bad authentication (%d)", err);
	ki.ki_init = 1;

cleanup:
	mf_kinit_cleanup(&ki);
	mf_krb5_free(&ki);
	return (ki.ki_init);
}

/*
 * Remove Kerberos tickets and X.509 certificates
 */
static void
mf_remove_certs(char *principal)
{
	char *file, *uid;
	int err;

	/* Get uid */
	if ((err = mf_user_id_from_principal(principal, &uid)) != 0)
		mf_log("mod_fum uid error %d", err);

	/* Remove Kerberos ticket. */
	file = mf_dstrcat(_PATH_KRB5CERT, uid);
	if ((err = remove(file)) != 0)
		mf_log("error removing Kerberos ticket: %s", file);

	/* Remove X.509 cert */
	file = mf_dstrcat(_PATH_X509CERT, uid);
	if ((err = remove(file)) != 0)
		mf_log("error removing X509CERT");
}

/*
 * dynamic strcat routine and malloc wrapper
 */
static char *
mf_dstrcat(const char *s1, const char *s2)
{
	size_t len;
	char *s;

	len = strlen(s1) + strlen(s2) + 1;
	s = (char *)apr_palloc(mf_pool, sizeof(char)*len);
	if (s == NULL) {
		mf_log("apr_palloc failed");
		return (NULL);
	}
	(void)strncpy(s, s1, strlen(s1));
	s[strlen(s1)] = '\0';
	(void)strncat(s, s2, strlen(s2));
	s[len - 1] = '\0';
	return (s);
}

static void
mf_log(const char *fmt, ...)
{
	char tbuf[BUFSIZ], buf[BUFSIZ];
	va_list ap;
	int __sav_errno = errno;

	va_start(ap, fmt);
	(void)vsnprintf(buf, sizeof(buf), fmt, ap);
	if (__sav_errno) {
		(void)snprintf(tbuf, sizeof(tbuf), "%s: %s", buf,
		    strerror(__sav_errno));
		(void)strncpy(buf, tbuf, sizeof(buf) - 1);
		buf[sizeof(buf) - 1] = '\0';
	}
	/* (void)snprintf(buf, sizeof(buf), "%s\n", buf); */
	va_end(ap);

#ifndef STANDALONE
	ap_log_error(APLOG_MARK, APLOG_ERR, (apr_status_t)NULL,
	    mf_rec->server, "%s", buf);
#endif /* STANDALONE */
	errno = __sav_errno;
}
