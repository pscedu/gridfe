/* $Id$ */
package jasp;

import java.util.*;

public class HTMLElement {
	private JASP jasp;
	private String name;
	private String value;
	private List attrs;
	private List styles;

	public HTMLElement(JASP jasp, String name, String value) {
		this.jasp  = jasp;
		this.name  = name;
		this.value = value;
	}

	public HTMLElement(JASP jasp, String name) {
		this.jasp = jasp;
		this.name = name;
	}

	public String build() {
		String s;
		Attribute attr;
		Iterator i;

		s = "<" + this.name;

		if (!this.styles.isEmpty()) {
			String t = "";
			for (i = this.styles.iterator(); i.hasNext(); ) {
				attr = (Attribute)i.next();
				t += attr.name + ":" + attr.value + ";";
			}
			this.addAttribute("style", t);
		}

		for (i = this.attrs.iterator(); i.hasNext(); ) {
			attr = (Attribute)i.next();
			s += " " + attr.name + "=\""
			  + this.jasp.escapeHTML(attr.value) + "\"";
		}

		if (this.value == null)
			this.value = "";
		if (this.value == "" || this.name == "div")
			s += ">" + this.value + "</" + this.name+ ">";
		else
			s += " />";

		return s;
	}

	public void addAttribute(String name, String value) {
		this.attrs.add(new Attribute(name, value));
	}

	public void addStyle(String name, String value) {
		this.styles.add(new Attribute(name, value));
	}

	public void setValue(String s) {
		this.value = s;
	}

	public void appendValue(String s) {
		this.value += s;
	}
}

class Attribute {
	public String name;
	public String value;

	public Attribute(String name, String value) {
		this.name  = name;
		this.value = value;
	}
}
