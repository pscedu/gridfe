/* $Id$ */

/*
** GramInt.java - GRAM Internals
*/

package gridint;

import org.globus.gram.*;
import org.ietf.jgss.*;

public class GramInt //implements GramJobListener
{
	private String host;
	private String rsl;
	private GramJob job = null;
	private boolean batch = false;
	private GSSCredential gss;

	public GramInt(GSSCredential gss)
	{
		this.gss = gss;
	}
	public GramInt(GSSCredential gss, String host)
	{
		this.gss = gss;
		this.host = host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public void setRsl(String rsl)
	{
		this.rsl = rsl;
	}

	/*
	** globus-job-submit & globus-job-run
	*/
	public void jobSubmit(String host, String rsl) throws GramException, GSSException
	{
		this.batch = true;
		this.gramRequest(host, rsl);
	}

	public void jobSubmit(String rsl) throws GramException, GSSException
	{
		this.batch = true;
		this.gramRequest(this.host, rsl);
	}

	public void jobSubmit() throws GramException, GSSException
	{
		this.batch = true;
		this.gramRequest(this.host, this.rsl);
	}
	
/*	only batch jobs are needed... no waiting around for stuff

	public void jobRun(String host, String rsl) throws GramException, GSSException
	{
		this.batch = false;
		this.gramRequest(host, rsl);
	}

	public void jobRun(String rsl) throws GramException, GSSException
	{
		this.batch = false;
		this.gramRequest(this.host, rsl);
	}
	public void jobRun() throws GramException, GSSException
	{
		this.batch = false;
		this.gramRequest(this.host, this.rsl);
	}
*/

	private void gramRequest(String host, String rsl) throws GramException, GSSException
	{
		/* Make sure the host is there */
		Gram.ping(this.gss, host);

		/* Create and process Job */
		this.job = new GramJob(this.gss, rsl);
		this.job.request(host, this.batch);

		/*
		** Add callback listener for status change
		** which requires "implements GramJobListener"
		** and a public method call "stateChanged"
		*/
		//this.job.addListener(this);
	}

	/* globus-job-status */
	public int getStatus()
	{
		return this.job.getStatus();
	}

	public String getStatusString()
	{
		return this.job.getStatusAsString();
	}

	/* globus-job-cancel */
	public void cancel() throws GramException, GSSException
	{
		this.job.cancel();
	}

	public GramJob getJob()
	{
		return this.job;
	}

	/*  Job Listener Implementation */
	/*
	public void stateChanged(GramJob j)
	{
		System.out.print("Job " + j.getID());
		System.out.println("changed state: " + j.getStatusAsString());
	}
	*/
}
