# $Id$

MAINMK = ${SYSROOT}/lib/mk/main.mk
JAVAC = `which javac`
JAVA = `which java`
JAVA_INSTALL_PATH = /usr/java/j2sdk1.4.2_05
COG_INSTALL_PATH = /usr/java/cog-1.2
SCLASSPATH =						\
	${SYSROOT}/src:					\
	/usr/share/java/servlet.jar:			\
	${JAVA_INSTALL_PATH}/jre/lib/rt.jar:		\
	${COG_INSTALL_PATH}/lib/cog-jglobus.jar:	\
	${COG_INSTALL_PATH}/lib/puretls.jar:		\
	${COG_INSTALL_PATH}/lib/cryptix32.jar:		\
	${COG_INSTALL_PATH}/lib/cryptix-asn1.jar:	\
	.
CLASSPATH = `echo ${SCLASSPATH} | tr -d '\t\n\ '`
JFLAGS = -classpath ${CLASSPATH}
JCFLAGS = -g -classpath ${CLASSPATH}
JDEP = ${SYSROOT}/tools/jdep/jdep
KX509_PREFIX = /usr/local/kx509/src/lib
APR_PREFIX = /usr/include/httpd
APXS = /usr/sbin/apxs
KRB_PREFIX = /usr/kerberos
