/* $Id$ */

package gridfe.gridint.auth;

import jasp.Uid;

/* Class to Encapsulate the Location for the X.509 Certificate */
public class CertFile
{
	/*
	** Certificates are normally stored in /tmp/x509_u_!!!
	** where !!! is the userid
	*/
	private final String def = "/tmp/x509up_u";
	private String file;

	public CertFile(Uid uid)
	{
		this.file = this.def + uid.intValue();
	}

	public String toString()
	{
		return this.file;
	}
}

