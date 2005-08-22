/* $Id$ */

package oof;

public class OOFBadElementFormException extends OOFException {
	private String tag;

	public OOFBadElementFormException(String tag) {
		super();
		this.tag = tag;
	}

	public String getMessage() {
		return this.tag + ": required attributes missing";
	}
}
