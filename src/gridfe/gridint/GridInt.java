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

		/*
		** If min/max are equal, use specific port 
		** otherwise, random...
		*/
		if(min != max)
		{
			/* Seed = CertLife * Uid */
			r = new Random(this.getCertInfo().time * this.uid.intValue());

			/*
			** Randomly Generate a Port between
			** our MIN/MAX Port Boundary using
			** LCM (Linear Congruent Method)
			** XXX - configuration for this??
			*/
			port = r.nextInt((max - min)) + min + 1;
		}
		else
			port = min;

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
		this.gass.start();

		/* XXX - Remote Start Support Needed */
		//this.gass.start_remote();
	}

	/* Setup file retrieval */
	public void startRetrieve(GridJob job, String file, int port)
		throws GassException, IOException, GSSException
	{
		/* Start with min/max == port */
		this.startRetrieve(job, file, port, port);
	}

	public void startRetrieve(GridJob job, String file, int min, int max)
		throws GassException, IOException, GSSException
	{
		String data = "";

		/* XXX Remote Fetch */
		if(job.remote(file))
		{
			throw new IOException("Remote Output not supported yet.");
		}
		/* Local Fetch */
		else
		{
			/* Start Gass Server (0,0 = random port) */
			this.startGass(min, max, job.getHost());
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
			this.gass.read(str, len);
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

	/* Implement Serializable using revive() */
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException,
			GSSException, GlobusCredentialException
	{
		in.defaultReadObject();
		this.revive();
	}
};
