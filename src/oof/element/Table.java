/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class Table extends ELEMENT {
	private Object[][][] rows;
	public Table(OOF oof, Object[] attrs, Object[][][] osss) {
		super(oof, attrs, new Object[] {});
		this.rows = osss;
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
