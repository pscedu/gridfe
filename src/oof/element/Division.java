/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class Division extends ELEMENT {
	public Division(OOF oof, Object[] attrs, Object[] os) {
		super(oof, attrs, os);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
