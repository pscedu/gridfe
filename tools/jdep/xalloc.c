/* $Id$ */

#include <err.h>
#include <stdio.h>
#include <stdlib.h>

#include "xalloc.h"

void *
xmalloc(size_t siz)
{
	void *p;

	if ((p = malloc(siz)) == NULL)
		err(1, NULL);
	return p;
}

void *
xrealloc(void *t, size_t siz)
{
	void *p;

	if ((p = realloc(t, siz)) == NULL)
		err(1, NULL);
	return p;
}

char *
xstrdup(const char *s)
{
	char *p;

	if ((p = strdup(s)) == NULL)
		err(1, NULL);
	return p;
}
