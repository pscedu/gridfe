/* $ID$ */

package gridfe.gridint.auth;

/* Certificate Information */
public class CertInfo
{
	public int type;
	public int key;
	public long time;
	public String sub;
	public String issuer;
	public String ident;
	
	CertInfo(String sub, int type, String issuer, 
		int key, String ident, long time)
	{
		this.sub = sub; 
		this.type = type;
		this.issuer = issuer;
		this.key = key;
		this.ident = ident;
		this.time = time;
	}
}

