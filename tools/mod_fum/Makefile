# $Id$

SYSROOT = ../..

include ${SYSROOT}/lib/mk/defs.mk

TARGET = mod_fum.so
OBJS = mod_fum.o
CFLAGS += -Wall -rdynamic -shared
LIBS = -lkrb5
TESTS += driver
TESTENV = LD_LIBRARY_PATH=`pwd`

include ${MAINMK}

${TARGET}: ${OBJS}
	${CC} -shared -Wl,-rpath,LIBDIR ${LIBS} -o $@ ${OBJS}

driver:
	${CC} -Wall -fPIC -ldl driver.c -o $@
