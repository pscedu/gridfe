/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import oof.*;

public class submit
{
	public static String main(Page page)
		throws Exception
	{
		OOF oof = page.getOOF();

		return	page.header("Submit Job") +
				oof.p("Submit job test page.") +
				page.footer();
	}
};

/* vim: set ts=4: */
