/* $Id$ */

/*
** Free Apache Module to provide
** the functionality of kinit,
** kx509, and kxlist -p ...
*/

#include <krb5.h>
#include <err.h>

#include"mod_fum.h"

static void mf_kinit_setup(krb5_inst_ptr, krb5_prefs_ptr);
static void mf_kinit_cleanup(krb5_inst_ptr);

/*
** Perform the functionality of kinit...
*/
void mf_kinit(krb5_inst_ptr k5, krb5_prefs_ptr kprefs)
{
	krb5_error_code err;
	krb5_get_init_creds_opt opt;

	mf_kinit_setup(k5, kprefs);

	/* Set default credential options? */
	krb5_get_init_creds_opt_init(&opt);

	/* Make our changes to the defaults */
	krb5_get_init_creds_opt_set_forwardable(&opt, kprefs->forwardable);
	krb5_get_init_creds_opt_set_proxiable(&opt, kprefs->proxiable);
	krb5_get_init_creds_opt_set_tkt_life(&opt, kprefs->lifetime);
	krb5_get_init_creds_opt_set_address_list(&opt, NULL);

	/* Create credentials from give password, or prompt for password */
	err = krb5_get_init_creds_password(k5->context, &k5->credentials,
						k5->principal, kprefs->password,
						krb5_prompter_posix, NULL, 0,
						NULL, &opt);
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
		
	mf_kinit_cleanup(k5);
}

/* Set the user (principal) and password */
void mf_kinit_set_uap(krb5_prefs_ptr kprefs, char *principal, char *password)
{
	kprefs->password = password;
	kprefs->pname = principal;
}

void mf_kinit_set_defaults(krb5_prefs_ptr kprefs)
{
	kprefs->proxiable = kProxiable;
	kprefs->forwardable = kForwardable;
	kprefs->lifetime = kLifetime;
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

static void mf_kinit_cleanup(krb5_inst_ptr k5)
{
	if(&k5->credentials)
		krb5_free_cred_contents(k5->context, &k5->credentials);
	if(k5->principal)
		krb5_free_principal(k5->context, k5->principal);
	if(k5->cache)
		krb5_cc_close(k5->context, k5->cache);
	if(k5->context)
		krb5_free_context(k5->context);
}
