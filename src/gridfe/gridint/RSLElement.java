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
		return build(this.req, this.pairs);
	}

	public static String build(String req, HashMap m) {
		String name, iv, s = "";

		if (req != null)
			s += req;
		for (Iterator k = m.keySet().iterator();
		     k.hasNext() && (name = (String)k.next()) != null; ) {
			Object val = m.get(name);

			s += "(" + name;
			if (req != null)
				s += " =";
			if (val instanceof HashMap)
				s += build(null, (HashMap)val);
			else if (val instanceof String[]) {
				String[] vs = (String[])val;
				for (int j = 0; j < vs.length; j++)
					s += " \"" + vs[j] + '"';
			} else
				s += " \"" + (String)val + '"';
			s += ")";
		}
		return (s);
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
