/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import oof.*;

public class status
{
	public static String main(Page page)
		throws Exception
	{
		OOF oof = page.getOOF();

		return	page.header("Job Status") +
				oof.p("Job status test page.") +
				page.footer();
	}
};

/* vim: set ts=4: */
