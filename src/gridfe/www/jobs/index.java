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
				OOF.LIST_UN,
				new Object[] {
					oof.link("Submit a job", p.buildURL("/jobs/submit")),
					oof.link("Get information about a previously submitted job", p.buildURL("/jobs/status"))
				}
		     )
		   + p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
