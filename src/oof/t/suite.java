/* $Id$ */

import jasp.*;
import oof.*;

public class suite {
	public static void main(String[] args) {
		JASP j = new JASP();
		OOF o = new OOF(j, "xhtml");

		System.out.println("br: " + o.br());
		System.out.println("br: " + o.br(new Object[] { "clear", "right" }));

		System.out.println("code: " + o.code("sdfsf"));
		System.out.println("code: " + o.code(new Object[] { "class", "test" }, "sdfsf"));

		System.out.println("div: " + o.div("some content"));
		System.out.println("div: " + o.div(new Object[] { "align", "center" }, "some content"));
		System.out.println("div: " + o.div(new Object[] { "some content ", "and some more" }));
		System.out.println("div: " + o.div(new Object[] { "align", "right" },
						   new Object[] { "content, ", "content, ", "and more" }));

		System.out.println("div_start: " + o.div_start(new Object[] { "class", "foo" }));
		System.out.println("div_start: " + o.div_start());

		System.out.println("div_end: " + o.div_end());

		System.out.println("email: " + o.email("foo@bar.com"));
		System.out.println("email: " + o.email("Foobar", "foo@bar.com"));

		System.out.println("emph: " + o.emph("some emphasized text"));
		System.out.println("emph: " + o.emph(new Object[] { "class", "foo" }, "some emphasized text"));
		System.out.println("emph: " + o.emph(new Object[] { "some ", "more ", "text" }));

		System.out.println("fieldset: " + o.fieldset(new Object[] {
							"field1: ", oof.input(),
							"field2: ", oof.input()}));

		System.out.println("form: " + o.form(new Object[] { "method", "post", "action", "url" },
						     new Object[] {
							"field1: ", oof.input(),
							"field2: ", oof.input()}));
		System.out.println("form: " + o.form(new Object[] {
							"field1: ", oof.input(),
							"field2: ", oof.input()}));

		System.out.println("form_start: " + o.form_start(new Object[] { "method", "get" }));
		System.out.println("form_start: " + o.form_start());

		System.out.println("form_end: " + o.form_end());

		System.out.println("header: " + o.header("bleh"));
		System.out.println("header: " + o.header("bleh", 2));
		System.out.println("header: " + o.header(new Object[] { "size", 2 }, "bleh"));

		System.out.println("hr: " + o.hr());
		System.out.println("hr: " + o.hr(new Object[] { "noborder", "yes" }));

		System.out.println("img: " + o.img());
		System.out.println("img: " + o.img(new Object[] { "src", "foo.jpg" }));

		System.out.println("input: " + o.input());
		System.out.println("input: " + o.input(new Object[] { "type", "text", "name", "username" }));
		/* XXX: select, textarea */

		System.out.println("link: " + o.link("foo", "url"));

		System.out.println("list: " + o.list(oof.LIST_UN, new Object[] { "i1", "i2", "i3" }));
		System.out.println("list: " + o.list(oof.LIST_OD, new Object[] { "a", "b", "c" }));

		System.out.println("list_start: " + o.list_start(oof.LIST_UN));
		System.out.println("list_end: " + o.list_end(oof.LIST_OD));

		System.out.println("list_item: " + o.list_item("sup"));

		System.out.println("pre: " + o.pre("some pre text"));
		System.out.println("pre: " + o.pre(new Object[] { "class", "foo" }, "some pre text"));

		System.out.println("span: " + o.span());
		System.out.println("span: " + o.span(new Object[] { "some ", "text" }));
		System.out.println("span: " + o.span(new Object[] { "class", "foo" },
						     new Object[] { "more ", "text" }));
		System.out.println("span: " + o.span(new Object[] { "class", "bleh" }, "even more text"));

		System.out.println("strong: " + o.strong());
		System.out.println("strong: " + o.strong("str text"));
		System.out.println("strong: " + o.strong(new Object[] { "attr", "val" }, "str text2"));
		System.out.println("strong: " + o.strong(new Object[] { "attr", "val" },
							 new Object[] { "str ", "text3" }));

		System.out.println("table: " + o.table(new Object[][] {
							new Object[] { "r1c1", "r1c2" },
							new Object[] { "r2c1", "r2c2" }}));
		System.out.println("table: " + o.table(new Object[] { "border", 2, "cols",
							new Object[][] {
								new Object[] { "width", 1 },
								new Object[] { "width", 3 }}},
							new Object[][] {
								new Object[] { "r1c1", "r1c2" },
								new Object[] { "r2c1", "r2c2" }}));
		System.out.println("table_start: " + o.table_start(new Object[] { "width", 500 }));
		System.out.println("table_end: " + o.table_end());
		System.out.println("table_row: " + o.table_row(new Object[][] {
								new Object[] { "r1c1", "r1c2" },
								new Object[] { "r2c1", "r2c2" }}));
	}
}
