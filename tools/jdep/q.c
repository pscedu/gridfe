/* $Id$ */

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
	char *pkg;
	struct workq *q;

	pkg = q->pkg;
	headq = q->next;
	free(q);
	return pkg;
}
