package dcpu.assembler;

import java.util.List;
import java.util.Map;

import dcpu.assembler.parser.NumberToken;
import dcpu.assembler.parser.SyntaxException;
import dcpu.assembler.parser.Token;


/**
 * A Value represents one of the parameters to an operation.  Depending on
 * the type of value, one or both of `register` and `value` may be used.
 */
public class Value {
	public static enum ValueType {
		GPR,                // register (A, B, C, X, Y, Z, I, J)
		GPR_DEREF,          // [register]
		GPR_RELATIVE_DEREF, // [next word + register]
		SPR,                // POP, PEEK, PUSH, SP, PC, O
		CONST,              // next word
		CONST_DEREF         // [next word]
	}
	
	private static enum GPR { A, B, C, X, Y, Z, I, J }
	
	private List<Token> tokens;
	private ValueType type;
	private GPR       register;
	private int       value;
	//private int       size;
	private String    label;
	private int       labelTokenId;
	
	public Value(List<Token> tokens) {
		this.tokens = tokens;
		if (tokens.size() == 1) {
			Token token = tokens.get(0);
			
			if (token.getType().equals("NAME")) {
				String registerStr = token.getValue().toUpperCase();
				
				if (isGeneralPurposeRegister(registerStr)) {
					this.type     = ValueType.GPR;
					this.register = GPR.valueOf(registerStr);
					
				} else if (isSpecialPurposeRegister(registerStr)) {
					this.type  = ValueType.SPR;
					this.value = SPRToInt(registerStr);
					
				} else {
					this.type = ValueType.CONST;
					this.label = token.getValue();
					this.labelTokenId = 0;
				}
				
			} else if (token.getType().equals("NUMBER")) {
				type  = ValueType.CONST;
				value = ((NumberToken)token).getIntegerValue();
				
			} else {
				throw new SyntaxException(token,
						"Unexpected token while trying to parse value.");
			}
			
		} else if (tokens.size() == 3) {
			// Three tokens means it has to be of the form: "[" something "]"
			if (!tokens.get(0).getValue().equals("[")
					|| !tokens.get(2).getValue().equals("]")) {
				throw new SyntaxException(tokens.get(1),
						"Unexpected surrounding tokens.");
			}
			
			Token token = tokens.get(1);
			
			if (token.getType().equals("NAME")) {
				String registerStr = token.getValue().toUpperCase();
				
				if (isGeneralPurposeRegister(registerStr)) {
					this.type     = ValueType.GPR_DEREF;
					this.register = GPR.valueOf(registerStr);
					
				} else if (isSpecialPurposeRegister(registerStr)) {
					this.type  = ValueType.SPR;
					this.value = SPRToInt(registerStr);
					
				} else {
					this.type = ValueType.CONST_DEREF;
					this.label = token.getValue();
					this.labelTokenId = 1;
				}
				
			} else if (token.getType().equals("NUMBER")) {
				type  = ValueType.CONST_DEREF;
				value = ((NumberToken)token).getIntegerValue();
				
			} else {
				throw new SyntaxException(token,
						"Unexpected token while trying to parse value.");
			}
			
		} else if (tokens.size() == 5) {
			// [A+x] and [x+A] for any GPR A and any constant x.
			if (!tokens.get(0).getValue().equals("[")
					|| !tokens.get(4).getValue().equals("]")
					|| !tokens.get(2).getValue().equals("+")) {
				throw new SyntaxException(tokens.get(1),
						"Unexpected surrounding tokens.");
			}
			
			Token t1 = tokens.get(1)
			    , t2 = tokens.get(3);
			
			Token regToken, valueToken;
			
			if (t1.getType().equals("NAME") && t2.getType().equals("NAME")) {
				// [Label + A] or [A + Label]
				boolean t1IsGPR = isGeneralPurposeRegister(t1.getValue());
				boolean t2IsGPR = isGeneralPurposeRegister(t2.getValue());
				
				if (t1IsGPR == t2IsGPR)
					throw new SyntaxException(t1, "Unexpected token.");
				
				if (t1IsGPR) {
					// Swap
					t1 = tokens.get(3);
					t2 = tokens.get(1);
				}
				
				this.type = ValueType.GPR_RELATIVE_DEREF;
				this.register = GPR.valueOf(t2.getValue().toUpperCase());
				this.label = t1.getValue();
				this.labelTokenId = 3;
				
			} else {
				if (t1.getType().equals("NAME")) {
					// [A + x]
					if (!isGeneralPurposeRegister(t1.getValue())
							|| !t2.getType().equals("NUMBER")) {
						throw new SyntaxException(t1, "Unexpected token.");
					}
					
					regToken   = t1;
					valueToken = t2;
					
				} else {
					// [x + A]
					if (!t2.getType().equals("NAME")
							|| !isGeneralPurposeRegister(t2.getValue())
							|| !t1.getType().equals("NUMBER")) {
						throw new SyntaxException(t1, "Unexpected token.");
					}
					
					regToken   = t2;
					valueToken = t1;
				}
				
				this.type     = ValueType.GPR_RELATIVE_DEREF;
				this.register = GPR.valueOf(regToken.getValue().toUpperCase());
				this.value    = ((NumberToken)valueToken).getIntegerValue();
			}
			
		} else {
			String msg = "Couldn't parse tokens into a value."; 
			
			if (tokens.size() > 0) {
				throw new SyntaxException(tokens.get(0), msg);
			} else {
				throw new SyntaxException(msg);
			}
		}
	}
	
	/**
	 * Returns the size that this value takes, in number of words.
	 */
	public int getSize() {
		switch (this.type) {
			case CONST: case CONST_DEREF: case GPR_RELATIVE_DEREF:
				return 1;
			
			default:
				return 0;
		}
	}
	
	/**
	 * Returns the number value associated with this Value (i.e. what would go
	 * in "next word").
	 */
	public short getNumber(Map<String, Integer> labelMap) {
		if (label == null) {
			return (short)value;
		} else {
			Integer address = labelMap.get(label);
			if (address != null){
				return (short)(int)address;
			} else {
				throw new SyntaxException(tokens.get(labelTokenId),
						"Label not defined.");
			}
		}
    }
	
	/**
	 * The "raw" value of the instruction, which is what would go in "a" or "b".
	 * That is, 0x1f for "next word" as opposed to the actual value that the
	 * next word should contain.
	 */
	public int getRawValue() {
		switch (this.type) {
			case GPR:                return this.register.ordinal();
			case GPR_DEREF:          return 0x08 + this.register.ordinal();
			case GPR_RELATIVE_DEREF: return 0x10 + this.register.ordinal();
			case SPR:                return this.value;
			case CONST_DEREF:        return 0x1e;
			case CONST:              return 0x1f;
			
			default:
				// Shouldn't be reachable, but javac doesn't know that.
				throw new IllegalStateException();
		}
	}

	private static int SPRToInt(String str) {
		if (str.equals("POP")) {
			return 0x18;
		} else if (str.equals("PEEK")) {
			return 0x19;
		} else if (str.equals("PUSH")) {
			return 0x1a;
		} else if (str.equals("SP")) {
			return 0x1b;
		} else if (str.equals("PC")) {
			return 0x1c;
		} else if (str.equals("O")) {
			return 0x1d;
		} else {
			throw new IllegalArgumentException(
					"No such special-purpose register: " + str);
		}
	}

	/**
	 * Returns whether the given string represents a general-purpose register.
	 */
	private static boolean isGeneralPurposeRegister(String str) {
		// Note: str can't contain comma anyway, so we shouldn't get any false
		// positives from the below.
		return ("A,B,C,X,Y,Z,I,J".contains(str));
	}
	
	/**
	 * Returns whether the given string represents a special-purpose register.
	 */
	private static boolean isSpecialPurposeRegister(String str) {
		// See above note for `isGeneralPurposeRegister`.
		
		return (",POP,PEEK,PUSH,SP,PC,O,".contains("," + str + ","));
	}
}