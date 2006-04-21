/* $Id$ */

package gridfe;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class GridFE extends HttpServlet {
//	static final long serialVersionUID = 1L;

	private HttpServletResponse res;

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
		this.res = res;

		/* XXX: wrong */
		res.setContentType("text/html");
		PrintWriter w = res.getWriter();

		Page p;
		String uri = req.getRequestURI();
		String classname = uri;
		try {
			p = new Page(req, res);
			classname = "gridfe/www" +
			    classname.replaceAll(p.getServRoot(), "");
		} catch (Exception e) {
			this.handleError(null, e + ": " + e.getMessage());
			return;
		}

		/* ``/'' is optional for directory pages but see below. */
		if (classname.charAt(classname.length() - 1) == '/')
			classname += "index";
		classname = classname.replace('/', '.');

		/*
		 * For directory requests, redirect to someplace
		 * inside so that relative path names work.
		 */
		if (Package.getPackage(classname) != null &&
		    !uri.endsWith("/")) {
			res.sendRedirect(uri + "/index");
			return;
		}

		String s;
		try {
			Class handler;
			if ((handler = Class.forName(classname)) == null)
				handler = gridfe.www.notfound.class;
			s = (String)handler.getMethod("main",
				new Class[] { Page.class }).invoke(null, new Object[] {p});
			p.end();
		} catch (Exception e) {
			s = this.handleError(p, e + ": " + e.getMessage());
			e.printStackTrace();
		}

		w.print(s);
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
