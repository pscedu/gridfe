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
		return "test";
/*
		return	page.header("Error") +
				oof.p("You are already logged on.") +
				page.footer();
	*/
	}
};

/* vim: set ts=4: */
