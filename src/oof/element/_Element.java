/* $Id$ */
package oof.element;

import java.util.*;
import oof.*;
import oof.filter.*;

public abstract class ELEMENT {
	public LinkedList attrs;
	public LinkedList children;
	public OOF oof;

	public ELEMENT(OOF oof, Object[] attrs, Object[] os) {
		this.oof	= oof;
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

	abstract public String toString();

	public String getAttribute(String key) {
		Object e;
		for (Iterator i = this.attrs.iterator();
		     (e = i.next()) != null; )
			if (((String)e).equals(key))
				return (String)i.next();
		return null;
	}
};
