/* $Id$ */

import gridfe.*;
import gridfe.gridint.*;
import gridfe.gridint.auth.*;
import jasp.*;
import java.io.*;
import java.lang.Integer;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.*;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.Subject;

public class suite
{
	public static void main(String[] args) throws Exception
	{

		/* GridInt Test Suite */
		//GridInt gi = new GridInt(BasicServices.getUserID());
		GridInt gi = new GridInt(6342);
		gi.auth();
		System.out.print("Remaining Lifetime: ");
		System.out.println(gi.getRemainingLifetime());
//		System.out.println(gi.getGlobusAuth().getCredential().getPrivateKey());
//		System.out.println(gi.getGlobusAuth().getSubject());
		System.out.println(gi.getName());

		/* Create a new job */
		System.out.println("Creating New Job...");

		GridJob j = new GridJob("mugatu.psc.edu");
		j.setRSL(new String[] {"executable", "stdout"},
			new String[] {"/bin/sleep", "gram.out"},
			new String("arguments"),
			new String[] {"10s"});

		GridJob j2 = new GridJob("mugatu.psc.edu");
		j2.setRSL(new String[] {"executable"},
			new String[] {"/bin/sleep"},
			new String("arguments"),
			new String[] {"15s"});

		/* test output permission */
		GridJob j3 = new GridJob("mugatu.psc.edu");
		j3.setRSL(new String[] {"executable", "stdout"},
			new String[] {"/bin/date", "/tmp/gram.out.date"});
		System.out.println(j3.toString());
		gi.jobSubmit(j3);

		/* Submit the job to GRAM */
		System.out.println("Submiting Job...");
		gi.jobSubmit(j);
		gi.jobSubmit(j2);

		/* Print the job id string */
		System.out.println("j - id string: "+j.getIDAsString());
		System.out.println("j2 - id string: "+j2.getIDAsString());

		/* Test Serialization */
		System.out.println("Serializing Job...");
		FileOutputStream fout = new FileOutputStream("job.revive");
		ObjectOutputStream out = new ObjectOutputStream(fout);
		out.writeObject(gi);
		out.close();

		/* Explicitly get rid of this object! */
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

		do
		{
			System.out.println("J1: "+gi.getJobStatus()+" : "+gi.getJobStatusAsString());
			System.out.println("J2: "+gi.getJobStatus(1)+" : "+gi.getJobStatusAsString(1));
			Thread.sleep(600);

		}while(gi.getJobStatus() != -1 || gi.getJobStatus(1) != -1);

		System.out.println("J1: "+gi.getJobStatus()+" : "+gi.getJobStatusAsString());
		System.out.println("J2: "+gi.getJobStatus(1)+" : "+gi.getJobStatusAsString(1));
	}
};
