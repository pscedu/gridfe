/* $Id$ */
/* Authentication test suite. */

import gridint.auth.*;
import javax.security.auth.Subject;
import java.util.*;
import java.security.*;
import javax.security.auth.kerberos.KerberosKey;
import java.lang.reflect.*;
import java.lang.Integer;

public class suite
{
	public static void main(String[] args) throws Exception
	{

		/* GridInt Test Suite */
		GridInt gi = new GridInt(args[0]);
		gi.authenticate();
		System.out.println(gi.getIssuer());
		System.out.println(gi.getRemainingLifetime());
		System.out.println(gi.getGlobusAuth().getCredential().getPrivateKey());
		System.out.println(gi.getGlobusAuth().getSubject());
		

		/* GlobusAuth Test Suite */
/*
		GlobusAuth ga = new GlobusAuth(args[0]);
		ga.createCredential();

		System.out.print(ga.getPrivateKey());
		System.out.print("\n\n");
		System.out.println(ga.getSubject());
		System.out.print("\n\n");
		System.out.println(ga.getIssuer());
		System.out.print("\n\n");
		System.out.print(ga.getStrength());
		System.out.print("\n\n");
		System.out.print(ga.getCertNum());

		System.out.println(ga.getProxyType());
		//int i = ga.getProxyType();
*/
		
		/* KerbAuth Test Suite */
/*
		Subject sub;
		Set prin;
		Set priv;
		Set pub;
		KerberosKey key;
		KerbAuth krb = new KerbAuth();
		krb.login();

		sub = krb.getSubject();
	
		System.out.println("-------");
		System.out.print(sub.toString());
		System.out.println("\n-------");

		prin = sub.getPrincipals();

		System.out.print(prin.toString());
		System.out.println("\n-------");

		pub = sub.getPublicCredentials();
		System.out.print(pub.toString());
		System.out.println("\n-------");

		priv = sub.getPrivateCredentials();
		System.out.print(priv.toString());
		System.out.println("\n-------");

		System.out.print(prin.getClass().getName());
		System.out.print(sub.getPrivateCredentials().getClass().getName());
		System.out.println("\n-------");

		System.out.print(prin.size()+"\n");

		krb.logout();
*/

		/* X509Auth Test Suite */
/*		
		int uid = 6342;
		X509Auth krb = new X509Auth("/tmp/x509up_u" + uid);
		krb.instantiate();
	
		krb.checkValidity();
		
		System.out.println("X509 Certificate:");
		System.out.print(krb.getCertificate());
*/
	}
}
