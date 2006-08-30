/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import gridfe.gridint.*;
import java.text.*;
import java.util.*;
import javax.servlet.http.*;
import oof.*;
import org.globus.gram.*;

public class status {
	public static String main(Page p)
	  throws Exception {
		HttpServletRequest req = p.getRequest();
		OOF oof = p.getOOF();
		String s = "";
		int i;

		String out = req.getParameter("out");
		if (out != null && out.equals("rss"))
			return (rss(p));

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

	public static String rss(Page p) {
		GridInt gi = p.getGridInt();
		String s = "";

		/* XXX use p.buildPath */
		String logo = "http://gridfe.psc.edu/gridfe/img/gridfe2sm.png";

		/* 2006-08-18T17:31:43+00:00 */
		Format fmt1 = new SimpleDateFormat("yyyy-MM-dd");
		Format fmt2 = new SimpleDateFormat("HH:mm:ss");
		Format fmt3 = new SimpleDateFormat("Z");
		Date now = new Date();
		String date = fmt1.format(now) + "T" + fmt2.format(now);
		String tz = fmt3.format(now);

		if (tz.startsWith("-"))
			date += "-";
		else
			date += "+";

		date += tz.substring(tz.length() - 4, tz.length() - 2) +
		  ":" + tz.substring(tz.length() - 2);

		s += "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
		  +	 "<rdf:RDF "
		  +	 " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
		  +	 " xmlns=\"http://purl.org/rss/1.0/\" "
		  +	 " xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
		  +	 " xmlns:syn=\"http://purl.org/rss/1.0/modules/syndication/\">"
		  +  	"<channel rdf:about=\"http://gridfe.psc.edu/\">"
		  +  		"<title>GridFE Jobs</title>"
		  +  		"<link>http://gridfe.psc.edu/gridfe/gridfe/jobs</link>"
		  +  		"<description>GridFE - grid front end</description>"
		  +  		"<dc:language>en-us</dc:language>"
		  +				"<dc:rights>"
		  +					"Copyright (c) 2006 Pittsburgh Supercomputing Center"
		  +				"</dc:rights>"
		  +  		"<dc:date>" + date + "</dc:date>"
//		  +  		"<dc:publisher>PSC</dc:publisher>"
		  +  		"<syn:updatePeriod>hourly</syn:updatePeriod>"
		  +  		"<syn:updateFrequency>6</syn:updateFrequency>"
		  +  		"<syn:updateBase>1970-01-01T00:00+00:00</syn:updateBase>"
		  +  		"<image rdf:resource=\"" + logo + "\" />";

		String seqs = "", items = "";
		List list = gi.getJobList().getList();
		for (int i = 0; i < list.size(); i++) {
			GridJob j = (GridJob)list.get(i);

			String url = p.buildURL("/jobs/output?qid=" + j.getQID());
			String stat = "unknown";

			try {
				stat = j.getStatusAsString();
			} catch (Exception e) {
			}

			seqs += "<rdf:li rdf:resource=\"" + url + "\" />";
			items +=	"<item rdf:about=\"" + url + "\">"
				  +			"<title>"
				  +				p.escapeHTML(j.getName() + ": " + stat)
				  +			"</title>"
				  + 		"<link>" + url + "</link>"
				  +			"<description>"
				  + 			"name: " + j.getHost() + "; \n"
				  + 			"host: " + j.getHost() + "; \n"
				  + 			"status: " + stat
				  + 		"</description>"
//				  + 		"<dc:date>2006-08-18T04:01:00+00:00</dc:date>"
				  +		"</item>";
		}

		s +=		"<items>"
		  +				"<rdf:Seq>"
		  +					seqs
		  +				"</rdf:Seq>"
		  +			"</items>"
		  +			"</channel>"
		  +			"<image rdf:about=\"" + logo + "\">"
		  +				"<title>GridFE Jobs</title>"
		  +					"<url>" + logo + "</url>"
		  +				"<link>http://gridfe.psc.edu/gridfe/gridfe/</link>"
		  +			"</image>"
		  +			items
		  +		"</rdf:RDF>";
		p.getResponse().setContentType("text/xml");
		return (s);
	}
};

/* vim: set ts=4: */
