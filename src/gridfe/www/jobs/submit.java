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
			  exec == null || stdout == null)
				errmsg = "Please specify all parameters.";
			if (errmsg == null) {
				GridJob j = new GridJob(host);
				j.setName(label);
				j.setRSL(
					new String[] { "executable", "arguments", "stdout" },
					new String[] { exec, args, stdout });
				j.run();

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

		s += p.header("Submit Job");
		if (errmsg != null)
			s += oof.p(errmsg);
		s += oof.form(
				new Object[] {
					"action", "submit",
					"method", "GET",
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
									"value", "Label:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", oof.input(new Object[] {
												"type", "text",
												"name", "label"
											}) }
							},
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
									"value", "Host:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", oof.input(new Object[] {
												"type", "text",
												"name", "host"
											})
								}
							},
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
									"value", "Executable:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", oof.input(new Object[] {
												"type", "text",
												"name", "exec"
											})
								}
							},
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
									"value", "Arguments:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", oof.input(new Object[] {
												"type", "textarea",
												"name", "args"
											})
								}
							},
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
									"value", "Output File:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", oof.input(new Object[] {
												"type", "text",
												"name", "stdout"
											})
								}
							},
							new Object[][] {
								new Object[] {
									"colspan", "2",
									"class", p.CCTBLFTR,
									"value", oof.input(new Object[] {
												"type", "submit",
												"value", "Submit"
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
