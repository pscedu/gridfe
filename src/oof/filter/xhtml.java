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

	private String build(String name, Element e)
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
			t += " ";
			t += i.next().toString() + "=\"";
			t += i.next().toString() + "\"";
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
		return this.build("br", (Element)e);
	}

	public String build(Code e)
	{
		return this.build("code", (Element)e);
	}

	public String build(Division e)
	{
		return this.build("div", (Element)e);
	}

	public String build(Email e)
	{
		/* XXX: modify clone */
		e.addAttribute("href", "mailto:" + e.addr);
		return this.build("a", (Element)e);
	}

	public String build(Emphasis e)
	{
		return this.build("em", (Element)e);
	}

	public String build(Fieldset e)
	{
		return this.build("fieldset", (Element)e);
	}

	public String build(Form e)
	{
		return this.build("form", (Element)e);
	}

	public String build(Header e)
	{
		return this.build("h" + e.size, (Element)e);
	}

	public String build(HorizontalRuler e)
	{
		return this.build("hr", (Element)e);
	}

	public String build(Image e)
	{
		return this.build("img", (Element)e);
	}

	public String build(Input e)
	{
		return this.build("input", (Element)e);
	}

	public String build(Link e)
	{
		return this.build("a", (Element)e);
	}

	public String build(ListItem e)
	{
		return this.build("li", (Element)e);
	}

	public String build(List e)
	{
		String tag;
		if (e.type.equals(this.oof.LIST_OD))
			tag = "ol";
		else
			tag = "ul";
		return this.build(tag, (Element)e);
	}

	public String build(Paragraph e)
	{
		return this.build("p", (Element)e);
	}

	public String build(Preformatted e)
	{
		return this.build("pre", (Element)e);
	}

	public String build(Span e)
	{
		return this.build("span", (Element)e);
	}

	public String build(Strong e)
	{
		return this.build("strong", (Element)e);
	}

	public String build(Table e)
	{
		/* XXX: modify clone */
		Object dcols = e.removeAttribute("cols");
		if (dcols != null) {
			Object[][] cols = (Object[][])dcols;
			String s = "";
			s += "<colgroup>";
			for (int i = 0; i < cols.length; i++) {
				s += "<col";
				for (int j = 0; j < cols[i].length; j += 2)
					/* XXX: escapeHTML */
					s += " " + cols[i][j] + "=\"" + cols[i][j + 1] + "\"";
				s += " />";
			}
			s += "</colgroup>";
			e.prepend(s);
		}
		return this.build("table", (Element)e);
	}

	public String build(TableRow e)
	{
		return this.build("tr", (Element)e);
	}

	public String build(TableCell e)
	{
		return this.build("td", (Element)e);
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
