/* $Id$ */

package gridfe.www;

import gridfe.*;
import oof.*;

public class index {
	public static String main(Page p)
	  throws Exception {
		OOF oof = p.getOOF();
		String s = "";

		s += p.header("Welcome!")
		   + oof.p("Welcome to the GridFE portal.  GridFE provides a Web-accessible "
		   +  "interface to various Grid technologies.")
		   + oof.link("" + oof.img(new Object[] {
				"src", p.buildURL(p.PATHTYPE_WEB, "/img/learn/jobs.png"),
				"align", "left",
				"border", "0",
				"hspace", "5" }), p.buildURL("/jobs/submit"))
		   + oof.p(""
		   +   oof.strong("Learn how to submit jobs.")
		   +   oof.br()
		   +   "Follow the instructions to learn how to submit jobs and retrieve output.")
		   + p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
