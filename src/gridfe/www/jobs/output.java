/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import oof.*;
import gridfe.gridint.*;

public class output {
	public static String main(Page p)
	  throws Exception {
		OOF oof = p.getOOF();
		GridInt gi;
		JobList jl;
		int size, i;
		String s = "";

		gi = p.getGridInt();
		jl = gi.getJobList();

		/* XXX:
		** retrieve size to have a progress metere
		** showing how much of the file has been transfered
		*/
//		size = gi.gass.getSize();
		size = jl.size();

		s += p.header("Job Output") +
			 oof.p("Please select a job:");

		/* Loop and show jobs */
		Object[] jobs = new Object[size];
		for(i = 0; i < size; i++)
		{
//			jobs[i] = oof.link(jl.getJob(i).getName(), "/jobs/retrieve");
			s += oof.p("/jobs/retrieve");
		}

//		s += oof.list(oof.LIST_UN, jobs);

		s += p.footer();

		return s;
	}
};

/* vim: set ts=4: */
