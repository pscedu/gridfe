/* $Id$ */

package gridfe.gridint;

import java.io.*;
import java.util.*;

public class RSLElement implements Serializable {
	private String req = "&";
	private HashMap pairs;

	public RSLElement() {
		this.pairs = new HashMap();
	}

	/* Serializable */
	private void writeObject(ObjectOutputStream out)
	    throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in)
	    throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}

	public String getStdout() {
		return ((String)this.pairs.get("stdout"));
	}

	public String getStderr() {
		return ((String)this.pairs.get("stderr"));
	}

	public String getDirectory() {
		return ((String)this.pairs.get("directory"));
	}

	public HashMap getMap() {
		return (this.pairs);
	}

	public String build() {
		return build(this.req, this.pairs, new String[] { });
	}

	public String build(String[] skip) {
		return build(this.req, this.pairs, skip);
	}

	private static boolean inArray(Object o, Object[] list) {
		if (list == null)
			return (false);
		for (int i = 0; i < list.length; i++)
			if (list[i].equals(o))
				return (true);
		return (false);
	}

	public static String build(String req, HashMap m, String[] skip) {
		String name, iv, s = "";

		for (Iterator k = m.keySet().iterator();
		     k.hasNext() && (name = (String)k.next()) != null; ) {
			if (inArray(name, skip))
				continue;
			Object val = m.get(name);
			s += "(" + name;
			if (req != null)
				s += " =";
			if (val instanceof HashMap)
				s += build(null, (HashMap)val, null);
			else if (val instanceof String[]) {
				String[] vs = (String[])val;

				for (int j = 0; j < vs.length; j++)
					s += " \"" + vs[j] + '"';
			} else if (val instanceof List) {
				List l = (List)val;
				String lv;

				for (Iterator i = l.iterator();
				  i.hasNext() && (lv = (String)i.next()) != null; )
					s += " \"" + lv + '"';
			} else
				s += " \"" + (String)val + '"';
			s += ")";
		}
		if (!s.equals("") && req != null)
			s = req + s;
		return (s);
	}

	public String extraRSL() {
		return (this.build(new String[] {
		  "arguments", "executable",
		  "stdout", "stderr", "queue" }));
	}

	public String toString() {
		return (this.build());
	}
};

/*
	(* this is a comment *)
	& (executable = a.out (* <-- that is an unquoted literal *))
	  (directory  = /home/nobody )
	  (arguments  = arg1 "arg 2")
	  (count = 1)

	& (rsl_substitution  = (TOPDIR  "/home/nobody")
	                (DATADIR $(TOPDIR)"/data")
	                (EXECDIR $(TOPDIR)/bin) )
	  (executable = $(EXECDIR)/a.out
	        (* ^-- implicit concatenation *))
	  (directory  = $(TOPDIR) )
	  (arguments  = $(DATADIR)/file1
	        (* ^-- implicit concatenation *)
	                $(DATADIR) # /file2
	        (* ^-- explicit concatenation *)
	                '$(FOO)'            (* <-- a quoted literal *))
	  (environment = (DATADIR $(DATADIR)))
	  (count = 1)
*/
