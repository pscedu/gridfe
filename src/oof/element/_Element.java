/* $Id$ */
package oof.element;

public class ELEMENT {
	private String name;
	private LinkedList attrs;
	private LinkedList children;

	ELEMENT(Object[] attrs = {}, String s = null) {
		this.attrs    = new LinkedList();
		this.children = new LinkedList(s);
	}

	ELEMENT(Object[] attrs = {}, ELEMENT e = null) {
		this.attrs    = new LinkedList();
		this.children = new LinkedList(e);
	}

	ELEMENT() {
	}

	public void append(String s) {
		this.children.addLast(s);
	}

	public void append(ELEMENT e) {
		this.children.addLast(e);
	}

	public void prepend(String s) {
		this.children.addFirst(s);
	}

	public void prepend(Element e) {
		this.children.addFirst(e);
	}

	public String toString() {
		throw new OOFToStringException();
	}

	public String build() {
		return this.filter.build(this);
	}
};
