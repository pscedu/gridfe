/* $Id$ */

package gridfe.www.gridftp;

import gridfe.*;
import gridfe.gridint.*;
import java.sql.*;
import javax.servlet.http.*;
import oof.*;

public class copy {
	public static String main(Page p)
	  throws Exception {
		HttpServletRequest req = p.getRequest();
		String errmsg = null;
		String shost = req.getParameter("shost");
		String dhost = req.getParameter("dhost");
		String sfile = req.getParameter("sfile");
		String dfile = req.getParameter("dfile");

		if (shost == null)
			shost = "";
		if (dhost == null)
			dhost = "";
		if (sfile == null)
			sfile = "";
		if (dfile == null)
			dfile = "";

		if (req.getParameter("submitted") != null) {
			if (shost.equals("") || dhost.equals("")
				|| sfile.equals("") || dfile.equals(""))
				errmsg = "Please specify all required form fields.";
			if (errmsg == null) {
				GridInt gi = p.getGridInt();

				/* Copy the file */
				GridFTP.urlCopy(gi.getGSS().getGSSCredential(), shost, dhost, sfile, dfile);

				String s = "";
				s += p.header("urlCopy")
				   + p.getOOF().p("The file has been copied successfully.")
				   + p.footer();
				return s;
			}
		}

		String s = "";
		OOF oof = p.getOOF();

		String js_submit =
			"	if (this.value == 'Copy') {	" +
			"		this.value = 'Please wait...';	" +
			"		return (true)					" +
			"	}";

		PreparedStatement sth = p.getDBH().prepareStatement(
			"	SELECT					" +
			"			COUNT(*) AS cnt	" +
			"	FROM					" +
			"			hosts			" +
			"	WHERE					" +
			"			uid = ?			");	/* 1 */
		sth.setInt(1, p.getUID());
		ResultSet rs = sth.executeQuery();

		int nhosts = 0;
		if (rs.next())
			nhosts = rs.getInt("cnt");

		sth = p.getDBH().prepareStatement(
			"	SELECT					" +
			"			host			" +
			"	FROM					" +
			"			hosts			" +
			"	WHERE					" +
			"			uid = ?			");	/* 1 */
		sth.setInt(1, p.getUID());
		rs = sth.executeQuery();

		Object[] hlist = new Object[2 * nhosts + 2];
		hlist[0] = "";
		hlist[1] = "Choose a resource";
		for (int i = 2; rs.next(); i += 2)
			hlist[i] = hlist[i + 1] = rs.getString("host");

		s += p.header("urlCopy")
		   + oof.p("You can fill out the fields below and press the copy "
		   +   "button to copy a file to another machine. Afterwards you can "
		   +   "navigate the menu on the left side of the page to view the file "
		   +   "structure using the GridFTP Browser.");
		if (errmsg != null)
			s += oof.p(new Object[] { "class", "err" },
			  "" + oof.strong("An error has occurred while processing your copy: ") +
			  errmsg);
		s += oof.form(
				new Object[] {
					"action", "copy",
					"method", "POST",
					"enctype", "application/x-www-form-urlencoded"
				},
				new Object[] {
					oof.table(
						new Object[] {
							"class", Page.CCTBL,
							"border", "0",
							"cellspacing", "0",
							"cellpadding", "0"
						},
						new Object[][][] {
							new Object[][] {
								new Object[] {
									"class", Page.CCHDR,
									"value", "urlCopy",
									"colspan", "2"
								}
							},
							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Source resource:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(shost),
											"name", "shost"
										}) +
										oof.input(new Object[] {
											"type", "select",
											"onchange", copy.js_hostchg("shost"),
											"options", hlist
										}) +
										oof.br() +
										"&raquo; This field should contain the host name " +
										"of the source machine on which you would " +
										"copy from.  You may select a previously " +
										"configured host from the drop-down box on the " +
										"right, which may be done through the " +
										oof.link("Node Availibility", p.buildURL("/nodes")) +
										" page."
								}
							},
							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Target resource:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(dhost),
											"name", "dhost"
										}) +
										oof.input(new Object[] {
											"type", "select",
											"onchange", copy.js_hostchg("dhost"),
											"options", hlist
										}) +
										oof.br() +
										"&raquo; This field should contain the host name " +
										"of the target machine on which you would " +
										"copy to.  You may select a previously " +
										"configured host from the drop-down box on the " +
										"right."
								}
							},
/*
							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Message Passing Interface:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "checkbox",
											"name", "mpi",
											"label", "This job uses MPI."
										})
								}
							},
*/
							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Source File:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(sfile),
											"name", "sfile"
										}) +
										oof.br() +
										"&raquo; This field specifies the location of the" +
										"source file you want to copy. Use the checkbox to" +
										"specify if the path is relative to your home directory." +
										"Otherwise, the path is considered absolute."
								}
							},
							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Target File:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(dfile),
											"name", "dfile"
										}) +
										oof.br() +
										"&raquo; This field specifies the location of the" +
										"target file. Use the checkbox to specify if the" +
										"path is relative to your home directory." +
										"Otherwise, the path is considered absolute."
								}
							},
							new Object[][] {
								new Object[] {
									"colspan", "2",
									"class", Page.CCTBLFTR,
									"value", "" +
										oof.input(new Object[] {
											"onclick", js_submit,
											"type", "submit",
											"name", "submitted",
											"class", "button",
											"value", "Copy"
										}) +
										oof.input(new Object[] {
											"type", "reset",
											"class", "button",
											"value", "Reset Fields"
										})
								}
							}
						}
					)
				}
			 );

		s += p.footer();
		return (s);
	}

	public static String js_hostchg(String host) {
		String s =
			"	document.forms[0].elements['"+host+"'].value = " +
			"		(this.options[this.selectedIndex].value == 'Choose a host...') ? " +
			"		'' : this.options[this.selectedIndex].value ";
			return s;
	}
};

/* vim: set ts=4: */
