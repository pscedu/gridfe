# $Id$

SYSROOT = ../..

include ${SYSROOT}/lib/mk/defs.mk

TARGET = jdep
OBJS = jdep.o q.o xalloc.o lbuf.o
CFLAGS += -Wall -g

include ${MAINMK}

${TARGET}: ${OBJS}
	${CC} ${LIBS} -o $@ ${OBJS}
