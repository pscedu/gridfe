/* $Id$ */

package gridfe.www.gridftp;

import gridfe.*;
import gridfe.gridint.*;
import java.sql.*;
import javax.servlet.http.*;
import oof.*;

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

		String llogin = "";
		String rlogin = "";
		String lbrowse = "";
		String rbrowse = "";

		/*
		** Set the content to display - login table if not logged in, otherwise browser
		*/
		if(lactive == null)
			llogin += browser.loginTable(p, oof, lhost, "lhost", browser.js_subval("Log In"), "lactive", hlist);
		else if(lactive.equals("Log out")) {
			/* XXX - do any necessary logout stuff */
//			llogin += "logged out";
			llogin += browser.loginTable(p, oof, lhost, "lhost", browser.js_subval("Log In"), "lactive", hlist);

		} else {
			llogin += "llogin is active";
		}

		if(ractive == null)
			rlogin = browser.loginTable(p, oof, rhost, "rhost", browser.js_subval("Log In"), "ractive", hlist);
		else if(ractive.equals("Log Out")) {
			/* XXX - do any necessary logout stuff */
//			rlogin += "logged out";
			rlogin = browser.loginTable(p, oof, rhost, "rhost", browser.js_subval("Log In"), "ractive", hlist);
		} else {
			rlogin += "rlogin is active";
		}
			

		/* Setup the nested table garbage */
		/* ------------------------------------------------------------- */
		s += oof.table(
				new Object[] {
				"class", Page.CCTBL,
				"border", "0",
				"cellspacing", "0",
				"cellpadding", "0"
			},
			new Object[][][]
			{
				/* 1st Row */
				new Object[][]
				{
					new Object[] {
						"class", Page.CCHDR,
						"value", "File Browsers",
						"colspan", "2"
					}
				},
				/* 2nd Row */
				new Object[][]
				{
					new Object[]
					{
						"value", llogin
					},
					new Object[]
					{
						"value", rlogin
					}
				},
				/* 3rd Row */
				new Object[][]
				{
					new Object[]
					{
						"colspan", "1",
						"class", Page.CCTBLFTR,
						"value", "" +
							oof.input(new Object[]
							{
								"onclick", browser.js_subval("Log Out"),
								"type", "submit",
								"name", "lactive",
								"class", "button",
								"value", "Log out"
							})
					},
					new Object[]
					{
						"colspan", "1",
						"class", Page.CCTBLFTR,
						"value", "" +
							oof.input(new Object[]
							{
								"onclick", browser.js_subval("Log Out"),
								"type", "submit",
								"name", "ractive",
								"class", "button",
								"value", "Log out"
							})
					}
				}
			}
		);
		/* ------------------------------------------------------------- */

		return s;
	}

	/*
	** login table for the gridftp structure 
	** Example: loginTable(oof, lhost, "lhost", ... )
	*/
	public static String loginTable(Page p, OOF oof, String varHost, String strHost,
					String js_submit, String subName, Object[] hlist)
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
							"value", "File Browser",
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
									oof.input(new Object[]
									{
										"onclick", js_submit,
										"type", "submit",
										"name", subName,
										"class", "button",
										"value", "Log in"
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
