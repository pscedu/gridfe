# $Id$

MAINMK = ${SYSROOT}/lib/mk/main.mk
JAVAC = ${JAVA_PREFIX}/bin/javac
JAVA = ${JAVA_PREFIX}/bin/java
SCLASSPATH =					\
	${SYSROOT}/src:				\
	/usr/share/java/servlet.jar:		\
	${JAVA_PREFIX}/jre/lib/rt.jar:		\
	${COG_PREFIX}/lib/cog-jglobus.jar:	\
	${COG_PREFIX}/lib/puretls.jar:		\
	${COG_PREFIX}/lib/cryptix32.jar:	\
	${COG_PREFIX}/lib/cryptix-asn1.jar:	\
	${COG_PREFIX}/lib/jce-jdk13-125.jar:	\
	.
CLASSPATH = `echo ${SCLASSPATH} | tr -d '\t\n\ '`
CFLAGS += -Wall -W -g
JFLAGS = -classpath ${CLASSPATH}
JCFLAGS = -g -classpath ${CLASSPATH}
JDEP = ${SYSROOT}/tools/jdep/jdep
MKDEP = mkdep

-include ${SYSROOT}/lib/mk/local.mk
