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

	/* Core Elements. */
	public ELEMENT br() {
		return new Break(new Object[] {}, new Object[] {});
	}
	public ELEMENT br(Object[] attrs) {
		return new Break(attrs, new Object[] {});
	}

	public ELEMENT code() {
		return new Code(new Object[] {}, new Object[] {});
	}
	public ELEMENT code(Object[] os) {
		return new Code(new Object[] {}, os);
	}
	public ELEMENT code(Object o) {
		return new Code(new Object[] {}, new Object[] {o});
	}
	public ELEMENT code(Object[] attrs, Object o) {
		return new Code(attrs, new Object[] {o});
	}
	public ELEMENT code(Object[] attrs, Object[] os) {
		return new Code(attrs, os);
	}

	public ELEMENT div() {
		return new Division(new Object[] {}, new Object[] {});
	}
	public ELEMENT div(Object o) {
		return new Division(new Object[] {}, new Object[] {o});
	}
	public ELEMENT div(Object[] os) {
		return new Division(new Object[] {}, os);
	}
	public ELEMENT div(Object[] attrs, Object o) {
		return new Division(attrs, new Object[] {o});
	}
	public ELEMENT div(Object[] attrs, Object[] os) {
		return new Division(attrs, os);
	}

	public ELEMENT email() {
		return new Email(new Object[] {}, new Object[] {});
	}
	public ELEMENT email(String addr) {
		Email e = new Email(new Object[] {addr}, new Object[] {});
		e.addr = addr;
		return e;
	}
	public ELEMENT email(String title, String addr) {
		Email e = new Email(new Object[] {title}, new Object[] {});
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
		return new Emphasis(new Object[] {}, new Object[] {});
	}
	public ELEMENT emph(Object[] os) {
		return new Emphasis(new Object[] {}, os);
	}
	public ELEMENT emph(Object o) {
		return new Emphasis(new Object[] {}, new Object[] {o});
	}
	public ELEMENT emph(Object[] attrs, Object o) {
		return new Emphasis(attrs, new Object[] {o});
	}
	public ELEMENT emph(Object[] attrs, Object[] os) {
		return new Emphasis(attrs, os);
	}

	public ELEMENT fieldset() {
		return new Fieldset(new Object[] {}, new Object[] {});
	}
	public ELEMENT fieldset(Object o) {
		return new Fieldset(new Object[] {}, new Object[] {o});
	}
	public ELEMENT fieldset(Object[] os) {
		return new Fieldset(new Object[] {}, os);
	}
	public ELEMENT fieldset(Object[] attrs, Object o) {
		return new Fieldset(attrs, new Object[] {o});
	}
	public ELEMENT fieldset(Object[] attrs, Object[] os) {
		return new Fieldset(attrs, os);
	}

	public ELEMENT form() {
		return new Form(new Object[] {}, new Object[] {});
	}
	public ELEMENT form(Object[] os) {
		return new Form(new Object[] {}, os);
	}
	public ELEMENT form(Object o) {
		return new Form(new Object[] {}, new Object[] {o});
	}
	public ELEMENT form(Object[] attrs, Object o) {
		return new Form(attrs, new Object[] {o});
	}
	public ELEMENT form(Object[] attrs, Object[] os) {
		return new Form(attrs, os);
	}

	public ELEMENT header() {
		return new Header(new Object[] {}, new Object[] {});
	}
	public ELEMENT header(Object o) {
		return new Header(new Object[] {}, new Object[] {o});
	}
	public ELEMENT header(Object o, Object size) {
		return new Header(new Object[] { "size", size }, new Object[] {o});
	}
	public ELEMENT header(Object[] os) {
		return new Header(new Object[] {}, os);
	}
	public ELEMENT header(Object[] attrs, Object o) {
		return new Header(attrs, new Object[] {o});
	}
	public ELEMENT header(Object[] attrs, Object[] os) {
		return new Header(attrs, os);
	}

	public ELEMENT hr() {
		return new HorizontalRuler(new Object[] {}, new Object[] {});
	}
	public ELEMENT hr(Object[] attrs) {
		return new HorizontalRuler(attrs, new Object[] {});
	}

	public ELEMENT img() {
		return new Image(new Object[] {}, new Object[] {});
	}
	public ELEMENT img(Object[] attrs) {
		return new Image(attrs, new Object[] {});
	}

	public ELEMENT input() {
		return new Input(new Object[] {}, new Object[] {});
	}
	public ELEMENT input(Object[] attrs) {
		return new Input(attrs, new Object[] {});
	}

	public ELEMENT link() {
		return new Link(new Object[] {}, new Object[] {});
	}
	public ELEMENT link(String name) {
		return new Link(new Object[] { "name", name }, new Object[] {});
	}
	public ELEMENT link(String title, String href) {
		return new Link(new Object[] { "href", href }, new Object[] { title });
	}
/*
	public ELEMENT link(Object[] attrs) {
		return new Link();
	}
*/

	public ELEMENT list() {
		return new List(new Object[] {}, new Object[] {});
	}
	public ELEMENT list(Object type) {
		return new List(new Object[] { "type", type }, new Object[] {});
	}
	public ELEMENT list(Object[] os) {
		return new List(new Object[] {}, os);
	}
	public ELEMENT list(Object type, Object[] os) {
		return new List(new Object[] { "type", type }, os);
	}

	public ELEMENT list_item() {
		return new ListItem(new Object[] {}, new Object[] {});
	}
	public ELEMENT list_item(Object o) {
		return new ListItem(new Object[] {}, new Object[] {o});
	}
	public ELEMENT list_item(Object[] os) {
		return new ListItem(new Object[] {}, os);
	}

	public ELEMENT p() {
		return new Paragraph(new Object[] {}, new Object[] {});
	}
	public ELEMENT p(Object o) {
		return new Paragraph(new Object[] {}, new Object[] {o});
	}
	public ELEMENT p(Object[] os) {
		return new Paragraph(new Object[] {}, os);
	}
	public ELEMENT p(Object[] attrs, Object o) {
		return new Paragraph(attrs, new Object[] {o});
	}
	public ELEMENT p(Object[] attrs, Object[] os) {
		return new Paragraph(attrs, os);
	}

	public ELEMENT pre() {
		return new Preformatted(new Object[] {}, new Object[] {});
	}
	public ELEMENT pre(Object o) {
		return new Preformatted(new Object[] {}, new Object[] {o});
	}
	public ELEMENT pre(Object[] os) {
		return new Preformatted(new Object[] {}, os);
	}
	public ELEMENT pre(Object[] attrs, Object o) {
		return new Preformatted(attrs, new Object[] {o});
	}
	public ELEMENT pre(Object[] attrs, Object[] os) {
		return new Preformatted(attrs, os);
	}

	public ELEMENT span() {
		return new Span(new Object[] {}, new Object[] {});
	}
	public ELEMENT span(Object o) {
		return new Span(new Object[] {}, new Object[] {o});
	}
	public ELEMENT span(Object[] os) {
		return new Span(new Object[] {}, os);
	}
	public ELEMENT span(Object[] attrs, Object o) {
		return new Span(attrs, new Object[] {o});
	}
	public ELEMENT span(Object[] attrs, Object[] os) {
		return new Span(attrs, os);
	}

	public ELEMENT strong() {
		return new Strong(new Object[] {}, new Object[] {});
	}
	public ELEMENT strong(Object o) {
		return new Strong(new Object[] {}, new Object[] {o});
	}
	public ELEMENT strong(Object[] os) {
		return new Strong(new Object[] {}, os);
	}
	public ELEMENT strong(Object[] attrs, Object o) {
		return new Strong(attrs, new Object[] {o});
	}
	public ELEMENT strong(Object[] attrs, Object[] os) {
		return new Strong(attrs, os);
	}

	public ELEMENT table() {
		return new Table(new Object[] {}, new Object[][][] {});
	}
	public ELEMENT table(Object[] attrs, Object[] rows) {
		Object[][][] osss = new Object[rows.length][1][1];
		for (int i = 0; i < rows.length; i++)
			osss[i][0][0] = rows[i];
		return new Table(attrs, osss);
	}
	public ELEMENT table(Object[] rows) {
		Object[][][] osss = new Object[rows.length][1][1];
		for (int i = 0; i < rows.length; i++)
			osss[i][0][0] = rows[i];
		return new Table(new Object[] {}, osss);
	}
	public ELEMENT table(Object[] attrs, Object[][] oss) {
		/* This is wrong. */
		Object[][][] osss = new Object[oss.length][1][1];
		for (int i = 0; i < oss.length; i++)
			for (int j = 0; j < oss[i].length; j++)
				osss[i][j][0] = oss[i][j];
		return new Table(attrs, osss);
	}
	public ELEMENT table(Object[][] oss) {
		/* This is wrong. */
		Object[][][] osss = new Object[oss.length][1][1];
		for (int i = 0; i < oss.length; i++)
			for (int j = 0; j < oss[i].length; j++)
				osss[i][j][0] = oss[i][j];
		return new Table(new Object[] {}, osss);
	}
	public ELEMENT table(Object[][][] osss) {
		return new Table(new Object[] {}, osss);
	}
	public ELEMENT table(Object[] attrs, Object[][][] osss) {
		return new Table(attrs, osss);
	}

	public ELEMENT table_row(Object o) {
		return new TableRow(new Object[][] { { new Object[] {o} } });
	}
	public ELEMENT table_row(Object[] os) {
		Object[][] oss = new Object[os.length][1];
		for (int i = 0; i < os.length; i++)
			oss[i][0] = os[i];
		return new TableRow(oss);
	}
	public ELEMENT table_row(Object[][] oss) {
		return new TableRow(oss);
	}

	/* Piecewise Elements. */
	public ELEMENT div_start() {
		return new DivisionStart(new Object[] {});
	}
	public ELEMENT div_start(Object[] attrs) {
		return new DivisionStart(attrs);
	}

	public ELEMENT div_end() {
		return new DivisionEnd();
	}

	public ELEMENT form_start() {
		return new FormStart(new Object[] {});
	}
	public ELEMENT form_start(Object[] attrs) {
		return new FormStart(attrs);
	}

	public ELEMENT form_end() {
		return new FormEnd();
	}

	public ELEMENT list_start() {
		return new ListStart(new Object[] {});
	}
	public ELEMENT list_start(Object type) {
		return new ListStart(new Object[] { "type", type });
	}
	
	public ELEMENT list_end() {
		return new ListEnd(new Object[] {});
	}
	public ELEMENT list_end(Object type) {
		return new ListEnd(new Object[] { "type", type });
	}
	
	public ELEMENT table_start() {
		return new TableStart(new Object[] {});
	}
	public ELEMENT table_start(Object[] attrs) {
		return new TableStart(attrs);
	}
	
	public ELEMENT table_end() {
		return new TableEnd();
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
