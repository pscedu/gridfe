/* $Id$ */

package jasp;

import java.io.*;

public class Uid implements Serializable {
	private int uid;

	public Uid(int uid) {
		this.uid = uid;
	}

	public Uid(Integer uid) {
		this.uid = uid.intValue();
	}

	public Uid(String uid) {
		Integer i = new Integer(uid);
		this.uid = i.intValue();
	}

	public int intValue() {
		return this.uid;
	}
};
