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
		String errmsg = null;
		Object[] hlist;

		hlist = browser.createHostList(p);

		String lhost = req.getParameter("lhost");
		String rhost = req.getParameter("rhost");
		String action = req.getParameter("action");

		String s = "";
		OOF oof = p.getOOF();
		s += p.header("GridFTP File Browser");

		if (lhost == null)
			lhost = "";
		if (rhost == null)
			rhost = "";

		/*
		 * Set the content to display - login table if
		 * not logged in, otherwise browser
		 */
		if (lhost.equals("")) {
			s += oof.p("This GridFTP interface allows you to browse two " +
					"resources simultaneously and provides the ability to " +
					"transfer files between them.  Alternatively, you may " +
					"connect to only one resource if you wish to transfer " +
					"files between your local machine and that target resource.")
			  +  browser.login(p, "lhost", hlist);
		} else {
			s += browser.browse(p, lhost);
		}
		s += oof.hr();
		if (rhost.equals("")) {
			s += browser.login(p, "rhost", hlist);
		} else {
			s += "right browser is active";
		}
		s += p.footer();
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
			"	var el = this.form.elements[3];		" +
			"	var bv = 'Please wait...';			" +
			"	if (el.value != bv) {				" +
			"		el.value = bv;					" +
			"		return (true)					" +
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

	public static String browse(Page p, String hostname)
	  throws Exception {
		String s = "";
		OOF oof = p.getOOF();

		/* Grab the GridInt and make the GridFTP connection */
		GridInt gi = p.getGridInt();
		GridFTP gftp = new GridFTP(gi.getGSS().getGSSCredential(),
		  hostname, GRIDFTP_PORT);

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
		  + parse(p, gftp.gls())
		  + oof.table_row(new Object[][] {
				new Object[] {
					"class", Page.CCHDR,
					"value", "Testing - Logout Button",
					"colspan", "4"
				}
			})
		  + oof.table_end();
		 return (s);
	}

	public static String parse(Page p, Vector v) throws Exception {
		String s = "";
		GridFile gf;

		Collections.sort(v);

		for (int j = 0; j < v.size(); j++) {
			gf = (GridFile)v.get(j);
			if (gf.name.equals("."))
				continue;
			s += browser.build(gf, p);
		}
		return (s);
	}

	/* Build a row of the file list */
	private static String build(GridFile f, Page p)
	  throws Exception {
		String c = p.genClass();
		OOF oof = p.getOOF();

		return "" + oof.table_row(new Object[][] {
			new Object[] { "class", c,
			  "value", oof.link(p.escapeHTML(f.name) +
			  (f.isDirectory() ? "/" : ""), "?") },
			new Object[] { "class", c,
			  "value", humansiz(f.size),
			  "align", "right" },
			new Object[] { "class", c, "value", f.date + " " + f.time },
			new Object[] { "class", c, "value", f.perm }
		});
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
			"	var idx = this.selectedIndex;				" +
			"	var bv = 'Choose a resource...';			" +
			"	this.form.elements['" + host + "'].value =	" +
			"		(this.options[idx].value == bv) ?		" +
			"		'' : this.options[idx].value;			" +
			"	if (this.options[idx].value != bv)			" +
			"		this.form.submit();						";
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
