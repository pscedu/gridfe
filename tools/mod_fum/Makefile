# $Id$

SYSROOT = ../..

include ${SYSROOT}/lib/mk/defs.mk

MODULE = mod_fum
TARGET = ${MODULE}.so
OBJS = ${MODULE}.o gss.o
CFLAGS += -I${KX509_PREFIX} -I${APR_PREFIX}
CFLAGS += -I${HTTPD_PREFIX} -I${KRB_PREFIX}/include
LIBS = -lcrypto -L${KRB_PREFIX}/lib -lkrb5 -lkx509
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
