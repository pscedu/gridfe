/* $Id$ */

package gridfe.gridint;

import gridfe.gridint.*;
import java.io.*;
import java.util.*;

/*
** JobList combines both a hash table and an
** array list.  This allows us to keep track of jobs
** by both order and name.
*/
public class JobList implements Serializable
{
	private List list;
	private Hashtable table;

	public JobList()
	{
		this.list = new ArrayList();
		this.table = new Hashtable();
	}

	public void push(GridJob j)
	{
		/* Add to end of list */
		this.list.add(j);

		/* Save index into hashtable */
		this.table.put(j.getName(), new Integer(this.list.size() - 1));
	}

	public boolean remove(GridJob j)
	{
		/* Remove from hashtable */
		this.table.remove(j.getName());

		/* Remove from list array */
		return this.list.remove(j);
	}

	/* Return most recent job submission */
	public GridJob get()
	{
		int index = this.list.size() - 1;
		return (GridJob)(this.list.get(index));
	}

	public GridJob get(int index)
	{
		return (GridJob)(this.list.get(index));
	}
	
	public GridJob get(String name)
	{
		/* Grab the index from the hashtable */
		int index = ((Integer)(this.table.get(name))).intValue();

		/* Then grab the job */
		return (GridJob)(this.list.get(index));
	}
	
	public int size()
	{
		return this.list.size();
	}

//	public Enumeration keys()
//	{
		/* Return the list of keys */
//		return this.table.keys();
//	}
};
