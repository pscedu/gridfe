/* $Id$ */

package gridfe;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

class DelegationHandler {
	private String base;
	private Class handler;

	public DelegationHandler(String base, Class handler) {
		this.base = base;
		this.handler = handler;
	}

	public String getBase() {
		return this.base;
	}

	public Class getHandler() {
		return this.handler;
	}
};

public class GridFE extends HttpServlet {
	private HttpServletRequest req;
	private HttpServletResponse res;

	/* XXX: make nestable and modularable. */
	final DelegationHandler[] dtab = new DelegationHandler[] {
		new DelegationHandler("/certs",		gridfe.www.certs.class),
		new DelegationHandler("/jobs/index",	gridfe.www.jobs.index.class),
		new DelegationHandler("/jobs/output",	gridfe.www.jobs.output.class),
		new DelegationHandler("/jobs/status",	gridfe.www.jobs.status.class),
		new DelegationHandler("/jobs/submit",	gridfe.www.jobs.submit.class),
		new DelegationHandler("/nodes",		gridfe.www.nodes.class),
		new DelegationHandler("/rls/index",	gridfe.www.rls.index.class),
		new DelegationHandler("/rls/addcat",	gridfe.www.rls.addcat.class),
		new DelegationHandler("/rls/addres",	gridfe.www.rls.addres.class),
		new DelegationHandler("/rls/rmcat",	gridfe.www.rls.rmcat.class),
		new DelegationHandler("/rls/search",	gridfe.www.rls.search.class),
		new DelegationHandler("/index",		gridfe.www.index.class)
	};

	public void doGet(HttpServletRequest req, HttpServletResponse res)
	    throws IOException, ServletException {
		this.workHorse(req, res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
	    throws IOException, ServletException {
		this.workHorse(req, res);
	}

	/* XXX: remove exceptions to always output a gridfe page. */
	private void workHorse(HttpServletRequest req, HttpServletResponse res)
	    throws IOException, ServletException {
		this.req = req;
		this.res = res;

		/* XXX: wrong */
		res.setContentType("text/html");
		PrintWriter w = res.getWriter();

		Page p;
		try {
			p = new Page(req, res);
		} catch (Exception e) {
			this.handleError(null, e + ": " + e.getMessage());
			return;
		}

		String uri = req.getRequestURI();
		
		/* ``/'' is optional for index pages. */
		if (uri.charAt(uri.length() - 1) == '/')
			uri += "index";

		Class handler = null;
		String s, best;
		int bestlen = 5000 /* XXX: INT_MAX */;
		char c;
		for (int i = 0; i < this.dtab.length; i++) {
			s = p.getServRoot() + this.dtab[i].getBase();
			if (uri.length() > s.length())
				c = uri.charAt(s.length());
			else
				c = '\0';
w.print("uri: " + uri + "; s: " + s);
			if (uri.equals(s) || (uri.startsWith(s) &&
			    (c == '/' || c == '?'))) {
				handler = this.dtab[i].getHandler();
				break;
			}
			/* Handle "/foo" for "/foo/index". */
			if (s.length() > uri.length())
				c = s.charAt(uri.length());
			else
				c = '\0';
			if (s.startsWith(uri) && c == '/' &&
			    s.length() < bestlen) {
				best = s;
				bestlen = s.length();
			}
		}
		if (handler == null)
			handler = gridfe.www.notfound.class;

		try {
			s = (String)handler.getMethod("main",
				new Class[] { Page.class }).invoke(null, new Object[] {p});
		} catch (Exception e) {
			s = this.handleError(p, e + ": " + e.getMessage());
		}

		w.print(s);
		if (handler == gridfe.www.notfound.class)
			w.print("URL: " + uri);
	}

	private String handleError(Page p, String msg) {
		String s;

		try {
			s = p.header("Fatal Error") +
			    p.getOOF().p("A fatal error has occured: " + msg) +
			    p.footer();
		} catch (Exception e) {
			/* This is bad. */
			s  = "<br />\nFatal error: " + e + ": " + e.getMessage();
			s += "<br />\nOriginally: " + msg;
		}
		return (s);
	}
}
