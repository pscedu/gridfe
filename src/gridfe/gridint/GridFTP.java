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
import gridfe.gridint.*;

public class GridFTP extends GridFTPClient {
	
	private GSSCredential gss;

	public GridFTP(GSSCredential gss, String host, int port)
		throws IOException, ServerException, ClientException {
		super(host, port);
		this.gss = gss;	
		this.authenticate(this.gss);
	}

	public Vector ls() 
		throws IOException, ServerException, ClientException {
		return this.list(null, null);
	}

	public Vector ls(String path) 
		throws IOException, ServerException, ClientException {
		return this.nlist(path);
	}

	/* Wrap the GridFTP FileInfo code to use our GridFile class */
	public Vector gls()
		throws IOException, ServerException, ClientException {
		Vector v = this.list(null, null);
		return GridFTP.fi2GridFile(v);
	}

	public Vector gls(String path)
		throws IOException, ServerException, ClientException {
		Vector v = this.list(path);
		return GridFTP.fi2GridFile(v);
	}

/*
	public void mv(String s, String d) {
		this.rename(s, d);
	}

	public void rm() {
	}

	public String pwd() {
		return this.getCurrentDir();
	}
	public void cd() {
		this.changeDir();
	}

	public void mkdir(String dir) {
		this.makeDir(dir);
	}
*/

	/* Convert FileInfo Vectors to GridFile Vectors */
	public static Vector fi2GridFile(Vector v) {
		Vector gv = new Vector(v.size());

		for(int i = 0; i < v.size(); i++)
			gv.add(new GridFile((FileInfo)(v.get(i))));
			
		return gv;
	}

	/*
	** Use GridFTP to copy a file.
	** host in the form of: gridfe.psc.edu
	** file in the form of: /home/rbudden/foo (assumes absolute)
	*/
	public static void urlCopy(GSSCredential gss, String shost, String dhost, String sfile, String dfile)
		throws MalformedURLException, UrlCopyException {

		/* Use gsiftp to transfer */
		UrlCopy url = new UrlCopy();
		
		/* Set the credentials to use */
		url.setCredentials(gss);

		/* Add an extra "/" after the host for absolute, (bug in CoG) */
		GlobusURL su = new GlobusURL("gsiftp://"+shost+"/"+sfile);
		GlobusURL du = new GlobusURL("gsiftp://"+dhost+"/"+dfile);

		/* Always use Third Party */
		url.setUseThirdPartyCopy(true);

		url.setSourceUrl(su);
		url.setDestinationUrl(du);
		url.copy();
	}
}
