/*
** mod_fum.c
** Free Apache Module to provide
** the functionality of kinit,
** kx509, and kxlist -p ...
*/

//#include"mod_fum.h"
#include<stdio.h>
#include<krb5.h>
#include<time.h>

/* Error Wrappers if running standalone */
#define MF_STANDALONE 1
#ifndef MF_STANDALONE
#define mf_err(x,y,z) ap_log_error(APLOG_MARK,APLOG_EMERG,z,x ": Err %d", y)
#define mf_warn(x,y,z) ap_log_error(APLOG_MARK,APLOG_WARN,z,x ": Err %d", y)
#else
#include<err.h>
#define mf_err(x,y,z) errx(1,"%s: Err %d  on line %d in %s",x,y,__LINE__,__FILE__)
#define mf_warn(x,y,z) warnx(1,"%s: Err %d on line %d in %s",x,y,__LINE__,__FILE__)
#endif

/* Kerberos 5 Instance */
typedef struct
{
	krb5_context context;
	krb5_ccache cache;
	krb5_principal principal;
	krb5_creds credentials;

	/* XXX this might not be needed */
	char *name;
	
}krb5_inst, *krb5_inst_ptr;

/* Kerberos 5 Prefences */
typedef struct
{
	/*
	** XXX only implementing what
	** we currently need... add support
	** for more krb5 options later
	*/
	krb5_deltat lifetime;
	krb5_deltat starttime;
	//krb5_deltat remaintime;
	int forwardable;
	int proxiable;
	//char *name;
	char *sname;
	char *pname;
	char *password;
	
}krb5_prefs, *krb5_prefs_ptr;

/* Prototypes */
void mf_kinit(krb5_inst_ptr k5, krb5_prefs_ptr kprefs);
void mf_kinit_set_defaults(krb5_prefs_ptr kprefs, char *pass);
void mf_kinit_setup(krb5_inst_ptr k5, krb5_prefs_ptr kprefs);
void mf_kinit_cleanup(krb5_inst_ptr k5);

/* Standalone Test */
#ifdef MF_STANDALONE
int main(int argc, char *argv[])
{
	krb5_inst k5;
	krb5_prefs kprefs;
	char *pass;

	if(argc < 1)
		mf_err("too few arguments",1,TODO);

	pass = argv[1];

	mf_kinit_set_defaults(&kprefs, pass);
	mf_kinit(&k5, &kprefs);

	return 0;
}
#endif

/*
** Perform the functionality of kinit...
*/
void mf_kinit(krb5_inst_ptr k5, krb5_prefs_ptr kprefs)
{
	krb5_error_code err;
	krb5_get_init_creds_opt opt;
	//krb5_keytab keytab = 0;

	mf_kinit_setup(k5, kprefs);

	/* Set default credential options? */
	krb5_get_init_creds_opt_init(&opt);

	/* Make our changes to the defaults */
	krb5_get_init_creds_opt_set_forwardable(&opt, kprefs->forwardable);
	krb5_get_init_creds_opt_set_proxiable(&opt, kprefs->proxiable);
	krb5_get_init_creds_opt_set_tkt_life(&opt, kprefs->lifetime);
	krb5_get_init_creds_opt_set_address_list(&opt, NULL);

	/* Get the initial credentials */
	/* this credentials already stored in keytab...
	err = krb5_get_init_creds_keytab(k5->context, &k5->credentials,
					k5->principal, keytab,
					kprefs->starttime,
					kprefs->sname, &opt);
	*/
	err = krb5_get_init_creds_password(k5->context, &k5->credentials,
						k5->principal, kprefs->password,
						NULL, NULL, kprefs->starttime,
						kprefs->sname, &opt);
	if(err)
		mf_err("get initial credentials failed", err, TODO);
		
	/* XXX this may not be needed */
	/* Validate the Credentials */
	err = krb5_get_validated_creds(k5->context,
					&k5->credentials, 
					k5->principal,
					k5->cache,
					kprefs->sname);
	if(err)
		mf_err("validate credentials failed", err, TODO);
	
	/* Initialize the cache file */
	err = krb5_cc_initialize(k5->context, k5->cache, k5->principal);
	if(err)
		mf_err("initialize cache failed", err, TODO);
	
	/* Store the Credential */
	err = krb5_cc_store(k5->context, k5->cache, &k5->credentials);
	if(err)
		mf_err("store credentials failed", err, TODO);
		
	mf_kinit_cleanup(k5);
}

void mf_kinit_set_defaults(krb5_prefs_ptr kprefs, char *pass)
{
	kprefs->proxiable = 1;
	kprefs->forwardable = 0;
	kprefs->password = pass;

	/* 8 hrs. default */
	kprefs->lifetime = 28800;
}

/*
** Intial context setup
*/
void mf_kinit_setup(krb5_inst_ptr k5, krb5_prefs_ptr kprefs)
{
	krb5_error_code err; 

	/* Initialize Application Context */
	err = krb5_init_context(&k5->context);
	if(err)
		mf_err("krb5_init_context failed", err, TODO);
	
	/*
	** Read the default credential cache:
	** equivalent to krb5__cc_resolve(k5->context,
	**				getenv("KRB5CACHE"),
	**				k5->cache);
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
	
	/* XXX Not sure if this is needed or not.. we will see */
	/*
	** Take the principal name we were given and parse it
	** into the appropriate form for authentication protocols
	*/
	err = krb5_parse_name(k5->context, kprefs->pname, &k5->principal);
	if(err)
		mf_err("parse_name failed", err, TODO);
	err = krb5_unparse_name(k5->context, k5->principal, &k5->name);
	if(err)
		mf_err("unparse_name failed", err, TODO);
}

/*
** Close and free memory
*/
void mf_kinit_cleanup(krb5_inst_ptr k5)
{
	if(&k5->credentials)
		krb5_free_cred_contents(k5->context, &k5->credentials);
	if(k5->name)
		krb5_free_unparsed_name(k5->context, k5->name);
	if(k5->principal)
		krb5_free_principal(k5->context, k5->principal);
	if(k5->cache)
		krb5_cc_close(k5->context, k5->cache);
	if(k5->context)
		krb5_free_context(k5->context);
}


