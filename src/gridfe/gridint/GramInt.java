/* $Id$ */

/*
** GramInt.java - GRAM Internals
*/

package gridint;

import gridint.*;
import org.globus.gram.*;
import org.ietf.jgss.*;

public class GramInt
{
	private String host;
	private RSLElement rsl;
	private GramJob job = null;
	private boolean batch = false;
	private GSSCredential gss;

	/* Randomized Job Output filename */
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

	public GramInt(GSSCredential gss, String host, RSLElement rsl)
	{
		this.gss = gss;
		this.host = host;
		this.rsl = rsl;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public void setRsl(RSLElement rsl)
	{
		this.rsl = rsl;
	}

	/*
	** globus-job-submit & globus-job-run
	*/
	public void jobSubmit(String host, RSLElement rsl) throws GramException, GSSException
	{
		this.batch = true;
		this.rsl = rsl;
		this.host = host;
		this.gramRequest(this.host, this.rsl);
	}

	public void jobSubmit(RSLElement rsl) throws GramException, GSSException
	{
		this.batch = true;
		this.rsl = rsl;
		this.gramRequest(this.host, this.rsl);
	}

	public void jobSubmit() throws GramException, GSSException
	{
		this.batch = true;
		this.gramRequest(this.host, this.rsl);
	}
	
	private void gramRequest(String host, RSLElement rsl) throws GramException, GSSException
	{
		/* Make sure the host is there */
		Gram.ping(this.gss, host);

		/* Create and process Job */
		this.job = new GramJob(this.gss, rsl.toString());
		this.job.request(host, this.batch);
	}

	/* globus-job-status */
	public int getStatus()
	{
		return this.job.getStatus();
	}

	public String getStatusAsString()
	{
		return this.job.getStatusAsString();
	}

	/* globus-job-cancel */
	public void cancel() throws GramException, GSSException
	{
		this.job.cancel();
	}

	public String getIDAsString()
	{
		return this.job.getIDAsString();
	}

	/* filename of the job output */
	public String getStdout()
	{
		/* Attempt a build */
		if(this.rsl.getStdout() == null)
			this.rsl.build();

		return this.rsl.getStdout();
	}

	public GramJob getJob()
	{
		return this.job;
	}
}
