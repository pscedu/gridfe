/* $Id$ */

package gridfe.gridint;

import gridfe.gridint.*;
import java.net.MalformedURLException;
import org.globus.gram.*;
import org.ietf.jgss.*;

public class GramInt
{
	private String host;
	//private RSLElement rsl;
	String rsl;
	private GramJob job = null;
	private boolean batch = false;
	private GSSCredential gss;
	private String jOut;

	public GramInt(GSSCredential gss)
	{
		this.gss = gss;
	}

	public GramInt(GSSCredential gss, String host)
	{
		this.gss = gss;
		this.host = host;
	}

	public GramInt(GSSCredential gss, String host, String rsl)
	{
		this.gss = gss;
		this.host = host;
		this.rsl = rsl;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public void setRsl(String rsl)
	{
		this.rsl = rsl;
	}

	/* globus-job-submit */
	public void jobSubmit(String host, String rsl)
		throws GramException, GSSException
	{
		this.batch = true;
		this.rsl = rsl;
		this.host = host;
		this.gramRequest(this.host, this.rsl);
	}

	public void jobSubmit(String rsl)
		throws GramException, GSSException
	{
		this.batch = true;
		this.rsl = rsl;
		this.gramRequest(this.host, this.rsl);
	}

	public void jobSubmit(GridJob job) 
		throws GramException, GSSException
	{
		this.batch = true;
		this.gramRequest(this.host, job.toString());
	}

	public void jobSubmit()
		throws GramException, GSSException
	{
		this.batch = true;
		this.gramRequest(this.host, this.rsl);
	}

	/* submit a request to the GRAM server */
	private void gramRequest(String host, String rsl)
		throws GramException, GSSException
	{
		/* Make sure the host is there */
		Gram.ping(this.gss, host);

		/* Create and process Job */
		this.job = this.createJob(rsl);
		this.job.request(host, this.batch);
	}

	/* Used to internally to rebuild a Gram Job */
	public void createJob()
	{
		this.job = new GramJob(this.gss, this.rsl);
	}

	/* Used internally to build a gramRequest */
	private GramJob createJob(String rsl)
	{
		this.job = new GramJob(this.gss, rsl);
		return this.job;
	}

	/* globus-job-status */
	public int getStatus()
		throws GSSException
	{
		/*
		** TODO: Find a good way to tell if the
		** job has a status of FAIL or DONE after
		** the jobmanager has termianted and the
		** GramException (below) is thrown...
		*/
		int status = -2;

		try
		{
			Gram.jobStatus(this.job);
			status = this.job.getStatus();
		}
		catch(GramException e)
		{
			/*
			** Job manager cannont be contacted. Therefore the job
			** is done. However, we do not know if the job finished
			** normally or terminated. -1 can stand for DONE/FAIL.
			*/
			if(e.getErrorCode() == GramException.ERROR_CONTACTING_JOB_MANAGER)
				status = -1;
		}

		return status;
	}

	/* Get the job status in human readable form */
	public String getStatusAsString()
		throws GSSException
	{
		String status;
		status = new String("UNKNOWN");

		try
		{
			Gram.jobStatus(this.job);
			status = new String(this.job.getStatusAsString());
		}
		catch(GramException e)
		{
			/* See above .getStatus() for information on this... */
			if(e.getErrorCode() == GramException.ERROR_CONTACTING_JOB_MANAGER)
				status = new String("DONE/FAIL");
		}

		return status;
	}

	/* globus-job-cancel */
	public void cancel()
		throws GramException, GSSException
	{
		this.job.cancel();
	}

	public String getIDAsString()
	{
		return this.job.getIDAsString();
	}

	public void setID(String id)
		throws MalformedURLException
	{
		this.job.setID(id);
	}

	/* filename of the job output */
//	public String getStdout()
//	{
		/* Attempt a build */
/*
		if(this.rsl.getStdout() == null)
			this.rsl.build();

		return this.rsl.getStdout();
	}
*/

	public GramJob getJob()
	{
		return this.job;
	}
};
