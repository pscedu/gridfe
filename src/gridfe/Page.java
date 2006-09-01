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
	private HttpServletResponse res;
	private HttpServletRequest req;
//	private String sysroot;
	private int classCount;
	private GridInt gi;
	private JASP jasp;
	private OOF oof;
	private Connection dbh;
	private boolean dohdr;
	private int uid;

	/* Database parameters. */
	private static final String DB_DRIVER = "mysql";
	private static final String DB_HOST = "localhost";
	private static final String DB_USER = "gridfe";
	private static final String DB_NAME = "gridfe";
	private static final String DB_PASS = "pDMLP534AO6";

	/* CSS classes. */
	public final static Object CCDESC	= (Object)"desc";
	public final static Object CCHDR 	= (Object)"hdr";
	public final static Object CCSUBHDR	= (Object)"subhdr";
	public final static Object CCTBL	= (Object)"tbl";
	public final static Object CCTBLFTR	= (Object)"tblftr";
	public final static Object CCMONO	= (Object)"mono";

	/* buildPath() targets. */
	public final static int PATHTYPE_WEB = 0;
	public final static int PATHTYPE_SERV = 1;

	/* System paths. */
	public static final String WEBROOT = "/gridfe";			/* path to gridfe root */
	public static final String SERVROOT = "/gridfe/gridfe";	/* path to servlet root */
//		this.sysroot = "/var/www/gridfe/WEB-INF/classes/gridfe";

	Page(HttpServletRequest req, HttpServletResponse res) {
		this.req = req;
		this.res = res;
		this.jasp = new JASP(req, res);
		this.classCount = 1;
		this.uid = -1;
		this.dohdr = true;
		this.dbh = null;
	}

	public void login() throws Exception {
		/* XXX: load oof prefs from config/resource. */
		this.oof = new OOF(this.jasp, "xhtml");

		String dsn = "jdbc:" + DB_DRIVER + "://" + DB_HOST + "/" + DB_NAME;
		String dbclass = "com." + DB_DRIVER + ".jdbc.Driver";
		DriverManager.registerDriver((Driver)Class.forName(dbclass).newInstance());
		this.dbh = DriverManager.getConnection(dsn, DB_USER, DB_PASS);

		/* XXX: check for errors here? */
		this.uid = req.getIntHeader("X-Fum-UID");

		if (this.restoreGI()) {
			if (this.gi.getGSS().getRemainingLifetime() == 0)
				this.gi.auth();
		} else {
			this.gi = new GridInt(this.uid);
			this.gi.auth();

			this.storeCert();
		}
	}

	private void storeCert() throws Exception {
		OutputStream os = new ByteArrayOutputStream();
		this.gi.getGSS().getGlobusCredential().save(os);
		String scert = os.toString();

		PreparedStatement sth = this.dbh.prepareStatement(
			"	DELETE FROM					" +
			"		x509_certs				" +
			"	WHERE						" +
			"		uid = ?					");
		sth.setInt(1, this.uid);
		sth.executeUpdate();

		sth = this.dbh.prepareStatement(
			"	INSERT INTO x509_certs (	" +
			"		uid, cert				" +
			"	) VALUES (					" +
			"		?, ?					" +
			"	)							");
		sth.setInt(1, this.uid);
		sth.setString(2, scert);
		sth.executeUpdate();
	}

	public int getUID() {
		return (this.uid);
	}

	private String getGIPath() {
		return ("/tmp/gridfe.gi_" + this.uid);
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

	public String imageName(String name) {
		String t = "";

		for (int i = 0; i < name.length(); i++)
			if (name.charAt(i) == ' ')
				t += '-';
			else
				t += Character.toLowerCase(name.charAt(i));
		return (t);
	}

	public void sentHeader() {
		this.dohdr = false;
	}

	public String header(String title) {
		String s, name, url;
		String wr = WEBROOT;
		LinkedList menus = new LinkedList();
		Menu m;

		if (!this.dohdr)
			return ("");

		/* Register menus. */
		menus.add(new Menu("Main", "/", null));
		menus.add(new Menu("System News",
		    "http://www.psc.edu/general/posts/posts.html", null));
		menus.add(new Menu("Jobs", "/jobs", new Object[] {
				"Submit", "/jobs/submit",
				"Status", "/jobs/status"
			}));
		menus.add(new Menu("Certificate Management", "/certs", null));
/*
		menus.add("MDS/LDAP", "/ldap", null);
		menus.add("GRIS/GIIS", "/gris", null);
		menus.add("Replica Locator", "/rls",
			new Object[] {
				"Add Catalog",		"/rls/addcat",
				"Remove Catalog",	"/rls/rmcat",
				"Search Catalogs",	"/rls/search",
				"Add Resource",		"/rls/addres"
			});
*/
		menus.add(new Menu("GridFTP", "/gridftp",
			new Object[] {
				"URL Copy",		"/gridftp/copy",
				"Browser",		"/gridftp/browser"
			}));
		menus.add(new Menu("Node Availability", "/nodes", null));

		/* Start page output. */
		s = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\">"
		  + "<html lang=\"en-US\" xml:lang=\"en-US\" xmlns=\"http://www.w3.org/1999/xhtml\">"
		  + 	"<head>"
		  +	 		"<title>GridFE - " + this.escapeHTML(title) + "</title>"
		  +			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />"
		  +			"<meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />"
		  +			"<link rel=\"stylesheet\" type=\"text/css\" "
		  +				"href=\"" + wr + "/lib/main.css\" media=\"screen\" />"
		  +			"<script type=\"text/javascript\" src=\"" + wr + "/lib/Global.js\"></script>"
		  +			this.addScript(
		   				"include('" + wr + "/lib/Browser.js');" +
		   				"include('" + wr + "/lib/util.js');" +
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

		for (Iterator i = menus.iterator();
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
		   +						"<img src=\"" + wr + "/img/cog-toolkit.png\" alt=\"[cog]\" border=\"0\" />"
		   +					"</a>"
		   +					"<a href=\"http://www.globus.org/toolkit/\">"
		   +						"<img src=\"" + wr + "/img/globus-toolkit.png\" alt=\"[globus]\" border=\"0\" />"
		   +					"</a>"
		   +					"<br /><br />"
		   +				"</td>"
		   +				"<td style=\"background-color: #eeeeff; padding-left: 5px; "
		   +				  "border-left: 1px solid black\" valign=\"top\">"
		   +					"<h3 style=\"margin-top: 0px\">"
		   +						"<img align=\"absmiddle\" src=\"" + wr + "/img/box.png\" "
		   +						  "alt=\"\" border=\"0\" />" + this.escapeHTML(title)
		   +					"</h3>";
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
		return (SERVROOT);
	}

	public String getWebRoot() {
		return (WEBROOT);
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
			return (SERVROOT + s);
		}
	}

	public String buildURL(int type, String s) {
		if (s.indexOf(':') != -1)
			return (s);
		switch (type) {
		case PATHTYPE_WEB:
			return (WEBROOT + s);
		case PATHTYPE_SERV:
			return (SERVROOT + s);
		default:
			/* throw new InvalidPathTypeException(); */
			return (null);
		}
	}

	public String escapeHTML(String s) {
		return (this.jasp.escapeHTML(s));
	}

	public String escapeURL(String s) {
		String t = "";
		char ch;
		for (int i = 0; i < s.length(); i++) {
			ch = s.charAt(i);
			if (ch == ' ')
				t += '+';
			else if (Character.isLetterOrDigit(ch) ||
			  ch == '.' || ch == '/')
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
