/* $Id$ */

/*
 * mod_fum
 * Module to provide the functionality of kinit(1),
 * kx509(1), and kxlist(1) -p
 *
 * Robert Budden	<rbudden@psc.edu>, <rmb265@psu.edu>
 * Jared Yanovich	<yanovich@psc.edu>
 * Summer 2004-2006 Pittsburgh Supercomputing Center
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

#include <gssapi/gssapi.h>

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

#include "fum.h"

#ifdef STANDALONE
#define apr_palloc(pool, siz)		malloc(siz)
#define apr_pstrdup(pool, str)		strdup(str)
#define apr_env_set(name, val, pool)	setenv(name, val, 1)
#endif

#define FUM_VERSION	"mod_fum/0.2"

#define FPD_PROXIABLE	1
#define FPD_FORWARDABLE	0
#define FPD_LIFETIME	28800 /* 8hrs. default */

#define KX509_HOSTNAME	"certificate"
#define KX509_SERVNAME	"kx509"

/* XXX: read from httpd.conf */
#define _PATH_X509CERT	"/tmp/x509up_fum_u"
#define _PATH_KRB5TKT	"/tmp/krb5cc_fum_"

#define X509_FILE_PERM	0600

#define FUM_USERENV	"REMOTE_USER"

struct fum_cert {
	krb5_deltat	 fc_life;
	int		 fc_fwd;
	int		 fc_proxy;
	const char	*fc_prin;
	const char	*fc_pass;
};

char *fum_keytab;

int do_kx509(int, char *[]);

/*
 * Error logging without errno.
 */
void
fum_logx(request_rec *r, const char *fmt, ...)
{
	va_list ap;
	char *msg;

	va_start(ap, fmt);
#ifdef STANDALONE
	vprintf(fmt, ap);
#else
	msg = apr_pvsprintf(r->pool, fmt, ap);
	if (msg)
		ap_log_error(APLOG_MARK, APLOG_ERR,
		    APR_SUCCESS, r->server, "%s", msg);
#endif
	va_end(ap);
}

/*
 * Error logging with errno.
 */
void
fum_log(request_rec *r, const char *fmt, ...)
{
	int save_errno = errno;
	char *prefix, *msg;
	va_list ap;

	msg = NULL;

	va_start(ap, fmt);
#ifdef STANDALONE
	vprintf(fmt, ap);
#else
	prefix = apr_pvsprintf(r->pool, fmt, ap);
	if (prefix)
		msg = apr_psprintf(r->pool, "%s: %s",
		    prefix, strerror(save_errno));
	if (msg)
		ap_log_error(APLOG_MARK, APLOG_ERR,
		    APR_SUCCESS, r->server, "%s", msg);
#endif
	va_end(ap);
}

/*
 * Parse the principal and get the UID from /etc/passwd.
 * XXX: should the KDC retrieve this?
 */
static int
fum_getuid(request_rec *r, const char *prin, uid_t *uid)
{
	struct passwd *pw;
	char *p, *s;

	/* Parse principal. */
	p = apr_pstrdup(r->pool, prin);
	if (p == NULL) {
		fum_log(r, "fum_getuid: apr_pstrdup");
		return (HTTP_ISE);
	}
	if ((s = strchr(p, '@')) != NULL)
		*s = '\0';
	if ((pw = getpwnam(p)) == NULL) {
		fum_log(r, "fum_getuid: user not in /etc/passwd: %s", p);
		return (HTTP_UNAUTHORIZED);
	}
	*uid = pw->pw_uid;
	return (OK);
}

/*
 * Check for existence of previous credentials.
 */
static int
fum_cert_exists(request_rec *r, const char *certfn)
{
	struct stat stb;

fum_logx(r, "fum_cert_exists: does %s exist?", certfn);
	if (stat(certfn, &stb) == -1) {
		if (errno != ENOENT) {
			fum_log(r, "fum_cert_exists: stat %s",
			    certfn);
			return (HTTP_ISE);
		}
		return (HTTP_UNAUTHORIZED);
	}
	return (OK);
}

/*
 * Initialize a "fum" object.
 *
 * This includes:
 *	- looking up the UID of the authenticating user,
 *	- determining their ticket and certificate locations, and
 *	- calling standard krb5 initialization routines on their behalf.
 *
 * Other members of "struct fum", such as the delegated
 * GSS credential, get filled in from other places.
 *
 * fum_free() properly releases resources on all fields
 * assigned here, and does so properly in all circumstances,
 * regardless of error conditions during fum_new().
 */
static int
fum_new(request_rec *r, struct fum *f, const char *user)
{
	krb5_error_code kerr;
	int error;
	uid_t uid;

	if ((error = fum_getuid(r, user, &uid)) != OK)
		return (error);
	if ((f->f_tktcachefn = apr_psprintf(r->pool, "%s%d",
	    _PATH_KRB5TKT, uid)) == NULL) {
		fum_log(r, "fum: apr_psprintf tkt %s%d",
		    _PATH_KRB5TKT, uid);
		return (HTTP_ISE);
	}
	if ((f->f_certfn = apr_psprintf(r->pool, "%s%d",
	    _PATH_X509CERT, uid)) == NULL) {
		fum_log(r, "fum: apr_psprintf cert %s%d",
		    _PATH_X509CERT, uid);
		return (HTTP_ISE);
	}

	/* Initialize application context. */
	if ((kerr = krb5_init_context(&f->f_ctx)) != 0) {
		fum_logx(r, "fum_new: krb5_init_context failed (%d)", kerr);
		return (HTTP_ISE);
	}
fum_logx(r, "KRB5_INIT_CONTEXT");

	/*
	 * The default cache location may already
	 * occupy a certificate owned by the actual
	 * user, so use our custom location.
	 */
	if ((kerr = krb5_cc_resolve(f->f_ctx, f->f_tktcachefn,
	     &f->f_cache)) != 0) {
		fum_logx(r, "fum_new: cache resolve failed (%s %d)",
		    f->f_tktcachefn, kerr);
		return (HTTP_ISE);
	}
fum_logx(r, "loading f_cache at %s", f->f_tktcachefn);

#if 0
	/*
	 * Generate a full principal name to be used for authentication.
	 */
	if ((kerr = krb5_sname_to_principal(f->f_ctx, NULL, NULL,
	     KRB5_NT_SRV_HST, &f->f_prin)) != 0) {
		fum_logx(r, "fum_valid_user: get principal failed %s (%d)",
		    kerr);
		return (HTTP_ISE);
	}
#endif

	/*
	 * Take the principal name we were given and parse it into the
	 * appropriate form for authentication protocols.
	 */
	if ((kerr = krb5_parse_name(f->f_ctx, user, &f->f_prin)) != 0) {
		fum_logx(r, "fum_valid_user: krb5_parse_name failed %s (%d)",
		    user, kerr);
		return (HTTP_ISE);
	}
fum_logx(r, "parsed name for %s", user);
	return (OK);
}

/*
 * Cleanup fum object.
 */
static void
fum_free(request_rec *r, struct fum *f)
{
	krb5_creds kczero;
	krb5_principal kpzero;

	fum_gss_free(f);

	if (f->f_ctx) {
		memset(&kczero, 0, sizeof(kczero));
		memset(&kpzero, 0, sizeof(kpzero));
		if (memcmp(&f->f_prin, &kpzero, sizeof(kpzero)) != 0)
			krb5_free_principal(f->f_ctx, f->f_prin);
		if (memcmp(&f->f_cred, &kczero, sizeof(kczero)) != 0)
			krb5_free_cred_contents(f->f_ctx, &f->f_cred);
		if (memcmp(&f->f_x509cred, &kczero, sizeof(kczero)) != 0)
			krb5_free_cred_contents(f->f_ctx, &f->f_x509cred);
fum_logx(r, "freeing f_cache");
		krb5_cc_close(f->f_ctx, f->f_cache);
		krb5_free_context(f->f_ctx);
	}
}

/*
 * Perform actions identical to kinit(1).
 */
static int
fum_kinit(request_rec *r, struct fum *f, struct fum_cert *fc)
{
	krb5_get_init_creds_opt opt;
	krb5_error_code kerr;

	/* Set default credential options. */
	krb5_get_init_creds_opt_init(&opt);

	/* Make our changes to the defaults. */
	krb5_get_init_creds_opt_set_forwardable(&opt, fc->fc_fwd);
	krb5_get_init_creds_opt_set_proxiable(&opt, fc->fc_proxy);
	krb5_get_init_creds_opt_set_tkt_life(&opt, fc->fc_life);
	krb5_get_init_creds_opt_set_address_list(&opt, NULL);

	/* Initialize credentials from given password. */
	if ((kerr = krb5_get_init_creds_password(f->f_ctx,
	    &f->f_cred, f->f_prin, apr_pstrdup(r->pool, fc->fc_pass),
	    krb5_prompter_posix, NULL, 0, NULL, &opt)) != 0) {
		fum_log(r, "fum_kinit: auth failed (%d)", kerr);
		return (HTTP_UNAUTHORIZED);
	}

	/* Initialize the cache file. */
	if ((kerr = krb5_cc_initialize(f->f_ctx, f->f_cache,
	     f->f_prin)) != 0) {
		fum_log(r, "initialize cache failed (%d)", kerr);
		return (HTTP_ISE);
	}

	/* Store the credential. */
	if ((kerr = krb5_cc_store_cred(f->f_ctx, f->f_cache,
	     &f->f_cred)) != 0) {
		fum_log(r, "store credentials failed (%d)", kerr);
		return (HTTP_ISE);
	}

	return (OK);
}

/*
 * Perform actions identical to kx509(1).
 */
static int
fum_kx509(request_rec *r, struct fum *f)
{
	char *argv[4];
	int argc, err;

	/*
	 * libkx509's main interface desires setup
	 * like a new process' main().
	 */
	argv[0] = apr_pstrdup(r->pool, "kx509");
	argv[1] = apr_pstrdup(r->pool, "-c");
	argv[2] = apr_pstrdup(r->pool, f->f_tktcachefn);
	argv[3] = NULL;
	argc = 3;

	if ((err = do_kx509(argc, argv)) != KX509_STATUS_GOOD) {
		fum_log(r, "fum_kx509: failure (%d)", err);
		return (HTTP_ISE);
	}
	return (OK);
}

/* Obtain K.X509 credentials from ticket cache. */
static int
fum_kx509_cred(request_rec *r, struct fum *f)
{
	krb5_creds screds, sczero;
	krb5_error_code kerr;
	int error;

	error = HTTP_UNAUTHORIZED;
	memset(&screds, 0, sizeof(screds));
	memset(&sczero, 0, sizeof(sczero));

fum_logx(r, "  kx: get_prin");
	/* The primary principal will be for the client. */
	if ((kerr = krb5_cc_get_principal(f->f_ctx, f->f_cache,
	    &screds.client)) != 0) {
		fum_logx(r, "fum_kx509_cred: "
		    "get client principal failed (%d)", kerr);
		goto done;
	}

fum_logx(r, "  kx: sname_to");
	/* Now obtain one for the server. */
	if ((kerr = krb5_sname_to_principal(f->f_ctx, KX509_HOSTNAME,
	     KX509_SERVNAME, KRB5_NT_UNKNOWN, &screds.server)) != 0) {
		fum_logx(r, "fum_kx509_cred: "
		    "get server principal failed (%d)", kerr);
		error = HTTP_ISE;
		goto done;
	}

fum_logx(r, "  kx: retrieve");
	/* Retrieve the kx509 credentials, search by service name only */
	if ((kerr = krb5_cc_retrieve_cred(f->f_ctx, f->f_cache,
	    KRB5_TC_MATCH_SRV_NAMEONLY, &screds, &f->f_x509cred)) != 0) {
		fum_logx(r, "fum_kx509_cred: "
		    "unable to retrieve kx509 credential (%d)", kerr);
		error = HTTP_ISE;
		goto done;
	}

fum_logx(r, "  kx: greatness");
	error = OK;

done:
fum_logx(r, "  kx: free screds");
	if (memcmp(&screds, &sczero, sizeof(sczero)) != 0)
		krb5_free_cred_contents(f->f_ctx, &screds);
fum_logx(r, "  kx: free cli prin");
	if (memcmp(&screds.client, &sczero.client,
	    sizeof(sczero.client)) != 0)
		krb5_free_principal(f->f_ctx, screds.client);
fum_logx(r, "  kx: free srv prin");
	if (memcmp(&screds.server, &sczero.server,
	    sizeof(sczero.server)) != 0)
		krb5_free_principal(f->f_ctx, screds.server);
fum_logx(r, " RETURN(%d)", error);
	return (error);
}

/*
 * Perform actions identical to kxlist(1) with -p:
 * do crypto and write the K.509 certificate.
 */
static int
fum_kxlist(request_rec *r, struct fum *f)
{
	const unsigned char *data;
	unsigned int klen, clen;
	X509 *cert = NULL;
	RSA *priv = NULL;
	int error;
	FILE *fp;

fum_logx(r, "fum_kxlist: opening %s", f->f_certfn);
	if ((error = fum_kx509_cred(r, f)) != OK)
		return (error);

fum_logx(r, "fum_kxlist: opened");
	if ((fp = fopen(f->f_certfn, "w")) == NULL) {
		fum_log(r, "%s", f->f_certfn);
		return (HTTP_ISE);
	}
	if (fchmod(fileno(fp), X509_FILE_PERM) == -1)
		fum_log(r, "chmod %s", f->f_certfn);

	klen = f->f_x509cred.ticket.length;
	clen = f->f_x509cred.second_ticket.length;

	/* Decode the certificate (we want PEM format). */
	data = (void *)f->f_x509cred.second_ticket.data;
	d2i_X509(&cert, &data, clen);

	/* Extract & decode the RSA private key from the certificate. */
	data = (void *)f->f_x509cred.ticket.data;
	d2i_RSAPrivateKey(&priv, &data, klen);

	if (priv == NULL) {
		fum_logx(r, "d2i_RSAPrivateKey failed"); // XXX errmsg
		remove(f->f_certfn);
		fclose(fp);
		return (HTTP_ISE);
	}

	/* Write the certificate, appropriately formatted. */
	PEM_write_X509(fp, cert);
	PEM_write_RSAPrivateKey(fp, priv, NULL, NULL, 0, NULL, NULL);
	fclose(fp);
	return (OK);
}

/*
 * Check if the user/pass combo is valid.
 */
static int
fum_valid_user(request_rec *r, struct fum *f, const char *password)
{
	krb5_get_init_creds_opt opt;
	krb5_error_code kerr;

	krb5_get_init_creds_opt_init(&opt);

	/*
	 * Try to get an initial ticket.  If this succeeds,
	 * then the username/password is correct.
	 */
	if ((kerr = krb5_get_init_creds_password(f->f_ctx,
	    &f->f_cred, f->f_prin, apr_pstrdup(r->pool, password),
	    krb5_prompter_posix, NULL, 0, NULL, &opt)) != 0) {
		fum_logx(r, "bad authentication (%d)", kerr);
		return (HTTP_UNAUTHORIZED);
	}
	return (OK);
}

int
fum_main(request_rec *r, struct fum *f, const char *principal,
    const char *password)
{
	struct fum_cert fc;
	int error;

fum_logx(r, "fum_main: kinit/gss");
	if (password) {
		fc.fc_proxy = FPD_PROXIABLE;
		fc.fc_fwd = FPD_FORWARDABLE;
		fc.fc_life = FPD_LIFETIME;
		fc.fc_prin = principal;
		fc.fc_pass = password;

		if ((error = fum_kinit(r, f, &fc)) != OK)
			goto done;
	} else  {
fum_logx(r, "fum_main: gss");
		if ((error = fum_gss_storecred(r, f)) != OK)
			goto done;
	}
fum_logx(r, "fum_main: kx509");
	if ((error = fum_kx509(r, f)) != OK)
		goto done;
fum_logx(r, "fum_main: kxlist");
	if ((error = fum_kxlist(r, f)) != OK)
		goto done;

#if 0
	/* Tomcat doesn't receive this. */
	if ((err = apr_env_set(FUM_USERENV,
	    principal, r->pool)) != OK)
		return (err);
#endif

done:
	return (error);
}

/*
 * Check certificate expiration.
 */
static int
fum_valid_cred(struct fum *f)
{
	if (time(NULL) < f->f_x509cred.times.endtime)
		return (OK);
	else
		return (HTTP_UNAUTHORIZED);
}

/*
 * Remove Kerberos ticket and X.509 certificate.
 */
static void
fum_cert_destroy(request_rec *r, struct fum *f)
{
	if (remove(f->f_tktcachefn) == -1)
		fum_log(r, "fum_cert_destroy: remove %s", f->f_tktcachefn);
	if (remove(f->f_certfn) == -1)
		fum_log(r, "fum_cert_destroy: remove %s", f->f_certfn);
}

/*
 * Apache authentication hook.
 */
int
fum_auth(request_rec *r)
{
	const char *auth, *name, *type, *pass;
	int error, allow_neg;
	struct fum f;
	char *user;

fum_logx(r, "fum_main: handle?");
	if ((type = ap_auth_type(r)) == NULL ||
	    strcasecmp(type, "fum") != 0)
		return (DECLINED);
fum_logx(r, "fum_main: yes");

	memset(&f, 0, sizeof(f));
	fum_gss_init(&f);
	allow_neg = 1;
	error = HTTP_UNAUTHORIZED;

	if ((auth = apr_table_get(r->headers_in,
	    "Authorization")) == NULL)
		goto done;
fum_logx(r, "fum_main: read Authorization");
	allow_neg = 0;
	if ((type = ap_getword_white(r->pool, &auth)) == NULL)
		goto done;
	if (strcasecmp(type, "Basic") == 0) {
fum_logx(r, "fum_main: handling Basic Authentication");
		pass = ap_pbase64decode(r->pool, auth);
		user = ap_getword(r->pool, &pass, ':');
	} else if (strcasecmp(type, "Negotiate") == 0) {
fum_logx(r, "fum_main: handling Negotiate Authentication");
		/* Give user one chance with Negotiate. */
		pass = NULL;
		if ((error = fum_gss(r, &f, auth, &user)) != OK)
			goto done;
	} else
{
fum_logx(r, "fum_main: no acceptable Authentication type given");
		goto done;
}
	if ((error = fum_new(r, &f, user)) != OK)
		goto done;

	/* Check if previous credentials exist. */
	if (fum_cert_exists(r, f.f_certfn) != OK) {
fum_logx(r, "fum_main: no credentials, creating new");
		/* Create new certs. */
		error = fum_main(r, &f, user, pass);
		goto done;
	}
fum_logx(r, "fum_main: credentials exist, checking for validity");

	/*
	 * They exist, make sure the user/pass is correct;
	 * otherwise, a correct username and wrong password
	 * will work if old credentials exist.
	 *
	 * If no password, assume GSS checked out.
	 */
	if (pass && (error = fum_valid_user(r, &f, pass)) != OK) {
		fum_logx(r, "wrong user/pass combination; user: %s", user);
		goto done;
	}

	/*
	 * Finally check if the credentials have expired
	 * or not.  mod_fum assumes that if the Kerberos
	 * ticket (created by mod_fum) is valid, then the
	 * X.509 ticket is as well.
	 */
	if ((error = fum_valid_cred(&f)) == OK)
		goto done;

/* May also be a permissions problem. */
fum_logx(r, "credentials expired, removing and recreating");

	/* Try to remove old certs */
	fum_cert_destroy(r, &f);

	/* Create new certs */
	error = fum_main(r, &f, user, pass);

done:
fum_logx(r, "fum_main: finishing");
	fum_free(r, &f);
fum_logx(r, "fum_main: freed");
	if (error) {
fum_logx(r, "fum_main: adding Authentication header");
		if (allow_neg)
			apr_table_add(r->err_headers_out,
			    "WWW-Authenticate", "Negotiate");
		name = ap_auth_name(r);
		apr_table_add(r->err_headers_out, "WWW-Authenticate",
		    apr_pstrcat(r->pool, "Basic realm=\"", name, "\"", NULL));
	}
fum_logx(r, "fum_main: returning");
	return (error);
}

#ifdef APXS1
/* XXX do Apache 1 stuff */
#else
/* Register hooks in Apache. */
void
fum_hooks(apr_pool_t *p)
{
# ifndef STANDALONE
	/* We need to be the first to intercept the password. */
	ap_hook_check_user_id(fum_auth, NULL, NULL, APR_HOOK_MIDDLE);
	ap_add_version_component(p, FUM_VERSION);
# endif /* STANDALONE */
}

static const command_rec fum_cmd[] = {
	AP_INIT_TAKE1("FumKeytab", ap_set_file_slot, &fum_keytab,
	    OR_AUTHCFG | RSRC_CONF, "Location of KerberosV5 keytab."),
	{ NULL, { NULL }, NULL, 0, 0, NULL }
};

/* Apache 2.x lmodule record, handlers, & hooks */
module fum_module = {
	STANDARD20_MODULE_STUFF,
	NULL,				/* create per-httpd.conf */
	NULL,				/* merge per-httpd.conf */
	NULL,				/* create pre-.htaccess */
	NULL,				/* merge pre-.htaccess */
	fum_cmd,			/* conf commands */
	fum_hooks			/* hooks */
};
#endif
