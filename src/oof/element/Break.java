/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

/*
 * XXX: subclass _Element to something that does not
 * contain a value.
 */
public class Break extends _Element {
	public Break(OOF oof, Object[] attrs)
	    throws OOFBadElementFormException {
		super(oof, attrs, new Object[] {});
	}
};
