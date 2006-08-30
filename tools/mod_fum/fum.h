/* $Id$ */

#include <krb5.h>

#include <httpd/httpd.h>

#define HTTP_ISE	HTTP_INTERNAL_SERVER_ERROR

struct fum {
	krb5_context	 f_ctx;
	krb5_ccache	 f_cache;
	krb5_principal	 f_prin;
	krb5_creds	 f_cred;
	krb5_creds	 f_x509cred;
	char		*f_tktcachefn;
	char		*f_certfn;
	uid_t		 f_uid;
	gss_cred_id_t	 f_delegated_cred;
	gss_ctx_id_t	 f_gssctx;
	gss_cred_id_t	 f_server_creds;
};

void fum_log(request_rec *, const char *, ...);
void fum_logx(request_rec *, const char *, ...);
int  fum_gss(request_rec *, struct fum *, const char *, char **);
int  fum_gss_storecred(request_rec *, struct fum *);
void fum_gss_free(struct fum *);
void fum_gss_init(struct fum *);

char *fum_keytab;
