/* $Id$ */

/*
-------------------------------------------------------------------
** mod_fum - Free Unadulterated Moderation (for Kerberos)
** Free Apache Module to provide the functionality of kinit,
** kx509, and kxlist -p
**
** Robert Budden
** rbudden@psc.edu - rmb265@psu.edu
** Summer 2004 Pittsburgh Supercomputing Center
-------------------------------------------------------------------
*/

#include <sys/types.h>
#include <sys/stat.h>

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

#define MF_VERSION "mod_fum/1.0-a"

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
static char	*mf_dstrslice(const char *, int, int);
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

int do_kx509(int, char **);
int mod_fum_main(const char *, const char *);
int mod_fum_auth(request_rec *);

static apr_pool_t	*mf_pool = NULL;
static request_rec	*mf_rec = NULL;

/* Apache 2.x lmodule record, handlers, & hooks */
module fum_module = {
	STANDARD20_MODULE_STUFF,	/* */
	NULL,				/* */
	NULL,				/* */
	NULL,				/* */
	NULL,				/* */
	NULL,				/* */
	mod_fum_hooks,			/* */
};

/* Register hooks in Apache */
static void
mod_fum_hooks(apr_pool_t *p)
{
	/* We need to be the first to intercept the password */
	ap_hook_check_user_id(mod_fum_auth, NULL, NULL, APR_HOOK_FIRST);
	ap_add_version_component(p, MF_VERSION);
}

/* Apache authentication hook */
int
mod_fum_auth(request_rec *r)
{
	const char *pass = NULL;
	char *user;
	int err;

	/* Save request rec */
	mf_pool = r->pool;
	mf_rec = r;

	/*
	 * Get user/pass - NOTE: ap_get_basic_auth_pw() must be called
	 * first, otherwise r->user will be NULL.
	 */
	err = ap_get_basic_auth_pw(r, &pass);
	user = r->user;

	if (err == OK && user && pass) {
		err = HTTP_UNAUTHORIZED;

		/* Check if previous credentials exist */
		if (mf_check_for_cred(user)) {
			/*
			 * If they exist, make sure the user/pass is correct;
			 * otherwise, a correct username and wrong password
			 * will work.
			 */
			if (mf_valid_user(user, pass)) {
				/*
				 * Finally check if the credentials have expired
				 * or not.  If so create new certs, if not, do
				 * nothing.
				 */
				if (mf_valid_cred(user))
					/* Create new certs */
					err = mod_fum_main(user, pass);
				else
					/*
					 * XXX This could also be a permissions
					 * problem.  If the user has already a
					 * valid X.509 certificate, Apache does
					 * not have permissions to read it.
					 */
					mf_log("credentials expired");
			} else
				mf_log("wrong user/pass combination");
		} else
			/* Create new certs */
			err = mod_fum_main(user, pass);
	} else {
		mf_log("authentication incomplete (%d)", err);
		err = HTTP_UNAUTHORIZED;
	}
	return (err);
}

int
mod_fum_main(const char *principal, const char *password)
{
	char *tkt_cache, *uid;
	struct krb5_prefs kp;
	struct krb5_inst ki;
	int err;

	/* XXX Resolve UID from KDC with given principal (possible?) */

	/* Read uid from /etc/passwd */
	if ((err = mf_user_id_from_principal(principal, &uid)) != OK)
		return (err);
	if ((tkt_cache = mf_dstrcat(_PATH_KRB5CERT, uid)) == NULL) {
		mf_log("tkt_cache is NULL");
		return (HTTP_INTERNAL_SERVER_ERROR);
	}
	free(uid);

	if ((err = mf_krb5_init(&ki, tkt_cache)) != OK)
		return (err);

	kp.kp_proxy = KPD_PROXIABLE;
	kp.kp_fwd = KPD_FORWARDABLE;
	kp.kp_life = KPD_LIFETIME;
	kp.kp_prin = principal;
	kp.kp_pw = password;

	if ((err = mf_kinit_setup(&ki, &kp)) != OK)
		return (err);

	/* kinit -c /tmp/krb5cc_$UID */
	if ((err = mf_kinit(&ki, &kp)) != OK)
		return (err);

	mf_kinit_cleanup(&ki);
	mf_krb5_free(&ki);

	if ((err = mf_kx509(tkt_cache)) != OK)
		return (err);

	/* kxlist -p */
	return (mf_kxlist(tkt_cache));
}

static int
mf_kxlist(const char *tkt_cache)
{
	char *uid, *name;
	struct krb5_inst ki;
	int err;

	/*
	 * Parse tkt_cache name to get UID:
	 * /tmp/krb5cc_UID_FUBAR
	 * /tmp/krb5cc_UID
	 */
	uid = mf_get_uid_from_ticket_cache(tkt_cache);
	if (uid) {
		/* krb5 initial context setup */
		err = mf_krb5_init(&ki, tkt_cache);
		if (err == OK) {
			/* Obtain proper kx509 credentials */
			if ((err = mf_kxlist_setup(&ki)) != OK)
				return (err);

			/* Perform crypto & write certificate */
			name = mf_dstrcat(_PATH_X509CERT, uid);
			if (name == NULL)
				return (HTTP_INTERNAL_SERVER_ERROR);
			err = mf_kxlist_crypto(&ki, name);
			mf_krb5_free(&ki);
		}
	} else
		return (HTTP_INTERNAL_SERVER_ERROR);
	return (err);
}

/*
 * Perform crypto and write the X.509 certificate
 */
static int
mf_kxlist_crypto(struct krb5_inst *ki, char *name)
{
	unsigned int klen, clen;
	unsigned char *data;
	X509 *cert = NULL;
	RSA *priv = NULL;
	int err = OK;
	FILE *file;

	if ((file = fopen(name, "w")) == NULL) {
		mf_log("%s", name);
		return (HTTP_INTERNAL_SERVER_ERROR);
	} else {
		klen = ki->ki_cred.ticket.length;
		clen = ki->ki_cred.second_ticket.length;

		/* Decode the certificate (we want PEM format) */
		data = ki->ki_cred.second_ticket.data;
		d2i_X509((X509**)(&cert), &data, clen);

		/* Extract & decode the RSA private key from the certificate */
		data = ki->ki_cred.ticket.data;
		d2i_RSAPrivateKey(&priv, (const unsigned char **)(&data), klen);

		if (priv == NULL) {
			mf_log("d2i_RSAPrivateKey failed");
			err = HTTP_INTERNAL_SERVER_ERROR;
		} else {
			/* Write the certificate appropriately formatted */
			PEM_write_X509(file, cert);
			PEM_write_RSAPrivateKey(file, priv, NULL, NULL, 0, NULL,
			    NULL);
			(void)fclose(file);

			/* Set proper permissions */
			(void)chmod(name, X509_FILE_PERM);
		}
	}
	return (err);
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
	    &screds.client)) != KRB5KDC_ERR_NONE) {
		mf_log("get client principal failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}

	/* Now obtain one for the server */
	if ((err = krb5_sname_to_principal(ki->ki_ctx, KX509_HOSTNAME,
	     KX509_SERVNAME, KRB5_NT_UNKNOWN, &screds.server)) !=
	    KRB5KDC_ERR_NONE) {
		mf_log("get server principal failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}

	/* Retrieve the kx509 credentials, search by service name only */
	if ((err = krb5_cc_retrieve_cred(ki->ki_ctx, ki->ki_cache,
	     KRB5_TC_MATCH_SRV_NAMEONLY, &screds, &ki->ki_cred)) !=
	    KRB5KDC_ERR_NONE)
	if (err) {
		mf_log("unable to retrieve kx509 credential (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}
	krb5_free_cred_contents(ki->ki_ctx, &screds);
	return (OK);
}

/*
 * Parse the user ID from the ticket_cache name
 */
static char *
mf_get_uid_from_ticket_cache(const char *tkt)
{
	int i, j, b, e;
	char *uid;

	/* Default to end of the string. */
	e = strlen(tkt) - 1;

	/* Grab the boundary of the UID. */
	for (i = 0, j = 0; i < (strlen(tkt) - 1) && j < 2; i++) {
		if (tkt[i] == '_') {
			if (j)
				e = i;
			else
				b = i + 1;
			j++;
		}
	}

	/* Slice and convert the UID. */
	uid = mf_dstrslice(tkt, b, e);
	if (uid == NULL)
		mf_log("uid slice error");
	return (uid);
}

/*
 * Parse the principal and get the uid either from the KDC
 * (XXX if that is even possible!) or just read from /etc/passwd
 */
static int
mf_user_id_from_principal(const char *principal, char **uid)
{
	struct passwd *pw;
	int i, j, err = 0;
	char *p;

	/* Parse principal. */
	for (i = 0, j = 0; i < strlen(principal); i++, j++) {
		if (principal[i] == '@')
			break;
	}

	/* slice username */
	p = mf_dstrslice(principal, 0, j - 1);

	if (p) {
		pw = getpwnam(p);
		if (pw == NULL) {
			mf_log("User not in /etc/passwd");
			err = HTTP_UNAUTHORIZED;
		} else {
			if (asprintf(*uid, "%d", pw->pw_uid) == -1) {
				*uid = NULL;
				mf_log("asprintf");
				err = HTTP_INTERNAL_SERVER_ERROR;
			}
		}
	} else {
		mf_log("principal slice error");
		err = HTTP_INTERNAL_SERVER_ERROR;
	}
	return (err);
}

static int
mf_kx509(const char *tkt_cache)
{
	char *argv[3];
	int argc, err;

	/* setup kx509 as would be called from command line */
	argv[0] = apr_pstrdup(mf_pool, "kx509");
	argv[1] = apr_pstrdup(mf_pool, "-c");
	argv[2] = apr_pstrdup(mf_pool, tkt_cache);
	/* argv[3] = NULL; */
	argc = 3;

	if ((err = do_kx509(argc, argv)) != KX509_STATUS_GOOD)
		mf_log("kx509 failed (%d)", err);
	return (err ? HTTP_INTERNAL_SERVER_ERROR : OK);
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
	err = krb5_get_init_creds_password(ki->ki_ctx, &ki->ki_cred,
	    ki->ki_prin, (char *)(kp->kp_pw), krb5_prompter_posix,
	    NULL, 0, NULL, &opt);
	if (err != KRB5KDC_ERR_NONE) {
		mf_log("get initial credentials failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}

	ki->ki_init = 1;

	/* Initialize the cache file */
	err = krb5_cc_initialize(ki->ki_ctx, ki->ki_cache, ki->ki_prin);
	if (err) {
		/*
		 * In testing, this is thrown with the error -1765328188
		 * when there are already credentials created and mod_fum
		 * does not have the proper permissions to read them!
		 */
		mf_log("initialize cache failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}

	/* Store the credential */
	err = krb5_cc_store_cred(ki->ki_ctx, ki->ki_cache, &ki->ki_cred);
	if (err)
		mf_log("store credentials failed (%d)", err);
	return (err == KRB5KDC_ERR_NONE ? OK : HTTP_UNAUTHORIZED);
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
	if ((err = krb5_init_context(&ki->ki_ctx)) == KRB5KDC_ERR_NONE) {
		/*
		 * Don't use the default.  We need to be able to
		 * write the tkt out with different uid's for each
		 * individual user.
		 */
		/* err = krb5_cc_default(ki->ki_ctx, &ki->cache); */
		err = krb5_cc_resolve(ki->ki_ctx, tkt_cache, &ki->ki_cache);
		if (err)
			mf_log("default cache failed (%d)", err);
	} else
		mf_log("krb5_init_context failed (%d)", err);
	return (err == KRB5KDC_ERR_NONE ? OK : HTTP_UNAUTHORIZED);
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
	 * Generate a full principal name
	 * to be used for authentication.
	 */
	err = krb5_sname_to_principal(ki->ki_ctx, NULL, NULL, KRB5_NT_SRV_HST,
	    &ki->ki_prin);
	if (err) {
		mf_log("create principal failed (%d)", err);
		return (HTTP_UNAUTHORIZED);
	}

	/*
	 * Take the principal name we were given and parse it
	 * into the appropriate form for authentication protocols
	 */
	err = krb5_parse_name(ki->ki_ctx, kp->kp_prin, &ki->ki_prin);
	if (err)
		mf_log("parse_name failed (%d)", err);
	return (err == KRB5KDC_ERR_NONE ? OK : HTTP_UNAUTHORIZED);
}

static void
mf_kinit_cleanup(struct krb5_inst *ki)
{
	if (ki->ki_prin)
		krb5_free_principal(ki->ki_ctx, ki->ki_prin);
	if (ki->ki_ctx)
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
	/* Does the file exist */
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
	if ((tkt_cache = mf_dstrcat(_PATH_KRB5CERT, uid)) != NULL) {
		if (mf_krb5_init(&ki, tkt_cache) != OK)
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
	}
	return (0);
}

/*
 * Check if the user/pass is valid
 * (for now just simulate a Kerberos authentication)
 *
 * XXX - There must be a better way to validate the
 * authenticity of the user.
 * XXX - if this is the best way
 * to do this, then i need to try and find a more modular
 * way, because much of this code is similar to kinit.
 */
static int
mf_valid_user(const char *principal, const char *password)
{
	char tkt[] = _PATH_MFTMP;
	krb5_get_init_creds_opt opt;
	struct krb5_prefs kp;
	struct krb5_inst ki;
	int err;

	err = mf_krb5_init(&ki, tkt);
	if (err != OK) {
		mf_log("krb5_init failed (%d)", err);
		return (0);
	}
	ki.ki_init = 0;
	kp.kp_prin = principal;
	kp.kp_pw = password;
	if ((err = mf_kinit_setup(&ki, &kp)) == KRB5KDC_ERR_NONE)
		err = OK;
	else
		err = HTTP_UNAUTHORIZED;
	if (err == OK) {
		krb5_get_init_creds_opt_init(&opt);

		/* Try and get an initial ticket */
		err = krb5_get_init_creds_password(ki.ki_ctx,
		    &ki.ki_cred, ki.ki_prin, (char *)(kp.kp_pw),
		    krb5_prompter_posix, NULL, 0, NULL, &opt);

		/* If this succeeds, then the user/pass is correct */
		if (err == OK)
			ki.ki_init = 1;
		else
			mf_log("bad authentication (%d)", err);
	} else
		mf_log("mf_kinit_setup failed (%d)", err);
	mf_kinit_cleanup(&ki);
	mf_krb5_free(&ki);
	return (ki.ki_init);
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
	s = (char *)(apr_palloc(mf_pool, sizeof(char)*len));
	if (s == NULL)
		mf_log("malloc failed");
	else {
		strncpy(s, s1, strlen(s1));
		s[strlen(s1)] = '\0';
		strncat(s, s2, strlen(s2));
		s[len - 1] = '\0';
	}
	return (s);
}

/*
 * dynamic string routing to slice a section
 */
static char *
mf_dstrslice(const char *s, int x, int y)
{
	size_t len, slen;
	char *s2;

	slen = strlen(s);
	len = y - x + 2;

	if (len) {
		if ((s2 = apr_palloc(mf_pool, sizeof(char) * len)) ==
		    NULL) {
			mf_log("malloc failed");
			return (NULL);
		}
		if (x >= 0 && y >= 0) {
			if (x < slen && y < slen) {
				/* ptr increment to start location */
				s += x;
				strncpy(s2, s, len - 1);
				s2[len - 1] = '\0';
			} else
				s2 = NULL;
		}
	}
	return (s2);
}

static void
mf_log(const char *fmt, ...)
{
	char buf[BUFSIZ];
	va_list ap;
	int __sav_errno = errno;

	va_start(ap, fmt);
	(void)vsnprintf(buf, sizeof(buf), fmt, ap);
	if (__sav_errno)
		(void)snprintf(buf, sizeof(buf), "%s: %s", buf,
		    strerror(__sav_errno));
	/* (void)snprintf(buf, sizeof(buf), "%s\n", buf); */
	va_end(ap);

	ap_log_error(APLOG_MARK, APLOG_ERR, (apr_status_t)(NULL),
	    mf_rec->server, "%s", buf);
}
