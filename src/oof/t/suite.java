/* $Id$ */

import jasp.*;
import oof.*;

public class suite {
	public static void t(String desc, Object a, String b) {
		String v = a.toString();
		System.out.println(desc + ": " + v);
		if (!b.equals(v)) {
			System.out.println("Output does not match expected!");
			System.out.println("expected: " + b);
			System.exit(1);
		}
	}

	public static void main(String[] args) throws Exception {
		JASP j = new JASP();
		OOF o = new OOF(j, "xhtml");

		t("br", o.br(), "<br />");
		t("br", o.br(new Object[] { "clear", "right" }), "<br clear=\"right\" />");

		t("code", o.code("sdfsf"), "<code>sdfsf</code>");
		t("code", o.code(new Object[] { "class", "test" }, "sdfsf"),
			"<code class=\"test\">sdfsf</code>");

		t("div", o.div(), "<div></div>");
		t("div", o.div("some content"), "<div>some content</div>");
		t("div", o.div(new Object[] { "align", "center" }, "some content"),
			"<div align=\"center\">some content</div>");
		t("div", o.div(new Object[] { "some content ", "and some more" }),
			"<div>some content and some more</div>");
		t("div", o.div(new Object[] { "align", "right" },
		      	   new Object[] { "content, ", "content, ", "and more" }),
			"<div align=\"right\">content, content, and more</div>");

		t("div_start", o.div_start(new Object[] { "class", "foo" }),
			"<div class=\"foo\">");
		t("div_start", o.div_start(), "<div>");

		t("div_end", o.div_end(), "</div>");

		t("email", o.email("foo@bar.com"),
			"<a href=\"mailto:foo@bar.com\">foo@bar.com</a>");
		t("email", o.email("Foobar", "foo@bar.com"),
			"<a href=\"mailto:foo@bar.com\">Foobar</a>");

		t("emph", o.emph("some emphasized text"), "<em>some emphasized text</em>");
		t("emph", o.emph(new Object[] { "class", "foo" }, "some emphasized text"),
			"<em class=\"foo\">some emphasized text</em>");
		t("emph", o.emph(new Object[] { "some ", "more ", "text" }),
			"<em>some more text</em>");

		t("fieldset", o.fieldset(new Object[] {
				"field1: ", o.input(),
				"field2: ", o.input()}),
			"<fieldset>field1: <input />field2: <input /></fieldset>");

		t("form", o.form(new Object[] { "method", "post", "action", "url" },
		      	     new Object[] {
		      		"field1: ", o.input(),
		      		"field2: ", o.input()}),
			"<form method=\"post\" action=\"url\">" +
				"field1: <input />field2: <input />" +
			"</form>");
		t("form", o.form(new Object[] {
		      		"field1: ", o.input(),
		      		"field2: ", o.input()}),
			"<form>field1: <input />field2: <input /></form>");

		t("form_start", o.form_start(new Object[] { "method", "get" }),
			"<form method=\"get\">");
		t("form_start", o.form_start(), "<form>");

		t("form_end", o.form_end(), "</form>");

		try {
		t("header", o.header("a"), "<h>a</h>");
		} catch (Exception e) {
		}
		t("header", o.header("b", "2"), "<h2>b</h2>");
		t("header", o.header(new Object[] { "size", "2" }, "c"),
			"<h2>c</h2>");

		t("hr", o.hr(), "<hr />");
		t("hr", o.hr(new Object[] { "noborder", "yes" }),
			"<hr noborder=\"yes\" />");

		t("img", o.img(), "<img />");
		t("img", o.img(new Object[] { "src", "foo.jpg" }),
			"<img src=\"foo.jpg\" />");

		t("input", o.input(), "<input />");
		t("input", o.input(new Object[] { "type", "text", "name", "username" }),
			"<input type=\"text\" name=\"username\" />");
		/* XXX: select, textarea */

		t("link", o.link("supername"), "<a name=\"supername\"></a>");
		t("link", o.link("foo", "url"), "<a href=\"url\">foo</a>");

		t("list", o.list(o.LIST_UN, new Object[] { "i1", "i2", "i3" }),
			"<ul><li>i1</li><li>i2</li><li>i3</li></ul>");
		t("list", o.list(o.LIST_OD, new Object[] { "a", "b", "c" }),
			"<ol><li>a</li><li>b</li><li>c</li></ol>");

		t("list_start", o.list_start(o.LIST_UN), "<ul>");
		t("list_end", o.list_end(o.LIST_OD), "</ol>");

		t("list_item", o.list_item("sup"), "<li>sup</li>");

		t("p", o.p(), "<p />");
		t("p", o.p("paragraph test"), "<p>paragraph test</p>");
		t("p", o.p(new Object[] { "align", "center" }, "paragraph test"),
			"<p align=\"center\">paragraph test</p>");

		t("pre", o.pre("some pre text"),
			"<pre>some pre text</pre>");
		t("pre", o.pre(new Object[] { "class", "foo" }, "some pre text"),
			"<pre class=\"foo\">some pre text</pre>");

		t("span", o.span(), "<span />");
		t("span", o.span(new Object[] { "some ", "text" }),
			"<span>some text</span>");
		t("span", o.span(new Object[] { "class", "foo" },
		      	     new Object[] { "more ", "text" }),
			"<span class=\"foo\">more text</span>");
		t("span", o.span(new Object[] { "class", "bleh" }, "even more text"),
			"<span class=\"bleh\">even more text</span>");

		t("strong", o.strong(), "<strong />");
		t("strong", o.strong("str text"),
			"<strong>str text</strong>");
		t("strong", o.strong(new Object[] { "attr", "val" }, "str text2"),
			"<strong attr=\"val\">str text2</strong>");
		t("strong", o.strong(new Object[] { "attr", "val" },
		      		 new Object[] { "str ", "text3" }),
			"<strong attr=\"val\">str text3</strong>");

		t("table", o.table(new Object[][] {
		      		new Object[] { "r1c1", "r1c2" },
		      		new Object[] { "r2c1", "r2c2" }}),
			"<table>" +
				"<tr>" +
					"<td>r1c1</td>" +
					"<td>r1c2</td>" +
				"</tr>" +
				"<tr>" +
					"<td>r2c1</td>" +
					"<td>r2c2</td>" +
				"</tr>" +
			"</table>");
		t("table", o.table(new Object[] { "border", "2", "cols",
		      		new Object[][] {
		      			new Object[] { "width", "1" },
		      			new Object[] { "width", "3" }}},
		      		new Object[][] {
		      			new Object[] { "r1c1", "r1c2" },
		      			new Object[] { "r2c1", "r2c2" }}),
			"<table border=\"2\">" +
				"<colgroup>" +
					"<col width=\"1\" />" +
					"<col width=\"3\" />" +
				"</colgroup>" +
				"<tr>" +
					"<td>r1c1</td>" +
					"<td>r1c2</td>" +
				"</tr>" +
				"<tr>" +
					"<td>r2c1</td>" +
					"<td>r2c2</td>" +
				"</tr>" +
			"</table>");
		t("table_start", o.table_start(new Object[] { "width", "500" }),
			"<table width=\"500\">");
		t("table_end", o.table_end(), "</table>");
		t("table_row", o.table_row(new Object[][] {
					new Object[] { "c1p1k", "c1p1v" },
					new Object[] { "c2p1k", "c2p1v" }}),
			"<tr><td c1p1k=\"c1p1v\" /><td c2p1k=\"c2p1v\" /></tr>"
		);
	}
}
