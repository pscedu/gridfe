/* $Id$ */

package gridfe.gridint;

import java.io.*;
import org.globus.gram.*;
import org.globus.gsi.gssapi.auth.*;
import org.globus.io.gass.client.*;
import org.globus.io.gass.server.*;
import org.globus.io.streams.*;
import org.ietf.jgss.*;
import java.util.*;

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

	/*
	** Overload start 
	** Update: This works for GT 4.0 and is used for machines running
	** the standard fork manager.  However, this fails for unknown reasons
	** on gsissh setups like ben, lemieux, etc...
	*/
	public void start()
	    throws GassException, IOException {

		this.setOptions(this.options);
		this.start(this.host);
	}

	/*
	** Manually remote start a server - this fails for some reason though (GT2.4.3), more FUBAR
	** Update: This works for GT 4.0 and is necessary for our ben gsissh setup. The job should
	** be run through out BenShell.pm JobManager (which basically executes commands via gsissh)
	*/
	public void start_remote()
	    throws GramException, GSSException {
		GridJob j = new GridJob(this.host);

		// Build a gridjob that starts the gass server
		HashMap m = j.getMap();
//		m.put("executable", "${GLOBUS_LOCATION}/bin/globus-gass-server");
		m.put("executable", "/usr/local/packages/tg/globus-4.0.1-r3/bin/globus-gass-server");
		m.put("arguments", new String[] {"-c", "-p", Integer.toString(this.port), "-t", "-u", "-r"});
		m.put("stdout", "gass-out.log");
		m.put("stderr", "gass-err.log");


		/*
		** This needs to be executed to start our gass server properly...
		   gsissh ben 'export GLOBUS_LOCATION=/usr/local/packages/tg/globus-4.0.1-r3; 
		   . $GLOBUS_LOCATION/etc/globus-user-env.sh; 
		  /usr/local/packages/tg/globus-4.0.1-r3/bin/globus-gass-server -c -p 50001 -t -u'
		*/

		j.init(this.gss);
		j.run();
	}

	/*
	** Inherited Methods (needed from RemoteGassServer):
	** shutdown()
	*/

	/* Open a file for reading */
	public void open(String file)
	    throws GSSException, GassException, IOException {
		/* SelfAuthorization uses GlobusCredentials for authentication */
		SelfAuthorization auth = new SelfAuthorization();

		/*
		** Create a secure input stream
		**
		** Update: XXX there needs to be a way to figure out the 
		** machine that the file resides on. For example, submitting
		** to the ben jobmanager on gridfe (where host = gridfe.psc.edu/jobmanager-ben-pbs)
		** we need to know that the machine being executed on is ben.psc.edu.
		*/
		this.fin = new GassInputStream(this.gss, auth, "ben.psc.edu",
//		this.fin = new GassInputStream(this.gss, auth, this.host,
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
