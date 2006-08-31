/* $Id$ */

package gridfe.gridint;

import java.io.*;
import java.net.*;
import java.util.*;
import org.globus.gram.*;
import org.ietf.jgss.*;

public class GridJob extends RSLElement implements Serializable {
	private transient GramInt gmi;
	private String resource;
	private String id;	/* globus url */
	private String name;	/* mnemonic name */
	private Date ctime;	/* creation time */
	private Date mtime;	/* last mod time */
	private int qid;	/* unique id */
	private int stat;	/* recent status */

	public GridJob(String res) {
		this.gmi = null;
		this.resource = res;
		this.qid = -1;
		this.id = null;
		this.mtime = null;
		this.ctime = null;
		this.stat = -1;
	}

	public void setHost(String res) {
		this.resource = res;
	}

	public String getHost() {
		return (this.resource);
	}

	/* User specified job names, for retrieval */
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return (this.name);
	}

	public int getQID() {
		return (this.qid);
	}

	public void setQID(int qid) {
		this.qid = qid;
	}

	public Date getCreateTime() {
		return (this.ctime);
	}

	public Date getModTime() {
		return (this.mtime);
	}

	/*
	 * setRSL() wrappers are inherited from RSLElement.
	 */

	/* Internal methods to be called by GridInt only. */
	public void init(GSSCredential gss) {
		this.gmi = new GramInt(gss, this.resource);
	}

	/* Submit the job. */
	public void run()
	    throws GramException, GSSException {
		this.gmi.jobSubmit(this);
		this.id = this.gmi.getIDAsString();
		this.ctime = new Date();

		/*
		 * Cache the status so we know
		 * when it has changed.
		 */
		this.stat = this.gmi.getStatus();
	}

	public void setModTime() {
		this.mtime = new Date();
	}

	/* Cancel the job and dispose of gramint instance */
	public void cancel()
	    throws GramException, GSSException {
		this.gmi.cancel();

		/* Get rid of gmi entirely */
		this.gmi = null;
	}

	/* Convert GRAM stdout, and directory to a GASS filename */
	public String convert(String file) {
		String dir = null;

		/*
		 * Determine if directory needs prepended to output.
		 * If stdout/stderr string starts with a '/' or '~'
		 * then the user has explicitly stated the path.
		 * If directory does not start with '/' then it
		 * needs to default to "~".
		 */
		if (file != null) {
			String rsldir = this.getDirectory();

			dir = (file.charAt(0) != '/') ? "~" : "";
			if (rsldir != null)
				dir += "/" + rsldir;
		}

		/*
		 * Unfortunately GRAM assumes directories start from
		 * ~/ and if ~/dir is specified GRAM cannot expand the ~
		 *
		 * GASS on the other hand seems to assume a full path
		 * and support ~ expansion via the TILDE_EXPAND_ENABLE
		 * option.
		 *
		 * Therefore we have to manually adjust stdout, stderr
		 * and directory accordingly.
		 */
		if (file != null && file.charAt(0) != '/' &&
		    file.charAt(0) != '~')
			file = dir + "/" + file;
		return (file);
	}

	public int getStatus()
	    throws GSSException {
		int newstat = this.gmi.getStatus();

		if (newstat != this.stat) {
System.err.println("JOB " + this.name +
  " has changed status: " + this.stat + " -> " + newstat);
			this.setModTime();
			this.stat = newstat;
		}

		return (newstat);
	}

	public String getStatusAsString()
	    throws GSSException {
		this.getStatus();
		return (this.gmi.getStatusAsString());
	}

	public String getIDAsString() {
		return (this.id);
	}

	/*
	 * Reinsantiate a GridJob from the given values,
	 * e.g. after serialization.  This should be called
	 * only once to reinstantiate.
	 */
	public void revive(GSSCredential gss)
	    throws MalformedURLException {
		/* Revive GramInt and its private data. */
		this.gmi = new GramInt(gss, this.resource, this.toString());
		this.gmi.createJob(this.toString());
		this.gmi.setID(this.id);
	}
};
