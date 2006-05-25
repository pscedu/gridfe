/* $Id$ */

package gridfe.www.gridftp;

import gridfe.*;
import gridfe.gridint.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.http.*;
import oof.*;
import org.globus.ftp.exception.*;

public class browser {
	public static final int GRIDFTP_PORT = 2811;

	public static String main(Page p)
	  throws Exception {
		HttpServletRequest req = p.getRequest();

		String lhost = req.getParameter("lhost");
		String rhost = req.getParameter("rhost");
		String lcwd = req.getParameter("lcwd");
		String rcwd = req.getParameter("rcwd");
		String action = req.getParameter("action");

		if (lhost == null)
			lhost = "";
		if (rhost == null)
			rhost = "";
		if (lcwd == null)
			lcwd = "";
		if (rcwd == null)
			rcwd = "";
		if (action == null)
			action = "";

		int len = 0;

		if (!lhost.equals(""))
			len++;
		if (!rhost.equals(""))
			len++;
		if (!lcwd.equals(""))
			len++;
		if (!rcwd.equals(""))
			len++;
		if (!action.equals(""))
			len++;

		String[] params = new String[2 * len];

		int j = 0;

		if (!lhost.equals("")) {
			params[j++] = "lhost";
			params[j++] = lhost;
		}
		if (!rhost.equals("")) {
			params[j++] = "rhost";
			params[j++] = rhost;
		}
		if (!lcwd.equals("")) {
			params[j++] = "lcwd";
			params[j++] = lcwd;
		}
		if (!rcwd.equals("")) {
			params[j++] = "rcwd";
			params[j++] = rcwd;
		}
		if (!action.equals("")) {
			params[j++] = "action";
			params[j++] = action;
		}

		if (action.equals("download")) {
		}

		Object[] hlist = browser.createHostList(p);

		String s = "";
		OOF oof = p.getOOF();
		s += p.header("GridFTP File Browser");

		/*
		 * Set the content to display -- browser if
		 * connected, otherwise login table.
		 */
		if (lhost.equals("")) {
			s += oof.p("This GridFTP interface allows you to browse two " +
					"resources simultaneously and provides the ability to " +
					"transfer files between them.  Alternatively, you may " +
					"connect to only one resource if you wish to transfer " +
					"files between your local machine and that target resource.")
			  +  browser.login(p, "lhost", hlist);
		} else {
			s += browser.browse(p, "l", lhost, params, lcwd);
		}

		s += oof.hr();

		if (rhost.equals("")) {
			s += browser.login(p, "rhost", hlist);
		} else {
			s += browser.browse(p, "r", rhost, params, rcwd);
		}
		s += p.footer();
		return (s);
	}

	/* Construct a query string (?foo=bar&...). */
	public static String buildQS(Page page, String[] p, String[] pnew) {
		String s, key, val;

		s = "?";
		for (int j = 0; j + 1 < p.length; j += 2) {
			val = p[j + 1];
			for (int k = 0; k + 1 < pnew.length; k += 2)
				if (pnew[k].equals(p[j])) {
					val = pnew[k + 1];
					break;
				}
			s += p[j] + "=" + page.escapeURL(val) + "&amp;";
		}

		boolean found;
		for (int k = 0; k + 1 < pnew.length; k += 2) {
			found = false;
			for (int j = 0; j + 1 < p.length; j += 2)
				if (p[j].equals(pnew[k])) {
					found = true;
					break;
				}
			if (found)
				continue;
			s += pnew[k] + "=" + page.escapeURL(pnew[k + 1]) + "&amp;";
		}
		return (s);
	}

	/*
	 * Login table for the gridftp structure
	 * Example: loginTable(oof, lhost, "lhost", ... )
	 */
	public static String login(Page p, String hfname, Object[] hlist)
	  throws Exception {
		String s = "";
		OOF oof = p.getOOF();

		String js_submit =
			"	var el = this.form.elements[2];		" +
			"	var bv = 'Please wait...';			" +
			"	if (el.value != bv) {				" +
			"		el.value = bv;					" +
			"		return (true);					" +
			"	}									";

		/* Form field for logging in */
		s += oof.form(
				new Object[] {
					"action", "browser",
					"onsubmit", js_submit
				},
				new Object[] {
					"Hostname: " +
					oof.input(new Object[] {
						"type", "text",
						"name", hfname
					}) +
					oof.input(new Object[] {
						"type", "select",
						"onchange", js_hostchg(hfname),
						"options", hlist
					}) +
					oof.p("&raquo; Enter the host name of the resource " +
					"that you would like to browse over GridFTP.") +
					oof.input(new Object[] {
						"type", "submit",
						"class", "button",
						"value", "Login"
					})
				});
		 return (s);
	}

	public static String browse(Page p, String display, String hostname,
	  String[] params, String cwd) throws Exception {
		String s = "";
		OOF oof = p.getOOF();

		/* Grab the GridInt and make the GridFTP connection */
		GridInt gi = p.getGridInt();
		GridFTP gftp = new GridFTP(gi.getGSS().getGSSCredential(),
		  hostname, GRIDFTP_PORT);
		if (!cwd.equals(""))
			gftp.changeDir(cwd);

		/* Form field for logging in */
		s += ""
		  + oof.p("Click on a file to download or a directory to view it's contents.")
		  + oof.form_start(new Object[] {
				"action", "browser",
				"method", "GET",
				"enctype", "application/x-www-form-urlencoded"
			})
		  + oof.table_start(new Object[] {
				"class", Page.CCTBL,
				"border", "0",
				"cellspacing", "0",
				"cellpadding", "0",
				"cols", new Object[][] {
					new Object[] { },
					new Object[] { "align", "char", "char", "." },
					new Object[] { },
					new Object[] { },
				}
			})
		  + oof.table_row(new Object[][] {
				new Object[] {
					"class", Page.CCHDR,
					"value", "Viewing gridftp://" + hostname + gftp.getCurrentDir(),
					"colspan", "4"
				}
			})
		  + oof.table_row(new Object[][] {
				new Object[] { "class", Page.CCSUBHDR, "value", "Name" },
				new Object[] { "class", Page.CCSUBHDR, "value", "Size" },
				new Object[] { "class", Page.CCSUBHDR, "value", "Date" },
				new Object[] { "class", Page.CCSUBHDR, "value", "Modes" }
			})
		/*
		 * Parse the list and put each file (with size, perm,
		 * date/time) on a table row.
		 */
		  + listing(display, p, gftp, params)
		  + oof.table_row(new Object[][] {
				new Object[] {
					"class", Page.CCTBLFTR,
					"value", "" + oof.form(new Object[] {
							"action", "?",
							"style", "display: inline"
						}, new Object[] {
							oof.input(new Object[] {
								"type", "submit",
								"class", "button",
								"value", "Logout"
							}),
						}) +
						oof.form(new Object[] {
							"action", "?",
							"style", "display: inline"
						}, new Object[] {
							oof.input(new Object[] {
								"type", "submit",
								"class", "button",
								"value", "Upload"
							}),
						}),
					"colspan", "4"
				}
			})
		  + oof.table_end();
		 return (s);
	}

	public static final int MAXFNLEN = 40;

	public static String listing(String display, Page p, GridFTP gftp,
	  String[] params) throws Exception {
		OOF oof = p.getOOF();
		GridFile gf;

		Vector v = gftp.gls();
		Collections.sort(v);
		String cwd = gftp.getCurrentDir();

		String prefix = p.getWebRoot() + "/img/";

		String s = "", qs;
		for (int j = 0; j < v.size(); j++) {
			gf = (GridFile)v.get(j);
			if (gf.name.equals("."))
				continue;

			String cl = p.CCMONO + p.genClass();
			String fn = p.escapeHTML(gf.name.length() > MAXFNLEN ?
			  gf.name.substring(0, MAXFNLEN) + "..." : gf.name) +
			  (gf.isDirectory() ? "/" : "");
			String img = "" + oof.img(new Object[] {
				"src", prefix + (gf.isDirectory() ? "folder.png" : "file.png"),
				"alt", "[img]",
				"align", "middle",
				"border", "0"
			});

			if (gf.isDirectory())
				qs = buildQS(p, params, new String[] {
					display + "cwd", cwd + "/" + gf.name
				});
			else
				qs = buildQS(p, params, new String[] {
					"action", "download",
					"file", gf.name
				});

			s += "" + oof.table_row(new Object[][] {
				new Object[] { "class", cl,
					"value", oof.link(img + " " + fn, qs) },
				new Object[] { "class", cl,
					"value", humansiz(gf.size),
					"align", "right" },
				new Object[] { "class", cl, "value",
					/* XXX: escapeHTML() these? */
					gf.date + " " + gf.time },
				new Object[] { "class", cl, "value", gf.perm }
			});
		}
		return (s);
	}

	public static String humansiz(long n) {
		String sufx = "BKMGTP";
		int idx = 0;

		double m = (double)n;
		while (m > 1024.0) {
			idx++;
			m /= 1024.0;
		}
		if (idx >= sufx.length())
			return "" + n;
		else
			return "" + (((long)(m * 10)) / 10.0) + sufx.charAt(idx);
	}

	public static String js_hostchg(String host) {
		/*
		 * XXX: use options[0].value instead
		 * of hardcoding option value.
		 */
		String s =
			"	var idx = this.selectedIndex;						" +
			"	var bv = 'Choose a resource...';					" +
			"	this.form.elements['" + host + "'].value =			" +
			"		(this.options[idx].value == bv) ?				" +
			"		'' : this.options[idx].value;					" +
			"	if (this.options[idx].value != bv) {				" +
			"		this.form.submit();								" +
			"		this.form.elements[2].value = 'Please wait...';	" +
			"	}													";
		return (s);
	}

	/* Retrieve the list of hostname for the host drop down menu */
	public static Object[] createHostList(Page p)
		throws SQLException {

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
		hlist[1] = "Choose a resource...";
		for (int i = 2; rs.next(); i += 2)
			hlist[i] = hlist[i + 1] = rs.getString("host");

		return hlist;
	}
};

/* vim: set ts=4: */
