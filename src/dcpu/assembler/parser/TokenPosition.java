package dcpu.assembler.parser;


/**
 * Represents the position of a given token.
 */
public class TokenPosition {
	private final String filename;
	private final int    column;
	private final int    row;
	private final String line;
	
	/**
	 * Creates a new Position, with the specified filename, row, column and line.
	 */
	public TokenPosition(String filename, int row, int column, String line) {
		this.filename = filename;
		this.row      = row;
		this.column   = column;
		this.line     = line;
	}
	
	/**
	 * Returns a string representation of this Position.
	 */
	@Override
	public String toString() {
		return this.filename + ":" + this.row + ":" + this.column;
	}
	
	private String getArrow() {
		StringBuilder builder = new StringBuilder(this.column);
		
		for (int i=0; i<this.column; i++) {
			builder.append(' ');
		}
		builder.append('^');
		
		return builder.toString();
	}
}
