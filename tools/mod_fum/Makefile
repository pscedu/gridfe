# $Id$

SYSROOT = ../..
TARGET = mod_fum.so
OBJS = mod_fum.o
CFLAGS += -Wall
LIBS = -lkrb5

include ${SYSROOT}/lib/mk/defs.mk

${TARGET}: ${OBJS}
	${CC} -shared -o $@ ${LIBS} ${OBJS}

include ${MAINMK}
