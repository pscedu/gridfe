/* $Id$ */

package gridfe.www;

import gridfe.*;
import oof.*;

public class index
{
	public static String main(Page page)
		throws Exception
	{
		OOF oof = page.getOOF();

		return	page.header("Welcome") +
				oof.p("Welcome to the portal.") +
				oof.p("Welcome to the portal.") +
				oof.p("Welcome to the portal.") +
				oof.p("Welcome to the portal.") +
				oof.p("Welcome to the portal.") +
				page.footer();
	}
};

/* vim: set ts=4: */
