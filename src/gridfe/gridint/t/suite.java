/* $Id$ */

import gridfe.*;
import gridfe.gridint.*;
import gridfe.gridint.auth.*;
import jasp.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;

public class suite {
	public static void main(String[] args)
	    throws Exception {
		String thost = "gridfe.psc.edu/jobmanager-ben-pbs";
//		String thost = "gt4-submit.psc.teragrid.org/jobmanager-lemieux-pbs";
	    	String stdout_dir = "gram_jobs";

		GridInt gi = new GridInt(BasicServices.getUserID(), GridInt.GIF_REGCERT);
		gi.auth();

		CertInfo ci = gi.getCertInfo();

		long tmp;
		long sec = ci.time;
		long days = (sec / (tmp = 24*60*60));
		sec -= days * tmp;
		long hours = (sec / (tmp = 60*60));
		sec -= hours * tmp;
		long min = (sec / 60);
		sec -= min * 60;

		System.out.print("Remaining Lifetime: " + ci.time +
			" (" + days + " days, " + hours + " hours, " +
			min + " minutes, " + sec + " Seconds)\n" +
			"Subject: " + ci.sub + "\n" +
			"Identity: " + ci.ident + "\n" +
			"Issuer: " + ci.issuer + "\n" +
			"KeyStrength: " + ci.key + "\n");

		/* Create a new job */
		System.out.println("Creating jobs...");

		HashMap m;
		GridJob j1 = new GridJob(thost);
		j1.setName("J1");
		m = j1.getMap();
		m.put("executable", "/bin/sleep");
		m.put("stdout", "gram.out");
		m.put("arguments", new String[] { "2" } );

		GridJob j2 = new GridJob(thost);
		j2.setName("J2");
		m = j2.getMap();
		m.put("executable", "/bin/sleep");
		m.put("arguments", new String[] { "5" } );

		GridJob j3 = new GridJob(thost);
		j3.setName("J3.mkdir");
		m = j3.getMap();
		m.put("executable", "/bin/mkdir");
		m.put("arguments", new String[] { "-p", stdout_dir });

		/* Job to test output permission */
		GridJob j4 = new GridJob(thost);
		j4.setName("Date");
		m = j4.getMap();
		m.put("executable", "/bin/date");
		m.put("directory", stdout_dir);
		m.put("stdout", "gram.out.date");
		m.put("stderr", "gram.err.date");

		/* Submit the job to GRAM */
		System.out.println("RSL: " + j4);
		System.out.println("Submitting jobs...");
		gi.jobSubmit(j3); /* try to mkdir first */
		gi.jobSubmit(j1);
		gi.jobSubmit(j2);
		gi.jobSubmit(j4);

		int qid1 = j1.getQID();
		int qid2 = j2.getQID();
		int qid4 = j4.getQID();

		/* Print the job id string */
		System.out.println("j1 - qid: " + qid1 + "; id string: " + j1.getIDAsString());
		System.out.println("j2 - qid: " + qid2 + "; id string: " + j2.getIDAsString());
		System.out.println("j4 - qid: " + qid4 + "; id string: " + j4.getIDAsString());

		/* Test Serialization */
		System.out.println("Serializing gridint...");
		FileOutputStream fout = new FileOutputStream("job.revive");
		ObjectOutputStream out = new ObjectOutputStream(fout);
		out.writeObject(gi);
		out.close();

		/* Explicitly get rid of this object! */
		System.out.println("Removing instance of job...");
		WeakReference r = new WeakReference(gi);
		r.clear();
		gi = null;
		j1 = j2 = j4 = null;

		/* Test deserialization */
		System.out.println("Deserializing gridint...");
		FileInputStream fin = new FileInputStream("job.revive");
		ObjectInputStream in = new ObjectInputStream(fin);
		gi = (GridInt)in.readObject();
		in.close();

		/* Monitor job status */
		GridJob jj1, jj2;
		do {
			jj1 = gi.getJob(qid1);
			jj2 = gi.getJob(qid2);
			System.out.print(
				"J1: " + jj1.getStatus() + ": " + jj1.getStatusAsString() + "\n" +
				"J2: " + jj2.getStatus() + ": " + jj2.getStatusAsString() + "\n");
			Thread.sleep(600);
		} while (jj1.getStatus() != -1 || jj2.getStatus() != -1);

		/* Use a GassInt to grab job output */

		System.out.println("Retrieving job data...");
		j4 = gi.getJob(qid4);
		System.out.println(j4);

		/* Data retrieval (read a few chunks, then the rest */
		String[] data = { "", "" };
		String[] file = { j4.getStdout(), j4.getStderr() };
		for (int i = 0; i < 2; i++) {
System.out.println("retrieve #" + i + ": " + file[i]);
			if (file[i] == null)
				continue;

			int tlen = 32;
			int toff = 0;

			try {
				/* Start with port range or specific port */
				gi.startRetrieve(j4, file[i], 28000, 28255);
//				gi.startRetrieve(j4, file[i], 28001);

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

		System.out.println("stderr: " + file[1] + ": " + data[1]);
		System.out.println("stdout: " + file[0] + ": " + data[0]);

		/* Get the job list and len */
		List jl = gi.getJobList().getList();
		for (int i = 0; i < jl.size(); i++) {
			GridJob job = (GridJob)jl.get(i);
			System.out.println(
				"Name: " + job.getName() + "\n" +
				"\tStatus: " + job.getStatusAsString() + "\n" +
				"\tRSL: " + job);
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

		UserMap um = new UserMap();
		String id = "yanovich";

		System.out.println("Kerberos ID: " + id);
		System.out.println("System UID: " + um.kerberosToSystem(id));

		/*
		 * XXX this seems like the only way the test suite will terminate
		 * when using the GassInt code.  Seems like there is some kind of
		 * thread still running that needs terminated.
		 */
		System.exit(0);
	}
};
