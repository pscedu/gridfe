# $Id$

SYSROOT = ../..

include ${SYSROOT}/lib/mk/defs.mk

TARGET = splunderd
OBJS = splunderd.o
PREFIX = /usr/local
LDFLAGS += -lssl -lgssapi -lcrypto -L${KRB_PREFIX}/lib -lkrb5

include ${MAINMK}

${TARGET}: ${OBJS}
	${CC} ${LDFLAGS} -o $@ ${OBJS}

install: ${TARGET}
	install -m 755 ${TARGET} ${PREFIX}/bin
