/* $Id$ */
/*
** file: GridInt.java
** purpose: Wrapper around authentication to kerberos
**		and grid computers...
*/

package gridint.auth;

//import com.sun.security.auth.module.Krb5LoginModule;
import javax.security.auth.*;
import javax.security.auth.Subject.*;
import javax.security.auth.login.*;
import java.lang.Object.*;
import java.security.*;
//import java.security.cert.X509Certificate;

/*
public class GridInt
{
	private String user;
	private String realm;

	GridInt()
	{
	}
}
*/

public class GridInt
{
	public static void main(String[] args)
	{
		try
		{
			KerbInt krb = new KerbInt();
			krb.login();
	
			System.out.println("-------");
			krb.logout();
	
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
}


