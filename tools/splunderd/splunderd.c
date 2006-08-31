/* $Id$ */

#define _GNU_SOURCE /* asprintf */
#include <sys/param.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include <err.h>
#include <errno.h>
#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <openssl/err.h>
#include <openssl/ssl.h>

#include <krb5.h>
#include <gssapi.h>

#ifndef __dead
#define __dead __attribute__((__noreturn__))
#endif

#define LISTENQ	5

__dead void usage(void);

char		rss_host[MAXHOSTNAMELEN];
char		rss_port[BUFSIZ];
char		rss_path[BUFSIZ];

char		local_host[MAXHOSTNAMELEN];
char		local_port[BUFSIZ];

int		verbose;
const char	*progname;

#define DPRINTF(x)			\
	if (verbose)			\
		warnx x

void
conf_init(void)
{
	snprintf(rss_host, sizeof(rss_host), "gridfe.psc.edu");
	snprintf(rss_port, sizeof(rss_port), "443");
	snprintf(rss_path, sizeof(rss_path),
	    "/gridfe/gridfe/jobs/status?out=rss");

	snprintf(local_host, sizeof(local_host), "*");
	snprintf(local_port, sizeof(local_port), "4521");

	DPRINTF(("set rss_host to %s", rss_host));
	DPRINTF(("set rss_port to %s", rss_port));
	DPRINTF(("set rss_path to %s", rss_path));

	DPRINTF(("set local_host to %s", local_host));
	DPRINTF(("set local_port to %s", local_port));
}

int
setup(void)
{
	struct addrinfo hints, *res, *res0;
	char buf[BUFSIZ], svcbuf[BUFSIZ];
	int error, save_errno, s, opt;
	const char *lastcause = NULL;
	socklen_t siz;

	memset(&hints, 0, sizeof(hints));
	hints.ai_family = PF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE;
	error = getaddrinfo(local_host, local_port, &hints, &res0);
	if (error)
		errx(1, "getaddrinfo %s: %s", local_host,
		    gai_strerror(error));

	s = -1;
	for (res = res0; res; res = res->ai_next) {
		s = socket(res->ai_family, res->ai_socktype,
		    res->ai_protocol);
		if (s == -1) {
			lastcause = "socket";
			continue;
		}
		opt = 1;
		siz = sizeof(opt);
		if (setsockopt(s, SOL_SOCKET, SO_REUSEADDR, &opt,
		    siz) == -1)
			err(1, "setsockopt");
		if (bind(s, res->ai_addr, res->ai_addrlen) == -1) {
			lastcause = "bind";
			save_errno = errno;
			close(s);
			errno = save_errno;
			s = -1;
			continue;
		}
		if (listen(s, LISTENQ) == -1) {
			lastcause = "listen";
			save_errno = errno;
			close(s);
			errno = save_errno;
			s = -1;
			continue;
		}
		break;
	}
	if (s == -1)
		err(1, "%s", lastcause);
	if ((error = getnameinfo(res->ai_addr, res->ai_addrlen,
	    buf, sizeof(buf), svcbuf, sizeof(svcbuf),
	    NI_NUMERICHOST | NI_NUMERICSERV)) != 0)
		errx(1, "getnameinfo: %s", gai_strerror(error));
	freeaddrinfo(res0);
	DPRINTF(("listening on %s:%s", buf, svcbuf));
	return (s);
}

void
ssl_init(void)
{
	SSL_library_init();
	OpenSSL_add_all_algorithms();
	SSL_load_error_strings();
}

const char *
ssl_error(void)
{
#define ERRBUF_LEN 120
        static char errbuf[ERRBUF_LEN];

        return (ERR_error_string(ERR_get_error(), errbuf));
}

void
xwrite(SSL *ssl, const void *buf, size_t siz)
{
	int len = siz;

	/* XXX try to write more on short writes instead of failure */
	if (SSL_write(ssl, buf, len) != len)
		errx(1, "SSL_write: %s", ssl_error());
}

/*
 * enc buffer must be 4/3+1 the size of buf.
 * Note: enc and buf are NOT C-strings.
 */
void
base64_encode(const void *buf, char *enc, size_t siz)
{
	static char pres[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	    "abcdefghijklmnopqrstuvwxyz0123456789+/";
	const unsigned char *p;
	u_int32_t val;
	size_t pos;
	int i;

	i = 0;
	for (pos = 0, p = buf; pos < siz; pos += 3, p += 3) {
		/*
		 * Convert 3 bytes of input (3*8 bits) into
		 * 4 bytes of output (4*6 bits).
		 *
		 * If fewer than 3 bytes are available for this
		 * round, use zeroes in their place.
		 */
		val = p[0] << 16;
		if (pos + 1 < siz)
			val |= p[1] << 8;
		if (pos + 2 < siz)
			val |= p[2];

		enc[i++] = pres[val >> 18];
		enc[i++] = pres[(val >> 12) & 0x3f];
		if (pos + 1 >= siz)
			break;
		enc[i++] = pres[(val >> 6) & 0x3f];
		if (pos + 2 >= siz)
			break;
		enc[i++] = pres[val & 0x3f];
	}
	if (pos + 1 >= siz) {
		enc[i++] = '=';
		enc[i++] = '=';
	} else if (pos + 2 >= siz)
		enc[i++] = '=';
	enc[i++] = '\0';
	DPRINTF(("base64: wrote %d chars", i));
}

void
serve(int clifd)
{
	const char authline[] = "Authorization: Negotiate ";
	const char service[] = "HTTP";
	const char nl[] = "\r\n";

	gss_OID_desc krb5_oid = { 9, "\x2a\x86\x48\x86\xf7\x12\x01\x02\x02" };
	gss_buffer_desc otoken = GSS_C_EMPTY_BUFFER;
	gss_buffer_desc itoken = GSS_C_EMPTY_BUFFER;
	OM_uint32 major, minor, rflags, rtime;
	gss_ctx_id_t gss_ctx;
	gss_name_t name;
	gss_OID oid;

	char *p, buf[BUFSIZ], svcbuf[BUFSIZ];
	int n, rssfd, error, save_errno, len;
	struct addrinfo hints, *res, *res0;
	const char *lastcause = NULL;
	size_t bsiz;

	SSL_CTX *ssl_ctx;
	SSL *ssl;

	/* Default to MIT Kerberos */
	oid = &krb5_oid;

	/* itoken = "HTTP/f.q.d.n" aka "HTTP@hostname.foo.bar" */
	if ((itoken.length = asprintf((char **)&itoken.value,
	    "%s@%s", service, rss_host)) == (size_t)-1)
		err(1, "asprintf");

	/* Convert the printable name to an internal format */
	major = gss_import_name(&minor, &itoken,
	    GSS_C_NT_HOSTBASED_SERVICE, &name);

	free(itoken.value);

	if (GSS_ERROR(major))
		errx(1, "gss_import_name");

	/* Initiate a security context */
	rflags = GSS_C_DELEG_FLAG;
	rtime = GSS_C_INDEFINITE;
	gss_ctx = GSS_C_NO_CONTEXT;
	major = gss_init_sec_context(&minor, GSS_C_NO_CREDENTIAL,
	    &gss_ctx, name, oid, rflags, rtime,
	    GSS_C_NO_CHANNEL_BINDINGS, GSS_C_NO_BUFFER,
	    NULL, &otoken, NULL, NULL);

	if (GSS_ERROR(major) || otoken.length == 0)
		err(1, "gss_init_sec_context");

	/* resolve and connect to rss host */
	memset(&hints, 0, sizeof(hints));
	hints.ai_family = PF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;
	error = getaddrinfo(rss_host, rss_port, &hints, &res0);
	if (error)
		errx(1, "%s", gai_strerror(error));

	rssfd = -1;
	for (res = res0; res; res = res->ai_next) {
		rssfd = socket(res->ai_family, res->ai_socktype,
		    res->ai_protocol);
		if (rssfd == -1) {
			lastcause = "socket";
			continue;
		}
		if (connect(rssfd, res->ai_addr, res->ai_addrlen) == -1) {
			lastcause = "connect";
			save_errno = errno;
			close(rssfd);
			errno = save_errno;
			rssfd = -1;
			continue;
		}
		break;
	}
	if (rssfd == -1)
		err(1, "%s", lastcause);
	if ((error = getnameinfo(res->ai_addr, res->ai_addrlen,
	    buf, sizeof(buf), svcbuf, sizeof(svcbuf),
	    NI_NUMERICHOST | NI_NUMERICSERV)) != 0)
		errx(1, "getnameinfo: %s", gai_strerror(error));

	freeaddrinfo(res0);

	DPRINTF(("established connection to %s:%s", buf, svcbuf));

	/* initialize SSL */
	if ((ssl_ctx = SSL_CTX_new(SSLv23_client_method())) == NULL)
		errx(1, "SSL_CTX_new: %s", ssl_error());
	ssl = SSL_new(ssl_ctx);
	SSL_set_fd(ssl, rssfd);
	if (SSL_connect(ssl) != 1)
		errx(1, "SSL_connect: %s", ssl_error());

	DPRINTF(("initialized SSL"));

	len = snprintf(buf, sizeof(buf),
	    "GET %s HTTP/1.1\r\n"
	    "Host: %s\r\n"
	    "Connection: close\r\n",
	    rss_path, rss_host);
	if (len == -1)
		err(1, "snprintf");
	xwrite(ssl, buf, len);

	bsiz = (otoken.length + 3) * 4 / 3 + 1;
	DPRINTF(("base64: have %zu chars", bsiz));
	if ((p = malloc(bsiz)) == NULL)
		err(1, "malloc");
	base64_encode(otoken.value, p, otoken.length);
	xwrite(ssl, authline, strlen(authline));
	xwrite(ssl, p, strlen(p));
	free(p);
	xwrite(ssl, nl, strlen(nl));

	len = snprintf(buf, sizeof(buf), "\r\n");
	if (len == -1)
		err(1, "snprintf");
	xwrite(ssl, buf, len);

	while ((n = SSL_read(ssl, buf, sizeof(buf))) != 0 && n != -1)
		if (write(clifd, buf, n) != n)
			err(1, "write");
	if (n == -1)
		errx(1, "SSL_read: %s", ssl_error());

	SSL_free(ssl);
	SSL_CTX_free(ssl_ctx);

	gss_release_name(&minor, &name);
	if (gss_ctx != GSS_C_NO_CONTEXT)
		gss_delete_sec_context(&minor, &gss_ctx, GSS_C_NO_BUFFER);
}

int
main(int argc, char *argv[])
{
	char buf[BUFSIZ], svcbuf[BUFSIZ];
	struct sockaddr_storage ss;
	int error, clifd, s, c;
	socklen_t siz;

	progname = argv[0];

	ssl_init();
	conf_init();

	while ((c = getopt(argc, argv, "v")) != -1)
		switch (c) {
		case 'v':
			verbose++;
			break;
		default:
			usage();
		}

	s = setup();
	for (;;) {
		siz = sizeof(ss);
		memset(&ss, 0, sizeof(ss));
		clifd = accept(s, (struct sockaddr *)&ss, &siz);
		if (clifd == -1)
			err(1, "accept");
		if ((error = getnameinfo((struct sockaddr *)&ss, siz,
		    buf, sizeof(buf), svcbuf, sizeof(svcbuf),
		    NI_NUMERICHOST | NI_NUMERICSERV)) != 0)
			errx(1, "getnameinfo: %s", gai_strerror(error));
		DPRINTF(("received client connection from %s:%s",
		    buf, svcbuf));
		serve(clifd);
		close(clifd);
	}
	/* NOTREACHED */
}

__dead void
usage(void)
{
	fprintf(stderr, "usage: %s\n", progname);
	exit(1);
}
