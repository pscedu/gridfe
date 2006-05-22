/* $Id$ */

package gridfe.www.gridftp;

import gridfe.*;
import oof.*;

public class index {
	public static String main(Page p)
	  throws Exception {
		OOF oof = p.getOOF();
		String s = "";

		s += p.header("GridFTP")
		   + oof.p("Please select an action:")
		   + oof.list(
				OOF.LIST_UN,
				new Object[] {
					oof.link("URL copy", p.buildURL("/gridftp/copy")),
					oof.link("Browser", p.buildURL("/gridftp/browser")),
				}
		     )
		   + p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
