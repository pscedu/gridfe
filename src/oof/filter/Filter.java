/* $Id$ */
package oof.filter;

import oof.element.*;

public abstract class FILTER {
	private OOF oof;

	public FILTER(OOF oof) {
		this.oof = oof;
	}

	public abstract String build(ELEMENT e);
};
