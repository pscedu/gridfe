/* $Id$ */

package gridfe;

import gridfe.gridint.*;
import jasp.*;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import oof.*;

class NavigationMenu
{
	private String name;
	private String url;
	private LinkedList items;

	public NavigationMenu(String name, String url, Object[] items)
	{
		this.name = name;
		this.url = url;
		this.items = new LinkedList();
		for (int i = 0; i < items.length; i++)
			this.items.add(items[i]);
	}

	public String getName() {
		return this.name;
	}

	public String getURL() {
		return this.url;
	}

	public LinkedList getItems()
	{
		return this.items;
	}
}

public class Page
{
	private OOF  oof;
	private JASP jasp;
	private GridInt gi;
	private int classCount;
	private HttpServletRequest req;
	private HttpServletResponse res;
	private LinkedList nav;
	private String root;

	/* CSS class desc */
	public final static Object CCDESC = (Object)"desc";

	Page(HttpServletRequest req, HttpServletResponse res)
	{
		this.classCount = 1;
		this.req = req;
		this.res = res;
		this.root = "/gridfe";
		this.jasp = new JASP();

		try {
			// this.gi = new GridInt(0/* XXX: get kerb uid */);
			/* XXX: load oof prefs from resource. */
			this.oof = new OOF(this.jasp, "xhtml");
		} catch (Exception e) {
			this.error(e.toString());
		}
	}

	private void registerNavigationMenu(String name, String url, Object[] sub)
	{
		this.nav.add(new NavigationMenu(name, url, sub));
	}

	private LinkedList getNavigationMenus()
	{
		return this.nav;
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

	public String buildMenuCode()
	{
		String name, url, p, t = "var menus = [";
		NavigationMenu m;
		Iterator i, j;
		for (i = this.getNavigationMenus().iterator();
		     i.hasNext() && (m = (NavigationMenu)i.next()) != null; ) {
			t += " [ '" + m.getName() + "', ";
			if (m.getItems() != null) {
				t += "menu" + m.getName();
				p = "";
				p += "var menu" + m.getName() + " = [";
				for (j = m.getItems().iterator();
				     j.hasNext() && (name = (String)j.next()) != null &&
				     j.hasNext() && (url  = (String)j.next()) != null; ) {
					p += "'" + m.getName() + name + "'";
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
		this.registerNavigationMenu("Main", "/", null);
		this.registerNavigationMenu("Jobs", "/jobs",
			new Object[] {
				"Submit", "/jobs/submit",
				"Status", "/jobs/status",
				"Output", "/jobs/output"
			});
		this.registerNavigationMenu("Certificate Management", "/certs", null);
		this.registerNavigationMenu("Grid FTP", "/ftp", null);
		this.registerNavigationMenu("Replica Locator", "/rls",
			new Object[] {
				"Add Catalogue",	"/rls/add-catalogue",
				"Remove Catalogue",	"/rls/remove-catalogue",
				"Search Catalogues",	"/rls/search",
				"Add Resource",		"/rls/add-resource"
			});
		this.registerNavigationMenu("Node Availibility", "/nodes", null);

		String r = this.root;

		String s = new String();
		s += "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\">"
		   + "<html lang=\"en-US\" xml:lang=\"en-US\" xmlns=\"http://www.w3.org/1999/xhtml\">"
		   + 	"<head>"
		   +	 	"<title>" + this.jasp.escapeHTML(title) + "</title>"
		   +		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />"
		   +		"<link rel=\"stylesheet\" type=\"text/css\" href=\"/lib/main.css\" media=\"screen\">"
		   +		"<script type=\"text/javascript\" src=\"" + r + "/lib/Global.js\"></script>"
		   +		this.addScript(
		   			"include('" + r + "/lib/Browser.js');" +
		   			"include('" + r + "/lib/util.js');")
		   +		this.addScript(this.buildMenuCode())
		   +		this.addScript(
		   			/* This must be loaded last. */
		   			"include('" + r + "/lib/main.js');")
		   +	"</head>"
		   +	"<body>"
		   +		"<div class=\"bg\" style=\"width: 826px;\">"
		   +			"<div class=\"bg\" style=\"width: 200px; float: left; text-align:center;\">"
		   +				"<br />"
		   				/* PSC logo */
		   +				"<div style=\"position: relative; top:0px; left:0px; z-index:100\">"
		   +					"<a href=\"http://www.psc.edu/\">"
		   +						"<img src=\"img/psc.png\" "
		   +						     "alt=\"[Pittsburgh Supercomputing Center]\" "
		   +						     "border=\"0\" />"
		   +					"</a>"
		   +					"<br /><br />"
		   +				"</div>";

		/* Menu */
		NavigationMenu m;
		String name, url;
		for (Iterator i = this.getNavigationMenus().iterator();
		     i.hasNext() && (m = (NavigationMenu)i.next()) != null; ) {
			s +=			"<div style=\"position: relative; top:-80px; left:0px; "
			   +			     "z-index:10\" id=\"" + m.getName() + "\">"
			   +				"<a href=\"" + r + m.getURL() + "\" "
			   +				   "onmouseover=\"alert('hi')\">"
			   +					"<img src=\"" + r + "/img/buttons/main.png\" "
			   +					     "alt=\"" + m.getName() + "\" border=\"0\" />"
			   +				"</a>"
			   +			"</div>";
			if (m.getItems() != null) {
				/* Sub-menu */
				for (Iterator j = m.getItems().iterator();
				     j.hasNext() && (name = (String)j.next()) != null &&
				     j.hasNext() && (url  = (String)j.next()) != null; ) {
					s +=	"<div style=\"position: relative; top:0px; left:0px; z-index:5; "
					   +	     "display:none\" id=\"" + m.getName() + name + "\">"
					   +		"<a href=\"" + r + url + "\">"
					   +			"<img src=\"img/buttons/" + name + ".png\" "
					   +			     "alt=\"" + name + "\" border=\"0\" />"
					   +		"</a>"
					   +	"</div>";
				}
			}
		}
						/* Sponsors */
		s +=				"<a href=\"http://www-unix.globus.org/cog/\">"
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
