/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import gridfe.gridint.*;
import java.io.*;
import javax.servlet.http.*;
import oof.*;

public class output {
	private static final int CHUNKSIZ = 1024;

	public static String main(Page p) throws Exception {
		HttpServletRequest req = p.getRequest();
		PrintWriter w = p.getResponse().getWriter();
		GridInt gi = p.getGridInt();
		JobList jl = gi.getJobList();
		OOF oof = p.getOOF();
		String qid, which, s = "";
		GridJob j;

		which = req.getParameter("which");
		if (which == null ||
		  (!which.equals("stdout") && !which.equals("stderr")))
			which = "stdout";

		qid = req.getParameter("qid");
		if (qid == null || !qid.matches("^\\d+$") ||
		  (j = jl.get(Integer.parseInt(qid))) == null) {
			s += p.header("Error")
			  +  oof.p("Sorry, the system could not find the requested job.")
			  +  p.footer();
			return (s);
		}

		String fn = (which.equals("stdout") ? j.getStdout() : j.getStderr());
		String ftype = (which.equals("stdout") ? "output" : "error");

		if (fn == null) {
			s += p.header("Job Output")
			  +  oof.p("No standard " + ftype +
			 		" file was specified during job submission.")
			  +  p.footer();
			return (s);
		}

		boolean download = false;
		String act = req.getParameter("act");
		if (act != null && act.equals("save"))
			download = true;

		if (download) {
			p.getResponse().setContentType("application/octet-stream");
			p.getResponse().setHeader("Content-disposition",
			    "attachment; filename=\"" +
				p.getJASP().escapeAttachName(fn) + "\"");
		} else {
			s += p.header("Job Output")
			  +  oof.p("You are currently viewing output generated by your job " +
			 		p.escapeHTML(j.getName()) + ".")
			  +  oof.p("You may alternatively "
			  +		oof.link("download and save this output",
			  		  req.getRequestURI() + "?qid=" + qid +
					  	"&amp;which=" + which+ "&amp;act=save")
			  +		" to your local machine.")
			  +  oof.pre(new Object[] { "style", "overflow: scroll; height: 400px" }, "");
			w.print(s.replaceFirst("</pre>", ""));
			s = "";
		}
		p.sentHeader();
		w.flush();

		/* This should not be hard coded! read env?? */
//		gi.startRetrieve(j, fn, 28000, 28255);
		gi.startRetrieve(j, fn, 50000, 51000);
		int size = (int)gi.getGass().getSize();

		int off = 0;
		while (size > 0) {
			int chunksiz = CHUNKSIZ;

			if (chunksiz > size)
				chunksiz = size;
			String chunk = gi.retrieve(chunksiz, off);
			if (!download)
				chunk = p.escapeHTML(chunk);
			w.print(chunk);
			w.flush();
			size -= chunksiz;
			off += chunksiz;
		}
		gi.stopRetrieve();

		if (download)
			return ("");

		s = "</pre>"
		  +  p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
