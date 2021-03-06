/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class ListStart extends _Start {
	public Object type;
	public ListStart(OOF oof, Object[] attrs)
	    throws OOFBadElementFormException {
		super(oof, attrs);
		if ((this.type = this.removeAttribute("type")) != this.oof.LIST_UN &&
		    this.type != this.oof.LIST_OD)
			throw new OOFBadElementFormException("list");
	}
};
