/* $Id$ */

package gridfe.gridint;

import java.io.*;
import java.util.*;

/*
 * JobList combines both a hash table and an
 * array list.  This allows us to keep track of jobs
 * by both order and name.
 */
public class JobList implements Serializable {
	private List list;
//	private Hashtable table;

	public JobList() {
		this.list = new ArrayList();
//		this.table = new Hashtable();
	}

	public void add(GridJob j) {
		/* Add to end of list. */
		this.list.add(j);

		/* Save index into hash table. */
//		this.table.put(j.getName(), this.genQID());
	}

	public boolean remove(GridJob j) {
		/* Remove from hashtable. */
//		this.table.remove(j.getQID());

		/* Remove from list array. */
		return (this.list.remove(j));
	}

	public GridJob get(int qid) {
		for (int i = 0; i < this.list.size(); i++)
			if (((GridJob)this.list.get(i)).getQID() == qid)
				return ((GridJob)this.list.get(i));
		return (null);
//		return ((GridJob)this.table.get(qid));
	}

	public List getList() {
		return (this.list);
	}

	public int genQID() {
		int qid;
		Random r = new Random();
		do {
			qid = Math.abs(r.nextInt()) % 32768;
		} while (this.get(qid) != null);
		return (qid);
	}
};
