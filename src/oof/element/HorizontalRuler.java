/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class HorizontalRuler extends ELEMENT {
	public HorizontalRuler(OOF oof, Object[] attrs) {
		super(oof, attrs, new Object[] {});
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
