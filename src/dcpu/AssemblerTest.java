package dcpu;

import dcpu.assembler.Assembler;

public class AssemblerTest {
	public static void main(String[] args) {
		String input =
				"          set A, 0x10\n" +
				"          set PC, 0\n" ;
		
		short[] res = Assembler.assemble(input, "<input>");
		
		System.out.print("Result");
		for (short instr : res) {
			System.out.print(", 0x" + Integer.toHexString(instr));
		}
		System.out.println();
	}
}
