/* $Id$ */
package oof.element;

import java.util.*;
import oof.*;
import oof.filter.*;

public class ELEMENT {
	public String name;
	public LinkedList attrs;
	public LinkedList children;
	public FILTER filter;

	public ELEMENT(Object[] attrs, Object[] os) {
		this.attrs	= new LinkedList();
		this.children	= new LinkedList();
		for (int i = 0; i < attrs.length; i++)
			this.attrs.add(attrs[i]);
		for (int i = 0; i < os.length; i++)
			this.children.add(os[i]);
	}

	public void append(Object o) {
		this.children.addLast(o);
	}

	public void prepend(Object o) {
		this.children.addFirst(o);
	}

	public String toString() {
//		throw new OOFToStringException();
		return "@@@@@ GARBAGE " + this.name + " @@@@@";
	}

	public String build() {
		return this.filter.build(this);
	}
};
