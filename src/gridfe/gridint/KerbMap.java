/* $Id$ */

package gridint;

import jasp.*;

public class KerbMap {
	public KerbMap(HttpServletRequest req) {
		UserMap m = new UserMap();

		String kerbuser = req.getRemoteUser();
		String username = m.kerberosToSystem(kerbuser);
		return (BasicServices.getUserId(username));
	}
};
