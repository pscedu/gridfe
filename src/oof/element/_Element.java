/* $Id$ */
package oof.element;

import java.util.*;
import oof.*;
import oof.filter.*;

public class ELEMENT {
	private String name;
	private LinkedList attrs;
	private LinkedList children;
	private FILTER filter;

	public ELEMENT() {
		this.attrs	= new LinkedList();
		this.children	= new LinkedList();
	}

	public ELEMENT(Object[] attrs) {
		this.attrs	= new LinkedList();
		this.children	= new LinkedList();
		for (int i = 0; i < attrs.length; i++)
			this.attrs.add(attrs[i]);
	}

	public ELEMENT(String s) {
		this.attrs	= new LinkedList();
		this.children	= new LinkedList();
		this.children.add(s);
	}

	public ELEMENT(ELEMENT e) {
		this.attrs	= new LinkedList();
		this.children	= new LinkedList();
		this.children.add(e);
	}

	public ELEMENT(Object[] attrs, String s) {
		this.attrs	= new LinkedList();
		this.children	= new LinkedList();
		for (int i = 0; i < attrs.length; i++)
			this.attrs.add(attrs[i]);
		this.children.add(s);
	}

	public ELEMENT(Object[] attrs, ELEMENT e) {
		this.attrs	= new LinkedList();
		this.children	= new LinkedList();
		for (int i = 0; i < attrs.length; i++)
			this.attrs.add(attrs[i]);
		this.children.add(e);
	}

	public void append(String s) {
		this.children.addLast((Object)s);
	}

	public void append(ELEMENT e) {
		this.children.addLast((Object)e);
	}

	public void prepend(String s) {
		this.children.addFirst((Object)s);
	}

	public void prepend(ELEMENT e) {
		this.children.addFirst((Object)e);
	}

	public String toString() {
//		throw new OOFToStringException();
		return "@@@@@ GARBAGE " + this.name + " @@@@@";
	}

	public String build() {
		return this.filter.build(this);
	}
};
