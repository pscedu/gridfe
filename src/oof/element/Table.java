/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class Table extends ELEMENT implements Startable, Endable {
	public TableRow[] rows;
	public Table(OOF oof, Object[] attrs, Object[][][] osss) throws OOFBadElementFormException {
		super(oof, attrs, new Object[] {});
		this.rows = new TableRow[osss.length];
		for (int i = 0; i < osss.length; i++)
			this.rows[i] = new TableRow(oof, osss[i]);
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
