/* $Id$ */
package oof.filter;

import oof.element.*;

public class XHTML extends FILTER {
	public String build(ELEMENT e) {
		HTMLElement html = new HTMLElement(e.name);
		String s;
		Attribute attr;


		return html.build();
	}
}
