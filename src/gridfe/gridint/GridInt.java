/*
** file: GridInt.java
** purpose: Wrapper around authentication to kerberos
**		and grid computers...
*/

package gridint;

import com.sun.security.auth.module.Krb5LoginModule;
//import java.security.cert.X509Certificate;

public class gridint
{
	private String user;
	private String realm;
	private String principle;

	gridint()
	{
		user = null;
		domain = null;
		principle = null;
	}

	gridint(String u, String r)
	{
		user = u;
		realm = r;
	}

}
