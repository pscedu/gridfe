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
		String s = "", v = "";

		s += "<" + name;

		for (Iterator i = e.children.iterator(); i.hasNext(); )
			v += i.next().toString();

		if (!v.equals("") && !name.equals("div"))
			s += ">" + "</" + name + ">";
		else
			s += "/>";

		return s;
	}

	private String build(String name, START s) {
		return "<" + name + ">";
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
		ELEMENT l = this.oof.link("mailto:" + e.addr, (String)e.children.get(0));
		return l.toString();
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
		return this.build("h", (ELEMENT)e);
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
		return this.build("ul", (ELEMENT)e);
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
		return this.build("", (START)e);
	}

	public String build(ListEnd e) {
		return this.build("", (END)e);
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
