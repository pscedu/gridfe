/*
** GlobusAuth.java
**
** Obtains a GlobusCredential from 
** a file which is a valid X.509 Certificate
*/
package gridint.auth;

import org.globus.gsi.*;
//import org.globus.gsi.gssapi.*;
import java.security.PrivateKey;
//import org.ietf.jgss.*;
//import org.ietf.jgss.GSSException.*;
//import org.ietf.jgss.GSSCredential.*;

public class GlobusAuth
{
	private GlobusCredential gc = null;
	private String file;
	private Uid uid;
//	private GlobusGSSCredentialImpl gss = null;

	/*
	** X.509 Standard for files /tmp/x509up_uXXX
	** where XXX is the userid
	*/
	private final String def = "/tmp/x509up_u";

	public GlobusAuth(int uid)
	{
		this.uid = new Uid(uid);
		this.file = this.def + this.uid.intValue();
	}

	public GlobusAuth(String uid)
	{
		this.uid = new Uid(uid);
		this.file = this.def + this.uid.intValue();
	}

	/* Overide Default X.509 Certificate File*/
	public void setFile(String file)
	{
		this.file = file;
	}

	public void createCredential() throws GlobusCredentialException//, GSSException
	{
		this.gc = new GlobusCredential(file);
//		this.gss = new GlobusGSSCredentialImpl(this.gc, 
//				GSSCredential.INITIATE_AND_ACCEPT);
	}

	/*
	** Generic Private Data Interfaces
	*/

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

	// this is screwing up for some reason...
	public int getProxyType()
	{
		return this.gc.getProxyType();
	}
	// fails with a casting exception...

	public String getIssuer()
	{
		return this.gc.getIssuer();
	}

	public int getStrength()
	{
		return this.gc.getStrength();
	}

};

/*
** Data type wrapper over the uid
*/
class Uid
{
	private int uid;

	public Uid(int uid)
	{
		this.uid = uid;
	}

	public Uid(Integer uid)
	{
		this.uid = uid.intValue();
	}
	
	public Uid(String uid)
	{
		Integer i = new Integer(uid);
		this.uid = i.intValue();
	}

	public int intValue()
	{
		return this.uid;
	}
}
