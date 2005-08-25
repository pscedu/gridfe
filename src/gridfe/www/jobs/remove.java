/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import gridfe.gridint.*;
import javax.servlet.http.*;
import oof.*;

public class remove {
	public static String main(Page p)
	  throws Exception {
		String s = "";
		OOF oof = p.getOOF();
		boolean success = true;
		GridInt gi = p.getGridInt();
		JobList jl = gi.getJobList();

		String[] qids = p.getRequest().getParameterValues("qid");
		if (qids == null)
			qids = new String[0];
		for (int i = 0; i < qids.length; i++) {
			if (qids[i].matches("^\\d+$")) {
				GridJob j = jl.get(Integer.parseInt(qids[i]));
				if (j != null) {
					jl.remove(j);
					continue;
				}
			}
			success = false;
		}

		if (success) {
			s += p.header("Removed Jobs")
			  +  oof.p("All selected job(s) were removed successfully.")
			  +  p.footer();
		} else {
			s += p.header("Error")
			  +  oof.p("Some of the selected job(s) could not be removed.")
			  +  p.footer();
		}
		return (s);
	}
};

/* vim: set ts=4: */
