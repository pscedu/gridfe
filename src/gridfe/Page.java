/* $Id$ */

import gridint.*;
import jasp.*;
import javax.servlet.http.*;
import oof.*;

public class Page {
	private OOF  oof;
	private JASP jasp;
	private GridInt gi;
	private int classCount;
	private HttpServletRequest req;
	private HttpServletResponse res;

	/* CSS class desc */
	final static Object CCDESC = (Object)"desc";

	Page(HttpServletRequest req, HttpServletResponse res) {
		this.jasp = new JASP();
		this.gi = new GridInt(0/* XXX: get kerb uid */);

		try {
			/* XXX: load oof prefs from resource. */
			this.oof = new OOF(this.jasp, "xhtml");
		} catch (Exception e) {
			this.error(e.toString());
		}
		
		this.classCount = 1;
		this.req = req;
		this.res = res;
	}

	public GridInt getGridInt() {
		return this.gi;
	}

	public String header(String title) {
		String s = new String();

		s = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\">"
		  + "<html lang=\"en-US\" xml:lang=\"en-US\" xmlns=\"http://www.w3.org/1999/xhtml\">"
		  + 	"<head>"
		  +	 	"<title>" + this.jasp.escapeHTML(title) + "</title>"
		  +		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />"
		  +		"<link rel=\"stylesheet\" type=\"text/css\" href=\"/lib/main.css\" media=\"screen\">"
		  + 	"</head>"
		  + 	"<body>"
		  +		"";

		return s;
	}

	public String footer() {
		String s = new String();

		s =	"</body>"
		  + "</html>";

		return s;
	}

	public void error(String error) {
		System.out.println("Error: " + error);
		System.exit(1);
	}
	
	public void error(Exception e) {
		this.error(e.toString() + ": " + e.getMessage());
	}

	public OOF getOOF() {
		return this.oof;
	}

	public JASP getJASP() {
		return this.jasp;
	}

	public String genClass() {
		return this.classCount++ % 2 == 0 ? "data1" : "data2";
	}
};
