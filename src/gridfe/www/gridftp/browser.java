/* $Id$ */

package gridfe.www.gridftp;

import gridfe.*;
import gridfe.gridint.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import javax.servlet.http.*;
import oof.*;
import org.globus.ftp.*;
import org.globus.ftp.exception.*;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.io.output.DeferredFileOutputStream; /* needed by fileupload */

public class browser {
	public static final int GRIDFTP_PORT = 2811;

	private static final int I_LHOST	= 0;
	private static final int I_RHOST	= 1;
	private static final int I_LCWD		= 2;
	private static final int I_RCWD		= 3;
	private static final int I_LTYPE	= 4;
	private static final int I_RTYPE	= 5;
	private static final int I_ACTION	= 6;
	private static final int I_DISPLAY	= 7;
	private static final int NI			= 8;

	public static String main(Page p)
	  throws Exception {
		HttpServletRequest req = p.getRequest();
		List upfiles = new LinkedList();
		String emsg = "";

		String v[] = new String[NI];
		for (int j = 0; j < v.length; j++)
			v[j] = null;

		if (ServletFileUpload.isMultipartContent(req)) {
			parseMultipart(v, req, upfiles);
		} else {
			v[I_LHOST] = req.getParameter("lhost");
			v[I_RHOST] = req.getParameter("rhost");
			v[I_LCWD] = req.getParameter("lcwd");
			v[I_RCWD] = req.getParameter("rcwd");
			v[I_LTYPE] = req.getParameter("ltype");
			v[I_RTYPE] = req.getParameter("rtype");
			v[I_ACTION] = req.getParameter("action");
			v[I_DISPLAY] = req.getParameter("display");
		}

		for (int j = 0; j < v.length; j++)
			if (v[j] == null)
				v[j] = "";

		String lhost = v[I_LHOST];
		String rhost = v[I_RHOST];
		String lcwd = v[I_LCWD];
		String rcwd = v[I_RCWD];
		String ltype = v[I_LTYPE];
		String rtype = v[I_RTYPE];
		String action = v[I_ACTION];
		String display = v[I_DISPLAY];

		/* validity checks */
		if (!ltype.equals("archiver") &&
		    !ltype.equals("gridftp"))
			ltype = "gridftp";

		if (!rtype.equals("archiver") &&
		    !rtype.equals("gridftp"))
			rtype = "gridftp";

		if (!display.equals("l") &&
		    !display.equals("r"))
			display = "";

		/*
		 * Logout action needs to be handled
		 * here to simply not re-login.
		 */
		if (action.equals("Logout")) {
			if (display.equals("l"))
				lhost = "";
			else if (display.equals("r"))
				rhost = "";
		}

		/*
		 * Count up and add args to pass around in
		 * an array for easy query string building.
		 */
		int len = 0;

		if (!lhost.equals("")) {
			len++;
			if (!lcwd.equals(""))
				len++;
			if (!ltype.equals(""))
				len++;
		}
		if (!rhost.equals("")) {
			len++;
			if (!rcwd.equals(""))
				len++;
			if (!rtype.equals(""))
				len++;
		}

		String[] params = new String[2 * len];

		int j = 0;

		if (!lhost.equals("")) {
			params[j++] = "lhost";
			params[j++] = lhost;
			if (!lcwd.equals("")) {
				params[j++] = "lcwd";
				params[j++] = lcwd;
			}
			if (!ltype.equals("")) {
				params[j++] = "ltype";
				params[j++] = ltype;
			}
		}
		if (!rhost.equals("")) {
			params[j++] = "rhost";
			params[j++] = rhost;
			if (!rcwd.equals("")) {
				params[j++] = "rcwd";
				params[j++] = rcwd;
			}
			if (!rtype.equals("")) {
				params[j++] = "rtype";
				params[j++] = rtype;
			}
		}

		/* Establish GridFTP connections. */
		GridFTP lgftp = null, rgftp = null;
		GridInt gi = p.getGridInt();
		if (!lhost.equals("")) {
			try {
				lgftp = new GridFTP(gi.getGSS().getGSSCredential(),
				  lhost, GRIDFTP_PORT);
				if (!lcwd.equals(""))
					lgftp.changeDir(lcwd);
			} catch (Exception e) {
				emsg += " Error connecting to " +
				  p.escapeHTML(lhost + ": " + e.getMessage()) + ".";
			}
			if (lgftp != null)
				lcwd = lgftp.getCurrentDir();
		}

		if (!rhost.equals("")) {
			try {
				rgftp = new GridFTP(gi.getGSS().getGSSCredential(),
				  rhost, GRIDFTP_PORT);
				if (!rcwd.equals(""))
					rgftp.changeDir(rcwd);
			} catch (Exception e) {
				emsg += " Error connecting to " +
				  p.escapeHTML(rhost + ": " + e.getMessage()) + ".";
			}
			if (rgftp != null)
				rcwd = rgftp.getCurrentDir();
		}

		GridFTP dgftp = null, ogftp = null;
		String dhost = null, ohost = null;
		String dcwd = null, ocwd = null;
		String dtype = null, otype = null;

		if (display.equals("l")) {
			dgftp = lgftp;	ogftp = rgftp;
			dhost = lhost;	ohost = rhost;
			dcwd = lcwd;	ocwd = rcwd;
			dtype = ltype;	otype = rtype;
		} else if (display.equals("r")) {
			dgftp = rgftp;	ogftp = lgftp;
			dhost = rhost;	ohost = lhost;
			dcwd = rcwd;	ocwd = lcwd;
			dtype = rtype;	otype = ltype;
		}

		/*
		 * Handle actions.
		 */
		if (action.equals("download")) {
			emsg += download(p, dgftp, dcwd);
			if (emsg.equals(""))
				return ("");
		} else if (action.equals("Upload")) {
			emsg += upload(p, dgftp, dcwd, upfiles);
		} else if (action.equals("Delete Checked")) {
			emsg += rm(p, dgftp);
		} else if (action.equals("Create Directory")) {
			emsg += mkdir(p, dgftp);
		} else if (action.equals("Copy Checked To Other Host")) {
			emsg += copy(p, dgftp, dhost, dcwd, ohost, ocwd);
		} else if (action.equals("Stage Checked to Host")) {
			emsg += stage(p, dgftp, dhost, dcwd, dtype);
		}

		Object[] hlist = browser.createHostList(p);

		String s = "";
		OOF oof = p.getOOF();
		s += p.header("GridFTP File Browser");
		if (!emsg.equals(""))
			s += oof.p(new Object[] { "class", "err" }, emsg);

		/*
		 * Set the content to display -- browser if
		 * connected, otherwise login table.
		 */
		if (lhost.equals(""))
			s += oof.p("This GridFTP interface allows you to browse two " +
					"resources simultaneously and provides the ability to " +
					"transfer files between them.  Alternatively, you may " +
					"connect to only one resource if you wish to transfer " +
					"files between your local machine and that target resource.")
			  +  login(p, "l", params, hlist);
		else
			s += browse(p, "l", lhost, params, lcwd,
			  ltype, lgftp, rgftp != null);

		s += oof.hr();

		if (rhost.equals(""))
			s += login(p, "r", params, hlist);
		else
			s += browse(p, "r", rhost, params, rcwd,
			  rtype, rgftp, lgftp != null);
		s += p.footer();
		return (s);
	}

	private static void parseMultipart(String[] v,
	  HttpServletRequest req, List upfiles) {
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// maximum size that will be stored in memory
		factory.setSizeThreshold(1024);
		// the location for saving data that is larger than getSizeThreshold()
		factory.setRepository(new File("/tmp"));

        ServletFileUpload upload = new ServletFileUpload(factory);
        // maximum size before a FileUploadException will be thrown
        upload.setSizeMax(1024 * 1024 * 10);

		List fileItems;
		try {
			fileItems = upload.parseRequest(req);
		} catch (Exception e) {
			return;
		}

		FileItem fi;
        for (Iterator i = fileItems.iterator();
		  i.hasNext() && (fi = (FileItem)i.next()) != null; ) {
			if (fi.isFormField()) {
				if (fi.getFieldName().equals("lhost"))
					v[I_LHOST] = fi.getString();
				if (fi.getFieldName().equals("rhost"))
					v[I_RHOST] = fi.getString();
				if (fi.getFieldName().equals("lcwd"))
					v[I_LCWD] = fi.getString();
				if (fi.getFieldName().equals("rcwd"))
					v[I_RCWD] = fi.getString();
				if (fi.getFieldName().equals("ltype"))
					v[I_LTYPE] = fi.getString();
				if (fi.getFieldName().equals("rtype"))
					v[I_RTYPE] = fi.getString();
				if (fi.getFieldName().equals("action"))
					v[I_ACTION] = fi.getString();
				if (fi.getFieldName().equals("display"))
					v[I_DISPLAY] = fi.getString();
			} else
				upfiles.add(fi);
		}
	}

	public static String getParam(String param, String[] params) {
		for (int j = 0; j + 1 < params.length; j += 2)
			if (params[j].equals(param))
				return (params[j + 1]);
		return (null);
	}

	/* Construct a query string (?foo=bar&...). */
	public static String buildQS(Page page, String[] p, String[] pnew) {
		String s, key, val;

		s = "?";
		for (int j = 0; j + 1 < p.length; j += 2) {
			val = p[j + 1];
			for (int k = 0; k + 1 < pnew.length; k += 2)
				if (pnew[k].equals(p[j])) {
					val = pnew[k + 1];
					break;
				}
			s += p[j] + "=" + page.escapeURL(val) + "&amp;";
		}

		boolean found;
		for (int k = 0; k + 1 < pnew.length; k += 2) {
			found = false;
			for (int j = 0; j + 1 < p.length; j += 2)
				if (p[j].equals(pnew[k])) {
					found = true;
					break;
				}
			if (found)
				continue;
			s += pnew[k] + "=" + page.escapeURL(pnew[k + 1]) + "&amp;";
		}
		return (s);
	}

	private static String upload(Page p, GridFTP gftp,
	  String cwd, List upfiles) {
		String emsg = "";
		FileItem fi;

		for (Iterator it = upfiles.iterator();
		  it.hasNext() && (fi = (FileItem)it.next()) != null; ) {
			try {
				if (fi.getFieldName().equals("upfile")) {
					File tmpf = File.createTempFile("gridfe.up", null);
					fi.write(tmpf);
					gftp.put(tmpf, cwd + fi.getName(), false);
				}
			} catch (Exception e) {
				emsg += " Error while uploading: " + e.getMessage() + ".";
			}
		}
		return (emsg);
	}

	private static String rm(Page p, GridFTP gftp) {
		HttpServletRequest req = p.getRequest();
		String[] files = req.getParameterValues("file");
		String emsg = "";

		if (gftp == null)
			return (" Error deleting files: not connected to host.");
		if (files == null || files.length == 0)
			return (" Error deleting files: no files specified.");

		for (int k = 0; k < files.length; k++) {
			try {
				MlsxEntry mx = gftp.mlst(files[k]);
				if (mx.get(MlsxEntry.TYPE).equals(MlsxEntry.TYPE_FILE))
					gftp.deleteFile(files[k]);
				else if (mx.get(MlsxEntry.TYPE).equals(MlsxEntry.TYPE_DIR))
					gftp.deleteDir(files[k]);
				else
					throw new Exception("unknown file type");
			} catch (Exception e) {
				emsg += " Error while trying to delete " +
				  p.escapeHTML(files[k]) + ": " + e.getMessage() + ".";
			}
		}
		return (emsg);
	}

	private static String mkdir(Page p, GridFTP gftp) {
		HttpServletRequest req = p.getRequest();
		String dir = req.getParameter("newdir");
		String emsg = "";

		try {
			if (dir == null || dir.equals("") ||
			  dir.matches("/") || dir.matches("^."))
				throw new Exception("invalid directory name");
			gftp.makeDir(dir);
		} catch (Exception e) {
			emsg += " Error while trying to create a new directory: " +
			  e.getMessage() + ".";
		}
		return (emsg);
	}

	private static String copy(Page p, GridFTP src_gftp,
	  String src_host, String src_cwd,
	  String dst_host, String dst_cwd) {
		HttpServletRequest req = p.getRequest();
		GridInt gi = p.getGridInt();
		String[] files = req.getParameterValues("file");
		String emsg = "";

		try {
			if (src_gftp == null)
				throw new Exception("not connected to source resource");
			if (src_host == null)
				throw new Exception("no source resource specified");
			if (dst_host == null)
				throw new Exception("no destination resource specified");
			if (src_cwd == null)
				throw new Exception("no source working directory specified");
			if (dst_cwd == null)
				throw new Exception("no destination working directory specified");
			if (files == null || files.length == 0)
				throw new Exception("no files specified");

			for (int k = 0; k < files.length; k++) {
				if (files[k].equals(".") || files[k].equals(".."))
					continue;
				MlsxEntry mx = src_gftp.mlst(files[k]);
				if (!mx.get(MlsxEntry.TYPE).equals(MlsxEntry.TYPE_FILE))
					continue;
				GridFTP.urlCopy(gi.getGSS().getGSSCredential(),
				  src_host, dst_host, src_cwd + "/" + files[k],
				  dst_cwd + "/" + files[k]);
			}
		} catch (Exception e) {
			emsg += "Error while trying to transfer files: " +
			  e.getMessage();
		}
		return (emsg);
	}

	private static String stage(Page p, GridFTP gftp, String host,
	  String cwd, String type) {
		HttpServletRequest req = p.getRequest();
		String[] files = req.getParameterValues("file");
		String st_host = req.getParameter("st_host");
		String st_cwd = req.getParameter("st_cwd");
		String emsg = "";

		try {
			if (gftp == null)
				throw new Exception("no connection to host");
			if (st_host == null)
				throw new Exception("no stage host specified");
			if (st_cwd == null)
				throw new Exception("no stage working directory specified");
			if (files == null || files.length == 0)
				throw new Exception("no files specified");

			/* XXX - add support to url-copy to archiver then run above */
			/* Indirect staging, must be copied to archiver first */
			if (!type.equals("archiver"))
				throw new Exception("Files can only be staged from " +
				  "the archiver.  Please use the GridFTP browser to " +
				  "copy the files to the archiver.");

			/* Allows direct staging */
			for (int i = 0; i < files.length; i++) {
				MlsxEntry mx = gftp.mlst(files[i]);
				if (!mx.get(MlsxEntry.TYPE).equals(MlsxEntry.TYPE_FILE))
					continue;

				StageJob.archive2host(p.getGridInt(), host, st_host,
				  cwd, st_cwd, files[i]);
			}
		} catch (Exception e) {
			emsg += " Error while staging files: " + e.getMessage() + ".";
		}
		return (emsg);
	}

	private static String download(Page p, GridFTP gftp, String cwd) {
		HttpServletRequest req = p.getRequest();
		String file = req.getParameter("file");
		String emsg = "";

		try {
			PrintWriter w = p.getResponse().getWriter();

			if (gftp == null)
				throw new Exception("not connected to host");
			if (cwd == null)
				throw new Exception("no working directory specified");
			if (file == null || file.equals(""))
				throw new Exception("no file specified");

			File tmpf = File.createTempFile("gridfe.dl", null);
			gftp.get(cwd + "/" + file, tmpf);

			p.getResponse().setContentType("application/octet-stream");
			p.getResponse().setHeader("Content-disposition",
			    "attachment; filename=\"" +
				p.getJASP().escapeAttachName(file) + "\"");

			BufferedReader r = new BufferedReader(new FileReader(tmpf));
			int c;
			while ((c = r.read()) != -1)
				w.write(c);
		} catch (Exception e) {
			emsg += "Error while trying to fetch " +
			  p.escapeHTML(file) + ": " + e.getMessage();
		}
		return (emsg);
	}

	/*
	 * Login table for the gridftp structure
	 */
	public static String login(Page p, String display,
	  String[] params, Object[] hlist) throws Exception {
		String s = "";
		OOF oof = p.getOOF();

		String js_submit =
			"	var n = this.form.elements.length - 1;	" +
			"	var el = this.form.elements[n];			" +
			"	var bv = 'Please wait...';				" +
			"	if (el.value != bv) {					" +
			"		el.value = bv;						" +
			"		return (true);						" +
			"	}										";

		String extra = "";
		String other = (display.equals("r") ? "l" : "r");
		String ohost, ocwd, otype;
		if ((ohost = getParam(other + "host", params)) != null) {
			extra += "" + oof.input(new Object[] {
					"type", "hidden",
					"name", other + "host",
					"value", ohost
				});

			if ((ocwd = getParam(other + "cwd", params)) != null)
				extra += "" + oof.input(new Object[] {
						"type", "hidden",
						"name", other + "cwd",
						"value", ocwd
					});

			if ((otype = getParam(other + "type", params)) != null)
				extra += "" + oof.input(new Object[] {
						"type", "hidden",
						"name", other + "type",
						"value", otype
					});
		}

		/* Form field for logging in */
		s += oof.form(
				new Object[] {
					"action", "browser",
					"onsubmit", js_submit
				},
				new Object[] {
					"Hostname: " +
					oof.input(new Object[] {
						"type", "text",
						"name", display + "host"
					}) +
					oof.input(new Object[] {
						"type", "select",
						"onchange", js_hostchg(display + "host"),
						"options", hlist
					}) +
					oof.br() + "Host type: " +
					oof.input(new Object[] {
						"type", "select",
						"name", display + "type",
						"options", new Object[] {
							"gridftp", "GridFTP",
							"archiver", "Archiver"
						}
					}) +
					oof.p("&raquo; Enter the host name of the resource " +
					"that you would like to browse over GridFTP.") +
					extra +
					oof.input(new Object[] {
						"type", "submit",
						"class", "button",
						"value", "Login"
					})
				});
		 return (s);
	}

	public static String browse(Page p, String display, String hostname,
	  String[] params, String cwd, String type, GridFTP gftp, boolean oconn)
	  throws Exception {
		String s = "";
		OOF oof = p.getOOF();

		String extra = "" + oof.input(new Object[] {
				"type", "hidden",
				"name", display + "host",
				"value", hostname
			});
		if (type != null)
			extra += "" + oof.input(new Object[] {
					"type", "hidden",
					"name", display + "type",
					"value", type
				});

		String other = (display.equals("r") ? "l" : "r");
		String ohost, ocwd, otype;
		if ((ohost = getParam(other + "host", params)) != null) {
			extra += "" + oof.input(new Object[] {
					"type", "hidden",
					"name", other + "host",
					"value", ohost
				});

			if ((ocwd = getParam(other + "cwd", params)) != null)
				extra += "" + oof.input(new Object[] {
						"type", "hidden",
						"name", other + "cwd",
						"value", ocwd
					});

			if ((otype = getParam(other + "type", params)) != null)
				extra += "" + oof.input(new Object[] {
						"type", "hidden",
						"name", other + "type",
						"value", otype
					});
		}
		String cd_extra = extra;

		if (cwd != null)
			extra += "" + oof.input(new Object[] {
					"type", "hidden",
					"name", display + "cwd",
					"value", cwd
				});

		String rctl = "";
		if (oconn)
			rctl += "" + oof.input(new Object[] {
					"type", "submit",
					"class", "button",
					"name", "action",
					"value", "Copy Checked To Other Host"
				});

		String st_extra = "";
		if (type.equals("archiver"))
			st_extra += oof.br() + "" + oof.br() +
				"Stage to host: " +
				oof.input(new Object[] {
					"type", "text",
					"name", "st_host"
				}) + oof.br() +
				"Directory: " +
				oof.input(new Object[] {
					"type", "text",
					"name", "st_cwd"
				}) + oof.br() +
				oof.input(new Object[] {
					"type", "submit",
					"class", "button",
					"name", "action",
					"value", "Stage Checked to Host"
				});

		String js_toggle =
		  "	var f = this.form.elements['file'];				" +
		  "	if (f) {										" +
		  "		if (f.length)								" +
		  "			for (var i = 0; i < f.length; i++)		" +
		  "				f[i].checked = !f[i].checked;		" +
		  "		else										" +
		  "			f.checked = !f.checked;					" +
		  "	}												";

		String js_gohome =
		  "	this.form.elements['" + display +
			"cwd'].value = '~';								" +
		  "	this.form.submit();								";

		String buildpath = "";
		String path = "";
		String[] ancestors = cwd.split("/");
		for (int j = 0; j < ancestors.length; j++) {
			if (ancestors[j].length() == 0)
				continue;

			buildpath += "/" + ancestors[j];
			path += "/" + oof.link(p.escapeHTML(ancestors[j]),
			  buildQS(p, params, new String[] {
				display + "cwd", buildpath
			  })
			);
		}

		/* Form field for logging in */
		s += ""
		  + oof.p("Click on a file to download or a directory to view its contents.")
		  + oof.form_start(new Object[] {
				"action", "browser",
				"method", "GET",
				"enctype", "application/x-www-form-urlencoded"
			})
		  + oof.table_start(new Object[] {
				"class", Page.CCTBL,
				"border", "0",
				"cellspacing", "0",
				"cellpadding", "0",
				"cols", new Object[][] {
					new Object[] { },
					new Object[] { "align", "char", "char", "." },
					new Object[] { },
					new Object[] { },
				}
			})
		  + oof.table_row(new Object[][] {
				new Object[] {
					"class", Page.CCHDR,
					"value", "Viewing gridftp://" + hostname + path,
					"colspan", "5"
				}
			})
		  + oof.table_row(new Object[][] {
				new Object[] { "class", Page.CCSUBHDR, "value", "Name" },
				new Object[] { "class", Page.CCSUBHDR, "value", "Size" },
				new Object[] { "class", Page.CCSUBHDR, "value", "Date" },
				new Object[] { "class", Page.CCSUBHDR, "value", "Modes" },
				new Object[] { "class", Page.CCSUBHDR, "value", "" }
			})
		/*
		 * Parse the list and put each file (with size, perm,
		 * date/time) on a table row.
		 */
		  + listing(display, p, gftp, params)
		  + oof.table_row(new Object[][] {
				new Object[] {
					"class", Page.CCTBLFTR,
					"colspan", "5",
					"value", extra +
						"Create directory: " +
						oof.input(new Object[] {
							"type", "text",
							"name", "newdir"
						}) +
						oof.input(new Object[] {
							"type", "submit",
							"class", "button",
							"name", "action",
							"value", "Create Directory"
						}) +
						oof.br() +
						oof.input(new Object[] {
							"type", "button",
							"class", "button",
							"value", "Toggle All",
							"onclick", js_toggle
						}) +
						oof.input(new Object[] {
							"type", "hidden",
							"name", "display",
							"value", display
						}) +
						rctl +
						oof.input(new Object[] {
							"type", "submit",
							"class", "button",
							"name", "action",
							"value", "Delete Checked"
						}) +
						oof.input(new Object[] {
							"type", "submit",
							"class", "button",
							"name", "action",
							"value", "Logout"
						}) +
						st_extra
				}
			})
		  + oof.table_end()
		  + oof.form_end()
		  + oof.form(new Object[] {
				"action", "browser",
				"enctype", "multipart/form-data",
				"method", "POST"
			}, new Object[] {
			    "Upload file: " +
				oof.input(new Object[] {
					"type", "file",
					"name", "upfile"
				}) +
				extra +
				oof.input(new Object[] {
					"type", "submit",
					"class", "button",
					"name", "action",
					"value", "Upload"
				})
			})
		  + oof.form(new Object[] {
				"action", "browser"
		 	},
		  	  new Object[] {
				oof.table(new Object[] {
					"class", Page.CCTBL,
					"border", "0",
					"cellspacing", "0",
					"cellpadding", "0"
				  },
				  new Object[][][] {
				    new Object[][] {
				      new Object[] {
						"class", Page.CCTBLFTR,
						"value",
							"Change directory: " +
							oof.input(new Object[] {
								"type", "text",
								"name", display + "cwd",
								"value", cwd
							}) +
							oof.br() +
							cd_extra +
							oof.input(new Object[] {
								"type", "reset",
								"class", "button",
								"value", "Reset to Current"
							}) +
							oof.input(new Object[] {
								"type", "submit",
								"class", "button",
								"value", "Change Directory"
							}) +
							oof.input(new Object[] {
								"type", "button",
								"class", "button",
								"value", "Go Home",
								"onclick", js_gohome
							})
					  }
					}
				  }
				)
			  }
			);
		 return (s);
	}

	public static final int MAXFNLEN = 40;

	public static String listing(String display, Page p, GridFTP gftp,
	  String[] params) throws Exception {
		OOF oof = p.getOOF();
		GridFile gf;

		Vector v = gftp.gls();

		Collections.sort(v);
		String cwd = gftp.getCurrentDir();

		String prefix = p.getWebRoot() + "/img/";

		String s = "", qs, type;
		for (int j = 0; j < v.size(); j++) {
			gf = (GridFile)v.get(j);
			if (gf.name.equals("."))
				continue;

			String cl = p.CCMONO + p.genClass();
			String fn = p.escapeHTML(gf.name.length() > MAXFNLEN ?
			  gf.name.substring(0, MAXFNLEN) + "..." : gf.name) +
			  (gf.isDirectory() ? "/" : "");

			if (gf.name.equals(".."))
				type = "parentdir";
			else if (gf.isDirectory())
				type = "folder";
			else
				type = "file";
			String img = "" + oof.img(new Object[] {
				"src", prefix + type + ".png",
				"alt", "[img]",
				"align", "absmiddle",
				"border", "0"
			});

			if (gf.isDirectory())
				qs = buildQS(p, params, new String[] {
					display + "cwd", cwd + "/" + gf.name
				});
			else
				qs = buildQS(p, params, new String[] {
					"action", "download",
					"file", gf.name,
					"display", display
				});

			String cbox = "";
			if (!gf.name.equals(".."))
				cbox = "" + oof.input(new Object[] {
					"type", "checkbox",
					"name", "file",
					"value", p.escapeHTML(gf.name)
				});

			s += "" + oof.table_row(new Object[][] {
				new Object[] { "class", cl,
					"value", oof.link(img + " " + fn, qs) },
				new Object[] { "class", cl,
					"value", humansiz(gf.size),
					"align", "right" },
				new Object[] { "class", cl, "value",
					/* XXX: escapeHTML() these? */
					gf.date + " " + gf.time,
					"style", "text-align: center; white-space: nowrap" },
				new Object[] { "class", cl,
					"value", gf.perm,
					"align", "center" },
				new Object[] { "class", cl,
					"align", "center",
					"value", cbox
				}
			});
		}
		return (s);
	}

	public static String humansiz(long n) {
		String sufx = "BKMGTP";
		int idx = 0;

		double m = (double)n;
		/* XXX: use log(m, 1024) */
		while (m > 1024.0) {
			idx++;
			m /= 1024.0;
		}
		if (idx >= sufx.length())
			return "" + n;
		else
			return "" + (((long)(m * 10)) / 10.0) + sufx.charAt(idx);
	}

	public static String js_hostchg(String host) {
		/*
		 * XXX: use options[0].value instead
		 * of hardcoding option value.
		 */
		String s =
			"	var idx = this.selectedIndex;						" +
			"	var bv = 'Choose a resource...';					" +
			"	this.form.elements['" + host + "'].value =			" +
			"		(this.options[idx].value == bv) ?				" +
			"		'' : this.options[idx].value;					" +
			"	if (this.options[idx].value != '') {				" +
			"		this.form.submit();								" +
			"		var n = this.form.elements.length - 1;			" +
			"		this.form.elements[n].value = 'Please wait...';	" +
			"	}													";
		return (s);
	}

	/* Retrieve the list of hostname for the host drop down menu */
	public static Object[] createHostList(Page p)
		throws SQLException {

		PreparedStatement sth = p.getDBH().prepareStatement(
			"	SELECT					" +
			"			COUNT(*) AS cnt	" +
			"	FROM					" +
			"			hosts			" +
			"	WHERE					" +
			"			uid = ?			");	/* 1 */
		sth.setInt(1, p.getUID());
		ResultSet rs = sth.executeQuery();

		int nhosts = 0;
		if (rs.next())
			nhosts = rs.getInt("cnt");

		sth = p.getDBH().prepareStatement(
			"	SELECT					" +
			"			host			" +
			"	FROM					" +
			"			hosts			" +
			"	WHERE					" +
			"			uid = ?			");	/* 1 */
		sth.setInt(1, p.getUID());
		rs = sth.executeQuery();

		Object[] hlist = new Object[2 * nhosts + 2];
		hlist[0] = "";
		hlist[1] = "Choose a resource...";
		for (int i = 2; rs.next(); i += 2)
			hlist[i] = hlist[i + 1] = rs.getString("host");

		return hlist;
	}
};

/* vim: set ts=4: */
