/* $Id$ */

/*
-------------------------------------------------------------------
** mod_fum - Free Unadulterated Moderation (for kerberos)
** Free Apache Module to provide the functionality of kinit,
** kx509, and kxlist -p ...
**
** Robert Budden
** rbudden@psc.edu
-------------------------------------------------------------------
*/

/*
XXX Things todo still:
	1) ap_uname2id(char*) - use this to obtain user id?

	2) handle errors appropriately, allow gridfe page to explain what
		went wrong... return error codes, can't just use exit();
	

	X) mf_get_uid_from_ticket_cache really isn't needed... we could
		save the uid from what we got from passwd, but for now
		just keep this in case code gets switched later, we don't
		have to rely on anything except the ticket cache name.

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
static int mf_kinit_setup(krb5_inst_ptr, krb5_prefs_ptr);
static void mf_kinit_cleanup(krb5_inst_ptr);
static void mf_kinit_set_uap(krb5_prefs_ptr, const char*, const char*);
static void mf_kinit_set_defaults(krb5_prefs_ptr);
static int mf_kinit(krb5_inst_ptr, krb5_prefs_ptr);
static int mf_user_id_from_principal(const char*, char**);
static int mf_kx509(const char*);
static int mf_kxlist(const char*);
static int mf_kxlist_setup(krb5_inst_ptr);
static int mf_kxlist_crypto(krb5_inst_ptr, char*);
static int mf_krb5_init(krb5_inst_ptr kinst, const char*);
static void mf_krb5_free(krb5_inst_ptr kinst);
static char* mf_get_uid_from_ticket_cache(const char*);
static char* mf_dstrslice(const char*, int, int);
static char* mf_dstrcat(const char*, const char*);
static char* mf_dstritoa(int);
static request_rec* mf_request(request_rec*);
static apr_pool_t* mf_pool(apr_pool_t*);
static void mod_fum_hooks(apr_pool_t*);
static int mf_check_for_credentials(const char*);
static int mf_valid_user(const char*, const char*);
static int mf_valid_credentials(char *);
int mod_fum_auth(request_rec*);
int do_kx509(int, char**);
int mf_main(const char*, const char*);

/* Apache (2.X only!) module record, handlers, & hooks */
module fum_module =
{
	STANDARD20_MODULE_STUFF,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	mod_fum_hooks,
};

/* Register hooks in Apache */
static void mod_fum_hooks(apr_pool_t *p)
{
	/* We need to be the first to intercept the password */
	ap_hook_check_user_id(mod_fum_auth, NULL, NULL, APR_HOOK_FIRST);

	ap_add_version_component(p, kModuleVersion);
}

/* Apache Authentication Hook */
int mod_fum_auth(request_rec *r)
{
	char *user;
	int err;
	const char *pass = NULL;

	/* Save request rec */
	mf_save_pool(r->pool);
	mf_save_request(r);
	
	/*
	** Get user/pass - NOTE: ap_get_basic_auth_pw()
	** must be called first!!! otherwise r->user will
	** be NULL!!
	*/
	err = ap_get_basic_auth_pw(r, &pass);
	user = r->user;

	if(err == OK && user && pass)
	{

		/* Check if previous credentials exist */
		if(mf_check_for_credentials(user))
		{

			/*
			** If they exist, make sure the user/pass
			** is correct... otherwise a correct username
			** and wrong password will work!!!
			*/
			if(mf_valid_user(user, pass))
			{
				/*
				** Finally check if the Credentials
				** have expired or not. If so create
				** new certs, if not, do nothing
				*/
				if(mf_valid_credentials(user))
					/* Create new certs */
					err = mf_main(user, pass);
				else
					mf_err("credentials expired", 1);
			}
			else
			{
				mf_err("wrong user/pass combination", 1);
				err = HTTP_UNAUTHORIZED;
			}
		}
		else
			/* Create new certs */
			err = mf_main(user, pass);
	}
	else
	{
		mf_err("authentication form incomplete", err);
		err = HTTP_UNAUTHORIZED;
	}

	return err;
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
	int err;

	/* XXX Resolve UID from KDC with given principal (posible??) */

	/* Read uid from /etc/passwd */
	err = mf_user_id_from_principal(principal, &uid);

	if(err == OK)
	{
		tkt_cache = mf_dstrcat(kKrb5DefaultFile, uid);

		if(tkt_cache)
		{

			/* ----------- KINIT ----------- */

			/* kinit - requires only principal/password */
			err = mf_krb5_init(&kinst, tkt_cache);
			if(err != OK)
				goto RET;

			mf_kinit_set_defaults(&kprefs);
			mf_kinit_set_uap(&kprefs, principal, password);

			err = mf_kinit_setup(&kinst, &kprefs);
			if(err != OK)
				goto RET;
		
			/* kinit -c /tmp/krb5cc_$UID */
			err = mf_kinit(&kinst, &kprefs);
			if(err != OK)
				goto RET;
		
			mf_kinit_cleanup(&kinst);
			mf_krb5_free(&kinst);
		
			/* ----------- KX509 ----------- */
			
			/* kx509 - just call the kx509lib*/
			err = mf_kx509(tkt_cache);
			if(err != OK)
				goto RET;
		
			/* ----------- KXLIST ----------- */
		
			/* kxlist -p */
			err = mf_kxlist(tkt_cache);
			if(err != OK)
				goto RET;
		}
		else
		{
			mf_err("tkt_cache is Null", 1);
			err = HTTP_INTERNAL_SERVER_ERROR;
		}
	}

	RET:	
	return err;
}

/*
-------------------------------KXLIST------------------------------
*/
static int mf_kxlist(const char *tkt_cache)
{
	krb5_inst kinst;
	char *uid;
	char *name;
	int err;

	/* 
	** Parse tkt_cache name to get uid:
	** /tmp/krb5cc_UID_FUBAR
	** /tmp/krb5cc_UID
	*/
	uid = mf_get_uid_from_ticket_cache(tkt_cache);

	if(uid)
	{
		/* krb5 initial context setup */
		err = mf_krb5_init(&kinst, tkt_cache);

		if(err == OK)
		{
			/* Obtain proper kx509 credentials */
			err = mf_kxlist_setup(&kinst);

			if(err != OK)
				goto RET;

			/* Perform Crypto & write Certficate */
			name = mf_dstrcat(kX509DefaultFile, uid);

			if(!name)
			{
				err = HTTP_INTERNAL_SERVER_ERROR;
				goto RET;
			}

			err = mf_kxlist_crypto(&kinst, name);
			mf_krb5_free(&kinst);
		}
	}
	else
	{
		err = HTTP_INTERNAL_SERVER_ERROR;
		goto RET;
	}

	RET:
	return err;
}

/*
** Perform crypto and write the X.509 Certificate
*/
static int mf_kxlist_crypto(krb5_inst_ptr kinst, char *name)
{
	unsigned int klen;
	unsigned int clen;
	unsigned char *data;
	FILE *file;
	int err = OK;
	/* Must be set! */
	RSA *priv = NULL;
	X509 *cert = NULL;

	file = fopen(name, "w");
	if(file)
	{

		klen = kinst->credentials.ticket.length;
		clen = kinst->credentials.second_ticket.length;
	
		/* Decode the Certifcate (we want PEM format) */
		data = kinst->credentials.second_ticket.data;
		d2i_X509((X509**)(&cert), &data, clen);

		/* Extract & decode the RSA Private Key from the certifcate */
		data = kinst->credentials.ticket.data;
		d2i_RSAPrivateKey(&priv, (const unsigned char**)(&data), klen);

		if(priv)
		{
			/* write the certificate appropriately formated */
			PEM_write_X509(file, cert);
			PEM_write_RSAPrivateKey(file, priv, NULL, NULL, 0, NULL, NULL);
			
			fclose(file);
		
			/* Set proper permissions */
			chmod(name, kX509FilePermissions);
		}
		else
		{
			mf_err("d2i_RSAPrivateKey failed", 1);
			err = HTTP_INTERNAL_SERVER_ERROR;
		}
	}
	else
	{
		mf_err("mem error", 1);
		err = HTTP_INTERNAL_SERVER_ERROR;
	}

	return err;
}


/*
** Load the kx509 credentials
*/
static int mf_kxlist_setup(krb5_inst_ptr kinst)
{
	krb5_error_code err;
	krb5_creds screds;

	/* just make sure!! (had match problems before) */
	memset(&screds, '\0', sizeof(krb5_creds));

	/* The primary principal will be for the client */
	err = krb5_cc_get_principal(kinst->context,
					kinst->cache,
					&screds.client);	
	if(err)
	{
		mf_err("get client principal failed", err);
		goto RET;
	}
	
	/* Now obtain one for the server */
	err = krb5_sname_to_principal(kinst->context,
					kKX509HostName,
					kKX509ServiceName,
					KRB5_NT_UNKNOWN,
					&screds.server);
	if(err)
	{
		mf_err("get server principal failed", err);
		goto RET;
	}

	/* Retrieve the kx509 credentials, search by Service Name Only! */
	err = krb5_cc_retrieve_cred(kinst->context,
					kinst->cache,
					KRB5_TC_MATCH_SRV_NAMEONLY,
					&screds,
					&kinst->credentials);
	if(err)
	{
		mf_err("unable to retrieve kx509 credential", err);
		goto RET;
	}
	
	krb5_free_principal(kinst->context, screds.server);
	krb5_free_principal(kinst->context, screds.client);

	RET:
	return KrbToApache(err);
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
static int mf_user_id_from_principal(const char *principal, char **uid)
{
	struct passwd *pw;
	int i, j;
	char *p;
	int err = 0;

	/* parse principal */
	for(i = 0, j = 0; i < strlen(principal); i++, j++)
	{
		if(principal[i] == '@')
			break;
	}

	/* slice username */
	p = mf_dstrslice(principal, 0, j - 1);

	if(p)
	{
		/* read the passwd file */
		pw = getpwnam(p);
		
		if(pw)
		{
			/* 
			** convert uid to (char*), 
			** (first snprintf gives the size)
			*/
			(*uid) = mf_dstritoa(pw->pw_uid);
		}
		else
		{
			mf_err("User not in /etc/passwd", 1);
			err = HTTP_UNAUTHORIZED;
		}
	}
	else
	{
		mf_err("principal slice error", 1);
		err = HTTP_INTERNAL_SERVER_ERROR;
	}

	return err;
}


/*
-------------------------------KX509-------------------------------
*/

static int mf_kx509(const char *tkt_cache)
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
	
	return Kx509ToApache(err);
}


/*
-------------------------------KINIT-------------------------------
*/

/*
** Perform the functionality of kinit...
*/
static int mf_kinit(krb5_inst_ptr kinst, krb5_prefs_ptr kprefs)
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

	/* Create credentials from given password */
	err = krb5_get_init_creds_password(kinst->context, &kinst->credentials,
		kinst->principal, (char*)(kprefs->password),
		krb5_prompter_posix, NULL, 0, NULL, &opt);
	if(err)
	{
		mf_err("get initial credentials failed", err);
		goto RET;
	}
		

	/* Initialize the cache file */
	err = krb5_cc_initialize(kinst->context, kinst->cache, kinst->principal);
	if(err)
	{
		mf_err("initialize cache failed", err);
		goto RET;
	}
	
	
	/* Store the Credential */
	err = krb5_cc_store_cred(kinst->context, kinst->cache, &kinst->credentials);
	if(err)
		mf_err("store credentials failed", err);

	RET:
	return KrbToApache(err);
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
static int mf_krb5_init(krb5_inst_ptr kinst, const char *tkt_cache)
{
	krb5_error_code err; 

	/* Important!! segfaults without this!! */
	memset(&kinst->credentials, '\0', sizeof(krb5_creds));

	/* Initialize Application Context */
	err = krb5_init_context(&kinst->context);
	if(!err)
	{
	
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
	else
		mf_err("krb5_init_context failed", err);
	
	return KrbToApache(err);
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
static int mf_kinit_setup(krb5_inst_ptr kinst, krb5_prefs_ptr kprefs)
{
	krb5_error_code err; 

	/*
	** Generate a full principal name
	** to be used for authentication.
	*/
	err = krb5_sname_to_principal(kinst->context,
		NULL, NULL, KRB5_NT_SRV_HST, &kinst->principal);
	if(err)
	{
		mf_err("create principal failed", err);
		goto RET;
	}
	
	/*
	** Take the principal name we were given and parse it
	** into the appropriate form for authentication protocols
	*/
	err = krb5_parse_name(kinst->context, kprefs->pname, &kinst->principal);
	if(err)
		mf_err("parse_name failed", err);
	
	RET:
	return KrbToApache(err);
}

static void mf_kinit_cleanup(krb5_inst_ptr kinst)
{
	if(kinst->principal)
		krb5_free_principal(kinst->context, kinst->principal);
	if(kinst->context)
		krb5_free_context(kinst->context);
}

/*
** Check for previous credentials in /tmp
*/
static int mf_check_for_credentials(const char *principal)
{
	char *uid;
	char *cert;
	int found = 0;
	int err;
	DIR *dir;
	struct dirent *d;

	/* Read uid from /etc/passwd */
	err = mf_user_id_from_principal(principal, &uid);

	if(err == OK)
	{
		/* Create cert name */
		cert = mf_dstrcat(kCredentialFileName, uid);

		if(cert)
		{
			/* Does the file exist */
			dir = opendir(kCredentialPath);

			while(dir)
			{
				if((d = readdir(dir)) == NULL)
					break;
				if(strcmp(d->d_name, cert) == 0)
				{
					found = 1;
					break;
				}
			}

			closedir(dir);
		}
		else
			mf_err("error creating cert string", 1);
	}
	else
		mf_err("error finding uid", err);

	return found;
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
static int mf_valid_credentials(char *principal)
{
	char *uid;
	krb5_inst kinst;
	krb5_timestamp end;
	int err;
	char *tkt_cache;
	int valid = 0;
	
	err = mf_user_id_from_principal(principal, &uid);

	if(err == OK)
	{
		tkt_cache = mf_dstrcat(kKrb5DefaultFile, uid);

		if(tkt_cache)
		{
			err = mf_krb5_init(&kinst, tkt_cache);

			if(err != OK)
				goto RET;

			/* Grab the kx509 credentials */
			err = mf_kxlist_setup(&kinst);

			if(err != OK)
				goto RET;

			/* Get the expiration time of the cert */
			end = kinst.credentials.times.endtime;

			/* Compare with our time now */
			if(time(NULL) < end)
				valid = 1;
		}
	}
	else
		mf_err("uid failed", err);

	RET:
	return valid;
}

/*
** Check if the user/pass is valid
** (for now just simulate a kerberos authentication)
**
** XXX - There must be a better way to validate the
** authenticity of the user... 
** XXX - if this is the best way
** to do this, then i need to try and find a more modular
** way, because much of this code is similar to kinit...
*/
static int mf_valid_user(const char *principal, const char *password)
{
	int err;
	krb5_inst kinst;
	krb5_prefs kprefs;
	krb5_get_init_creds_opt opt;
	int valid = 0;
	char tkt[] = "/tmp/mod-fum-tmp";

	err = mf_krb5_init(&kinst, tkt);

	if(err == OK)
	{
		mf_kinit_set_uap(&kprefs, principal, password);
	
		err = KrbToApache(mf_kinit_setup(&kinst, &kprefs));

		if(err == OK)
		{
			krb5_get_init_creds_opt_init(&opt);

			/* Try and get an intial ticket */
			err = krb5_get_init_creds_password(kinst.context,
							&kinst.credentials,
							kinst.principal,
							(char*)(kprefs.password),
							krb5_prompter_posix,
							NULL,
							0,
							NULL,
							&opt);

			/* If this succeeds, then the user/pass is correct */
			if(err == OK)
				valid = 1;
			else
				mf_err("bad authentication", err);
		}
		else
			mf_err("mf_kinit_setup failed", err);

		mf_kinit_cleanup(&kinst);
		mf_krb5_free(&kinst);
	}
	else
		mf_err("krb5_init failed", err);

	return valid;
}



/*
-------------------------------DSTR-------------------------------
*/

/*
** dynamic strcat routine and malloc wrapper
*/
static char* mf_dstrcat(const char *s1, const char *s2)
{
	char *s;
	size_t len;

	len = strlen(s1) + strlen(s2) + 1;
	s = (char*)(apr_palloc(mf_get_pool(), sizeof(char)*len));

	if(s)
	{
		strncpy(s, s1, strlen(s1));
		/* strncpy is being a bitch null terminate ourselves */
		s[strlen(s1)] = '\0';
		strncat(s, s2, strlen(s2));
		s[len-1] = '\0';
	}
	else
		mf_err("malloc failed", 1);

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
				s2 = NULL;
		}
	}	
	
	return s2;
}

/*
** dynamic integer to ascii conversion
*/
static char* mf_dstritoa(int l)
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
