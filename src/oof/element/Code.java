/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class Code extends ELEMENT {
	public Code(OOF oof, Object[] attrs, Object[] os) {
		super(oof, attrs, os);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
