/* $Id$ */
package oof.element;

import java.util.*;
import oof.*;

public abstract class _Start implements Startable {
	public HashMap attrs;
	public OOF oof;

	public _Start(OOF oof, Object[] attrs)
	    throws OOFBadElementFormException {
		this.oof	= oof;
		this.attrs	= new HashMap();
		for (int i = 0; i < attrs.length; i += 2)
			this.attrs.put(attrs[i], attrs[i + 1]);
	}

	public void addAttribute(String key, Object val) {
		this.attrs.put(key, val);
	}

	public Object getAttribute(String key) {
		return (this.attrs.get(key));
	}

	public Object removeAttribute(String key) {
		return (this.attrs.remove(key));
	}

	public HashMap getAttributes() {
		return (this.attrs);
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
