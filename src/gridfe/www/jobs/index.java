/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import oof.*;

public class index {
	public static String main(Page p)
	  throws Exception {
		OOF oof = p.getOOF();
		String s = "";

		s += p.header("Job Management")
		   + oof.p("Please select an action:")
		   + oof.list(
				oof.LIST_UN,
				new Object[] {
					oof.link("Submit a job", "submit"),
					oof.link("View status of a job", "status"),
					oof.link("Collect output from a job", "output")
				}
		     )
		   + p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
