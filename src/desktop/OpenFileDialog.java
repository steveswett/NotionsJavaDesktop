/*
 * OpenFileDialog
 * 
 * Written: 12/21/15 by S Swett
 * 
 * Open a JFileChooser in a JDialog window 
 *  
 */



package desktop;

import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class OpenFileDialog extends JDialog {
	private final JFileChooser fileChooser = new JFileChooser();
	private String selectedFileName = "";
	
	
	protected String getSelectedFileName()
	{
		return selectedFileName;
	}
	
	
	protected JFileChooser getFileChooser()
	{
		return fileChooser;
	}
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					OpenFileDialog dialog = new OpenFileDialog();
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the dialog.
	 */
	public OpenFileDialog() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Choose File");
		setBounds(100, 100, 900, 600);
		fileChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				final String cmd = e.getActionCommand();
				
				if (cmd.equals(JFileChooser.APPROVE_SELECTION))
				{
					selectedFileName = fileChooser.getSelectedFile().getAbsolutePath();
					dispose();
				}
				
				else if (cmd.equals(JFileChooser.CANCEL_SELECTION))
				{
					selectedFileName = "";
					dispose();
				}
			}
		});
		
		getContentPane().add(fileChooser, BorderLayout.CENTER);

	}

}