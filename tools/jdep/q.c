/* $Id$ */

#include <stdlib.h>

#include "q.h"
#include "xalloc.h"

void
pushq(struct workq **headq, char *buf)
{
	struct workq *q;

	q = xmalloc(sizeof(*q));
	q->buf = buf;
	q->next = *headq;
	*headq = q;
}

char *
popq(struct workq **headq)
{
	struct workq *q;
	char *buf = NULL;

	if (*headq != NULL) {
		q = *headq;
		buf = q->buf;
		*headq = q->next;
		free(q);
	}
	return buf;
}
