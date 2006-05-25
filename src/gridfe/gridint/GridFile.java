/* $Id$ */

package gridfe.gridint;

import java.io.*;
import java.net.*;
import java.util.*;
import org.ietf.jgss.*;
import org.globus.io.urlcopy.*;
import org.globus.util.*;
import org.globus.ftp.*;
import org.globus.ftp.exception.*;

public class GridFile implements Comparable {
	public String name;
	public String date;
	public String time;
	public String perm;
	public long size;
	private FileInfo file;
	private boolean isDir;
	private boolean isFile;

	public GridFile() {}
	public GridFile(FileInfo file) {
		this.set(file);
	}

	public boolean isDirectory() {
		return (this.isDir);
	}

	public void set(FileInfo file) {
		this.file = file;
		this.name = file.getName();
		this.date = file.getDate();
		this.time = file.getTime();
		this.size = file.getSize();
		this.calcPerm();
		this.isDir = file.isDirectory();
		this.isFile = file.isFile();
	}

	private void calcPerm() {
		/* XXX - directory? */
		this.perm = "";
		this.perm += this.file.userCanRead()	? 'r' : '-';
		this.perm += this.file.userCanWrite()	? 'w' : '-';
		this.perm += this.file.userCanExecute()	? 'x' : '-';
		this.perm += this.file.groupCanRead()	? 'r' : '-';
		this.perm += this.file.groupCanWrite()	? 'w' : '-';
		this.perm += this.file.groupCanExecute()? 'x' : '-';
		this.perm += this.file.allCanRead()	? 'r' : '-';
		this.perm += this.file.allCanWrite()	? 'w' : '-';
		this.perm += this.file.allCanExecute()	? 'x' : '-';
	}

	/* I.e - "-rw-rw--r-- 6234 May 17 15:04" */
	public String longList() {
		String s = "";
		s += this.perm+"\t"+this.size+"\t"+this.date+"\t"+this.time;
		return s;
	}

	public String toString() {
		return this.longList() + "\t" + this.name;
	}

	public int compareTo(Object o) {
		return (this.name.compareTo(((GridFile)o).name));
	}
}
