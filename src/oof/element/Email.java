/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class Email extends ELEMENT {
	public String addr;
	public Email(OOF oof, Object[] attrs, Object[] os) throws OOFBadElementFormException {
		super(oof, attrs, os);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
