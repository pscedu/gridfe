/* $Id$ */

package gridfe.www;

import gridfe.*;
import oof.*;

public class notfound
{
	public static String main(Page p)
		throws Exception
	{
		OOF oof = p.getOOF();
		String s = "";

		s += p.header("404 Not Found")
		   + oof.p("The requested page does not exist.  " +
				   "Please report the dead link.")
		   + p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
