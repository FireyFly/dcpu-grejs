package dcpu.assembler.parser;



/**
 * Represents a given token in the token stream that the lexer emits.
 *
 */
public class Token {
	private String        type;
	private String        value;
	private TokenPosition position;
	
	/**
	 * Creates a new token of the given type, associated with the given value.
	 * The value of a token is the characters that the token consists of.  No
	 * character belongs to multiple tokens.
	 * 
	 * @param type  the type of the token.
	 * @param value the value that this token holds.
	 */
	public Token(String type, String value, TokenPosition pos) {
		this.type     = type;
		this.value    = value;
		this.position = pos;
	}
	
	/**
	 * Returns the type of this token.
	 */
	public String getType() {
	    return type;
    }
	
	/**
	 * Returns the value associated with this token, i.e. the characters that it
	 * consists of.
	 */
	public String getValue() {
	    return value;
    }
	
	/**
	 * Returns the position of this Token.
	 */
	public TokenPosition getPosition() {
	    return position;
    }
	
	@Override
	public String toString() {
		return "[" + this.type + ": '" + this.value + "']";
	}
}
