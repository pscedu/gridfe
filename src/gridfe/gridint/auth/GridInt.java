/* $Id$ */
/*
** file: GridInt.java
** purpose: Wrapper around authentication to kerberos
**		and grid computers...
*/

package gridint.auth;

import org.ietf.jgss.*;
import org.globus.gsi.*;
import java.security.PrivateKey;
import gridint.auth.*;

public class GridInt
{
	private GlobusAuth ga;
	private GSSAuth gss;
	private Uid uid;

	public GridInt(String uid)
	{
		this.uid = new Uid(uid);
	}

	public void authenticate() throws GSSException, GlobusCredentialException
	{
	//	try
	//	{
			this.ga = new GlobusAuth(this.uid);
			this.ga.createCredential();

			this.gss = new GSSAuth(ga);
			this.gss.createCredential();
	//	}
	//	catch(GSSException e)
	//	catch(GlobusCredentialException)
	//	{
			//ADD Auth failed... try other means of auth?
	//	}
	}

	/*
	** Generic Private Data Interfaces
	*/
	
	public GlobusCredential getGlobusCredential()
	{
		return this.ga.getCredential();
	}

	public GSSCredential getGSSCredential()
	{
		return this.gss.getGSSCredential();
	}

	public GlobusAuth getGlobusAuth()
	{
		return this.ga;
	}

	public GSSAuth getGSSAuth()
	{
		return this.gss;
	}

	/*
	** Implement Methods (GlobusAuth)
	*/
	public PrivateKey getPrivateKey()
	{
		return this.ga.getPrivateKey();
	}
     
	public String getSubject()
	{
		return this.ga.getSubject();
	}

	public int getProxyType()
	{
		return this.ga.getProxyType();
	}
	
	public String getIssuer()
	{
		return this.ga.getIssuer();
	}
	
	public int getStrength()
	{
		return this.ga.getStrength();
	}
	
	public int getCertNum()
	{
		return this.ga.getCertNum();
	}

	/*
	** Implement Methods (GSSAuth)
	*/
	public GSSName getName() throws GSSException
	{
		return this.gss.getName();
	}
	
	public int getRemainingLifetime() throws GSSException
	{
		return this.gss.getRemainingLifetime();
	}
}


