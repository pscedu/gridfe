/* $Id$ */

package gridfe.www;

import gridfe.gridint.auth.*;
import gridfe.gridint.*;
import gridfe.*;
import oof.*;

public class certs
{
	public static String main(Page page)
		throws Exception
	{
		OOF oof = page.getOOF();

		int uid = 6342;
//		int uid = page.getUserid();
		CertInfo ci;

		GridInt gi = new GridInt(uid);
		gi.auth();

		ci = gi.getCertInfo();

		long tmp;
		long sec = ci.time;
		long days = (sec / (tmp = 24*60*60));
		sec -= days * tmp;
		long hours = (sec / (tmp = 60*60));
		sec -= hours * tmp;
		long min = (sec / 60);
		sec -= min * 60;

		return	page.header("Certificate Management") +
//				oof.p("Certificate management test page.") +
				oof.p("- Location -") +
				oof.p("Kerberos Ticket:\t" + ci.kfile) +
				oof.p("X.509 Certificate:\t" + ci.xfile) +
				oof.p("") +
				oof.p("- Contents -") +
				oof.p("Issuer:\t" + ci.issuer) +
				oof.p("Subject:\t" + ci.sub) +
				oof.p("Identity:\t" + ci.ident) +
				oof.p("Type:\t\t" + ci.type) +
				oof.p("Key Strength:\t" + ci.key + "bit") +
				oof.p("Remaining Lifetime:\t" + ci.time + 
						" ("+ days + " Days, " + hours + 
						" Hours, " + min + " Minuets, " +
						sec + " Seconds)") +
				oof.p("") +
				page.footer();
	}
};

/* vim: set ts=4: */
