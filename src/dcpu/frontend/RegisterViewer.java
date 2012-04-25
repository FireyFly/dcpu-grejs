package dcpu.frontend;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RegisterViewer extends JComponent {
	private JTextField txtA;
	private JTextField txtB;
	private JTextField txtC;
	private JTextField txtX;
	private JTextField txtY;
	private JTextField txtZ;
	private JTextField txtI;
	private JTextField txtJ;
	private JTextField txtPC;
	private JTextField txtSP;
	private JTextField txtO;
	
	private JTextField[] regLabels;
	private short[] regs;
	
	public RegisterViewer() {
		
		JLabel lblA = new JLabel("A:");
		lblA.setBounds(12, 12, 70, 15);
		add(lblA);
		
		JLabel lblB = new JLabel("B:");
		lblB.setBounds(12, 39, 70, 15);
		add(lblB);
		
		JLabel lblC = new JLabel("C:");
		lblC.setBounds(12, 66, 70, 15);
		add(lblC);
		
		JLabel lblX = new JLabel("X:");
		lblX.setBounds(94, 12, 70, 15);
		add(lblX);
		
		JLabel lblY = new JLabel("Y:");
		lblY.setBounds(94, 39, 70, 15);
		add(lblY);
		
		JLabel lblZ = new JLabel("Z:");
		lblZ.setBounds(94, 66, 70, 15);
		add(lblZ);
		
		JLabel lblI = new JLabel("I:");
		lblI.setBounds(176, 12, 70, 15);
		add(lblI);
		
		JLabel lblJ = new JLabel("J:");
		lblJ.setBounds(176, 39, 70, 15);
		add(lblJ);
		
		JLabel lblPc = new JLabel("PC:");
		lblPc.setBounds(258, 12, 70, 15);
		add(lblPc);
		
		JLabel lblSp = new JLabel("SP:");
		lblSp.setBounds(258, 39, 70, 15);
		add(lblSp);
		
		JLabel lblO = new JLabel("O:");
		lblO.setBounds(258, 66, 70, 15);
		add(lblO);
		
		txtA = new JTextField();
		txtA.setEditable(false);
		txtA.setBounds(35, 10, 47, 19);
		add(txtA);
		txtA.setColumns(10);
		
		txtB = new JTextField();
		txtB.setEditable(false);
		txtB.setColumns(10);
		txtB.setBounds(35, 39, 47, 19);
		add(txtB);
		
		txtC = new JTextField();
		txtC.setEditable(false);
		txtC.setColumns(10);
		txtC.setBounds(35, 66, 47, 19);
		add(txtC);
		
		txtX = new JTextField();
		txtX.setEditable(false);
		txtX.setColumns(10);
		txtX.setBounds(117, 10, 47, 19);
		add(txtX);
		
		txtY = new JTextField();
		txtY.setEditable(false);
		txtY.setColumns(10);
		txtY.setBounds(117, 37, 47, 19);
		add(txtY);
		
		txtZ = new JTextField();
		txtZ.setEditable(false);
		txtZ.setColumns(10);
		txtZ.setBounds(117, 64, 47, 19);
		add(txtZ);
		
		txtI = new JTextField();
		txtI.setEditable(false);
		txtI.setColumns(10);
		txtI.setBounds(199, 10, 47, 19);
		add(txtI);
		
		txtJ = new JTextField();
		txtJ.setEditable(false);
		txtJ.setColumns(10);
		txtJ.setBounds(199, 37, 47, 19);
		add(txtJ);
		
		txtPC = new JTextField();
		txtPC.setEditable(false);
		txtPC.setColumns(10);
		txtPC.setBounds(291, 10, 47, 19);
		add(txtPC);
		
		txtSP = new JTextField();
		txtSP.setEditable(false);
		txtSP.setColumns(10);
		txtSP.setBounds(291, 37, 47, 19);
		add(txtSP);
		
		txtO = new JTextField();
		txtO.setEditable(false);
		txtO.setColumns(10);
		txtO.setBounds(291, 64, 47, 19);
		add(txtO);
		
		regLabels = new JTextField[] {txtA, txtB, txtC, txtX, txtY, txtZ, txtI, txtJ, txtPC, txtSP, txtO};
		regs = new short[11];
		
		for(int i=0; i<11; i++)
			regLabels[i].setText("0000");
	}
	
	public void updateRegister(int id, short value) {
		if (regs[id] != value) {
			regs[id] = value;
			regLabels[id].setText(String.format("%04x", value & 0xffff));
		}
	}
}
