/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class FormStart extends START implements Startable {
	public FormStart(OOF oof, Object[] attrs) throws OOFBadElementFormException {
		super(oof, attrs);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
