/* $Id$ */
/*
** GridJob - Handles creating and maintaining of
** a grid job and the gram internals
*/

package gridint;

import gridint.*;
import org.ietf.jgss.*;
import org.globus.gram.*;

public class GridJob
{
	private String host;
	private RSLElement rsl;
	private GramInt gi;

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

}
