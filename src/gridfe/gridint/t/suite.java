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

		GridJob j;
		boolean deserialized = false;

		try
		{
			/* Test Deserialization */
			FileInputStream fin = new FileInputStream("job.revive");
			ObjectInputStream in = new ObjectInputStream(fin);

			System.out.println("Deserializing Job...");
			j = (GridJob)(in.readObject());

			in.close();
			deserialized = true;
			
			/* switch these to resubmit/revive job */
			j.revive(gi.getGSSAuth().getGSSCredential());
			//gi.jobSubmit(j);
		}
		catch(IOException e)
		{

			/* Create a new job if a previous one doesn't exist */
			System.out.println("Creating New Job...");

			j = new GridJob("mugatu.psc.edu");
			j.setRSL(new String[] {"executable", "stdout"},
				new String[] {"/bin/sleep", "gram.out"},
				new String("arguments"),
				new String[] {"30s"});

			gi.jobSubmit(j);
		}

		/* Test Serialization */
		FileOutputStream fout = new FileOutputStream("job.revive");
		ObjectOutputStream out = new ObjectOutputStream(fout);
		out.writeObject(j);
		out.close();

		/* 
		** Revival Test
		** make sure we can recreate GridJobs from stored values
		** and gss credentials. (this is actually a clone test now
		** that deserialization has been implemented)
		*/
		GridJob j2 = new GridJob();
		j2.revive(j.getHost(), j.getIDAsString(), gi.getGSSAuth().getGSSCredential(), j.getRSL());

		/* If running deserialized, the wait till finished */
		if(deserialized)
		{
			do
			{
				System.out.println("J: "+j.getStatus()+" : "+j.getStatusAsString());
				System.out.println("J2: "+j2.getStatus()+" : "+j2.getStatusAsString());
				Thread.sleep(800);
	
			}while(j.getStatus() != -1);
		}
		/* If running a new job, output a few, then quit to test deserialization */
		else
		{
			for(int i = 0; i < 5; i++)
			{
				System.out.println("J: "+j.getStatus()+" : "+j.getStatusAsString());
				System.out.println("J2: "+j2.getStatus()+" : "+j2.getStatusAsString());
				Thread.sleep(800);
			}
		}

		System.out.println("J: "+j.getStatus()+" : "+j.getStatusAsString());
		System.out.println("J2: "+j2.getStatus()+" : "+j2.getStatusAsString());
	}
}
