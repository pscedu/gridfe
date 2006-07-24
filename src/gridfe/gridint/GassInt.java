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
	private GridJob job = null;

	public GassInt(GSSCredential gss, String host, int port) {
		/* call superclass constructor, secure_mode=true */
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
//		GridJob job = new GridJob(this.host);

		
		/* XXX - HARDCODED - fix */
		this.job = new GridJob("gridfe.psc.edu/jobmanager-ben-shell");
//		this.job = new GridJob(this.host);

		// Build a gridjob that starts the gass server
		HashMap m = this.job.getMap();
		m.put("executable", "${GLOBUS_LOCATION}/bin/globus-gass-server");
//		m.put("executable", "/usr/local/packages/tg/globus-4.0.1-r3/bin/globus-gass-server");

//		m.put("arguments", new String[] {"-c", "-t", "-u", "-r"});
		m.put("arguments", new String[] {"-c", "-p", Integer.toString(this.port), "-t", "-u", "-r"});
		
		/*
		** XXX - part of the port fix stuff Derek suggested 
		** we don't specify -p so globus picks a port range for us.
		** however this makes us have to read the output file and
		** strip the port number off of the contact string
		*/
//		m.put("stdout", "/tmp/gass-out.log");
//		m.put("stderr", "/tmp/gass-err.log");


		this.job.init(this.gss);
		this.job.run();

//		while(this.job.getStatus() != -1 && this.job.getStatus() != GramJob.STATUS_ACTIVE &&
//			this.job.getStatus() != GramJob.STATUS_DONE) {
		while(this.job.getStatus() != GramJob.STATUS_PENDING && this.job.getStatus() != -1) {

			try{Thread.sleep(1000);}catch(Exception e){}
			System.out.println("Gass Status: "+this.job.getStatus());
		}

		System.out.println("Gass Status: "+this.job.getStatus());
	}

	/*
	** The remote gass server is stopped by writing to /dev/globus_gass_client_shutdown
	** (which is completely obsured). Also, note that paths are absolute and CoG has
	** automatically added a '/' at the beginning.
	*/
	public void stop_remote() {


/*
org.apache.log4j.Logger.getLogger(org.globus.io.streams.HTTPOutputStream.class.getName()).setLevel(org.globus.util.log4j.CoGLevel.TRACE);
org.apache.log4j.Logger.getLogger(org.globus.io.gass.server.RemoteGassServer.class.getName()).setLevel(org.globus.util.log4j.CoGLevel.TRACE);
org.apache.log4j.Logger.getLogger(org.globus.io.gass.server.GassServer.class.getName()).setLevel(org.globus.util.log4j.CoGLevel.TRACE);

if (org.apache.log4j.Logger.getLogger(org.globus.io.streams.HTTPOutputStream.class.getName()).isEnabledFor(org.globus.util.log4j.CoGLevel.TRACE))
 System.out.println("log enabled");
else
 System.out.println("log disabled");

org.apache.log4j.Logger.getLogger(org.globus.io.streams.HTTPOutputStream.class.getName()).log(org.globus.util.log4j.CoGLevel.TRACE, " @@@ testing the log\n");
 System.out.println("done trying test log");

*/
		try {
			this.write("I Hate Globus", "ben.psc.edu",
			  this.port, "dev/globus_gass_client_shutdown");
		} catch (Exception e) {
			System.out.println("shutdown failed - rogue gass server running\n"+e);
		}


/*
org.apache.log4j.Logger.getLogger(org.globus.io.streams.HTTPOutputStream.class.getName()).setLevel(org.apache.log4j.Level.WARN);
org.apache.log4j.Logger.getLogger(org.globus.io.gass.server.RemoteGassServer.class.getName()).setLevel(org.apache.log4j.Level.WARN);
org.apache.log4j.Logger.getLogger(org.globus.io.gass.server.GassServer.class.getName()).setLevel(org.apache.log4j.Level.WARN);
*/

		/*
		** We are just going to kill the gass process.
		** Here is our ridiculous command to take care of disposal:
		** ps axo pid,command | grep "globus-gass-server -c -p 50543" | awk '{print $1}' | xargs kill
		*/
	/*
		GridJob job = new GridJob(this.host);

		HashMap m = job.getMap();
		m.put("executable", "/bin/bash");

		m.put("arguments", new String[] {"-c",
		  "/bin/ps axo pid,command | /bin/grep 'globus-gass-server -c -p " + this.port + "' | /bin/awk '{print$1}' >args.out"});
		m.put("stdout", "bash.out");
		m.put("stderr", "bash.err");
//			"/bin/awk", "'{print $1}'", "|", "/bin/xargs", "kill"});

		System.out.println("RSL to KILL: "+job);

		job.init(this.gss);
		try{job.run();}catch(Exception e){};
	*/

		// If the job that was started has not been destroyed
/*
	try{
		if(this.job != null) {
			StringBuffer contact = new StringBuffer();
*/
			/* 1. Use the open Gass Server to read the contact string from /tmp */
/*
			this.open("/tmp/gass-"+this.port);
			this.read(contact, (int)(this.getSize()));
			this.close();
*/
			/* contact string is actually on gridfe's /tmp/gass-X */
/*			
			FileReader f = new FileReader("/tmp/gass-"+this.port);
			int c = f.read();
			while(c != -1) {
				contact.append(Integer.toString(c));
				c = f.read();
			}

			System.out.println("Contact: "+contact);
*/
			/* 2. Submit a job to run the shutdown client script */
/*			
			GridJob kill = new GridJob(this.host);

			HashMap m = kill.getMap();
			m.put("executable", "/usr/local/packages/tg/globus-4.0.1-r3/bin/globus-gass-server-shutdown");

			m.put("arguments", new String[] {contact.toString()});
			m.put("stdout", "kill.out.log");
			m.put("stderr", "kill.err.log");

			System.out.println("RSL to KILL: "+kill);

			kill.init(this.gss);
			try{kill.run();}catch(Exception e){System.out.println("Contact Error: "+e);};
		}
	}catch(Exception e){System.out.println("Contact Error: "+e);}
*/
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

		System.out.println("opening the file: "+file);
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
	public void write(String buf, String host, int port, String file)
		throws GSSException, GassException, IOException {

		GassOutputStream fout = new
		GassOutputStream(this.gss, host, port, file, -1, false);

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
