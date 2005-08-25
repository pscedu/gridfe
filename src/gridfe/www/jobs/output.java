/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import gridfe.gridint.*;
import javax.servlet.http.*;
import oof.*;

public class output {
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
				String fn_stdout = (j.dir == null ? "" : j.dir) + j.stdout;
				gi.startRetrieve(j, fn_stdout, 28000, 28255);
				int size = (int)gi.getGass().getSize();
/*
				if (size > )
					error
*/
				String data = gi.retrieve(size, 0);
				gi.stopRetrieve();

				s += p.header("Job Output")
				  +  oof.pre(new Object[] { "style", "overflow: scroll; height: 400px" },
				  		p.escapeHTML(data))
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

/* vim: set ts=4: */
