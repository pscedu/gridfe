/* $Id$ */

package gridint;

import gridint.*;
import gridint.auth.*;
import jasp.Uid;
import java.io.*;
import java.net.MalformedURLException;
import java.security.PrivateKey;
import org.globus.gram.*;
import org.globus.gsi.*;
import org.ietf.jgss.*;

public class GridInt implements Serializable
{
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

	public void revive()
		throws MalformedURLException, GSSException, GlobusCredentialException
	{
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

	/* DEBUG */
	public GlobusAuth getGlobusAuth()
	{
		return this.ga;
	}

	/* DEBUG */
	public GSSAuth getGSSAuth()
	{
		return this.gss;
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
};
