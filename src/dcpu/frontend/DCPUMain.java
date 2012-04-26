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
import java.io.File;
import java.io.FileReader;
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
	private JTextArea editor;
	private Cpu cpu;
	
	private Thread cpuThread;
	
	private JTextArea errorArea;
	private SpringLayout springLayout;
	private JLabel cycleCountLabel;
	
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
		
		final JButton btnAssemble = new JButton("Assemble");
		final JButton btnRun      = new JButton("Run");
		final JButton btnStop     = new JButton("Stop");
		final JButton btnStep     = new JButton("Step");
		
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
				btnAssemble.setEnabled(true);
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
		
		JMenuItem mntmOpen = new JMenuItem("Open...");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				chooser.showOpenDialog(DCPUMain.this);
				File file = chooser.getSelectedFile();
				
				if (file == null) {
					// User cancelled; didn't select a file.
					return;
				}
				
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
		mnFile.add(mntmOpen);
		
		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);
		
		JMenuItem mntmSaveAs = new JMenuItem("Save as...");
		mnFile.add(mntmSaveAs);
		
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("Open binary...");
		mnFile.add(mntmNewMenuItem);
		
		JMenuItem mntmSaveBinary = new JMenuItem("Save binary...");
		mnFile.add(mntmSaveBinary);
		
		JSeparator separator_1 = new JSeparator();
		mnFile.add(separator_1);
		
		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Exit");
		mnFile.add(mntmNewMenuItem_1);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About...");
		mnHelp.add(mntmAbout);
	}
	
//	private static final ActionListener 
	
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
