/* $Id$ */

struct workq {
	char *pkg;
	struct workq *next;
};

extern struct workq *headq;

void  pushq(char *);
char *popq(void);
