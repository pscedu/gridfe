# $Id$

all clean depend: ${TARGET}
	@for i in ${SUBDIRS}; do					\
		echo -n "===> ";					\
		if [ -n "${DIRPREFIX}" ]; then				\
			echo -n ${DIRPREFIX};				\
		fi;							\
		echo $$i;						\
		(cd $$i && make DIRPREFIX=${DIRPREFIX}$$i/ $@);		\
		if [ -n "${DIRPREFIX}" ]; then				\
			echo "<=== ${DIRPREFIX}" | sed 's!/$$!!';	\
		fi;							\
	done
