# $Id$

SYSROOT = ../..

include ${SYSROOT}/lib/mk/defs.mk

MODULE = mod_fum
TARGET = ${MODULE}.so
OBJS = ${MODULE}.o
CFLAGS += -Wall -rdynamic -shared -g -I${KX509_PREFIX}
CFLAGS += -I${HTTPD_PREFIX} -I${APR_PREFIX}
LIBS = -lcrypto -lkrb5 -lkx509 -L${KX509_PREFIX}
CLEAN = ${MODULE}.la ${MODULE}.la ${MODULE}.slo

TESTS += driver
TESTENV = LD_LIBRARY_PATH=`pwd`

include ${MAINMK}

${TARGET}: ${OBJS}
	${CC} ${CFLAGS} -Wl,-rpath,LIBDIR ${LIBS} -o $@ ${OBJS}

driver:
	${CC} ${LIBS} -Wall -g -fPIC -ldl driver.c -o $@

install: ${TARGET}
	${APXS} -i -a -I${KX509_PREFIX} ${APXSFLAGS} ${TARGET}
