/* $Id$ */

package gridfe;

import gridfe.gridint.*;
import jasp.*;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import oof.*;

class Menu
{
	private String name;
	private String url;
	private LinkedList items;

	public Menu(String name, String url, Object[] items)
	{
		this.name = name;
		this.url = url;
		this.items = new LinkedList();
		if (items != null)
			for (int i = 0; i < items.length; i++)
				this.items.add(items[i]);
	}

	public String getName()
	{
		return this.name;
	}

	public String getURL()
	{
		return this.url;
	}

	public LinkedList getItems()
	{
		return this.items;
	}
}

public class Page
{
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

	/* CSS class desc */
	public final static Object CCDESC = (Object)"desc";

	Page(HttpServletRequest req, HttpServletResponse res)
	{
		this.req = req;
		this.res = res;
		this.jasp = new JASP();
		this.menus = new LinkedList();
		this.webroot = "/gridfe";
		this.sysroot = "/var/www/gridfe/WEB-INF/classes/gridfe";
		this.servroot = "/gridfe/gridfe";
		this.classCount = 1;

		try {
			// this.gi = new GridInt(0/* XXX: get kerb uid */);
			/* XXX: load oof prefs from resource. */
			this.oof = new OOF(this.jasp, "xhtml");
		} catch (Exception e) {
			this.error(e.toString());
		}
	}

	private void addMenu(String name, String url, Object[] items)
	{
		this.menus.add(new Menu(name, url, items));
	}

	private LinkedList getMenus()
	{
		return this.menus;
	}

	private String addScript(String code)
	{
		return	  "<script type=\"text/javascript\">"
			+	"<!--\n" /* Mozilla requires a newline here. */
					/* XXX: quote/JS escape */
			+		code
			+	"// -->"
			+ "</script>";
	}

	public String divName(String name)
	{
		String t = "";

		for (int i = 0; i < name.length(); i++)
			if (name.charAt(i) != ' ')
				t += name.charAt(i);
		return (t);
	}

	public String imageName(String name)
	{
		String t = "";

		for (int i = 0; i < name.length(); i++)
			if (name.charAt(i) == ' ')
				t += '-';
			else
				t += Character.toLowerCase(name.charAt(i));
		return (t);
	}

	public String buildMenu()
	{
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
		return t;
	}

	public String header(String title)
	{
		this.addMenu("Main", "/", null);
		this.addMenu("Jobs", "/jobs",
			new Object[] {
				"Submit", "/jobs/submit",
				"Status", "/jobs/status",
				"Output", "/jobs/output"
			});
		this.addMenu("Certificate Management", "/certs", null);
		this.addMenu("Replica Locator", "/rls",
			new Object[] {
				"Add Catalogue",		"/rls/addcat",
				"Remove Catalogue",		"/rls/rmcat",
				"Search Catalogues",	"/rls/search",
				"Add Resource",			"/rls/addres"
			});
		this.addMenu("Node Availability", "/nodes", null);

		String r = this.webroot;

		String s;
		s = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\">"
		  + "<html lang=\"en-US\" xml:lang=\"en-US\" xmlns=\"http://www.w3.org/1999/xhtml\">"
		  + 	"<head>"
		  +	 		"<title>" + this.jasp.escapeHTML(title) + "</title>"
		  +			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />"
		  +			"<link rel=\"stylesheet\" type=\"text/css\" href=\"/lib/main.css\" media=\"screen\">"
		  +			"<script type=\"text/javascript\" src=\"" + r + "/lib/Global.js\"></script>"
		  +			this.addScript(
		   				"include('" + r + "/lib/Browser.js');" +
		   				"include('" + r + "/lib/util.js');")
		  +			this.addScript(this.buildMenu())
		  +			this.addScript(
		   				// This must be loaded last.
		   				"include('" + r + "/lib/main.js');")
		  +		"</head>"
		  +		"<body>"
		  +			"<div class=\"bg\" style=\"width: 826px;\">"
		  +				"<div class=\"bg\" style=\"width: 200px; float: left; text-align:center;\">"
		  +					"<br />"
		   					// PSC logo
		  +					"<div style=\"position: relative; top:0px; left:0px; z-index:100\">"
		  +						"<a href=\"http://www.psc.edu/\">"
		  +							"<img src=\"" + r + "/img/psc.png\" "
		  +							     "alt=\"[Pittsburgh Supercomputing Center]\" "
		  +							     "border=\"0\" />"
		  +						"</a>"
		  +						"<br /><br />"
		  +					"</div>";

		// Menu
		Menu m;
		String name, url;
		for (Iterator i = this.getMenus().iterator();
		     i.hasNext() && (m = (Menu)i.next()) != null; ) {
			s +=			"<div style=\"position: relative; top:-80px; left:0px; "
											/* Netscape 4 will not like this. */
			   +			     "z-index:10; visibility: hidden; \" "
			   +			     "id=\"" + divName(m.getName()) + "\">"
			   +				"<a href=\"" + r + m.getURL() + "\">"
			   +					"<img src=\"" + r + "/img/buttons/"
			   +						imageName(m.getName()) + ".png\" "
			   +					     "alt=\"" + m.getName() + "\" border=\"0\" />"
			   +				"</a>"
			   +			"</div>";
			if (m.getItems() != null) {
				// Sub-menu
				for (Iterator j = m.getItems().iterator();
				     j.hasNext() && (name = (String)j.next()) != null &&
				     j.hasNext() && (url  = (String)j.next()) != null; ) {
					s +=	"<div style=\"position: relative; top:0px; left:0px; z-index:5; "
					   +	     "display:none\" id=\"" + divName(m.getName() + name) + "\">"
					   +		"<a href=\"" + r + url + "\">"
					   +			"<img src=\"" + r + "/img/buttons/"
					   +				imageName(name) + ".png\" "
					   +			     "alt=\"" + name + "\" border=\"0\" />"
					   +		"</a>"
					   +	"</div>";
				}
			}
		}

							// Sponsors
		s +=				"<br />"
		   +				"<a href=\"http://www-unix.globus.org/cog/\">"
		   +					"<img src=\"" + r + "/img/cog-toolkit.png\" border=\"0\" />"
		   +				"</a>"
		   +				"<a href=\"\">"
		   +					"<img src=\"" + r+ "/img/globus-toolkit.png\" border=\"0\" />"
		   +				"</a>"
		   +				"<br /><br />"
		   +			"</div>"
		   +			"<div style=\"width: 700px; margin-left: 413px;\">"
		   +				"<img src=\"" + r + "/img/gridfe.png\" alt=\"[GridFE]\" />"
		   +			"</div>"
		   +			"<div style=\"background-color: #ffffff; width: 626px; margin-left: 200px;\">";
		return s;
	}

	public String footer()
	{
		String s = "";

		s +=			"</div>"
		   +		"</div>"
		   +	"</body>"
		   + "</html>";

		return s;
	}

	public void error(String error)
	{
		try {
			PrintWriter w;
			w = this.res.getWriter();
			w.print("Error: " + error);
//			System.exit(1);
		} catch (Exception e) {
		}
	}

	public void error(Exception e)
	{
		this.error(e.toString() + ": " + e.getMessage());
	}

	public OOF getOOF()
	{
		return this.oof;
	}

	public JASP getJASP()
	{
		return this.jasp;
	}

	public String genClass()
	{
		return this.classCount++ % 2 == 0 ? "data1" : "data2";
	}

	public GridInt getGridInt()
	{
		return this.gi;
	}
};

/* vim: set ts=4: */
