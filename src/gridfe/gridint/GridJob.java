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

		/* Job submitted to local machine */
		boolean local = false;

		/* XXX - Check for host to be localhost */
//		if(this.host.equals(BasicServices.getLocalhost()))
		if(this.host.equalsIgnoreCase("gridinfo.psc.edu"))
			local = true;

		/* Check for starting 'http:' or 'https:' */
		if(this.stdout != null)
			if(this.stdout.startsWith("http:") ||
			   this.stdout.startsWith("https:"))
				which = 1;

		if(this.stderr != null)
			if(this.stderr.startsWith("http:") ||
			   this.stderr.startsWith("https:"))
				which += 2;

		/*
		** If the job is remote, the output
		** is automatically remote whether it
		** via a gass server or not
		*/
		/* XXX - if it is not local submitted it
		** could be a job submitted to intel2 with output
		** going to rachel... need a way to extract the
		** proper host, etc...
		*/
		if(!local) 
			which = 3;
		
		return which;
	}

	/* Convert GRAM stdout, and directory to a GASS filename */
	public String convert(String file)
	{
		String dir = null;

		/*
		** Determine if directory needs prepended to output.
		** If std(out/err) string starts with a '/' or '~'
		** then the user has explicitly stated the path.
		** If directory does not start with '/' then it
		** needs to default to "~".
		*/
		if(file != null)
		{
			dir = (file.charAt(0) != '/') ? "~" : "";
			dir += (this.dir != null) ? "/" + this.dir : "";
		}

		/*
		** Unfortunately GRAM assumes directories start from
		** ~/ and if ~/dir is specified GRAM cannot expand the ~
		**
		** GASS on the other hand seems to assume a full path
		** and support ~ expansion via the TILDE_EXPAND_ENABLE
		** option.
		**
		** Therefore we have to manually adjust stdout, stderr
		** and directory accordingly.
		*/
		if(file != null && file.charAt(0) != '/' &&
			file.charAt(0) != '~')
		{
			file = dir + "/" + file;
		}

		System.out.println(file);
		return file;
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
