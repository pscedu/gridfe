/* $Id$ */
package oof.filter;

import oof.element.*;

public class XHTML extends Filter {
	private OOF oof;

	public build(ELEMENT e) {
		HTMLElement html = new HTMLElement(e.name);
		String s;
		Attribute attr;


		return html.build();
	}
}
