/* $Id$ */

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
	final DelegationHandler[] dtab = new DelegationHandler[] {
		new DelegationHandler("/login", login.class),
		new DelegationHandler("/logout", logout.class)
	};

	public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws IOException, ServletException {
		System.out.print(this.workHorse(req, res));
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws IOException, ServletException {
		this.workHorse(req, res);
		System.out.print(this.workHorse(req, res));
	}

	/* XXX: remove exceptions to always output a gridfe page. */
	private String workHorse(HttpServletRequest req, HttpServletResponse res) {
		Class handler = null;
		String uri = req.getRequestURI();
		Page p = new Page(req, res);

		for (int i = 0; i < this.dtab.length; i++)
			if (uri.startsWith(this.dtab[i].getBase())) {
				handler = this.dtab[i].getHandler();
				break;
			}

		if (handler == null)
			return this.handleError(p, "Page not found");

		String s;

		try {
			s = (String)handler.getMethod("main",
				new Class[] { Page.class }).invoke(null, new Object[] {p});
		} catch (Exception e) {
			s = this.handleError(p, e + ": " + e.getMessage());
		}

		return s;
	}

	private String handleError(Page p, String msg) {
		String s;

		try {
			s =	p.header("Fatal Error") +
				p.getOOF().p("A fatal error has occured: " + msg) +
				p.footer();
		} catch (Exception e) {
			/* This is bad. */
			s = "Fatal error: " + e + ": " + e.getMessage();
		}

		return s;
	}
}
