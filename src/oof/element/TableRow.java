/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class TableRow extends ELEMENT {
	private Object[][] cells;
	public TableRow(OOF oof, Object[][] oss) {
		super(oof, new Object[] {}, new Object[] {});
		this.cells = oss;
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
