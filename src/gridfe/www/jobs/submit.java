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
		String s = "";

		s += page.header("Submit Job")
		   + oof.p("Submit job test page.")
		   + page.footer();

		return (s);
	}
};

/* vim: set ts=4: */
