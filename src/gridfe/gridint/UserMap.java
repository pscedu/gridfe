/* $Id$ */

package gridfe.gridint;

import jasp.*;
import java.io.*;

public class UserMap {
	private static final String _PATH_MAPFILE =
	    "/etc/grid-security/grid-mapfile";

	public UserMap() {
	}

	public String kerberosToSystem(String prin) {
		try {
			String line;
			File f = new File(_PATH_MAPFILE);
			BufferedReader br =
			    new BufferedReader(new FileReader(f));
			while ((line = br.readLine()) != null) {
				DN dn = this.parseDN(line);
				String[] fields = dn.getDN().split("/");
				if (fields.length == 1)
					fields = dn.getDN().split(":");
				for (int j = 0; j < fields.length; j++) {
					String[] pair = fields[j].split("=");
					if (pair.length == 2 &&
					    pair[0].equals("USERID") &&
					    pair[1].equals(prin))
						return (dn.getUsername());
				}
			}
			br.close();
		} catch (Exception e) {
			return null;
		}
		return (null);
	}

	private DN parseDN(String line) {
		String dn, username;
		int i;
		char c;

		dn = "";
		username = "";
		for (i = 0; i < line.length(); i++) {
			c = line.charAt(i);

			switch (c) {
			case '"':
				for (; i < line.length(); i++) {
					c = line.charAt(i);
					if (c == '"')
						break;
					dn += c;
				}
				break;
			case ' ':
			case 't':
				/* Skip space. */
				for (; i < line.length(); i++) {
					c = line.charAt(i);
					if (c != ' ' && c != '\t')
						break;
				}
				for (; i < line.length(); i++) {
					c = line.charAt(i);
					if (c != '\n')
						username += c;
				}
				break;
			default:
				return (null);
			}
		}
		return (new DN(dn, username));
	}
};

class DN {
	private String dn;
	private String username;

	public DN(String dn, String username) {
		this.dn = dn;
		this.username = username;
	}

	public String getDN() {
		return (this.dn);
	}

	public String getUsername() {
		return (this.username);
	}
};
