/* $Id$ */

package jasp;

import java.io.*;
import java.util.*;

public class GetPWEnt {

//	struct passwd {
//	char    *pw_name;       /* user name */
//	char    *pw_passwd;     /* encrypted password */
//	uid_t   pw_uid;         /* user uid */
//	gid_t   pw_gid;         /* user gid */
//	time_t  pw_change;      /* password change time */
//	char    *pw_class;      /* user access class */
//	char    *pw_gecos;      /* Honeywell login info */
//	char    *pw_dir;        /* home directory */
//	char    *pw_shell;      /* default shell */
//	time_t  pw_expire;      /* account expiration */
//	int     pw_fields;      /* internal: fields filled in */
//	};

	public String pw_name;
//	public String pw_password;
	public int pw_uid;
	public int pw_gid;
//	public time_t pw_change;
//	public String pw_class;
//	public String pw_gecos;
	public String pw_dir;
	public String pw_shell;
//	public time_t  pw_expire;
	public int pw_fields;

	/* It is sad that there is no way to do this in J2SE. */
	public GetPWEnt(String username) {

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
				fields = splitString(line, ":");
				if (fields.length > 0 &&
				    fields[0].equals(username)) {
					this.uid = Integer.valueOf(fields[2]).intValue();
					this.gid = Integer.valueOf(fields[3]).intValue();
					this.pw_dir = fields[5];
					this.pw_shell = fields[6];
					break;
				}
			}
			r.close();

			this.pw_name = username;

		} catch (Exception e) {
			return (-1);
		}
	}
};
