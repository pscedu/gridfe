/* $Id$ */

CREATE TABLE gridints (
	uid	VARCHAR(32),
	gi	TINYBLOB,

	PRIMARY_KEY(uid)
);

/*
CREATE TABLE jobs (
	uid	VARCHAR(32),
	label	VARCHAR(50),
);
*/

CREATE TABLE hosts (
	uid	VARCHAR(32),
	host	VARCHAR(255),

	PRIMARY_KEY(uid),
	KEY(host)
);

/*
CREATE TABLE x509_certs (
);
*/
