/* $Id$ */
package oof.element;

public class BASE {
	private Container[] children;
	private int len, pos;
	private String name;
	private Attribute[] attrs;

	Element() {
		this.len = this.pos = 0;
	}

	Element(String s) {
		this.len = this.pos = 0;
		this.append(s);
	}

	Element(Element e) {
		this.len = this.pos = 0;
		this.append(e);
	}

	private void ensure(int newlen) {
		Container[] newchildren;
		newchildren = new Container[newlen];
		if (this.len)
			System.copyarray(this.children, newchildren, 0, 0, this.len);
		this.children = newchildren;
		this.len = newlen;
	}

	public void append(String s) {
		this.ensure(++this.pos);
		children[this.pos].type = TSTRING;
		children[this.pos].s = s;
	}

	public void append(Element e) {
		this.ensure(++this.pos);
		children[this.pos].type = TELEM;
		children[this.pos].e = e;
	}

/*
	public void prepend(String s) {
		this.ensure(++this.pos);
		for (int i = 0; i < this.len)
			
		children[this.pos].type = TSTRING;
		children[this.pos].s = s;
	}

	public void prepend(Element e) {
		this.ensure(++this.pos);
		children[this.pos].type = TELEM;
		children[this.pos].e = e;
	}
*/

	public String toString() {
		throw new OOFToStringException();
	}

	public String build() {
		return 
	}
};

protected class Container {
	public int type;
	public String s;
	public Element e;
};
