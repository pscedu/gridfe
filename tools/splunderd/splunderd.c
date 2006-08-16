/* $Id$ */

#include <sys/param.h>
#include <sys/socket.h>

#include <err.h>
#include <errno.h>
#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <krb5.h>

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
init_conf(void)
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
	const char *lastcause = NULL;
	int error, save_errno, s;

	memset(&hints, 0, sizeof(hints));
	hints.ai_family = PF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE;
	error = getaddrinfo(local_host, local_port, &hints, &res0);
	if (error)
		errx(1, "%s", gai_strerror(error));

	s = -1;
	for (res = res0; res; res = res->ai_next) {
		s = socket(res->ai_family, res->ai_socktype,
		    res->ai_protocol);
		if (s == -1) {
			lastcause = "socket";
			continue;
		}
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
	if (s != -1)
		err(1, "%s", lastcause);
	freeaddrinfo(res0);
	/* XXX print the truth */
	DPRINTF(("listening on %s:%s", local_host, local_port));
	return (s);
}

const char *
ssl_error(void)
{
#define ERRBUF_LEN 120
        static char errbuf[ERRBUF_LEN];

        return (ERR_error_string(ERR_get_error(), errbuf));
}

void
ssl_init(void)
{
	SSL_library_init();
	OpenSSL_add_all_algorithms();
	SSL_load_error_strings();
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
	DPRINTF(("base64: wrote %d chars\n", i));
}

gss_name_t	 gss_server;
gss_ctx_id_t	 gss_ctx;
OM_uint32	 gss_minor;
gss_buffer_desc	 gss_otoken = GSS_C_EMPTY_BUFFER;

void
gss_finish(void)
{
	gss_release_name(&gss_minor, &gss_server);
	if (gss_ctx != GSS_C_NO_CONTEXT)
		gss_delete_sec_context(&gss_minor,
		    &gss_ctx, GSS_C_NO_BUFFER);
}

void
gss_build_auth(const struct ustream *us)
{
	const char authline[] = "Authorization: Negotiate ";
	const char nl[] = "\r\n";
	size_t bsiz;
	char *p;

	bsiz = (gss_otoken.length + 3) * 4 / 3 + 1;
	DPRINTF(("base64: have %d chars\n", bsiz));
	if ((p = malloc(bsiz)) == NULL)
		err(1, "malloc");
	base64_encode(gss_otoken.value, p, gss_otoken.length);

	if (us_write(us, authline, strlen(authline)) != strlen(authline))
		err(1, "us_write");

	if (us_write(us, p, strlen(p)) != (ssize_t)strlen(p))
		err(1, "us_write");
	free(p);

	if (us_write(us, nl, strlen(nl)) != strlen(nl))
		err(1, "us_write");

	gss_finish();
}

int
gss_valid(const char *host)
{
	static int valid = 1;

	gss_OID_desc krb5_oid = {9, "\x2a\x86\x48\x86\xf7\x12\x01\x02\x02"};
	gss_buffer_desc itoken = GSS_C_EMPTY_BUFFER;
	OM_uint32 major, rflags, rtime;
	const char service[] = "HTTP";
	gss_OID oid;

	/* Default to MIT Kerberos */
	oid = &krb5_oid;

	/* itoken = "HTTP/f.q.d.n" aka "HTTP@hostname.foo.bar" */
	if ((itoken.length = asprintf((char **)&itoken.value,
	    "%s@%s", service, host)) == (size_t)-1)
		err(1, "asprintf");

	/* Convert the printable name to an internal format */
	major = gss_import_name(&gss_minor, &itoken,
	    GSS_C_NT_HOSTBASED_SERVICE, &gss_server);

	free(itoken.value);

	if (GSS_ERROR(major))
		errx(1, "gss_import_name");

	/* Initiate a security context */
	rflags = 0;
	rtime = GSS_C_INDEFINITE;
	gss_ctx = GSS_C_NO_CONTEXT;
	major = gss_init_sec_context(&gss_minor, GSS_C_NO_CREDENTIAL,
	    &gss_ctx, gss_server, oid, rflags, rtime,
	    GSS_C_NO_CHANNEL_BINDINGS, GSS_C_NO_BUFFER,
	    NULL, &gss_otoken, NULL, NULL);

	if (GSS_ERROR(major) || gss_otoken.length == 0) {
		gss_finish();
		warnx("gss_init_sec_context");
		valid = 0;
	}
	return (valid);
}

void
serve(int clifd)
{
	struct addrinfo hints, *res, *res0;
	int rssfd, error, save_errno, len;
	const char *lastcause = NULL;
	char buf[BUFSIZ];
	ssize_t n;

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
	if (rssfd != -1)
		err(1, "%s", lastcause);
	freeaddrinfo(res0);

	/* XXX print the truth */
	DPRINTF(("established connection to %s:%s", rss_host, rss_port));

	len = snprintf(buf, sizeof(buf),
	    "GET %s HTTP/1.1\r\n"
	    "Host: %s\r\n"
	    "Connection: close\r\n",
	    rss_path, rss_host);
	if (len == -1)
		err(1, "snprintf");
	if (write(rssfd, buf, (size_t)len) != (ssize_t)len)
		err(1, "write");








	bsiz = (gss_otoken.length + 3) * 4 / 3 + 1;
	DPRINTF(("base64: have %d chars\n", bsiz));
	if ((p = malloc(bsiz)) == NULL)
		err(1, "malloc");
	base64_encode(gss_otoken.value, p, gss_otoken.length);

	if (us_write(us, authline, strlen(authline)) != strlen(authline))
		err(1, "us_write");

	if (us_write(us, p, strlen(p)) != (ssize_t)strlen(p))
		err(1, "us_write");
	free(p);

	if (us_write(us, nl, strlen(nl)) != strlen(nl))
		err(1, "us_write");

	gss_finish();





	len = snprintf(buf, sizeof(buf), "\r\n");
	if (len == -1)
		err(1, "snprintf");
	if (write(rssfd, buf, (size_t)len) != (ssize_t)len)
		err(1, "write");

	while ((n = SSL_read(rssfd, buf, sizeof(buf))) != 0 &&
	    n != -1)
		if (write(clifd, buf, n) != n)
			err(1, "write");
	if (n == -1)
		errx(1, "SSL_read: %s", ssl_error());
}

int
main(int argc, char *argv[])
{
	int clifd, s, c;
	socklen_t siz;

	progname = argv[0];

	while ((c = getopt(argc, argv, "v")) != -1)
		switch (c) {
		case 'v':
			verbose++;
			break;
		default:
			usage();
		}

	init_conf();
	s = setup();
	siz = 0;
	for (;;) {
		clifd = accept(s, NULL, &siz);
		if (clifd == -1)
			err(1, "accept");
		DPRINTF(("received client connection"));
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
