/* $Id$ */

package gridint.auth;

import java.io.*;
import java.security.cert.*;
import java.security.cert.CertificateException;

public class X509Auth
{
	private CertificateFactory cf;
	private X509Certificate cert;
	private String certFile;

	public X509Auth()
	{
		cf = null;
		cert = null;
		certFile = null;
	}

	public X509Auth(String file)
	{
		cf = null;
		cert = null;
		certFile = file;
	}

	/*
	 * Instantiate an X509 Certificate...
	 * certificate is obtained through
	 * the locally stored X509 cert which
	 * is usually in /tmp/x509up_u++++
	 * where ++++ is the user's UID
	 */
	public void instantiate() throws CertificateException, IOException
	{
		InputStream iStream;
		iStream = new FileInputStream(certFile);

		cf = CertificateFactory.getInstance("X.509");
		
		cert = (X509Certificate)(cf.generateCertificate(iStream));
		
		iStream.close();
	}

	//public X509Certificate getCertificate() throws Certificate Exception
	public X509Certificate getCertificate()
	{
		/* Check validity first... */
		//this.checkValidity();
		return cert;
	}

	/*
	 * X509Certificate Wrappers 
	 */

	public void checkValidity() throws CertificateException
	{
		cert.checkValidity();
	}

};

