# $Id$

MAINMK = ${SYSROOT}/lib/mk/main.mk
JAVAC = `which javac`
JAVA = `which java`
JAVA_INSTALL_PATH = /usr/java/j2sdk1.4.2_04
COG_INSTALL_PATH = /usr/java/cog-1.2
CLASSPATH =						\
	${SYSROOT}/lib:					\
	/usr/share/java/servlet.jar:			\
	${JAVA_INSTALL_PATH}/jre/lib/rt.jar:		\
	${COG_INSTALL_PATH}/lib/cog-jglobus.jar:	\
	${COG_INSTALL_PATH}/lib/puretls.jar:		\
	${COG_INSTALL_PATH}/lib/cryptix32.jar:		\
	${COG_INSTALL_PATH}/lib/cryptix-asn1.jar:	\
	.
JFLAGS = -classpath `echo ${CLASSPATH} | tr -d '\t\n\ '`
JCFLAGS = -g -classpath `echo ${CLASSPATH} | tr -d '\t\n\ '`
JDEP = ${SYSROOT}/tools/jdep/jdep
