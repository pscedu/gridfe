/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class ListEnd extends END implements Endable {
	public Object type;
	public ListEnd(OOF oof, Object[] attrs) throws OOFBadElementFormException {
		super(oof, attrs);
		if ((this.type = this.removeAttribute("type")) != this.oof.LIST_UN &&
		    this.type != this.oof.LIST_OD)
			throw new OOFBadElementFormException("list");
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
