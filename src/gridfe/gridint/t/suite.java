/* $Id$ */
/* Authentication test suite. */

import gridint.auth.*;
import gridint.*;
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
		System.out.print("Remaining Lifetime: ");
		System.out.println(gi.getRemainingLifetime());
		System.out.println(gi.getGlobusAuth().getCredential().getPrivateKey());
		System.out.println(gi.getGlobusAuth().getSubject());
		System.out.println(gi.getGSSAuth().getName());

		/* Use GridInt to test GramInt */
		GramInt gri = new GramInt(gi.getGSSAuth().getGSSCredential(), "mugatu.psc.edu");
		gri.jobRun("&(executable="+args[1]+")(stdout=/home/rbudden/gram-job-output.log)");
//		System.out.println("Status of `which hostname` on mugatu.psc.edu: "+gri.getStatusString());

		return ;
	}
}
