/* $Id$ */
package oof;

import java.lang.reflect.*;
import jasp.*;
import oof.filter.*;
import oof.element.*;

public class OOF {
	private Filter filter;
	private JASP jasp;

	public static final Object LIST_UN = "1";
	public static final Object LIST_OD = "2";

	public OOF(JASP j, String filter)
		throws ClassNotFoundException, NoSuchMethodException,
		       InstantiationException, IllegalAccessException,
		       InvocationTargetException
	{
		this.jasp = j;
		this.filter = (Filter)Class.forName("oof.filter." + filter).
				getConstructor(new Class[] { JASP.class, OOF.class }).
				newInstance(new Object[] { this.jasp, this });
	}

	public Filter __getFilter()
	{
		return this.filter;
	}

	/* Core Elements. */
	public Element br()
		throws OOFBadElementFormException
	{
		return new Break(this, new Object[] {});
	}
	public Element br(Object[] attrs)
		throws OOFBadElementFormException
	{
		return new Break(this, attrs);
	}

	public Element Code()
		throws OOFBadElementFormException
	{
		return new Code(this, new Object[] {}, new Object[] {});
	}
	public Element code(Object[] os)
		throws OOFBadElementFormException
	{
		return new Code(this, new Object[] {}, os);
	}
	public Element code(Object o)
		throws OOFBadElementFormException
	{
		return new Code(this, new Object[] {}, new Object[] {o});
	}
	public Element code(Object[] attrs, Object o)
		throws OOFBadElementFormException
	{
		return new Code(this, attrs, new Object[] {o});
	}
	public Element code(Object[] attrs, Object[] os)
		throws OOFBadElementFormException
	{
		return new Code(this, attrs, os);
	}

	public Element div()
		throws OOFBadElementFormException
	{
		return new Division(this, new Object[] {}, new Object[] {});
	}
	public Element div(Object o)
		throws OOFBadElementFormException
	{
		return new Division(this, new Object[] {}, new Object[] {o});
	}
	public Element div(Object[] os)
		throws OOFBadElementFormException
	{
		return new Division(this, new Object[] {}, os);
	}
	public Element div(Object[] attrs, Object o)
		throws OOFBadElementFormException
	{
		return new Division(this, attrs, new Object[] {o});
	}
	public Element div(Object[] attrs, Object[] os)
		throws OOFBadElementFormException
	{
		return new Division(this, attrs, os);
	}

	public Element email()
		throws OOFBadElementFormException
	{
		return new Email(this, new Object[] {}, new Object[] {});
	}
	public Element email(String addr)
		throws OOFBadElementFormException
	{
		Email e = new Email(this, new Object[] {}, new Object[] {addr});
		e.addr = addr;
		return e;
	}
	public Element email(String title, String addr)
		throws OOFBadElementFormException
	{
		Email e = new Email(this, new Object[] {}, new Object[] {title});
		e.addr = addr;
		return e;
	}
/*
	public Element email(Object[] attrs)
		throws OOFBadElementFormException
	{
		return new Email();
	}
	public Element email(obj[]...)
		throws OOFBadElementFormException
	{
		return new Email();
	}
*/

	public Element emph()
		throws OOFBadElementFormException
	{
		return new Emphasis(this, new Object[] {}, new Object[] {});
	}
	public Element emph(Object[] os)
		throws OOFBadElementFormException
	{
		return new Emphasis(this, new Object[] {}, os);
	}
	public Element emph(Object o)
		throws OOFBadElementFormException
	{
		return new Emphasis(this, new Object[] {}, new Object[] {o});
	}
	public Element emph(Object[] attrs, Object o)
		throws OOFBadElementFormException
	{
		return new Emphasis(this, attrs, new Object[] {o});
	}
	public Element emph(Object[] attrs, Object[] os)
		throws OOFBadElementFormException
	{
		return new Emphasis(this, attrs, os);
	}

	public Element fieldset()
		throws OOFBadElementFormException
	{
		return new Fieldset(this, new Object[] {}, new Object[] {});
	}
	public Element fieldset(Object o)
		throws OOFBadElementFormException
	{
		return new Fieldset(this, new Object[] {}, new Object[] {o});
	}
	public Element fieldset(Object[] os)
		throws OOFBadElementFormException
	{
		return new Fieldset(this, new Object[] {}, os);
	}
	public Element fieldset(Object[] attrs, Object o)
		throws OOFBadElementFormException
	{
		return new Fieldset(this, attrs, new Object[] {o});
	}
	public Element fieldset(Object[] attrs, Object[] os)
		throws OOFBadElementFormException
	{
		return new Fieldset(this, attrs, os);
	}

	public Element form()
		throws OOFBadElementFormException
	{
		return new Form(this, new Object[] {}, new Object[] {});
	}
	public Element form(Object[] os)
		throws OOFBadElementFormException
	{
		return new Form(this, new Object[] {}, os);
	}
	public Element form(Object o)
		throws OOFBadElementFormException
	{
		return new Form(this, new Object[] {}, new Object[] {o});
	}
	public Element form(Object[] attrs, Object o)
		throws OOFBadElementFormException
	{
		return new Form(this, attrs, new Object[] {o});
	}
	public Element form(Object[] attrs, Object[] os)
		throws OOFBadElementFormException
	{
		return new Form(this, attrs, os);
	}

	public Element header()
		throws OOFBadElementFormException
	{
		return new Header(this, new Object[] {}, new Object[] {});
	}
	public Element header(Object o)
		throws OOFBadElementFormException
	{
		return new Header(this, new Object[] {}, new Object[] {o});
	}
	public Element header(Object o, Object size)
		throws OOFBadElementFormException
	{
		return new Header(this, new Object[] { "size", size }, new Object[] {o});
	}
	public Element header(Object[] os)
		throws OOFBadElementFormException
	{
		return new Header(this, new Object[] {}, os);
	}
	public Element header(Object[] attrs, Object o)
		throws OOFBadElementFormException
	{
		return new Header(this, attrs, new Object[] {o});
	}
	public Element header(Object[] attrs, Object[] os)
		throws OOFBadElementFormException
	{
		return new Header(this, attrs, os);
	}

	public Element hr()
		throws OOFBadElementFormException
	{
		return new HorizontalRuler(this, new Object[] {});
	}
	public Element hr(Object[] attrs)
		throws OOFBadElementFormException
	{
		return new HorizontalRuler(this, attrs);
	}

	public Element img()
		throws OOFBadElementFormException
	{
		return new Image(this, new Object[] {}, new Object[] {});
	}
	public Element img(Object[] attrs)
		throws OOFBadElementFormException
	{
		return new Image(this, attrs, new Object[] {});
	}

	public Element input()
		throws OOFBadElementFormException
	{
		return new Input(this, new Object[] {}, new Object[] {});
	}
	public Element input(Object[] attrs)
		throws OOFBadElementFormException
	{
		return new Input(this, attrs, new Object[] {});
	}

	public Element link()
		throws OOFBadElementFormException
	{
		return new Link(this, new Object[] {}, new Object[] {});
	}
	public Element link(String name)
		throws OOFBadElementFormException
	{
		return new Link(this, new Object[] { "name", name }, new Object[] {});
	}
	public Element link(String title, String href)
		throws OOFBadElementFormException
	{
		return new Link(this, new Object[] { "href", href }, new Object[] { title });
	}
/*
	public Element link(Object[] attrs)
		throws OOFBadElementFormException
	{
		return new Link();
	}
*/

	public Element list()
		throws OOFBadElementFormException
	{
		return new List(this, new Object[] {}, new Object[] {});
	}
	public Element list(Object type)
		throws OOFBadElementFormException
	{
		return new List(this, new Object[] { "type", type }, new Object[] {});
	}
	public Element list(Object[] os)
		throws OOFBadElementFormException
	{
		Object[] items = new Object[os.length];
		for (int i = 0; i < items.length; i++)
			items[i] = new ListItem(this, new Object[] {}, new Object[] {os[i]});
		return new List(this, new Object[] {}, items);
	}
	public Element list(Object type, Object[] os)
		throws OOFBadElementFormException
	{
		Object[] items = new Object[os.length];
		for (int i = 0; i < items.length; i++)
			items[i] = new ListItem(this, new Object[] {}, new Object[] {os[i]});
		return new List(this, new Object[] { "type", type }, items);
	}

	public Element list_item()
		throws OOFBadElementFormException
	{
		return new ListItem(this, new Object[] {}, new Object[] {});
	}
	public Element list_item(Object o)
		throws OOFBadElementFormException
	{
		return new ListItem(this, new Object[] {}, new Object[] {o});
	}
	public Element list_item(Object[] os)
		throws OOFBadElementFormException
	{
		return new ListItem(this, new Object[] {}, os);
	}

	public Element p()
		throws OOFBadElementFormException
	{
		return new Paragraph(this, new Object[] {}, new Object[] {});
	}
	public Element p(Object o)
		throws OOFBadElementFormException
	{
		return new Paragraph(this, new Object[] {}, new Object[] {o});
	}
	public Element p(Object[] os)
		throws OOFBadElementFormException
	{
		return new Paragraph(this, new Object[] {}, os);
	}
	public Element p(Object[] attrs, Object o)
		throws OOFBadElementFormException
	{
		return new Paragraph(this, attrs, new Object[] {o});
	}
	public Element p(Object[] attrs, Object[] os)
		throws OOFBadElementFormException
	{
		return new Paragraph(this, attrs, os);
	}

	public Element pre()
		throws OOFBadElementFormException
	{
		return new Preformatted(this, new Object[] {}, new Object[] {});
	}
	public Element pre(Object o)
		throws OOFBadElementFormException
	{
		return new Preformatted(this, new Object[] {}, new Object[] {o});
	}
	public Element pre(Object[] os)
		throws OOFBadElementFormException
	{
		return new Preformatted(this, new Object[] {}, os);
	}
	public Element pre(Object[] attrs, Object o)
		throws OOFBadElementFormException
	{
		return new Preformatted(this, attrs, new Object[] {o});
	}
	public Element pre(Object[] attrs, Object[] os)
		throws OOFBadElementFormException
	{
		return new Preformatted(this, attrs, os);
	}

	public Element span()
		throws OOFBadElementFormException
	{
		return new Span(this, new Object[] {}, new Object[] {});
	}
	public Element span(Object o)
		throws OOFBadElementFormException
	{
		return new Span(this, new Object[] {}, new Object[] {o});
	}
	public Element span(Object[] os)
		throws OOFBadElementFormException
	{
		return new Span(this, new Object[] {}, os);
	}
	public Element span(Object[] attrs, Object o)
		throws OOFBadElementFormException
	{
		return new Span(this, attrs, new Object[] {o});
	}
	public Element span(Object[] attrs, Object[] os)
		throws OOFBadElementFormException
	{
		return new Span(this, attrs, os);
	}

	public Element strong()
		throws OOFBadElementFormException
	{
		return new Strong(this, new Object[] {}, new Object[] {});
	}
	public Element strong(Object o)
		throws OOFBadElementFormException
	{
		return new Strong(this, new Object[] {}, new Object[] {o});
	}
	public Element strong(Object[] os)
		throws OOFBadElementFormException
	{
		return new Strong(this, new Object[] {}, os);
	}
	public Element strong(Object[] attrs, Object o)
		throws OOFBadElementFormException
	{
		return new Strong(this, attrs, new Object[] {o});
	}
	public Element strong(Object[] attrs, Object[] os)
		throws OOFBadElementFormException
	{
		return new Strong(this, attrs, os);
	}

	public Element table()
		throws OOFBadElementFormException
	{
		return new Table(this, new Object[] {}, new Object[] {});
	}
	public Element table(Object[] attrs, Object[] rows)
		throws OOFBadElementFormException
	{
		Object[] os = new Object[rows.length];
		for (int i = 0; i < rows.length; i++)
			os[i] = this.table_row(rows[i]);
		return new Table(this, attrs, os);
	}
	public Element table(Object[] rows)
		throws OOFBadElementFormException
	{
		Object[] os = new Object[rows.length];
		for (int i = 0; i < rows.length; i++)
			os[i] = this.table_row(rows[i]);
		return new Table(this, new Object[] {}, os);
	}
	public Element table(Object[] attrs, Object[][] rows)
		throws OOFBadElementFormException
	{
		Object[] os = new Object[rows.length];
		for (int i = 0; i < rows.length; i++)
			os[i] = this.table_row(rows[i]);
		return new Table(this, attrs, os);
	}
	public Element table(Object[][] rows)
		throws OOFBadElementFormException
	{
		Object[] os = new Object[rows.length];
		for (int i = 0; i < rows.length; i++)
			os[i] = this.table_row(rows[i]);
		return new Table(this, new Object[] {}, os);
	}
	public Element table(Object[][][] rows)
		throws OOFBadElementFormException
	{
		Object[] os = new Object[rows.length];
		for (int i = 0; i < rows.length; i++)
			os[i] = this.table_row(rows[i]);
		return new Table(this, new Object[] {}, os);
	}
	public Element table(Object[] attrs, Object[][][] rows)
		throws OOFBadElementFormException
	{
		Object[] os = new Object[rows.length];
		for (int i = 0; i < rows.length; i++)
			os[i] = this.table_row(rows[i]);
		return new Table(this, attrs, os);
	}

	private Element table_cell(Object o)
		throws OOFBadElementFormException
	{
		return new TableCell(this, new Object[] {}, new Object[] {o});
	}
	private Element table_cell(Object[] attrs)
		throws OOFBadElementFormException
	{
		Element tc = new TableCell(this, attrs, new Object[] {});
		tc.append(tc.removeAttribute("value"));
		return tc;
	}

	public Element table_row(Object cell)
		throws OOFBadElementFormException
	{
		return new TableRow(this, new Object[] { this.table_cell(cell) });
	}
	public Element table_row(Object[] cells)
		throws OOFBadElementFormException
	{
		Object[] os = new Object[cells.length];
		for (int i = 0; i < cells.length; i++)
			os[i] = this.table_cell(cells[i]);
		return new TableRow(this, os);
	}
	public Element table_row(Object[][] cells)
		throws OOFBadElementFormException
	{
		Object[] os = new Object[cells.length];
		for (int i = 0; i < cells.length; i++)
			os[i] = this.table_cell(cells[i]);
		return new TableRow(this, os);
	}

	/* Piecewise Elements. */
	public Startable div_start()
		throws OOFBadElementFormException
	{
		return new DivisionStart(this, new Object[] {});
	}
	public Startable div_start(Object[] attrs)
		throws OOFBadElementFormException
	{
		return new DivisionStart(this, attrs);
	}

	public Endable div_end()
		throws OOFBadElementFormException
	{
		return new DivisionEnd(this);
	}

	public Startable form_start()
		throws OOFBadElementFormException
	{
		return new FormStart(this, new Object[] {});
	}
	public Startable form_start(Object[] attrs)
		throws OOFBadElementFormException
	{
		return new FormStart(this, attrs);
	}

	public Endable form_end()
		throws OOFBadElementFormException
	{
		return new FormEnd(this);
	}

	public Startable list_start()
		throws OOFBadElementFormException
	{
		return new ListStart(this, new Object[] {});
	}
	public Startable list_start(Object type)
		throws OOFBadElementFormException
	{
		return new ListStart(this, new Object[] { "type", type });
	}

	public Endable list_end()
		throws OOFBadElementFormException
	{
		return new ListEnd(this, null);
	}
	public Endable list_end(Object type)
		throws OOFBadElementFormException
	{
		/* XXX: make a list/array */
		return new ListEnd(this, type);
	}

	public Startable table_start()
		throws OOFBadElementFormException
	{
		return new TableStart(this, new Object[] {});
	}
	public Startable table_start(Object[] attrs)
		throws OOFBadElementFormException
	{
		return new TableStart(this, attrs);
	}

	public Endable table_end()
		throws OOFBadElementFormException
	{
		return new TableEnd(this);
	}

	/* Alias Elements. */
/*
	// Yeah right
	public Element em()		{ return new Emphasis(); }
	public Element image()		{ return new Image(); }
	public Element para()		{ return new Paragraph(); }
	public Element tr()		{ return new TableRow(); }
*/
};
