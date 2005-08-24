/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import gridfe.gridint.*;
import javax.servlet.http.*;
import oof.*;

public class submit {
	public static String main(Page p)
	  throws Exception {
		HttpServletRequest req = p.getRequest();
		String rlsout = null;
		String errmsg = null;

		String label = req.getParameter("label");
		String host = req.getParameter("host");
		String localexec = req.getParameter("localexec");
		String remoteexec = req.getParameter("remoteexec");
		String args = req.getParameter("args");
		String dir = req.getParameter("dir");
		String stdout = req.getParameter("stdout");
		String stderr = req.getParameter("stderr");

		if (label == null)
			label = "";
		if (host == null)
			host = "";
		if (localexec == null)
			localexec = "";
		if (remoteexec == null)
			remoteexec = "";
		if (args == null)
			args = "";
		if (dir == null)
		if (dir == null)
			stdout = "";
		if (stderr == null)
			stderr = "";

		if (req.getParameter("submitted") != null) {
			if (host.equals("") || label.equals("") ||
			  (remoteexec.equals("") && localexec.equals("")))
				errmsg = "Please specify all required form fields.";
			if (errmsg == null) {
				GridJob j = new GridJob(host);
				GridInt gi = p.getGridInt();
				j.setName(label);

				int n = 1; /* exec */
				if (!dir.equals(""))
					n++;
				if (!stdout.equals(""))
					n++;
				if (!stderr.equals(""))
					n++;
				if (!args.equals(""))
					n++;
				String[] r_keys = new String[n];
				String[] r_vals = new String[n];
				n = 0;
				r_keys[n] = "executable";
				r_vals[n] = remoteexec;
				n++;
				if (!args.equals("")) {
					r_keys[n] = "arguments";
					r_vals[n] = args;
					n++;
				}
				if (!dir.equals("")) {
					r_keys[n] = "directory";
					r_vals[n] = dir;
					n++;
				}
				if (!stdout.equals("")) {
					r_keys[n] = "stdout";
					r_vals[n] = stdout;
					n++;
				}
				if (!stderr.equals("")) {
					r_keys[n] = "stderr";
					r_vals[n] = stderr;
					n++;
				}
				j.setRSL(r_keys, r_vals);

				if (req.getParameter("submitted").equals("View RSL For This Submission"))
					rlsout = j.toString();
				else {
					gi.jobSubmit(j);

					String s = "";
					s += p.header("Submitted Job")
					   + p.getOOF().p("The job has been submitted successfully.")
					   + p.footer();
					return (s);
				}
			}
		}

		String s = "";
		OOF oof = p.getOOF();

		String hostchgjs =
			"document.forms[0].elements['host'].value = " +
			"	(this.options[this.selectedIndex].value == 'Choose a host...') ? " +
			"	'' : this.options[this.selectedIndex].value ";

		s += p.header("Submit Job")
		   + oof.p("You can fill out the fields below and press the submit "
		   +   "button to send a job to another machine.  Afterwards, you can "
		   +   "navigate the menu on the left side of the page to view status "
		   +   "information about the job and retrieve any output from the job "
		   +   "once it is completed.");
		if (errmsg != null)
			s += oof.p(new Object[] { "class", "err" },
			  "" + oof.strong("An error has occurred while processing your submission: ") +
			  errmsg);
		s += oof.form(
				new Object[] {
					"action", "submit",
					"method", "POST",
					"enctype", "application/x-www-form-urlencoded"
				},
				new Object[] {
					oof.table(
						new Object[] {
							"class", p.CCTBL,
							"border", "0",
							"cellspacing", "0",
							"cellpadding", "0"
						},
						new Object[][][] {
							new Object[][] {
								new Object[] {
									"class", p.CCHDR,
									"value", "Submit Job",
									"colspan", "2"
								}
							},
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
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
										"&raquo; This field should contain a label " +
										"that serves as a mnemonic to you so that " +
										"you can later quickly identify the job."
								}
							},
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
									"value", "Target host:"
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
											"onchange", hostchgjs,
											"options", new Object[] {
												"", "Choose a host...",
												"test", "testhost"
											}
										}) +
										oof.br() +
										"&raquo; This field should contain the host name " +
										"of the target machine on which you would " +
										"your job to run.  You may select a previously " +
										"configured host from the drop-down box on the " +
										"right, which may be done through the " +
										oof.link("Node Availibility", p.buildURL("/nodes")) +
										" page."
								}
							},
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
									"value", "Executable program:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "Choose a local program: " +
										oof.input(new Object[] {
											"type", "file",
											"name", "localexec"
										}) +
										oof.br() +
										"Or enter remote program path: " +
										oof.input(new Object[] {
											"type", "text",
											"value", p.escapeHTML(remoteexec),
											"name", "remoteexec"
										})
/*
										[x] Relative to my home directory
*/
								}
							},
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
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
										"the program can be placed here."
								}
							},
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
									"value", "Remote directory:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
//											"value", p.escapeHTML(dir),
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
									"class", p.CCDESC,
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
										oof.link("Job Status", p.buildURL("/jobs/status")) +
										" page." +
										oof.br() +
										oof.br() +
										"Leaving this field blank will result in no output " +
										"being saved."
/*
										checkbox
										[x] Download to local machine?
										[x] Display output on retrieval page.
*/
								}
							},
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
									"value", "Remote standard error file:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "text",
//											"value", p.escapeHTML(stderr),
											"name", "stderr"
										}) +
										oof.br() +
										"&raquo; This specifies the file name that will " +
										"contain any error output produced by your job, and it " +
										"will be saved on the machine where you chose to " +
										"run your job." +
										oof.br() +
										oof.br() +
										"Leaving this field blank will result in no output " +
										"being saved."
/*
										checkbox
										[x] Download to local machine?
										[x] Display output on retrieval page.
*/
								}
							},
							new Object[][] {
								new Object[] {
									"colspan", "2",
									"class", p.CCTBLFTR,
									"value", "" +
										oof.input(new Object[] {
											"type", "submit",
											"name", "submitted",
											"class", "button",
											"value", "View RSL For This Submission"
										}) +
										oof.input(new Object[] {
											"type", "submit",
											"name", "submitted",
											"class", "button",
											"value", "Submit Job"
										}) +
										oof.input(new Object[] {
											"type", "reset",
											"class", "button",
											"value", "Reset Fields"
										})
								}
							}
						}
					)
				}
			 );

		if (rlsout != null) {
			s += ""
			   + oof.p("The RLS output for this job:")
			   + oof.pre(rlsout);
		}
		s += p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
