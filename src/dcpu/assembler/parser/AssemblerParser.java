package dcpu.assembler.parser;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains tools for the syntax analysis part of the DCPU assembly parser.
 */
public class AssemblerParser {
	
	/**
	 * Splits a list of Tokens on line-feed tokens (type "LF"), into a list of
	 * lines (where each line is represented by a list of tokens).
	 * @param tokens  the tokens to perform the splitting on.
	 * @return        the resulting list of lists of tokens, where the inner
	 *                lists represent lines.
	 */
	public static List<List<Token>> splitLines(List<Token> tokens) {
		List<List<Token>> res = new ArrayList<List<Token>>();
		
		List<Token> sublist = new ArrayList<Token>();
		res.add(sublist);
		
		for (Token token : tokens) {
			if (token.getType().equals("LF")) {
				sublist = new ArrayList<Token>();
				res.add(sublist);
				
			} else {
				sublist.add(token);
			}
		}
		
		return res;
	}
}
