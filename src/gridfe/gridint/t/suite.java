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
import java.io.*;
import java.lang.ref.*;

public class suite
{
	public static void main(String[] args) throws Exception
	{

		/* GridInt Test Suite */
		GridInt gi = new GridInt(BasicServices.getUserID());
		gi.auth();
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

		/* Other Method tests */
//		System.out.println(gri.getJob().getID());
//		System.out.println(gri.getIDAsString());
//		System.out.println("Stdout: " + gri.getStdout());

		/* Create a new job */
		System.out.println("Creating New Job...");

		GridJob j = new GridJob("mugatu.psc.edu");
		j.setRSL(new String[] {"executable", "stdout"},
			new String[] {"/bin/sleep", "gram.out"},
			new String("arguments"),
			new String[] {"20s"});

		GridJob j2 = new GridJob("mugatu.psc.edu");
		j2.setRSL(new String[] {"executable"},
			new String[] {"/bin/sleep"},
			new String("arguments"),
			new String[] {"30s"});

		/* Submit the job to GRAM */
		System.out.println("Submiting Job...");
		gi.jobSubmit(j);
		gi.jobSubmit(j2);

		/* Test Serialization */
		System.out.println("Serializing Job...");
		FileOutputStream fout = new FileOutputStream("job.revive");
		ObjectOutputStream out = new ObjectOutputStream(fout);
		out.writeObject(gi);
		out.close();

		/* Implicitly get rid of this object! */
		System.out.println("Removing Instance of Job...");
		WeakReference r = new WeakReference(gi);
		r.clear();
		gi = null;
		j = null;
		j2 = null;

		/* Test Deserialization */
		System.out.println("Deserializing Job...");
		FileInputStream fin = new FileInputStream("job.revive");
		ObjectInputStream in = new ObjectInputStream(fin);

		gi = (GridInt)(in.readObject());

		in.close();
		gi.revive();

		/* 
		** Revival Test
		** make sure we can recreate GridJobs from stored values
		** and gss credentials. (this is actually a clone test now
		** that deserialization has been implemented)
		*/
		//GridJob jc = new GridJob();
		//jc.revive(j.getHost(), j.getIDAsString(), gi.getGSSAuth().getGSSCredential(), j.getRSL());

		do
		{
			System.out.println("J1: "+gi.getJobStatus()+" : "+gi.getJobStatusAsString());
			System.out.println("J2: "+gi.getJobStatus(1)+" : "+gi.getJobStatusAsString(1));
			//System.out.println("JC: "+j2.getStatus()+" : "+jc.getStatusAsString());
			Thread.sleep(600);

		}while(gi.getJobStatus() != -1 || gi.getJobStatus(1) != -1);

		System.out.println("J1: "+gi.getJobStatus()+" : "+gi.getJobStatusAsString());
		System.out.println("J2: "+gi.getJobStatus(1)+" : "+gi.getJobStatusAsString(1));
		//System.out.println("JC: "+j2.getStatus()+" : "+jc.getStatusAsString());
	}
}
