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

#define IT_REL 1	/* #include "foo" */
#define IT_ABS 2	/* #include <foo> */

struct pathqe {
	const char	*pq_path;
	struct pathqe	*pq_next;
};

struct incqe {
	const char	*iq_file;
	struct incqe	*iq_next;
	int		 iq_type;
};

static		int   exists(const char *);
static		int   shift(FILE *, off_t);
static		void  freeiq(struct incqe *);
static		void  freepq(struct pathqe *);
static		void  getincs(const char *, struct incqe **);
static		void  mkdep(FILE *, const char *, struct pathqe *);
static		void  pushiq(struct incqe **, const char *, int);
static		void  pushpq(struct pathqe **, const char *);
static		void  split(const char *, int *, char ***);
static __dead	void  usage(void);
static const	char *find(const char *, struct pathqe *);

int
main(int argc, char *argv[])
{
	struct pathqe *pathqh = NULL;
	char *cflags;
	FILE *fp;
	int c;

	opterr = 0;
	pushpq(&pathqh, _PATH_USR_INCLUDE);
	while ((c = getopt(argc, argv, "I:")) != -1)
		switch (c) {
		case 'I':
			pushpq(&pathqh, optarg);
			break;
		}
	argv += optind;
	if (*argv == NULL)
		usage();
	/* Read CFLAGS from environment. */
	if ((cflags = getenv("CFLAGS")) != NULL) {
		char **cf_argv, **p;
		int cf_argc;

		split(cflags, &cf_argc, &cf_argv);
		optind = 0; /* XXX */
		while ((c = getopt(cf_argc, cf_argv, "I:")) != -1)
			switch (c) {
			case 'I':
				pushpq(&pathqh, optarg);
				break;
			}
		for (p = cf_argv; *p != NULL; p++)
			free(*p);
		free(cf_argv);
	}

	if ((fp = fopen(".depend", "rw")) == NULL)
		err(EX_NOINPUT, "open .depend"); /* XXX */
	while (*argv != NULL)
		mkdep(fp, *argv++, pathqh);
	freepq(pathqh);
	(void)fclose(fp);
	exit(EXIT_SUCCESS);
}

#define ALLOCINT 20

static void
split(const char *s, int *argcp, char ***argvp)
{
	size_t max, pos;
	const char *p;
	char **argv;

	*argcp = 1; /* Trailing NULL.  Will be deducted later. */
	for (p = s; *p != '\0'; p++) {
		if (isspace(*p)) {
			while (isspace(*++p))
				;
			p--;
			++*argcp;
		}
	}
	if ((*argvp = calloc(*argcp, sizeof(**argvp))) == NULL)
		return;
	argv = *argvp;
	max = 0;
	pos = 0;
	for (p = s; *p != '\0'; p++) {
		if (isspace(*p)) {
			while (isspace(*++p))
				;
			p--;
			if (pos >= max) {
				max += ALLOCINT;
				if ((*argv = realloc(*argv, max *
				     sizeof(**argv))) == NULL)
					err(EX_OSERR, "realloc");
			}
			(*argv)[pos] = '\0';
			*++argv = NULL;
			max = 0;
			pos = 0;
		} else {
			if (pos >= max) {
				max += ALLOCINT;
				if ((*argv = realloc(*argv, max *
				     sizeof(**argv))) == NULL)
					err(EX_OSERR, "realloc");
			}
			(*argv)[pos++] = *p;
		}
	}
	if (pos >= max) {
		max += ALLOCINT;
		if ((*argv = realloc(*argv, max * sizeof(**argv))) ==
		    NULL)
			err(EX_OSERR, "realloc");
	}
	(*argv)[pos] = '\0';
	*++argv = NULL;

	/* argc needs not count trailing NULL. */
	--*argcp;
}

static void
pushpq(struct pathqe **pqh, const char *s)
{
	struct pathqe *pq;

	if ((pq = malloc(sizeof(*pq))) == NULL)
		err(EX_OSERR, "malloc");
	pq->pq_path = s;
	pq->pq_next = *pqh;
	*pqh = pq;
}

static void
pushiq(struct incqe **iqh, const char *s, int type)
{
	struct incqe *iq;

	if ((iq = malloc(sizeof(*iq))) == NULL)
		err(EX_OSERR, "malloc");
	iq->iq_file = s;
	iq->iq_next = *iqh;
	iq->iq_type = type;
	*iqh = iq;
}

static void
freepq(struct pathqe *pq)
{
	struct pathqe *next;

	for (; pq != NULL; pq = next) {
		next = pq->pq_next;
		free(pq);
	}
}

static void
freeiq(struct incqe *iq)
{
	struct incqe *next;

	for (; iq != NULL; iq = next) {
		next = iq->iq_next;
		free(iq);
	}
}

/*
 * Since .depend entries are being removed, the contents that appear
 * after a removed entry will need to be shifted forward to fill the
 * gap created by the removal.
 */
static int
shift(FILE *fp, off_t dif)
{
	char buf[BUFSIZ];
	off_t oldpos;
	ssize_t siz;

	/*
	 * XXX: pread would be much nicer here but we can't mix stdio
	 * and raw.
	 */
	for (;;) {
		oldpos = ftell(fp);
		if (fseek(fp, dif, SEEK_CUR) == -1)
			return (1);
		siz = fread(buf, 1, sizeof(buf), fp);
		if (siz <= 0)
			break;
		if (fseek(fp, oldpos, SEEK_SET) == -1)
			return (1);
		siz = fwrite(buf, 1, siz, fp);
	}
	return (0);
}

static void
getincs(const char *s, struct incqe **iqp)
{
	char *p, *t, buf[BUFSIZ];
	FILE *fp;

	if ((fp = fopen(s, "r")) == NULL) {
		warn("open %s", s);
		return;
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
			pushiq(iqp, p, IT_ABS);
			break;
		case '"':
			if ((t = strchr(++p, '"')) == NULL)
				continue;
			*t = '\0';
			pushiq(iqp, p, IT_REL);
			break;
		default:
			continue;
			/* NOTREACHED */
		}
	}
	(void)fclose(fp);
}

static void
stripmke(const char *s, FILE *fp)
{
	off_t start, end;
	char buf[BUFSIZ];
	int c, gap, esc;
	size_t len;

	buf[0] = '\0';

	/* Look for entry in .depend. */
	fseek(fp, 0, SEEK_SET);
	len = strlen(s) - 1; /* .c to .o */
	/* XXX: this is horrid but the data should always be at the
	 * beginning of the line anyway.
	 */
	while (fgets(buf, sizeof(buf), fp) != NULL) {
		if (strncmp(buf, s, len) == 0 &&
		    buf[len] == 'o' && buf[len + 1] == ':') {
			esc = 0;
			start = ftell(fp) - strlen(buf);
			for (; (c = fgetc(fp)) != EOF; gap++) {
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
			break;
		}
	}
end:
	end = ftell(fp) - strlen(buf);
	shift(fp, end - start);
}

static void
mkdep(FILE *depfp, const char *fil, struct pathqe *pqh)
{
	struct incqe *iqh, *iq;
	const char *path;

	getincs(fil, &iqh);
	if (iqh == NULL)
		return;
	stripmke(fil, depfp);
	if (fseek(depfp, 0, SEEK_END) == -1)
		/* XXX */;
	(void)fprintf(depfp, "%s:", fil);
	for (iq = iqh; iq != NULL; iq = iq->iq_next) {
		/* Find full path. */
		if (iq->iq_type == IT_REL)
			(void)fprintf(depfp, " \\\n\t%s", iq->iq_file);
		else {
			if ((path = find(fil, pqh)) != NULL)
				(void)fprintf(depfp, " \\\n\t%s", path);
		}
	}
	(void)fprintf(depfp, "\n");
	freeiq(iqh);
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
