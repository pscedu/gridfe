# $Id$

SYSROOT = .

include ${SYSROOT}/lib/mk/defs.mk

SUBDIRS += tools src

include ${MAINMK}

build:
	make clean && make depend && make && make test
