/* $Id$ */
package gridfe.gridint;

import org.globus.gsi.gssapi.auth.*;
import org.globus.io.gass.server.*;
import org.globus.io.gass.client.*;
import org.globus.io.streams.*;
import org.ietf.jgss.*;
import java.io.*;

public class GassInt extends RemoteGassServer
{
	//private RemoteGassServer server;
	private GassInputStream fin;
	private int port;
	private String host;
	private int options;
	//private boolean secure;
	private GSSCredential gss;

	public GassInt(GSSCredential gss, String host, int port)
	{
		/* call superclass constructor, secure_mode=true */
		super(gss, true, port);

		this.port = port;
		this.host = host;
		this.gss = gss;
		this.options = GassServer.CLIENT_SHUTDOWN_ENABLE | 
				GassServer.READ_ENABLE;
	}

	/* Overload start */
	public void start()
		throws GassException
	{
		super.setOptions(this.options);
		super.start(host);
	}

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
