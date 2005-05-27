/* $Id$ */

package gridfe.www;

import gridfe.*;
import oof.*;

public class nodes {
	public static String main(Page p)
	  throws Exception {
		Action[] acts = new Action[] {
			new Action("add", add),
			new Action("list", list),
			new Action("status", status),
			new Action("remove", remove)
		};

		for (int i = 0; i < acts.length; i++)
			if (acts[i].getName().equals(act))
				return (acts[i].getMethod().invoke(null, new Object[] { p }));
		return (status(p));
	}

	public static String add(Page p)
	  throws Exception {
		OOF oof = p.getOOF();
		String s = "", emsg;
		HttpServletRequest req = p.getRequest();

		if (req.getParameter("submitted") != null) {
			String host = req.getParameter("host");

			if (host == null)
				emsg = "Invalid host: " + p.escapeHTML(host);
			} else {
//				add(host);
				s += p.header("Host Added")
				   + p.p("The host " + p.escapeHTML(host) +
						" has been successfully added.");
				return (s);
			}
		}

		return (s);
	}

	public static String remove(Page p)
	  throws Exception {
		OOF oof = p.getOOF();
		String s = "";

		return (s);
	}

	public static String status(Page p)
	  throws Exception {
		OOF oof = p.getOOF();
		String s = "";


		s += p.header("Node Availibility")
		return (s);
	}

	public static String list(Page p)
	  throws Exception {
		OOF oof = p.getOOF();
		String s = "";


		s += p.header("Node Availibility")
		return (s);
	}

	private void load(Page p) {
	}
};

class Action {
	private String name;
	private Method meth;

	public Action(String name, Method m) {
		this.name = name;
		this.m = m;
	}

	public String getName() {
		return (this.name);
	}

	public Method getName() {
		return (this.m);
	}
};

/* vim: set ts=4: */
