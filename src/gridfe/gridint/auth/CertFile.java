/* $Id$ */

package gridfe.gridint.auth;

import jasp.Uid;

/* Class to Encapsulate the Location for the X.509 Certificate */
public class CertFile
{
	/*
	** Certificates are normally stored in /tmp/x509up_u!!!
	** where !!! is the userid
	*/
	private final String xdef = "/tmp/x509up_u";
	private final String kdef = "/tmp/krb5cc_";

	/*
	** These are what mod_fum creates so they dont
	** conflict with console login credentials that
	** may exist already... Use these when deployed.
	**
	** XXX - put this stuff in some configuration
	** file to be read in... kinda nasty hard coded
	** the way it is for developement purposes!
	*/
	//private final String xdef = "/tmp/x509up_fum_u";
	//private final String kdef = "/tmp/krb5cc_fum_";

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
