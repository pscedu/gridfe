/* $Id$ */
/*
** Data type wrapper over the uid
*/
package gridint.auth;

public class Uid
{
	private int uid;

	public Uid(int uid)
	{
		this.uid = uid;
	}

	public Uid(Integer uid)
	{
		this.uid = uid.intValue();
	}
	
	public Uid(String uid)
	{
		Integer i = new Integer(uid);
		this.uid = i.intValue();
	}

	public int intValue()
	{
		return this.uid;
	}
}

