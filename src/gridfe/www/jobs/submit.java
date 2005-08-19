/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import gridfe.gridint.*;
import javax.servlet.http.*;
import oof.*;

public class submit {
	public static String main(Page p)
	  throws Exception {
		HttpServletRequest req;
		String errmsg = null;

		req = p.getRequest();
		if (req.getParameter("submitted") != null) {
			String label, host, args, exec, stdout;

			label = req.getParameter("label");
			host = req.getParameter("host");
			args = req.getParameter("args");
			exec = req.getParameter("exec");
			stdout = req.getParameter("stdout");

			if (label == null || host == null || args == null ||
			  exec == null || stdout == null || host.equals("") ||
			  label.equals("") || exec.equals(""))
				errmsg = "Please specify all required form fields.";
			if (errmsg == null) {
System.out.println("GridJob()");
				GridJob j = new GridJob(host);
System.out.println("GridInt()");
				GridInt gi = p.getGridInt();
System.out.println("job.set()");
				j.setName(label);
				j.setRSL(
					new String[] { "executable", "arguments", "stdout" },
					new String[] { exec, args, stdout });
//				j.run();
System.out.println("job.submit()");
				gi.jobSubmit(j);
System.out.println("job.submit() returned");

				String s = "";
				s += p.header("Submitted Job")
				   + p.getOOF().p("The job has been submitted successfully.")
				   + p.footer();
				return (s);
			}
		}
		return (form(p, errmsg));
	}

	public static String form(Page p, String errmsg)
	  throws Exception {
		String s = "";
		OOF oof = p.getOOF();

		s += p.header("Submit Job")
		   + oof.p("You can fill out the fields below and press the submit "
		   +   "button to send a job to another machine.  Aftewards, you can "
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
											"name", "label"
										}) +
										oof.br() +
										"This field should contain a label " +
										"that serves as a mnemonic to you so that " +
										"you can later quickly access the job."
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
											"name", "host"
										}) +
										oof.input(new Object[] {
											"type", "select",
											"options", "Choose a host..."
										}) +
										oof.br() +
										"This fields should contain the host name " +
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
											"name", "args"
										}) +
										oof.br() +
										"Any optional command-line arguments to the " +
										"program can be placed here."
								}
							},
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
									"value", "Remote output file:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", "" +
										oof.input(new Object[] {
											"type", "textarea",
											"name", "stdout"
										}) +
										oof.br() +
										"This specifies the file name that will contain " +
										"any output produced by your job, and it will saved " +
										"on the machine where you chose to run your job.  " +
										"The contents may, however, be displayed or saved " +
										"to your local computer from the " +
										oof.link("Job Output", p.buildURL("/jobs/output")) +
										" page."
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
											"value", "Clear Fields"
										})
								}
							}
						}
					)
				}
			 )
		   + p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
