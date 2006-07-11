/* $Id$ */

DROP TABLE IF EXISTS gridints;
CREATE TABLE gridints (
	uid	VARCHAR(32)	NOT NULL,
	gi	TINYBLOB,

	PRIMARY KEY(uid)
);

/*

CREATE TABLE jobs (
	uid	VARCHAR(32),
	label	VARCHAR(50),
)

*/

DROP TABLE IF EXISTS hosts;
CREATE TABLE hosts (
	uid	VARCHAR(32)	NOT NULL,
	host	VARCHAR(255),
	type	VARCHAR(32),

	KEY(uid),
	KEY(host)
);

DROP TABLE IF EXISTS x509_certs;
CREATE TABLE x509_certs (
	uid	VARCHAR(32)	NOT NULL,
	cert	TEXT		NOT NULL,

	PRIMARY KEY(uid)
);
