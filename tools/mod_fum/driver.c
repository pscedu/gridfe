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
	void (*def)(krb5_prefs_ptr);
	void (*pw)(krb5_prefs_ptr, char *, char *);
	void (*init)(krb5_inst_ptr, krb5_prefs_ptr);

	if (argc != 2 && argc != 3)
		usage();

	if ((h = dlopen(_PATH_MOD_FUM, RTLD_LAZY)) == NULL)
		errx(1, "%s: %s", _PATH_MOD_FUM, dlerror());
	if ((def = dlsym(h, "mf_kinit_set_defaults")) == NULL)
		errx(1, "dlsym: %s: %s", "", dlerror());
	if ((pw = dlsym(h, "mf_kinit_set_uap")) == NULL)
		errx(1, "dlsym: %s: %s", "", dlerror());
	if ((init = dlsym(h, "mf_kinit")) == NULL)
		errx(1, "dlsym: %s: %s", "", dlerror());

	(*def)(&kprefs);
	(*pw)(&kprefs, argv[1], argv[2]);
	(*init)(&kinst, &kprefs);

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
