package dcpu.frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import dcpu.emulator.Cpu;

public class DCPUToolFrame extends JFrame {
	private JPanel textPane;
	private JPanel memoryPane;
	
	private Cpu emulator;
	
	public DCPUToolFrame() {
		this.setLayout(new BorderLayout());
		
		this.emulator = new Cpu();
		
		this.setupTextPane();
		this.setupMemoryPane();
	}
	
	private void setupTextPane() {
		JTextArea textArea = new JTextArea(25, 40);
		
		this.textPane = new JPanel();
		//this.textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
		this.textPane.add(textArea);
		
		this.add(this.textPane, BorderLayout.WEST);
	}
	
	private void setupMemoryPane() {
		this.memoryPane = new JPanel();
		this.memoryPane.add(new RAMViewer(this.emulator.memory));
		
		this.add(this.memoryPane, BorderLayout.CENTER);
	}
	
	// Inner class for the RAM showing widget.
	static class RAMViewer extends JComponent {
		private short[] memory;
		
		public RAMViewer(short[] memory) {
			this.memory = memory;
	    }
		
		@Override
		protected void paintComponent(Graphics g) {
		    int widthCount = 16;
		    
		    System.out.println("Redrawing memory pane...");
	    	
	    	g.setColor(Color.CYAN);

	    	for (int i=0; i<this.memory.length; i++) {
		    	int x = i % widthCount
		    	  , y = i / widthCount;
		    	
		    	g.fillRect(x*32, y*16, 30, 14);
		    }
		}
	}
}
