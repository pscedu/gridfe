/* $Id$ */
package oof.filter;

import jasp.*;
import oof.*;
import oof.element.*;

public abstract class FILTER {
	protected OOF oof;
	protected JASP jasp;

	public FILTER(JASP jasp, OOF oof) {
		this.jasp = jasp;
		this.oof  = oof;
	}

	public abstract String build(ELEMENT e);
};
