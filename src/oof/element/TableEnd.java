/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class TableEnd extends END {
	public TableEnd(OOF oof) {
		super(oof);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
