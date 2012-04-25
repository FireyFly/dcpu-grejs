package dcpu.frontend;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BoxLayout;

public class RamViewer extends JPanel {
	private short[] memory;
	private JLabel[] labels;
	
	public RamViewer() {
		JScrollPane scrollPane = new JScrollPane();
		this.setLayout(new BorderLayout());
	//	scrollPane.setBounds(12, 12, 370, 220);
		add(scrollPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		scrollPane.setViewportView(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		memory = new short[65536];
		labels = new JLabel[65536/8];
		
		Font font = new Font(Font.MONOSPACED, Font.PLAIN, 12);
		
		for(int i=0; i<65536; i+=8) {
			JLabel lbl = labels[i/8] = new JLabel();
			lbl.setFont(font);
			StringBuilder sb = new StringBuilder();
			sb.append("0x" + String.format("%04x", i) + ":");
			for(int j=0; j<8; j++){
				sb.append(" " + String.format("%04x", memory[i+j] & 0xffff));
			}
			lbl.setText(sb.toString());
			panel.add(lbl);
		}
	}
	
	public void replaceMemory(short[] mem) {
		if (mem.length != 65536) {
			throw new IllegalArgumentException("Invalid memory length.");
		}
		
		this.memory = mem;
		for (int i=0; i<65536; i+=8) {
			redrawRow(i);
		}
	}

	public void updateMemoryAt(int address, short value) {
		if (memory[address] != value) {
			memory[address] = value;
			redrawRow(address);
		}
	}
	
	public void redrawRow(int address) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("0x%04x:", address & ~7));
		
		for(int j=0; j<8; j++) {
			int roundedAddress = address & ~7;
			sb.append(String.format(" %04x", memory[roundedAddress + j] & 0xffff));
		}
		
		labels[address/8].setText(sb.toString());
	}
}
