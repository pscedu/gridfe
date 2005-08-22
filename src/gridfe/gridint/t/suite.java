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
		GridInt gi = new GridInt(BasicServices.getUserID());
		gi.auth();

		CertInfo ci;
		ci = gi.getCertInfo();

		long tmp;
		long sec = ci.time;
		long days = (sec / (tmp = 24*60*60));
		sec -= days * tmp;
		long hours = (sec / (tmp = 60*60));
		sec -= hours * tmp;
		long min = (sec / 60);
		sec -= min * 60;

		System.out.print("Remaining Lifetime: ");
		System.out.print(ci.time);

		System.out.print(" (" + days + "Days, ");
		System.out.print(hours + "Hours, ");
		System.out.print(min + "Minuets, ");
		System.out.println( sec + "Seconds)");

		System.out.print("Subject: ");
		System.out.println(ci.sub);
		System.out.print("Identity: ");
		System.out.println(ci.ident);
		System.out.print("Issuer: ");
		System.out.println(ci.issuer);
		System.out.print("KeyStrength: ");
		System.out.println(ci.key);
//		System.out.print("Name: ");
//		System.out.println(gi.getName());

		/* Create a new job */
		System.out.println("Creating New Job...");

		GridJob j = new GridJob("intel2.psc.edu");
//		GridJob j = new GridJob("mugatu.psc.edu");
//		GridJob j = new GridJob("gridinfo.psc.edu");
		j.setRSL(new String[] {"executable", "stdout"},
			new String[] {"/bin/sleep", "gram.out"},
			new String("arguments"),
//			new String[] {"10s"});
			new String[] {"2s"});
		j.setName("J1");

//		GridJob j2 = new GridJob("mugatu.psc.edu");
//		GridJob j2 = new GridJob("gridinfo.psc.edu");
		GridJob j2 = new GridJob("intel2.psc.edu");
		j2.setRSL(new String[] {"executable"},
			new String[] {"/bin/sleep"},
			new String("arguments"),
//			new String[] {"15s"});
			new String[] {"5s"});
		j2.setName("J2");

		/* job to test output permission */
		String j3_out = "gram.out.date";
//		String j3_out = "/tmp/gram.out.date";
		String j3_err = "gram.err";
		String j3_host = "intel2.psc.edu";
//		String j3_host = "mugatu.psc.edu";
		String j3_name = "Date";
		int j3_port = 28003;
		GridJob j3 = new GridJob(j3_host);
		j3.setRSL(new String[] {"executable", "directory", "stdout"},
			new String[] {"/bin/date", "gram_jobs", j3_out});
//		j3.setRSL(new String[] {"executable", "stdout", "stderr"},
//			new String[] {"/bin/date", j3_out, j3_err});
//		j3.setRSL(new String[] {"executable", "stdout", "directory", "stderr"},
//			new String[] {"/bin/date", j3_out, "gram_jobs", j3_err});
		j3.setName(j3_name);

		/* Submit the job to GRAM */
		System.out.println("RSL: " + j3);
		System.out.println("Submiting Job...");
		gi.jobSubmit(j);
		gi.jobSubmit(j2);
		gi.jobSubmit(j3);

		/* Print the job id string */
		System.out.println("j - id string: "+j.getIDAsString());
		System.out.println("j2 - id string: "+j2.getIDAsString());
		System.out.println("j3 - id string: "+j3.getIDAsString());

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
		j3 = null;

		/* Test Deserialization */
		System.out.println("Deserializing Job...");
		FileInputStream fin = new FileInputStream("job.revive");
		ObjectInputStream in = new ObjectInputStream(fin);

		gi = (GridInt)(in.readObject());

		in.close();

		/* Monitor job status */
		do {
			System.out.print("J1: "+gi.getJob("J1").getStatus());
			System.out.println(" : "+gi.getJob(0).getStatusAsString());
			System.out.print("J2: "+gi.getJob("J2").getStatus());
			System.out.println(" : "+gi.getJob(1).getStatusAsString());
			Thread.sleep(600);
		} while (gi.getJob(0).getStatus() != -1 ||
		    gi.getJob(1).getStatus() != -1);

		System.out.print("J1: "+gi.getJob("J1").getStatus());
		System.out.println(" : "+gi.getJob(0).getStatusAsString());
		System.out.print("J2: "+gi.getJob("J2").getStatus());
		System.out.println(" : "+gi.getJob(1).getStatusAsString());

		/* Use a GassInt to grab job output */

		System.out.println("Retrieving job data...");
//		j3 = gi.getJob(2);
		j3 = gi.getJob(j3_name);
		System.out.println(j3);

		/* Data Retrieval (Read a few chunks, then the rest */
	/* Data Retrieval only works on localhost for now, so disable */
	int ret = 1;
	if(ret == 1)
	{
		String[] data = {"", ""};
		String[] file = {j3.stdout, j3.stderr};
		for (int i = 0; i < 2; i++) {
			int tlen = 32;
			int toff = 0;

			try {
				/* Start with port range or specific port */
				gi.startRetrieve(j3, file[i], 28000, 28255);
//				gi.startRetrieve(j3, file[i], 28001);

				data[i] += gi.retrieve(tlen, toff);
                        	toff += tlen;
                        	data[i] += gi.retrieve(tlen, toff);
                        	toff += tlen;
                        	data[i] += gi.retrieve(0, toff);
				
				gi.stopRetrieve();
			} catch (Exception e) {
				data[i] += e.getMessage();
				gi.stopRetrieve();
			}
		}

		System.out.println("stderr: "+data[1]);
		System.out.println("stdout: "+data[0]);
	}

		/* Get the job list and len */
		JobList jl = gi.getJobList();
		for (int i = 0; i < jl.size(); i++) {
			GridJob job = jl.get(i);
			System.out.print("Name: "+job.getName());
			System.out.print("\tStatus: "+job.getStatusAsString());
			System.out.println("\tRSL: "+job);
		}

		/* Logout - remove credentials */
		//gi.logout();
		//gi.logout("job.revive");

		// this shows that the gass server does terminate properly...
/*
		if(gass.shutdown())
			System.out.println("Shutdown completed");
		else
			System.out.println("Shutdown failed");
*/

		System.out.println("\n");

		UserMap m = new UserMap();
		String id = "yanovich";

		System.out.println("Kerberos ID: " + id);
		System.out.println("System UID: " + m.kerberosToSystem(id));

		/*
		 * XXX this seems like the only way the test suite will terminate
		 * when using the GassInt code.  Seems like there is some kind of
		 * thread still running that needs terminated.
		 */
		System.exit(0);
		return;
	}
};
