/* $Id$ */

package jasp;

import java.io.*;

public class BasicServices {
	public static int getUserID() {
		try {
			return (getUserID(System.getProperty("user.name")));
		} catch (Exception e) {
			return (-1);
		}
	}

	/* It is sad that there is no way to do this in J2SE. */
	public static int getUserID(String username) {
		int uid = -1;

		try {
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
					uid = Integer.valueOf(
					    fields[2]).intValue();
					break;
				}
			}
			r.close();
		} catch (Exception e) {
			return (-1);
		}
		return (uid);
	}

	public static String lcfirst(String s) {
		return (s.substring(0, 1).toUpperCase() +
			s.substring(1));
	}

	public static String stripSpace(String s) {
		if (s == null)
			return (null);
		String t = "";
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) != ' ')
				t += s.charAt(i);
		return (t);
	}
};
