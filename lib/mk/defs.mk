# $Id$

MAINMK = ${SYSROOT}/lib/mk/main.mk
JAVAC = `which javac`
JAVA = `which java`
JFLAGS = -g -classpath ${SYSROOT}/lib:/usr/share/java/servlet.jar:${COG_INSTALL_PATH}/lib/cog-jglobus.jar:/usr/java/j2sdk1.4.2_04/jre/lib/rt.jar:${COG_INSTALL_PATH}/lib/puretls.jar:${COG_INSTALL_PATH}/lib/cryptix32.jar:${COG_INSTALL_PATH}/lib/cryptix-asn1.jar:.
