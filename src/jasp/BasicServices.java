/* $Id$ */

package jasp;

import java.io.*;

public class BasicServices {
	/* It is sad that there is no way to do this in J2SE. */
	public static int getUserID() {
		int uid = -1;
		String username;

		try {
			username = System.getProperty("user.name");

			/*
			 * An equivalent to getpwent() might be
			 * better here...
			 */
			String line;
			String[] fields;
			File f = new File("/etc/passwd");
			BufferedReader r = new BufferedReader(new
						FileReader(f));
			while ((line = r.readLine()) != null) {
				fields = line.split(":");
				if (fields.length > 0 &&
				    fields[0].equals(username)) {
					uid = Integer.getInteger(
						fields[2]).intValue();
					break;
				}
			}
			r.close();
		} catch (Exception e) {
			return -1;
		}

		return uid;
	}
}
