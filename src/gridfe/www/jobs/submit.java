/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import gridfe.gridint.*;
import java.sql.*;
import java.util.*;
import javax.servlet.http.*;
import oof.*;

public class submit {
	public static String main(Page p)
	  throws Exception {
		HttpServletRequest req = p.getRequest();
		OOF oof = p.getOOF();
		String rslout = null;
		String errmsg = "";

		String label = req.getParameter("label");
		String host = req.getParameter("host");
		String remoteexec = req.getParameter("remoteexec");
		String args = req.getParameter("args");
		String dir = req.getParameter("dir");
		String stdout = req.getParameter("stdout");
		String stderr = req.getParameter("stderr");
		String mpi = req.getParameter("mpi");
		String queue = req.getParameter("queue");
		String addres = req.getParameter("addres");
		String maxtime = req.getParameter("maxtime");
		String nnodes = req.getParameter("nnodes");
		String nprocs = req.getParameter("nprocs");

		if (label == null)
			label = "";
		if (host == null)
			host = "";
		if (remoteexec == null)
			remoteexec = "";
		if (args == null)
			args = "";
		if (dir == null)
			dir = "";
		if (stdout == null)
			stdout = "";
		if (stderr == null)
			stderr = "";
		if (mpi == null)
			mpi = "";
		if (queue == null)
			queue = "";
		if (maxtime == null)
			maxtime = "";
		if (nnodes == null)
			nnodes = "";
		if (nprocs == null)
			nprocs = "";

		if (req.getParameter("submitted") != null) {
			if (host.equals("") || label.equals("") || remoteexec.equals(""))
				errmsg += "Please specify all required form fields.  ";
			if (!maxtime.equals("") && !maxtime.matches("^\\d+$"))
				errmsg += "Please enter a numerical value for maximum wall time.  ";
			if (!nnodes.equals("") && !nnodes.matches("^\\d+$"))
				errmsg += "Please enter a numerical value for number of nodes.  ";
			if (!nprocs.equals("") && !nprocs.matches("^\\d+$"))
				errmsg += "Please enter a numerical value for number of processes.  ";
			if (!nprocs.equals("") && !nnodes.equals("") &&
			  Integer.parseInt(nprocs) < Integer.parseInt(nnodes))
				errmsg += "Number of processes must be greater than number of nodes.  ";

			if (errmsg.equals("")) {
				GridJob j = new GridJob(host);
				GridInt gi = p.getGridInt();
				j.setName(label);

				HashMap m = j.getMap();
				m.put("executable", remoteexec);
				if (!args.equals(""))
					m.put("arguments", parse_args(args));
				if (!dir.equals(""))
					m.put("directory", dir);
				if (!stdout.equals(""))
					m.put("stdout", stdout);
				if (!stderr.equals(""))
					m.put("stderr", stderr);
				if (!mpi.equals(""))
					m.put("jobtype", "mpi");
				if (!queue.equals(""))
					m.put("queue", queue);
				if (!maxtime.equals(""))
					m.put("maxWallTime", maxtime);
				if (!nnodes.equals(""))
					m.put("hostCount", nnodes);
				if (!nprocs.equals(""))
					m.put("count", nprocs);

				if (req.getParameter("submitted").equals("View RSL"))
					rslout = j.toString();
				else if (req.getParameter("submitted").equals("Save RSL")) {
					HttpServletResponse res = p.getResponse();
					res.setContentType("application/octet-stream");
					res.setHeader("Content-disposition",
					    "attachment; filename=\"" +
						p.getJASP().escapeAttachName(label) + ".rsl\"");
					return (j.toString());
				} else {
					gi.jobSubmit(j);

					if (addres != null) {
						PreparedStatement sth = p.getDBH().prepareStatement(
							"	INSERT INTO hosts (		" +
							"		uid, host, type		" +
							"	) VALUES (				" +
							"		?, ?, ?				" +
							"	)						");
						sth.setInt(1, p.getUID());
						sth.setString(2, host);
						sth.setString(3, "other");
						sth.executeUpdate();
					}

					String cmd = "$ globus-job-submit " + p.escapeHTML(host);

					String rsl = j.extraRSL();
					if (rsl.length() > 0)
						cmd += " -x '" + p.escapeHTML(rsl) + "'";

					if (!queue.equals(""))
						cmd += " -queue " + p.escapeHTML(queue);
					if (!stdout.equals(""))
						cmd += " -stdout " + p.escapeHTML(stdout);
					if (!stderr.equals(""))
						cmd += " -stderr " + p.escapeHTML(stderr);

					cmd += p.escapeHTML(" " + remoteexec + " " + args);

					String s = "";
					s += p.header("Submitted Job")
					   + oof.p("The job has been submitted successfully.")
					   + oof.p("You may now view the " +
					       oof.link("job status", p.buildURL("/jobs/status")) +
						   " page to await the completion of this job.")
					   + oof.p("The equivalent command-line invocation of this " +
					       "job submission would have been:")
					   + oof.pre(new Object[] { "style", "white-space: normal" }, cmd)
					   + p.footer();
					return (s);
				}
			}
		}

		String s = "";

		String js_hostchg =
			"	document.forms[0].elements['host'].value = " +
			"		(this.options[this.selectedIndex].value == 'Choose a host...') ? " +
			"		'' : this.options[this.selectedIndex].value ";

		String js_submit =
			"	if (this.value == 'Submit Job') {	" +
			"		this.value = 'Please wait...';	" +
			"		return (true)					" +
			"	}";

		PreparedStatement sth = p.getDBH().prepareStatement(
			"	SELECT					" +
			"			COUNT(*) AS cnt	" +
			"	FROM					" +
			"			hosts			" +
			"	WHERE					" +
			"			uid = ?			");	/* 1 */
		sth.setInt(1, p.getUID());
		ResultSet rs = sth.executeQuery();

		int nhosts = 0;
		if (rs.next())
			nhosts = rs.getInt("cnt");

		sth = p.getDBH().prepareStatement(
			"	SELECT					" +
			"			host			" +
			"	FROM					" +
			"			hosts			" +
			"	WHERE					" +
			"			uid = ?			");	/* 1 */
		sth.setInt(1, p.getUID());
		rs = sth.executeQuery();

		Object[] hlist = new Object[2 * nhosts + 2];
		hlist[0] = "";
		hlist[1] = "Choose a resource";
		for (int i = 2; rs.next(); i += 2)
			hlist[i] = hlist[i + 1] = rs.getString("host");

		s += p.header("Submit Job")
		   + oof.p("You can fill out the fields below and press the submit "
		   +   "button to send a job to another machine.  Afterwards, you can "
		   +   "navigate the menu on the left side of the page to view status "
		   +   "information about the job and retrieve any output from the job "
		   +   "once it is completed.");
		if (!errmsg.equals(""))
			s += oof.p(new Object[] { "class", "err" },
			  "" + oof.strong("An error has occurred while processing your submission: ") +
			  errmsg);
		s += oof.form(
				new Object[] {
					"action", "submit",
					"method", "post",
					"enctype", "application/x-www-form-urlencoded"
				},
				new Object[] {
					oof.table(
						new Object[] {
							"class", Page.CCTBL,
							"border", "0",
							"cellspacing", "0",
							"cellpadding", "0"
						},
						new Object[][][] {
							new Object[][] {
								new Object[] {
									"class", Page.CCHDR,
									"value", "Submit Job",
									"colspan", "2"
								}
							},
							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Job name/label:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
									    oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(label),
											"name", "label"
										}) +
										oof.br() +
										"&raquo; This field serves as a mnemonic " +
										"to you so that you can quickly later " +
										"identify this job."
								}
							},
							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Target resource:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(host),
											"name", "host"
										}) +
										oof.input(new Object[] {
											"type", "select",
											"onchange", js_hostchg,
											"options", hlist
										}) +
										oof.br() +
										oof.input(new Object[] {
											"type", "checkbox",
											"name", "addres",
											"label", "Add this to my " +
											  oof.link("resource list", p.buildURL("/nodes")) + "."
										}) +
										oof.br() +
										oof.br() +
										"&raquo; This field specifies the resource host name " +
										"of the target machine where your job will run.  " +
										"You may select a previously configured host from " +
										"the drop-down box, which may be configured through the " +
										oof.link("node availability", p.buildURL("/nodes")) +
										" page."
								}
							},
							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Target queue:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(queue),
											"name", "queue"
										}) +
										oof.br() +
										"&raquo; This optional field may contain the PBS queue name."
								}
							},
							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Remote executable program:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(remoteexec),
											"name", "remoteexec"
										}) +
										oof.br() +
										"&raquo; This field specifies the path on the " +
										"remote machine to the executable that will be " +
										"run.  You may follow the steps on the " +
										oof.link("stage job", p.buildURL("/jobs/StageJob")) +
										" page to set up any necessary files on the remote " +
										"machine, including this executable."
								}
							},
							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Program arguments:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "textarea",
											"value", p.escapeHTML(args),
											"name", "args"
										}) +
										oof.br() +
										"&raquo; Any optional command-line arguments to " +
										"the program can be placed here, separated by space.  " +
										"If an argument need contain a space, the argument " +
										"may be surrounded by double quote characters (&quot;&quot;)."
								}
							},
							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Message passing interface:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "checkbox",
											"name", "mpi",
											"label", "This job uses MPI."
										})
								}
							},

							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Max wall time:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(maxtime),
											"name", "maxtime"
										}) +
										oof.br() +
										"&raquo; This optional field specifies the maximum " +
										"amount of time for which your job will run according " +
										"to total elasped time or &quot;wall&quot; clock time.  " +
										"This value must be specified in minutes."
								}
							},

							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Number of processes:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(nprocs),
											"name", "nprocs"
										}) +
										oof.br() +
										"&raquo; This optional field specifies the number of execution " +
										"processes required to run the job."
								}
							},

							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Number of nodes:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(nnodes),
											"name", "nnodes"
										}) +
										oof.br() +
										"&raquo; This optional field specifies the number of nodes " +
										"to distribute the execution processes across."
								}
							},

							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Remote directory:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(dir),
											"name", "dir"
										}) +
										oof.br() +
										"&raquo; This field specifies the directory out of " +
										"which your job will run.  If left unspecified, your " +
										"job will run out of your home directory.  Using this " +
										"parameter may simplify path names to important files, " +
										"since they can be accessed relative to this directory " +
										"on the remote machine."
								}
							},
							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Remote standard output file:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(stdout),
											"name", "stdout"
										}) +
										oof.br() +
										"&raquo; This specifies the file name that will " +
										"contain any output produced by your job, and it " +
										"will be saved on the machine where you chose to " +
										"run your job.  The contents may, however, be " +
										"displayed or saved to your local computer from the " +
										"job output page, which can be accessed through the " +
										oof.link("job status", p.buildURL("/jobs/status")) +
										" page." +
										oof.br() +
										oof.br() +
										"Leaving this field blank will result in no output " +
										"being saved."
								}
							},
							new Object[][] {
								new Object[] {
									"class", Page.CCDESC,
									"value", "Remote standard error file:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(stderr),
											"name", "stderr"
										}) +
										oof.br() +
										"&raquo; This specifies the file name that will " +
										"contain any error output produced by your job, and it " +
										"will be saved on the machine where you chose to " +
										"run your job." +
										oof.br() +
										oof.br() +
										"Leaving this field blank will result in no error output " +
										"being saved."
								}
							},
							new Object[][] {
								new Object[] {
									"colspan", "2",
									"class", Page.CCTBLFTR,
									"value", "" +
										oof.input(new Object[] {
											"type", "submit",
											"name", "submitted",
											"class", "button",
											"value", "View RSL"
										}) +
										oof.input(new Object[] {
											"type", "submit",
											"name", "submitted",
											"class", "button",
											"value", "Save RSL"
										}) +
										oof.input(new Object[] {
											"onclick", js_submit,
											"type", "submit",
											"name", "submitted",
											"class", "button",
											"value", "Submit Job"
										}) +
										oof.input(new Object[] {
											"type", "reset",
											"class", "button",
											"value", "Reset Fields",
											"onclick", "return (confirm('OK to reset all fields?'))"
										})
								}
							}
						}
					)
				}
			 );

		if (rslout != null) {
			s += ""
			   + oof.p("The RSL output for this job:")
			   + oof.pre(p.escapeHTML(rslout));
		}
		s += p.footer();
		return (s);
	}

	private static List parse_args(String args) {
		boolean dquot = false;
		boolean esc = false;
		List argv = new LinkedList();
		String arg = "";

		for (int k = 0; k < args.length(); k++) {
			char c = args.charAt(k);

			switch (c) {
			case '\\':
				esc = !esc;
				break;
			case '"':
				if (esc) {
					esc = false;
					arg += c;
				} else
					dquot = !dquot;
				break;
			case ' ':
				if (!dquot) {
					argv.add(arg);
					arg = "";
					break;
				}
				/* FALLTHROUGH */
			default:
				arg += c;
				break;
			}
		}
		argv.add(arg);

		return (argv);
	}
};

/* vim: set ts=4: */
