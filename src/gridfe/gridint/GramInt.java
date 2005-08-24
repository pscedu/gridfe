/* $Id$ */

package gridfe.gridint;

import java.net.*;
import org.globus.gram.*;
import org.ietf.jgss.*;

public class GramInt {
	private String host;
	private String rsl;
	private GramJob job = null;
	private boolean batch = false;
	private GSSCredential gss;
	private String jOut;

	public GramInt(GSSCredential gss, String host) {
		this.gss = gss;
		this.host = host;
	}

	public GramInt(GSSCredential gss, String host, String rsl) {
		this.gss = gss;
		this.host = host;
		this.rsl = rsl;
	}

	/* globus-job-submit: submit a request to the GRAM server */
	public void jobSubmit(GridJob j)
	    throws GramException, GSSException {
		/* Make sure the host is there */
		System.out.println("Trying to ping host");
		Gram.ping(this.gss, this.host);

		/* Create and process Job */
		this.job = this.createJob(j.toString());
		System.out.println("Trying GramJob.request()");
		this.job.request(this.host, true);
		System.out.println("GramJob.request() finished");
	}

	/* Used internally to by this.gramRequest and GridJob.revive */
	public GramJob createJob(String rsl) {
		this.job = new GramJob(this.gss, rsl);
		return this.job;
	}

	/* globus-job-status */
	public int getStatus()
	    throws GSSException {
		/*
		 * TODO: Find a good way to tell if the
		 * job has a status of FAIL or DONE after
		 * the jobmanager has termianted and the
		 * GramException (below) is thrown.
		 */
		int status = -2;

		try {
			Gram.jobStatus(this.job);
			status = this.job.getStatus();
		} catch (GramException e) {
			/*
			 * Job manager cannont be contacted. Therefore the job
			 * is done. However, we do not know if the job finished
			 * normally or terminated. -1 can stand for DONE/FAIL.
			 */
			if (e.getErrorCode() ==
			    GramException.ERROR_CONTACTING_JOB_MANAGER)
				status = -1;
		}
		return (status);
	}

	/* Get the job status in human readable form */
	public String getStatusAsString()
	    throws GSSException {
		String status = "UNKNOWN";

		try {
			Gram.jobStatus(this.job);
			status = this.job.getStatusAsString();
		} catch (GramException e) {
			/* See above .getStatus() for information on this. */
			if (e.getErrorCode() ==
			    GramException.ERROR_CONTACTING_JOB_MANAGER)
				status = "DONE/FAIL";
		}
		return (status);
	}

	/* globus-job-cancel */
	public void cancel()
	    throws GramException, GSSException {
		this.job.cancel();
	}

	public String getIDAsString() {
		return (this.job.getIDAsString());
	}

	public void setID(String id)
	    throws MalformedURLException {
		this.job.setID(id);
	}

	public GramJob getJob() {
		return (this.job);
	}
};
