/* $Id$ */

package jasp;

import java.io.*;
import java.util.*;

public class GetPWEnt {
	public String pw_name;
	public int pw_uid;
	public int pw_gid;
	public String pw_dir;
	public String pw_shell;
	public int pw_fields;

	/* It is sad that there is no way to do this in J2SE. */
	public GetPWEnt(String username) {
		try {
			String line;
			String[] fields;
			File f = new File("/etc/passwd");
			BufferedReader r = new BufferedReader(new
			    FileReader(f));
			while ((line = r.readLine()) != null) {
				fields = line.split(":");
				if (fields.length > 0 &&
				    fields[0].equals(username)) {
					this.pw_uid = Integer.valueOf(fields[2]).intValue();
					this.pw_gid = Integer.valueOf(fields[3]).intValue();
					this.pw_dir = fields[5];
					this.pw_shell = fields[6];
					break;
				}
			}
			r.close();
			this.pw_name = username;
		} catch (Exception e) {
		}
	}
};
