/* $Id$ */

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

	public void authenticate() throws GSSException, GlobusCredentialException
	{
		this.ga = new GlobusAuth(this.uid);
		this.ga.createCredential();

		this.gss = new GSSAuth(ga);
		this.gss.createCredential();
	}

	public GlobusAuth getGlobusAuth()
	{
		return this.ga;
	}

	public GSSAuth getGSSAuth()
	{
		return this.gss;
	}

	public int getRemainingLifetime() throws GSSException
	{
		return this.gss.getRemainingLifetime();
	}
}
