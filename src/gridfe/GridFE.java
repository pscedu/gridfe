/* $Id$ */

package gridfe;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

class DelegationHandler
{
	private String base;
	private Class handler;

	public DelegationHandler(String base, Class handler)
	{
		this.base = base;
		this.handler = handler;
	}

	public String getBase()
	{
		return this.base;
	}

	public Class getHandler()
	{
		return this.handler;
	}
};

public class GridFE extends HttpServlet
{
	private HttpServletRequest req;
	private HttpServletResponse res;

	final DelegationHandler[] dtab = new DelegationHandler[] {
		new DelegationHandler("",		gridfe.www.index.class),
		new DelegationHandler("/",		gridfe.www.index.class),
		new DelegationHandler("/login",		gridfe.www.login.class),
		new DelegationHandler("/logout",	gridfe.www.logout.class)
	};

	public void doGet(HttpServletRequest req, HttpServletResponse res)
		throws IOException, ServletException
	{
		this.workHorse(req, res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)
		throws IOException, ServletException
	{
		this.workHorse(req, res);
	}

	/* XXX: remove exceptions to always output a gridfe page. */
	private void workHorse(HttpServletRequest req, HttpServletResponse res)
		throws IOException, ServletException
	{
		this.req = req;
		this.res = res;

		String uri = req.getRequestURI();

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

		Class handler = null;
		for (int i = 0; i < this.dtab.length; i++)
			if (uri.startsWith(this.dtab[i].getBase())) {
				handler = this.dtab[i].getHandler();
				break;
			}

		if (handler == null) {
			w.print(this.handleError(p, "Page not found: " + uri));
			return;
		}

		String s;

		w.print(p.getOOF().p("asdf"));

		try {
			Method m = handler.getMethod("main",
				new Class[] { Page.class });
			w.println("Found m: " + m);
			s = (String)handler.getMethod("main",
				new Class[] { Page.class }).invoke(null, new Object[] {p});
		} catch (Exception e) {
			s = this.handleError(p, e + ": " + e.getMessage());
		}

		w.print(s);
	}

	private String handleError(Page p, String msg)
	{
		String s;

		try {
			s =	p.header("Fatal Error") +
				p.getOOF().p("A fatal error has occured: " + msg) +
				p.footer();
		} catch (Exception e) {
			/* This is bad. */
			s = "Fatal error: " + e + ": " + e.getMessage();
			s += "\nOriginally: " + msg;
		}

		return s;
	}
}
