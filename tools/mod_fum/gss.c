/* $Id$ */

/*
 * Copyright (c) 2004-2006 Masarykova universita
 * (Masaryk University, Brno, Czech Republic)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the University nor the names of its contributors may
 *    be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

#define _GNU_SOURCE /* off64_t */
#include <sys/types.h>

#include <stdio.h>
#include <unistd.h>

#include <gssapi/gssapi.h>
#include <gssapi/gssapi_krb5.h>

#include <krb5.h>

#include <httpd/httpd.h>
#include <httpd/http_core.h>
#include <apr_strings.h>
#include <apr_base64.h>

#include "fum.h"

#ifdef GSSAPI_SUPPORTS_SPNEGO
static int
fum_gss_cmptype(gss_buffer_t token, gss_OID oid)
{
	unsigned char *p;
	size_t len;

	if (token->length == 0)
		return GSS_S_DEFECTIVE_TOKEN;

	p = token->value;
	if (*p++ != 0x60)
		return GSS_S_DEFECTIVE_TOKEN;
	len = *p++;
	if (len & 0x80) {
		if ((len & 0x7f) > 4)
			return GSS_S_DEFECTIVE_TOKEN;
		p += len & 0x7f;
	}
	if (*p++ != 0x06)
		return GSS_S_DEFECTIVE_TOKEN;

	if (((OM_uint32) *p++) != oid->length)
		return GSS_S_DEFECTIVE_TOKEN;

	return memcmp(p, oid->elements, oid->length);
}
#endif

static const char *
fum_gss_error(request_rec *r, OM_uint32 emajor, OM_uint32 eminor)
{
	gss_buffer_desc majbuf, minbuf, mzero;
	OM_uint32 maj, min, ctx;
	char *s;

	memset(&majbuf, 0, sizeof(majbuf));
	memset(&minbuf, 0, sizeof(minbuf));
	memset(&mzero, 0, sizeof(mzero));

	ctx = 0;
	maj = gss_display_status(&min, emajor, GSS_C_GSS_CODE,
	    GSS_C_NO_OID, &ctx, &majbuf);
	if (GSS_ERROR(maj)) {
		s = apr_psprintf(r->pool, "error decoding major "
		    "error message (%u)", emajor);
		goto done;
	}

	ctx = 0;
	maj = gss_display_status(&min, eminor, GSS_C_MECH_CODE,
	    GSS_C_NULL_OID, &ctx, &minbuf);
	if (GSS_ERROR(maj)) {
		s = apr_psprintf(r->pool, "%s (error decoding minor "
		    "error message - %u)", (char *)majbuf.value, eminor);
		goto done;
	}

	s = apr_psprintf(r->pool, "%s (%s)",
	    (char *)majbuf.value, (char *)minbuf.value);
done:
	if (memcmp(&majbuf, &mzero, sizeof(mzero)) != 0)
		gss_release_buffer(&min, &majbuf);
	if (memcmp(&minbuf, &mzero, sizeof(mzero)) != 0)
		gss_release_buffer(&min, &minbuf);
	return (s);
}

int
fum_gss_storecred(request_rec *r, struct fum *f)
{
	OM_uint32 major, minor;
	krb5_error_code kerr;

fum_logx(r, "STORECRED");
	if (f->f_delegated_cred == GSS_C_NO_CREDENTIAL) {
		fum_logx(r, "fum_gss_storecred: no cred");
		return (HTTP_ISE);
	}

	if ((kerr = krb5_cc_initialize(f->f_ctx,
	    f->f_cache, f->f_prin)) != 0) {
		fum_logx(r, "fum_gss_storecred: "
		    "krb5_cc_initialize: %s", kerr);
		return (HTTP_ISE);
	}

fum_logx(r, "krb5_copy_ccache");
	major = gss_krb5_copy_ccache(&minor,
	    f->f_delegated_cred, f->f_cache);
	if (GSS_ERROR(major)) {
		fum_logx(r, "fum_gss_storecred: failure (%s)",
		    fum_gss_error(r, major, minor));
		return (HTTP_ISE);
	}
fum_logx(r, "copied del to ccache");
	return (OK);
}

static int
fum_gss_creds(request_rec *r, gss_cred_id_t *server_creds)
{
	gss_buffer_desc token = GSS_C_EMPTY_BUFFER;
	gss_name_t server_name = GSS_C_NO_NAME;
	OM_uint32 major, minor, minor2;
	const char service[] = "HTTP";
	char *buf;

	buf = apr_psprintf(r->pool, "%s@%s",
	    service, ap_get_server_name(r));
	if (buf == NULL)
		return (HTTP_ISE);

	token.value = buf;
	token.length = strlen(buf) + 1;

	major = gss_import_name(&minor, &token,
	    GSS_C_NT_HOSTBASED_SERVICE, &server_name);
	memset(&token, 0, sizeof(token));
	if (GSS_ERROR(major)) {
		fum_logx(r, "fum_gss_creds: gss_import_name: %s",
		    fum_gss_error(r, major, minor));
		return (HTTP_ISE);
	}

	major = gss_display_name(&minor, server_name, &token, NULL);
	if (GSS_ERROR(major)) {
		fum_logx(r, "fum_gss_creds: gss_display_name: %s",
		    fum_gss_error(r, major, minor));
		return (HTTP_ISE);
	}

	fum_logx(r, "acquiring creds for %s", token.value);
	gss_release_buffer(&minor, &token);

	fum_logx(r, "setting keytab location to %s", fum_keytab);
	gsskrb5_register_acceptor_identity(fum_keytab);

	major = gss_acquire_cred(&minor, server_name, GSS_C_INDEFINITE,
	    GSS_C_NO_OID_SET, GSS_C_ACCEPT, server_creds, NULL, NULL);
	gss_release_name(&minor2, &server_name);
	if (GSS_ERROR(major)) {
		fum_logx(r, "fum_gss_creds: gss_acquire_cred: %s",
		    fum_gss_error(r, major, minor));
		return (HTTP_ISE);
	}
	return (OK);
}

int
fum_gss(request_rec *r, struct fum *f, const char *enc, char **user)
{
	OM_uint32 (*accept_sec_token)(OM_uint32 *, gss_ctx_id_t *,
	    const gss_cred_id_t, const gss_buffer_t,
	    const gss_channel_bindings_t, gss_name_t *, gss_OID *,
	    gss_buffer_t, OM_uint32 *, OM_uint32 *, gss_cred_id_t *);
	gss_buffer_desc output = GSS_C_EMPTY_BUFFER;
	gss_buffer_desc input = GSS_C_EMPTY_BUFFER;
	gss_name_t client_name = GSS_C_NO_NAME;
	gss_OID_desc spnego_oid;
	OM_uint32 major, minor;
	int error;

	spnego_oid.length = 6;
	spnego_oid.elements = (void *)"\x2b\x06\x01\x05\x05\x02";

	if (fum_gss_creds(r, &f->f_server_creds) != OK) {
		fum_logx(r, "fum_gss_creds");
		return (HTTP_ISE);
	}

	input.length = apr_base64_decode_len(enc) + 1;
	input.value = apr_pcalloc(r->pool, input.length);
	if (input.value == NULL) {
		fum_log(r, "apr_pcalloc");
		error = HTTP_ISE;
		goto end;
	}
	input.length = apr_base64_decode(input.value, enc);

#ifdef GSSAPI_SUPPORTS_SPNEGO
	accept_sec_token = (fum_gss_cmptype(&input, &spnego_oid) == 0) ?
	    gss_accept_sec_context_spnego : gss_accept_sec_context;
#else
	accept_sec_token = gss_accept_sec_context;
#endif

	fum_logx(r, "Verifying client data using %s",
	    (accept_sec_token == gss_accept_sec_context) ?
	    "KRB5 GSS-API" : "SPNEGO GSS-API");

fum_logx(r, "fum_gss: del cred ok? *%s",
f->f_delegated_cred == GSS_C_NO_CREDENTIAL ? "BAD" : "good" );

	major = accept_sec_token(&minor, &f->f_gssctx, f->f_server_creds,
	    &input, GSS_C_NO_CHANNEL_BINDINGS, &client_name, NULL,
	    &output, NULL, NULL, &f->f_delegated_cred);
	fum_logx(r, "Verification returned code %u", major);
	if (output.length)
		gss_release_buffer(&minor, &output);

#if 0
	if (output.length) {
		char *token = NULL;
		size_t len;

		len = ap_base64encode_len(output.length) + 1;
		token = ap_pcalloc(r->pool, len + 1);
		if (token == NULL) {
			fum_log("ap_pcalloc");
			error = HTTP_ISE;
			gss_release_buffer(&minor, &output);
			goto end;
		}
		ap_base64encode(token, output.value, output.length);
		token[len] = '\0';

		log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,
		    "GSS-API token of length %d bytes will be sent back",
		    output.length);
		gss_release_buffer(&minor, &output);
//		set_kerb_auth_headers(r, conf, 0, 0, *negotiate_ret_value);
	}
#endif

	if (GSS_ERROR(major)) {
		fum_logx(r, "gss_accept_sec_context: %s",
		    fum_gss_error(r, major, minor));
		error = HTTP_UNAUTHORIZED;
		goto end;
	}

fum_logx(r, "fum_gss: del cred ok? %s",
f->f_delegated_cred == GSS_C_NO_CREDENTIAL ? "BAD" : "good" );

	major = gss_display_name(&minor, client_name, &output, NULL);
	gss_release_name(&minor, &client_name);

	if (GSS_ERROR(major)) {
		fum_logx(r, "gss_display_name: %s",
		    fum_gss_error(r, major, minor));
		error = HTTP_ISE;
		goto end;
	}

fum_logx(r, "fum_gss: doing this for %s", (char *)output.value);

	*user = apr_pstrdup(r->pool, output.value);
	gss_release_buffer(&minor, &output);

	error = OK;

end:
	if (output.length)
		gss_release_buffer(&minor, &output);
	if (client_name != GSS_C_NO_NAME)
		gss_release_name(&minor, &client_name);

fum_logx(r, "fum_gss: del cred ok? %s",
f->f_delegated_cred == GSS_C_NO_CREDENTIAL ? "BAD" : "good" );

	return (error);
}

void
fum_gss_free(struct fum *f)
{
	OM_uint32 minor;

	if (f->f_delegated_cred != GSS_C_NO_CREDENTIAL)
		gss_release_cred(&minor, &f->f_delegated_cred);
	if (f->f_server_creds != GSS_C_NO_CREDENTIAL)
		gss_release_cred(&minor, &f->f_server_creds);
	if (f->f_gssctx != GSS_C_NO_CONTEXT)
		gss_delete_sec_context(&minor, &f->f_gssctx, GSS_C_NO_BUFFER);
}

void
fum_gss_init(struct fum *f)
{
	f->f_gssctx = GSS_C_NO_CONTEXT;
	f->f_server_creds = GSS_C_NO_CREDENTIAL;
	f->f_delegated_cred = GSS_C_NO_CREDENTIAL;
}
