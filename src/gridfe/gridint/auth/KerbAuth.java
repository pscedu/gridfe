/* $Id$ */
/*
 * file: GridInt.java
 * purpose: Wrapper around authentication to kerberos
 *		and grid computers...
 */

package gridint.auth;

import java.util.Set;
import javax.security.auth.*;
import javax.security.auth.login.*;

/*
 * KerbInt - handle the Kerberos authentication
 */
public class KerbAuth
{
	protected Subject subject;
	//protected Object principal;
	protected Set principal;
	protected Object pbcred;
	protected Object prcred;
	private LoginContext krb5;

	public KerbAuth()
		throws LoginException
	{
		/*
		 * Read the "krb5" configuration
		 * entry from the JAAS file...
		 */
		krb5 = new LoginContext("krb5");
	}

	public void login()
		throws LoginException
	{
		krb5.login();
		subject = krb5.getSubject();
		System.out.println(subject.toString());
		principal = subject.getPrincipals();
		pbcred = subject.getPublicCredentials();
		prcred = subject.getPrivateCredentials();
	}

	public void logout()
		throws LoginException
	{
		krb5.logout();
	}
};
