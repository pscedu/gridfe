/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class ListEnd extends END implements Endable {
	public Object type;
	public ListEnd(OOF oof, Object type) throws OOFBadElementFormException {
		super(oof);
		if (type != this.oof.LIST_UN && type != this.oof.LIST_OD)
			throw new OOFBadElementFormException("list");
		this.type = type;
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
