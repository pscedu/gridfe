/* $Id$ */
/*
** GridJob - Handles creating and maintaining of
** a grid job and the gram internals
*/

package gridint;

import gridint.*;
import org.ietf.jgss.*;
import org.globus.gram.*;
import java.net.MalformedURLException;
import java.io.*;
public class GridJob implements Serializable
{
	private String host;
	private RSLElement rsl;
	private transient GramInt gi;
	private String id;

	/* Needed for revive */
	public GridJob()
	{

	}

	public GridJob(String host)
	{
		this.host = new String(host);
	}

	public GridJob(String host, RSLElement rsl)
	{
		this.rsl = rsl;
		this.host = new String(host);
	}

	public void setHost(String host)
	{
		this.host = new String(host);
	}

	/* RSLElement Wrappers */
	public void setRSL(String[] p, String[] v)
	{
		this.rsl = new RSLElement(p, v);
	}

	public void setRSL(String[] gp, String[] gv, String vp, String[] vv)
	{
		this.rsl = new RSLElement(gp, gv, vp, vv);
	}
	
	public void setRSL(String[] gp, String[] gv, String kp, String[] kk, String[] kv)
	{
		this.rsl = new RSLElement(gp, gv, kp, kk, kv);
	}

	public void setRSL(String[] gp, String[] gv, String vp, String[] vv, String kp, String[] kk, String[] kv)
	{
		this.rsl = new RSLElement(gp, gv, vp, vv, kp, kk, kv);
	}

	public void setRSL(RSLElement rsl)
	{
		this.rsl = rsl;
	}


	/* Internal methods to be called by GridInt ONLY! */
	public void init(GSSCredential gss)
	{
		this.gi = new GramInt(gss);	
		this.gi.setHost(this.host);
	}

	public void run() throws GramException, GSSException
	{
		this.gi.jobSubmit(this.rsl);	
		this.id = new String(this.gi.getIDAsString());
		
		/* DEBUG */
		System.out.println("GridJob.run():"+this.id);
	}

	public void cancel() throws GramException, GSSException
	{
		this.gi.cancel();

		/* Get rid of gi entirely */
		this.gi = null;
	}


	/* Obtain Private Data Methods */
	public RSLElement getRSL()
	{
		return this.rsl;
	}

	public String getHost()
	{
		return this.host;
	}

	/* GramInt Wrappers */
	public int getStatus() throws GSSException
	{
		return this.gi.getStatus();
	}

	public String getStatusAsString() throws GSSException
	{
		return this.gi.getStatusAsString();
	}

	public String getIDAsString()
	{
		return this.gi.getIDAsString();
	}

	/* DEBUG */
	/*
	public void setID(String id) throws MalformedURLException
	{
		System.out.println("GridJob: "+id);
		if(this.gi == null)
			System.out.println("wtf");
		this.gi.setID(new String(id));
		//this.gi.getJob().setID(id);
	}
	*/

	/*
	** Revive allows a GridJob (and hence a GramJob) to be
	** recreated from saved values (similar to serialization).
	*/

	/* This revive to be called manually for testing or even cloning jobs */
	public void revive(String host, String id, GSSCredential gss, RSLElement rsl) throws MalformedURLException
	{
		this.host = host;
		this.rsl = rsl;
		this.id = id;
		this.revive(gss);
	}

	/* This revive should be called after a deserialization */
	public void revive(GSSCredential gss) throws MalformedURLException
	{
		/* Revive GridJob private data */
		//this.host = host;
		//this.rsl = rsl;

		/* Revive GramInt and it's private data */
		this.gi = new GramInt(gss, this.host, this.rsl);
		this.gi.createJob();
		this.gi.setID(this.id);
	}

	/* Serializable Implementation */
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		/* DEBUG */
		System.out.println(this.host);
		System.out.println(this.rsl.toString());
		System.out.println(this.id);
	}


}
