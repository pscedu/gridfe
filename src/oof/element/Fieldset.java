/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class Fieldset extends ELEMENT {
	public Fieldset(OOF oof, Object[] attrs, Object[] os) {
		super(oof, attrs, os);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
