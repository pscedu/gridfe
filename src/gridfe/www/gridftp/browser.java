/* $Id$ */

package gridfe.www.gridftp;

import gridfe.*;
import gridfe.gridint.*;
import java.sql.*;
import javax.servlet.http.*;
import oof.*;
import java.util.*;

public class browser {
	public static String main(Page p)
	  throws Exception {
		HttpServletRequest req = p.getRequest();
		String errmsg = null;
		Object[] hlist;
		
		hlist = browser.createHostList(p);
		
		String lhost = req.getParameter("lhost");
		String rhost = req.getParameter("rhost");
		String lactive = req.getParameter("lactive");
		String ractive = req.getParameter("ractive");

		String s = "";
		OOF oof = p.getOOF();
		s += p.header("GridFTP File Browser");
		
		if(lhost == null) lhost = "";
		if(rhost == null) rhost = "";

		String lbrowser = "";
		String rbrowser = "";

		/*
		** Set the content to display - login table if not logged in, otherwise browser
		*/
		if(lactive == null) {
			lbrowser += browser.login(p, oof, lhost, "lhost", browser.js_subval("Login"), "lactive", "Machine 1", hlist);
		} else if(lactive.equals("Log out")) {
			/* XXX - do any necessary logout stuff */
			lbrowser += browser.login(p, oof, lhost, "lhost", browser.js_subval("Login"), "lactive", "Machine 1", hlist);

		} else {

			/* Grab the GridInt and make the GridFTP connection */
			GridInt gi = p.getGridInt();
			GridFTP gftp = new GridFTP(gi.getGSS().getGSSCredential(), lhost, 2811);

			Vector v = gftp.gls();
			GridFile gf;
			String data = "";
			
			/* Put each file (with size, perm, date, time) on a separte line */
			// XXX - name should become a link for chdir or download file
			for(int i = 0; i < v.size(); i++)
			{
				gf = (GridFile)(v.get(i));
				data += oof.p(gf.toString());
			}
			
			lbrowser += browser.browse(p, oof, "Logout", "lactive", "Machine 1", data);
		}

		if(ractive == null) {
			rbrowser += browser.login(p, oof, rhost, "rhost", browser.js_subval("Login"), "ractive", "Machine 2", hlist);
		} else if(ractive.equals("Log Out")) {
			/* XXX - do any necessary logout stuff */
			rbrowser += browser.login(p, oof, rhost, "rhost", browser.js_subval("Login"), "ractive", "Machine 2", hlist);
		} else {
			rbrowser += "right browser is active";
		}
			

		/* Setup the layout */
		s += oof.table(
			new Object[]
			{
				"class", Page.CCTBL,
				"border", "0",
				"cellspacing", "0",
				"cellpadding", "0"
			},
			new Object[][][]
			{
				new Object[][]
				{
					new Object[]
					{
						"class", Page.CCHDR,
						"value", "File Browsers",
						"colspan", "2"
					}
				},
				new Object[][]
				{
					new Object[]
					{
						"value", lbrowser
					},
				},
				new Object[][]
				{
					new Object[]
					{
						"value", rbrowser
					}
				}
			}
		);

		return s;
	}

	/*
	** login table for the gridftp structure 
	** Example: loginTable(oof, lhost, "lhost", ... )
	*/
	public static String login(Page p, OOF oof, String varHost, String strHost,
					String js_submit, String subName, String title, Object[] hlist)
					throws OOFBadElementFormException {
		String s = "";

		/* Form field for logging in */
		s += oof.p("Enter a hostname to browse");
		s += oof.form(
				new Object[]
				{
					"action", "browser",
					"method", "POST",
					"enctype", "application/x-www-form-urlencoded"
				},
				new Object[]
				{
					oof.table(
					new Object[]
					{
						"class", Page.CCTBL,
						"border", "0",
						"cellspacing", "0",
						"cellpadding", "0"
					},
					new Object[][][]
					{
						new Object[][]
						{
							new Object[] {
							"class", Page.CCHDR,
							"value", title,
							"colspan", "2"
							}
						},
						new Object[][]
						{
							new Object[]
							{
								"class", Page.CCDESC,
								"value", "Hostname:"
							},
							new Object[]
							{
								"class", p.genClass(),
								"value", "" +
									oof.input(new Object[] {
										"type", "text",
										"value", p.escapeHTML(varHost),
										"name", strHost
									}) +
									oof.input(new Object[] {
										"type", "select",
										"onchange", browser.js_hostchg(strHost),
										"options", hlist
									}) +
									oof.br() +
									"&raquo; This field should contain the host name " +
									"of the machine you wish to browse"
							}
						},
						new Object[][]
						{
							new Object[]
							{
								"colspan", "2",
								"class", Page.CCTBLFTR,
								"value", "" +
									oof.input(new Object[] {
										"onclick", js_submit,
										"type", "submit",
										"name", subName,
										"class", "button",
										"value", "Login"
									})
							}
						}
					}
				)
			}
		 );

		 return s;
	}

	public static String browse(Page p, OOF oof, String subStr, String subName, String title, String data)
					throws OOFBadElementFormException {
		String s = "";

		/* Form field for logging in */
		s += oof.p("Enter a hostname to browse");
		s += oof.form(
				new Object[]
				{
					"action", "browser",
					"method", "POST",
					"enctype", "application/x-www-form-urlencoded"
				},
				new Object[]
				{
					oof.table(
					new Object[]
					{
						"class", Page.CCTBL,
						"border", "0",
						"cellspacing", "0",
						"cellpadding", "0"
					},
					new Object[][][]
					{
						new Object[][]
						{
							new Object[] {
							"class", Page.CCHDR,
							"value", title,
							"colspan", "2"
							}
						},
						new Object[][]
						{
							new Object[]
							{
								"value", data
							}
						},
						new Object[][]
						{
							new Object[]
							{
								"colspan", "2",
								"class", Page.CCTBLFTR,
								"value", "" +
									oof.input(new Object[] {
										"onclick", browser.js_subval("Logout"),
										"type", "submit",
										"name", subName,
										"class", "button",
										"value", "Logout"
									})
							}
						}
					}
				)
			}
		 );

		 return s;
	}
		
	public static String js_subval(String value) {
		String js_submit =
			"	if (this.value == '"+value+"') {	" +
			"		this.value = 'Please wait...';	" +
			"		return (true)					" +
			"	}";

		return js_submit;
	}
	public static String js_hostchg(String host) {
		String s =
			"	this.form.elements['"+host+"'].value = " +
			"		(this.options[this.selectedIndex].value == 'Choose a host...') ? " +
			"		'' : this.options[this.selectedIndex].value ";
			return s;
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
		hlist[1] = "Choose a resource";
		for (int i = 2; rs.next(); i += 2)
			hlist[i] = hlist[i + 1] = rs.getString("host");

		return hlist;
	}
};

/* vim: set ts=4: */
