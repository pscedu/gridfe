/* $Id$ */

package gridfe.gridint;

import gridfe.gridint.*;
import java.io.*;
import java.net.*;
import org.globus.gram.*;
import org.ietf.jgss.*;

public class GridJob extends RSLElement implements Serializable
{
	private String host;
	private transient GramInt gi;
	private String id;

	public GridJob(String host)
	{
		this.host = new String(host);
		this.id = new String("No job submitted");
	}

	public void setHost(String host)
	{
		this.host = new String(host);
	}

	/*
	** setRSL Wrappers are inherited from RSLElement.java
	*/

	/* Internal methods to be called by GridInt ONLY! */
	public void init(GSSCredential gss)
	{
		this.gi = new GramInt(gss, this.host);
	}

	/* Submit the job and save the id string */
	public void run()
		throws GramException, GSSException
	{
		this.gi.jobSubmit(this);
		this.id = new String(this.gi.getIDAsString());
	}

	/* cancel the job and dispose of gramint instance */
	public void cancel()
		throws GramException, GSSException
	{
		this.gi.cancel();

		/* Get rid of gi entirely */
		this.gi = null;
	}

	public String getHost()
	{
		return this.host;
	}

	/* GramInt Wrappers */
	public int getStatus()
		throws GSSException
	{
		return this.gi.getStatus();
	}

	public String getStatusAsString()
		throws GSSException
	{
		return this.gi.getStatusAsString();
	}

	/* Id is saved when job is submitted */
	public String getIDAsString()
	{
		return this.id;
	}

	/*
	** Revive allows a GridJob (and hence a GramJob) to be
	** recreated from saved values (similar to serialization).
	*/

	/* This revive should be called ONLY after a deserialization */
	public void revive(GSSCredential gss)
		throws MalformedURLException
	{
		/* Revive GramInt and it's private data */
		this.gi = new GramInt(gss, this.host, this.toString());
		this.gi.createJob(this.toString());
		this.gi.setID(this.id);
	}

	/* Serializable Implementation */
	private void writeObject(ObjectOutputStream out)
		throws IOException
	{
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}
};
