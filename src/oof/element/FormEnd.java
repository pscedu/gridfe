/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class FormEnd extends END implements Endable {
	public FormEnd(OOF oof) throws OOFBadElementFormException {
		super(oof);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
