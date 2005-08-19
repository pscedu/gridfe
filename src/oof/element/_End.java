/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public abstract class END implements Endable {
	public OOF oof;

	public END(OOF oof)
	    throws OOFBadElementFormException {
		this.oof = oof;
	}

	public String toString() {
		/* This would be so much easier:
		 *	this.oof.__getFilter().build(this);
		 */
		try {
			return (String)this.oof.__getFilter().getClass().getMethod("build",
				new Class[] { this.getClass() }).invoke(
					this.oof.__getFilter(), new Object[] { this });
		} catch (Exception e) {
			/* Fuck */
			return "(@@@@@ FAILED @@@@@)";
		}
	}
};
