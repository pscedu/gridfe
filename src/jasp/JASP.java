/* $Id$ */

package jasp;

import javax.servlet.http.*;

public class JASP {
	private entity[] entmap = new entity[] {
		new entity('"', "quot"),
		new entity('\'', "apos"),
		new entity('&', "amp"),
		new entity('<', "lt"),
		new entity('>', "gt"),
	};
	private String validHTML =
		"abcdefghijklmnopqrstuvwxyz" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
		"0123456789" +
		" \n\t\r" +
		",./?;:[]{}\\|`~!@#$%^*()_+-=";
	private HttpServletRequest req;
	private HttpServletResponse res;

	public JASP(HttpServletRequest req, HttpServletResponse res) {
		this.req = req;
		this.res = res;
	}

	public HttpServletRequest getRequest() {
		return (this.req);
	}

	public String escapeHTML(String s) {
		int i, j;
		String t = "";
		for (i = 0; i < s.length(); i++) {
			/* Entity names. */
			for (j = 0; j < entmap.length; j++)
				if (entmap[j].getRaw() == s.charAt(i)) {
					t += entmap[j].getEsc();
					break;
				}
			if (j < entmap.length)
				continue;

			/*
			 * Characters that do not need to be
			 * represented by entities.
			 */
			for (j = 0; j < validHTML.length(); j++)
				if (validHTML.charAt(j) == s.charAt(i)) {
					t += s.charAt(i);
					break;
				}
			if (j < validHTML.length())
				continue;

			/*
			 * Character not allowed; use ASCII
			 * value.
			 */
			t += "&#" + ((int)s.charAt(i)) + ";";
		}
		return (t);
	}

	public String unescapeHTML(String s) {
		String t = new String(s);
		return t;
	}

	public String escapeAttachName(String s) {
		int idx = s.lastIndexOf('/');
		if (idx != -1) {
			if (s.length() <= idx)
				s = "";
			else
				s = s.substring(idx);
		}
		String t = "";
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (Character.isLetterOrDigit(ch) ||
			    ch == '.' || ch == ' ')
				t += ch;
		}
		if (t.equals(""))
			t = "noname";
		return (t);
	}
};

class entity {
	private char raw;
	private String esc;

	public entity(char raw, String esc) {
		this.raw = raw;
		this.esc = esc;
	}

	public char getRaw() {
		return this.raw;
	}

	public String getEsc() {
		return this.esc;
	}
};
