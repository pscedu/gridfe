/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class DivisionEnd extends END {
	public DivisionEnd(OOF oof) throws OOFBadElementFormException {
		super(oof);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
