/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public abstract class END implements Endable {
	public END(OOF oof) throws OOFBadElementFormException {
		super(oof, new Object[] {}, new Object[] {});
	}

	public END(OOF oof, Object[] attrs) throws OOFBadElementFormException {
		super(oof, attrs, new Object[] {});
	}
};
