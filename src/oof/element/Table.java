/* $Id$ */
package oof.element;

import oof.*;
import oof.element.*;

public class Table extends ELEMENT implements Startable, Endable {
	private Object[][][] rows;
	public Table(OOF oof, Object[] attrs, Object[][][] osss) throws OOFBadElementFormException {
		super(oof, attrs, new Object[] {});
		this.rows = osss;
	}

	public String toString() {
		return this.oof.__getFilter().build(this);
	}
};
