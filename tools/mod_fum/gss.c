/* $Id$ */

static int
fum_gss(const char *enc)
{
	OM_uint32 (KRB5_LIB_FUNCTION *accept_sec_token)(OM_uint32 *,
	    gss_ctx_id_t *, const gss_cred_id_t, const gss_buffer_t,
	    const gss_channel_bindings_t, gss_name_t *, gss_OID *,
	    gss_buffer_t, OM_uint32 *, OM_uint32 *, gss_cred_id_t *);
	gss_cred_id_t delegated_cred = GSS_C_NO_CREDENTIAL;
	gss_cred_id_t server_creds = GSS_C_NO_CREDENTIAL;
	gss_buffer_desc output = GSS_C_EMPTY_BUFFER;
	gss_buffer_desc input = GSS_C_EMPTY_BUFFER;
	gss_ctx_id_t context = GSS_C_NO_CONTEXT;
	gss_name_t client_name = GSS_C_NO_NAME;
	gss_OID_desc spnego_oid;
	OM_uint32 major, minor;
	char *ktname;
	int error;

	spnego_oid.length = 6;
	spnego_oid.elements = (void *)"\x2b\x06\x01\x05\x05\x02";

	if (conf->krb_5_keytab) {
		char *ktname;
		/*
		 * We don't use the ap_* calls here,
		 * since the string passed to putenv()
		 * will become part of the enviroment
		 * and shouldn't be free()'d by Apache.
		 */
		if (asprintf(&ktname, "KRB5_KTNAME=%s",
		    keytab) == -1) {
			fum_log("asprintf gss");
			return (HTTP_INTERNAL_SERVER_ERROR);
		}
		putenv(ktname);
#ifdef HEIMDAL
		/* Seems to be also supported by latest MIT */
		gsskrb5_register_acceptor_identity(keytab);
#endif
	}

	if (fum_gss_creds(r, conf, &server_creds) != OK) {
		fum_log("fum_gss_creds");
		return (HTTP_INTERNAL_SERVER_ERROR);
	}

	input.length = ap_base64decode_len(enc) + 1;
	input.value = ap_pcalloc(fum_pool, input.length);
	if (input.value == NULL) {
		fum_log("ap_pcalloc");
		ret = HTTP_INTERNAL_SERVER_ERROR;
		goto end;
	}
	input.length = ap_base64decode(input.value, enc);

#ifdef GSSAPI_SUPPORTS_SPNEGO
	accept_sec_token = gss_accept_sec_context;
#else
	accept_sec_token = (cmp_gss_type(&input_token, &spnego_oid) == 0) ?
	    gss_accept_sec_context_spnego : gss_accept_sec_context;
#endif

	fum_log("Verifying client data using %s",
	    (accept_sec_token == gss_accept_sec_context) ?
	    "KRB5 GSS-API" : "SPNEGO GSS-API");

	major = accept_sec_token(&minor, &context, server_creds,
	    &input, GSS_C_NO_CHANNEL_BINDINGS, &client_name, NULL,
	    &output, NULL, NULL, &delegated_cred);
	fum_log("Verification returned code %d", major);

	if (output.length) {
		char *token = NULL;
		size_t len;

		len = ap_base64encode_len(output.length) + 1;
		token = ap_pcalloc(fum_pool, len + 1);
		if (token == NULL) {
			fum_log("ap_pcalloc");
			ret = HTTP_INTERNAL_SERVER_ERROR;
			gss_release_buffer(&minor, &output);
			goto end;
		}
		ap_base64encode(token, output.value, output.length);
		token[len] = '\0';
		*negotiate_ret_value = token;
		log_rerror(APLOG_MARK, APLOG_DEBUG, 0, r,
		"GSS-API token of length %d bytes will be sent back",
		output_token.length);
		gss_release_buffer(&minor, &output);
		set_kerb_auth_headers(r, conf, 0, 0, *negotiate_ret_value);
	}

	if (GSS_ERROR(major_status)) {
		fum_log("gss_accept_sec_context: %s",
		    fum_gss_error(major, minor));
		ret = HTTP_UNAUTHORIZED;
		goto end;
	}

	major = gss_display_name(&minor, client_name, &output, NULL);
	gss_release_name(&minor, &client_name);

	if (GSS_ERROR(major)) {
		fum_log("gss_display_name: %s",
		    fum_gss_error(major, minor));
		ret = HTTP_INTERNAL_SERVER_ERROR;
		goto end;
	}

	MK_AUTH_TYPE = MECH_NEGOTIATE;
	MK_USER = ap_pstrdup(r->pool, output_token.value);

	if (conf->krb_save_credentials && delegated_cred != GSS_C_NO_CREDENTIAL)
		store_gss_creds(r, conf, (char *)output.value, delegated_cred);

	gss_release_buffer(&minor, &output);
	ret = OK;

end:
	if (delegated_cred)
		gss_release_cred(&minor, &delegated_cred);
	if (output.length)
		gss_release_buffer(&minor, &output);
	if (client_name != GSS_C_NO_NAME)
		gss_release_name(&minor, &client_name);
	if (server_creds != GSS_C_NO_CREDENTIAL)
		gss_release_cred(&minor, &server_creds);
	if (context != GSS_C_NO_CONTEXT)
		gss_delete_sec_context(&minor, &context, GSS_C_NO_BUFFER);
	return ret;
}
