/* $Id$ */

public class suite {
	public static void main(String args[]) throws Exception {
		System.setProperty("java.class.path",
			System.getProperty("java.class.path") + ":..");
		Page p = new Page();
		System.out.println(login.main(p));
	}
}
