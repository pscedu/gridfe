/* $Id$ */

import oof.*;
import gridint.*;

public class logout {
	public static String main(Page page) throws Exception {
		String s;
		OOF oof = page.getOOF();
		GridInt gi = page.getGridInt();

		gi.auth();
		gi.logout();

		s =	page.header("Logout") +
			oof.p("You have successfully logged out.") +
			page.footer();

		return s;
	}
};

/* vim: set ts=4: */
