/*
** file: GridInt.java
** purpose: Wrapper around authentication to kerberos
**		and grid computers...
*/

package gridint;

//import com.sun.security.auth.module.Krb5LoginModule;
//import javax.security.auth.Subject;
//import java.security.cert.X509Certificate;

public class gridint
{
	private String user;
	private String realm;

	gridint()
	{
	}
}

/*
** kerbint - handle the kerberos authentication
*/
public class kerbint extends gridint
{
	protected Subject subject;
	protected Principle principle;
	protected Object pbcred;
	protected Object prcred;
	private LoginContext krb5;

	public kerbint()
	{
		/*
		** Read the "krb5" configuration
		** entry from the JAAS file...
		*/
		krb5 = new LoginContext("krb5");	
	}

	void public kerblogin()
	{
		try
		{
			if(krb5.login())
			{
				subject = krb5.getSubject();
				principle = subject.getPrinciple();
				pbcred = subject.getPublicCredentials();
				prcred = subject.getPrivateCredentials();
			}
			else
				System.out.println("Blah");
		}
		catch(LoginException)
		{
			//throw KerbIntLoginException;
			System.out.println("Login Failed");
		}
	}

	void public kerblogout()
	{
		krb5.logout();	
	}


}
