/* $Id$ */
package oof.filter;

import java.util.Iterator;
import jasp.*;
import oof.*;
import oof.element.*;

public class xhtml extends FILTER {
	public xhtml(JASP jasp, OOF oof) {
		super(jasp, oof);
	}

	private String build(String name, ELEMENT e) {
		String t = "";

		t += "<" + name;
		for (Iterator i = e.attrs.iterator(); i.hasNext(); ) {
			t += " " + i.next().toString() + "=\"" +
			     i.next().toString() + "\"";
		}

		String v = "";
		for (Iterator i = e.children.iterator(); i.hasNext(); )
			v += i.next().toString();

		if (v.equals("") && !name.equals("div"))
			t += " />";
		else
			t += ">" + v + "</" + name + ">";

		return t;
	}

	private String build(String name, START s) {
		String t = "";

		t += "<" + name;
		for (Iterator i = s.attrs.iterator(); i.hasNext(); ) {
			t += " " + i.next().toString() + "=\"" +
			     i.next().toString() + "\"";
		}
		t += ">";
		return t;
	}

	private String build(String name, END e) {
		return "</" + name + ">";
	}

	public String build(Break e) {
		return this.build("br", (ELEMENT)e);
	}

	public String build(Code e) {
		return this.build("code", (ELEMENT)e);
	}

	public String build(Division e) {
		return this.build("div", (ELEMENT)e);
	}

	public String build(Email e) {
		e.addAttribute("href", "mailto:" + e.addr);
		return this.build("a", (ELEMENT)e);
	}

	public String build(Emphasis e) {
		return this.build("em", (ELEMENT)e);
	}

	public String build(Fieldset e) {
		return this.build("fieldset", (ELEMENT)e);
	}

	public String build(Form e) {
		return this.build("form", (ELEMENT)e);
	}

	public String build(Header e) {
		return this.build("h" + e.size, (ELEMENT)e);
	}

	public String build(HorizontalRuler e) {
		return this.build("hr", (ELEMENT)e);
	}

	public String build(Image e) {
		return this.build("img", (ELEMENT)e);
	}

	public String build(Input e) {
		return this.build("input", (ELEMENT)e);
	}

	public String build(Link e) {
		return this.build("a", (ELEMENT)e);
	}

	public String build(ListItem e) {
		return this.build("li", (ELEMENT)e);
	}

	public String build(List e) {
		String tag;
		if (e.type.equals(this.oof.LIST_OD))
			tag = "ol";
		else
			tag = "ul";
		return this.build(tag, (ELEMENT)e);
	}

	public String build(Paragraph e) {
		return this.build("p", (ELEMENT)e);
	}

	public String build(Preformatted e) {
		return this.build("pre", (ELEMENT)e);
	}

	public String build(Span e) {
		return this.build("span", (ELEMENT)e);
	}

	public String build(Strong e) {
		return this.build("strong", (ELEMENT)e);
	}

	public String build(Table e) {
		return this.build("table", (ELEMENT)e);
	}

	public String build(TableRow e) {
		return this.build("tr", (ELEMENT)e);
	}

	public String build(DivisionStart e) {
		return this.build("div", (START)e);
	}

	public String build(DivisionEnd e) {
		return this.build("div", (END)e);
	}

	public String build(ListStart e) {
		String tag;
		if (e.type.equals(this.oof.LIST_OD))
			tag = "ol";
		else
			tag = "ul";
		return this.build(tag, (START)e);
	}

	public String build(ListEnd e) {
		String tag;
		if (e.type.equals(this.oof.LIST_OD))
			tag = "ol";
		else
			tag = "ul";
		return this.build(tag, (END)e);
	}

	public String build(FormStart e) {
		return this.build("form", (START)e);
	}

	public String build(FormEnd e) {
		return this.build("form", (END)e);
	}

	public String build(TableStart e) {
		return this.build("table", (START)e);
	}

	public String build(TableEnd e) {
		return this.build("table", (END)e);
	}
}
