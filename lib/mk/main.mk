# $Id$

.SUFFIXES: .class .java

TARGET = ${CLASSES}

all: ${TARGET}
	@# XXX: make recursion factorable
	@for i in ${SUBDIRS}; do						\
		echo -n "===> ";						\
		if [ -n "${DIRPREFIX}" ]; then					\
			echo -n ${DIRPREFIX};					\
		fi;								\
		echo $$i;							\
		(cd $$i && make DIRPREFIX=${DIRPREFIX}$$i/ $@) || exit 1;	\
		if [ -n "${DIRPREFIX}" ]; then					\
			echo "<=== ${DIRPREFIX}" | sed 's!/$$!!';		\
		fi;								\
	done

.java.class:
	${JAVAC} ${JCFLAGS} $< || exit 1

depend:
	@for i in ${SUBDIRS}; do						\
		echo -n "===> ";						\
		if [ -n "${DIRPREFIX}" ]; then					\
			echo -n ${DIRPREFIX};					\
		fi;								\
		echo $$i;							\
		(cd $$i && make DIRPREFIX=${DIRPREFIX}$$i/ $@) || exit 1;	\
		if [ -n "${DIRPREFIX}" ]; then					\
			echo "<=== ${DIRPREFIX}" | sed 's!/$$!!';		\
		fi;								\
	done
	rm -f .depend
	@for i in ${CLASSES}; do						\
		echo "$$i: $${i%class}java" >> .depend;				\
	done

test: all ${TESTS}
	@for i in ${SUBDIRS}; do						\
		echo -n "===> ";						\
		if [ -n "${DIRPREFIX}" ]; then					\
			echo -n ${DIRPREFIX};					\
		fi;								\
		echo $$i;							\
		(cd $$i && make DIRPREFIX=${DIRPREFIX}$$i/ $@) || exit 1;	\
		if [ -n "${DIRPREFIX}" ]; then					\
			echo "<=== ${DIRPREFIX}" | sed 's!/$$!!';		\
		fi;								\
	done
	@for i in ${TESTS}; do							\
		echo "${JAVA} ${JFLAGS} $$i";					\
		${JAVA} ${JFLAGS} $$i;						\
	done

clean:
	@# XXX: test classes
	rm -f ${CLASSES} .depend
	@for i in ${SUBDIRS}; do						\
		echo -n "===> ";						\
		if [ -n "${DIRPREFIX}" ]; then					\
			echo -n ${DIRPREFIX};					\
		fi;								\
		echo $$i;							\
		(cd $$i && make DIRPREFIX=${DIRPREFIX}$$i/ $@) || exit 1;	\
		if [ -n "${DIRPREFIX}" ]; then					\
			echo "<=== ${DIRPREFIX}" | sed 's!/$$!!';		\
		fi;								\
	done
