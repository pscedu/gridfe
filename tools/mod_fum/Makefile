# $Id$

SYSROOT = ../..

include ${SYSROOT}/lib/mk/defs.mk

MODULE = mod_fum
TARGET = ${MODULE}.so
OBJS = ${MODULE}.o
CFLAGS += -Wall -g -I${KX509_PREFIX} -I${APR_PREFIX}
LIBS = -lcrypto -lkrb5 -L${KX509_PREFIX} -lkx509
CLEAN = ${MODULE}.lo ${MODULE}.la ${MODULE}.slo .libs

TESTS += driver
TESTENV = LD_LIBRARY_PATH=`pwd`

include ${MAINMK}

${TARGET}: ${OBJS}
	${CC} -rdynamic -shared ${CFLAGS} -Wl,-rpath,LIBDIR ${LIBS} -o $@ ${OBJS}

install: ${TARGET}
	${APXS} -i -a ${TARGET}

driver: driver.c
	${CC} ${CFLAGS} ${LIBS} -fPIC -ldl driver.c -o $@
