/* $Id$ */

package gridfe.www;

import gridfe.*;
import oof.*;

public class nodes
{
	public static String main(Page page)
		throws Exception
	{
		OOF oof = page.getOOF();

		return	page.header("Node Availibility") +
				oof.p("Node availibility test page.") +
				page.footer();
	}
};

/* vim: set ts=4: */
