/* $Id$ */
/*
** file: GridInt.java
** purpose: Wrapper around authentication to kerberos
**		and grid computers...
*/

//package gridint.auth;
package grindint;

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

	/*
	** Authenticate to the Grid
	*/
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
	** Interface Private Classes
	*/
	public GlobusAuth getGlobusAuth()
	{
		return this.ga;
	}

	public GSSAuth getGSSAuth()
	{
		return this.gss;
	}

	/*
	** Methods
	*/

	/*
	** Get the Remaining lifetime of the Certificate
	*/
	public int getRemainingLifetime() throws GSSException
	{
		return this.gss.getRemainingLifetime();
	}
}


