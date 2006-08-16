/* $Id$ */

#include <stdio.h>

#ifndef __dead
#define __dead __attribute__((__noreturn__))
#endif

#define LISTENQ	5

char		rss_host[MAXHOSTNAMELEN];
char		rss_port[BUFSIZ];
char		rss_path[BUFSIZ];

char		local_host[MAXHOSTNAMELEN];
char		local_port[BUFSIZ];

int		verbose;
const char	*progname;

#define DPRINTF(x)			\
	if (verbose)			\
		warnx(x)

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
		clifd = accept(s, NULL, siz);
		if (clifd == -1)
			err(1, "accept");
		serve(clifd);
		close(clifd);
	}
	/* NOTREACHED */
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
	return (s);
}

__dead void
serve(int clifd)
{
	struct addrinfo hints, *res, *res0;
	int rssfd, error, save_errno, len;
	const char *lastcause = NULL;

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
		err(1, "%s", cause);
	freeaddrinfo(res0);

	len = snprintf(buf, sizeof(buf),
	    "GET %s HTTP/1.1\r\n"
	    "Host: %s\r\n"
	    "Connection: close\r\n",
	    rss_path, rss_host);
	if (len == -1)
		err(1, "snprintf");
	if (write(rssfd, buf, (size_t)len) != (ssize_t)len)
		err(1, "write");



	len = snprintf(buf, sizeof(buf), "\r\n");
	if (len == -1)
		err(1, "snprintf");
	if (write(rssfd, buf, (size_t)len) != (ssize_t)len)
		err(1, "write");
}

__dead void
usage(void)
{
	fprintf(stderr, "usage: %s\n", progname);
	exit(1);
}
