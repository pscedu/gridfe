/* $Id$ */

package gridfe.www.rls;

import gridfe.*;
import oof.*;

public class search
{
	public static String main(Page page)
		throws Exception
	{
		OOF oof = page.getOOF();

		return	page.header("Test") +
				oof.p("Test RLS page.") +
				page.footer();
	}
};

/* vim: set ts=4: */
