/* $Id$ */

#include <dlfcn.h>
#include <stdio.h>
#include <krb.h>

#define _PATH_MOD_FUM "mod_fum.so"

int
main(int argc, char *argv[])
{
	void *h;
	f;
	krb5_inst kinst;

	if ((h = dlopen(_PATH_MOD_FUM, RTLD_LAZY)) == NULL)
		errx("%s: %s", _PATH_MOD_FUM, dlerror());
	if ((f = dlsym(h, )) == NULL)
		errx("dlsym: %s: %s", "", dlerror());

	(*f)();

	dlclose(h);
}
