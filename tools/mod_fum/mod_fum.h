/* $Id $*/

/*
** Free Apache Module to provide
** the functionality of kinit,
** kx509, and kxlist -p ...
*/

#include <krb5.h>

#if 0
#define mf_err(x,y,z) ap_log_error(APLOG_MARK,APLOG_EMERG,z,x ": Err %d", y)
#define mf_warn(x,y,z) ap_log_error(APLOG_MARK,APLOG_WARN,z,x ": Err %d", y)
#endif

#define mf_err(x,y,z) errx(1, x ": error %d", y)
#define mf_warn(x,y,z) warnx(x ": error %d", y)

#define kProxiable 1
#define kForwardable 0
/* 8hrs. default */
#define kLifetime 28800

typedef struct
{
	krb5_context context;
	krb5_ccache cache;
	krb5_principal principal;
	krb5_creds credentials;
	
}krb5_inst, *krb5_inst_ptr;

/* Kerberos 5 Preferences */
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
	const char *pname;
	const char *password;
	
}krb5_prefs, *krb5_prefs_ptr;

int mf_main(const char *principal, const char *password);
