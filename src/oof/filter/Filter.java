/* $Id$ */
package oof.filter;

import jasp.*;
import oof.*;
import oof.element.*;

public abstract class FILTER {
	protected OOF oof;
	protected JASP jasp;

	public FILTER(JASP jasp, OOF oof) {
		this.jasp = jasp;
		this.oof  = oof;
	}

	public abstract String build(Break e);
	public abstract String build(Code e);
	public abstract String build(DivisionEnd e);
	public abstract String build(Division e);
	public abstract String build(DivisionStart e);
	public abstract String build(Email e);
	public abstract String build(Emphasis e);
	public abstract String build(Fieldset e);
	public abstract String build(FormEnd e);
	public abstract String build(Form e);
	public abstract String build(FormStart e);
	public abstract String build(Header e);
	public abstract String build(HorizontalRuler e);
	public abstract String build(Image e);
	public abstract String build(Input e);
	public abstract String build(Link e);
	public abstract String build(ListEnd e);
	public abstract String build(ListItem e);
	public abstract String build(List e);
	public abstract String build(ListStart e);
	public abstract String build(Paragraph e);
	public abstract String build(Preformatted e);
	public abstract String build(Span e);
	public abstract String build(Strong e);
	public abstract String build(TableEnd e);
	public abstract String build(Table e);
	public abstract String build(TableRow e);
	public abstract String build(TableStart e);
};
