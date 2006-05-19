/* $Id$ */

package gridfe.www.gridftp;

import gridfe.*;
import oof.*;

public class copy {
	public static String main(Page p)
	  throws Exception {
		OOF oof = p.getOOF();
		String s = "";

		s += p.header("GridFTP Copy")
		   + oof.p("Choose two URLs to copy between:")
		   + oof.table(
				new Object[] { }
		     )
		   + p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
