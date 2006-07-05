/* $Id$ */

package gridfe.gridint;

import java.io.*;
import org.globus.gram.*;
import org.globus.gsi.gssapi.auth.*;
import org.globus.io.gass.client.*;
import org.globus.io.gass.server.*;
import org.globus.io.streams.*;
import org.ietf.jgss.*;

public class GassInt extends RemoteGassServer {
	private GassInputStream fin;
	private int port;
	private String host;
	private int options;
	private GSSCredential gss;
	private GassServer ga;

	public GassInt(GSSCredential gss, String host, int port) {
		/* call superclass constructor, secure_mode=true */
		super(gss, true, port);

		this.port = port;
		this.host = host;
		this.gss = gss;
		this.options = GassServer.CLIENT_SHUTDOWN_ENABLE |
		    RemoteGassServer.TILDE_EXPAND_ENABLE |
		    RemoteGassServer.USER_EXPAND_ENABLE |
		    GassServer.READ_ENABLE;
	}

	/* Overload start */
	public void start()
	    throws GassException, IOException {
/*
		//This is additions after hacked CoG code, where the file RemoteGassServer.java
		//was modified to allow additional customization. However, this still FUBAR.

		int success = 0;
		int n = 0;

		super.RBudden_set_env(new String[] {"GLOBUS_TCP_PORT_RANGE", "GLOBUS_UDP_PORT_RANGE"},
					new String[] {"\"28000 28255\"", "\"28000 28255\""});
		super.RBudden_set_port(0);
		super.RBudden_set_output("/tmp/gram.stdout","/tmp/gram.stderr");
*/
		this.setOptions(this.options);
		System.err.println(host);
		this.start(this.host);
	}

	/* Manually remote start a server - this fails for some reason though (GT2.4.3), more FUBAR */
/*
	public void start_remote()
	    throws GramException, GSSException {
//		GridJob j = new GridJob("intel2.psc.edu");
		GridJob j = new GridJob(this.host);

		// Build a gridjob that starts the gass server
		j.setRSL(new String[] { "executable", "stdout", "stderr" },
		    new String[] { "$GLOBUS_LOCATION/bin/globus-gass-server",
			"/tmp/gram.stdout", "/tmp/gram.stderr" },
		new String("arguments"),
//		new String[] { "-p", Integer.toString(this.port),"-c", "-r", "&" });
		new String[] { "-p", Integer.toString(this.port),"-c", "-r" });

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
	    throws GSSException, GassException, IOException {
		/* SelfAuthorization uses GlobusCredentials for authentication */
		SelfAuthorization auth = new SelfAuthorization();

		/* Create a secure input stream */
		this.fin = new GassInputStream(this.gss, auth, this.host,
		    this.port, file);
	}

	/* Read len bytes from the open stream */
	public int read(StringBuffer buf, int len)
	    throws IOException {
		int read = 0;

		/* Read from offset = 0, to len */
		while (read < len) {
			/* Create a new buf that is proper size */
			byte[] tmp = new byte[len - read];
			read += this.fin.read(tmp, 0, len - read);
			buf.append(new String(tmp));
		}
		return (read);
	}

	/* Write data to a remote file using Gass */
	public void write(String buf, int port, String file)
		throws GSSException, GassException, IOException {

		GassOutputStream fout = new
		GassOutputStream(this.gss, this.host, port, file, -1, false);

		byte[] data = buf.getBytes();
		fout.write(data);
		fout.close();
		fout = null;
	}

	/* Get the size of the file opened by the GassInputStream */
	public long getSize() {
		return (this.fin.getSize());
	}

	/* Close the stream */
	public void close()
	    throws IOException {
		if (this.fin != null) {
			this.fin.close();
			this.fin = null;
		}
	}
};
