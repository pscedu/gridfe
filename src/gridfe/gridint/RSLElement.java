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
	** Prepend Args
	** Specification:
	**	multi		+
	**	conjunct	&
	**	disjunct	|
	*/
	private String pre = "&";

	/* transient buffer, this can be rebuilt */
	private transient StringBuffer data;

	private String[] gParam, gValue, vValue, kValue, kKey;
	private String vParam, kParam;
	private String jOut;

	public RSLElement()
	{
	}

	public RSLElement(String[] param, String[] value)
	{
		this.setGenerics(param, value);
	}

	public RSLElement(String[] gp, String[] gv, String vp, String[] vv)
	{
		this.setGenerics(gp, gv);
		this.setVarArgs(vp, vv);
	}

	public RSLElement(String[] gp, String[] gv, String kp, String[] kk, String[] kv)
	{
		this.setGenerics(gp, gv);
		this.setKeyPairs(kp, kk, kv);
	}

	public RSLElement(String[] gp, String[] gv, String vp, String[] vv, String kp,
			  String[] kk, String[] kv)
	{
		this.setGenerics(gp, gv);
		this.setVarArgs(vp, vv);
		this.setKeyPairs(kp, kk, kv);
	}

	/*
	** Change whether it's Multi, Conjunct, or Disjunct
	** (default is conjunct)
	*/
	public void changeSpec(String s)
	{
		this.pre = new String(s);
	}

	public void setGenerics(String[] param, String[] value)
	{
		this.gParam = (String[])param.clone();
		this.gValue = (String[])value.clone();
	}

	public void setVarArgs(String param, String[] value)
	{
		this.vParam = new String(param);
		this.vValue = (String[])value.clone();
	}

	public void setKeyPairs(String param, String[] key, String[] value)
	{
		this.kParam = new String(param);
		this.kKey = (String[])key.clone();
		this.kValue = (String[])value.clone();
	}

	/*
	** Setup the file that will contain the job
	** output. This filename will be needed later
	** to retreive the data so a random name will
	** be given. Once the job has succesfully ran
	** the output file will be renamed according
	** to the jobID it is given by GRAM. However,
	** this filename can be overriden as can be
	** seen below ...
	*/
/*
	public void setStdout(String file)
	{
		this.jOut = new String(file);
	}

	public void setStdout()
	{
		// TODO: rand function for filename
		this.jOut = new String("/home/rbudden/rand.tmp");
	}
*/

	public String getStdout()
	{
		return this.jOut;
	}

/*
	public void buildStdout()
	{
		this.buildGenerics( new String[] {"stdout"}, new String[] {this.jOut});
	}
*/

	/* Generic build for "(param=value)" */
	public void buildGenerics(String[] param, String[] value)
	{
		//for(int i = 0; i < param.length; i++)
		//	this.data.append(b+param[i]+m+q+value[i]+q+e);

		for(int i = 0; i < param.length; i++)
		{
			this.data.append(b+param[i]+m+q+value[i]+q+e);

			/* Save stdout to retrieve job output */
			if(param[i] == "stdout")
				this.jOut = new String(value[i]);
		}
	}

	/*
	** Build in the form of '(param="arg1" "arg2")'
	** Example: (arguments="arg1" "arg number 2");
	*/
	public void buildVarArgs(String param, String[] value)
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
	public void buildKeyPairs(String param, String[] key, String[] value)
	{
		this.data.append(b+param+m);

		/* Quote all args to be safe! */
		for(int i = 0; i < key.length; i++)
			//this.data.append(b+q+key[i]+q+s+q+value[i]+q+e);
			this.data.append(b+key[i]+s+q+value[i]+q+e);

		this.data.append(e);
	}

	public void build()
	{
		this.data = new StringBuffer(pre);

		/* Build those that are not empty */
		if(this.gParam != null)
			this.buildGenerics(this.gParam, this.gValue);
		if(this.vParam != null)
			this.buildVarArgs(this.vParam, this.vValue);
		if(this.kParam != null)
			this.buildKeyPairs(this.kParam, this.kKey, this.kValue);

		/* Unless the stdout has manually been overriden */
/*
		if(this.jOut == null)
		{
			// Setup the random filename
			this.setStdout();
			this.buildStdout();
		}
*/
	}

	public String toString()
	{
		if(this.data == null)
			this.build();

		return this.data.toString();
	}
};
