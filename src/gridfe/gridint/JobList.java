/* $Id$ */
/*
** GridJobList -
** Wrapper over a GridJob manager.
** Something to maintain past & present
** submitted GridJobs
*/

package gridint;

import gridint.*;
import java.io.*;
import java.util.*;

public class GridJobList implements Serializable
{
	private List list;

	public GridJobList()
	{
		this.list = new ArrayList();
	}

	public void push(GridJob j)
	{
		this.list.add(0, j);
	}

	public GridJob pop()
	{
		return (GridJob)(this.list.remove(0));
	}

	public boolean remove(GridJob j)
	{
		return this.list.remove(j);
	}

	public GridJob remove(int index)
	{
		return (GridJob)(this.list.remove(index));
	}

	public GridJob get(int index)
	{
		return (GridJob)(this.list.get(index));
	}

	public void set(int index, GridJob j)
	{
		this.list.set(index, j);
	}

	public int size()
	{
		return this.list.size();
	}

	public void clear()
	{
		this.list.clear();
	}
}
