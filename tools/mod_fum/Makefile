# $Id$

SYSROOT = ../..

include ${SYSROOT}/lib/mk/defs.mk

TARGET = mod_fum.so
OBJS = mod_fum.o
CFLAGS += -Wall -rdynamic -shared -g -I${KX509_PREFIX} -I/usr/include/httpd -I"/usr/include/apr-0"
LIBS = -lcrypto -lkrb5 -lkx509 -L${KX509_PREFIX}

TESTS += driver
TESTENV = LD_LIBRARY_PATH=`pwd`

APXS = /usr/sbin/apxs
APXS_FLAGS = -i -a
APXS_SOURCE = mod_fum.c
APSX_OBJS = mod_fum.o mod_fum.la mod_fum.lo mod_fum.slo

include ${MAINMK}

${TARGET}: ${OBJS}
	${CC} ${CFLAGS} -Wl,-rpath,LIBDIR ${LIBS} -o $@ ${OBJS}

install:
	${APXS} -I${KX509_PREFIX} ${APXS_FLAGS} -c ${APXS_SOURCE} ${LIBS}

driver:
	${CC} ${LIBS} -Wall -g -fPIC -ldl driver.c -o $@
