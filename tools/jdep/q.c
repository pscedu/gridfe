/* $Id$ */

#include <stdlib.h>

#include "q.h"
#include "xalloc.h"

struct workq *headq;

void
pushq(char *pkg)
{
	struct workq *q;

	q = xmalloc(sizeof(*q));
	q->pkg = pkg;
	q->next = headq;
	headq = q;
}

char *
popq(void)
{
	struct workq *q;
	char *pkg = NULL;

	if (headq != NULL) {
		q = headq;
		pkg = q->pkg;
		headq = q->next;
		free(q);
	}
	return pkg;
}
