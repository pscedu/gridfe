/*
** GSSAuth.java
**
** Takes a GlobusCredential and 
** then creates a standard GSSCredential 
** for use with all globus api
*/
package gridint.auth;

import org.globus.gsi.*;
import org.globus.gsi.gssapi.*;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl.*;
import java.security.PrivateKey;
import org.ietf.jgss.*;
import org.ietf.jgss.GSSException.*;
import org.ietf.jgss.GSSCredential.*;

public class GSSAuth
{
	private GlobusCredential gc = null;
	private GSSCredential gss = null;

	public GSSAuth(GlobusAuth ga)
	{
		this.gc = ga.getCredential();
	}

	public GSSAuth(GlobusCredential gc)
	{
		this.gc = gc;
	}

	public void createCredential() throws GSSException
	{
		/*
		** The following class does a conversion between
		** GlobusCredential to GSSCredential... However,
		** this is broken in CoG 1.1 (and previous also
		** i assume)... In order for this to work properly
		** CoG jglobus was compiled from the "CoG 2.0 pre alpha"
		** source code. (cvs.globus.org)
		*/
		this.gss = new GlobusGSSCredentialImpl(this.gc, 
				GSSCredential.INITIATE_AND_ACCEPT);
	}

	/*
	** Generic Private Data Interfaces
	*/

	public GlobusCredential getGlobusCredential()
	{
		return this.gc;
	}

	public GSSCredential getGSSCredential()
	{
		return this.gss;
	}

	/*
	** GSSCredential Methods
	*/

	public GSSName getName() throws GSSException
	{
		return this.gss.getName();
	}

	public int getRemainingLifetime() throws GSSException
	{
		return this.gss.getRemainingLifetime();
	}

};
