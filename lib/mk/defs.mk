# $Id$

MAINMK = ${SYSROOT}/lib/mk/main.mk
JAVAC = `which javac`
JAVA = `which java`
JAVA_PREFIX = /usr/java/j2sdk1.4.2_05
COG_PREFIX = /usr/java/cog-1.2
SCLASSPATH =					\
	${SYSROOT}/src:				\
	/usr/share/java/servlet.jar:		\
	${JAVA_PREFIX}/jre/lib/rt.jar:		\
	${COG_PREFIX}/lib/cog-jglobus.jar:	\
	${COG_PREFIX}/lib/puretls.jar:		\
	${COG_PREFIX}/lib/cryptix32.jar:	\
	${COG_PREFIX}/lib/cryptix-asn1.jar:	\
	${COG_PREFIX}/lib/jce-jdk13-120.jar:	\
	.
CLASSPATH = `echo ${SCLASSPATH} | tr -d '\t\n\ '`
CFLAGS += -Wall -W -g
JFLAGS = -classpath ${CLASSPATH}
JCFLAGS = -g -classpath ${CLASSPATH}
JDEP = ${SYSROOT}/tools/jdep/jdep
MKDEP = mkdep

-include ${SYSROOT}/lib/mk/local.mk
