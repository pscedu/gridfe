/* $Id$ */

package gridfe.www;

import gridfe.*;
import gridfe.gridint.*;
import gridfe.gridint.auth.*;
import oof.*;

import java.io.*;
import org.ietf.jgss.GSSException;
import org.globus.gsi.GlobusCredentialException;

public class certs
{
	public static String main(Page p)
		throws OOFException, IOException, GSSException, GlobusCredentialException
	{
		OOF oof = p.getOOF();
		String s = "";

		int uid = 6342;
//		int uid = page.getUserID();
//		FileOutputStream fp = new FileOutputStream("/tmp/gridfe.err");

		// DEBUG - use p.getGridInt() later;
//		fp.write(65);
		GridInt gi = new GridInt(uid);
//		fp.write(66);
		gi.auth();
//		fp.write(67);
//		fp.close();
		
		CertInfo ci;
		ci = gi.getCertInfo();

		long tmp;
		long sec = ci.time;
		long days = (sec / (tmp = 24*60*60));
		sec -= days * tmp;
		long hours = (sec / (tmp = 60*60));
		sec -= hours * tmp;
		long min = (sec / 60);
		sec -= min * 60;

		String lifetime;
		lifetime = ci.time + " (" +
				   days + " days, " +
				   hours + " hours, " +
				   min + " mins)";

		s += p.header("Certificate Management")
		   + oof.table(
				new Object[] {},
				new Object[][][] {
					new Object[][] {
						new Object[] {
							"colspan", "2",
							"class", p.CCHDR,
							"value", "Current Certificate Parameters"
						},
					},
					new Object[][] {
						new Object[] {
							"colspan", "2",
							"class", p.CCSUBHDR,
							"value", "Location"
						}
					},
					new Object[][] {
						new Object[] {
							"class", p.CCDESC,
							"value", "Kerberos Ticket:"
						},
						new Object[] {
							"class", p.genClass(),
							"value", ci.kfile
						}
					},
					new Object[][] {
						new Object[] {
							"class", p.CCDESC,
							"value", "X.509 Certificate:"
						},
						new Object[] {
							"class", p.genClass(),
							"value", ci.xfile
						}
					},
					new Object[][] {
						new Object[] {
							"colspan", "2",
							"class", p.CCSUBHDR,
							"value", "Contents"
						}
					},
					new Object[][] {
						new Object[] {
							"class", p.CCDESC,
							"value", "Issuer:"
						},
						new Object[] {
							"class", p.genClass(),
							"value", ci.issuer
						}
					},
					new Object[][] {
						new Object[] {
							"class", p.CCDESC,
							"value", "Subject:"
						},
						new Object[] {
							"class", p.genClass(),
							"value", ci.sub
						}
					},
					new Object[][] {
						new Object[] {
							"class", p.CCDESC,
							"value", "Identity:"
						},
						new Object[] {
							"class", p.genClass(),
							"value", ci.ident
						}
					},
					new Object[][] {
						new Object[] {
							"class", p.CCDESC,
							"value", "Type:"
						},
						new Object[] {
							"class", p.genClass(),
							"value", ci.type + ""
						}
					},
					new Object[][] {
						new Object[] {
							"class", p.CCDESC,
							"value", "Key Strength:"
						},
						new Object[] {
							"class", p.genClass(),
							"value", ci.key + "-bit"
						}
					},
					new Object[][] {
						new Object[] {
							"class", p.CCDESC,
							"value", "Remaining Lifetime:"
						},
						new Object[] {
							"class", p.genClass(),
							"value", lifetime
						}
					},
				}
		     )
		   + p.footer();

		return (s);
	}
};

/* vim: set ts=4: */
