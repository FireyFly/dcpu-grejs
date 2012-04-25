package dcpu.assembler.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * Lexer for the DCPU-16 assembly language.
 * 
 * @author Jonas Höglund
 * @verison 2012-04-10
 */
public class AssemblerLexer {
	private static final Pattern
			PATTERN_NOT_ALNUM = Pattern.compile("[^\\w\\d]"),
			PATTERN_NOT_HEX   = Pattern.compile("[^\\da-fA-F]"),
			PATTERN_NOT_DIGIT = Pattern.compile("\\D");
	
	/**
	 * Splits an input string into a list of tokens. The given filename is used
	 * for debugging and error reporting.
	 * 
	 * @param input
	 *            the input to process.
	 * @param filename
	 *            the filename of the file that the input came from.
	 * @return a list of tokens.
	 */
	public static List<Token> lex(String input, String filename) {
		List<Token> res = new ArrayList<Token>();
		
		int row   = 1
		  , rowI0 = 0;
		
		String line = AssemblerLexer.upto('\n', input, 0);

		// Iterate over the input, adjusting the input pointer as necessary.
		for (int i = 0, length = input.length(); i < length; i++) {
			char chr = input.charAt(i);
			
			String name, value;
			Token token;

			// Ignore all non-LF whitespace (for all purposes other than to
			// separate tokens).
			if (AssemblerLexer.isIgnoreable(chr)) {
				continue;

			// Line feed tokens
			} else if (chr == '\n') {
				// Emit a newline token.
				name  = "LF";
				value = "\n";
				
				// Update the "current line" metadata (used for error reporting
				// in emitted tokens).
				row++;
				rowI0 = i;
				line  = upto('\n', input, i + 1);
			
			// Comma separator tokens
			} else if (chr == ',') {
				name  = "COMMA";
				value = ",";
				
			// Various types of parens.
			} else if (isParenLike(chr)) {
				name  = "PAREN";
				value = Character.toString(chr);

			// Operators
			} else if (isOperator(chr)) {
				name  = "OPERATOR";
				value = Character.toString(chr);

			// Label declarations
			} else if (chr == ':') {
				String content = upto(PATTERN_NOT_ALNUM, input, i + 1);
				
				name  = "LABEL";
				value = ":" + content;
				
			// Names & registers
			} else if (Character.isLetter(chr)) {
				String content = upto(PATTERN_NOT_ALNUM, input, i);
				
				name  = "NAME";
				value = content;
			
			// Number literals
			} else if (Character.isDigit(chr)) {
				if (chr == '0' && input.length() > i+1
						       && input.charAt(i + 1) == 'x') {
					// Hex literal: 0x1234
					String content = upto(PATTERN_NOT_HEX, input, i + 2);
					
					name  = "NUMBER";
					value = "0x" + content;
					
				} else {
					// Decimal literal
					String content = upto(PATTERN_NOT_DIGIT, input, i);
					
					name  = "NUMBER";
					value = content;
					
				}
			
			// Comments
			} else if (chr == ';') {
				i += AssemblerLexer.upto('\n', input, i).length();
				continue;
			
			// Unknown character
			} else {
				throw new SyntaxException("Unknown character: '" + chr + "'");
			}
			
			int column = i - rowI0;
			TokenPosition pos = new TokenPosition(filename, row, column, line);
			
			if (name.equals("NUMBER")) {
				res.add(new NumberToken(name, value, pos));
			} else {
				res.add(new Token(name, value, pos));
			}
			
			// Skip over the characters that the token consumed.
			i += value.length() - 1;
		}

		return res;
	}

	/**
	 * Returns the substring of a string leading up to the given pattern, or the
	 * whole string if the pattern is not encountered.
	 */
	private static String upto(Pattern pattern, String str, int offset) {
		Matcher matcher = pattern.matcher(str);
		
		if (!matcher.find(offset)) {
			return str.substring(offset);
		} else {
			return str.substring(offset, matcher.start());
		}
	}
	
	/**
	 * Returns the substring of a string leading up to the given character, or
	 * the whole string if the character is not encountered.
	 */
	private static String upto(char chr, String str, int offset) {
		int idx = str.indexOf(chr, offset);
		
		if (idx == -1) {
			return str.substring(offset);
		} else {
			return str.substring(offset, idx);
		}
	}
	
	// Helper functions
	/** Returns whether a character is ignoreable. */
	private static boolean isIgnoreable(char chr) {
		return chr == '\t' || chr == '\f' || chr == ' ' || chr == '\r';
	}

	/** Returns whether a character is parenthesis-like, i.e. is paired with a
	 *  corresponding closing character. */
	private static boolean isParenLike(char chr) {
		return chr == '[' || chr == ']';
	}
	
	/** Returns whether a character represents an operator. */
	private static boolean isOperator(char chr) {
		return chr == '+' || chr == '-' || chr == '*' || chr == '/';
	}
}
