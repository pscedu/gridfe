/* $Id$ */
public class HTMLElement {
	private String name;
	private String value;
	private Attribute[] attrs;
	private Style[] styles;

	public build() {
		String s;

		s = "<" + this.name;

		for (i = 0; i < attrs.length(); i++)
			s += " " + attrs[i].name + "=\""
			  + oof.jasp.escapeHTML(attr.value) + "\"";

		s += value ? 

		if (value) {
		} else {
		}

		s += "";
	}

	public void addAttribute(String name, String value) {
		attrs[attrs.length++] = ;
	}

	public void addStyle(String name, String value) {
		if (styles == null) {
			
		}
	}

	public void setValue(String s) {
		this.value = s;
	}

	public void appendValue(String s) {
		this.value += s;
	}
}
