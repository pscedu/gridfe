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

static void jdep(char *);
static void procpkgs(void);
static void dumpfiles(char *);
static void usage(void);

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
	printf("%.*s.class: \\\n", strstr(fil, ".java") - fil, fil);
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
						pushq(xstrdup(pkg));
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

	procpkgs();
	printf("\t%s\n", fil);
}

static void
procpkgs(void)
{
	char *cp, *p, *pkg, **paths, **v;
	char dir[MAXPATHLEN];
	int pos, max, wild;
	struct stat st;

	cp = getenv("CLASSPATH");
	pos = max = 0;
	paths = xmalloc(sizeof(*paths));
	*paths = cp;
	for (p = cp; ; p++)
		switch (*p) {
		case '\0':
			if (++pos >= max) {
				max++;
				paths = xrealloc(paths, max);
			}
			paths[pos] = "";
			goto find;
			/* NOTREACHED */
		case ':':
			if (++pos >= max) {
				max += 3;
				paths = xrealloc(paths, max);
			}
			paths[pos] = p + 1;
			*p = '\0';
			break;
		}
find:
	while ((pkg = popq()) != NULL) {
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
			snprintf(dir, sizeof(dir), "%s/%s", *v, pkg);
			/* XXX: yes, this is horribly wrong. */
			if (strstr(*v, ".jar") != NULL) {
				printf("\t%s \n", *v);
			} else if (stat(dir, &st) != -1) {
				if (wild) {
					dumpfiles(dir);
				} else {
					printf("\t%s \n", *v);
				}
				/*
				 * `break` would be nice, but it would
				 * skip any .jars in CLASSPATH.
				 */
			}
		}
		free(pkg);
	}
}

static void
dumpfiles(char *dir)
{
	DIR *dp;
	struct dirent *e;
	char *p;

	if ((dp = opendir(dir)) == NULL) {
		warn("%s", dir);
		return;
	}
	while ((e = readdir(dp)) != NULL) {
		if (e->d_name[0] == '.')
			continue;
		/* XXX: might match just a .class file. */
		if ((p = strstr(e->d_name, ".java")) != NULL) {
			printf("\t%s%.*s.class \\\n", dir,
			       p - e->d_name, e->d_name);
			printf("\t%s%s \\\n", dir, e->d_name);
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
