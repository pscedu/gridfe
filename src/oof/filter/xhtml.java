/* $Id$ */
package oof.filter;

import jasp.*;
import oof.*;
import oof.element.*;

public class xhtml extends FILTER {
	public xhtml(JASP jasp, OOF oof) {
		super(jasp, oof);
	}

	public String build(ELEMENT e) {
		HTMLElement html = new HTMLElement(this.jasp, e.name);
		return html.build();
	}
}
