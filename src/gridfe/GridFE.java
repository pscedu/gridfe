/* $Id$ */

package gridfe;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class GridFE extends HttpServlet {
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
		/* XXX: wrong */
		res.setContentType("text/html");
		PrintWriter w = res.getWriter();

		String uri = req.getRequestURI();
		String classname = uri;
		Page p = new Page(req, res);
		try {
			p.login();
		} catch (Exception e) {
			this.handleError(w, p, e);
			return;
		}

		/* XXX: escape meta/regex chars on servroot */
		classname = "gridfe/www" + classname.replaceFirst(p.getServRoot(), "");

		/* ``/'' is optional for directory pages but see below. */
		if (classname.charAt(classname.length() - 1) == '/')
			classname += "index";
		classname = classname.replace('/', '.');

		/*
		 * For directory requests, redirect to someplace
		 * inside so that relative path names work.
		 */
		try {
			if (!uri.endsWith("/") &&
			  Class.forName(classname + ".index", false,
			  this.getClass().getClassLoader()) != null) {
				res.sendRedirect(uri + "/index");
				return;
			}
		} catch (Exception e) {
		}

		try {
			Class handler;
			if ((handler = Class.forName(classname)) == null)
				handler = gridfe.www.notfound.class;
			String s = (String)handler.getMethod("main",
				new Class[] { Page.class }).invoke(null, new Object[] {p});
			p.end();
			w.print(s);
		} catch (Exception e) {
			/* XXX: return 500 status */
			this.handleError(w, p, e);
		}
	}

	private void handleError(PrintWriter w, Page p, Exception e) {
		w.println(p.header("Fatal Error") +
		    "A fatal error has occured: " + e);
		e.printStackTrace(w);
		w.print(p.footer());
	}
}
