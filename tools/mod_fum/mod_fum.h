/* $Id $*/

/*
** Free Apache Module to provide
** the functionality of kinit,
** kx509, and kxlist -p ...
*/

#include<krb5.h>
#include"httpd.h"

/* Macro Functions */
#define mf_save_pool(x) mf_pool(x)
#define mf_get_pool() mf_pool(NULL)
#define mf_save_request(x) mf_request(x)
#define mf_get_request() mf_request(NULL)
/*
#define mf_err(x,y) ap_log_error(APLOG_MARK, APLOG_EMERG, \
			(apr_status_t)(NULL), (mf_get_request())->server, \
			"%s: error: %d", x, y)
*/
//DEBUG
#define mf_err(x,y) mod_fum_err(x,y)

#define kProxiable 1
#define kForwardable 0
/* 8hrs. default */
#define kLifetime 28800

#define LIBKX509_PATH "libkx509.so"

#define kKX509HostName "certificate"
#define kKX509ServiceName "kx509"
#define kX509DefaultFile "/tmp/x509up_u"
#define kKrb5DefaultFile "/tmp/krb5cc_"
#define kX509FilePermissions 0600

typedef struct
{
	krb5_context context;
	krb5_ccache cache;
	krb5_principal principal;
	krb5_creds credentials;
	
}krb5_inst, *krb5_inst_ptr;

typedef struct
{
	krb5_deltat lifetime;
	int forwardable;
	int proxiable;
	const char *pname;
	const char *password;
	
}krb5_prefs, *krb5_prefs_ptr;

int mf_main(const char *principal, const char *password);
