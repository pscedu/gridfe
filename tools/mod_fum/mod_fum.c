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

#include "mod_fum.h"

static apr_pool_t	*mf_pool(apr_pool_t *);
static char		*mf_dstrcat(const char *, const char *);
static char		*mf_dstritoa(int);
static char		*mf_dstrslice(const char *, int, int);
static char		*mf_get_uid_from_ticket_cache(const char *);
static int		 mf_check_for_credentials(const char *);
static int		 mf_kinit(krb5_inst_ptr, krb5_prefs_ptr);
static int		 mf_kinit_setup(krb5_inst_ptr, krb5_prefs_ptr);
static int		 mf_krb5_init(krb5_inst_ptr kinst, const char *);
static int		 mf_kx509(const char *);
static int		 mf_kxlist(const char *);
static int		 mf_kxlist_crypto(krb5_inst_ptr, char *);
static int		 mf_kxlist_setup(krb5_inst_ptr);
static int		 mf_user_id_from_principal(const char *, char **);
static int		 mf_valid_credentials(char *);
static int		 mf_valid_user(const char *, const char *);
static request_rec	*mf_request(request_rec *);
static void		 mf_kinit_cleanup(krb5_inst_ptr);
static void		 mf_kinit_set_defaults(krb5_prefs_ptr);
static void		 mf_kinit_set_uap(krb5_prefs_ptr, const char *, const char *);
static void		 mf_krb5_free(krb5_inst_ptr kinst);
static void		 mod_fum_hooks(apr_pool_t *);

int do_kx509(int, char **);
int mf_main(const char *, const char *);
int mod_fum_auth(request_rec *);

/* Apache 2.x lmodule record, handlers, & hooks */
module fum_module = {
	STANDARD20_MODULE_STUFF,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	mod_fum_hooks,
};

/* Register hooks in Apache */
static void
mod_fum_hooks(apr_pool_t *p)
{
	/* We need to be the first to intercept the password */
	ap_hook_check_user_id(mod_fum_auth, NULL, NULL, APR_HOOK_FIRST);
	ap_add_version_component(p, kModuleVersion);
}

/* Apache authentication hook */
int
mod_fum_auth(request_rec *r)
{
	const char *pass = NULL;
	char *user;
	int err;

	/* Save request rec */
	mf_save_pool(r->pool);
	mf_save_request(r);

	/*
	** Get user/pass - NOTE: ap_get_basic_auth_pw() must be called
	** first, otherwise r->user will be NULL.
	*/
	err = ap_get_basic_auth_pw(r, &pass);
	user = r->user;

	/* 
	 * Check
	 */
	if (err == OK && user && pass) {
		err = HTTP_UNAUTHORIZED;

		/* Check if previous credentials exist */
		if (mf_check_for_credentials(user)) {
			/*
			** If they exist, make sure the user/pass
			** is correct... otherwise a correct username
			** and wrong password will work!!!
			*/
			if (mf_valid_user(user, pass)) {
				/*
				** Finally check if the credentials
				** have expired or not. If so create
				** new certs, if not, do nothing
				*/
				if (mf_valid_credentials(user))
					/* Create new certs */
					err = mf_main(user, pass);
				else
					/*
					** XXX This could also be a permissions
					** problem... If the user has already
					** a valid X.509 certificate, apache
					** does not have permissions to read
					** it!!
					*/
					mf_err("credentials expired", 1);
			} else
				mf_err("wrong user/pass combination", 1);
		} else
			/* Create new certs */
			err = mf_main(user, pass);
	} else {
		mf_err("authentication form incomplete", err);
		err = HTTP_UNAUTHORIZED;
	}
	return (err);
}

/*
** Save and retrieve pool (useful for ap_mallocs!)
** #define mf_save_pool(x) mf_pool(x)
** #define mf_get_pool() mf_pool(NULL)
*/
static apr_pool_t *
mf_pool(apr_pool_t *p)
{
	static apr_pool_t *pool = NULL;

	if (p != NULL)
		pool = p;
	return (pool);
}

/*
** Save and retrieve request record (useful for ap_log_err())
** #define mf_save_request(x) mf_request(x)
** #define mf_get_request() mf_request(NULL)
*/
static request_rec *
mf_request(request_rec *r)
{
	static request_rec *rec = NULL;

	if (r != NULL)
		rec = r;
	return (rec);
}

int
mf_main(const char *principal, const char *password)
{
	krb5_inst kinst;
	krb5_prefs kprefs;
	char *tkt_cache, *uid;
	int err;

	/* XXX Resolve UID from KDC with given principal (possible?) */

	/* Read uid from /etc/passwd */
	err = mf_user_id_from_principal(principal, &uid);
	if (err == OK) {
		tkt_cache = mf_dstrcat(kKrb5DefaultFile, uid);
		if (tkt_cache == NULL) {
			mf_err("tkt_cache is NULL", 1);
			err = HTTP_INTERNAL_SERVER_ERROR;
		} else {
			err = mf_krb5_init(&kinst, tkt_cache);
			if (err != OK)
				goto bail;

			mf_kinit_set_defaults(&kprefs);
			mf_kinit_set_uap(&kprefs, principal, password);

			err = mf_kinit_setup(&kinst, &kprefs);
			if (err != OK)
				goto bail;

			/* kinit -c /tmp/krb5cc_$UID */
			err = mf_kinit(&kinst, &kprefs);
			if (err != OK)
				goto bail;

			mf_kinit_cleanup(&kinst);
			mf_krb5_free(&kinst);

			err = mf_kx509(tkt_cache);
			if (err != OK)
				goto bail;

			/* kxlist -p */
			err = mf_kxlist(tkt_cache);
			if (err != OK)
				goto bail;
		}
	}
bail:
	return (err);
}

static int
mf_kxlist(const char *tkt_cache)
{
	krb5_inst kinst;
	char *uid, *name;
	int err;

	/*
	** Parse tkt_cache name to get UID:
	** /tmp/krb5cc_UID_FUBAR
	** /tmp/krb5cc_UID
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
			name = mf_dstrcat(kX509DefaultFile, uid);
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
** Perform crypto and write the X.509 certificate
*/
static int mf_kxlist_crypto(krb5_inst_ptr kinst, char *name)
{
	unsigned int klen, clen;
	unsigned char *data;
	FILE *file;
	int err = OK;
	RSA *priv = NULL;
	X509 *cert = NULL;

	if ((file = fopen(name, "w")) == NULL) {
		mf_err("mem error", 1);
		return (HTTP_INTERNAL_SERVER_ERROR);
	} else {
		klen = kinst->credentials.ticket.length;
		clen = kinst->credentials.second_ticket.length;

		/* Decode the certificate (we want PEM format) */
		data = kinst->credentials.second_ticket.data;
		d2i_X509((X509**)(&cert), &data, clen);

		/* Extract & decode the RSA Private Key from the certificate */
		data = kinst->credentials.ticket.data;
		d2i_RSAPrivateKey(&priv, (const unsigned char **)(&data), klen);

		if (priv) {
			/* Write the certificate appropriately formatted */
			PEM_write_X509(file, cert);
			PEM_write_RSAPrivateKey(file, priv, NULL, NULL, 0, NULL,
			    NULL);
			(void)fclose(file);

			/* Set proper permissions */
			(void)chmod(name, kX509FilePermissions);
		} else {
			mf_err("d2i_RSAPrivateKey failed", 1);
			err = HTTP_INTERNAL_SERVER_ERROR;
		}
	}
	return (err);
}

/*
** Load the kx509 credentials
*/
static int
mf_kxlist_setup(krb5_inst_ptr kinst)
{
	krb5_error_code err;
	krb5_creds screds;

	memset(&screds, '\0', sizeof(krb5_creds));

	/* The primary principal will be for the client */
	err = krb5_cc_get_principal(kinst->context, kinst->cache,
	    &screds.client);
	if (err) {
		mf_err("get client principal failed", err);
		goto bail;
	}

	/* Now obtain one for the server */
	err = krb5_sname_to_principal(kinst->context, kKX509HostName,
	    kKX509ServiceName, KRB5_NT_UNKNOWN, &screds.server);
	if (err) {
		mf_err("get server principal failed", err);
		goto bail;
	}

	/* Retrieve the kx509 credentials, search by Service Name Only! */
	err = krb5_cc_retrieve_cred(kinst->context, kinst->cache,
	    KRB5_TC_MATCH_SRV_NAMEONLY, &screds, &kinst->credentials);
	if (err) {
		mf_err("unable to retrieve kx509 credential", err);
		goto bail;
	}

	krb5_free_cred_contents(kinst->context, &screds);

bail:
	return (KrbToApache(err));
}

/*
** Parse the user ID from the ticket_cache name
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
		mf_err("uid slice error", 1);
	return (uid);
}

/*
** Parse the principal and get the uid either from the KDC
** (XXX if that is even possible!) or just read from /etc/passwd
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
			mf_err("User not in /etc/passwd", 1);
			err = HTTP_UNAUTHORIZED;
		} else
			/*
			** convert uid to (char *),
			** (first snprintf gives the size)
			*/
			(*uid) = mf_dstritoa(pw->pw_uid);
	} else {
		mf_err("principal slice error", 1);
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
	argv[0] = apr_pstrdup(mf_get_pool(), "kx509");
	argv[1] = apr_pstrdup(mf_get_pool(), "-c");
	argv[2] = apr_pstrdup(mf_get_pool(), tkt_cache);
	argc = 3;

	/* simply run kx509 */
	err = do_kx509(argc, argv);

	if (err != KX509_STATUS_GOOD)
		mf_err("kx509 failed", err);

	return (Kx509ToApache(err));
}

static int
mf_kinit(krb5_inst_ptr kinst, krb5_prefs_ptr kprefs)
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
		kinst->principal, (char *)(kprefs->password),
		krb5_prompter_posix, NULL, 0, NULL, &opt);
	if (err) {
		mf_err("get initial credentials failed", err);
		goto bail;
	}

	kinst->initialized = 1;

	/* Initialize the cache file */
	err = krb5_cc_initialize(kinst->context, kinst->cache,
	    kinst->principal);
	if (err) {
		/*
		** In testing, this is thrown with the error -1765328188
		** when there are already credentials created and mod_fum
		** does not have the proper permissions to read them!
		*/
		mf_err("initialize cache failed", err);
		goto bail;
	}

	/* Store the credential */
	err = krb5_cc_store_cred(kinst->context, kinst->cache,
	    &kinst->credentials);
	if (err)
		mf_err("store credentials failed", err);

bail:
	return (KrbToApache(err));
}

/* Set the user (principal) and password */
static void
mf_kinit_set_uap(krb5_prefs_ptr kprefs, const char *principal,
		 const char *password)
{
	kprefs->password = password;
	kprefs->pname = principal;
}

static void
mf_kinit_set_defaults(krb5_prefs_ptr kprefs)
{
	kprefs->proxiable = kProxiable;
	kprefs->forwardable = kForwardable;
	kprefs->lifetime = kLifetime;
}

/*
** Handle standard krb5 initial functions
*/
static int
mf_krb5_init(krb5_inst_ptr kinst, const char *tkt_cache)
{
	krb5_error_code err;

	memset(&kinst->credentials, '\0', sizeof(krb5_creds));
	kinst->initialized = 0;

	/* Initialize application context */
	err = krb5_init_context(&kinst->context);
	if (err)
		mf_err("krb5_init_context failed", err);
	else {
		/*
		** Don't use the default!! we need to be able to
		** write the tkt out with different uid's for each
		** individual user...
		*/
		//err = krb5_cc_default(kinst->context, &kinst->cache);
		err = krb5_cc_resolve(kinst->context, tkt_cache, &kinst->cache);
		if (err)
			mf_err("default cache failed", err);
	}

	return (KrbToApache(err));
}

static void
mf_krb5_free(krb5_inst_ptr kinst)
{
	if (kinst->initialized)
		krb5_free_cred_contents(kinst->context, &kinst->credentials);

	if (kinst->cache)
		krb5_cc_close(kinst->context, kinst->cache);
}

/*
** Kinit initial setup
*/
static int
mf_kinit_setup(krb5_inst_ptr kinst, krb5_prefs_ptr kprefs)
{
	krb5_error_code err;

	/*
	** Generate a full principal name
	** to be used for authentication.
	*/
	err = krb5_sname_to_principal(kinst->context, NULL, NULL,
	    KRB5_NT_SRV_HST, &kinst->principal);
	if (err) {
		mf_err("create principal failed", err);
		goto bail;
	}

	/*
	** Take the principal name we were given and parse it
	** into the appropriate form for authentication protocols
	*/
	err = krb5_parse_name(kinst->context, kprefs->pname, &kinst->principal);
	if (err)
		mf_err("parse_name failed", err);

bail:
	return (KrbToApache(err));
}

static void
mf_kinit_cleanup(krb5_inst_ptr kinst)
{
	if (kinst->principal)
		krb5_free_principal(kinst->context, kinst->principal);
	if (kinst->context)
		krb5_free_context(kinst->context);
}

/*
** Check for previous credentials in /tmp
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
		cert = mf_dstrcat(kCredentialFileName, uid);
		if (cert) {
			/* Does the file exist */
			dp = opendir(kCredentialPath);
			while (dp != NULL) {
				if ((d = readdir(dp)) == NULL)
					break;
				if (strcmp(d->d_name, cert) == 0) {
					found = 1;
					break;
				}
			}
			(void)closedir(dp);
		} else
			mf_err("error creating cert string", 1);
	} else
		mf_err("error finding uid", err);
	return (found);
}

/*
** Check if the certificate has expired or not by
** finding the kx509 certificate and checking it's
** expiration time, compared with the current time.
**
** XXX - This code is so similar to stuff from mf_main
** and kxlist, that there has to be a nice way to break
** some of it down to be more modular and compact...
*/
static int
mf_valid_credentials(char *principal)
{
	char *uid, *tkt_cache;
	krb5_inst kinst;
	krb5_timestamp end;
	int err, valid = 0;

	err = mf_user_id_from_principal(principal, &uid);
	if (err == OK) {
		tkt_cache = mf_dstrcat(kKrb5DefaultFile, uid);
		if (tkt_cache) {
			err = mf_krb5_init(&kinst, tkt_cache);
			if (err != OK)
				goto bail;

			/* Grab the kx509 credentials */
			err = mf_kxlist_setup(&kinst);
			if (err != OK)
				goto bail;

			/* Get the expiration time of the cert */
			end = kinst.credentials.times.endtime;

			/* Compare with our time now */
			if (time(NULL) < end)
				valid = 1;
			mf_krb5_free(&kinst);
		}
	} else
		mf_err("uid failed", err);

bail:
	return (valid);
}

/*
** Check if the user/pass is valid
** (for now just simulate a Kerberos authentication)
**
** XXX - There must be a better way to validate the
** authenticity of the user...
** XXX - if this is the best way
** to do this, then i need to try and find a more modular
** way, because much of this code is similar to kinit...
*/
static int
mf_valid_user(const char *principal, const char *password)
{
	char tkt[] = "/tmp/mod-fum-tmp";
	krb5_get_init_creds_opt opt;
	krb5_prefs kprefs;
	krb5_inst kinst;
	int err;

	err = mf_krb5_init(&kinst, tkt);
	kinst.initialized = 0;

	if (err == OK) {
		mf_kinit_set_uap(&kprefs, principal, password);
		err = KrbToApache(mf_kinit_setup(&kinst, &kprefs));
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
				mf_err("bad authentication", err);
		} else
			mf_err("mf_kinit_setup failed", err);

		mf_kinit_cleanup(&kinst);
		mf_krb5_free(&kinst);
	} else
		mf_err("krb5_init failed", err);
	return (kinst.initialized);
}

/*
** dynamic strcat routine and malloc wrapper
*/
static char * mf_dstrcat(const char *s1, const char *s2)
{
	char *s;
	size_t len;

	len = strlen(s1) + strlen(s2) + 1;
	s = (char *)(apr_palloc(mf_get_pool(), sizeof(char)*len));

	if (s) {
		strncpy(s, s1, strlen(s1));
		s[strlen(s1)] = '\0';
		strncat(s, s2, strlen(s2));
		s[len-1] = '\0';
	} else
		mf_err("malloc failed", 1);

	return s;
}

/*
** dynamic string routing to slice a section
*/
static char * mf_dstrslice(const char *s, int x, int y)
{
	char *s2;
	size_t len, slen;

	slen = strlen(s);
	len = y - x + 2;

	if(len)
	{
		s2 = (char *)(apr_palloc(mf_get_pool(), sizeof(char)*len));

		if(!s2)
			mf_err("malloc failed", 1);

		if(x >= 0 && y >= 0)
		{
			if(x < slen && y < slen)
			{
				/* ptr increment to start location */
				s += x;

				strncpy(s2, s, len - 1);
				s2[len-1] = '\0';
			}
			else
				s2 = NULL;
		}
	}

	return s2;
}

/*
** dynamic integer to ascii conversion
*/
static char * mf_dstritoa(int l)
{
	char *ascii;
	int i, j;

	i = snprintf(NULL, 0, "%d", l);
	j = (i+1)*sizeof(char);
	ascii = apr_palloc(mf_get_pool(), j);
	snprintf(ascii, j, "%d", l);
	ascii[i] = '\0';

	return ascii;
}
