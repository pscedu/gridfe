/* $Id$ */

#include <dlfcn.h>
#include <err.h>
#include <krb5.h>
#include <stdio.h>
#include <stdlib.h>

#include "mod_fum.h"

#define _PATH_MOD_FUM "mod_fum.so"

void usage(void);

int
main(int argc, char *argv[])
{
	void *h;
	void (*init)(const char *, const char *);
	char *username, *password;

	password = NULL;
	switch (argc) {
	case 3:
		password = argv[2];
		/* FALLTHROUGH */
	case 2:
		username = argv[1];
		break;
	case 1:
		username = getenv("USER");
		break;
	default:
		usage();
		/* NOTREACHED */
	}

	if ((h = dlopen(_PATH_MOD_FUM, RTLD_LAZY)) == NULL)
		errx(1, "%s: %s", _PATH_MOD_FUM, dlerror());
	if ((init = dlsym(h, "mf_main")) == NULL)
		errx(1, "dlsym: %s: %s", "mf_main", dlerror());

	(*init)(username, password);
	dlclose(h);
	exit(0);
}

void
usage(void)
{
	extern char *__progname;

	fprintf(stderr, "usage: %s [username [password]]\n", __progname);
	exit(1);
}
