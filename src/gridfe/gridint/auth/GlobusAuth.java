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
	private String file;
	private Uid uid;

	/*
	** X.509 Standard for files /tmp/x509up_u!!!
	** where !!! is the userid
	*/
	private final String def = "/tmp/x509up_u";
	public GlobusAuth(Uid uid)
	{
		this.file = this.def + uid.intValue();
	}

	/* Grab the credential from the file */
	public void createCredential()
		throws GlobusCredentialException
	{
		this.gc = new GlobusCredential(file);
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
