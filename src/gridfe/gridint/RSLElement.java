/* $Id$ */

package gridfe.gridint;

import java.io.*;

public class RSLElement implements Serializable
{
	/* Strings in the form "&(executable=`which hostname`)..." */
	private final static String b = "(";
	private final static String m = "=";
	private final static String e = ")";
	private final static String s = " ";
	private final static String q = "\"";

	/*
	** Request Type
	** Specification:
	**	multi		+
	**	conjunct	&
	**	disjunct	|
	*/
	private String req = "&";

	private String[] gParam, gValue;
	private String[] vValue, kValue, kKey;
	private String vParam, kParam;

	/*
	** transient date will be reconstructed 
	*/
	private transient StringBuffer data;

	/* Stdout and Stderr default to null */
	public transient String stdout = null;
	public transient String stderr = null;

	/* Default directory to HOME */
	public transient String directory = "~";

	/*
	** Build RSL Strings that have args, env variables, and
	** standard parameters.
	*/
	public void setRSL(String[] param, String[] value)
	{
		this.gParam = (String[])param.clone();
		this.gValue = (String[])value.clone();
	}

	/* this one is internal only! */
	private void setRSL(String param, String[] key, String[] value)
	{
		this.kParam = new String(param);
		this.kKey = (String[])key.clone();
		this.kValue = (String[])value.clone();
	}

	public void setRSL(String[] gp, String[] gv, String vp, String[] vv)
	{
		this.setRSL(gp, gv);
		this.vParam = new String(vp);
		this.vValue = (String[])vv.clone();
	}

	public void setRSL(String[] gp, String[] gv, String kp, String[] kk,
				String[] kv)
	{
		this.setRSL(gp, gv);
		this.setRSL(kp, kk, kv);
	}

	public void setRSL(String[] gp, String[] gv, String vp, String[] vv,
				String kp, String[] kk, String[] kv)
	{
		this.setRSL(gp, gv, vp, vv);
		this.setRSL(kp, kk, kv);
	}

	/*
	** Change whether it's Multi, Conjunct, or Disjunct
	** (default is conjunct, see request type above...)
	*/
	public void setRequestType(String s)
	{
		this.req = new String(s);
	}

	/* Generic build for "(param=value)" */
	private void buildGenerics(String[] param, String[] value)
	{
		for(int i = 0; i < param.length; i++)
		{
			/*
			** XXX later will may want to intercept options
			** like stdout, stderr, directory, etc... so 
			** that we can do internal work first, then
			** change to what the user specified.
			** Example:
			** stdout was set to have output go to a different
			** computer via a GAAS server. first we intercept
			** stdout and have the data saved locally, we can then
			** open the file later to show job output through web,
			** and finally use GridFTP to send the file where they
			** wanted it... (something to this effect)
			*/
			this.data.append(b+param[i]+m+q+value[i]+q+e);

			/* Save some parameters to retrieve job output/err */
			if(param[i] == "stdout")
				this.stdout = new String(value[i]);
			if(param[i] == "stderr")
				this.stderr = new String(value[i]);
			if(param[i] == "directory")
				this.directory = new String(value[i]);
		}
	}

	/*
	** Build in the form of '(param="arg1" "arg2")'
	** Example: (arguments="arg1" "arg number 2");
	*/
	private void buildVarArgs(String param, String[] value)
	{
		int i;
		this.data.append(b+param+m);

		/* Quote all args to be safe! */
		for(i = 0; i < value.length - 1; i++)
			this.data.append(q+value[i]+q+s);

		/* Manually add last one to avoid extra " )" */
		this.data.append(q+value[i]+q);
		this.data.append(e);
	}

	/*
	** Build in the form of "(param=(key1 value1)(key2 value2))"
	** Example: (environment=(MANPATH /usr/man)(EDITOR vi));
	*/
	private void buildKeyPairs(String param, String[] key, String[] value)
	{
		this.data.append(b+param+m);

		/* Quote all args to be safe! */
		for(int i = 0; i < key.length; i++)
			this.data.append(b+key[i]+s+q+value[i]+q+e);

		this.data.append(e);
	}

	public void build()
	{
		this.data = new StringBuffer(this.req);

		/* Build those that are not empty */
		if(this.gParam != null)
			this.buildGenerics(this.gParam, this.gValue);
		if(this.vParam != null)
			this.buildVarArgs(this.vParam, this.vValue);
		if(this.kParam != null)
			this.buildKeyPairs(this.kParam, this.kKey, this.kValue);
	}

	public String toString()
	{
		/* make sure it's been built first! */
		if(this.data == null)
			this.build();

		return this.data.toString();
	}
};
