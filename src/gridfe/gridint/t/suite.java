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

		CertInfo ci;
		ci = gi.getCertInfo();

		System.out.print("Remaining Lifetime: ");
		System.out.println(ci.time);
		System.out.print("Subject: ");
		System.out.println(ci.sub);
		System.out.print("Identity: ");
		System.out.println(ci.ident);
		System.out.print("Issuer: ");
		System.out.println(ci.issuer);
		System.out.print("KeyStrength: ");
		System.out.println(ci.key);
		System.out.print("Name: ");
//		System.out.println(gi.getName());

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
		String j3_out = new String("/tmp/gram.out.date");
		String j3_host = new String("intel2.psc.edu");
		GridJob j3 = new GridJob(j3_host);
		j3.setRSL(new String[] {"executable", "stdout"},
			new String[] {"/bin/date", j3_out});
		System.out.println(j3.toString());

		/* Submit the job to GRAM */
		System.out.println("Submiting Job...");
		gi.jobSubmit(j);
		gi.jobSubmit(j2);
	//	gi.jobSubmit(j3);

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

		/* Monitor job status */
		do
		{
			System.out.println("J1: "+gi.getJobStatus()+" : "+gi.getJobStatusAsString());
			System.out.println("J2: "+gi.getJobStatus(1)+" : "+gi.getJobStatusAsString(1));
			Thread.sleep(600);

		}while(gi.getJobStatus() != -1 || gi.getJobStatus(1) != -1);

		System.out.println("J1: "+gi.getJobStatus()+" : "+gi.getJobStatusAsString());
		System.out.println("J2: "+gi.getJobStatus(1)+" : "+gi.getJobStatusAsString(1));

		/* Use a GassInt to grab job output */
	/*
		GassInt gass = new GassInt(gi.getCredential(), j3_host, 28003);
		System.out.println("Starting Remote Gass Server");
		gass.start();
		gass.start_remote();
		System.out.println("Attempting to open file: "+j3_out);
		gass.open(j3_out);
		System.out.println("Size: "+gass.getSize());
	*/
		/* Grab the job output */
	/*	System.out.println("Reading file...");
		String data = gass.read();
		System.out.println(data);

		gass.close();
		gass.shutdown();
		System.out.println("Gass Server shutdown");
	*/
		/* - this shows that the gass server does terminate properly...
		if(gass.shutdown())
			System.out.println("Shutdown completed");
		else
			System.out.println("Shutdown failed");
		*/

		/* 
		** Wow! this is the only way i can get the test suite to terminate
		** when using the GassInt code... seems like there is some kind of
		** thread still running that needs terminated!
		*/
		System.exit(0);
		return;
	}
};
