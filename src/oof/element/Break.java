/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

/*
 * XXX: subclass ELEMENT to something that does not
 * contain a value.
 */
public class Break extends ELEMENT {
	public Break(OOF oof, Object[] attrs) {
		super(oof, attrs, new Object[] {});
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
