/* $Id$ */

#include <stdio.h>
#include <stdlib.h>

#include "lbuf.h"
#include "xalloc.h"

void
lbuf_init(struct lbuf *lb)
{
	lb->buf = NULL;
	lb->max = lb->pos = -1;
}

void
lbuf_push(struct lbuf *lb, char c)
{
	if (++lb->pos >= lb->max) {
		lb->max += 30;
		lb->buf = xrealloc(lb->buf, lb->max);
	}
	lb->buf[lb->pos] = c;
}

void
lbuf_reset(struct lbuf *lb)
{
	lb->pos = -1;
}

char *
lbuf_get(struct lbuf *lb)
{
	return lb->buf;
}

void
lbuf_free(struct lbuf *lb)
{
	free(lb->buf);
	lb->buf = NULL;
}
