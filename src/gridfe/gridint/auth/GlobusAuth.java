/* $Id$ */

/*
** GlobusAuth.java
**
** Obtains a GlobusCredential from 
** a file which is a valid X.509 Certificate.
*/
package gridint.auth;

import org.globus.gsi.*;
import org.globus.gsi.gssapi.*;
import java.security.PrivateKey;
import org.ietf.jgss.*;
import jasp.Uid;

public class GlobusAuth
{
	private GlobusCredential gc = null;
	private String file;
	private Uid uid;

	/*
	** X.509 Standard for files /tmp/x509up_uXXX
	** where XXX is the userid
	*/
	private final String def = "/tmp/x509up_u";


	public GlobusAuth(Uid uid)
	{
		this.file = this.def + uid.intValue();
	}

	public GlobusAuth(int uid)
	{
		this.uid = new Uid(uid);
		this.file = this.def + this.uid.intValue();
	}

	/*
	public GlobusAuth(String uid)
	{
		this.uid = new Uid(uid);
		this.file = this.def + this.uid.intValue();
	}
	*/

	/* Overide Default X.509 Certificate File*/
	public void setFile(String file)
	{
		this.file = file;
	}

	public void createCredential() throws GlobusCredentialException//, GSSException
	{
		this.gc = new GlobusCredential(file);
	}

	/*
	** Generic Private Data Interfaces
	*/

	//public GlobusCredential getGlobusCredential()
	public GlobusCredential getCredential()
	{
		return this.gc;
	}

	public Uid getUid()
	{
		return this.uid;
	}

	/*
	** Implement the GlobusCredential methods we need 
	*/

	public PrivateKey getPrivateKey()
	{
		return this.gc.getPrivateKey();
	}

	public String getSubject()
	{
		return this.gc.getSubject();
	}

	public int getProxyType()
	{
		return this.gc.getProxyType();
	}

	public String getIssuer()
	{
		return this.gc.getIssuer();
	}

	public int getStrength()
	{
		return this.gc.getStrength();
	}

	public int getCertNum()
	{
		return this.gc.getCertNum();
	}
};
