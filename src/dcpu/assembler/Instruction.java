package dcpu.assembler;


/**
 * Represents a single DCPU assembly instruction.
 *
 */
public class Instruction {
	private Opcode opcode;
	private Value a;
	private Value b;
	
	/**
	 * Creates a new basic instruction, with the specified basic opcode
	 * and the specified parameters.
	 */
	public Instruction(Opcode opcode, Value a, Value b) {
		if (opcode.isExtended()) {
			throw new IllegalArgumentException(
					"Trying to create extended instruction with two arguments.");
		}
		
		this.opcode = opcode;
		this.a      = a;
		this.b      = b;
	}
	
	/**
	 * Creates a new extended instruction, with the specified extended opcode
	 * and the specified parameter.
	 */
	public Instruction(Opcode opcode, Value b) {
		if (!opcode.isExtended()) {
			throw new IllegalArgumentException(
					"Trying to create basic instruction with one argument.");
		}
		
		this.opcode = opcode;
		this.b      = b;
	}
	
	/**
	 * Returns the size (in words) that this instruction occupies.
	 */
	public int getSize() {
		if (this.opcode.isExtended()) {
			return 1 + this.b.getSize();
		} else {
			return 1 + this.a.getSize() + this.b.getSize();
		}
		
	}
	
	/**
	 * Assembles this instruction into an array of 1-3 shorts.
	 */
	public short[] assemble() {
		int op = 0;
		
		if (this.opcode.isExtended()) {
			op |= (this.opcode.getCode() & 0x3f) <<  4;
			op |= (this.b.getRawValue()  & 0x3f) << 10;
			
			if (this.b.getSize() > 0) {
				return new short[] { (short)op, this.b.getNumber() };
			} else {
				return new short[] { (short)op };
			}
			
		} else {
			op |= (this.opcode.getCode() & 0x0f);
			op |= (this.a.getRawValue()  & 0x3f) <<  4;
			op |= (this.b.getRawValue()  & 0x3f) << 10;
			
			short[] res = new short[this.getSize()];
			res[0] = (short)op;
			
			int i = 1;
			if (this.a.getSize() > 0) {
				res[i++] = this.a.getNumber(); 
			}
			if (this.b.getSize() > 0) {
				res[i++] = this.b.getNumber();
			}
			
			return res;
		}
	}
}
