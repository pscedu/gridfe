/* $Id$ */

package gridfe.gridint;

import java.io.*;
import java.net.*;
import java.util.*;
import org.ietf.jgss.*;
import org.globus.io.urlcopy.*;
import org.globus.util.*;
import org.globus.ftp.*;
import org.globus.ftp.exception.*;
import org.globus.gram.*;
import org.ietf.jgss.*;
import gridfe.gridint.*;
import org.globus.ftp.MlsxEntry;

public class GridFTP extends GridFTPClient {
	private static final String _PATH_GSISCP = "/usr/psc/bin/gsiscp";

	private GSSCredential gss;

	public GridFTP(GSSCredential gss, String host, int port)
	  throws IOException, ServerException, ClientException {
		super(host, port);

//		java.lang.System.setProperty("org.globus.tcp.port.range", "28000,28255");
		java.lang.System.setProperty("org.globus.tcp.port.range", "50000,51000");

		this.gss = gss;
		this.authenticate(this.gss);

		System.out.println("Authenticated to host "+host+":"+port);

		/* Ugly test to chose port range */
		/*
		try {
			this.list();
		} catch (Exception e) {
			throw new IOException("no route to host");
		}
		*/
	}

/*
	public Vector ls()
	  throws IOException, ServerException, ClientException {
		return this.list(null, null);
	}
*/

/*
	public Vector ls(String path)
	  throws IOException, ServerException, ClientException {
		return this.nlist(path);
	}
*/

	/* Wrap the GridFTP FileInfo code to use our GridFile class */
	public Vector gls()
	  throws IOException, ServerException, ClientException {
		System.out.println("Trying GridFTP.gls()");
//		Vector v = this.list(null, null);
		Vector v = this.list(); /* XXX: use mlsd */
		System.out.println("Successful GridFTP.gls()");
		return GridFTP.fi2GridFile(v);
	}

/*
	public Vector gls(String path)
	  throws IOException, ServerException, ClientException {
		Vector v = this.nlist(path);
		return GridFTP.fi2GridFile(v);
	}
*/

/*
	public Vector test()
	  throws IOException, ServerException, ClientException {
		System.out.println("Trying GridFTP.mlsd()");
		Vector v = this.mlsd();
		System.out.println("Successful GridFTP.mlsd()");
		return GridFTP.mlsx2GridFile(v);
	}
*/

	/* Convert FileInfo Vectors to GridFile Vectors */
	public static Vector fi2GridFile(Vector v) {
		Vector gv = new Vector(v.size());

		for(int i = 0; i < v.size(); i++)
			gv.add(new GridFile((FileInfo)(v.get(i))));

		return gv;
	}

	/* Convert MlsxEntry Vectors to GridFile Vectors */
	public static Vector mlsx2GridFile(Vector v) {
		Vector gv = new Vector(v.size());

		for(int i = 0; i < v.size(); i++) {
			GridFile f = new GridFile();
			MlsxEntry e = (MlsxEntry)(v.get(i));
			f.name = e.getFileName();
			f.date = "1";
			f.time = "2";
			f.perm = "3";
			f.size = 4;
			gv.add(f);
		}

		return gv;
	}

	/*
	** Use GridFTP to copy a file.
	** host in the form of: gridfe.psc.edu
	** file in the form of: /home/rbudden/foo (assumes absolute)
	*/
	public static void urlCopy(GSSCredential gss, String shost,
	  String dhost, String sfile, String dfile)
	  throws MalformedURLException, UrlCopyException {
		/* Use gsiftp to transfer */
		UrlCopy url = new UrlCopy();

		/* Set the credentials to use */
		url.setCredentials(gss);

		/* Add an extra "/" after the host for absolute, (bug in CoG) */
		GlobusURL su = new GlobusURL("gsiftp://" + shost + "/" + sfile);
		GlobusURL du = new GlobusURL("gsiftp://" + dhost + "/" + dfile);

		/* Always use Third Party */
		url.setUseThirdPartyCopy(true);

		url.setSourceUrl(su);
		url.setDestinationUrl(du);
		url.copy();
	}

	/* Submit a job to a machine to perform a gsiscp between it and another host */
	public static void gsiScp(GridInt gi, String manager, String shost,
		String dhost, String sfile, String dfile)
		throws GSSException, GramException {

		GridJob j = new GridJob(manager);

		/* gsiscp gridfe:/home/rbudden/foo ben:/home/rbudden/bar */
		HashMap m = j.getMap();
		m.put("executable", _PATH_GSISCP);
		m.put("arguments", new String[] {
		  shost + sfile, dhost + dfile } );

		j.init(gi.getGSS().getGSSCredential());
		j.run();
	}
}
