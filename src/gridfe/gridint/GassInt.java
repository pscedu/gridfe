/* $Id$ */
package gridfe.gridint;

import org.globus.gsi.gssapi.auth.*;
import org.globus.io.gass.server.*;
import org.globus.io.gass.client.*;
import org.globus.io.streams.*;
import org.globus.gram.*;
import org.ietf.jgss.*;
import java.io.*;

public class GassInt extends RemoteGassServer
{
	private GassInputStream fin;
	private int port;
	private String host;
	private int options;
	private GSSCredential gss;
	private GassServer ga;

	public GassInt(GSSCredential gss, String host, int port)
	{
		/* call superclass constructor, secure_mode=true */
		super(gss, true, port);

		this.port = port;
		this.host = host;
		this.gss = gss;
		this.options = GassServer.CLIENT_SHUTDOWN_ENABLE | 
				RemoteGassServer.TILDE_EXPAND_ENABLE |
				GassServer.READ_ENABLE;
	}

	/* Overload start */
	public void start()
		throws GassException, IOException
	{
		/*
		int success = 0;
		int n = 0;

		super.RBudden_set_env(new String[] {"GLOBUS_TCP_PORT_RANGE", "GLOBUS_UDP_PORT_RANGE"},
					new String[] {"\"28000 28255\"", "\"28000 28255\""});
		super.RBudden_set_port(0);
		super.RBudden_set_output("/tmp/gram.stdout","/tmp/gram.stderr");
		*/

		super.setOptions(this.options);

		super.start(host);
		/*
		while(success == 0 && n++ < 10)
		{
			try
			{
				System.out.println("Trying...");
				super.start(host);
				success = 1;
				System.out.println("Success!!");
			}
			catch(GassException e)
			{
				System.out.println("Failed...");
				success = 0;
			}
		}
		*/
	}

	/* Manually remote start a server - this fails for some reason though */
	/*
	public void start_remote()
		throws GramException, GSSException
	{
		GridJob j = new GridJob("intel2.psc.edu");
		j.setRSL(new String[] {"executable", "stdout", "stderr"},
		new String[] {"$GLOBUS_LOCATION/bin/globus-gass-server",
		"/tmp/gram.stdout", "/tmp/gram.stderr"},
		new String("arguments"),
		//new String[] {"-p", Integer.toString(this.port),"-c", "-r", "&"});
		new String[] {"-p", Integer.toString(this.port),"-c", "-r"});
		j.init(this.gss);
		j.run();
	}
	*/

	/* 
	** Inherited Methods (needed from RemoteGassServer):
	** shutdown()
	*/

	/* Open a file for reading */
	public void open(String file)
		throws GSSException, GassException, IOException
	{
		/* SelfAuthorization uses GlobusCredentials for authentication */
		SelfAuthorization auth = new SelfAuthorization();

		/* Create a secure input stream */
		this.fin = new GassInputStream(this.gss, auth, this.host,
							this.port, file);
	}

	/*
	** XXX - extend HttpInputStream to allow this (double extends??):
	** Inherited Methods (needed from HttpInputStream)
	** getSize() read() close()
	*/
	public int read(byte[] buf, int offset, int len)
		throws IOException
	{
		return this.fin.read(buf, offset, len);
	}

	/* Read the entire file into a String */
	public String read()
		throws IOException
	{
		int len = (int)(this.getSize());
		byte[] buf = new byte[len];
		
		/* Read from offset=0, up to len */
		int read = this.fin.read(buf, 0, len);

		/* convert to string */
		String str = new String(buf);

		return str;
	}

	//DEBUG
	//int getSize() // wow... documentation error
	public long getSize()
	{
		return this.fin.getSize();
	}

	public void close()
		throws IOException
	{
		this.fin.close();
	}

};
