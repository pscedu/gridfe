/* $Id$ */
public class Page {
	private OOF oof;

	Page() {
		this.oof = new OOF();
	}

	public void header(String title) {
		String s = new String();

		s = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\">"
		  + "<html lang=\"en-US\" xml:lang=\"en-US\" xmlns=\"http://www.w3.org/1999/xhtml\">"
		  + 	"<head>"
		  +		 	"<title>" + this.escapeHTML(title) + "</title>"
		  +			"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />"
		  +			"<link rel=\"stylesheet\" type=\"text/css\" href=\"/lib/main.css\" media=\"screen\">"
		  + 	"</head>"
		  + 	"<body>"
		  +			"";

		return s;
	}

	public void footer() {
		String s = new String();

		s =		"</body>"
		  + "</html>";

		return s;
	}

	public String escapeHTML(String s) {
		return s;
	}
};
