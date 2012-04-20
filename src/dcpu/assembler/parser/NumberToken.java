package dcpu.assembler.parser;

public class NumberToken extends Token {
	private int integerValue;
	
	public NumberToken(String type, String value, TokenPosition pos) {
		super(type, value, pos);
		this.integerValue = parseNumber(value);
    }
	
	private static int parseNumber(String str) {
		if (str.startsWith("0x")) {
			return Integer.parseInt(str.substring(2), 16);
		} else {
			return Integer.parseInt(str);
		}
	}
	
	/**
	 * Returns the integer value that this number token wraps.
	 */
	public int getIntegerValue() {
	    return integerValue;
    }
}
