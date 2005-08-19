/* $Id$ */

package gridfe.www.jobs;

import gridfe.gridint.*;
import gridfe.*;
import oof.*;

public class status {
	public static String main(Page p)
	  throws Exception {
		OOF oof = p.getOOF();
		String s = "";
		int i;

		GridInt gi = p.getGridInt();
		JobList list = gi.getJobList();
		GridJob j;

		s += p.header("Job Status")
		  +  oof.p("This page contains the status information for any jobs that " +
		  		"jobs that you have submitted.  For completed jobs, you may follow " +
				"the link to the " + oof.link("Job Output", p.buildURL("/jobs/output")) +
				" page to retrieve any output that the job may have generated.")
		  +  oof.table_start(new Object[] {
			  "class", p.CCTBL,
			  "border", "0",
			  "cellspacing", "0",
			  "cellpadding", "0" })
		  +		oof.table_row(new Object[][] {
		  			new Object[] {
						"class", p.CCHDR,
						"colspan", "5",
						"value", "Job Status"
					}
		  		})
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", p.CCSUBHDR, "value", "ID" },
					new Object[] { "class", p.CCSUBHDR, "value", "Name" },
					new Object[] { "class", p.CCSUBHDR, "value", "Host" },
//					new Object[] { "class", p.CCSUBHDR, "value", "Remote" },
					new Object[] { "class", p.CCSUBHDR, "value", "Status" }
				});
		for (i = 0; i < list.size(); i++) {
			j = list.get(i);
			String c = p.genClass();
			s += oof.table_row(new Object [][] {
				new Object[] { "class", c, "value", j.getIDAsString() },
				new Object[] { "class", c, "value", j.getHost() },
				new Object[] { "class", c, "value", j.getName() },
//				new Object[] { "class", c, "value", j.remote() },
				new Object[] { "class", c, "value", j.getStatusAsString() }
			});
		}
		if (i == 0)
			s += oof.table_row(new Object[][] {
				new Object[] {
					"class", "data1",
					"colspan", "5",
					"value", "No jobs currently in queue.  " +
						oof.link("Submit a new job.", p.buildURL("/jobs/submit"))
				}
			});
		s += oof.table_end()
		  +  p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
