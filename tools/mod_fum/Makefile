# $Id$

SYSROOT = ../..
TARGET = mod_fum.so
OBJS = mod_fum.o
CFLAGS += -Wall
LIBS =

include ${SYSROOT}/lib/mk/defs.mk

${TARGET}: ${OBJS}
	${CC} -shared -o $@ ${LIBS} ${OBJS}

include ${MAINMK}
