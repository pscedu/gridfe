/* $Id$ */
package oof.element;

import oof.element.*;

public class Table extends ELEMENT {
	private Object[][][] rows;
	public Table(Object[] attrs, Object[][][] osss) {
		super(attrs, new Object[] {});
		this.rows = osss;
	}
};
