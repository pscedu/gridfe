/* $Id$ */
package oof.element;

public class ELEMENT {
	private String name;
	private LinkedList attrs;
	private LinkedList children;

	ELEMENT() {
		this.initWork(Object[] {}, null);
	}
	
	ELEMENT(Object[] attrs) {
		this.initWork(attrs, null);
	}

	ELEMENT(String s) {
		this.initWork(Object[] {}, s);
	}

	ELEMENT(ELEMENT e) {
		this.initWork(Object[] {}, e);
	}

	ELEMENT(Object[] attrs, String s) {
		this.initWork(attrs, s);
	}

	ELEMENT(Object[] attrs, ELEMENT e) {
		this.initWork(attrs, e);
	}

	private void initWork(String name, Object[] attrs, Object value) {
		this.attrs = new LinkedList(attrs);
		this.children = value == null ? new LinkedList() : new LinkedList(value);
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
