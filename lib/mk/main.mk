# $Id$

.SUFFIXES: .class .java

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

.c.o:
	${CC} ${CFLAGS} -c $<

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
	@rm -f .depend
	@for i in ${CLASSES}; do						\
		echo "env CLASSPATH=${CLASSPATH} ${JDEP} $${i%class}java";	\
		env CLASSPATH=${CLASSPATH} ${JDEP} $${i%class}java >> .depend	\
			|| exit 1;						\
	done
ifdef OBJS
	mkdep ${OBJS:.o=.c}
endif

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
		if [ X"$${i%.class}" = X"$$i" ]; then				\
			echo "./$$i";						\
			env ${TESTENV} ./$$i;					\
		else 								\
			echo "${JAVA} ${JFLAGS} $$i";				\
			${JAVA} ${JFLAGS} $$i || exit 1;			\
		fi;								\
	done

clean:
	@# XXX: test classes
	rm -f ${OBJS} ${TARGET} .depend ${TESTS} ${PRIVCLASSES}
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
