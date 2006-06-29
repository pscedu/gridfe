/* $Id$ */

package gridfe.www.junk;

import gridfe.*;
import java.io.*;
import oof.*;

public class env {
	public static String main(Page p)
	  throws Exception {
		OOF oof = p.getOOF();

		PrintWriter w = p.getResponse().getWriter();
		w.print("" + p.header("ENV!") + "<pre>");
		System.getProperties().list(w);
		w.print("</pre>" + p.footer());

		return ("");
	}
};

/* vim: set ts=4: */
