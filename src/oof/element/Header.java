/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class Header extends ELEMENT {
	public String size;
	public Header(OOF oof, Object[] attrs, Object[] os) throws OOFBadElementFormException {
		super(oof, attrs, os);
		if ((this.size = this.removeAttribute("size")) == null)
			throw new OOFBadElementFormException("header");
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
