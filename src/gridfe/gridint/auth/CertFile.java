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
	private final String xdef = "/tmp/x509up_u";
	private final String kdef = "/tmp/krb5cc_";
	private String xfile;
	private String kfile;

	public CertFile(Uid uid)
	{
		this.xfile = this.xdef + uid.intValue();
		this.kfile = this.kdef + uid.intValue();
	}
	
	public String getX509()
	{
		return this.xfile;
	}

	public String getKrbTkt()
	{
		return this.kfile;
	}
}

