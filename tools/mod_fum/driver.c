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
	krb5_inst kinst;
	krb5_prefs kprefs;
	void *h;
	void (*init)(const char*, const char*);

	if (argc != 2 && argc != 3)
		usage();

	if ((h = dlopen(_PATH_MOD_FUM, RTLD_LAZY)) == NULL)
		errx(1, "%s: %s", _PATH_MOD_FUM, dlerror());
	if ((init = dlsym(h, "mf_main")) == NULL)
		errx(1, "dlsym: %s: %s", "", dlerror());

	(*init)(argv[1], argv[2]);

	dlclose(h);
	
	exit(0);
}

void
usage(void)
{
	extern char *__progname;
	fprintf(stderr, "usage: %s user pw\n", __progname);
	exit(1);
}
