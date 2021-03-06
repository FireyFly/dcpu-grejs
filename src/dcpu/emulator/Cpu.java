package dcpu.emulator;

public class Cpu {
	public short[] memory = new short[0x10000 + 8 + 1 + 1 + 1 + 2];
	private final int PC = 0x10000 + 8;
	private final int SP = 0x10000 + 9;
	private final int O = 0x10000 + 10;
	private final int Lit = 0x10000 + 11;
	private long cycleCount = 0;
	private MemoryCallback memCallback;
	private boolean isRunning = false;
	
	public long getCycleCount() {
		return cycleCount;
	}
	
	public Cpu(MemoryCallback callback) {
		memCallback = callback;
	}
	
	public static interface MemoryCallback {
		public void onMemoryChange(int address, short value);
		public void onRegisterChange(int id, short value);
		public void onCyclesChange(long cycleCount);
		public void onHalt();
	}
	
	private int getValue(short code, int op) {
		switch(code) {
			case 0x00:
			case 0x01:
			case 0x02:
			case 0x03:
			case 0x04:
			case 0x05:
			case 0x06:
			case 0x07:
				return 0x10000 + code;
			case 0x08:
			case 0x09:
			case 0x0a:
			case 0x0b:
			case 0x0c:
			case 0x0d:
			case 0x0e:
			case 0x0f:
				return memory[0x10000 + code - 0x08] & 0xffff;
			case 0x10:
			case 0x11:
			case 0x12:
			case 0x13:
			case 0x14:
			case 0x15:
			case 0x16:
			case 0x17:
				cycleCount++;
				return (memory[memory[PC]++ & 0xffff] + memory[0x10000 + code - 0x10]) & 0xffff;
			case 0x18:
				return memory[SP]++ & 0xffff;
			case 0x19:
				return memory[SP] & 0xffff;
			case 0x1a:
				return --memory[SP] & 0xffff;
			case 0x1b:
				return SP;
			case 0x1c:
				return PC;
			case 0x1d:
				return O;
			case 0x1e:
				cycleCount++;
				return memory[memory[PC]++ & 0xffff] & 0xffff;
			case 0x1f:
				cycleCount++;
				memory[Lit + op] = memory[memory[PC]++ & 0xffff];
				return Lit + op;
			default:
				memory[Lit + op] = (short)(code - 0x20);
				return Lit + op;
		}
	}
	private void skipValue(short code){
		switch(code) {
			case 0x10:
			case 0x11:
			case 0x12:
			case 0x13:
			case 0x14:
			case 0x15:
			case 0x16:
			case 0x17:
			case 0x1e:
			case 0x1f:
				memory[PC]++;
		}
	}

	private void skipNext() {
		short instruction = memory[memory[PC]++ & 0xffff];
		short opcode = (short)(instruction & 0xf);
		short a = (short)((instruction >>> 4) & 0x3f);
		short b = (short)((instruction >>> 10) & 0x3f);
		
		cycleCount++;
		
		if (opcode != 0)
			skipValue(a);
		skipValue(b); //b is called a in the specification (aaaaaaoooooo0000) if opcode == 0
	}
	
	public void executeNext() {
		short instruction = memory[memory[PC]++ & 0xffff];
		short opcode = (short)(instruction & 0xf);
		short a = (short)((instruction >>> 4) & 0x3f);
		short b = (short)((instruction >>> 10) & 0x3f);
		
		if (opcode == 0) {
			if (a == 0x01){
				//JSR
				cycleCount += 2;
				short next = memory[getValue(b, 0)]; //b here is called a in the specification
				memory[--memory[SP] & 0xffff] = memory[PC];
				memory[PC] = next;
				
				memoryTouched(memory[SP] & 0xffff);
			} else {
				//throw new UnsupportedOperationException();
				//Halt
				isRunning = false;
				memCallback.onHalt();
			}
		} else {
			int dst = getValue(a, 0);
			int src = getValue(b, 1);
			switch(opcode) {
				//SET
				case 0x1: {
					cycleCount++;
					memory[dst] = memory[src];
					break;
				}
				//ADD
				case 0x2: {
					cycleCount += 2;
					int res = (memory[dst] & 0xffff) + (memory[src] & 0xffff);
					memory[dst] = (short)(res);
					memory[O] = (short)(res > 0xffff ? 0x0001 : 0x0000);
					break;
				}
				//SUB
				case 0x3: {
					cycleCount += 2;
					int res = (memory[dst] & 0xffff) - (memory[src] & 0xffff);
					memory[dst] = (short)(res);
					memory[O] = (short)(res < 0 ? 0xffff : 0x0000);
					break;
				}
				//MUL
				case 0x4: {
					cycleCount += 2;
					int res = (memory[dst] & 0xffff) * (memory[src] & 0xffff);
					memory[dst] = (short)res;
					memory[O] = (short)(res >>> 16);
					break;
				}
				//DIV
				case 0x5: {
					cycleCount += 3;
					if (memory[src] == 0){
						memory[dst] = 0;
						memory[O] = 0;
					} else {
						int res = (memory[dst] & 0xffff) / (memory[src] & 0xffff);
						short o = (short)(((long)(memory[dst] & 0xffff) << 16) / (memory[src] & 0xffff));
						memory[dst] = (short)res;
						memory[O] = o;
					}
					break;
				}
				//MOD
				case 0x6: {
					cycleCount += 3;
					if (memory[src] == 0){
						memory[dst] = 0;
					}  else {
						memory[dst] = (short)((memory[dst] & 0xffff) % (memory[src] & 0xffff));
					}
					break;
				}
				//SHL
				case 0x7: {
					cycleCount += 2;
					if ((memory[src] & 0xffff) >= 32){
						memory[dst] = 0;
						memory[O] = 0;
					} else {
						int res = (memory[dst] & 0xffff) << (memory[src] & 0xffff);
						memory[dst] = (short)res;
						memory[O] = (short)(res >>> 16);
					}
					break;
				}
				//SHR
				case 0x8: {
					cycleCount += 2;
					if ((memory[src] & 0xffff) >= 32){
						memory[dst] = 0;
						memory[O] = 0;
					} else {
						int res = (memory[dst] & 0xffff) >>> (memory[src] & 0xffff);
						short o = (short)((memory[dst] << 16) >>> (memory[src] & 0xffff));
						memory[dst] = (short)res;
						memory[O] = o;
					}
					break;
				}
				//AND
				case 0x9: {
					cycleCount++;
					memory[dst] &= memory[src];
					break;
				}
				//BOR
				case 0xa: {
					cycleCount++;
					memory[dst] |= memory[src];
					break;
				}
				//XOR
				case 0xb: {
					cycleCount++;
					memory[dst] ^= memory[src];
					break;
				}
				//IFE:
				case 0xc: {
					cycleCount += 2;
					if (memory[dst] != memory[src]){
						skipNext();
					}
					break;
				}
				//IFN:
				case 0xd: {
					cycleCount += 2;
					if (memory[dst] == memory[src]){
						skipNext();
					}
					break;
				}
				//IFG:
				case 0xe: {
					cycleCount += 2;
					if (memory[dst] <= memory[src]){
						skipNext();
					}
					break;
				}
				//IFB:
				case 0xf: {
					cycleCount += 2;
					if ((memory[dst] & memory[src]) == 0){
						skipNext();
					}
					break;
				}
			}
			memoryTouched(dst);
		}
		memoryTouched(PC);
		memoryTouched(SP);
		memCallback.onCyclesChange(cycleCount);
	}
	
	private void memoryTouched(int address) {
		if (address < 65536)
			memCallback.onMemoryChange(address, memory[address]);
		else if (address < 65536 + 8 + 1 + 1 + 1) {
			memCallback.onRegisterChange(address - 65536, memory[address]);
		}
	}
	
	public void initMem(short[] mem) {
		for(int i=0; i<mem.length; i++)
			memory[i] = mem[i];
		
		for(int i=mem.length; i<memory.length; i++)
			memory[i] = 0;
	}
	
	public static void main(String[] args){
		short tal = (short)(-10);
		System.out.println(tal & 0xffff);
		Cpu cpu = new Cpu(null);
		int[] arr = new int[]{
			0x7c01,	0x0030,
			0x7de1, 0x1000, 0x0020,
			0x7803, 0x1000,
			0xc00d,
			0x7dc1, 0x001a,
			
			0xa861,
			0x7c01, 0x2000,
			0x2161, 0x2000,
			0x8463,
			0x806d,
			0x7dc1, 0x000d,
			
			0x9031,
			0x7c10, 0x0018,
			0x7dc1, 0x001a,
			0x9037,
			0x61c1,
			
			0x7dc1, 0x001a
		};
		for(int i=0; i<arr.length; i++) cpu.memory[i] = (short)arr[i];
		cpu.start();
		for(int i=0; i<cpu.memory.length; i++){
			if (i % 32 == 0) System.out.print(i + ": ");
			System.out.print(cpu.memory[i] + " ");
			if (i % 32 == 31) System.out.println();
		}
	}
	
	public void resetRegisters() {
		cycleCount = 0;
		memCallback.onCyclesChange(0);
		for(int i=0; i<11; i++){
			memory[0x10000 + i] = 0;
			memoryTouched(0x10000 + i);
		}
	}
	
	public void stop() {
		isRunning = false;
	}

	public void start() {
		if (isRunning)
			return;
		isRunning = true;
		while(isRunning)
			executeNext();
	}
}
