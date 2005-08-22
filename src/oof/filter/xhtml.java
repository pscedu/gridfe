/* $Id$ */
package oof.filter;

import jasp.*;
import java.util.Iterator;
import oof.*;
import oof.element.*;
import java.util.Map.*;

public class xhtml implements Filter {
	protected OOF oof;
	protected JASP jasp;

	public static final String DEF_TEXTAREA_ROWS = "5";
	public static final String DEF_TEXTAREA_COLS = "30";

	public xhtml(JASP jasp, OOF oof) {
		this.jasp = jasp;
		this.oof  = oof;
	}

	private String build(String name, Elementable e) {
		String t = this.build(name, (Startable)e);
		String v = e.getValue();

		if (v.equals("") &&
		    !name.equals("div") &&
		    !name.equals("a") &&
		    !name.equals("select") &&
		    !name.equals("textarea")) {
			/* Strip completed start tag. */
			t = t.substring(0, t.length() - 1) + " />";
		} else
			t += v + this.build(name, (Endable)e);

		return t;
	}

	private String build(String name, Startable s) {
		String t = "";

		t += "<" + name;
		for (Iterator i = s.getAttributes().entrySet().iterator(); i.hasNext(); ) {
			Entry ent = (Entry)i.next();
			t += " ";
			t += ((String)ent.getKey()) + "=\"";
			t += ((String)ent.getValue()) + "\"";
		}
		t += ">";

		return t;
	}

	private String build(String name, Endable e) {
		return "</" + name + ">";
	}

	public String build(Break e) {
		return this.build("br", (Elementable)e);
	}

	public String build(Code e) {
		return this.build("code", (Elementable)e);
	}

	public String build(Division e) {
		return this.build("div", (Elementable)e);
	}

	public String build(Email e) {
		/* XXX: modify clone */
		e.addAttribute("href", "mailto:" + e.addr);
		return this.build("a", (Elementable)e);
	}

	public String build(Emphasis e) {
		return this.build("em", (Elementable)e);
	}

	public String build(Fieldset e) {
		return this.build("fieldset", (Elementable)e);
	}

	public String build(Form e) {
		return this.build("form", (Elementable)e);
	}

	public String build(Header e) {
		return this.build("h" + e.size, (Elementable)e);
	}

	public String build(HorizontalRuler e) {
		return this.build("hr", (Elementable)e);
	}

	public String build(Image e) {
		/* XXX: modify a clone. */
		if (e.getAttribute("alt") == null)
			e.addAttribute("alt", "");
		return this.build("img", (Elementable)e);
	}

	public String build(Input e) {
		return this.build("input", (Elementable)e);
	}

	public String build(Textarea e) {
		/* XXX: modify a clone. */
		if (e.getAttribute("rows") == null)
			e.addAttribute("rows", DEF_TEXTAREA_ROWS);
		if (e.getAttribute("cols") == null)
			e.addAttribute("cols", DEF_TEXTAREA_ROWS);
		/* XXX: hack, this shouldn't be here at all. */
		e.removeAttribute("type");
		e.append(e.removeAttribute("value"));
		return this.build("textarea", (Elementable)e);
	}

	public String build(Select e)
	    throws Exception {
		/* XXX: modify a clone. */
		if (e.getAttribute("size") == null)
			e.addAttribute("size", "1");
		/* XXX: hack, this shouldn't be here at all. */
		e.removeAttribute("type");

		String value = (String)e.removeAttribute("value");

		Object[] opts = (Object[])e.removeAttribute("options");
		if (opts == null)
			opts = new Object[] {};
		for (int i = 0; i < opts.length; i += 2) {
			Object[] e_optopts;

			if (((String)opts[i]).equals(value))
				e_optopts = new Object[] {
					"selected", "selected",
					"name", opts[i]
				};
			else
				e_optopts = new Object[] {
					"name", opts[i]
				};
			Option e_opt = new Option(this.oof, e_optopts,
			    new Object[] { opts[i + 1] });
			e.append(this.build("option", (Elementable)e_opt));
		}
		return this.build("select", (Elementable)e);
	}

	public String build(Link e) {
		return this.build("a", (Elementable)e);
	}

	public String build(ListItem e) {
		return this.build("li", (Elementable)e);
	}

	public String build(List e) {
		String tag;
		if (e.type.equals(this.oof.LIST_OD))
			tag = "ol";
		else
			tag = "ul";
		return this.build(tag, (Elementable)e);
	}

	public String build(Paragraph e) {
		return this.build("p", (Elementable)e);
	}

	public String build(Preformatted e) {
		return this.build("pre", (Elementable)e);
	}

	public String build(Span e) {
		return this.build("span", (Elementable)e);
	}

	public String build(Strong e) {
		return this.build("strong", (Elementable)e);
	}

	public String build(Table e) {
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
		return this.build("table", (Elementable)e);
	}

	public String build(TableRow e) {
		return this.build("tr", (Elementable)e);
	}

	public String build(TableCell e) {
		return this.build("td", (Elementable)e);
	}

	public String build(DivisionStart e) {
		return this.build("div", (_Start)e);
	}

	public String build(DivisionEnd e) {
		return this.build("div", (_End)e);
	}

	public String build(ListStart e) {
		String tag;
		if (e.type.equals(this.oof.LIST_OD))
			tag = "ol";
		else
			tag = "ul";
		return this.build(tag, (_Start)e);
	}

	public String build(ListEnd e) {
		String tag;
		if (e.type.equals(this.oof.LIST_OD))
			tag = "ol";
		else
			tag = "ul";
		return this.build(tag, (Endable)e);
	}

	public String build(FormStart e) {
		return this.build("form", (Startable)e);
	}

	public String build(FormEnd e) {
		return this.build("form", (Endable)e);
	}

	public String build(TableStart e) {
		return this.build("table", (Startable)e);
	}

	public String build(TableEnd e) {
		return this.build("table", (Endable)e);
	}
};
