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
	@if [ "${CLASSES}" ]; then						\
		if ! [ -x "${JDEP}" ]; then					\
			(cd ${SYSROOT}/tools/jdep && make);			\
		fi;								\
		echo "${JDEP} ${CLASSES:.class=.java}";				\
		env CLASSPATH=${CLASSPATH} ${JDEP} ${CLASSES:.class=.java}	\
			> .depend || exit 1;					\
	fi
ifdef OBJS
	$(MKDEP) ${CFLAGS} ${OBJS:.o=.c}
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
			echo "${JAVA} ${JFLAGS} $${i%.class}";			\
			${JAVA} ${JFLAGS} $${i%.class} || exit 1;			\
		fi;								\
	done

clean:
	@# XXX: test classes
	rm -rf ${OBJS} ${TARGET} .depend ${TESTS} ${CLEAN}
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

restart: all
	service tomcat5 restart
