/* $Id$ */

package gridfe.www;

import gridfe.*;
import oof.*;

public class certs
{
	public static String main(Page page)
		throws Exception
	{
		OOF oof = page.getOOF();

		return	page.header("Certificate Management") +
				oof.p("Certificate management test page.") +
				page.footer();
	}
};

/* vim: set ts=4: */
