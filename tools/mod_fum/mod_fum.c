/* $Id$ */

/*
-------------------------------------------------------------------
** Free Apache Module to provide the functionality of kinit,
** kx509, and kxlist -p ...
**
** Robert Budden
** rbudden@psc.edu
-------------------------------------------------------------------
*/

/*
XXX Things todo still:
	1) test module, fix any errors

XXX Developement Notes:
	1) currently only users with user account can use authenticate.
		mod_fum requires a uid lookup from /etc/passwd to write
		the x.509 certificates in /tmp. possibly try and find
		a way to have the kdc give us a uid for users that do
		not have local accounts...

	2) since mod_fum runs under apache the env X509_USER_PROXY cannot
		be read, therefore X.509 Certificates will be created at
		the default location (/tmp/x509u_u####). Users who require
		$X509_USER_PROXY to be set must do this manually if they
		plan on loging into the machine remotely (via ssh, etc...)
	3) Apache 2.X series support only! (1.X could be added, but is
		currently not needed for this project) Version 1.X changed
		enough functions, data types, etc. that it was not worth
		the time to try and support both types at the moment.
*/


/* Includes */
#include<stdio.h>
#include<string.h>
#include<stdlib.h>
#include<krb5.h>
#include<kx509.h>
#include<dlfcn.h>
#include<err.h>
#include<openssl/rsa.h>
#include<openssl/x509v3.h>
#include<openssl/pem.h>
#include<sys/stat.h>
#include<pwd.h>

#include"httpd.h"
#include"http_config.h"
#include"http_core.h"
#include"http_log.h"
#include"http_main.h"
#include"http_protocol.h"
#include"http_request.h"
#include"apr_strings.h"

#include"mod_fum.h"

/* Prototypes */
static void mf_kinit_setup(krb5_inst_ptr, krb5_prefs_ptr);
static void mf_kinit_cleanup(krb5_inst_ptr);
static void mf_kinit_set_uap(krb5_prefs_ptr, const char*, const char*);
static void mf_kinit_set_defaults(krb5_prefs_ptr);
static void mf_kinit(krb5_inst_ptr, krb5_prefs_ptr);
static void mf_user_id_from_principal(const char *principal, char **uid);
static void mf_kx509(const char*);
static void mf_kxlist(const char *);
static void mf_kxlist_setup(krb5_inst_ptr);
static void mf_kxlist_crypto(krb5_inst_ptr, char*);
static void mf_krb5_init(krb5_inst_ptr kinst, const char*);
static void mf_krb5_free(krb5_inst_ptr kinst);
static char* mf_get_uid_from_ticket_cache(const char*);
static char* mf_dstrslice(const char*, int, int);
static char* mf_dstrcat(const char*, const char*);
static request_rec* mf_request(request_rec *r);
static apr_pool_t* mf_pool(apr_pool_t *p);
static void mod_fum_hooks(apr_pool_t *p);
int mod_fum_auth(request_rec *r);
int do_kx509(int, char**);

//DEBUG
void mod_fum_err(char *str, int err);


/* Apache (2.X only!) module record, handlers, & hooks */
module fum_module =
{
	STANDARD20_MODULE_STUFF,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL, /* command table */
	mod_fum_hooks,
};

/* Register hooks in Apache */
static void mod_fum_hooks(apr_pool_t *p)
{
	ap_hook_check_user_id(mod_fum_auth, NULL, NULL, APR_HOOK_FIRST);
	//ap_hook_check_user_id(mod_fum_auth, NULL, NULL, APR_HOOK_MIDDLE);
}

//DEBUG
void mod_fum_err(char *str, int err)
{
	FILE *fp;
	fp = fopen("/tmp/rbudden-mod-fum", "a");
	fprintf(fp, "%s : error %d\n", str, err);
	fclose(fp);
}

/* Apache Authentication Hook */
int mod_fum_auth(request_rec *r)
{
	char *user;
	int err = OK;
	const char *pass = NULL;

	//DEBUG
	mod_fum_err("\nentered mod_fum_auth", 1);

//	auth_data_ptr data = (auth_data_ptr)
//		ap_module_config(r->per_dir_coinfig, &fum_module);

	/* Save request rec */
	mf_save_pool(r->pool);
	mf_save_request(r);
	
	//DEBUG
	mod_fum_err("pool/request saved", 1);

	/*
	** Get user/pass - NOTE: ap_get_basic_auth_pw()
	** must be called first!!! otherwise r->user will
	** be NULL!!
	*/
	err = ap_get_basic_auth_pw(r, &pass);
	user = r->user;

	//DEBUG
	mod_fum_err("user/pass", 1);
	mod_fum_err(user, 1);
	mod_fum_err(pass, 1);

	if(err != OK)
		mf_err("error retrieving password", err);
	
	if(!user || !pass)
	{
		mf_err("err obtaining username/password NULL", 1);
		return HTTP_UNAUTHORIZED;
		
		//DEBUG - DECLINED allows the request to go through to apache's passwd file
		//return DECLINED;
	}
	
	/* Create Certificate */
	//DEBUG
	mod_fum_err("create cert", 1);
	return HTTP_UNAUTHORIZED;

	mf_main(user, pass);

	mod_fum_err("cert created", 1);
	
	// XXX HTTP_ACCEPTED or HTTP_CREATED???
	return HTTP_ACCEPTED;
}

/*
** Save and Retrieve Pool (usefull for ap_mallocs!)
** #define mf_save_pool(x) mf_pool(x)
** #define mf_get_pool() mf_pool(NULL)
*/
static apr_pool_t* mf_pool(apr_pool_t *p)
{
	static apr_pool_t *pool = NULL;

	if(p)
		pool = p;
	
	return pool;
}

/*
** Save and Retrieve Request Record (usefull for ap_log_err())
** #define mf_save_request(x) mf_request(x)
** #define mf_get_request() mf_request(NULL)
*/
static request_rec* mf_request(request_rec *r)
{
	static request_rec *rec = NULL;

	if(r)
		rec = r;
	
	return rec;
}

/*
** mod_fum main... should be called by Apache
** with a username (principal) and password
*/
int mf_main(const char *principal, const char *password)
{
	krb5_inst kinst;
	krb5_prefs kprefs;
	char *tkt_cache;
	char *uid;

	/* XXX Resolve UID from KDC with given principal (posible??) */

	/* Read uid from /etc/passwd */
	mf_user_id_from_principal(principal, &uid);
	tkt_cache = mf_dstrcat(kKrb5DefaultFile, uid);

	/* ----------- KINIT ----------- */

	/* kinit - requires only principal/password */
	mf_krb5_init(&kinst, tkt_cache);
	mf_kinit_set_defaults(&kprefs);
	mf_kinit_set_uap(&kprefs, principal, password);
	mf_kinit_setup(&kinst, &kprefs);

	/* kinit -c /tmp/krb5cc_$UID */
	mf_kinit(&kinst, &kprefs);

	mf_kinit_cleanup(&kinst);
	mf_krb5_free(&kinst);

	/* ----------- KX509 ----------- */
	
	/* kx509 - just call the kx509lib*/
	mf_kx509(tkt_cache);

	/* ----------- KXLIST ----------- */
	
	/* kxlist -p */
	mf_kxlist(tkt_cache);
	
	return 0;
}

/*
-------------------------------KXLIST------------------------------
*/
static void mf_kxlist(const char *tkt_cache)
{
	krb5_inst kinst;
	char *uid;
	char *name;

	/* 
	** Parse tkt_cache name to get uid:
	** /tmp/krb5cc_UID_FUBAR
	** /tmp/krb5cc_UID
	*/
	uid = mf_get_uid_from_ticket_cache(tkt_cache);
	
	/* krb5 initial context setup */
	mf_krb5_init(&kinst, tkt_cache);
	
	/* Obtain proper kx509 credentials */
	mf_kxlist_setup(&kinst);

	/* Perform Crypto & write Certficate */
	name = mf_dstrcat(kX509DefaultFile, uid);
	
	mf_kxlist_crypto(&kinst, name);

	mf_krb5_free(&kinst);
}

/*
** Perform crypto and write the X.509 Certificate
*/
static void mf_kxlist_crypto(krb5_inst_ptr kinst, char *name)
{
	unsigned int klen;
	unsigned int clen;
	unsigned char *data;
	FILE *file;
	/* Must be set! */
	RSA *priv = NULL;
	X509 *cert = NULL;

	file = fopen(name, "w");
	if(!file)
		mf_err("mem error", 1);

	klen = kinst->credentials.ticket.length;
	clen = kinst->credentials.second_ticket.length;

	/* Decode the Certifcate (we want PEM format) */
	data = kinst->credentials.second_ticket.data;
	d2i_X509((X509**)(&cert), &data, clen);

	/* Extract & decode the RSA Private Key from the certifcate */
	data = kinst->credentials.ticket.data;
	d2i_RSAPrivateKey(&priv, (const unsigned char**)(&data), klen);

	if(!priv)
		mf_err("d2i_RSAPrivateKey failed", 1);

	/* write the certificate appropriately formated */
	PEM_write_X509(file, cert);
	PEM_write_RSAPrivateKey(file, priv, NULL, NULL, 0, NULL, NULL);
	
	fclose(file);

	/* Set proper permissions */
	chmod(name, kX509FilePermissions);
}


/*
** Load the kx509 credentials
*/
static void mf_kxlist_setup(krb5_inst_ptr kinst)
{
	krb5_error_code err;
	krb5_creds screds;

	/* just to make sure... */
	memset(&screds, '\0', sizeof(krb5_creds));

	/* The primary principal will be for the client */
	err = krb5_cc_get_principal(kinst->context,
					kinst->cache,
					&screds.client);	
	if(err)
		mf_err("get client principal failed", err);
	
	/* Now obtain one for the server */
	err = krb5_sname_to_principal(kinst->context,
					kKX509HostName,
					kKX509ServiceName,
					KRB5_NT_UNKNOWN,
					&screds.server);
	if(err)
		mf_err("get server principal failed", err);

	/* Retrieve the kx509 credentials search by Service Name Only! */
	err = krb5_cc_retrieve_cred(kinst->context,
					kinst->cache,
					KRB5_TC_MATCH_SRV_NAMEONLY,
					&screds,
					&kinst->credentials);
	if(err)
		mf_err("unable to retrieve kx509 credential", err);
	
	krb5_free_principal(kinst->context, screds.server);
	krb5_free_principal(kinst->context, screds.client);
}

/*
** Parse the user id from the ticket_cache name
*/
static char* mf_get_uid_from_ticket_cache(const char *tkt)
{
	int i, j;
	int b, e;
	char *uid;

	/* default to end of string */
	e = strlen(tkt) - 1;
	
	/* Grab the boundry of the uid */
	for(i = 0, j = 0; i < (strlen(tkt) - 1) && j < 2; i++)
	{
		if(tkt[i] == '_')
		{
			if(!j)
				b = i + 1;
			else if(j)
				e = i;
			j++;
		}
	}


	/* slice and convert the uid */
	uid = mf_dstrslice(tkt, b, e);

	if(!uid)
		mf_err("uid slice error", 1);

	return uid;
}

/*
** Parse the principal and get the uid either from the KDC
** (if that is even possible!) or just read from /etc/passwd
*/
static void mf_user_id_from_principal(const char *principal, char **uid)
{
	struct passwd *pw;
	int i, j;
	char *p;

	/* parse principal */
	for(i = 0, j = 0; i < strlen(principal); i++, j++)
	{
		if(principal[i] == '@')
			break;
	}

	/* slice username */
	p = mf_dstrslice(principal, 0, j - 1);

	if(!p)
		mf_err("principal slice error", 1);

	/* read the passwd file */
	pw = getpwnam(p);

	if(!pw)
		mf_err("User not in /etc/passwd", 1);

	/* convert uid to (char*), (first snprintf gives the size) */
	i = snprintf(NULL, 0, "%d", pw->pw_uid);
	j = (i+1)*sizeof(char);
	*uid = apr_palloc(mf_get_pool(), j);
	snprintf(*uid, j, "%d", pw->pw_uid);
	(*uid)[i] = '\0';
}


/*
-------------------------------KX509-------------------------------
*/

static void mf_kx509(const char *tkt_cache)
{
	char *argv[3];
	int argc;
	int err;

	/* setup kx509 as would be called from command line */
	argv[0] = apr_pstrdup(mf_get_pool(), "kx509");
	argv[1] = apr_pstrdup(mf_get_pool(), "-c");
	argv[2] = apr_pstrdup(mf_get_pool(), tkt_cache);
	argc = 3;

	/* simply run kx509 */
	err = do_kx509(argc, argv);

	if(err != KX509_STATUS_GOOD) 
		mf_err("kx509 failed", err);
}


/*
-------------------------------KINIT-------------------------------
*/

/*
** Perform the functionality of kinit...
*/
static void mf_kinit(krb5_inst_ptr kinst, krb5_prefs_ptr kprefs)
{
	krb5_error_code err;
	krb5_get_init_creds_opt opt;

	/* Set default credential options? */
	krb5_get_init_creds_opt_init(&opt);

	/* Make our changes to the defaults */
	krb5_get_init_creds_opt_set_forwardable(&opt, kprefs->forwardable);
	krb5_get_init_creds_opt_set_proxiable(&opt, kprefs->proxiable);
	krb5_get_init_creds_opt_set_tkt_life(&opt, kprefs->lifetime);
	krb5_get_init_creds_opt_set_address_list(&opt, NULL);

	/* Create credentials from give password, or prompt for password */
	err = krb5_get_init_creds_password(kinst->context, &kinst->credentials,
		kinst->principal, (char*)(kprefs->password),
		krb5_prompter_posix, NULL, 0, NULL, &opt);
	if(err)
		mf_err("get initial credentials failed", err);
		

	/* Initialize the cache file */
	err = krb5_cc_initialize(kinst->context, kinst->cache, kinst->principal);
	if(err)
		mf_err("initialize cache failed", err);
	
	
	/* Store the Credential */
	err = krb5_cc_store_cred(kinst->context, kinst->cache, &kinst->credentials);
	if(err)
		mf_err("store credentials failed", err);

}

/* Set the user (principal) and password */
static void mf_kinit_set_uap(krb5_prefs_ptr kprefs,
				const char *principal,
				const char *password)
{
	kprefs->password = password;
	kprefs->pname = principal;
}

static void mf_kinit_set_defaults(krb5_prefs_ptr kprefs)
{
	kprefs->proxiable = kProxiable;
	kprefs->forwardable = kForwardable;
	kprefs->lifetime = kLifetime;
}

/*
** Handle standard krb5 inital functions
*/
static void mf_krb5_init(krb5_inst_ptr kinst, const char *tkt_cache)
{
	krb5_error_code err; 

	/* Initialize Application Context */
	err = krb5_init_context(&kinst->context);
	if(err)
		mf_err("krb5_init_context failed", err);
	
	/*
	** Don't use the default!! we need to be able to
	** write the tkt out with different uid's for each
	** individual user...
	*/
	//err = krb5_cc_default(kinst->context, &kinst->cache);
	err = krb5_cc_resolve(kinst->context, tkt_cache, &kinst->cache);
	if(err)
		mf_err("default cache failed", err);
}

static void mf_krb5_free(krb5_inst_ptr kinst)
{
	if(&kinst->credentials)
		krb5_free_cred_contents(kinst->context, &kinst->credentials);
	if(kinst->cache)
		krb5_cc_close(kinst->context, kinst->cache);
}

/*
** Kinit initial setup
*/
static void mf_kinit_setup(krb5_inst_ptr kinst, krb5_prefs_ptr kprefs)
{
	krb5_error_code err; 

	/*
	** Generate a full principal name
	** to be used for authentication.
	*/
	err = krb5_sname_to_principal(kinst->context,
		NULL, NULL, KRB5_NT_SRV_HST, &kinst->principal);
	if(err)
		mf_err("create principal failed", err);
	
	/*
	** Take the principal name we were given and parse it
	** into the appropriate form for authentication protocols
	*/
	err = krb5_parse_name(kinst->context, kprefs->pname, &kinst->principal);
	if(err)
		mf_err("parse_name failed", err);
}

static void mf_kinit_cleanup(krb5_inst_ptr kinst)
{
	if(kinst->principal)
		krb5_free_principal(kinst->context, kinst->principal);
	if(kinst->context)
		krb5_free_context(kinst->context);
}

/*
** dynamic strcat routine and malloc wrapper
*/
static char* mf_dstrcat(const char *s1, const char *s2)
{
	char *s;
	size_t len;

	len = strlen(s1) + strlen(s2) + 1;
	s = (char*)(apr_palloc(mf_get_pool(), sizeof(char)*len));

	if(!s)
		mf_err("malloc failed", 1);

	strncpy(s, s1, strlen(s1));
	/* strncpy is being a bitch null terminate ourselves */
	s[strlen(s1)] = '\0';
	strncat(s, s2, strlen(s2));
	s[len-1] = '\0';

	return s;
}

/*
** dynamic string routing to slice a section 
*/
static char* mf_dstrslice(const char *s, int x, int y)
{
	char *s2;
	size_t len, slen;

	slen = strlen(s);
	len = y - x + 2;

	if(len)
	{
		s2 = (char*)(apr_palloc(mf_get_pool(), sizeof(char)*len));

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
			{
				s2 = NULL;
			}
		}
	}	
	
	return s2;
}
