/* $Id$ */

struct lbuf {
	char *buf;
	int max, pos;
};

void  lbuf_init(struct lbuf *);
void  lbuf_push(struct lbuf *, char);
void  lbuf_reset(struct lbuf *);
char *lbuf_get(struct lbuf *);
void  lbuf_free(struct lbuf *);
