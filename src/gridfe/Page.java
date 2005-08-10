/* $Id$ */

package gridfe;

import gridfe.gridint.*;
import jasp.*;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import oof.*;
import org.bouncycastle.util.encoders.*;

class Menu {
	private String name;
	private String url;
	private LinkedList items;

	public Menu(String name, String url, Object[] items) {
		this.name = name;
		this.url = url;
		this.items = new LinkedList();
		if (items != null)
			for (int i = 0; i < items.length; i++)
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

public class Page {
	private HttpServletRequest req;
	private HttpServletResponse res;
	private LinkedList menus;
	private String servroot;
	private String webroot;
	private String sysroot;
	private int classCount;
	private GridInt gi;
	private JASP jasp;
	private OOF oof;

	/* CSS class desc. */
	public final static Object CCDESC = (Object)"desc";
	public final static Object CCHDR = (Object)"hdr";
	public final static Object CCSUBHDR = (Object)"subhdr";
	public final static Object CCTBL = (Object)"tbl";
	public final static Object CCTBLFTR = (Object)"tblftr";
	public final static int MENU_ITEM_HEIGHT = 35;

	Page(HttpServletRequest req, HttpServletResponse res) {
		this.req = req;
		this.res = res;
		this.jasp = new JASP();
		this.menus = new LinkedList();
		this.webroot = "/gridfe";
		this.sysroot = "/var/www/gridfe/WEB-INF/classes/gridfe";
		this.servroot = "/gridfe/gridfe";
		this.classCount = 1;

		try {
			Base64 enc = new Base64();
			String hdr;

			/*
			 * Reparse authorization because getRemoteUser() doesn't
			 * work.
			 */
			hdr = (String)req.getHeader("authorization");
			if (hdr.startsWith("Basic "))
				hdr = hdr.substring(6);
			String combo = new String(enc.decode(hdr));
			String[] auth = BasicServices.splitString(combo, ":");
			String kuid = auth[0];
			UserMap m = new UserMap();
			String uid = m.kerberosToSystem(kuid);
			this.gi = new GridInt(BasicServices.getUserID(uid));
			this.gi.auth();
			/* XXX: load oof prefs from config/resource. */
			this.oof = new OOF(this.jasp, "xhtml");
		} catch (Exception e) {
			this.error(e.getClass().getName() + ": " + e.toString());
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
		int y;

		/* Register menu. */
		this.addMenu("Main", "/", null);
		this.addMenu("System News", "http://www.psc.edu/general/posts/posts.html", null);
		this.addMenu("Jobs", "/jobs",
			new Object[] {
				"Submit", "/jobs/submit",
				"Status", "/jobs/status",
				"Output", "/jobs/output"
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
		  +			"<div class=\"bg\" style=\"width: 826px;\">"
		  +				"<div class=\"bg\" style=\"width: 200px; float: left; text-align:center;\">"
		  +					"<br />"
		   					/* PSC logo. */
		  +					"<div style=\"position: relative; top:0px; left:0px; z-index:100\">"
		  +						"<a href=\"http://www.psc.edu/\">"
		  +							"<img src=\"" + wr + "/img/psc.png\" "
		  +							     "alt=\"[Pittsburgh Supercomputing Center]\" "
		  +							     "border=\"0\" />"
		  +						"</a>"
		  +						"<br /><br />"
		  +					"</div>";

		y = -1 * MENU_ITEM_HEIGHT * this.getMenus().size();
		for (Iterator i = this.getMenus().iterator();
		     i.hasNext() && (m = (Menu)i.next()) != null; ) {
			s +=			"<div style=\"position: relative; top:" + y + "px; left:0px; "
											/* Netscape 4 may not like this. */
			   +			     "z-index:10; visibility: hidden; \" "
			   +			     "id=\"" + divName(m.getName()) + "\">"
			   +				"<a href=\"" + this.buildURL(m.getURL()) + "\" "
			   +				   "onmouseover=\"menuShow(this.parentNode)\" "
			   +				   "onmouseout=\"menuHide(this.parentNode)\">"
			   +					"<img src=\"" + wr + "/img/buttons/"
			   +						imageName(m.getName()) + ".png\" "
			   +					     "alt=\"" + m.getName() + "\" border=\"0\" />"
			   +				"</a>"
			   +			"</div>";
			if (m.getItems() != null) {
				/* Sub-menu */
				for (Iterator j = m.getItems().iterator();
				     j.hasNext() && (name = (String)j.next()) != null &&
				     j.hasNext() && (url  = (String)j.next()) != null; ) {
					s +=		"<div style=\"position: relative; top:0px; left:0px; z-index:5; "
					   +		     "display:none\" id=\"" + divName(m.getName() + name) + "\">"
					   +			"<a href=\"" + this.buildURL(url) + "\" "
					   +			   "onmouseover=\"menuShow(objGet('" + divName(m.getName()) + "'))\" "
					   +			   "onmouseout=\"menuHide(objGet('" + divName(m.getName()) + "'))\">"
					   +				"<img src=\"" + wr + "/img/buttons/"
					   +					imageName(name) + ".png\" "
					   +				     "alt=\"" + name + "\" border=\"0\" />"
					   +			"</a>"
					   +		"</div>";
				}
			}
		}

							/* Sponsors */
		s +=				"<br />"
		   +				"<a href=\"http://www-unix.globus.org/cog/\">"
		   +					"<img src=\"" + wr + "/img/cog-toolkit.png\" border=\"0\" />"
		   +				"</a>"
		   +				"<a href=\"\">"
		   +					"<img src=\"" + wr+ "/img/globus-toolkit.png\" border=\"0\" />"
		   +				"</a>"
		   +				"<br /><br />"
		   +			"</div>"
		   +			"<div style=\"width: 700px; margin-left: 413px\">"
		   +				"<img src=\"" + wr + "/img/gridfe.png\" alt=\"[GridFE]\" />"
		   +			"</div>"
		   +			"<div style=\"background-color: #ffffff; width: 626px; margin-left: 200px; "
		   +			  "text-align: center; padding-left: 113px; padding-top:1px;\">"
		   +				"<img src=\"" + wr + "/img/propaganda.png\" alt=\"\" />"
		   +			"</div>"
		   +			"<div style=\"background-color: #ffffff; width: 626px; margin-left: 200px; padding-left: 3px;\">"
		   +				this.oof.header(new Object[] {
								"size", "3",
								"style", "margin-top:0px"
							}, title);
		return (s);
	}

	public String footer() {
		String s = "";

		s +=			"</div>"
		   +		"</div>"
		   +		"<div style=\"clear: both; width: 826px; text-align: right\">"
		   +			"Copyright &copy; 2004-2005 "
		   +			"<a href=\"http://www.psc.edu/\">Pittsburgh Supercomputing Center</a>"
		   +		"</div>"
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

	public String buildURL(String s) {
		if (s.indexOf(':') != -1) {
			return (s);
		} else {
			return (this.servroot + s);
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
				if (32 <= ch && ch <= 126)
					t += ch;
				else
					t += "&#" + new Integer(ch) + ";";
				break;
			}
		}
		return (t);
	}
};

/* vim: set ts=4: */
