/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import gridfe.gridint.*;
import javax.servlet.http.*;
import oof.*;

public class cancel {
	public static String main(Page p)
	  throws Exception {
		String s = "";
		OOF oof = p.getOOF();
		GridInt gi = p.getGridInt();
		JobList jl = gi.getJobList();
		String s_qid;
		HttpServletRequest req = p.getRequest();

		if ((s_qid = req.getParameter("qid")) != null &&
		  s_qid.matches("^\\d+$")) {
			/* This should never throw an exception since we just checked it. */
			GridJob j = jl.get(Integer.parseInt(s_qid));

			if (j != null) {
				j.cancel();

				s += p.header("Job Output")
				  +  oof.p("Job cancelled successfully.")
				  +  p.footer();
				return (s);
			}
		}
		s += p.header("Error")
		  +  oof.p("Sorry, the system could not find the requested job.")
		  +  p.footer();
		return (s);
	}
};
