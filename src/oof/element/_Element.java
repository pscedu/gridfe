/* $Id$ */
package oof.element;

import java.util.*;
import oof.*;
import oof.filter.*;

public abstract class ELEMENT implements Startable, Endable {
	public LinkedList attrs;
	public LinkedList children;
	public OOF oof;

	public ELEMENT(OOF oof, Object[] attrs, Object[] os) throws OOFBadElementFormException {
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

	public void addAttribute(String key, String val) {
		this.attrs.add((Object)key);
		this.attrs.add((Object)val);
	}

	public String removeAttribute(String key) {
		for (int i = 0; i < this.attrs.size(); i += 2) 
			if (((String)this.attrs.get(i)).equals(key)) {
				String val = (String)this.attrs.get(i + 1);
				this.attrs.remove(i);
				this.attrs.remove(i);
				return val;
			}
		return null;
	}

	public String getAttribute(String key) {
		Object e;
		for (Iterator i = this.attrs.iterator();
		     (e = i.next()) != null; )
			if (((String)e).equals(key))
				return (String)i.next();
		return null;
	}

	public String getValue() {
		String v = "";
		for (Iterator i = this.children.iterator(); i.hasNext(); )
			v += i.next().toString();
		return v;
	}

	public LinkedList getAttributes() {
		return this.attrs;
	}
};
