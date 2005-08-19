/* $Id$ */

package oof.element;

public interface Element extends Startable, Endable {
	public void append(Object o);
	public void prepend(Object o);
	public String getValue();
};
