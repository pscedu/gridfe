/* $Id$ */

package gridfe;

import gridfe.gridint.*;
import jasp.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.http.*;
import oof.*;
import org.bouncycastle.util.encoders.*;

public class Page {
	private HttpServletRequest req;
	private HttpServletResponse res;
	private LinkedList menus;
	private String servroot;
	private String webroot;
//	private String sysroot;
	private int classCount;
	private GridInt gi;
	private JASP jasp;
	private OOF oof;
	private int uid;
	private String kuid;
	private Connection dbh;

	private static final String DB_DRIVER = "mysql";
	private static final String DB_HOST = "localhost";
	private static final String DB_USER = "gridfe";
	private static final String DB_NAME = "gridfe";
	private static final String DB_PASS = "pDMLP534AO6";

	/* CSS class desc. */
	public final static Object CCDESC = (Object)"desc";
	public final static Object CCHDR = (Object)"hdr";
	public final static Object CCSUBHDR = (Object)"subhdr";
	public final static Object CCTBL = (Object)"tbl";
	public final static Object CCTBLFTR = (Object)"tblftr";
	public final static int MENU_ITEM_HEIGHT = 35;

	public final static int PATHTYPE_WEB = 0;
	public final static int PATHTYPE_SERV = 1;

	Page(HttpServletRequest req, HttpServletResponse res) {
		this.req = req;
		this.res = res;
		this.jasp = new JASP(req, res);
		this.menus = new LinkedList();
		this.webroot = "/gridfe";
//		this.sysroot = "/var/www/gridfe/WEB-INF/classes/gridfe";
		this.servroot = "/gridfe/gridfe";
		this.classCount = 1;
		this.uid = -1;
		this.dbh = null;

		try {
			String hdr;

			String dsn = "jdbc:" + DB_DRIVER + "://" + DB_HOST + "/" + DB_NAME;
			DriverManager.registerDriver((Driver)Class.forName("com." +
			    DB_DRIVER + ".jdbc.Driver").newInstance());
			this.dbh = DriverManager.getConnection(dsn, DB_USER, DB_PASS);

			/* XXX: check for errors here. */
			hdr = (String)req.getHeader("authorization");
			if (hdr.startsWith("Basic "))
				hdr = hdr.substring(6);
			String combo = new String(Base64.decode(hdr));
			String[] auth = BasicServices.splitString(combo, ":");
			this.kuid = auth[0];

			if (!this.restoreGI()) {
//				UserMap m = new UserMap();
//				String user = m.kerberosToSystem(kuid);
//				this.uid = BasicServices.getUserID(user);

				/*
				 * XXX - Assume user's Kerberos principal
				 * and system username are the same.
				 */
				this.uid = BasicServices.getUserID(kuid);

				/*
				 * Reparse authorization because getRemoteUser() doesn't
				 * work.
				 */
				this.gi = new GridInt(this.uid);
				this.gi.auth();
			}
			/* XXX: load oof prefs from config/resource. */
			this.oof = new OOF(this.jasp, "xhtml");
		} catch (Exception e) {
			this.error(e.getClass().getName() + ": " + e.toString());
		}
	}

	public int getUID() {
		return (this.uid);
	}

	private String getGIPath() {
		return ("/tmp/gridfe.gi_" + this.kuid);
	}

	private boolean restoreGI() {
		try {
			FileInputStream fin = new FileInputStream(this.getGIPath());
			ObjectInputStream in = new ObjectInputStream(fin);
			this.gi = (GridInt)in.readObject();
			in.close();

			this.uid = this.gi.getUID().intValue();
			return (true);
		} catch (Exception e) {
			return (false);
		}
	}

	public void end() {
		/* This may happen on error... */
		if (this.gi == null)
			return;
		/* Serialize GridInt. */
		try {
			FileOutputStream fout = new FileOutputStream(this.getGIPath());
			ObjectOutputStream out = new ObjectOutputStream(fout);
			out.writeObject(this.gi);
			out.close();
		} catch (Exception e) {
		}
	}

	private void addMenu(String name, String url, Object[] items) {
		this.menus.add(new Menu(name, url, items));
	}

	private LinkedList getMenus() {
		return (this.menus);
	}

	private String addScript(String code) {
		String s;

		s =   "<script type=\"text/javascript\">"
			+	"<!--\n" /* Mozilla requires a newline here. */
					/* XXX: quote/JS escape */
			+		code
			+	"// -->"
			+ "</script>";
		return (s);
	}

	public String divName(String name) {
		String t = "";

		for (int i = 0; i < name.length(); i++)
			if (name.charAt(i) != ' ' &&
			    name.charAt(i) != '/')
				t += name.charAt(i);
		return (t);
	}

	public String imageName(String name) {
		String t = "";

		for (int i = 0; i < name.length(); i++)
			if (name.charAt(i) == ' ')
				t += '-';
			else
				t += Character.toLowerCase(name.charAt(i));
		return (t);
	}

	public String buildMenu() {
		String name, url, p, t = "var menus = [";
		Menu m;
		Iterator i, j;

		for (i = this.getMenus().iterator();
		     i.hasNext() && (m = (Menu)i.next()) != null; ) {
			t += " [ '" + divName(m.getName()) + "', ";
			if (m.getItems() != null) {
				t += "menu" + divName(m.getName());
				p = "var menu" + divName(m.getName()) + " = [";
				for (j = m.getItems().iterator();
				     j.hasNext() && (name = (String)j.next()) != null &&
				     j.hasNext() && (url  = (String)j.next()) != null; ) {
					p += "'" + divName(m.getName() + name) +
					     "'";
					if (j.hasNext())
						p += ",";
				}
				p += "];";
				t = p + t;
			} else {
				t += "null";
			}
			t += " ]";
			if (i.hasNext())
				t += ",";
		}

		t += "];";
		return (t);
	}

	public String header(String title)
	  throws OOFException {
		String s, name, url;
		String wr = this.webroot;
		Menu m;

		/* Register menu. */
		this.addMenu("Main", "/", null);
		this.addMenu("System News", "http://www.psc.edu/general/posts/posts.html", null);
		this.addMenu("Jobs", "/jobs",
			new Object[] {
				"Submit", "/jobs/submit",
				"Status", "/jobs/status"
			});
		this.addMenu("Certificate Management", "/certs", null);
//		this.addMenu("MDS/LDAP", "/ldap", null);
//		this.addMenu("GridFTP", "/gridftp", null);
//		this.addMenu("GRIS/GIIS", "/gris", null);
		this.addMenu("Replica Locator", "/rls",
			new Object[] {
				"Add Catalog",		"/rls/addcat",
				"Remove Catalog",	"/rls/rmcat",
				"Search Catalogs",	"/rls/search",
				"Add Resource",		"/rls/addres"
			});
		this.addMenu("Node Availability", "/nodes", null);

		/* Start page output. */
		s = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\">"
		  + "<html lang=\"en-US\" xml:lang=\"en-US\" xmlns=\"http://www.w3.org/1999/xhtml\">"
		  + 	"<head>"
		  +	 		"<title>GridFE - " + this.jasp.escapeHTML(title) + "</title>"
		  +			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />"
		  +			"<meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />"
		  +			"<link rel=\"stylesheet\" type=\"text/css\" "
		  +				"href=\"" + wr + "/lib/main.css\" media=\"screen\">"
		  +			"<script type=\"text/javascript\" src=\"" + wr + "/lib/Global.js\"></script>"
		  +			this.addScript(
		   				"include('" + wr + "/lib/Browser.js');" +
		   				"include('" + wr + "/lib/util.js');")
		  +			this.addScript(this.buildMenu())
		  +			this.addScript(
		   				/* This must be loaded last. */
		   				"include('" + wr + "/lib/main.js');")
		  +		"</head>"
		  +		"<body>"
		  +			"<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"bg\" width=\"827\">"
		  +				"<tr>"
		  +					"<td width=\"170\" align=\"center\" valign=\"top\" "
		  +					  "style=\"border-bottom:1px solid black\">"
		  						/* GridFE logo. */
		  +						"<img src=\"" + wr + "/img/gridfe2sm.png\" alt=\"[GridFE]\" "
		  +						 "border=\"0\" style=\"margin-top: 5px\" />"
		  +						"<br /><br />";

//		y = -1 * MENU_ITEM_HEIGHT * this.getMenus().size();
		for (Iterator i = this.getMenus().iterator();
		     i.hasNext() && (m = (Menu)i.next()) != null; ) {
			s +=				"<a href=\"" + this.buildURL(m.getURL()) + "\">"
			   +					"<img src=\"" + wr + "/img/buttons/"
			   +						imageName(m.getName()) + ".png\" "
			   +					     "alt=\"" + m.getName() + "\" border=\"0\" />"
			   +				"</a>";
			if (m.getItems() != null) {
				/* Sub-menu */
				for (Iterator j = m.getItems().iterator();
				     j.hasNext() && (name = (String)j.next()) != null &&
				     j.hasNext() && (url  = (String)j.next()) != null; ) {
					s +=			"<a href=\"" + this.buildURL(url) + "\">"
					   +				"<img src=\"" + wr + "/img/buttons/"
					   +					imageName(name) + ".png\" "
					   +				     "alt=\"" + name + "\" border=\"0\" />"
					   +			"</a>";
				}
			}
		}

								/* Sponsors */
		s +=					"<br /><br />"
		   						/* PSC logo. */
		   +					"<a href=\"http://www.psc.edu/\">"
		   +						"<img src=\"" + wr + "/img/psc.png\" "
		   +						     "alt=\"[Pittsburgh Supercomputing Center]\" "
		   +						     "border=\"0\" />"
		   +					"</a>"
		   +					"<br /><br />"
		   +					"<a href=\"http://www-unix.globus.org/cog/\">"
		   +						"<img src=\"" + wr + "/img/cog-toolkit.png\" border=\"0\" />"
		   +					"</a>"
		   +					"<a href=\"http://www.globus.org/toolkit/\">"
		   +						"<img src=\"" + wr + "/img/globus-toolkit.png\" border=\"0\" />"
		   +					"</a>"
		   +					"<br /><br />"
		   +				"</td>"
		   +				"<td style=\"background-color: #eeeeff; padding-left: 5px; "
		   +				  "border-left: 1px solid black\" valign=\"top\">"
		   +					this.oof.header(new Object[] {
									"size", "3",
									"style", "margin-top:0px"
								}, "<img align=\"middle\" src=\"" + wr + "/img/box.png\" " +
									"alt=\"\" border=\"0\" />" + title);
		return (s);
	}

	public String footer() {
		String s = "";

		s +=				"</td>"
		   +			"</tr>"
		   +			"<tr>"
		   +				"<td align=\"right\" colspan=\"3\" style=\"background-color: #eeeeff\">"
		   +					"Copyright &copy; 2004-2006 "
		   +					"<a href=\"http://www.psc.edu/\">Pittsburgh Supercomputing Center</a>"
		   +				"</td>"
		   +			"</tr>"
		   +		"</table>"
		   +	"</body>"
		   + "</html>";

		return (s);
	}

	public void error(String error) {
		try {
			PrintWriter w;
			w = this.res.getWriter();
			w.println("Error: " + error);
//			System.exit(1);
		} catch (Exception e) {
		}
	}

	public OOF getOOF() {
		return (this.oof);
	}

	public JASP getJASP() {
		return (this.jasp);
	}

	public String genClass() {
		return (this.classCount++ % 2 == 0 ? "data1" : "data2");
	}

	public GridInt getGridInt() {
		return (this.gi);
	}

	public String getServRoot() {
		return (this.servroot);
	}

	public String getWebRoot() {
		return (this.webroot);
	}

	public HttpServletRequest getRequest() {
		return (this.req);
	}

	public HttpServletResponse getResponse() {
		return (this.res);
	}

	public Connection getDBH() {
		return (this.dbh);
	}

	public String buildURL(String s) {
		if (s.indexOf(':') != -1) {
			return (s);
		} else {
			return (this.servroot + s);
		}
	}

	public String buildURL(int type, String s) {
		if (s.indexOf(':') != -1)
			return (s);
		switch (type) {
		case PATHTYPE_WEB:
			return (this.webroot + s);
		case PATHTYPE_SERV:
			return (this.servroot + s);
		default:
			/* throw new InvalidPathTypeException(); */
			return (null);
		}
	}

	public String escapeHTML(String s) {
		String t = "";
		char ch;
		for (int i = 0; i < s.length(); i++) {
			ch = s.charAt(i);
			switch (ch) {
			case '<':  t += "&lt;";		break;
			case '>':  t += "&gt;";		break;
			case '"':  t += "&quot;";	break;
			case '\'': t += "&apos;";	break;
			case '&':  t += "&amp;";	break;
			default:
				if (ch == ' ' || ch == '\n' || (32 <= ch && ch <= 126))
					t += ch;
				else if (ch != '\0')
					t += "&#" + new Integer(ch) + ";";
				break;
			}
		}
		return (t);
	}

	public String escapeURL(String s) {
		String t = "";
		char ch;
		for (int i = 0; i < s.length(); i++) {
			ch = s.charAt(i);
			if (ch == ' ')
				t += '+';
			else if (Character.isLetterOrDigit(ch))
				t += ch;
			else
				t += "%" + Integer.toHexString(ch);
		}
		return (t);
	}
};

class Menu {
	private String name;
	private String url;
	private LinkedList items;

	public Menu(String name, String url, Object[] items) {
		this.name = name;
		this.url = url;
		this.items = new LinkedList();
		if (items != null) for (int i = 0; i < items.length; i++)
				this.items.add(items[i]);
	}

	public String getName() {
		return (this.name);
	}

	public String getURL() {
		return (this.url);
	}

	public LinkedList getItems() {
		return (this.items);
	}
}

/* vim: set ts=4: */
