/* $Id$ */

package oof.element;

public interface Element {
	public void append(Object o);
	public void prepend(Object o);
	public String toString();
	public String getValue();
	public void addAttribute(String key, String val);
	public String removeAttribute(String key);
	public String getAttribute(String key);
};
