package dcpu.assembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import dcpu.assembler.parser.AssemblerLexer;
import dcpu.assembler.parser.AssemblerParser;
import dcpu.assembler.parser.SyntaxException;
import dcpu.assembler.parser.Token;


public class Assembler {
	public static short[] assemble(String input, String filename) {
		List<Token>       tokens = AssemblerLexer.lex(input, "<input>");
		List<List<Token>> lines  = AssemblerParser.splitLines(tokens);
		
		return assemble(lines, filename);
	}
	
	public static short[] assemble(List<List<Token>> lines, String filename) {
		Map<String, Integer> labelMap = new HashMap<String, Integer>();
		int offsetCtr = 0; // Keeps track of the offset of the current instruction.
		
		List<Instruction> instructions = new ArrayList<Instruction>();
		
		// First pass: turn the lines (represented as token arrays) into a label
		//   map and an array of "instruction" objects.
		for (List<Token> line : lines) {
			// Skip empty lines.
			if (line.isEmpty()) { continue; }
			
			//-- Handle (eventual) label.
			Token firstToken = line.get(0);
			if (firstToken.getType().equals("LABEL")) {
				String label = firstToken.getValue().substring(1);
				
				if (labelMap.containsKey(label)) {
					throw new SyntaxException(firstToken, "Label already defined.");
				}
				
				labelMap.put(label, offsetCtr);
				
				// Remove the label we handled from the line. 
				line.remove(0);
			}
			
			// If line contained only a label, then continue with next line.
			if (line.isEmpty()) { continue; }
			
			//-- Handle instruction.
			Token mnemonicToken = line.get(0);
			assertSyntax(isMnemonic(mnemonicToken.getValue()), mnemonicToken,
					"Invalid instruction mnemonic.");
			
			line.remove(0);
			List<List<Token>> params = splitParams(line);
			
			Opcode opcode = Opcode.valueOf(mnemonicToken.getValue().toUpperCase());
			
			Instruction instr;
			if (opcode.isExtended()) {
				assertSyntax(params.size() == 1, mnemonicToken,
						"Expected 1 parameter but found " + params.size() + ".");
			
				Value b = new Value(params.get(0));
				instr   = new Instruction(opcode, b);
				
			} else {
				assertSyntax(params.size() == 2, mnemonicToken,
						"Expected 2 parameters but found " + params.size() + ".");
				
				Value a = new Value(params.get(0))
				    , b = new Value(params.get(1));
				instr   = new Instruction(opcode, a, b);
			}
			
			instructions.add(instr);
			offsetCtr += instr.getSize();
		}
		
		// Final pass: turn instructions into words
		short[] res = new short[offsetCtr];
		
		ListIterator<Instruction> iter = instructions.listIterator();
		for (int i=0; i<offsetCtr;) {
			Instruction instr = iter.next();
			short[]     words = instr.assemble();
			
			for (short word : words) {
				res[i++] = word;
			}
		}
		
		return res;
	}
	
		// GPR
		// SPR
		// "[" GPR "]"
		// "[" value "+" GPR "]"
		// "[" value "]"
		// value
	
	/**
	 * Splits a list of Tokens on comma tokens (type "COMMA"), into a list of
	 * lists of tokens representing parameters to an instruction.
	 */
	private static List<List<Token>> splitParams(List<Token> tokens) {
		List<List<Token>> res = new ArrayList<List<Token>>();
		
		List<Token> sublist = new ArrayList<Token>();
		res.add(sublist);
		
		for (Token token : tokens) {
			if (token.getType().equals("COMMA")) {
				sublist = new ArrayList<Token>();
				res.add(sublist);
				
			} else {
				sublist.add(token);
			}
		}
		
		return res;
	}
	
	/**
	 * Checks whether the given string is a proper mnemonic.
	 */
	private static boolean isMnemonic(String str) {
		String[] mnemonics = {
				"JSR", "SET", "ADD", "SUB",
				"MUL", "DIV", "MOD", "SHL",
				"SHR", "AND", "BOR", "XOR",
				"IFE", "IFN", "IFG", "IFB"
		};
		
		String uppercased = str.toUpperCase();
		
		for (String mnem : mnemonics) {
			if (uppercased.equals(mnem)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static void assertSyntax(boolean cond, Token token, String message) {
		if (!cond) {
			throw new SyntaxException(token, message);
		}
	}
}
