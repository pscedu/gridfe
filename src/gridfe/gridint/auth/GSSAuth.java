/* $Id$ */

package gridfe.gridint.auth;

import java.security.PrivateKey;
import org.globus.gsi.*;
import org.globus.gsi.gssapi.*;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl.*;
import org.ietf.jgss.*;
import org.ietf.jgss.GSSCredential.*;
import org.ietf.jgss.GSSException.*;

public class GSSAuth {
	private GlobusCredential gc = null;
	private GSSCredential gss = null;

	public GSSAuth(GlobusAuth ga) {
		this.gc = ga.getCredential();
	}

	public GSSAuth(GlobusCredential gc) {
		this.gc = gc;
	}

	public void createCredential()
	    throws GSSException {
		/*
		 * The following class does a conversion between
		 * GlobusCredential to GSSCredential.
		 */
		this.gss = new GlobusGSSCredentialImpl(this.gc,
		  GSSCredential.INITIATE_AND_ACCEPT);
	}

	/* Generic private data interfaces */
	public GlobusCredential getGlobusCredential() {
		return (this.gc);
	}

	public GSSCredential getGSSCredential() {
		return (this.gss);
	}

	/* GSSCredential Methods */
	public GSSName getName()
	    throws GSSException {
		return (this.gss.getName());
	}

	public int getRemainingLifetime()
	    throws GSSException {
		return (this.gss.getRemainingLifetime());
	}
};
