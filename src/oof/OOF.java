/* $Id$ */

package oof;

import jasp.*;
import java.lang.reflect.*;
import oof.element.*;
import oof.filter.*;

public class OOF {
	private Filter filter;
	private JASP jasp;

	public static final Object LIST_UN = "1";
	public static final Object LIST_OD = "2";

	public OOF(JASP j, String filter)
	    throws ClassNotFoundException, NoSuchMethodException,
		   InstantiationException, IllegalAccessException,
		   InvocationTargetException {
		this.jasp = j;
		this.filter = (Filter)Class.forName("oof.filter." + filter).
				getConstructor(new Class[] { JASP.class, OOF.class }).
				newInstance(new Object[] { this.jasp, this });
	}

	public Filter __getFilter() {
		return this.filter;
	}

	/* Core Elements. */
	public Elementable br()
	    throws OOFBadElementFormException {
		return new Break(this, new Object[] {});
	}
	public Elementable br(Object[] attrs)
	    throws OOFBadElementFormException {
		return new Break(this, attrs);
	}

	public Elementable Code()
	    throws OOFBadElementFormException {
		return new Code(this, new Object[] {}, new Object[] {});
	}
	public Elementable code(Object[] os)
	    throws OOFBadElementFormException {
		return new Code(this, new Object[] {}, os);
	}
	public Elementable code(Object o)
	    throws OOFBadElementFormException {
		return new Code(this, new Object[] {}, new Object[] {o});
	}
	public Elementable code(Object[] attrs, Object o)
	    throws OOFBadElementFormException {
		return new Code(this, attrs, new Object[] {o});
	}
	public Elementable code(Object[] attrs, Object[] os)
	    throws OOFBadElementFormException {
		return new Code(this, attrs, os);
	}

	public Elementable div()
	    throws OOFBadElementFormException {
		return new Division(this, new Object[] {}, new Object[] {});
	}
	public Elementable div(Object o)
	    throws OOFBadElementFormException {
		return new Division(this, new Object[] {}, new Object[] {o});
	}
	public Elementable div(Object[] os)
	    throws OOFBadElementFormException {
		return new Division(this, new Object[] {}, os);
	}
	public Elementable div(Object[] attrs, Object o)
	    throws OOFBadElementFormException {
		return new Division(this, attrs, new Object[] {o});
	}
	public Elementable div(Object[] attrs, Object[] os)
	    throws OOFBadElementFormException {
		return new Division(this, attrs, os);
	}

	public Elementable email()
	    throws OOFBadElementFormException {
		return new Email(this, new Object[] {}, new Object[] {});
	}
	public Elementable email(String addr)
	    throws OOFBadElementFormException {
		Email e = new Email(this, new Object[] {}, new Object[] {addr});
		e.addr = addr;
		return e;
	}
	public Elementable email(String title, String addr)
	    throws OOFBadElementFormException {
		Email e = new Email(this, new Object[] {}, new Object[] {title});
		e.addr = addr;
		return e;
	}
/*
	public Elementable email(Object[] attrs)
	    throws OOFBadElementFormException {
		return new Email();
	}
	public Elementable email(obj[]...)
	    throws OOFBadElementFormException {
		return new Email();
	}
*/

	public Elementable emph()
	    throws OOFBadElementFormException {
		return new Emphasis(this, new Object[] {}, new Object[] {});
	}
	public Elementable emph(Object[] os)
	    throws OOFBadElementFormException {
		return new Emphasis(this, new Object[] {}, os);
	}
	public Elementable emph(Object o)
	    throws OOFBadElementFormException {
		return new Emphasis(this, new Object[] {}, new Object[] {o});
	}
	public Elementable emph(Object[] attrs, Object o)
	    throws OOFBadElementFormException {
		return new Emphasis(this, attrs, new Object[] {o});
	}
	public Elementable emph(Object[] attrs, Object[] os)
	    throws OOFBadElementFormException {
		return new Emphasis(this, attrs, os);
	}

	public Elementable fieldset()
	    throws OOFBadElementFormException {
		return new Fieldset(this, new Object[] {}, new Object[] {});
	}
	public Elementable fieldset(Object o)
	    throws OOFBadElementFormException {
		return new Fieldset(this, new Object[] {}, new Object[] {o});
	}
	public Elementable fieldset(Object[] os)
	    throws OOFBadElementFormException {
		return new Fieldset(this, new Object[] {}, os);
	}
	public Elementable fieldset(Object[] attrs, Object o)
	    throws OOFBadElementFormException {
		return new Fieldset(this, attrs, new Object[] {o});
	}
	public Elementable fieldset(Object[] attrs, Object[] os)
	    throws OOFBadElementFormException {
		return new Fieldset(this, attrs, os);
	}

	public Elementable form()
	    throws OOFBadElementFormException {
		return new Form(this, new Object[] {}, new Object[] {});
	}
	public Elementable form(Object[] os)
	    throws OOFBadElementFormException {
		return new Form(this, new Object[] {}, os);
	}
	public Elementable form(Object o)
	    throws OOFBadElementFormException {
		return new Form(this, new Object[] {}, new Object[] {o});
	}
	public Elementable form(Object[] attrs, Object o)
	    throws OOFBadElementFormException {
		return new Form(this, attrs, new Object[] {o});
	}
	public Elementable form(Object[] attrs, Object[] os)
	    throws OOFBadElementFormException {
		return new Form(this, attrs, os);
	}

	public Elementable header()
	    throws OOFBadElementFormException {
		return new Header(this, new Object[] {}, new Object[] {});
	}
	public Elementable header(Object o)
	    throws OOFBadElementFormException {
		return new Header(this, new Object[] {}, new Object[] {o});
	}
	public Elementable header(Object o, Object size)
	    throws OOFBadElementFormException {
		return new Header(this, new Object[] { "size", size }, new Object[] {o});
	}
	public Elementable header(Object[] os)
	    throws OOFBadElementFormException {
		return new Header(this, new Object[] {}, os);
	}
	public Elementable header(Object[] attrs, Object o)
	    throws OOFBadElementFormException {
		return new Header(this, attrs, new Object[] {o});
	}
	public Elementable header(Object[] attrs, Object[] os)
	    throws OOFBadElementFormException {
		return new Header(this, attrs, os);
	}

	public Elementable hr()
	    throws OOFBadElementFormException {
		return new HorizontalRuler(this, new Object[] {});
	}
	public Elementable hr(Object[] attrs)
	    throws OOFBadElementFormException {
		return new HorizontalRuler(this, attrs);
	}

	public Elementable img()
	    throws OOFBadElementFormException {
		return new Image(this, new Object[] {}, new Object[] {});
	}
	public Elementable img(Object[] attrs)
	    throws OOFBadElementFormException {
		return new Image(this, attrs, new Object[] {});
	}

	public Elementable input()
	    throws OOFBadElementFormException {
		return new Input(this, new Object[] {}, new Object[] {});
	}
	public Elementable input(Object[] attrs)
	    throws OOFBadElementFormException {
		return new Input(this, attrs, new Object[] {});
	}

	public Elementable link()
	    throws OOFBadElementFormException {
		return new Link(this, new Object[] {}, new Object[] {});
	}
	public Elementable link(String name)
	    throws OOFBadElementFormException {
		return new Link(this, new Object[] { "name", name }, new Object[] {});
	}
	public Elementable link(String title, String href)
	    throws OOFBadElementFormException {
		return new Link(this, new Object[] { "href", href }, new Object[] { title });
	}
/*
	public Elementable link(Object[] attrs)
	    throws OOFBadElementFormException {
		return new Link();
	}
*/

	public Elementable list()
	    throws OOFBadElementFormException {
		return new List(this, new Object[] {}, new Object[] {});
	}
	public Elementable list(Object type)
	    throws OOFBadElementFormException {
		return new List(this, new Object[] { "type", type }, new Object[] {});
	}
	public Elementable list(Object[] os)
	    throws OOFBadElementFormException {
		Object[] items = new Object[os.length];
		for (int i = 0; i < items.length; i++)
			items[i] = new ListItem(this, new Object[] {}, new Object[] {os[i]});
		return new List(this, new Object[] {}, items);
	}
	public Elementable list(Object type, Object[] os)
	    throws OOFBadElementFormException {
		Object[] items = new Object[os.length];
		for (int i = 0; i < items.length; i++)
			items[i] = new ListItem(this, new Object[] {}, new Object[] {os[i]});
		return new List(this, new Object[] { "type", type }, items);
	}

	public Elementable list_item()
	    throws OOFBadElementFormException {
		return new ListItem(this, new Object[] {}, new Object[] {});
	}
	public Elementable list_item(Object o)
	    throws OOFBadElementFormException {
		return new ListItem(this, new Object[] {}, new Object[] {o});
	}
	public Elementable list_item(Object[] os)
	    throws OOFBadElementFormException {
		return new ListItem(this, new Object[] {}, os);
	}

	public Elementable p()
	    throws OOFBadElementFormException {
		return new Paragraph(this, new Object[] {}, new Object[] {});
	}
	public Elementable p(Object o)
	    throws OOFBadElementFormException {
		return new Paragraph(this, new Object[] {}, new Object[] {o});
	}
	public Elementable p(Object[] os)
	    throws OOFBadElementFormException {
		return new Paragraph(this, new Object[] {}, os);
	}
	public Elementable p(Object[] attrs, Object o)
	    throws OOFBadElementFormException {
		return new Paragraph(this, attrs, new Object[] {o});
	}
	public Elementable p(Object[] attrs, Object[] os)
	    throws OOFBadElementFormException {
		return new Paragraph(this, attrs, os);
	}

	public Elementable pre()
	    throws OOFBadElementFormException {
		return new Preformatted(this, new Object[] {}, new Object[] {});
	}
	public Elementable pre(Object o)
	    throws OOFBadElementFormException {
		return new Preformatted(this, new Object[] {}, new Object[] {o});
	}
	public Elementable pre(Object[] os)
	    throws OOFBadElementFormException {
		return new Preformatted(this, new Object[] {}, os);
	}
	public Elementable pre(Object[] attrs, Object o)
	    throws OOFBadElementFormException {
		return new Preformatted(this, attrs, new Object[] {o});
	}
	public Elementable pre(Object[] attrs, Object[] os)
	    throws OOFBadElementFormException {
		return new Preformatted(this, attrs, os);
	}

	public Elementable span()
	    throws OOFBadElementFormException {
		return new Span(this, new Object[] {}, new Object[] {});
	}
	public Elementable span(Object o)
	    throws OOFBadElementFormException {
		return new Span(this, new Object[] {}, new Object[] {o});
	}
	public Elementable span(Object[] os)
	    throws OOFBadElementFormException {
		return new Span(this, new Object[] {}, os);
	}
	public Elementable span(Object[] attrs, Object o)
	    throws OOFBadElementFormException {
		return new Span(this, attrs, new Object[] {o});
	}
	public Elementable span(Object[] attrs, Object[] os)
	    throws OOFBadElementFormException {
		return new Span(this, attrs, os);
	}

	public Elementable strong()
	    throws OOFBadElementFormException {
		return new Strong(this, new Object[] {}, new Object[] {});
	}
	public Elementable strong(Object o)
	    throws OOFBadElementFormException {
		return new Strong(this, new Object[] {}, new Object[] {o});
	}
	public Elementable strong(Object[] os)
	    throws OOFBadElementFormException {
		return new Strong(this, new Object[] {}, os);
	}
	public Elementable strong(Object[] attrs, Object o)
	    throws OOFBadElementFormException {
		return new Strong(this, attrs, new Object[] {o});
	}
	public Elementable strong(Object[] attrs, Object[] os)
	    throws OOFBadElementFormException {
		return new Strong(this, attrs, os);
	}

	public Elementable table()
	    throws OOFBadElementFormException {
		return new Table(this, new Object[] {}, new Object[] {});
	}
	public Elementable table(Object[] attrs, Object[] rows)
	    throws OOFBadElementFormException {
		Object[] os = new Object[rows.length];
		for (int i = 0; i < rows.length; i++)
			os[i] = this.table_row(rows[i]);
		return new Table(this, attrs, os);
	}
	public Elementable table(Object[] rows)
	    throws OOFBadElementFormException {
		Object[] os = new Object[rows.length];
		for (int i = 0; i < rows.length; i++)
			os[i] = this.table_row(rows[i]);
		return new Table(this, new Object[] {}, os);
	}
	public Elementable table(Object[] attrs, Object[][] rows)
	    throws OOFBadElementFormException {
		Object[] os = new Object[rows.length];
		for (int i = 0; i < rows.length; i++)
			os[i] = this.table_row(rows[i]);
		return new Table(this, attrs, os);
	}
	public Elementable table(Object[][] rows)
	    throws OOFBadElementFormException {
		Object[] os = new Object[rows.length];
		for (int i = 0; i < rows.length; i++)
			os[i] = this.table_row(rows[i]);
		return new Table(this, new Object[] {}, os);
	}
	public Elementable table(Object[][][] rows)
	    throws OOFBadElementFormException {
		Object[] os = new Object[rows.length];
		for (int i = 0; i < rows.length; i++)
			os[i] = this.table_row(rows[i]);
		return new Table(this, new Object[] {}, os);
	}
	public Elementable table(Object[] attrs, Object[][][] rows)
	    throws OOFBadElementFormException {
		Object[] os = new Object[rows.length];
		for (int i = 0; i < rows.length; i++)
			os[i] = this.table_row(rows[i]);
		return new Table(this, attrs, os);
	}

	private Elementable table_cell(Object o)
	    throws OOFBadElementFormException {
		return new TableCell(this, new Object[] {}, new Object[] {o});
	}
	private Elementable table_cell(Object[] attrs)
	    throws OOFBadElementFormException {
		Elementable tc = new TableCell(this, attrs, new Object[] {});
		tc.append(tc.removeAttribute("value"));
		return tc;
	}

	public Elementable table_row(Object cell)
	    throws OOFBadElementFormException {
		return new TableRow(this, new Object[] { this.table_cell(cell) });
	}
	public Elementable table_row(Object[] cells)
	    throws OOFBadElementFormException {
		Object[] os = new Object[cells.length];
		for (int i = 0; i < cells.length; i++)
			os[i] = this.table_cell(cells[i]);
		return new TableRow(this, os);
	}
	public Elementable table_row(Object[][] cells)
	    throws OOFBadElementFormException {
		Object[] os = new Object[cells.length];
		for (int i = 0; i < cells.length; i++)
			os[i] = this.table_cell(cells[i]);
		return new TableRow(this, os);
	}

	/* Piecewise Elements. */
	public Startable div_start()
	    throws OOFBadElementFormException {
		return new DivisionStart(this, new Object[] {});
	}
	public Startable div_start(Object[] attrs)
	    throws OOFBadElementFormException {
		return new DivisionStart(this, attrs);
	}

	public Endable div_end()
	    throws OOFBadElementFormException {
		return new DivisionEnd(this);
	}

	public Startable form_start()
	    throws OOFBadElementFormException {
		return new FormStart(this, new Object[] {});
	}
	public Startable form_start(Object[] attrs)
	    throws OOFBadElementFormException {
		return new FormStart(this, attrs);
	}

	public Endable form_end()
	    throws OOFBadElementFormException {
		return new FormEnd(this);
	}

	public Startable list_start()
	    throws OOFBadElementFormException {
		return new ListStart(this, new Object[] {});
	}
	public Startable list_start(Object type)
	    throws OOFBadElementFormException {
		return new ListStart(this, new Object[] { "type", type });
	}

	public Endable list_end()
	    throws OOFBadElementFormException {
		return new ListEnd(this, null);
	}
	public Endable list_end(Object type)
	    throws OOFBadElementFormException {
		/* XXX: make a list/array */
		return new ListEnd(this, type);
	}

	public Startable table_start()
	    throws OOFBadElementFormException {
		return new TableStart(this, new Object[] {});
	}
	public Startable table_start(Object[] attrs)
	    throws OOFBadElementFormException {
		return new TableStart(this, attrs);
	}

	public Endable table_end()
	    throws OOFBadElementFormException {
		return new TableEnd(this);
	}

	/* Alias Elements. */
/*
	// Yeah right
	public Elementable em()		{ return new Emphasis(); }
	public Elementable image()		{ return new Image(); }
	public Elementable para()		{ return new Paragraph(); }
	public Elementable tr()		{ return new TableRow(); }
*/
};
