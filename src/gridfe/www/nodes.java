/* $Id$ */

package gridfe.www;

import gridfe.*;
import oof.*;

public class nodes
{
	public static String main(Page p)
		throws Exception
	{
		OOF oof = p.getOOF();
		String s = "";

		s += p.header("Node Availibility")
		   + oof.p("Node availibility test page.")
		   + p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
