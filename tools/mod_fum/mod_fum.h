/*
** mod_fum.h
** Free Apache Module to provide
** the functionality of kinit,
** kx509, and kxlist -p ...
*/

#include<stdio.h>
#include<krb5.h>
#include<time.h>

/* Error wrappers for running standalone */
#define MF_STANDALONE 1
#ifndef MF_STANDALONE
#define mf_err(x,y,z) ap_log_error(APLOG_MARK,APLOG_EMERG,z,x ": Err %d", y)
#define mf_warn(x,y,z) ap_log_error(APLOG_MARK,APLOG_WARN,z,x ": Err %d", y)
#else
#include<err.h>
#define mf_err(x,y,z) errx(1,"%s: Err %d  on line %d in %s",x,y,__LINE__,__FILE__)
#define mf_warn(x,y,z) warnx(1,"%s: Err %d on line %d in %s",x,y,__LINE__,__FILE__)
#endif

#define kProxiable 1
#define kForwardable 0
/* 8hrs. default */
#define kLifetime 28800

/* Kerberos 5 Instance */
typedef struct
{
	krb5_context context;
	krb5_ccache cache;
	krb5_principal principal;
	krb5_creds credentials;
	
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
	int forwardable;
	int proxiable;
	char *pname;
	char *password;
	
}krb5_prefs, *krb5_prefs_ptr;

/* Prototypes */
void mf_kinit(krb5_inst_ptr k5, krb5_prefs_ptr kprefs);
void mf_kinit_set_uap(krb5_prefs_ptr kprefs, char *principal, char *password);
void mf_kinit_set_defaults(krb5_prefs_ptr kprefs);
void mf_kinit_setup(krb5_inst_ptr k5, krb5_prefs_ptr kprefs);
void mf_kinit_cleanup(krb5_inst_ptr k5);
