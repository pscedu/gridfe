# $Id$

TARGET = jdep
OBJS = jdep.o q.o xalloc.o lbuf.o
SRCS = jdep.c q.c xalloc.c lbuf.c

all: ${TARGET}

.c.o:
	${CC} ${CFLAGS} -c $<

${TARGET}: ${OBJS}
	${CC} ${LIBS} -o $@ ${OBJS}

clean:
	rm -f ${OBJS} .depend ${TARGET}

depend:
	mkdep ${SRCS}
