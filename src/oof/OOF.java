/* $Id$ */
package oof;

import java.lang.reflect.*;
import jasp.*;
import oof.filter.*;
import oof.element.*;

public class OOF {
	private FILTER filter;
	private JASP jasp;

	public static final Object LIST_UN = new Integer(1);
	public static final Object LIST_OD = new Integer(2);

	public OOF(JASP j, String filter) throws ClassNotFoundException,
						 NoSuchMethodException,
						 InstantiationException,
						 IllegalAccessException,
						 InvocationTargetException {
		this.jasp = j;
		this.filter = (FILTER)Class.forName("oof.filter." + filter).
				getConstructor(new Class[] { JASP.class, OOF.class }).
				newInstance(new Object[] { this.jasp, this });
	}

	public FILTER __getFilter() {
		return this.filter;
	}

	/* Core Elements. */
	public ELEMENT br() {
		return new Break(this, new Object[] {});
	}
	public ELEMENT br(Object[] attrs) {
		return new Break(this, attrs);
	}

	public ELEMENT code() {
		return new Code(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT code(Object[] os) {
		return new Code(this, new Object[] {}, os);
	}
	public ELEMENT code(Object o) {
		return new Code(this, new Object[] {}, new Object[] {o});
	}
	public ELEMENT code(Object[] attrs, Object o) {
		return new Code(this, attrs, new Object[] {o});
	}
	public ELEMENT code(Object[] attrs, Object[] os) {
		return new Code(this, attrs, os);
	}

	public ELEMENT div() {
		return new Division(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT div(Object o) {
		return new Division(this, new Object[] {}, new Object[] {o});
	}
	public ELEMENT div(Object[] os) {
		return new Division(this, new Object[] {}, os);
	}
	public ELEMENT div(Object[] attrs, Object o) {
		return new Division(this, attrs, new Object[] {o});
	}
	public ELEMENT div(Object[] attrs, Object[] os) {
		return new Division(this, attrs, os);
	}

	public ELEMENT email() {
		return new Email(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT email(String addr) {
		Email e = new Email(this, new Object[] {}, new Object[] {addr});
		e.addr = addr;
		return e;
	}
	public ELEMENT email(String title, String addr) {
		Email e = new Email(this, new Object[] {}, new Object[] {title});
		e.addr = addr;
		return e;
	}
/*
	public ELEMENT email(Object[] attrs) {
		return new Email();
	}
	public ELEMENT email(obj[]...) {
		return new Email();
	}
*/

	public ELEMENT emph() {
		return new Emphasis(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT emph(Object[] os) {
		return new Emphasis(this, new Object[] {}, os);
	}
	public ELEMENT emph(Object o) {
		return new Emphasis(this, new Object[] {}, new Object[] {o});
	}
	public ELEMENT emph(Object[] attrs, Object o) {
		return new Emphasis(this, attrs, new Object[] {o});
	}
	public ELEMENT emph(Object[] attrs, Object[] os) {
		return new Emphasis(this, attrs, os);
	}

	public ELEMENT fieldset() {
		return new Fieldset(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT fieldset(Object o) {
		return new Fieldset(this, new Object[] {}, new Object[] {o});
	}
	public ELEMENT fieldset(Object[] os) {
		return new Fieldset(this, new Object[] {}, os);
	}
	public ELEMENT fieldset(Object[] attrs, Object o) {
		return new Fieldset(this, attrs, new Object[] {o});
	}
	public ELEMENT fieldset(Object[] attrs, Object[] os) {
		return new Fieldset(this, attrs, os);
	}

	public ELEMENT form() {
		return new Form(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT form(Object[] os) {
		return new Form(this, new Object[] {}, os);
	}
	public ELEMENT form(Object o) {
		return new Form(this, new Object[] {}, new Object[] {o});
	}
	public ELEMENT form(Object[] attrs, Object o) {
		return new Form(this, attrs, new Object[] {o});
	}
	public ELEMENT form(Object[] attrs, Object[] os) {
		return new Form(this, attrs, os);
	}

	public ELEMENT header() {
		return new Header(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT header(Object o) {
		return new Header(this, new Object[] {}, new Object[] {o});
	}
	public ELEMENT header(Object o, Object size) {
		return new Header(this, new Object[] { "size", size }, new Object[] {o});
	}
	public ELEMENT header(Object[] os) {
		return new Header(this, new Object[] {}, os);
	}
	public ELEMENT header(Object[] attrs, Object o) {
		return new Header(this, attrs, new Object[] {o});
	}
	public ELEMENT header(Object[] attrs, Object[] os) {
		return new Header(this, attrs, os);
	}

	public ELEMENT hr() {
		return new HorizontalRuler(this, new Object[] {});
	}
	public ELEMENT hr(Object[] attrs) {
		return new HorizontalRuler(this, attrs);
	}

	public ELEMENT img() {
		return new Image(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT img(Object[] attrs) {
		return new Image(this, attrs, new Object[] {});
	}

	public ELEMENT input() {
		return new Input(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT input(Object[] attrs) {
		return new Input(this, attrs, new Object[] {});
	}

	public ELEMENT link() {
		return new Link(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT link(String name) {
		return new Link(this, new Object[] { "name", name }, new Object[] {});
	}
	public ELEMENT link(String title, String href) {
		return new Link(this, new Object[] { "href", href }, new Object[] { title });
	}
/*
	public ELEMENT link(Object[] attrs) {
		return new Link();
	}
*/

	public ELEMENT list() {
		return new List(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT list(Object type) {
		return new List(this, new Object[] { "type", type }, new Object[] {});
	}
	public ELEMENT list(Object[] os) {
		return new List(this, new Object[] {}, os);
	}
	public ELEMENT list(Object type, Object[] os) {
		return new List(this, new Object[] { "type", type }, os);
	}

	public ELEMENT list_item() {
		return new ListItem(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT list_item(Object o) {
		return new ListItem(this, new Object[] {}, new Object[] {o});
	}
	public ELEMENT list_item(Object[] os) {
		return new ListItem(this, new Object[] {}, os);
	}

	public ELEMENT p() {
		return new Paragraph(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT p(Object o) {
		return new Paragraph(this, new Object[] {}, new Object[] {o});
	}
	public ELEMENT p(Object[] os) {
		return new Paragraph(this, new Object[] {}, os);
	}
	public ELEMENT p(Object[] attrs, Object o) {
		return new Paragraph(this, attrs, new Object[] {o});
	}
	public ELEMENT p(Object[] attrs, Object[] os) {
		return new Paragraph(this, attrs, os);
	}

	public ELEMENT pre() {
		return new Preformatted(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT pre(Object o) {
		return new Preformatted(this, new Object[] {}, new Object[] {o});
	}
	public ELEMENT pre(Object[] os) {
		return new Preformatted(this, new Object[] {}, os);
	}
	public ELEMENT pre(Object[] attrs, Object o) {
		return new Preformatted(this, attrs, new Object[] {o});
	}
	public ELEMENT pre(Object[] attrs, Object[] os) {
		return new Preformatted(this, attrs, os);
	}

	public ELEMENT span() {
		return new Span(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT span(Object o) {
		return new Span(this, new Object[] {}, new Object[] {o});
	}
	public ELEMENT span(Object[] os) {
		return new Span(this, new Object[] {}, os);
	}
	public ELEMENT span(Object[] attrs, Object o) {
		return new Span(this, attrs, new Object[] {o});
	}
	public ELEMENT span(Object[] attrs, Object[] os) {
		return new Span(this, attrs, os);
	}

	public ELEMENT strong() {
		return new Strong(this, new Object[] {}, new Object[] {});
	}
	public ELEMENT strong(Object o) {
		return new Strong(this, new Object[] {}, new Object[] {o});
	}
	public ELEMENT strong(Object[] os) {
		return new Strong(this, new Object[] {}, os);
	}
	public ELEMENT strong(Object[] attrs, Object o) {
		return new Strong(this, attrs, new Object[] {o});
	}
	public ELEMENT strong(Object[] attrs, Object[] os) {
		return new Strong(this, attrs, os);
	}

	public ELEMENT table() {
		return new Table(this, new Object[] {}, new Object[][][] {});
	}
	public ELEMENT table(Object[] attrs, Object[] rows) {
		Object[][][] osss = new Object[rows.length][1][1];
		for (int i = 0; i < rows.length; i++)
			osss[i][0][0] = rows[i];
		return new Table(this, attrs, osss);
	}
	public ELEMENT table(Object[] rows) {
		Object[][][] osss = new Object[rows.length][1][1];
		for (int i = 0; i < rows.length; i++)
			osss[i][0][0] = rows[i];
		return new Table(this, new Object[] {}, osss);
	}
	public ELEMENT table(Object[] attrs, Object[][] oss) {
		/* This is wrong. */
		Object[][][] osss = new Object[oss.length][][];
		for (int i = 0; i < oss.length; i++) {
			osss[i] = new Object[oss[i].length][1];
			for (int j = 0; j < oss[i].length; j++)
				osss[i][j][0] = oss[i][j];
		}
		return new Table(this, attrs, osss);
	}
	public ELEMENT table(Object[][] oss) {
		/* This is wrong. */
		Object[][][] osss = new Object[oss.length][1][1];
		for (int i = 0; i < oss.length; i++) {
			osss[i] = new Object[oss[i].length][1];
			for (int j = 0; j < oss[i].length; j++)
				osss[i][j][0] = oss[i][j];
		}
		return new Table(this, new Object[] {}, osss);
	}
	public ELEMENT table(Object[][][] osss) {
		return new Table(this, new Object[] {}, osss);
	}
	public ELEMENT table(Object[] attrs, Object[][][] osss) {
		return new Table(this, attrs, osss);
	}

	public ELEMENT table_row(Object o) {
		return new TableRow(this, new Object[][] { { new Object[] {o} } });
	}
	public ELEMENT table_row(Object[] os) {
		Object[][] oss = new Object[os.length][1];
		for (int i = 0; i < os.length; i++)
			oss[i][0] = os[i];
		return new TableRow(this, oss);
	}
	public ELEMENT table_row(Object[][] oss) {
		return new TableRow(this, oss);
	}

	/* Piecewise Elements. */
	public ELEMENT div_start() {
		return new DivisionStart(this, new Object[] {});
	}
	public ELEMENT div_start(Object[] attrs) {
		return new DivisionStart(this, attrs);
	}

	public ELEMENT div_end() {
		return new DivisionEnd(this);
	}

	public ELEMENT form_start() {
		return new FormStart(this, new Object[] {});
	}
	public ELEMENT form_start(Object[] attrs) {
		return new FormStart(this, attrs);
	}

	public ELEMENT form_end() {
		return new FormEnd(this);
	}

	public ELEMENT list_start() {
		return new ListStart(this, new Object[] {});
	}
	public ELEMENT list_start(Object type) {
		return new ListStart(this, new Object[] { "type", type });
	}
	
	public ELEMENT list_end() {
		return new ListEnd(this, new Object[] {});
	}
	public ELEMENT list_end(Object type) {
		return new ListEnd(this, new Object[] { "type", type });
	}
	
	public ELEMENT table_start() {
		return new TableStart(this, new Object[] {});
	}
	public ELEMENT table_start(Object[] attrs) {
		return new TableStart(this, attrs);
	}
	
	public ELEMENT table_end() {
		return new TableEnd(this);
	}

	/* Alias Elements. */
/*
	// Yeah right
	public ELEMENT em()		{ return new Emphasis(); }
	public ELEMENT image()		{ return new Image(); }
	public ELEMENT para()		{ return new Paragraph(); }
	public ELEMENT tr()		{ return new TableRow(); }
*/
};
