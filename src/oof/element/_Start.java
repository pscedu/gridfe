/* $Id$ */
package oof.element;

import java.util.*;
import oof.*;
import oof.element.*;

public abstract class _Start implements Startable {
	public LinkedList attrs;
	public OOF oof;

	public _Start(OOF oof, Object[] attrs)
	    throws OOFBadElementFormException {
		this.oof	= oof;
		this.attrs	= new LinkedList();
		for (int i = 0; i < attrs.length; i++)
			this.attrs.add(attrs[i]);
	}

	public Object removeAttribute(String key) {
		for (int i = 0; i < this.attrs.size(); i += 2)
			if (((String)this.attrs.get(i)).equals(key)) {
				Object val = this.attrs.get(i + 1);
				this.attrs.remove(i);
				this.attrs.remove(i);
				return val;
			}
		return null;
	}

	public void addAttribute(String key, String val) {
		this.attrs.add((Object)key);
		this.attrs.add((Object)val);
	}

	public String getAttribute(String key) {
		Object e;
		for (Iterator i = this.attrs.iterator();
		     (e = i.next()) != null; )
			if (((String)e).equals(key))
				return (String)i.next();
		return null;
	}

	public LinkedList getAttributes() {
		return this.attrs;
	}

	public String toString() {
		/*
		 * This would be so much easier:
		 *	this.oof.__getFilter().build(this);
		 */
		try {
			return (String)this.oof.__getFilter().getClass().getMethod("build",
				new Class[] { this.getClass() }).invoke(
					this.oof.__getFilter(), new Object[] { this });
		} catch (Exception e) {
			/* Fuck */
			return "(@@@@@ FAILED @@@@@)";
		}
	}
};
