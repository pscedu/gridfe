/* $Id$ */

package gridfe.gridint.auth;

import jasp.*;

/*
 * Class to encapsulate the location for the
 * X.509 certificates and Kerberos tickets.
 */
public class CertFile {
	public static int CF_REGCERT = (1<<1);
	/*
	 * Certificates are normally stored in /tmp/x509up_u$UID
	 *
	 * However, mod_fum creates others so they don't
	 * conflict with console login credentials that
	 * may exist already...  Use these when deployed.
	 */

	private String xfile;
	private String kfile;

	public CertFile(Uid uid, int flags) {
		if ((flags & CF_REGCERT) == CF_REGCERT) {
			this.xfile = "/tmp/x509up_u" + uid.intValue();
			this.kfile = "/tmp/krb5cc_"  + uid.intValue();
		} else {
			this.xfile = "/tmp/x509up_fum_u" + uid.intValue();
			this.kfile = "/tmp/krb5cc_fum_"  + uid.intValue();
		}
	}

	public String getX509() {
		return (this.xfile);
	}

	public String getKrbTkt() {
		return (this.kfile);
	}
};
