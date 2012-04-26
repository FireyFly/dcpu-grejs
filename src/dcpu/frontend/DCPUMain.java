package dcpu.frontend;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JSeparator;
import javax.swing.JEditorPane;
import javax.swing.JButton;

import dcpu.assembler.Assembler;
import dcpu.assembler.parser.SyntaxException;
import dcpu.emulator.Cpu;
import dcpu.frontend.RegisterViewer;
import dcpu.frontend.RamViewer;

public class DCPUMain extends JFrame {
	private RegisterViewer regViewer;
	private RamViewer ramViewer;
	private Cpu cpu;
	
	private Thread cpuThread;
	
	private JTextArea errorArea;
	private SpringLayout springLayout;
	private JLabel cycleCountLabel;
	
	private JTextArea editor;
	private JButton btnAssemble;
	private JButton btnRun;
	private JButton btnStop;
	private JButton btnStep;
	
	private String lastFileName;
	
	private short[] lastBinary;
	
	public DCPUMain() {
		cpu = new Cpu(new Cpu.MemoryCallback() {
			@Override
			public void onRegisterChange(int id, short value) {
				regViewer.updateRegister(id, value);
			}
			
			@Override
			public void onMemoryChange(int address, short value) {
				ramViewer.updateMemoryAt(address, value);
			}
			
			@Override
			public void onCyclesChange(long cycleCount) {
				cycleCountLabel.setText("Cycles: " + cycleCount);
			}
			
			@Override
			public void onHalt() {
				btnAssemble.setEnabled(true);
				btnRun.setEnabled(true);
				btnStop.setEnabled(false);
				btnStep.setEnabled(true);
			}
		});
		
		springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		
		JScrollPane editorScrollPane = new JScrollPane();
		getContentPane().add(editorScrollPane);
		
		editor = new JTextArea();
		editor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		editorScrollPane.setViewportView(editor);

		regViewer = new RegisterViewer();
		getContentPane().add(regViewer);
		
		ramViewer = new RamViewer();
		getContentPane().add(ramViewer);
		
		// Prepare buttons
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		
		btnAssemble = new JButton("Assemble");
		btnRun      = new JButton("Run");
		btnStop     = new JButton("Stop");
		btnStep     = new JButton("Step");
		
		btnStop.setEnabled(false);

		buttonPane.add(btnAssemble);
		buttonPane.add(btnRun);
		buttonPane.add(btnStop);
		buttonPane.add(btnStep);
		
		getContentPane().add(buttonPane);
		
		// Use runnables as "first-class functions" because.. why not?
		final Runnable startCpu = new Runnable() {
			@Override
			public void run() {
				btnAssemble.setEnabled(false);
				btnRun.setEnabled(false);
				btnStop.setEnabled(true);
				btnStep.setEnabled(false);
				cpu.start();
			}
		};
		final Runnable stopCpu = new Runnable() {
			@Override
			public void run() {
				btnAssemble.setEnabled(true);
				btnRun.setEnabled(true);
				btnStop.setEnabled(false);
				btnStep.setEnabled(true);
				cpu.stop();
			}
		};
		
		btnAssemble.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				String source = editor.getText();
				
				stopCpu.run();
				
				// TODO: Use correct filename.
				try {
					short[] binary = Assembler.assemble(source, "<input>");
					cpu.resetRegisters();
					cpu.initMem(binary);
					ramViewer.replaceMemory(Arrays.copyOf(binary, 65536));
					
					errorArea.setText(""); // Clear the error label content.
					
					lastBinary = binary;
					
				} catch (SyntaxException ex) {
					errorArea.setText(ex.getMessage());
					System.err.println(ex);
				}
			}
		});
		
		btnRun.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cpuThread = new Thread(startCpu);
				cpuThread.start();
			}
		});
		
		btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				stopCpu.run();
				cpuThread.interrupt();
			}
		});
		
		btnStep.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cpu.executeNext();
			}
		});
		
		// Prepare error pane
		this.errorArea = new JTextArea();
		this.errorArea.setEditable(false);
		this.errorArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		getContentPane().add(this.errorArea);
		
		// Prepare cycle count thing
		this.cycleCountLabel = new JLabel("Cycles: 0");
		getContentPane().add(this.cycleCountLabel);
		
		// Setup constraints
		springLayout.putConstraint(SpringLayout.NORTH, buttonPane, 10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST,  buttonPane, 10, SpringLayout.WEST,  getContentPane());
		
		springLayout.putConstraint(SpringLayout.NORTH, cycleCountLabel, 14, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST,  cycleCountLabel, -10, SpringLayout.WEST, regViewer);
		
		springLayout.putConstraint(SpringLayout.NORTH, editorScrollPane,  10, SpringLayout.SOUTH,       buttonPane);
		springLayout.putConstraint(SpringLayout.WEST,  editorScrollPane,  10, SpringLayout.WEST,  getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, editorScrollPane,  -5, SpringLayout.NORTH,        errorArea);
		springLayout.putConstraint(SpringLayout.EAST,  editorScrollPane, -10, SpringLayout.WEST,         regViewer);

		springLayout.putConstraint(SpringLayout.NORTH, errorArea, -70, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, errorArea,  10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST,  errorArea,  10, SpringLayout.WEST,  getContentPane());
		springLayout.putConstraint(SpringLayout.EAST,  errorArea, -10, SpringLayout.WEST,         regViewer);
		
		springLayout.putConstraint(SpringLayout.NORTH, regViewer,   10, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.WEST,  regViewer, -362, SpringLayout.EAST,  getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, regViewer,  100, SpringLayout.NORTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST,  regViewer,  -10, SpringLayout.EAST,  getContentPane());

		springLayout.putConstraint(SpringLayout.NORTH, ramViewer,   10, SpringLayout.SOUTH,        regViewer);
		springLayout.putConstraint(SpringLayout.WEST,  ramViewer, -362, SpringLayout.EAST,  getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, ramViewer,  -10, SpringLayout.SOUTH, getContentPane());
		springLayout.putConstraint(SpringLayout.EAST,  ramViewer,  -10, SpringLayout.EAST,  getContentPane());
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		final JFileChooser chooser = new JFileChooser();
		
		final JMenuItem mntmOpen = new JMenuItem("Open...");
		mnFile.add(mntmOpen);
		
		final JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		
		final JMenuItem mntmSaveAs = new JMenuItem("Save as...");
		mnFile.add(mntmSaveAs);
		
		// Behaviour of menu choices.
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooser.showOpenDialog(DCPUMain.this);
				File file = chooser.getSelectedFile();
				
				if (file == null) {
					// User cancelled; didn't select a file.
					return;
				}
				lastFileName = file.getAbsolutePath();
				
				try {
					BufferedReader reader = new BufferedReader(new FileReader(file));
					
					StringBuilder builder = new StringBuilder();
					while (reader.ready()) {
						builder.append(reader.readLine());
						builder.append("\n");
					}
					
					editor.setText(builder.toString());
					
				} catch (IOException ex) {
					String msg = "Couldn't open file: " + file.toString() + "\n"
					           + "Reason: " + ex.getMessage();
					JOptionPane.showMessageDialog(DCPUMain.this, msg, "Error!",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		/**
		 * Writes the data in the editor pane to the file denoted by `lastFileName`.
		 * NOTE: Assumes `lastFileName` to be set.
		 */
		final Runnable saveFile = new Runnable() {
			@Override
			public void run() {
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(lastFileName));
					String text = editor.getText();
					
					if (text == null) {
						throw new IOException("Internal error.");
					}
					
					writer.write(text);
					writer.close();
					
				} catch (IOException ex) {
					String msg = "Couldn't write to file: " + lastFileName + "\n"
					           + "Reason: " + ex.getMessage();
					JOptionPane.showMessageDialog(DCPUMain.this, msg, "Error!",
							JOptionPane.ERROR_MESSAGE);
				}
				
			}
		};
		mntmSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (lastFileName != null) {
					saveFile.run();
				} else {
					// FIXME: Ugly
					mntmSaveAs.getActionListeners()[0].actionPerformed(null);
				}
			}
		});
		mntmSaveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooser.showSaveDialog(DCPUMain.this);
				File file = chooser.getSelectedFile();
				
				if (file == null) {
					// User cancelled; didn't select a file.
					return;
				}
				
				lastFileName = file.getAbsolutePath();
				saveFile.run();
			}
		});
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		JMenuItem mntmOpenBinary = new JMenuItem("Open binary...");
		mnFile.add(mntmOpenBinary);
		
		JMenuItem mntmSaveBinary = new JMenuItem("Save binary...");
		mnFile.add(mntmSaveBinary);
		
		mntmOpenBinary.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooser.showOpenDialog(DCPUMain.this);
				File file = chooser.getSelectedFile();
				
				if (file == null) {
					// User cancelled; didn't select a file.
					return;
				}
				
				try {
					FileInputStream reader = new FileInputStream(file);
					
					byte[] input = new byte[65536*2];
					int pos = 0;
					for(;;) {
						int numRead = reader.read(input, pos, 4096);
						if (numRead == -1)
							break;
						pos += numRead;
					}
					
					if ((pos & 1) == 1) {
						String msg = "Couldn't open file: " + file.toString() + "\n"
						           + "Reason: Number of bytes in binary file not a multiple of 2";
						JOptionPane.showMessageDialog(DCPUMain.this, msg, "Error!",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					short[] binary = new short[pos/2];
					for(int i=0; i<pos; i+=2) {
						binary[i/2] = (short)((input[i] & 0xff) | (input[i+1] << 8));
					}
					
					cpu.resetRegisters();
					cpu.initMem(binary);
					ramViewer.replaceMemory(Arrays.copyOf(binary, 65536));
					
					reader.close();
					
				} catch (IOException ex) {
					String msg = "Couldn't open file: " + file.toString() + "\n"
					           + "Reason: " + ex.getMessage();
					JOptionPane.showMessageDialog(DCPUMain.this, msg, "Error!",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		mntmSaveBinary.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (lastBinary == null) {
					String msg = "You must assembly something first before you can save the binary blob.";
					JOptionPane.showMessageDialog(DCPUMain.this, msg, "Error!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				chooser.showOpenDialog(DCPUMain.this);
				File file = chooser.getSelectedFile();
				
				if (file == null) {
					// User cancelled; didn't select a file.
					return;
				}
				
				try {
					byte[] buffer = new byte[4096];
					FileOutputStream writer = new FileOutputStream(file);
					int left = lastBinary.length;
					
					while(left > 0) {
						int num = left > 2048 ? 2048 : left;
						for(int i=0; i<num; i++) {
							buffer[i*2] = (byte)(lastBinary[i] & 0xff);
							buffer[i*2+1] = (byte)(lastBinary[i] >> 8);
						}
						writer.write(buffer, 0, num*2);
						left -= num;
					}
					
					writer.close();
				} catch (IOException ex) {
					String msg = "Couldn't write to file: " + file.toString() + "\n"
					           + "Reason: " + ex.getMessage();
					JOptionPane.showMessageDialog(DCPUMain.this, msg, "Error!",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		JSeparator separator_1 = new JSeparator();
		mnFile.add(separator_1);
		
		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Exit");
		mnFile.add(mntmNewMenuItem_1);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About...");
		mnHelp.add(mntmAbout);
		mntmAbout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(DCPUMain.this,
						"This software was created by Emil Lenngren & Jonas Höglund. :-)",
						"About...", JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
	
	private void setupKeyListener() {
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent ev) {
				int key = ev.getKeyCode();
				
				if (key == KeyEvent.VK_O && ev.getModifiers() == KeyEvent.CTRL_DOWN_MASK) {
					
				}
			}
		});
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new DCPUMain();
				frame.setSize(800, 600);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
}
