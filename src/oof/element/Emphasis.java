/* $Id$ */
package oof.element;

import oof.element.*;

public class Emphasis extends ELEMENT {
	public Emphasis()				{ super(new Object[] {}, new Object[] {}); }
	public Emphasis(Object[] attrs)			{ super(attrs, new Object[] {}); }
	public Emphasis(Object o)			{ super(new Object[] {}, new Object[] {o}); }
	public Emphasis(Object[] attrs, Object o)	{ super(attrs, new Object[] {o}); }
	public Emphasis(Object[] attrs, Object[] os)	{ super(attrs, os); }
};
