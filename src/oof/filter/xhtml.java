/* $Id$ */
package oof.filter;

import oof.element.*;

public class xhtml extends FILTER {
	public xhtml(OOF oof) {
		super(oof);
	}

	public String build(ELEMENT e) {
		HTMLElement html = new HTMLElement(e.name);
		String s;
		Attribute attr;


		return html.build();
	}
}
