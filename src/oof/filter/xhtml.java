/* $Id$ */
package oof.filter;

import jasp.*;
import java.util.Iterator;
import oof.*;
import oof.element.*;

public class xhtml implements Filter
{
	protected OOF oof;
	protected JASP jasp;

	public xhtml(JASP jasp, OOF oof)
	{
		this.jasp = jasp;
		this.oof  = oof;
	}

	private String build(String name, ELEMENT e)
	{
		String t = this.build(name, (Startable)e);
		String v = e.getValue();

		if (v.equals("") && !name.equals("div") && !name.equals("a")) {
			/* Strip completed start tag. */
			t = t.substring(0, t.length() - 1) + " />";
		} else
			t += v + this.build(name, (Endable)e);

		return t;
	}

	private String build(String name, Startable s)
	{
		String t = "";

		t += "<" + name;
		for (Iterator i = s.getAttributes().iterator(); i.hasNext(); ) {
			t += " " + i.next().toString() + "=\"" +
			     i.next().toString() + "\"";
		}
		t += ">";
		return t;
	}

	private String build(String name, Endable e)
	{
		return "</" + name + ">";
	}

	public String build(Break e)
	{
		return this.build("br", (ELEMENT)e);
	}

	public String build(Code e)
	{
		return this.build("code", (ELEMENT)e);
	}

	public String build(Division e)
	{
		return this.build("div", (ELEMENT)e);
	}

	public String build(Email e)
	{
		/* XXX: modify clone */
		e.addAttribute("href", "mailto:" + e.addr);
		return this.build("a", (ELEMENT)e);
	}

	public String build(Emphasis e)
	{
		return this.build("em", (ELEMENT)e);
	}

	public String build(Fieldset e)
	{
		return this.build("fieldset", (ELEMENT)e);
	}

	public String build(Form e)
	{
		return this.build("form", (ELEMENT)e);
	}

	public String build(Header e)
	{
		return this.build("h" + e.size, (ELEMENT)e);
	}

	public String build(HorizontalRuler e)
	{
		return this.build("hr", (ELEMENT)e);
	}

	public String build(Image e)
	{
		return this.build("img", (ELEMENT)e);
	}

	public String build(Input e)
	{
		return this.build("input", (ELEMENT)e);
	}

	public String build(Link e)
	{
		return this.build("a", (ELEMENT)e);
	}

	public String build(ListItem e)
	{
		return this.build("li", (ELEMENT)e);
	}

	public String build(List e)
	{
		String tag;
		if (e.type.equals(this.oof.LIST_OD))
			tag = "ol";
		else
			tag = "ul";
		return this.build(tag, (ELEMENT)e);
	}

	public String build(Paragraph e)
	{
		return this.build("p", (ELEMENT)e);
	}

	public String build(Preformatted e)
	{
		return this.build("pre", (ELEMENT)e);
	}

	public String build(Span e)
	{
		return this.build("span", (ELEMENT)e);
	}

	public String build(Strong e)
	{
		return this.build("strong", (ELEMENT)e);
	}

	public String build(Table e)
	{
		String t = "";
		/* XXX: modify clone */

		t += this.build("table", (Startable)e);

		for (int i = 0; i < e.rows.length; i++)
			t += this.build(e.rows[i]);

		t += this.build("table", (Endable)e);

		return t;
	}

	public String build(TableRow e)
	{
		return this.build("tr", (ELEMENT)e);
	}

	public String build(DivisionStart e)
	{
		return this.build("div", (START)e);
	}

	public String build(DivisionEnd e)
	{
		return this.build("div", (END)e);
	}

	public String build(ListStart e)
	{
		String tag;
		if (e.type.equals(this.oof.LIST_OD))
			tag = "ol";
		else
			tag = "ul";
		return this.build(tag, (START)e);
	}

	public String build(ListEnd e)
	{
		String tag;
		if (e.type.equals(this.oof.LIST_OD))
			tag = "ol";
		else
			tag = "ul";
		return this.build(tag, (Endable)e);
	}

	public String build(FormStart e)
	{
		return this.build("form", (Startable)e);
	}

	public String build(FormEnd e)
	{
		return this.build("form", (Endable)e);
	}

	public String build(TableStart e)
	{
		return this.build("table", (Startable)e);
	}

	public String build(TableEnd e)
	{
		return this.build("table", (Endable)e);
	}
};
