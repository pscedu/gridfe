/* $Id$ */

import jasp.*;

public class suite {
	public static void main(String[] args) {
		System.out.println("User ID: " + new Integer(
			BasicServices.getUserID()));
		String[] pieces = BasicServices.splitString("foo/bar/baz", "/");
		for (int i = 0; i < pieces.length; i++)
			System.out.println(i + ": " + pieces[i]);
	}
};
