/* $Id$ */
/*
** file: GridInt.java
** purpose: Wrapper around authentication to kerberos
**		and grid computers...
*/

package gridint.auth;

/*
import javax.security.auth.*;
import javax.security.auth.Subject.*;
import javax.security.auth.login.*;
import java.lang.Object.*;
import java.security.*;
*/
import org.ietf.jgss.*;
import org.globus.gsi.*;
import gridint.auth.*;

public class GridInt
{
	private GlobusAuth ga;
	private GSSAuth gss;
	private Uid uid;

	public GridInt(int uid)
	{
		this.uid = new Uid(uid);
	}

	public void Authenticate() throws GSSException, GlobusCredentialException
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
			//ADD Auth failed... do something here
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
}


