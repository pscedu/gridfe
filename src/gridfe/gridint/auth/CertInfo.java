/* $Id$ */

package gridfe.gridint.auth;

/* Certificate Information */
public class CertInfo {
	public int type;
	public int key;
	public long time;
	public String sub;
	public String issuer;
	public String ident;
	public String xfile;
	public String kfile;
	
	CertInfo(String sub, int type, String issuer, int key,
	    String ident, long time, String xfile, String kfile) {
		this.sub = sub; 
		this.type = type;
		this.issuer = issuer;
		this.key = key;
		this.ident = ident;
		this.time = time;
		this.xfile = xfile;
		this.kfile = kfile;
	}
};
