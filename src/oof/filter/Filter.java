/* $Id$ */
package oof.filter;

import jasp.*;
import oof.*;
import oof.element.*;

public interface Filter {
	public String build(Break e);
	public String build(Code e);
	public String build(DivisionEnd e);
	public String build(Division e);
	public String build(DivisionStart e);
	public String build(Email e);
	public String build(Emphasis e);
	public String build(Fieldset e);
	public String build(FormEnd e);
	public String build(Form e);
	public String build(FormStart e);
	public String build(Header e);
	public String build(HorizontalRuler e);
	public String build(Image e);
	public String build(Input e);
	public String build(Link e);
	public String build(ListEnd e);
	public String build(ListItem e);
	public String build(List e);
	public String build(ListStart e);
	public String build(Paragraph e);
	public String build(Preformatted e);
	public String build(Span e);
	public String build(Strong e);
	public String build(TableEnd e);
	public String build(Table e);
	public String build(TableRow e);
	public String build(TableStart e);
};
