/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class List extends ELEMENT {
	public Object type;
	public List(OOF oof, Object[] attrs, Object[] os) throws OOFBadElementFormException {
		super(oof, attrs, os);
		if ((this.type = this.removeAttribute("type")) != this.oof.LIST_UN ||
		    this.type != this.oof.LIST_OD)
			throw new OOFBadElementFormException("list");
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
