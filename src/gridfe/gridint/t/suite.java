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
//		System.out.println(gi.getGlobusAuth().getCredential().getPrivateKey());
		System.out.println(gi.getGlobusAuth().getSubject());
//		System.out.println(gi.getGSSAuth().getName());

		/* RSLElement Test */
//		RSLElement r;
//		r = new RSLElement(new String[] {"executable","stdout"},
//				new String[] {"/bin/date", "job-output.gram"},
//				new String("arguments"),
//				new String[] {"-arg1", "-arg2 test"});

//		System.out.println(r);

//		r = new RSLElement(new String[] {"executable","stdout"},
//				new String[] {"/bin/date", "job-output.gram"},
//				new String("environment"),
//				new String[] {"env1", "env2"},
//				new String[] {"value1", "value2"});

//		System.out.println(r);

//		r = new RSLElement(new String[] {"executable","stdout"},
//				new String[] {"/bin/date", "job-output.gram"},
//				new String("arguments"),
//				new String[] {"-arg1", "-arg2 test"},
//				new String("environment"),
//				new String[] {"env1", "env2"},
//				new String[] {"value1", "value2"});

//		System.out.println(r);

		/* Use RSLElement for GRAM test */
//		RSLElement rsl;
//		rsl = new RSLElement(new String[] {"executable","stdout", "stderr"},
//				new String[] {"/bin/uname", "/home/rbudden/job-output.gram", "/home/rbudden/job-err.gram"},
//				new String("arguments"),
//				new String[] {"-a"});

//		r = new RSLElement(new String[] {"executable", "stdout"}, new String[] {"/bin/sleep", "gram.out"},
//					new String("arguments"), new String[] {"10s"});

		GridJob j = new GridJob("mugatu.psc.edu");
		j.setRSL(new String[] {"executable", "stdout"},
			new String[] {"/bin/sleep", "gram.out"},
			new String("arguments"),
			new String[] {"10s"});

		gi.jobSubmit(j);

		do
		{
			System.out.println(j.getStatus()+" : "+j.getStatusAsString());
			Thread.sleep(900);

		}while(j.getStatus() != -1);
		System.out.println(j.getStatus()+" : "+j.getStatusAsString());


		/* Use GridInt to test GramInt */
//		GramInt gri = new GramInt(gi.getGSSAuth().getGSSCredential(), "mugatu.psc.edu");
//		System.out.println("RSL build: " + r);
//		gri.jobSubmit(r);

		/* check status */
//		do
//		{
//			System.out.println(gri.getStatusAsString());
//			System.out.println(gri.getStatus());
//			Thread.sleep(1000);

//		}while(gri.getStatus() != -1);
//		System.out.println(gri.getStatusAsString());
//		System.out.println(gri.getStatus());

		/* Other Method tests */
//		System.out.println(gri.getJob().getID());
//		System.out.println(gri.getIDAsString());
//		System.out.println("Stdout: " + gri.getStdout());
	}
}
