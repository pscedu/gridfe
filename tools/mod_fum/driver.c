/* $Id$ */

#include <dlfcn.h>
#include <err.h>
#include <krb5.h>
#include <stdio.h>
#include <stdlib.h>

#define _PATH_MOD_FUM "mod_fum.so"

static void usage(void);
int mod_fum_main(const char *, const char *);

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
	if ((init = dlsym(h, "mod_fum_main")) == NULL)
		errx(1, "dlsym: %s: %s", "mod_fum_main", dlerror());

	(void)(*init)(username, password);
	(void)dlclose(h);
	exit(0);
}

static void
usage(void)
{
	extern char *__progname;

	(void)fprintf(stderr, "usage: %s [username [password]]\n", __progname);
	exit(1);
}
