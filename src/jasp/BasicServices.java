/* $Id$ */

package jasp;

import java.io.*;
import java.util.*;

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

	public static String[] splitString(String hay, String needle) {
		LinkedList list = new LinkedList();

		int last = 0;
		for (int i = 0; i < hay.length() - needle.length(); i++)
			if (hay.substring(i, i + needle.length()).equals(needle)) {
				if (i > 0)
					list.add(hay.substring(last, i));
				i += needle.length() - 1;
				last = i + 1;
			}
		if (last < hay.length() - needle.length())
			list.add(hay.substring(last));

		String[] ret = new String[list.size()];
		for (int i = 0; i < list.size(); i++)
			ret[i] = (String)list.get(i);
		return (ret);
	}
};
