/* $Id$ */
/* Authentication test suite. */

package gridint.auth;

//import com.sun.security.auth.module.Krb5LoginModule;
import javax.security.auth.*;
import javax.security.auth.Subject.*;
import javax.security.auth.login.*;
import java.lang.Object.*;
import java.security.*;
//import java.security.cert.X509Certificate;

public class suite {
	public static void main(String[] args) {
		try {
			KerbInt krb = new KerbInt();
			krb.login();
	
			System.out.println("-------");
			krb.logout();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
