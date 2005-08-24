/* $Id$ */

package gridfe.gridint;

import java.io.*;
import java.net.*;
import org.globus.gram.*;
import org.ietf.jgss.*;

public class GridJob extends RSLElement implements Serializable {
	private transient GramInt gmi;
	private transient GridInt gdi;
	private String host;
	private String id;
	private String name;
	private int qid;

	public GridJob(String host) {
		this.gmi = null;
		this.gdi = null;
		this.host = host;
		this.qid = -1;
		this.id = null;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return (this.host);
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

	/*
	 * setRSL Wrappers are inherited from RSLElement.java
	 */

	/* Internal methods to be called by GridInt ONLY */
	public void init(GridInt gdi, GSSCredential gss) {
		this.gdi = gdi;
		this.gmi = new GramInt(gss, this.host);
	}

	/* Submit the job and save the ID string */
	public void run()
	    throws GramException, GSSException {
		/*
		 * XXX - save a timestamp of when the job
		 * was submitted? (just a thought)
		 */
		this.gmi.jobSubmit(this);
		this.id = this.gmi.getIDAsString();
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
		 * If std(out/err) string starts with a '/' or '~'
		 * then the user has explicitly stated the path.
		 * If directory does not start with '/' then it
		 * needs to default to "~".
		 */
		if (file != null) {
			dir = (file.charAt(0) != '/') ? "~" : "";
			dir += (this.dir != null) ? "/" + this.dir : "";
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
		    file.charAt(0) != '~') {
			file = dir + "/" + file;
		}
		return (file);
	}

	/* GramInt wrappers */
	public int getStatus()
	    throws GSSException {
		return (this.gmi.getStatus());
	}

	public String getStatusAsString()
	    throws GSSException {
		return (this.gmi.getStatusAsString());
	}

	public String getIDAsString() {
		return (this.id);
	}

	/*
	 * Revive allows a GridJob (and hence a GramJob) to be
	 * recreated from saved values (similar to serialization).
	 */

	/* This revive should be called ONLY after a deserialization */
	public void revive(GridInt gdi, GSSCredential gss)
	    throws MalformedURLException {
	    	this.gdi = gdi;
		/* Revive GramInt and it's private data */
		this.gmi = new GramInt(gss, this.host, this.toString());
		this.gmi.createJob(this.toString());
		this.gmi.setID(this.id);
	}
};
