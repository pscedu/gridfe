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
/*
	public GridJob pop()
	{
		return (GridJob)(this.list.remove(0));
	}
*/
	public boolean remove(GridJob j)
	{
		this.table.remove(j.getName());

		return this.list.remove(j);
	}
/*
	public GridJob remove(int index)
	{
		return (GridJob)(this.list.remove(index));
	}
*/
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
/*
	public void set(int index, GridJob j)
	{
		this.list.set(index, j);
	}
*/
	public int size()
	{
		return this.list.size();
	}
/*
	public void clear()
	{
		this.list.clear();
	}
*/
	public Enumeration keys()
	{
		/* Return the list of keys */
		return this.table.keys();
	}
};
