/* $Id$ */

package gridint;

import gridint.auth.*;
import jasp.Uid;
import java.security.PrivateKey;
import org.globus.gram.*;
import org.globus.gsi.*;
import org.ietf.jgss.*;

public class GridInt
{
	private GlobusAuth ga;
	private GSSAuth gss;
	private Uid uid;

	public GridInt(String uid)
	{
		this.uid = new Uid(uid);
	}

	public GridInt(int uid)
	{
		this.uid = new Uid(uid);
	}

	public void auth() throws GSSException, GlobusCredentialException
	{
		this.ga = new GlobusAuth(this.uid);
		this.ga.createCredential();

		this.gss = new GSSAuth(ga);
		this.gss.createCredential();
	}

	public void jobSubmit(GridJob job) throws GramException, GSSException
	{
		job.init(this.gss.getGSSCredential());
		job.run();

		// TODO: add to linkedlist to keep track of jobs...
	}

	public void jobCancel(GridJob job) throws GramException, GSSException
	{
		job.cancel();

		// TODO: remove from linkedlist to keep track of jobs...
	}

	public void getJobOutput()
	{

	}

	public void getJobStatus()
	{

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
