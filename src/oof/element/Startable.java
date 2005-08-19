/* $Id$ */

package oof.element;

import java.util.*;

public interface Startable {
	public void addAttribute(String key, String val);
	public Object removeAttribute(String key);
	public String getAttribute(String key);
	public LinkedList getAttributes();
	public String toString();
};
