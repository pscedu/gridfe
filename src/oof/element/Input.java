/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class Input extends ELEMENT {
	public Input(OOF oof, Object[] attrs, Object[] os) throws OOFBadElementFormException {
		super(oof, attrs, os);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
