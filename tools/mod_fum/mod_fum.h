/* $Id$ */

/*
** Free Apache Module to provide the functionality of kinit,
** kx509, and kxlist -p
*/

#include <krb5.h>
#include <httpd/httpd.h>

#define kModuleVersion "mod_fum/1.0-a"

#define mf_save_pool(x) mf_pool(x)
#define mf_get_pool() mf_pool(NULL)
#define mf_save_request(x) mf_request(x)
#define mf_get_request() mf_request(NULL)
#define mf_err(x,y)							\
	ap_log_error(APLOG_MARK, APLOG_ERR, (apr_status_t)(NULL),	\
	    (mf_get_request())->server, "%s - %s: error %d on line %d",	\
	    kModuleVersion, x, y, __LINE__)

#define KrbToApache(x) ((x == KRB5KDC_ERR_NONE || x == 0) ? OK : HTTP_UNAUTHORIZED)
#define Kx509ToApache(x) ((x == KX509_STATUS_GOOD) ? OK : HTTP_INTERNAL_SERVER_ERROR)

#define PROXIABLE 1
#define FORWARDABLE 0
#define LIFETIME 28800 /* 8hrs. default */

#define KX509_HOSTNAME "certificate"
#define KX509_SERVNAME "kx509"

/* XXX: put in a .conf file? */
#define X509_DEFFILE "/tmp/x509up_fum_u"
#define KRB5_DEFFILE "/tmp/krb5cc_fum_"
#define CRED_FILE "x509up_fum_u"
#define CRED_DIR "/tmp"

#define X509_FILE_PERM 0600

struct krb5_inst {
	krb5_context context;
	krb5_ccache cache;
	krb5_principal principal;
	krb5_creds credentials;
	int initialized;
};

struct krb5_prefs {
	krb5_deltat lifetime;
	int forwardable;
	int proxiable;
	const char *pname;
	const char *password;
};

extern struct krb5_inst krb5_inst, *krb5_inst_ptr;
extern struct krb5_prefs krb5_prefs, *krb5_prefs_ptr;
