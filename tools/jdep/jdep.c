/* $Id$ */

#include <sys/param.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <ctype.h>
#include <dirent.h>
#include <err.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "lbuf.h"
#include "q.h"
#include "xalloc.h"

static void dumpfiles(const char *);
static void jdep(char *);
static void listdeps(char *);
static void procpkgs(char *);
static void usage(void);

struct workq *sawq, *procq;

int
main(int argc, char *argv[])
{
	/* XXX: process *.java in CWD. */
	if (argc < 2)
		usage();
	/* XXX: put . into CLASSPATH. */
	while (*++argv != NULL)
		if (strstr(*argv, ".java") == NULL)
			warnx("%s: invalid Java source file", *argv);
		else
			jdep(*argv);
	exit(0);
}

static void
jdep(char *fil)
{
	FILE *fp;
	int c, dquot, esc, squot;
	struct lbuf lb;
	char *pos, *tag = "import", *pkg;

	if ((fp = fopen(fil, "r")) == NULL)
		err(1, "%s", fil);

	pushq(&procq, xstrdup(fil));

	pos = tag;
	lbuf_init(&lb);
	esc = dquot = squot = 0;
	while ((c = fgetc(fp)) != EOF) {
		switch (c) {
		case '\\':
			esc = !esc;
			break;
		case '\'':
			if (esc)
				esc = !esc;
			else if (!dquot)
				squot = !squot;
			break;
		case '"':
			if (esc)
				esc = !esc;
			else if (!squot)
				dquot = !dquot;
			break;
		default:
			if (esc)
				esc = !esc;
			else if (squot || dquot)
				;
			else if (*pos == '\0') {
				/* Currently processing an import. */
				if (c == ';') {
					pos = tag;
					if ((pkg = lbuf_get(&lb)) !=
					    NULL) {
					    	lbuf_push(&lb, '\0');
						while (isspace(*pkg))
							pkg++;
						pushq(&sawq, xstrdup(pkg));
						lbuf_reset(&lb);
					}
				} else
					lbuf_push(&lb, (char)c);
			} else if (c == *pos)
				/* Currently processing 'import'. */
				pos++;
			else
				pos = tag;
		}
	}
	fclose(fp);
	lbuf_free(&lb);

	procpkgs(fil);
}

static void
procpkgs(char *fil)
{
	char *cp, *p, *pkg, **paths, **v;
	char dir[MAXPATHLEN];
	int pos, max, wild;
	struct stat st;

	cp = getenv("CLASSPATH");
	if (cp == NULL)
		cp = "";
	pos = max = 0;
	paths = xmalloc(sizeof(*paths));
	*paths = cp;
	for (p = cp; ; p++)
		switch (*p) {
		case '\0':
			if (++pos >= max) {
				max++;
				paths = xrealloc(paths, max * sizeof(*paths));
			}
			paths[pos] = "";
			goto find;
			/* NOTREACHED */
		case ':':
			if (++pos >= max) {
				max += 3;
				paths = xrealloc(paths, max * sizeof(*paths));
			}
			paths[pos] = p + 1;
			*p = '\0';
			break;
		}
find:
	while ((pkg = popq(&sawq)) != NULL) {
		wild = 0;
		for (p = pkg; *p != '\0'; p++)
			if (*p == '.')
				*p = '/';
		/* `if (p > pkg` is unnecessary. */
		if (p > pkg && *--p == '*') {
			*p = '\0';
			wild = 1;
		}
		for (v = paths; (*v)[0] != '\0'; v++) {
			/* XXX: yes, this is horribly wrong. */
			if (strstr(*v, ".jar") != NULL) {
				pushq(&procq, xstrdup(*v));
				continue;
			}
			snprintf(dir, sizeof(dir), "%s/%s", *v, pkg);
			if (stat(dir, &st) != -1) {
				if (wild)
					dumpfiles(dir);
				else
					pushq(&procq, xstrdup(*v));
				/*
				 * `break` would be nice, but it would
				 * skip any .jars in CLASSPATH.
				 */
			}
		}
		free(pkg);
	}
	free(paths);

	listdeps(fil);
}

static void
listdeps(char *fil)
{
	char *pkg;
	struct workq *q;

	if (procq != NULL)
		printf("%.*s.class: \\\n", strstr(fil, ".java") - fil,
		       fil);
	while ((pkg = popq(&procq)) != NULL) {
		for (q = procq; q != NULL; q = q->next)
			if (strcmp(q->buf, pkg) == 0)
				/*
				 * Skip it; it will be processed
				 * subsequently.
				 */
				goto next;
		printf("\t%s%s\n", pkg, procq == NULL ? "" : " \\");
next:
		free(pkg);
	}
}

static void
dumpfiles(const char *dir)
{
	DIR *dp;
	struct dirent *e;
	char *p;
	size_t siz;

	if ((dp = opendir(dir)) == NULL) {
		warn("%s", dir);
		return;
	}
	while ((e = readdir(dp)) != NULL) {
		if (e->d_name[0] == '.')
			continue;
		/* XXX: might match just a .class file. */
		if (strstr(e->d_name, ".class") != NULL) {
			siz = strlen(dir) + strlen(e->d_name) + 1;
			p = xmalloc(siz);
			snprintf(p, siz, "%s%s", dir, e->d_name);
			pushq(&procq, p);
		} else if ((p = strstr(e->d_name, ".java")) != NULL) {
			/* .java -> .class + '\0' */
			siz = strlen(dir) + strlen(e->d_name) + 2;
			p = xmalloc(siz);
			snprintf(p, siz, "%s%.*s.class", dir,
				 p - e->d_name, e->d_name);
			pushq(&sawq, p);

			/*
			 * There is probably an easier way to do this.
			 */
			siz--;
			p = xmalloc(siz);
			snprintf(p, siz, "%s%s", dir, e->d_name);
			pushq(&sawq, p);
		}

	}
#if 0
	if (error)
		warn("%s", dir);
#endif
	closedir(dp);
}

static void
usage(void)
{
	extern char *__progname;

	fprintf(stderr, "usage: %s file ...\n", __progname);
	exit(1);
}
