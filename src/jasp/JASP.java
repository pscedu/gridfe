/* $Id$ */
package jasp;

public class JASP {
	private entity[] entmap = new entity[] {
		new entity('"', "quot"),
//		new entity('\'', "apos"),
		new entity('&', "amp"),
		new entity('<', "lt"),
		new entity('>', "gt"),
	};
	private String validHTML =
		"abcdefghijklmnopqrstuvwxyz" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
		"0123456789,./?;:[]{}\\|`~!@#$%^*()_+-=";
	
	public JASP() {
	}
		
	public String escapeHTML(String s) {
		int i, j;
		String t = new String(s);
		for (i = 0; i < t.length(); i++) {
			/* Entity names. */
			for (j = 0; j < entmap.length; j++)
				if (entmap[j].raw == s.charAt(i)) {
					t += entmap[j].esc;
					continue;
				}
			/*
			 * Characters that do not need to be
			 * represented by entities.
			 */
			for (j = 0; j < validHTML.length(); j++)
				if (validHTML.charAt(j) == s.charAt(i)) {
					t += s.charAt(i);
					continue;
				}
			/*
			 * Character not allowed; use ASCII
			 * value.
			 */
			t += "&#" + ((int)s.charAt(i)) + ";";
		}
		return t;
	}

	public String unescapeHTML(String s) {
		String t = new String(s);
		return t;
	}
};

class entity {
	public char raw;
	public String esc;

	public entity(char raw, String esc) {
		this.raw = raw;
		this.esc = esc;
	}
};
