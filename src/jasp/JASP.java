/* $Id$ */
public class JASP {
	private entity[] entmap = {
		{ '"', "quot" },
//		{ '\'', "apos" },
		{ '&', "amp" },
		{ '<', "lt" },
		{ '>', "gt" }
	};
	private String validHTML =
		"abcdefghijklmnopqrstuvwxyz" +
		"ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
		"0123456789,./?;:[]{}\\|`~!@#$%^*()_+-=";
		
	public String escapeHTML(String s) {
		int i, j;
		String t = s.copy();
		for (i = 0; i < t.length(); t++) {
			/* Entity names. */
			for (j = 0; j < entmap.length(); j++)
				if (entmap[j].raw == s[i]) {
					t += entmap[j].esc;
					continue;
				}
			/*
			 * Characters that do not need to be
			 * represented by entities.
			 */
			for (j = 0; j < validHTML.length(); j++)
				if (validHTML[j] == s[i]) {
					t += s[i]
					continue;
				}
			/*
			 * Character not allowed; use ASCII
			 * value.
			 */
			t += "&#" + s[i].ord() + ";";
		}
	}

	public String unescapeHTML(String s) {
		String t = s.copy();
		return t;
	}
};

class entity {
	public char raw;
	public String esc;
};
