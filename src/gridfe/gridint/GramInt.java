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

	}

	public void gramJobSubmit(String host, String rsl) throws GramException, GSSException
	{
		this.host = host;
		this.rsl = rsl;
		this.batch = true;
		this.gramRequest();
	}

	public void gramJobRun(String host, String rsl) throws GramException, GSSException
	{
		this.host = host;
		this.rsl = rsl;
		this.batch = false;
		this.gramRequest();
	}

	private void gramRequest() throws GramException, GSSException
	{
		Gram.ping(this.host);

		this.job = new GramJob(this.gss, this.rsl);
		
	}
}
