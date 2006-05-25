/* $Id$ */

package gridfe.gridint;

import java.io.*;

public class RSLElement implements Serializable {
	/*
	 * Request Type
	 * Specification:
	 *	multi		+
	 *	conjunct	&
	 *	disjunct	|
	 */
	private String req = "&";

	private String[] gParam, gValue;
	private String[] vValue, kValue, kKey;
	private String vParam, kParam;

	/*
	 * Transient date will be reconstructed
	 * during revive() calls from GridJob.
	 */
	private transient StringBuffer data;

	/* stdout and stderr default to null */
	public transient String stdout = null;
	public transient String stderr = null;

	/* Default directory to HOME */
	public transient String dir = null;

	/*
	 * Build RSL Strings that have args, env variables, and
	 * standard parameters.
	 */
	public void setRSL(String[] param, String[] value) {
		this.gParam = (String[])param.clone();
		this.gValue = (String[])value.clone();
	}

	private void setRSL(String param, String[] key, String[] value) {
		this.kParam = param;
		this.kKey = (String[])key.clone();
		this.kValue = (String[])value.clone();
	}

	public void setRSL(String[] gp, String[] gv, String vp,
	    String[] vv) {
		this.setRSL(gp, gv);
		this.vParam = vp;
		this.vValue = (String[])vv.clone();
	}

	public void setRSL(String[] gp, String[] gv, String kp, String[] kk,
	    String[] kv) {
		this.setRSL(gp, gv);
		this.setRSL(kp, kk, kv);
	}

	public void setRSL(String[] gp, String[] gv, String vp, String[] vv,
	    String kp, String[] kk, String[] kv) {
		this.setRSL(gp, gv, vp, vv);
		this.setRSL(kp, kk, kv);
	}

	/*
	 * Change whether it's multi, conjunct, or disjunct
	 * (default is conjunct, see request type above...)
	 */
	public void setRequestType(String s) {
		this.req = s;
	}

	/* Generic build for "(param=value)" */
	private void buildGenerics(String[] param, String[] value) {
		for (int i = 0; i < param.length; i++) {
			this.data.append("(" + param[i] + "=" +
			    '"' + value[i] + "\")");

			/* Save some parameters to retrieve job output/err */
			if (param[i].equals("stdout"))
				this.stdout = value[i];
			else if (param[i].equals("stderr"))
				this.stderr = value[i];
			else if (param[i].equals("directory"))
				this.dir = value[i];
		}
	}

	/*
	 * Build in the form of '(param="arg1" "arg2")'
	 * Example: (arguments="arg1" "arg number 2")
	 */
	private void buildVarArgs(String param, String[] value) {
		int i;

		this.data.append("(" + param + "=");

		/* Quote all args to be safe */
		for (i = 0; i < value.length - 1; i++)
			this.data.append('"' + value[i] + "\" ");

		/* Manually add last one to avoid extra " )" */
		this.data.append('"' + value[i] + "\")");
	}

	/*
	 * Build in the form of "(param=(key1 value1)(key2 value2))"
	 * Example: (environment=(MANPATH /usr/man)(EDITOR vi))
	*/
	private void buildKeyPairs(String param, String[] key,
	    String[] value) {
		this.data.append("(" + param + "=");

		/* Quote all args to be safe */
		for (int i = 0; i < key.length; i++)
			this.data.append("(" + key[i] + " " +
			  '"' + value[i] + "\")");
		this.data.append(")");
	}

	public void build() {
		this.data = new StringBuffer(this.req);

		/* Build those that are not empty */
		if (this.gParam != null)
			this.buildGenerics(this.gParam, this.gValue);
		if (this.vParam != null)
			this.buildVarArgs(this.vParam, this.vValue);
		if (this.kParam != null)
			this.buildKeyPairs(this.kParam, this.kKey,
			    this.kValue);
	}

	/* Serializable Implementation */
	private void writeObject(ObjectOutputStream out)
	    throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in)
	    throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		/* Rebuild RSLElement */
		this.build();
	}

	public String toString() {
		/* make sure it's been built first */
		if (this.data == null)
			this.build();
		return (this.data.toString());
	}

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
	public void parse(String rsl) {
		int level = 0;
		boolean dquot = false;
		boolean squot = false;

		for (int i = 0; i < rsl.length(); i++) {
			char c = rsl.charAt(i);

			switch (c) {
			case '(':
				if (!squot && !dquot)
					level++;
				break;
			case ')':
				if (!squot && !dquot)
					level--;
				break;
			case '\'':
				if (!dquot)
					dquot = !dquot;
				break;
			case '"':
				if (!squot)
					squot = !squot;
				break;
			case '=':
				break;
			}

		}
	}
};
