/* $Id$ */

struct workq {
	char *buf;
	struct workq *next;
};

void  pushq(struct workq **, char *);
char *popq(struct workq **);
