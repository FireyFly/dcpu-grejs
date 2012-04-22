package dcpu.frontend;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class DCPUTools {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				DCPUToolFrame frame = new DCPUToolFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
				
			}
		});
	}
}
