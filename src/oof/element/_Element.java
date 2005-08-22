/* $Id$ */
package oof.element;

import java.util.*;
import oof.*;

public abstract class _Element extends _Start implements Elementable {
	protected LinkedList children;

	public _Element(OOF oof, Object[] attrs, Object[] os)
	    throws OOFBadElementFormException {
		super(oof, attrs);

		this.children = new LinkedList();
		/* XXX: check for attrs.length % 2. */
		for (int i = 0; i < os.length; i++)
			this.children.add(os[i]);
	}

	public void append(Object o) {
		if (o != null)
			this.children.addLast(o);
	}

	public void prepend(Object o) {
		if (o != null)
			this.children.addFirst(o);
	}

	public String getValue() {
		String v = "";
		for (Iterator i = this.children.iterator(); i.hasNext(); )
			v += i.next().toString();
		return v;
	}
};
