/* $Id$ */

package gridfe.gridint;

import gridfe.gridint.auth.*;
import jasp.Uid;
import java.io.*;
import java.net.MalformedURLException;
import java.security.PrivateKey;
import org.globus.gram.*;
import org.globus.gsi.*;
import org.ietf.jgss.*;

public class GridInt implements Serializable
{
	/* 
	** TODO: Serialize GSSAuth - so credentials can
	** be read back in easliy without messing with KDC
	** & mod_KCT, etc...
	*/
	private transient GlobusAuth ga;
	private transient GSSAuth gss;
	private Uid uid;
	private GridJobList list;

	public GridInt(String uid)
	{
		this.uid = new Uid(uid);
		this.list = new GridJobList();
	}

	public GridInt(int uid)
	{
		this.uid = new Uid(uid);
		this.list = new GridJobList();
	}

	public void auth()
		throws GSSException, GlobusCredentialException
	{
		this.ga = new GlobusAuth(this.uid);
		this.ga.createCredential();

		this.gss = new GSSAuth(ga);
		this.gss.createCredential();
	}

	public void jobSubmit(GridJob job)
		throws GramException, GSSException
	{
		job.init(this.gss.getGSSCredential());
		job.run();
		this.list.push(job);
	}

	public boolean jobCancel(GridJob job)
		throws GramException, GSSException
	{
		job.cancel();
		return this.list.remove(job);
	}

	/* Required after Deserialization */
	public void revive()
		throws MalformedURLException, GSSException, GlobusCredentialException
	{
		/* must authenticate first! */
		this.auth();

		for(int i = 0; i < this.list.size(); i++)
		{
			this.list.get(i).revive(this.gss.getGSSCredential());
		}
	}

	/*
	** TODO: provide a wrapper over this to get the job
	** the user wants... then call this function to
	** read the output file (from the RSLElement)
	** and return the data...
	*/
	public void getJobOutput(GridJob job)
	{
	}

	/*
	** TODO: elegant way to get status of jobs and retrieve
	** the job the user wants... possibly a Map? this will
	** be implemented when the UI is finalized
	*/
	/* currently get's the most recent job status */
	public String getJobStatusAsString()
		throws GSSException
	{
		return this.list.get(0).getStatusAsString();
	}
	public String getJobStatusAsString(int x)
		throws GSSException
	{
		return this.list.get(x).getStatusAsString();
	}
	public int getJobStatus()
		throws GSSException
	{
		return this.list.get(0).getStatus();
	}
	public int getJobStatus(int x)
		throws GSSException
	{
		return this.list.get(x).getStatus();
	}

	/*
	** TODO: - later there need to be lots more
	** functions for obtaining certificate information
	*/
	public int getRemainingLifetime()
		throws GSSException
	{
		return this.gss.getRemainingLifetime();
	}

	public GSSName getName() throws GSSException
	{
		return this.gss.getName();
	}

	/* Implement Serializable using revive() */
	private void writeObject(ObjectOutputStream out)
		throws IOException
	{
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException,
			GSSException, GlobusCredentialException
	{
		in.defaultReadObject();
		this.revive();
	}
};
