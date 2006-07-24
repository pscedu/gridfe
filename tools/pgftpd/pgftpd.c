/* $Id$ */

#include <sys/types.h>
#include <sys/socket.h>

#include <netinet/in.h>
#include <arpa/inet.h>

#include <err.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#define Q 100
#define PORT 24650

void usage(void);

const char *progname;

void
serve(int clifd)
{

}

int
main(int argc, char *argv[])
{
	struct sockaddr_in sin;
	int clifd, s, c;
	socklen_t siz;

	progname = argv[0];
	while ((c = getopt(argc, argv, "")) != -1)
		switch (c) {
		default:
			usage();
		}

	if ((s = socket(PF_INET, SOCK_STREAM, 0)) == -1)
		err(1, "socket");

	sin.sin_family = AF_INET;
#ifdef HAVE_SA_LEN
	sin.sin_len = sizeof(sin);
#endif
	sin.sin_port = PORT;
	sin.sin_addr.s_addr = htonl(INADDR_ANY);

	siz = sizeof(sin);
	if (bind(s, (struct sockaddr *)&sin, siz) == -1)
		err(1, "bind");
	if (listen(s, Q) == -1)
		err(1, "listen");

	switch (fork()) {
	case -1:
		err(1, "fork");
	case 0:
		for (;;) {
			if ((clifd = accept(s, (struct sockaddr *)&sin,
			    &siz)) == -1)
				err(1, "accept");
			switch (fork()) {
			case -1:
				err(1, "fork");
			case 0:
				close(s);
				serve(clifd);
				close(clifd);
				exit(0);
				break;
			default:
				close(clifd);
				break;
			}
		}
		/* NOTREACHED */
	}

	close(s);
	exit(0);
}

void
usage(void)
{
	fprintf(stderr, "usage: %s\n", progname);
	exit(1);
}
