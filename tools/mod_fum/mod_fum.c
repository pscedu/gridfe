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
XXX When deployed:
	1) change mf_err() for apache log
	2) change mf_dstrcpy() for apache malloc
	3) change mf_dstrfree() for apache free

XXX Developement Notes:
	1) currently all certificates are created under apache, therefore
		they will all have the uid of the user apache runs as...
		this is a problem because we really need the users real
		uid, even if they do not have a local account set up.
		somehow, we need the kdc machine to tell us the uid if
		that's even possible...

NOTES:
	1) since mod_fum runs under apache the env X509_USER_PROXY cannot
		be read, therefore X.509 Certificates will be created at
		the default location (/tmp/x509u_u####). Users who require
		$X509_USER_PROXY to be set must do this manually.
*/

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
#include"mod_fum.h"

static void mf_kinit_setup(krb5_inst_ptr, krb5_prefs_ptr);
static void mf_kinit_cleanup(krb5_inst_ptr);
static void mf_kinit_set_uap(krb5_prefs_ptr, const char*, const char*);
static void mf_kinit_set_defaults(krb5_prefs_ptr);
static void mf_kinit(krb5_inst_ptr, krb5_prefs_ptr);
static void mf_get_ticket_cache(krb5_inst_ptr, char**);
static void mf_free_ticket_cache(char*);
static void mf_kx509(const char*);
static void mf_kxlist(const char *);
static void mf_kxlist_setup(krb5_inst_ptr);
static void mf_kxlist_crypto(krb5_inst_ptr, char*);
static void mf_krb5_init(krb5_inst_ptr);
static void mf_krb5_free(krb5_inst_ptr kinst);
static char* mf_get_uid_from_ticket_cache(const char*);
static char* mf_dstrcpy(const char*); 
static void mf_dstrfree(char*);
static char* mf_dstrslice(const char*, int, int);
static char* mf_dstrcat(const char*, const char*);
int do_kx509(int, char**);

/*
** mod_fum main... should be called by Apache
** with a username (principal) and password
*/
int mf_main(const char *principal, const char *password)
{
	krb5_inst kinst;
	krb5_prefs kprefs;
	char *tkt_cache;

	/* ----------- KINIT ----------- */

	/* kinit - requires only principal/password */
	mf_krb5_init(&kinst);
	mf_kinit_set_defaults(&kprefs);
	mf_kinit_set_uap(&kprefs, principal, password);
	mf_kinit_setup(&kinst, &kprefs);

	/* kinit -c /tmp/krb5cc_$UID */
	mf_kinit(&kinst, &kprefs);

	/* Save the tkt_cache name kinit_cleanup() */
	mf_get_ticket_cache(&kinst, &tkt_cache);

	//DEBUG
	printf("ticket cache: %s\n",tkt_cache);
	
	mf_kinit_cleanup(&kinst);
	mf_krb5_free(&kinst);

	/* ----------- KX509 ----------- */
	
	/* kx509 - just call the kx509lib*/
	mf_kx509(tkt_cache);

	/* ----------- KXLIST ----------- */
	
	/* kxlist -p */
	mf_kxlist(tkt_cache);
	
	mf_free_ticket_cache(tkt_cache);
	
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
	mf_krb5_init(&kinst);
	
	/* Obtain proper kx509 credentials */
	mf_kxlist_setup(&kinst);

	/* Perform Crypto & write Certficate */
	//DEBUG
	puts(uid);
	puts(kX509DefaultFile);
	name = mf_dstrcat(kX509DefaultFile, uid);
	
	//DEBUG
	puts(name);
	
	mf_kxlist_crypto(&kinst, name);

	mf_krb5_free(&kinst);
	free(uid);
	free(name);
}

/* XXX
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
		mf_err("mem error", 1, TODO);

	klen = kinst->credentials.ticket.length;
	clen = kinst->credentials.second_ticket.length;

	/* Decode the Certifcate (we want PEM format) */
	data = kinst->credentials.second_ticket.data;
	d2i_X509((X509**)(&cert), &data, clen);

	/* Extract & decode the RSA Private Key from the certifcate */
	data = kinst->credentials.ticket.data;
	d2i_RSAPrivateKey(&priv, (const unsigned char**)(&data), klen);

	if(!priv)
		mf_err("d2i_RSAPrivateKey failed", 1, TODO);

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

	memset(&screds, '\0', sizeof(krb5_creds));

	/* The primary principal will be for the client */
	err = krb5_cc_get_principal(kinst->context,
					kinst->cache,
					&screds.client);	
	if(err)
		mf_err("get client principal failed", err, TODO);
	
	/* Now obtain one for the server */
	err = krb5_sname_to_principal(kinst->context,
					kKX509HostName,
					kKX509ServiceName,
					KRB5_NT_UNKNOWN,
					&screds.server);
	if(err)
		mf_err("get server principal failed", err, TODO);

	/* Retrieve the kx509 credentials search by Service Name Only! */
	err = krb5_cc_retrieve_cred(kinst->context,
					kinst->cache,
					KRB5_TC_MATCH_SRV_NAMEONLY,
					&screds,
					&kinst->credentials);
	if(err)
		mf_err("unable to retrieve kx509 credential", err, TODO);
	
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
		mf_err("uid slice error", 1, TODO);

	return uid;
}


/*
-------------------------------KX509-------------------------------
*/

static void mf_kx509(const char *tkt_cache)
{
	char *argv[3];
	int argc;
	int err;
	int i;

	/* setup kx509 as would be called from command line */
	argv[0] = mf_dstrcpy("kx509");
	argv[1] = mf_dstrcpy("-c");
	argv[2] = mf_dstrcpy(tkt_cache);
	argc = 3;

	/* simply run kx509 */
	err = do_kx509(argc, argv);

	if(err != KX509_STATUS_GOOD) 
		mf_err("kx509 failed", err, TODO);
	
	for(i = 0; i < argc; i++)
		mf_dstrfree(argv[i]);
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
		mf_err("get initial credentials failed", err, TODO);
		

	/* Initialize the cache file */
	err = krb5_cc_initialize(kinst->context, kinst->cache, kinst->principal);
	if(err)
		mf_err("initialize cache failed", err, TODO);
	
	
	/* Store the Credential */
	err = krb5_cc_store_cred(kinst->context, kinst->cache, &kinst->credentials);
	if(err)
		mf_err("store credentials failed", err, TODO);

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
** Get the ticket cache location
** NOTE: must be called with a valid krb5_inst!
*/
static void mf_get_ticket_cache(krb5_inst_ptr kinst, char **tkt_cache)
{
	const char *t;
	size_t len;

	/* Save the tkt_cache name kinit_cleanup() */
	t = krb5_cc_get_name(kinst->context, kinst->cache);
	len = strlen(t) + 1;
	*tkt_cache = malloc(len * sizeof(char));

	if(*tkt_cache == NULL)
		mf_err("malloc failed", 1, TODO);
	
	/* Null terminate manually, linux needs strlcpy! */
	strncpy(*tkt_cache, t, len - 1);
	(*tkt_cache)[len - 1] = '\0';
}

static void mf_free_ticket_cache(char *tkt_cache)
{
	if(tkt_cache)
		free(tkt_cache);
}

/*
** Handle standard krb5 inital functions
*/
static void mf_krb5_init(krb5_inst_ptr kinst)
{
	krb5_error_code err; 

	/* Initialize Application Context */
	err = krb5_init_context(&kinst->context);
	if(err)
		mf_err("krb5_init_context failed", err, TODO);
	
	/*
	** Read the default credential cache:
	** equivalent to
	** krb5__cc_resolve(kinst->context, getenv("KRB5CACHE"),
	**			kinst->cache);
	*/
	err = krb5_cc_default(kinst->context, &kinst->cache);
	if(err)
		mf_err("default cache failed", err, TODO);
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
		mf_err("create principal failed", err, TODO);
	
	/*
	** Take the principal name we were given and parse it
	** into the appropriate form for authentication protocols
	*/
	err = krb5_parse_name(kinst->context, kprefs->pname, &kinst->principal);
	if(err)
		mf_err("parse_name failed", err, TODO);
}

static void mf_kinit_cleanup(krb5_inst_ptr kinst)
{
	if(kinst->principal)
		krb5_free_principal(kinst->context, kinst->principal);
	if(kinst->context)
		krb5_free_context(kinst->context);
}


/* 
** dynamic strcpy routine and malloc() wrapper
** (null terminated)
*/
static char* mf_dstrcpy(const char *s)
{
	char *s2;
	size_t len;

	/* this can easily be changed for ap_malloc */
	len = strlen(s) + 1;
	s2 = (char*)(malloc(sizeof(char)*len));

	if(!s2)
		mf_err("malloc failed", 1, TODO);

	strncpy(s2,s,len);
	s2[len] = '\0';

	return s2;
}

/*
** dynamic strcat routine and malloc wrapper
*/
static char* mf_dstrcat(const char *s1, const char *s2)
{
	char *s;
	size_t len;

	/* this can easily be changed for ap_malloc */
	len = strlen(s1) + strlen(s2) + 1;
	s = (char*)(malloc(sizeof(char)*len));

	if(!s)
		mf_err("malloc failed", 1, TODO);

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
		s2 = (char*)(malloc(sizeof(char)*len));

		if(!s2)
			mf_err("malloc failed", 1, TODO);

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
				//mf_err("bounds out of rangeg", 1, TODO);
				free(s2);
				s2 = NULL;
			}
		}
	}	
	
	return s2;
}

/*
** dynamic string function free() wrapper
*/
static void mf_dstrfree(char *s)
{
	if(s)
		free(s);
}
