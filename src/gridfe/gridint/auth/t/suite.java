/* $Id$ */

import gridfe.*;
import gridfe.gridint.*;
import gridfe.gridint.auth.*;
import jasp.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.*;
import javax.security.auth.kerberos.*;
import javax.security.auth.Subject;

public class suite
{
	public static void main(String[] args) throws Exception
	{

		/* GridInt Test Suite */
		GridInt gi = new GridInt(args[0]);
		gi.authenticate();
		//System.out.println(gi.getIssuer());
		System.out.println(gi.getRemainingLifetime());
		System.out.println(gi.getGlobusAuth().getCredential().getPrivateKey());
		System.out.println(gi.getGlobusAuth().getSubject());

		/* GlobusAuth Test Suite */
		/*
		GlobusAuth ga = new GlobusAuth(BasicServices.getUserID());
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
		*/
		
		/* KerbAuth Test Suite */
		/*
		Subject sub;
		Set prin;
		Set priv;
		Set pub;
		Object[] o;
		//KerberosPrincipal[] p;
		KerberosPrincipal p;
		KerberosKey key;
		
		KerbAuth krb = new KerbAuth();
		krb.login();

		sub = krb.getSubject();
	
		System.out.println("---Subject----");
		System.out.print(sub.toString());
		System.out.println("\n---Principal----");
		prin = sub.getPrincipals();
		System.out.print(prin.toString());
		System.out.println("\n---Public Credentials----");
		pub = sub.getPublicCredentials();
		System.out.print(pub.toString());
		System.out.println("\n---Private Credentials----");
		priv = sub.getPrivateCredentials();
		System.out.println("Set size: "+priv.size());
		System.out.print(priv.toString());
		System.out.println("\n----Class Names &  Other---");
		System.out.println(prin.getClass().getName());
		System.out.print(sub.getPrivateCredentials().getClass().getName());
		System.out.print(prin.size());
		System.out.println("\n---Kerberos Principal----");
		//o = (KerberosPrincipal[])prin.toArray();
		o = (Object[])prin.toArray();
		p = (KerberosPrincipal)o[0];
		System.out.println(p);
		System.out.println(p.getRealm());
		System.out.println(p.getName());
		System.out.println(p.getNameType());
		System.out.println("\n---Debug----");
		System.out.println(priv.getClass());
		System.out.println(priv.getClass().getMethods());
		System.out.println(priv.getClass().getFields());
		System.out.println("Name: "+priv.getClass().getName());
		System.out.println(priv.getClass().getDeclaringClass().getName());
		System.out.println(priv.getClass().getDeclaringClass().getMethods());
		System.out.println(priv.getClass().getSuperclass());
		System.out.println(Class.forName("javax.security.auth.Subject").getFields());

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
};;
