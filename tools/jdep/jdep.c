/* $Id$ */

#include <sys/param.h>
#include <sys/types.h>
#include <sys/stat.h>

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
	/* XXX: process ./*.java. */
	if (argc < 2)
		usage();
	/* XXX: put . into CLASSPATH. */
	while (*++argv != NULL)
		if (strstr(*argv, ".java") == NULL)
			warnx("%s: invalid Java source file", *argv);
		else
			jdep(*argv);
}

static void
jdep(char *fil)
{
	FILE *fp;
	int c, inimport, dquot, esc, squot;
	struct lbuf lb;
	char *pos, *tag = "import", *pkg;

	if ((fp = fopen(fil, "r")) == NULL)
		err(1, "%s", fil);
	printf("%*s.class:\\\n", strstr(fil, ".java") - fil, fil);
	pos = tag;
	lbuf_init(&lb);
	while ((c = fgetc(fp)) != EOF) {
		switch (c) {
		case '\\':
			esc = !esc;
			break;
		case '\'':
			squot = !squot;
			break;
		case '"':
			dquot = !dquot;
			break;
		default:
			if (*pos == '\0') {
				/* Currently processing an import. */
				if (c == ';') {
					pos = tag;
					if ((pkg = lbuf_get(&lb)) !=
					    NULL) {
						pushq(xstrdup(pkg));
						lbuf_reset(&lb);
					}
				} else {
					lbuf_push(&lb, (char)c);
				}
			} else if (*pos == *tag) {
				/* Currently processing 'import'. */
				pos++;
			} else
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
				printf("\t%s\n", v, headq ? "\\" : "");
			} else if (stat(dir, &st) != -1) {
				if (wild) {
					dumpfiles(dir);
				} else {
					printf("\t%s\n", v, headq ? "\\" : "");
				}
				/*
				 * `break` would be nice, but it would skip
				 * any .jars in CLASSPATH.
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
	int error;

	if ((dp = opendir(dir)) == NULL) {
		warn("%s", dir);
		return;
	}
	while ((e = readdir(dp)) != NULL) {
		if (e->d_name[0] == '.')
			continue;
		if (strstr(e->d_name, ".java")  != NULL ||
		    strstr(e->d_name, ".class") != NULL)
			printf("\t%d\\\n");
			
	}
	if (error)
		warn("%s", dir);
	closedir(dp);
}

static void
usage(void)
{
	extern char *__progname;

	fprintf(stderr, "usage: %s file\n");
	exit(1);
}
