/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class TableStart extends START implements Startable {
	public TableStart(OOF oof, Object[] attrs) throws OOFBadElementFormException {
		super(oof, attrs);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
