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

		String qid = req.getParameter("qid");
		if (qid == null)
			qid = "";
		if (!qid.equals(""))
			return (jstat(p, qid));

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

			int stat = j.getStatus();

			String outlinks = "";
			String cancel = "";
			if (stat == GramJob.STATUS_ACTIVE ||
			  stat == GramJob.STATUS_PENDING) {
				outlinks = "N/A";
				cancel = "" + oof.link("Cancel",
				  p.buildURL("/jobs/cancel?qid=" + j.getQID()));
			} else {
				boolean stdout, stderr;

				String path = "/jobs/output?qid=" + j.getQID();
				stdout = (j.getStdout() != null);
				stderr = (j.getStderr() != null);
				if (stdout)
					outlinks += "" +
					  oof.link("output", p.buildURL(path + "&amp;which=stdout")) + " [" +
					  oof.link("save", p.buildURL(path + "&amp;which=stdout&amp;act=save")) + "]";
				if (stdout && stderr)
					outlinks += " : ";
				if (stderr)
					outlinks += "" +
					  oof.link("error", p.buildURL(path + "&amp;which=stderr")) + " [" +
					  oof.link("save", p.buildURL(path + "&amp;which=stderr&amp;act=save")) + "]";
				if (!stdout && !stderr)
					outlinks = "N/A";
				cancel = "N/A";
			}

			s += oof.table_row(new Object [][] {
				new Object[] { "class", c, "value", "" +
				  oof.link(p.escapeHTML(j.getName()),
				   p.buildURL("/jobs/status?qid=" + j.getQID())) },
				new Object[] { "class", c, "value", j.getHost() },
				new Object[] { "class", c, "value", j.getStatusAsString() },
				new Object[] { "class", c, "value", outlinks },
				new Object[] { "class", c, "value", cancel },
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

	public static String jstat(Page p, String qid) throws Exception {
		JobList jl = p.getGridInt().getJobList();
		OOF oof = p.getOOF();
		String s = "";

		GridJob j = jl.get(Integer.parseInt(qid));
		if (j == null) {
			s += p.header("Error")
			  +  oof.p("The system could not find the requested job.")
			  +  p.footer();
			return (s);
		}

		Date mtime = j.getModTime();
		String s_mtime = (mtime == null ? "unspecified" : rssdate(mtime));

		String path = "/jobs/output?qid=" + j.getQID();
		String stdout = j.getStdout();
		if (stdout == null)
			stdout = "unspecified";
		else
			stdout = "" +
			  oof.link(p.escapeHTML(stdout), path + "&amp;which=stdout") + " [" +
			  oof.link("save", path + "&amp;which=stderr&amp;act=save") + "]";

		String stderr = j.getStderr();
		if (stderr == null)
			stderr = "unspecified";
		else
			stderr = "" +
			  oof.link(p.escapeHTML(stderr), path + "&amp;which=stderr") + " [" +
			  oof.link("save", path + "&amp;which=stderr&amp;act=save") + "]";

		String cmd = (String)j.getMap().get("executable");
		String args = (String)j.getMap().get("arguments");
		if (args != null)
			cmd += " " + args;
		String queue = (String)j.getMap().get("queue");
		if (queue == null)
			queue = "unspecified";

		s += p.header("Job Status")
		  +  oof.p("Viewing information for job " +
		  		p.escapeHTML(j.getName()) + ":")
		  +  oof.table_start(new Object[] {
			  "class", Page.CCTBL,
			  "border", "0",
			  "cellspacing", "0",
			  "cellpadding", "0" })
		  +		oof.table_row(new Object[][] {
		  			new Object[] {
						"class", Page.CCHDR,
						"colspan", "2",
						"value", "Job Information"
					}
		  		})
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", Page.CCSUBHDR, "value", "Field" },
					new Object[] { "class", Page.CCSUBHDR, "value", "Value" }
				})
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", Page.CCDESC, "value", "Name:" },
					new Object[] { "class", p.genClass(), "value", p.escapeHTML(j.getName()) }
				})
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", Page.CCDESC, "value", "Globus URL:" },
					new Object[] { "class", p.genClass(), "value", p.escapeHTML(j.getIDAsString()) }
				})
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", Page.CCDESC, "value", "Resource:" },
					new Object[] { "class", p.genClass(), "value", j.getHost() }
				})
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", Page.CCDESC, "value", "Submission Time:" },
					new Object[] { "class", p.genClass(), "value", rssdate(j.getCreateTime()) }
				})
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", Page.CCDESC, "value", "Modification Time:" },
					new Object[] { "class", p.genClass(), "value", rssdate(j.getModTime()) }
				})
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", Page.CCDESC, "value", "Command:" },
					new Object[] { "class", p.genClass(), "value", p.escapeHTML(cmd) }
				})
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", Page.CCDESC, "value", "Queue:" },
					new Object[] { "class", p.genClass(), "value", p.escapeHTML(queue) }
				})
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", Page.CCDESC, "value", "Output File:" },
					new Object[] { "class", p.genClass(), "value", stdout }
				})
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", Page.CCDESC, "value", "Error File:" },
					new Object[] { "class", p.genClass(), "value", stderr }
				})
		  +		oof.table_row(new Object[][] {
					new Object[] { "class", Page.CCDESC, "value", "RSL:" },
					new Object[] { "class", p.genClass(), "value", p.escapeHTML(j.extraRSL()) }
				})
		  +  oof.table_end()
		  +  p.footer();
		return (s);
	}

	public static String rssdate(Date d) {
		/* 2006-08-18T17:31:43+00:00 */
		Format fmt1 = new SimpleDateFormat("yyyy-MM-dd");
		Format fmt2 = new SimpleDateFormat("HH:mm:ss");
		Format fmt3 = new SimpleDateFormat("Z");
		String date = fmt1.format(d) + "T" + fmt2.format(d);
		String tz = fmt3.format(d);

		if (tz.startsWith("-"))
			date += "-";
		else
			date += "+";

		date += tz.substring(tz.length() - 4, tz.length() - 2) +
		  ":" + tz.substring(tz.length() - 2);

		return (date);
	}

	public static String rss(Page p) {
		GridInt gi = p.getGridInt();

		/* XXX use p.buildPath */
		String logo = "http://gridfe.psc.edu/gridfe/img/gridfe2sm.png";
		String date = rssdate(new Date());
		String root = "https://gridfe.psc.edu";
		try {
			root = "https://" +
			    java.net.InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			System.err.println("Cannot look up local hostname: " + e);
		}

		String seqs = "", items = "";
		List list = gi.getJobList().getList();
		for (int i = 0; i < list.size(); i++) {
			GridJob j = (GridJob)list.get(i);

			String url = root + p.buildURL("/jobs/status?qid=" + j.getQID());
			String stat = "unknown";

			try {
				stat = j.getStatusAsString();
			} catch (Exception e) {
			}

			seqs += "<rdf:li rdf:resource=\"" + url + "\" />";
			items +=	"<item rdf:about=\"" + url + "\">"
				  +			"<title>"
				  +				p.escapeHTML(j.getName() + ": submitted")
				  +			"</title>"
				  + 		"<link>" + url + "</link>"
				  +			"<description>"
				  + 			"name: " + p.escapeHTML(j.getName()) + "; <br />\n"
				  + 			"resource: " + j.getHost() + "; <br />\n"
				  + 		"</description>"
				  + 		"<dc:date>" + rssdate(j.getCreateTime()) + "</dc:date>"
				  +		"</item>";

			Date mtime = j.getModTime();
			if (mtime != null) {
				seqs += "<rdf:li rdf:resource=\"" + url + "\" />";
				items +=	"<item rdf:about=\"" + url + "\">"
					  +			"<title>"
					  +				p.escapeHTML(j.getName() + ": " + stat)
					  +			"</title>"
					  + 		"<link>" + url + "</link>"
					  +			"<description>"
					  + 			"job &quot;" + p.escapeHTML(j.getName()) + "&quot; "
					  +				"has changed status: " + stat
					  + 		"</description>"
					  + 		"<dc:date>" + rssdate(mtime) + "</dc:date>"
					  +		"</item>";
			}
		}

		String s = ""
		  +  "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
		  +	 "<rdf:RDF "
		  +	 " xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "
		  +	 " xmlns=\"http://purl.org/rss/1.0/\" "
		  +	 " xmlns:dc=\"http://purl.org/dc/elements/1.1/\" "
		  +	 " xmlns:syn=\"http://purl.org/rss/1.0/modules/syndication/\">"
		  +  	"<channel rdf:about=\"" + root + "/\">"
		  +  		"<title>GridFE Jobs</title>"
		  +  		"<link>" + root + "/gridfe/gridfe/jobs</link>"
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
		  +  		"<image rdf:resource=\"" + logo + "\" />"
		  + 		"<items>"
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

/*
<?xml version="1.0"?>
<rss version="2.0">
  <channel>
    <title>Liftoff News</title>
    <link>http://liftoff.msfc.nasa.gov/</link>
    <description>Liftoff to Space Exploration.</description>
    <language>en-us</language>
    <pubDate>Tue, 10 Jun 2003 04:00:00 GMT</pubDate>
    <lastBuildDate>Tue, 10 Jun 2003 09:41:01 GMT</lastBuildDate>
    <docs>http://blogs.law.harvard.edu/tech/rss</docs>
    <generator>Weblog Editor 2.0</generator>
    <managingEditor>editor@example.com</managingEditor>
    <webMaster>webmaster@example.com</webMaster>

    <item>
      <title>Star City</title>
      <link>http://liftoff.msfc.nasa.gov/news/2003/news-starcity.asp</link>
      <description>How do Americans get ready to work with Russians aboard the
        International Space Station? They take a crash course in culture, language
        and protocol at Russia's Star City.</description>
      <pubDate>Tue, 03 Jun 2003 09:39:21 GMT</pubDate>
      <guid>http://liftoff.msfc.nasa.gov/2003/06/03.html#item573</guid>
    </item>

    <item>
      <title>Space Exploration</title>
      <link>http://liftoff.msfc.nasa.gov/</link>
      <description>Sky watchers in Europe, Asia, and parts of Alaska and Canada
        will experience a partial eclipse of the Sun on Saturday, May 31st.</description>
      <pubDate>Fri, 30 May 2003 11:06:42 GMT</pubDate>
      <guid>http://liftoff.msfc.nasa.gov/2003/05/30.html#item572</guid>
    </item>

    <item>
      <title>The Engine That Does More</title>
      <link>http://liftoff.msfc.nasa.gov/news/2003/news-VASIMR.asp</link>
      <description>Before man travels to Mars, NASA hopes to design new engines
        that will let us fly through the Solar System more quickly.  The proposed
        VASIMR engine would do that.</description>
      <pubDate>Tue, 27 May 2003 08:37:32 GMT</pubDate>
      <guid>http://liftoff.msfc.nasa.gov/2003/05/27.html#item571</guid>
    </item>

    <item>
      <title>Astronauts' Dirty Laundry</title>
      <link>http://liftoff.msfc.nasa.gov/news/2003/news-laundry.asp</link>
      <description>Compared to earlier spacecraft, the International Space
        Station has many luxuries, but laundry facilities are not one of them.
        Instead, astronauts have other options.</description>
      <pubDate>Tue, 20 May 2003 08:56:02 GMT</pubDate>
      <guid>http://liftoff.msfc.nasa.gov/2003/05/20.html#item570</guid>
    </item>
  </channel>
</rss>
*/

/* vim: set ts=4: */
