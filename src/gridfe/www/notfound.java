/* $Id$ */

package gridfe.www;

import gridfe.*;
import oof.*;

public class notfound
{
	public static String main(Page page)
		throws Exception
	{
		OOF oof = page.getOOF();

		return	page.header("404 Not Found") +
				oof.p("The requested page does not exist.  " +
					  "Please report the dead link") +
				page.footer();
	}
};

/* vim: set ts=4: */
