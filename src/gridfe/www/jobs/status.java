/* $Id$ */

package gridfe.www.jobs;

import gridfe.gridint.*;
import gridfe.*;
import oof.*;

public class status
{
	public static String main(Page p)
		throws Exception
	{
		OOF oof = p.getOOF();
		String s = "";

		/*
		GridInt gi = p.getGridInt();
		JobList list = gi.getJobList();
		*/

		s += p.header("Job Status")
		   + oof.p("Job status test page.")
		   + p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
