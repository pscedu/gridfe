/* $Id$ */

#include <sys/param.h>
#include <sys/stat.h>

#include <ctype.h>
#include <err.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sysexits.h>
#include <unistd.h>

#ifndef MIN
#define MIN(a, b) ((a) < (b) ? (a) : (b))
#endif

#ifndef __dead
#define __dead
#endif

#define _PATH_USR_INCLUDE "/usr/include"

struct pathqe {
	const char	*pq_path;
	struct pathqe	*pq_next;
};

static		int   exists(const char *);
static		void  mkdep(FILE *, const char *, struct pathqe *);
static		void  freeq(struct pathqe *);
static		void  push(struct pathqe **, const char *);
static		void  shift(FILE *, off_t);
static const	char *find(const char *, struct pathqe *);
static __dead	void  usage(void);

int
main(int argc, char *argv[])
{
	struct pathqe *pathqh = NULL;
	extern char *optarg;
	extern int optind;
	extern int opterr;
	off_t off;
	FILE *fp;
	int c;

	opterr = 0;
	push(&pathqh, _PATH_USR_INCLUDE);
	while ((c = getopt(argc, argv, "I:")) != -1) {
		switch (c) {
		case 'I':
			push(&pathqh, optarg);
			break;
		default:
			/*
			 * Ignore.
			 * We cannot possibly handle every option
			 * that can go to cc.
			 */
			break;
		}
	}
	argv += optind;

	if (*argv == NULL)
		usage();

	if ((fp = fopen(".depend", "rw")) == NULL)
		err(EX_NOINPUT, "open .depend"); /* XXX */
	while (*argv != NULL)
		mkdep(fp, *argv, pathqh);
	freeq(pathqh);
	(void)fclose(fp);
	exit(EXIT_SUCCESS);
}

static void
push(struct pathqe **pqh, const char *s)
{
	struct pathqe *pq;

	if ((pq = malloc(sizeof(*pq))) == NULL)
		err(EX_OSERR, "malloc");
	pq->pq_path = s;
	pq->pq_next = *pqh;
	*pqh = pq;
}

static void
freeq(struct pathqe *pq)
{
	struct pathqe *next;

	for (; pq != NULL; pq = next) {
		next = pq->pq_next;
		free(pq);
	}
}

static int
shift(FILE *fp, off_t dif)
{
	char buf[BUFSIZ];
	ssize_t siz;

	do {
		if (fseek(fp, dif, SEEK_CUR) == -1)
			return (1);
		siz = fread(buf, 1, MIN(sizeof(buf), off), fp);
		if (siz > 0) {
			if (fseek(fp, -1 * (dif + siz), SEEK_CUR) == -1)
				return (1);
			fwrite(buf, 1, siz, fp);
			if (fseek(fp, siz, SEEK_CUR) == -1)
				return (1);
		}
	} while (siz > 0);
	return (0);
}

getincs()
{
	if ((fp = fopen(s, "r")) == NULL) {
		warn("open %s", s);
		return (0);
	}
	while (fgets(buf, sizeof(buf), fp) != NULL) {
		p = buf;
		while (isspace(*p))
			p++;
		if (*p != '#')
			continue;
		while (isspace(*p))
			p++;
		if (strcmp(p, "include") != 0)
			continue;
		p += sizeof("include");
		while (isspace(*p))
			p++;
		switch (*p) {
			case '<':
				if ((t = strchr(++p, '>')) == NULL)
					continue;
				*t = '\0';
				push(&incqh, p);
				break;
			case '"':
				if ((t = strchr(++p, '"')) == NULL)
					continue;
				*t = '\0';
				push(&incqh, p);
				break;
			default:
				continue;
				/* NOTREACHED */
		}
	}
	incqh = NULL;
	(void)fclose(fp);
}

stripmke()
{
	/* Look for entry in .depend. */
	fseek(depfp, 0, SEEK_SET);
	len = strlen(s);
	while (fgets(buf, sizeof(buf), depfp) != NULL) {
		if (strncmp(buf, s, len - 1) == 0 &&
		    buf[len - 1] == 'o' && buf[len] == ':') {
			int esc = 0;
			for (; (c = fgetc(depfp)) != EOF; gap++) {
				switch (c) {
				case '\\':
					esc = !esc;
					break;
				case '\n':
					if (!esc)
						goto end;
					/* FALLTHROUGH */
				default:
					esc = 0;
					break;
				}
			}
		}
	}
end:
	;
}

static void
mkdep(FILE *depfp, const char *s, struct pathqe *incpqh)
{
	struct pathqe *incqh, *incq;
	char buf[BUFSIZ], *p, *t;
	off_t shift, pos;
	size_t len;
	char *path;
	FILE *fp;
	int gap;

	getincs(&incqh, s);

	if (incqh == NULL)
		return (0);


	pos = ftell(depfp);
	fseek(depfp, 0, SEEK_END);
	for (incq = incqh; incq != NULL; inqc = inqc->pq_next) {
		/* Find full path. */
		if ((path = find(s, incpqh)) != NULL) {
			printf(depfp, "\t%s", path);
		}
	}
	(void)printf(depfp, "\n");
	freeq(incqh);

	fseek(depfp, pos, SEEK_SET);
	shift(fp, off);
	return (shift);
}

static const char *
find(const char *s, struct pathqe *pqh)
{
	static char fil[MAXPATHLEN];

	for (; pqh != NULL; pqh = pqh->pq_next) {
		snprintf(fil, sizeof(fil), "%s/%s", pqh->pq_path, s);
		if (exists(fil))
			return (fil);
	}
	return (NULL);
}

static int
exists(const char *s)
{
	struct stat stb;

	if (stat(s, &stb) != -1)
		return (1);
	return (0);
}

static __dead void
usage(void)
{
	extern char *__progname;

	(void)fprintf(stderr, "usage: %s file ...\n", __progname);
	exit(EX_USAGE);
}
