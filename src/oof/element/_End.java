/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public abstract class END implements Endable {
	public OOF oof;
	
	public END(OOF oof) throws OOFBadElementFormException {
		this.oof = oof;
	}
};
