/* $Id$ */

package gridfe.www.gridftp;

import gridfe.*;
import gridfe.gridint.*;
import gridfe.www.gridftp.*;
import jasp.*;
import java.sql.*;
import java.util.*;
import javax.servlet.http.*;
import oof.*;

public class StageJob {
	public static void archive2host(GridInt gi, String archiver,
	  String host, String scwd, String dcwd, String file) throws Exception {
		String rlsout = null;
		String errmsg = null;
//		final String host = "ben.psc.edu";
		final String manager = "gridfe.psc.edu/jobmanager-ben-shell";
		final String prog = "/usr/psc/bin/far";
//		final String prog = "/usr/users/3/rbudden/far2";
//		final String tmp = "/usr/scratch/";
		String type = "";
		String user = "";
		String uid = "";
		int len = 0;
		String sfile = "";
		String dfile = "";

		//System.out.println("host"+host);

		//XXX - this is used elsewhere...
		/*
		** parse the password file and retrieve the
		** information needed to construct the path
		** to the user's scratch file.
		** i.e.
		** /usr/users/3/rbudden/ has the scratch dir
		** /usr/scratch/3/rbudden/
		*/

//		GetPWEnt pwd = new GetPWEnt(p.getUserName());
//		String[] fields = pwd.pw_dir.split("/");

		/*
		** This should submit a job to ben that
		** executes (using our modified fork) the
		** far command with the appropriate conditions.
		** i.e.
		** far store foo bar
		** far get foo bar
		*/
		GridJob j = new GridJob(manager);

//		if(submitted == far2host) {
			type += "get";
			sfile = scwd + "/" + file;
//			dfile = tmp + fields[2] + "/" + fields[3] + "/" + file;
			dfile = dcwd + "/" + file;
//		} else if(submitted == host2far) {
//			type += "store";
//			sfile = tmp + fields[1] + "/" + fields[2] + "/" + file;
//			dfile = file;
//		}

		/* XXX - be able to store/get an entire directory from FAR
		** using the rget and rstore commands ? */

		HashMap m = j.getMap();
		m.put("executable", prog);
		m.put("stdout", "stage-out.log");
		m.put("stderr", "stage-err.log");
		m.put("arguments", new String[] { type, sfile, dfile });

		System.out.println("Job RSL: "+j);

		gi.jobSubmit(j);
	}
}

/* vim: set ts=4: */
