/* $Id$ */
package oof.element;

import java.util.*;
import oof.*;
import oof.filter.*;

public abstract class ELEMENT implements Element
{
	public LinkedList attrs;
	public LinkedList children;
	public OOF oof;

	public ELEMENT(OOF oof, Object[] attrs, Object[] os) throws OOFBadElementFormException
	{
		this.oof	= oof;
		this.attrs	= new LinkedList();
		this.children	= new LinkedList();
		for (int i = 0; i < attrs.length; i++)
			this.attrs.add(attrs[i]);
		for (int i = 0; i < os.length; i++)
			this.children.add(os[i]);
	}

	public void append(Object o)
	{
		if (o != null)
			this.children.addLast(o);
	}

	public void prepend(Object o)
	{
		if (o != null)
			this.children.addFirst(o);
	}

	public void addAttribute(String key, String val)
	{
		this.attrs.add((Object)key);
		this.attrs.add((Object)val);
	}

	public Object removeAttribute(String key)
	{
		for (int i = 0; i < this.attrs.size(); i += 2)
			/* Attribute names must be strings. */
			if (((String)this.attrs.get(i)).equals(key)) {
				Object val = this.attrs.get(i + 1);
				this.attrs.remove(i);
				this.attrs.remove(i);
				return val;
			}
		return null;
	}

	public String getAttribute(String key)
	{
		Object e;
		for (Iterator i = this.attrs.iterator();
		     (e = i.next()) != null; )
			if (((String)e).equals(key))
				return (String)i.next();
		return null;
	}

	public String getValue()
	{
		String v = "";
		for (Iterator i = this.children.iterator(); i.hasNext(); )
			v += i.next().toString();
		return v;
	}

	public LinkedList getAttributes()
	{
		return this.attrs;
	}

	public String toString()
	{
		/* This would be so much easier:
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
