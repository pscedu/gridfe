# $Id$

SYSROOT = ../..

include ${SYSROOT}/lib/mk/defs.mk

TARGET = mod_fum.so
OBJS = mod_fum.o
CFLAGS += -Wall -rdynamic -shared -g -I${KX509_PREFIX}
LIBS = -lkrb5 -lkx -L${KX509_PREFIX}

TESTS += driver
TESTENV = LD_LIBRARY_PATH=`pwd`

include ${MAINMK}

${TARGET}: ${OBJS}
	${CC} ${CFLAGS} -Wl,-rpath,LIBDIR ${LIBS} -o $@ ${OBJS}

driver:
	${CC} -Wall -g -fPIC -ldl driver.c -o $@
