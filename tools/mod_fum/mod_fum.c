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

Developement Notes:
	1) find a way to combine all the krb5_init stuff that is used
		for both kinit, and kxlist...

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
#include <err.h>

#include"mod_fum.h"

static void mf_kinit_setup(krb5_inst_ptr, krb5_prefs_ptr);
static void mf_kinit_cleanup(krb5_inst_ptr);
static void mf_kinit_set_uap(krb5_prefs_ptr, const char*, const char*);
static void mf_kinit_set_defaults(krb5_prefs_ptr);
static void mf_kinit(krb5_inst_ptr, krb5_prefs_ptr);
static void mf_get_ticket_cache(krb5_inst_ptr , char **);
static void mf_free_ticket_cache(char *tkt_cache);
static void mf_kx509(const char*);
static void mf_kxlist(const char *tkt_cache);
static int mf_get_uid_from_ticket_cache(const char *tkt);
static char* mf_dstrcpy(const char*); 
static void mf_dstrfree(char *s);
static char* mf_dstrslice(const char *s, int x, int y);

/*
** mod_fum main... should be called by Apache
** with a username (principal) and password
*/
int mf_main(const char *principal, const char *password)
{
	krb5_inst kinst;
	krb5_prefs kprefs;
	char *tkt_cache;

	/* kinit - requires only principal/password */
	mf_kinit_set_defaults(&kprefs);
	mf_kinit_set_uap(&kprefs, principal, password);
	mf_kinit_setup(&kinst, &kprefs);

	mf_kinit(&kinst, &kprefs);

	/* Save the tkt_cache name kinit_cleanup() */
	mf_get_ticket_cache(&kinst, &tkt_cache);

	//DEBUG
	printf("ticket cache: %s\n",tkt_cache);
	
	mf_kinit_cleanup(&kinst);

	/* kx509 - just call the kx509lib*/
	mf_kx509(tkt_cache);

	/* kxlist -p */
	mf_kxlist(tkt_cache);
	
	/* XXX return err if auth failed */


	mf_free_ticket_cache(tkt_cache);
	
	return 0;
}

/*
-------------------------------KXLIST------------------------------
*/
static void mf_kxlist(const char *tkt_cache)
{
	int uid;

	/* 
	** Parse tkt_cache name to get uid:
	** /tmp/krb5cc_UID_FUBAR
	** /tmp/krb5cc_UID
	*/
	uid = mf_get_uid_from_ticket_cache(tkt_cache);
	if(uid == -1)
		mf_err("get uid failed", 1, TODO);
	
	//DEBUG
	printf("UID: %d\n", uid);


	

}

/*
** Parse the user id from the ticket_cache name
*/
static int mf_get_uid_from_ticket_cache(const char *tkt)
{
	int uid = -1;
	int i, j;
	int b, e;
	char *suid;
	const char *tmp = tkt;

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
	suid = mf_dstrslice(tkt, b, e);
	if(suid)
	{
		uid = atoi(suid);
	}

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
	void *h;
	int i;
	int (*do_kx509)(int, char **);

	/* setup kx509 as would be called from command line */
	argv[0] = mf_dstrcpy("kx509");
	argv[1] = mf_dstrcpy("-c");
	argv[2] = mf_dstrcpy(tkt_cache);
	argc = 3;

	/* dynamic load of libkx509.so */
	if((h = dlopen(LIBKX509_PATH, RTLD_LAZY)) == NULL)
		mf_err(dlerror(),1, TODO);
	
	if((do_kx509 = dlsym(h, "do_kx509")) == NULL)
		mf_err(dlerror(),1, TODO);

	/* simply run kx509 */
	err = (*do_kx509)(argc, argv);
	dlclose(h);

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
static void mf_kinit(krb5_inst_ptr k5, krb5_prefs_ptr kprefs)
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
	err = krb5_get_init_creds_password(k5->context, &k5->credentials,
		k5->principal, (char*)(kprefs->password),
		krb5_prompter_posix, NULL, 0, NULL, &opt);
	if(err)
		mf_err("get initial credentials failed", err, TODO);
		

	/* Initialize the cache file */
	err = krb5_cc_initialize(k5->context, k5->cache, k5->principal);
	if(err)
		mf_err("initialize cache failed", err, TODO);
	
	
	/* Store the Credential */
	err = krb5_cc_store_cred(k5->context, k5->cache, &k5->credentials);
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
** Intial context setup
*/
static void mf_kinit_setup(krb5_inst_ptr k5, krb5_prefs_ptr kprefs)
{
	krb5_error_code err; 

	/* Initialize Application Context */
	err = krb5_init_context(&k5->context);
	if(err)
		mf_err("krb5_init_context failed", err, TODO);
	
	/*
	** Read the default credential cache:
	** equivalent to
	** krb5__cc_resolve(k5->context, getenv("KRB5CACHE"),
	**			k5->cache);
	*/
	err = krb5_cc_default(k5->context, &k5->cache);
	if(err)
		mf_err("default cache failed", err, TODO);
	
	/*
	** Generate a full principal name
	** to be used for authentication.
	*/
	err = krb5_sname_to_principal(k5->context,
		NULL, NULL, KRB5_NT_SRV_HST, &k5->principal);
	if(err)
		mf_err("create principal failed", err, TODO);
	
	/*
	** Take the principal name we were given and parse it
	** into the appropriate form for authentication protocols
	*/
	err = krb5_parse_name(k5->context, kprefs->pname, &k5->principal);
	if(err)
		mf_err("parse_name failed", err, TODO);
}

static void mf_kinit_cleanup(krb5_inst_ptr kinst)
{
	/* free mem */
	if(&kinst->credentials)
		krb5_free_cred_contents(kinst->context, &kinst->credentials);
	if(kinst->principal)
		krb5_free_principal(kinst->context, kinst->principal);
	if(kinst->cache)
		krb5_cc_close(kinst->context, kinst->cache);
	if(kinst->context)
		krb5_free_context(kinst->context);
	
	/* reset defaults for kxlist */
	/*
	&kinst->credentials = NULL;
	kinst->principal = NULL;
	kinst->cache = NULL;
	kinst->context = NULL;
	*/
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
