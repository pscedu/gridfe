/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public abstract class START implements Startable {
	public START(OOF oof, Object[] attrs) throws OOFBadElementFormException {
		super(oof, attrs, new Object[] {});
	}
};
