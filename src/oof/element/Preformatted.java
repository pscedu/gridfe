/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class Preformatted extends ELEMENT {
	public Preformatted(OOF oof, Object[] attrs, Object[] os) throws OOFBadElementFormException {
		super(oof, attrs, os);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
