/* $Id$ */

package gridfe.gridint;

import java.io.*;
import java.util.*;
import org.globus.gram.*;
import org.globus.gsi.gssapi.auth.*;
import org.globus.io.gass.client.*;
import org.globus.io.gass.server.*;
import org.globus.io.streams.*;
import org.ietf.jgss.*;

public class GassInt extends RemoteGassServer {
	private GassInputStream fin;
	private GSSCredential gss;
	private GassServer ga;
	private int options, port;
	private String host;
	private GridJob job = null;

	public GassInt(GSSCredential gss, String host, int port) {
		/* Call superclass constructor, secure_mode=true */
		super(gss, true, port);

		/* XXX This should really be set elsewhere */
		/* Set the port range and let globus choose the port for us */
//		java.lang.System.setProperty("org.globus.tcp.port.range", "50000,51000");
		this.port = port;

		this.host = host;
		this.gss = gss;
		this.options = GassServer.CLIENT_SHUTDOWN_ENABLE |
		    RemoteGassServer.TILDE_EXPAND_ENABLE |
		    RemoteGassServer.USER_EXPAND_ENABLE |
		    GassServer.READ_ENABLE;
	}

	/*
	 * Overload start.
	 * This works with the standard fork job manager but
	 * certain accomodations must be made to gsissh setups
	 * to properly setup a temporary X509_USER_PROXY, as
	 * mod_fum will create one owned by user apache and
	 * not the system user.
	 */
	public void start()
	    throws GassException, IOException {
		this.setOptions(this.options);
		this.start(this.host);
	}

	/*
	 * Manually start a remote GASS server.
	 * CoG's remote GASS server does not work in
	 * setups where GLOBUS_LOCATIONS differs among
	 * resources.
	 */
	public void start_remote()
	    throws GramException, GSSException {
		/* XXX - HARDCODED - fix */
		this.job = new GridJob("gridfe.psc.edu/jobmanager-ben-shell");

		/*
		 * Build and submit a GridJob which
		 * starts the remote GASS server.
		 */
		HashMap m = this.job.getMap();
		m.put("executable", "${GLOBUS_LOCATION}/bin/globus-gass-server");

		/*
		 * If we didn't specify a port here,
		 * we would have to strip the port
		 * number from the job contact string.
		 */
		m.put("arguments", new String[] {
		  "-c", "-p", Integer.toString(this.port),
		  "-t", "-u", "-r" });

		this.job.init(this.gss);
		this.job.run();

		// STATUS_ACTIVE ?
		// change -1 to STATUS_DONE ?
		while (this.job.getStatus() != GramJob.STATUS_PENDING &&
		  this.job.getStatus() != -1) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
System.err.println("GASS job status: " + this.job.getStatus());
		}
System.err.println("GASS job status: " + this.job.getStatus());
	}

	/*
	 * The remote gass server is stopped by requesting
	 * the URL /dev/globus_gass_client_shutdown.
	 * Note: CoG automatically adds a '/' to the
	 * beginning of requests and the remote GASS server
	 * does not recognize this URL with two slashes
	 * prefixed (//dev/globus_gass_client_shutdown).
	 */
	public void stop_remote() {
		try {
			this.write("I hate Globus", "ben.psc.edu",
			  this.port, "dev/globus_gass_client_shutdown");
		} catch (Exception e) {
			/*
			 * Requesting this URL makes the GASS
			 * server shut itself down, but it
			 * always sends back a "Bad Request"
			 * error, which will propagate here.
			 */

			String success = "Gass PUT failed: Bad Request";
			if (!e.getMessage().equals(success))
				System.err.println("remote GASS shutdown failed, " +
				    "rogue server running: " + e);
		}
	}

	/*
	 * Inherited Methods (needed from RemoteGassServer):
	 * shutdown()
	 */

	/* Open a file for reading */
	public void open(String file)
	    throws GSSException, GassException, IOException {
		/* SelfAuthorization uses GlobusCredentials for authentication */
		SelfAuthorization auth = new SelfAuthorization();

		/* Create a secure input stream. */
		/* XXX: fix hardcode */
		this.fin = new GassInputStream(this.gss, auth, "ben.psc.edu",
		    this.port, file);
System.err.println("opening the file: " + file);
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

	/* Write data to a remote file using GASS. */
	public void write(String buf, String host, int port, String file)
	    throws GSSException, GassException, IOException {
		GassOutputStream fout = new GassOutputStream(this.gss,
		  host, port, file, -1, false);

		byte[] data = buf.getBytes();
		fout.write(data);
		fout.close();
		fout = null;
	}

	/* Get the size of the file opened by the GassInputStream. */
	public long getSize() {
		return (this.fin.getSize());
	}

	/* Close the stream. */
	public void close()
	    throws IOException {
		if (this.fin != null) {
			this.fin.close();
			this.fin = null;
		}
	}
};
