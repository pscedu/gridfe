/* $Id$ */
public class XHTML extends Filter {
	private OOF oof;

	public build(BASE e) {
		HTMLElement html = new HTMLElement(e.name);
		String s;
		Attribute attr;


		return html.build();
	}
	
	public build(Paragraph p) {
		return this.build(p);
	}
}
