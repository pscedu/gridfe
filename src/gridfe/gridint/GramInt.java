/* $Id$ */

/*
** GramInt.java - GRAM Internals
*/

package gridint;

import org.globus.gram.*;
import org.ietf.jgss.*;

public class GramInt
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
	public void gramJobSubmit(String host, String rsl) throws GramException, GSSException
	{
		this.batch = true;
		this.gramRequest(host, rsl);
	}

	public void gramJobSubmit(String rsl) throws GramException, GSSException
	{
		this.batch = true;
		this.gramRequest(this.host, rsl);
	}

	public void gramJobSubmit() throws GramException, GSSException
	{
		this.batch = true;
		this.gramRequest(this.host, this.rsl);
	}

	public void gramJobRun(String host, String rsl) throws GramException, GSSException
	{
		this.batch = false;
		this.gramRequest(host, rsl);
	}
	
	public void gramJobRun(String rsl) throws GramException, GSSException
	{
		this.batch = false;
		this.gramRequest(this.host, rsl);
	}
	public void gramJobRun() throws GramException, GSSException
	{
		this.batch = false;
		this.gramRequest(this.host, this.rsl);
	}

	private void gramRequest(String host, String rsl) throws GramException, GSSException
	{
		/* Make sure the host is there */
		Gram.ping(host);

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

	/* globus-job-cancel */
	public void cancel() throws GramException, GSSException
	{
		this.job.cancel();
	}
}
