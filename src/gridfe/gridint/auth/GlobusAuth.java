/* $Id$ */

package gridfe.gridint.auth;

import jasp.Uid;
import java.security.PrivateKey;
import org.globus.gsi.*;
import org.globus.gsi.gssapi.*;
import org.ietf.jgss.*;
import gridfe.gridint.*;

public class GlobusAuth
{
	private GlobusCredential gc = null;
	private Uid uid;
	private CertFile file;

	public GlobusAuth(Uid uid)
	{
		this.file = new CertFile(uid);
	}

	/* Grab the credential from the file */
	public void createCredential()
		throws GlobusCredentialException
	{
		this.gc = new GlobusCredential(this.file.getX509());
	}

	public GlobusCredential getCredential()
	{
		return this.gc;
	}

	/* Certificate Information Record */
	public CertInfo getCertInfo()
	{
		CertInfo ci = 
		new CertInfo( this.gc.getSubject(),
				this.gc.getProxyType(),
				this.gc.getIssuer(),
				this.gc.getStrength(),
				this.gc.getIdentity(),
				this.gc.getTimeLeft() );
		return ci;
	}
};
