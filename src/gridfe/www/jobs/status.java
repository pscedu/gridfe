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

		GridInt gi = p.getGridInt();
		JobList list = gi.getJobList();
		GridJob j;

		s += p.header("Job Status")
		  +  oof.table_start(new Object[] {
			  "class", p.CCTBL,
			  "border", "0",
			  "cellspacing", "0",
			  "cellpadding", "0" })
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", p.CCDESC, "value", "ID" },
					new Object[] { "class", p.CCDESC, "value", "Name" },
					new Object[] { "class", p.CCDESC, "value", "Host" },
//					new Object[] { "class", p.CCDESC, "value", "Remote" },
					new Object[] { "class", p.CCDESC, "value", "Status" }
				});
		for (int i = 0; i < list.size(); i++) {
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
		s += oof.table_end()
		  +  p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
