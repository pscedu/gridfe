/* $Id$ */

package gridfe.gridint;

import java.io.*;
import java.net.*;
import org.globus.gram.*;
import org.ietf.jgss.*;

public class GridJob extends RSLElement implements Serializable
{
	private String host;
	private transient GramInt gi;
	private String id;
	private String name;

	public GridJob(String host)
	{
		this.host = host;
		this.id = "No job submitted";
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public String getHost()
	{
		return this.host;
	}

	/* User specified Job names, for retrieval */
	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
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
		this.id = this.gi.getIDAsString();
	}

	/* cancel the job and dispose of gramint instance */
	public void cancel()
		throws GramException, GSSException
	{
		this.gi.cancel();

		/* Get rid of gi entirely */
		this.gi = null;
	}

	/*
	** Determine if stdout, stderr, or both are
	** being redirected to a Gass Server
	*/
	public int remote()
	{
		/*
		** 'which' data to retrieve Remotely:
		** 3 - Both
		** 2 - Stderr
		** 1 - Stdout
		** 0 - Neither (Retrieve Both locally)
		*/
		int which = 0;

		/* Check for starting 'http:' or 'https:' */
		if(this.stdout != null)
			if(this.stdout.startsWith("http:") ||
			   this.stdout.startsWith("https:"))
				which = 1;

		if(this.stderr != null)
			if(this.stderr.startsWith("http:") ||
			   this.stdout.startsWith("https:"))
				which += 2;	
		
		return which;
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
