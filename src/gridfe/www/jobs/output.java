/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import oof.*;

public class output
{
	public static String main(Page page)
		throws Exception
	{
		OOF oof = page.getOOF();

		return	page.header("Job Output") +
				oof.p("Job output test page.") +
				page.footer();
	}
};

/* vim: set ts=4: */
