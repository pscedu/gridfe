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
#include <err.h>
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
	krb5_context	 ki_context;
	krb5_ccache	 ki_cache;
	krb5_principal	 ki_principal;
	krb5_creds	 ki_credentials;
	int		 ki_initialized;
};

struct krb5_prefs {
	krb5_deltat	 kp_lifetime;
	int		 kp_forwardable;
	int		 kp_proxiable;
	const char	*kp_pname;
	const char	*kp_password;
};

static char	*mf_dstrcat(const char *, const char *);
static char	*mf_dstritoa(int);
static char	*mf_dstrslice(const char *, int, int);
static char	*mf_get_uid_from_ticket_cache(const char *);
static int	 mf_check_for_credentials(const char *);
static int	 mf_kinit(struct krb5_inst *, struct krb5_prefs *);
static int	 mf_kinit_setup(struct krb5_inst *, struct krb5_prefs *);
static int	 mf_krb5_init(struct krb5_inst *kinst, const char *);
static int	 mf_kx509(const char *);
static int	 mf_kxlist(const char *);
static int	 mf_kxlist_crypto(struct krb5_inst *, char *);
static int	 mf_kxlist_setup(struct krb5_inst *);
static int	 mf_user_id_from_principal(const char *, char **);
static int	 mf_valid_credentials(char *);
static int	 mf_valid_user(const char *, const char *);
static void	 mf_kinit_cleanup(struct krb5_inst *);
static void	 mf_krb5_free(struct krb5_inst *);
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
	mf_req = r;

	/*
	 * Get user/pass - NOTE: ap_get_basic_auth_pw() must be called
	 * first, otherwise r->user will be NULL.
	 */
	err = ap_get_basic_auth_pw(r, &pass);
	user = r->user;

	if (err == OK && user && pass) {
		err = HTTP_UNAUTHORIZED;

		/* Check if previous credentials exist */
		if (mf_check_for_credentials(user)) {
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
				if (mf_valid_credentials(user))
					/* Create new certs */
					err = mod_fum_main(user, pass);
				else
					/*
					 * XXX This could also be a permissions
					 * problem.  If the user has already a
					 * valid X.509 certificate, Apache does
					 * not have permissions to read it.
					 */
					mf_log("credentials expired", 1);
			} else
				mf_log("wrong user/pass combination", 1);
		} else
			/* Create new certs */
			err = mod_fum_main(user, pass);
	} else {
		mf_log("authentication form incomplete", err);
		err = HTTP_UNAUTHORIZED;
	}
	return (err);
}

int
mod_fum_main(const char *principal, const char *password)
{
	struct krb5_prefs kprefs;
	struct krb5_inst kinst;
	char *tkt_cache, *uid;
	int err;

	/* XXX Resolve UID from KDC with given principal (possible?) */

	/* Read uid from /etc/passwd */
	if ((err = mf_user_id_from_principal(principal, &uid)) != OK)
		return (err);
	if ((tkt_cache = mf_dstrcat(_PATH_KRB5CERT, uid)) == NULL) {
		mf_log("tkt_cache is NULL", 1);
		return (HTTP_INTERNAL_SERVER_ERROR);
	}

	if ((err = mf_krb5_init(&kinst, tkt_cache)) != OK)
		return (err);

	kprefs.kp_proxiable = PROXIABLE;
	kprefs.kp_forwardable = FORWARDABLE;
	kprefs.kp_lifetime = LIFETIME;
	kprefs.kp_principal = principal;
	kprefs.kp_password = password;

	if ((err = mf_kinit_setup(&kinst, &kprefs)) != OK)
		return (err);

	/* kinit -c /tmp/krb5cc_$UID */
	if ((err = mf_kinit(&kinst, &kprefs)) != OK)
		return (err);

	mf_kinit_cleanup(&kinst);
	mf_krb5_free(&kinst);

	if ((err = mf_kx509(tkt_cache)) != OK)
		return (err);

	/* kxlist -p */
	return (mf_kxlist(tkt_cache));
}

static int
mf_kxlist(const char *tkt_cache)
{
	char *uid, *name;
	struct krb5_inst kinst;
	int err;

	/*
	 * Parse tkt_cache name to get UID:
	 * /tmp/krb5cc_UID_FUBAR
	 * /tmp/krb5cc_UID
	 */
	uid = mf_get_uid_from_ticket_cache(tkt_cache);
	if (uid) {
		/* krb5 initial context setup */
		err = mf_krb5_init(&kinst, tkt_cache);
		if (err == OK) {
			/* Obtain proper kx509 credentials */
			if ((err = mf_kxlist_setup(&kinst)) != OK)
				return (err);

			/* Perform crypto & write certificate */
			name = mf_dstrcat(_PATH_KX509CERT, uid);
			if (name == NULL)
				return (HTTP_INTERNAL_SERVER_ERROR);
			err = mf_kxlist_crypto(&kinst, name);
			mf_krb5_free(&kinst);
		}
	} else
		return (HTTP_INTERNAL_SERVER_ERROR);
	return (err);
}

/*
 * Perform crypto and write the X.509 certificate
 */
static int
mf_kxlist_crypto(struct krb5_inst *kinst, char *name)
{
	unsigned int klen, clen;
	unsigned char *data;
	X509 *cert = NULL;
	RSA *priv = NULL;
	int err = OK;
	FILE *file;

	if ((file = fopen(name, "w")) == NULL) {
		mf_log("mem error", 1);
		return (HTTP_INTERNAL_SERVER_ERROR);
	} else {
		klen = kinst->credentials.ticket.length;
		clen = kinst->credentials.second_ticket.length;

		/* Decode the certificate (we want PEM format) */
		data = kinst->credentials.second_ticket.data;
		d2i_X509((X509**)(&cert), &data, clen);

		/* Extract & decode the RSA private key from the certificate */
		data = kinst->credentials.ticket.data;
		d2i_RSAPrivateKey(&priv, (const unsigned char **)(&data), klen);

		if (priv == NULL) {
			mf_log("d2i_RSAPrivateKey failed", 1);
			err = HTTP_INTERNAL_SERVER_ERROR;
		} else {
			/* Write the certificate appropriately formatted */
			PEM_write_X509(file, cert);
			PEM_write_RSAPrivateKey(file, priv, NULL, NULL, 0, NULL,
			    NULL);
			(void)fclose(file);

			/* Set proper permissions */
			(void)chmod(name, KX509_FILE_PERM);
		}
	}
	return (err);
}

/*
 * Load the kx509 credentials
 */
static int
mf_kxlist_setup(struct krb5_inst *kinst)
{
	krb5_error_code err;
	krb5_creds screds;

	memset(&screds, '\0', sizeof(krb5_creds));

	/* The primary principal will be for the client */
	if ((err = krb5_cc_get_principal(kinst->context, kinst->cache,
	    &screds.client)) != KRB5KDC_ERR_NONE) {
		mf_log("get client principal failed", err);
		return (HTTP_UNAUTHORIZED);
	}

	/* Now obtain one for the server */
	if ((err = krb5_sname_to_principal(kinst->context, KKX509_HOSTNAME,
	     KKX509_SERVNAME, KRB5_NT_UNKNOWN, &screds.server)) !=
	    KRB5KDC_ERR_NONE) {
		mf_log("get server principal failed", err);
		return (HTTP_UNAUTHORIZED);
	}

	/* Retrieve the kx509 credentials, search by Service Name Only! */
	if ((err = krb5_cc_retrieve_cred(kinst->context, kinst->cache,
	     KRB5_TC_MATCH_SRV_NAMEONLY, &screds, &kinst->credentials)) !=
	    KRB5KDC_ERR_NONE)
	if (err) {
		mf_log("unable to retrieve kx509 credential", err);
		return (HTTP_UNAUTHORIZED);
	}
	krb5_free_cred_contents(kinst->context, &screds);
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
		mf_log("uid slice error", 1);
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
			mf_log("User not in /etc/passwd", 1);
			err = HTTP_UNAUTHORIZED;
		} else
			/*
			 * convert uid to (char *),
			 * (first snprintf gives the size)
			 */
			(*uid) = mf_dstritoa(pw->pw_uid);
	} else {
		mf_log("principal slice error", 1);
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
		mf_log("kx509 failed", err);
	return (err ? HTTP_INTERNAL_SERVER_ERROR : OK);
}

static int
mf_kinit(struct krb5_inst *kinst, struct krb5_prefs *kprefs)
{
	krb5_get_init_creds_opt opt;
	krb5_error_code err;

	/* Set default credential options? */
	krb5_get_init_creds_opt_init(&opt);

	/* Make our changes to the defaults */
	krb5_get_init_creds_opt_set_forwardable(&opt, kprefs->forwardable);
	krb5_get_init_creds_opt_set_proxiable(&opt, kprefs->proxiable);
	krb5_get_init_creds_opt_set_tkt_life(&opt, kprefs->lifetime);
	krb5_get_init_creds_opt_set_address_list(&opt, NULL);

	/* Create credentials from given password */
	err = krb5_get_init_creds_password(kinst->context, &kinst->credentials,
	    kinst->principal, (char *)(kprefs->password), krb5_prompter_posix,
	    NULL, 0, NULL, &opt);
	if (err != KRB5KDC_ERR_NONE) {
		mf_log("get initial credentials failed", err);
		return (HTTP_UNAUTHORIZED);
	}

	kinst->initialized = 1;

	/* Initialize the cache file */
	err = krb5_cc_initialize(kinst->context, kinst->cache,
	    kinst->principal);
	if (err) {
		/*
		 * In testing, this is thrown with the error -1765328188
		 * when there are already credentials created and mod_fum
		 * does not have the proper permissions to read them!
		 */
		mf_log("initialize cache failed", err);
		return (HTTP_UNAUTHORIZED);
	}

	/* Store the credential */
	err = krb5_cc_store_cred(kinst->context, kinst->cache,
	    &kinst->credentials);
	if (err)
		mf_log("store credentials failed", err);
	return (err == KRB5KDC_ERR_NONE ? OK : HTTP_UNAUTHORIZED);
}

/*
 * Handle standard krb5 initial functions
 */
static int
mf_krb5_init(struct krb5_inst *kinst, const char *tkt_cache)
{
	krb5_error_code err;

	memset(&kinst->credentials, '\0', sizeof(krb5_creds));
	kinst->initialized = 0;

	/* Initialize application context */
	if ((err = krb5_init_context(&kinst->context)) == KRB5KDC_ERR_NONE) {
		/*
		 * Don't use the default.  We need to be able to
		 * write the tkt out with different uid's for each
		 * individual user.
		 */
		/* err = krb5_cc_default(kinst->context, &kinst->cache); */
		err = krb5_cc_resolve(kinst->context, tkt_cache, &kinst->cache);
		if (err)
			mf_log("default cache failed", err);
	} else
		mf_log("krb5_init_context failed", err);
	return (err == KRB5KDC_ERR_NONE ? OK : HTTP_UNAUTHORIZED);
}

static void
mf_krb5_free(struct krb5_inst *kinst)
{
	if (kinst->initialized)
		krb5_free_cred_contents(kinst->context, &kinst->credentials);
	if (kinst->cache)
		krb5_cc_close(kinst->context, kinst->cache);
}

/*
 * Kinit initial setup
 */
static int
mf_kinit_setup(struct krb5_inst *kinst, struct krb5_prefs *kprefs)
{
	krb5_error_code err;

	/*
	 * Generate a full principal name
	 * to be used for authentication.
	 */
	err = krb5_sname_to_principal(kinst->context, NULL, NULL,
	    KRB5_NT_SRV_HST, &kinst->principal);
	if (err) {
		mf_log("create principal failed", err);
		return (HTTP_UNAUTHORIZED);
	}

	/*
	 * Take the principal name we were given and parse it
	 * into the appropriate form for authentication protocols
	 */
	err = krb5_parse_name(kinst->context, kprefs->pname, &kinst->principal);
	if (err)
		mf_log("parse_name failed", err);
	return (err == KRB5KDC_ERR_NONE ? OK : HTTP_UNAUTHORIZED);
}

static void
mf_kinit_cleanup(struct krb5_inst *kinst)
{
	if (kinst->principal)
		krb5_free_principal(kinst->context, kinst->principal);
	if (kinst->context)
		krb5_free_context(kinst->context);
}

/*
 * Check for previous credentials in /tmp
 */
static int
mf_check_for_credentials(const char *principal)
{
	char *uid, *cert;
	int err, found = 0;
	struct dirent *d;
	DIR *dp;

	/* Read uid from /etc/passwd */
	err = mf_user_id_from_principal(principal, &uid);
	if (err == OK) {
		/* Create cert name */
		cert = mf_dstrcat(_PATH_CERT_FILE, uid);
		if (cert) {
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
		} else
			mf_log("error creating cert string", 1);
	} else
		mf_log("error finding uid", err);
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
mf_valid_credentials(char *principal)
{
	char *uid, *tkt_cache;
	struct krb5_inst kinst;
	krb5_timestamp end;

	if ((err = mf_user_id_from_principal(principal, &uid)) != OK) {
		mf_log("uid failed", err);
		return (0);
	}
	if ((tkt_cache = mf_dstrcat(_PATH_KRB5CERT, uid)) != NULL) {
		if (mf_krb5_init(&kinst, tkt_cache) != OK)
			return (0);

		/* Grab the kx509 credentials */
		if (mf_kxlist_setup(&kinst) != OK)
			return (0);

		/* Get the expiration time of the cert */
		end = kinst.credentials.times.endtime;
		mf_krb5_free(&kinst);

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
	struct krb5_prefs kprefs;
	struct krb5_inst kinst;
	int err;

	err = mf_krb5_init(&kinst, tkt);
	kinst.initialized = 0;

	if (err == OK) {
		kprefs.kp_principle = principal;
		kprefs.kp_password = password;
		if ((err = mf_kinit_setup(&kinst, &kprefs)) == KRB5KDC_ERR_NONE)
			err = OK;
		else
			err = HTTP_UNAUTHORIZED
		if (err == OK) {
			krb5_get_init_creds_opt_init(&opt);

			/* Try and get an initial ticket */
			err = krb5_get_init_creds_password(kinst.context,
			    &kinst.credentials, kinst.principal,
			    (char *)(kprefs.password), krb5_prompter_posix,
			    NULL, 0, NULL, &opt);

			/* If this succeeds, then the user/pass is correct */
			if (err == OK)
				kinst.initialized = 1;
			else
				mf_log("bad authentication", err);
		} else
			mf_log("mf_kinit_setup failed", err);
		mf_kinit_cleanup(&kinst);
		mf_krb5_free(&kinst);
	} else
		mf_log("krb5_init failed", err);
	return (kinst.initialized);
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
		mf_log("malloc failed", 1);
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
			mf_log("malloc failed", 1);
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

/*
 * dynamic integer to ascii conversion
 */
static char *
mf_dstritoa(int l)
{
	char *ascii;
	int i, j;

	i = snprintf(NULL, 0, "%d", l);
	j = (i+1)*sizeof(char);
	ascii = apr_palloc(mf_pool, j);
	snprintf(ascii, j, "%d", l);
	ascii[i] = '\0';
	return (ascii);
}

static void
mf_log(const char *fmt, ...)
{
	char buf[BUFSIZ];
	va_list ap;

	va_start(ap, fmt);
	(void)vsnprintf(buf, sizeof(buf), fmt, ap);
	va_end(ap);

	ap_log_error(APLOG_MARK, APLOG_ERR, (apr_status_t)(NULL),
	    mf_req->server, "%s", buf);
}
