/* $Id$ */
package oof;

import oof.filter.*;

public class OOF {
	private Filter filter;
	private JASP jasp;

	OOF(JASP j, String filter) {
		this.jasp = j;
	}
};

public class OOFException extends Exception {
};

public class OOFToStringException extends OOFException {
};
