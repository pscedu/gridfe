/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import oof.*;

public class status
{
	public static String main(Page p)
		throws Exception
	{
		OOF oof = p.getOOF();
		String s = "";

		s += p.header("Job Status")
		   + oof.p("Job status test page.")
		   + p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
