# $Id$

SYSROOT = ../..
TARGET = jdep
OBJS = jdep.o q.o xalloc.o lbuf.o
CFLAGS += -Wall -g

include ${SYSROOT}/lib/mk/defs.mk

${TARGET}: ${OBJS}
	${CC} ${LIBS} -o $@ ${OBJS}

include ${MAINMK}
