/* $Id$ */
import jasp.*;
import oof.*;

public class Page {
	private OOF  oof;
	private JASP jasp;

	Page() {
		/* XXX: load jasp prefs from resource. */
		this.jasp = new JASP();

		try {
			this.oof = new OOF(this.jasp, "xhtml");
		} catch (Exception e) {
			this.error(e.toString());
		}
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
};
