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
	private transient GlobusAuth ga;
	private transient GSSAuth gss;
	private transient GassInt gass;
	private Uid uid;
	private JobList list;

	public static final int kJobName = 0;
	public static final int kJobStatus = 1;
	public static final int kJobRSL = 2;

	public static final int OI_STDOUT = 0;
	public static final int OI_STDERR = 1;
	public static final int OI_MAX = 2;

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
		/* XXX: throw exception instead. */
		if(job.getName() == null)
			job.setName("Job-" + this.list.size());

		/* Add job to list */
		this.list.push(job);
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

	/* Start the Gass Server on a random port within our range */
	private void startGass(int min, int max, String host)
		throws GassException, IOException
	{
		Random r;
		int port;

		/* Seed = CertLife * Uid */
		r = new Random(this.getCertInfo().time * this.uid.intValue());

		/* XXX DEBUG: Pittsburgh Supercomputing Port Range */
		if(min == 0 && max == 0)
		{
			min = 28000;
			max = 28255;
		}

		/*
		** Randomly Generate a Port between
		** our MIN/MAX Port Boundary using
		** LCM (Linear Congruent Method)
		** XXX - configuration for this??
		*/
		port = r.nextInt((max - min)) + min + 1;
		
		/* Start with the random port */
		this.startGass(port, host);
	}

	/* Allow override of random port in range */
	private void startGass(int port, String host)
		throws GassException, IOException
	{
		/* Create a GASS Server to connect to */
		this.gass = new GassInt(this.gss.getGSSCredential(),
					host, port);

		/* Start the Gass Server */
		//this.gass.start();
		try
		{
			this.gass.start_remote();
		}
		catch(Exception e)
		{
			throw new IOException("start_remote() failed.");
		}
	}

	/* Setup file retrieval */
	public void startRetrieve(GridJob job, String file)
		throws GassException, IOException, GSSException
	{
		String data = "";

		/* Remote Fetch */
		if(job.remote(file))
		{
			throw new IOException("Remote Output not supported yet.");
		}
		/* Local Fetch */
		else
		{
			/* Start Gass Server (0,0 = random port) */
			this.startGass(0, 0, job.getHost());
		}

		/* Convert from GRAM -> GASS convention */
		file = job.convert(file);

		/* Open the file for reading */
		this.gass.open(file);
	}

	/* End file retrieval */
	public void stopRetrieve()
		throws IOException
	{
		/* Close the file */
		this.gass.close();

		/* Close the Gass Server */
		this.gass.shutdown();
	}


	/* File (Chunk of 'len' bytes, 'len < 1' read all) */
	public String retrieve(int len, int off)
		throws IOException
	{
		StringBuffer str = new StringBuffer("");
		String data = "";
		long size = this.gass.getSize();
		long left = size - off;

		/* 
		** Check len is not greater than len of file  (or
		** what is left of reading it)
		**
		** XXX - this is messed up how the size of the file
		** is of type long and the length to read must be an
		** int... (precision problems possible??)
		*/
		if(len > left)
			len = (int)(left);

		/* If there is still data */
		if(left > 0)
		{
			/* If len < 1 read the rest otherwise read len */
			if(len < 1)
				len = (int)(left);

			/* Read the data */
			this.gass.read(str, len, 0);
			data += str.toString();
		}

		return data;
	}

	/* Get a Job from the list by index */
	public GridJob getJob(int index)
	{
		return this.list.get(index);
	}

	/* Get a job from the list by it's name */
	public GridJob getJob(String name)
	{
		return this.list.get(name);
	}

//---This is most likely not needed anymore-------------------------------------
	/* Get the data from the job list for html output */
	public String[][] getJobListString()
		throws GSSException
	{
		/*
		** Format:
		**
		** String[][] = new String{{"job1", "job2", "job3"},
		**			{"status1", "status2", "status3"},
		**			{"rsl1", "rsl2", "rsl3"}};
		*/
		int len = this.list.size();
		String[][] jobs = new String[len][3];

		/* NOTE: this is reverse order than submitted! */
		for(int i = 0; i < len; i++)
		{
			jobs[i][kJobName] = this.list.get(i).getName();
			jobs[i][kJobStatus] = this.list.get(i).getStatusAsString();
			jobs[i][kJobRSL] = this.list.get(i).toString();
		}

		return jobs;
	}
//-----------------------------------------------------------------------------

	/* Get the raw JobList class */
	public JobList getJobList()
	{
		return this.list;
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
