/* $Id$ */

import gridfe.*;
import gridfe.gridint.*;
import gridfe.gridint.auth.*;
import jasp.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import org.globus.io.urlcopy.*;
import org.globus.util.*;
import org.globus.util.GlobusURL;

public class gftp {
	public static void main(String[] args)
	    throws Exception {

		GridInt gi = new GridInt(BasicServices.getUserID(), GridInt.GIF_REGCERT);
		gi.auth();


		/* +++++ GLOBUS-URL-COPY test +++++ */

		String shost = "gridfe.psc.edu";
		String dhost = "gridfe.psc.edu";
		String src = "/tmp/foo";
		String dst = "/tmp/bar";

		System.out.println("Creating /tmp/foo");

		/* Creat a tmp file to copy */
		FileOutputStream fs =  new FileOutputStream(src);
		String data = "Test file";
		fs.write(data.getBytes());
		fs.close();

		System.out.println("Using GridFtp urlCopy /tmp/foo -> /tmp/bar");

		/* Make a urlCopy using GridFTP */
		GridFTP.urlCopy(gi.getGSS().getGSSCredential(), shost, dhost, src, dst);

		System.out.println("Copy completed"); 


		/* +++++ GridFTP Test +++++ */

		GridFTP ftp = new GridFTP(gi.getGSS().getGSSCredential(), shost, 2811);
		System.out.println("ls:");

		Vector v = ftp.gls();
		GridFile gf;

		for(int i = 0; i < v.size(); i ++)
		{
			gf = (GridFile)(v.get(i));
			System.out.println(gf);
		}
		

		System.out.println("Done");

		System.exit(0);
		return;
	}
};
