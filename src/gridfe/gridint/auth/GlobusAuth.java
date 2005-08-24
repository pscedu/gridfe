/* $Id$ */

package gridfe.gridint.auth;

import gridfe.gridint.*;
import jasp.*;
import java.security.PrivateKey;
import org.globus.gsi.*;
import org.globus.gsi.gssapi.*;
import org.ietf.jgss.*;

public class GlobusAuth {
	public static final int GAF_REGCERT = (1<<1);

	private GlobusCredential gc = null;
	private Uid uid;
	private CertFile file;
	int flags;

	public GlobusAuth(Uid uid, int flags) {
		int cf_flags = 0;

		this.flags = flags;
		if ((flags & GAF_REGCERT) == GAF_REGCERT)
			cf_flags |= CertFile.CF_REGCERT;
		this.file = new CertFile(uid, flags);
	}

	/* Grab the credential from the file */
	public void createCredential()
	    throws GlobusCredentialException {
		this.gc = new GlobusCredential(this.file.getX509());
	}

	public GlobusCredential getCredential() {
		return (this.gc);
	}

	/* Certificate Information Record */
	public CertInfo getCertInfo() {
		CertInfo ci =
		new CertInfo(this.gc.getSubject(), this.gc.getProxyType(),
		    this.gc.getIssuer(), this.gc.getStrength(),
		    this.gc.getIdentity(), this.gc.getTimeLeft(),
		    this.file.getX509(), this.file.getKrbTkt());
		return (ci);
	}
};
