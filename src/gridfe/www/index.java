/* $Id$ */

package gridfe.www;

import gridfe.*;
import oof.*;

public class index
{
	public static String main(Page p)
		throws Exception
	{
		OOF oof = p.getOOF();
		String s = "";

		s += p.header("Welcome")
		   + oof.p("Welcome to the portal.")
		   + p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
