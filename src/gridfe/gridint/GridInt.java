/* $Id$ */

package gridfe.gridint;

import gridfe.gridint.auth.*;
import jasp.*;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import org.globus.gram.*;
import org.globus.gsi.*;
import org.globus.io.gass.client.*;
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
	private JobList list;

	/* ----- replace with Uid type */
	public GridInt(String uid)
	{
		this.uid = new Uid(uid);
		this.list = new JobList();
	}

	public GridInt(int uid)
	{
		this.uid = new Uid(uid);
		this.list = new JobList();
	}
	/* --------------------------- */

	/* Perform all Grid authentication */
	public void auth()
		throws GSSException, GlobusCredentialException
	{
		/* Read in the X.509 Cert */
		this.ga = new GlobusAuth(this.uid);
		this.ga.createCredential();

		/* Convert to a GSSCredential */
		this.gss = new GSSAuth(ga);
		this.gss.createCredential();
	}

	/* Cleanup and destroy credentials */
	public void logout()
	{
		this.logout(null);
	}
	public void logout(String file)
	{
		/*
		** List of files to remove:
		** 1) X.509 Certificate
		** 2) Kerberos 5 TKT
		** 3) GridInt serialize file
		*/
		CertFile cf = new CertFile(this.uid);
		String[] list = new String[]
		{
			cf.getX509(),
			cf.getKrbTkt(),
			file
		};
		int ln = (file == null) ? 2 : 3;
		File fp;

		for(int i = 0; i < ln; i++)
		{
			fp = new File(list[i]);

			if(fp.exists())
				fp.delete();
		}
	}

	/* globus-job-submit equivalent */
	public void jobSubmit(GridJob job)
		throws GramException, GSSException
	{
		job.init(this.gss.getGSSCredential());
		job.run();

		/* Set default job name if none specified */
		if(job.getName() == null)
			job.setName(new String("Job-"+this.list.size()));
		
		/* Add job to list */
		this.list.push(job);

		/* Register name in Map */
		//XXX - ADD MAP
	}

	/* Cancel Job and remove from Job List */
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
	public String[] getJobData(GridJob job)
	{
		String data[] = {"Stdout not specified.","Stderr not specified."};
		int remote;

		remote = job.remote();

		/*
		** XXX when remote output is implemented there needs to 
		** be a way to specify that getLocalJobData() should only
		** retrieve stdout or stderr or both, depending...
		*/

		/* Make sure they asked for output at all! */
		if(job.stdout != null || job.stderr != null)
		{
			if(remote > 0 && remote < 3)
			{
				/*
				** Currently we cannot get remote output due to
				** weird stdout/stderr problems with setting up
				** a remote GASS Server
				*/
				data[remote - 1] = "Remote data output not supported yet!";
			}
			else if (remote == 3)
			{
				data[0] = "Remote data output not supported yet!";
				data[1] = "Remote data output not supported yet!";
			}

			/* Make sure there is a local output to retrieve */
			if(remote != 3)
			{
				/* Get stdout/stderr or both */
				this.getLocalJobData(job, data, job.remote());
			}
		}

		return data;
	}

	private void getLocalJobData(GridJob job, String[] data, int which)
	{
		GassInt gass;
		Random r;
		int port;
		String dir[] = {null, null};
		String file[] = {job.stdout, job.stderr};
		int start;
		int end;

		/*
		** 'which' data to retrieve Remotely:
		** 3 - Both 
		** 2 - Stderr (corresponds to data[1])
		** 1 - Stdout (corresponds to data[0])
		** 0 - Neither (Retrieve Both locally)
		*/
		switch(which)
		{
			case 3: /* This should never happen */ return;

			/* 2 & 1 may seem logically reversed, be careful */
			case 2: start = 0; end = 1; break;
			case 1: start = 1; end = 2; break;

			case 0: start = 0; end = 2; break;
			default: which = 0; start = 0; end = 2; break;
		}

		/* Seed = CertLife * Uid */
		r = new Random(
			this.getCertInfo().time * this.uid.intValue() );

		/*
		** Randomly Generate a Port between
		** our MIN/MAX Port Boundary
		** XXX - configuration for this??
		*/
		final int MIN = 28000;
		final int MAX = 28255 + 1;
		port = r.nextInt((MAX-MIN)) + MIN;

		/* Create a GASS Server to connect to */
		gass = new GassInt(this.gss.getGSSCredential(),
					job.getHost(), port);

		try
		{
			/* Start the Gass Server */
			gass.start();
		}
		catch(Exception e)
		{
			/* Flag Error, Return */
			if(which != 0)
				data[start] += e.getMessage();
			else
			{
				data[0] += e.getMessage();
				data[1] += e.getMessage();
			}

			// XXX I hate having returns like this... better way?
			return;
		}

		for(int i = start; i < end; i++)
		{

			/*
			** Determine if directory needs prepended to output.
			** If std(out/err) string starts with a '/' or '~'
			** then the user has explicitly stated the path.
			** If directory does not start with '/' then it
			** needs to default to "~".
			*/
			if(file[i] != null)
			{
				dir[i] = (file[i].charAt(0) != '/') ? "~" : "";
				dir[i] += (job.dir != null) ? "/" + job.dir : "";
			}

			/* 
			** Unfortunately GRAM assumes directories start from
			** ~/ and if ~/dir is specified GRAM cannot expand the ~
			**
			** GASS on the other hand seems to assume a full path
			** and support ~ expansion via the TILDE_EXPAND_ENABLE
			** option.
			**
			** Therefore we have to manually adjust stdout, stderr
			** and directory accordingly.
			*/
			if(file[i] != null && file[i].charAt(0) != '/' &&
				file[i].charAt(0) != '~')
			{
				file[i] = new String(dir[i] + "/" + file[i]);
			}
			
			/* Read stdout/stderr */
			try
			{
				gass.open(file[i]);
				data[i] = gass.read();
				gass.close();
			}
			catch(Exception e)
			{
				data[i] += " Exception: "+e.getMessage();
			}
		}

		/* Terminate the Gass Server */
		gass.shutdown();
	}

	/* Get a Job from the list by index */
	public GridJob getJob(int index)
	{
		return this.list.get(index);
	}

	/* Get a job from the list by it's name */
//	public GridJob getJob(String name)
//	{

//	}

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

	/* Get Certificate Information */
	public CertInfo getCertInfo()
	{
		return this.ga.getCertInfo();
	}

	/* This could actually be omitted, (explicit declaration) */
	private void writeObject(ObjectOutputStream out)
		throws IOException
	{
		out.defaultWriteObject();
	}

	/* Implement Serializable using revive() */
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException,
			GSSException, GlobusCredentialException
	{
		in.defaultReadObject();
		this.revive();
	}

	//-----------------------------------------DEBUG
	public GSSCredential getCredential()
	{
		return this.gss.getGSSCredential();
	}
	public GlobusAuth getGlobusAuth()
	{
		return this.ga;
	}
	//-----------------------------------------DEBUG
};
