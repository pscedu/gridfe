/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class FormEnd extends END {
	public FormEnd(OOF oof) {
		super(oof);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
