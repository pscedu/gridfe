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
import jasp.*;

public class suite
{
	public static void main(String[] args) throws Exception
	{

		/* GridInt Test Suite */
		GridInt gi = new GridInt(BasicServices.getUserID());
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

		/* 
		** Revival Test
		** make sure we can recreate GridJobs from stored values
		** and gss credentials.
		*/
		GridJob j2 = new GridJob();
		j2.revive(j.getHost(), j.getIDAsString(), gi.getGSSAuth().getGSSCredential(), j.getRSL());

		do
		{
			System.out.println("J: "+j.getStatus()+" : "+j.getStatusAsString());
			System.out.println("J2: "+j2.getStatus()+" : "+j2.getStatusAsString());
			Thread.sleep(800);

		}while(j.getStatus() != -1);
		System.out.println("J: "+j.getStatus()+" : "+j.getStatusAsString());
		System.out.println("J2: "+j2.getStatus()+" : "+j2.getStatusAsString());


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
