/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class ListStart extends START {
	public ListStart(OOF oof, Object[] attrs) {
		super(oof, attrs);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
