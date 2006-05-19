/* $Id$ */

package gridfe.gridint;

import java.io.*;
import java.net.*;
import org.ietf.jgss.*;
import org.globus.io.urlcopy.*;
import org.globus.util.*;

public class GridFTP {
	
	private GSSCredential gss;

	public GridFTP(GSSCredential gss) {
		this.gss = gss;	
	}

	/*
	** Use GridFTP to copy a file.
	** host in the form of: gridfe.psc.edu
	** file in the form of: /home/rbudden/foo (assumes absolute)
	*/
	public void urlCopy(String shost, String dhost, String sfile, String dfile)
		throws MalformedURLException, UrlCopyException {

		/* Use gsiftp to transfer */
		UrlCopy url = new UrlCopy();
		
		/* Set the credentials to use */
		url.setCredentials(this.gss);

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
