package dcpu.assembler;

public enum Opcode {
	// extended opcodes
	JSR(0x1, true),
	
	// basic opcodes
	          SET(0x1),
	ADD(0x2), SUB(0x3),
	MUL(0x4), DIV(0x5),
	MOD(0x6), SHL(0x7),
	SHR(0x8), AND(0x9),
	BOR(0xa), XOR(0xb),
	IFE(0xc), IFN(0xd),
	IFG(0xe), IFB(0xf);
	
	private int code;
	private boolean isExtended;
	
	private Opcode(int code) {
		this(code, false);
	}
	
	private Opcode(int code, boolean isExtended) {
		this.code = code;
		this.isExtended = isExtended;
    }
	
	/**
	 * Returns the numeric code that represents this operation.  The pair of
	 * code and whether it's an extended opcode are together unique.
	 */
	public int getCode() {
        return code;
    }
	
	/**
	 * Returns whether the opcode is an extended opcode or not.
	 */
	public boolean isExtended() {
        return isExtended;
    }
}
