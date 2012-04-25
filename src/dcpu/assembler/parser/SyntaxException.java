package dcpu.assembler.parser;


/**
 * Represents a syntax error in a source file, that the parser might throw when
 * trying to parse its input.
 *
 */
public class SyntaxException extends RuntimeException {
	private Token token;
	
	/**
	 * Creates a new SyntaxException with the specified message explaining what
	 * the error is.
	 */
	public SyntaxException(String msg) {
		super(msg);
	}
	
	/**
	 * Creates a new SyntaxException with the specified message explaining what
	 * the error is, associated with the specified Token.
	 */
	public SyntaxException(Token token, String msg) {
		super(msg + " (near " + token + ")\n" + token.getPosition().getArrow());
		this.token = token;
	}
}
