/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class Link extends ELEMENT {
	public Link(OOF oof, Object[] attrs, Object[] os) {
		super(oof, attrs, os);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
