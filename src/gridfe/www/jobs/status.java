/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import gridfe.gridint.*;
import java.util.*;
import oof.*;
import org.globus.gram.*;

public class status {
	public static String main(Page p)
	  throws Exception {
		OOF oof = p.getOOF();
		String s = "";
		int i;

		GridInt gi = p.getGridInt();
		GridJob j;

		String js_toggle =
		"	var qid = this.form.elements['qid'];			" +
		"	if (qid) {										" +
		"		if (qid.length)								" +
		"			for (var i = 0; i < qid.length; i++)	" +
		"				qid[i].checked = !qid[i].checked;	" +
		"		else										" +
		"			qid.checked = !qid.checked;				" +
		"	}												";

		s += p.header("Job Status")
		  +  oof.p("This page contains the status information for any jobs that " +
		  		"jobs that you have submitted.  For completed jobs, you may follow " +
				"the link provided to the job output page to retrieve any output that " +
				"the job may have generated.")
		  +  oof.form_start(new Object[] {
				"action", p.buildURL("/jobs/remove")
			 })
		  +  oof.table_start(new Object[] {
			  "class", Page.CCTBL,
			  "border", "0",
			  "cellspacing", "0",
			  "cellpadding", "0" })
		  +		oof.table_row(new Object[][] {
		  			new Object[] {
						"class", Page.CCHDR,
						"colspan", "6",
						"value", "Job Status"
					}
		  		})
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", Page.CCSUBHDR, "value", "Name" },
					new Object[] { "class", Page.CCSUBHDR, "value", "Host" },
					new Object[] { "class", Page.CCSUBHDR, "value", "Status" },
					new Object[] { "class", Page.CCSUBHDR, "value", "Output" },
					new Object[] { "class", Page.CCSUBHDR, "value", "Cancel" },
					new Object[] { "class", Page.CCSUBHDR, "value", "Remove" }
				});

		List list = gi.getJobList().getList();
		for (i = 0; i < list.size(); i++) {
			j = (GridJob)list.get(i);
			String c = p.genClass();
			s += oof.table_row(new Object [][] {
				new Object[] { "class", c, "value", p.escapeHTML(j.getName()) },
				new Object[] { "class", c, "value", j.getHost() },
				new Object[] { "class", c, "value", j.getStatusAsString() },
				new Object[] { "class", c, "value", "" +
				  oof.link("View", p.buildURL("/jobs/output?qid=" + j.getQID())) +
				  " / " +
				  oof.link("Save", p.buildURL("/jobs/output?qid=" + j.getQID() + "&amp;act=save"))
				},
				new Object[] { "class", c, "value",
				  j.getStatus() == GramJob.STATUS_ACTIVE ? "" +
				  oof.link("Cancel",
				    p.buildURL("/jobs/cancel?qid=" + j.getQID())) : "N/A"
				},
				new Object[] { "class", c, "value",
					oof.input(new Object[] {
						"type", "checkbox",
						"name", "qid",
						"value", "" + j.getQID()
					})
				},
			});
		}
		if (i == 0)
			s += oof.table_row(new Object[][] {
				new Object[] {
					"class", "data1",
					"colspan", "6",
					"value", "No jobs currently in history/queue.  " +
						oof.link("Submit a new job.", p.buildURL("/jobs/submit"))
				}
			});
		s += ""
		  +  oof.table_row(new Object[][] {
				new Object[] {
					"class", "tblftr",
					"colspan", "6",
					"value",
					oof.input(new Object[] {
						"type", "submit",
						"class", "button",
						"value", "Remove Checked"
					}) + "" +
					oof.input(new Object[] {
						"type", "button",
						"class", "button",
						"value", "Toggle All",
						"onclick", js_toggle
					})
				}
			 })
		  +  oof.table_end()
		  +  oof.form_end()
		  +  p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
