/* $Id$ */

package gridfe.www;

import gridfe.*;
import gridfe.gridint.*;
import gridfe.gridint.auth.*;
import java.io.*;
import java.sql.*;
import javax.servlet.http.*;
import oof.*;
import org.globus.gram.*;
import org.ietf.jgss.*;

public class nodes {
	public static final int GRIDFTP_PORT = 2811;

	public static String main(Page p)
	  throws Exception {
		HttpServletRequest req = p.getRequest();

		String act = req.getParameter("action");
		if (act == null)
			act = "";
		if (act.equals("add"))
			return (add(p));
		else if (act.equals("remove"))
			return (remove(p));
		else
			return (status(p));
	}

	public static String add(Page p)
	  throws Exception {
		HttpServletRequest req = p.getRequest();
		OOF oof = p.getOOF();
		String s = "", emsg = "";

		String host = req.getParameter("host");
		String type = req.getParameter("type");

		if (host == null || host.equals(""))
			emsg += " Please specify a resource to add.";
		else if (type == null ||
		  (!type.equals("archiver") && !type.equals("gridftp") &&
		   !type.equals("scratch") && !type.equals("other")))
			emsg += " Please specify a resource type.";
		else {
			PreparedStatement sth = p.getDBH().prepareStatement(
				"	INSERT INTO hosts (		" +
				"		uid, host, type		" +
				"	) VALUES (				" +
				"		?, ?, ?				" +
				"	)						");
			sth.setInt(1, p.getUID());
			sth.setString(2, host);
			sth.setString(3, type);
			int nrows = sth.executeUpdate();

			if (nrows != 1)
				emsg += " The resource could not be added to the system.";

			if (emsg.equals("")) {
				s += p.header("Host Added")
				   + oof.p("The resource " + p.escapeHTML(host) +
						" has been successfully added.")
				   + p.footer();
				return (s);
			}
		}
		s += p.header("Error")
		   + oof.p(new Object[] { "class", "err" }, "An error has occurred:" + emsg)
		   + p.footer();
		return (s);
	}

	public static String remove(Page p)
	  throws Exception {
		HttpServletRequest req = p.getRequest();
		OOF oof = p.getOOF();
		String s = "";
		String host = req.getParameter("host");

		if (host == null)
			host = "";

		PreparedStatement sth = p.getDBH().prepareStatement(
			"	DELETE FROM				" +
			"		hosts				" +
			"	WHERE					" +
			"		uid  = ?			" +	/* 1 */
			"	AND	host = ? 			");	/* 2 */
		sth.setInt(1, p.getUID());
		sth.setString(2, host);
		sth.execute();

		s += p.header("Resources Removed")
		   + oof.p("Resources removed successfully.")
		   + p.footer();
		return (s);
	}

	public static String status(Page p)
	  throws Exception {
		OOF oof = p.getOOF();
		String s = "";

		PreparedStatement sth = p.getDBH().prepareStatement(
			"	SELECT					" +
			"		*					" +
			"	FROM					" +
			"		hosts				" +
			"	WHERE					" +
			"		uid = ?				");	/* 1 */
		sth.setInt(1, p.getUID());
		ResultSet rs = sth.executeQuery();

		s += p.header("Node Availibility")
		   + oof.p("The following lists which grid hosts you have specified as machines " +
		   		"you regularly access.  You may add and remove hosts from this list at " +
				"will.")
		   + oof.form_start(new Object[] {})
		   +	oof.table_start(new Object[] {
		   			"class", "tbl",
					"border", "0",
					"cellspacing", "0",
					"cellpadding", "0"
				})
		   +		oof.table_row(new Object[][] {
		   				new Object[] { "class", "subhdr", "value", "Resource" },
		   				new Object[] { "class", "subhdr", "value", "Type" },
		   				new Object[] { "class", "subhdr", "value", "GRAM" },
		   				new Object[] { "class", "subhdr", "value", "GridFTP" },
		   				new Object[] { "class", "subhdr", "value", "Remove?" }
		   			});

		PrintWriter w = p.getResponse().getWriter();
		w.print(s);
		s = "";

		int i = 0;
		for (; rs.next(); i++) {
			String host = rs.getString("host");
			String type = rs.getString("type");

			boolean up = true;
			try {
				Gram.ping(p.getGridInt().getGSS().getGSSCredential(), host);
			} catch (Exception e) {
				up = false;
			}

			boolean fup = true;
			try {
				GSSAuth gss = p.getGridInt().getGSS();
				GridFTP ftp = new GridFTP(gss.getGSSCredential(),
				  host, GRIDFTP_PORT);
			} catch (Exception e) {
				fup = false;
			}

			String path = p.getWebRoot() + "/img/";

			String cl = p.genClass();
			s = "" + oof.table_row(new Object[][] {
						new Object[] { "class", cl, "value", p.escapeHTML(host) },
						new Object[] { "class", cl, "value", type },
						new Object[] { "class", cl, "style", "text-align: center",
							"value", oof.img(new Object[] {
								"src", path + (up ? "on.png" : "off.png"),
								"alt", (up ? "[up]" : "[down]")})
						},
						new Object[] { "class", cl, "style", "text-align: center",
							"value", oof.img(new Object[] {
								"src", path + (fup ? "on.png" : "off.png"),
								"alt", (fup ? "[up]" : "[down]")})
						},
						new Object[] { "class", cl, "value", oof.input(new Object[] {
								"type", "checkbox",
								"name", "host",
								"value", p.escapeHTML(rs.getString("host"))
							}) }
					 });
			w.print(s);
			s = "";
		}
		if (i == 0)
			s += 	oof.table_row(new Object [][] {
						new Object[] {
							"class", "data1",
							"colspan", "5",
							"value", "You do not have any resources configured at this time."
						}
					 });
		else
			s += 	oof.table_row(new Object [][] {
						new Object[] {
							"class", "tblftr",
							"colspan", "5",
							"value", "" +
								oof.input(new Object[] {
									"type", "hidden",
									"name", "action",
									"value", "remove"
								}) +
								oof.input(new Object[] {
									"type", "submit",
									"class", "button",
									"value", "Remove Checked"
								})
						}
					 });
		s += ""
		   + 	oof.table_end()
		   + oof.form_end()
		   + oof.br()
		   + oof.form_start(new Object[] { })
		   +	"Add a new resource: "
		   +	oof.input(new Object[] {
		   			"type", "text",
					"name", "host"
				})
		   +	oof.input(new Object[] {
		   			"type", "select",
					"name", "type",
					"options", new Object[] {
						"other", "Other",
						"archiver", "Archiver",
						"gridftp", "GridFTP",
						"scratch", "Scratch"
					}
				})
		   +	oof.input(new Object[] {
		   			"type", "hidden",
					"name", "action",
					"value", "add"
				})
		   +	oof.input(new Object[] {
		   			"type", "submit",
					"class", "button",
					"value", "Add Host"
				})
		   + oof.form_end()
		   + p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
