/* $Id$ */

package oof.element;

import java.util.*;

public interface Startable {
	public HashMap getAttributes();
	public Object getAttribute(String key);
	public Object removeAttribute(String key);
	public String toString();
	public void addAttribute(String key, Object val);
};
