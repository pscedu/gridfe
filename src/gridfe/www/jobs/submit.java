/* $Id$ */

package gridfe.www.jobs;

import gridfe.*;
import oof.*;

public class submit {
	public static String main(Page p)
	  throws Exception {
		OOF oof = p.getOOF();
		String s = "";

		s += p.header("Submit Job")
		   + oof.form(
				new Object[] {},
				new Object[] {
					oof.table(
						new Object[] {},
						new Object[][][] {
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
									"value", "Label:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", oof.input(new Object[] {
												"type", "text",
												"name", "label"
											}) }
							},
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
									"value", "Host:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", oof.input(new Object[] {
												"type", "text",
												"name", "host"
											})
								}
							},
							new Object[][] {
								new Object[] {
									"class", p.CCDESC,
									"value", "Command:"
								},
								new Object[] {
									"class", p.genClass(),
									"value", oof.input(new Object[] {
												"type", "textarea",
												"name", "command"
											})
								}
							}
						}
					)
				}
			 )
		   + p.footer();
		return (s);
	}
};

/* vim: set ts=4: */
