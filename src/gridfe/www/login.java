/* $Id$ */

package gridfe.www;

import gridfe.*;
import oof.*;

public class login
{
	public static String main(Page page)
		throws Exception
	{
		String s = "";
		OOF oof = page.getOOF();

		if (false /* already logged on */) {
			/* XXX: make more welcomish. */
			s +=	page.header("Error") +
					oof.p("You are already logged on.") +
					page.footer();
		} else {
			/* Display login form. */
			s +=	page.header("Log In") +
					oof.form(
						new Object[] { "method", "post" },
						oof.table(
							new Object[] {},
							new Object[][][] {			/* The table */
								new Object[][] {		/* Row 1 */
									new Object[] {		/* Row 1, Column 1 */
										"class", Page.CCDESC,
										"value", "Username:"
									},
									new Object[] {		/* Row 1, Column 2 */
										"class", page.genClass(),
										"value", oof.input(new Object[] {
											"type", "text",
											"name", "username"
										})
									}
								},
								new Object[][] {		/* Row 2 */
									new Object[] {		/* Row 2, Column 1 */
										"class", Page.CCDESC,
										"value", "Password:"
									},
									new Object[] {		/* Row 2, Column 2 */
										"class", page.genClass(),
										"value", oof.input(new Object[] {
											"type", "password",
											"name", "password"
										})
									}
								}
							}
						)
					) +
					page.footer();
		}
		return s;
	}
};

/* vim: set ts=4: */
