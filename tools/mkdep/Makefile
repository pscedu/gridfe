# $Id$

SYSROOT = ../..

include ${SYSROOT}/lib/mk/defs.mk

TARGET = mkdep
OBJS = mkdep.o
CFLAGS += -Wall -g

include ${MAINMK}

${TARGET}: ${OBJS}
	${CC} ${LIBS} -o $@ ${OBJS}
