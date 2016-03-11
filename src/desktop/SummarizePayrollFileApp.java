/*
 * SummarizePayrollFileApp
 * 
 * Written: 12/21/15 by S Swett
 * 
 * This program converts a detailed input file of ADP-bound employee payroll data to a more summarized output file.  The output file will be summarized by 
 * employee file ID and department.  The data that is summarized is hours; regular, O/T, and "Hours 4".
 *  
 */


package desktop;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JTextPane;
import java.awt.SystemColor;
import javax.swing.JCheckBox;

public class SummarizePayrollFileApp {

	private JFrame frmTestAppWindow;
	private final JButton inputChooseButton = new JButton("Choose");
	private final JPanel panel = new JPanel();
	private final JLabel lblInputFileName = new JLabel("Input file name:");
	private final JTextField inputFileName = new JTextField();
	
	private final JPanel panel_1 = new JPanel();
	private final JButton outputChooseButton = new JButton("Choose");
	private final JLabel lblOutputFileName = new JLabel("Output file name:");
	private final JTextField outputFileName = new JTextField();
	private final JPanel panel_2 = new JPanel();
	private final JButton btnRun = new JButton("Run");
	private final JPanel panel_3 = new JPanel();
	private final JTextPane txtpnThisProgramConverts = new JTextPane();
	private final JPanel panel_4 = new JPanel();
	private final JCheckBox cbRemoveHrs4 = new JCheckBox("Remove \"Hours 4\" columns (Code and Amount) in output file");
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					SummarizePayrollFileApp window = new SummarizePayrollFileApp();
					window.frmTestAppWindow.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SummarizePayrollFileApp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		inputFileName.setBounds(134, 12, 461, 20);
		inputFileName.setColumns(10);
		frmTestAppWindow = new JFrame();
		frmTestAppWindow.setTitle("Summarize Payroll File for ADP Import");
		frmTestAppWindow.setBounds(100, 100, 800, 664);
		frmTestAppWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTestAppWindow.getContentPane().setLayout(null);

		
		panel.setBounds(22, 133, 732, 50);
		
		frmTestAppWindow.getContentPane().add(panel);
		panel.setLayout(null);

		inputChooseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				OpenFileDialog dialog = new OpenFileDialog();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv", "csv");
				dialog.getFileChooser().setFileFilter(filter);
				dialog.setVisible(true);
				
				// The above dialog is modal.  This frame is blocked until the dialog is diposed.
				// At that time, execution resumes here.
				
				String selectedFileName = dialog.getSelectedFileName();
				
				if (selectedFileName.length() > 0)
				{
					inputFileName.setText(selectedFileName);
				}
			}
		});

		inputChooseButton.setBounds(629, 11, 93, 23);
		panel.add(inputChooseButton);
		lblInputFileName.setBounds(10, 11, 96, 14);
		
		panel.add(lblInputFileName);
		
		panel.add(inputFileName);
		panel_1.setLayout(null);
		panel_1.setBounds(22, 194, 732, 50);
		
		frmTestAppWindow.getContentPane().add(panel_1);

		outputChooseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				OpenFileDialog dialog = new OpenFileDialog();
				dialog.setVisible(true);
				
				// The above dialog is modal.  This frame is blocked until the dialog is diposed.
				// At that time, execution resumes here.
				
				String selectedFileName = dialog.getSelectedFileName();
				
				if (selectedFileName.length() > 0)
				{
					outputFileName.setText(selectedFileName);
				}
			}
		});

		outputChooseButton.setBounds(629, 7, 93, 23);
		
		panel_1.add(outputChooseButton);
		lblOutputFileName.setBounds(10, 11, 110, 14);
		
		panel_1.add(lblOutputFileName);
		outputFileName.setColumns(10);
		outputFileName.setBounds(134, 8, 463, 20);
		
		panel_1.add(outputFileName);
		panel_2.setBounds(22, 331, 732, 50);
		
		frmTestAppWindow.getContentPane().add(panel_2);

		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				boolean hasError = false;
						
				String trimmedInputFilename = inputFileName.getText().trim();
				
				if (trimmedInputFilename.length() == 0)
				{
					JOptionPane.showMessageDialog(frmTestAppWindow.getContentPane(), "The input file name is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
					hasError = true;
				}
				else if ( !doesFileExist(trimmedInputFilename) )
				{
					JOptionPane.showMessageDialog(frmTestAppWindow.getContentPane(), "The input file name does not exist.", "Input Error", JOptionPane.ERROR_MESSAGE);
					hasError = true;
				}

				String trimmedOutputFilename = outputFileName.getText().trim();
				
				if (trimmedOutputFilename.length() == 0)
				{
					JOptionPane.showMessageDialog(frmTestAppWindow.getContentPane(), "The output file name is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
					hasError = true;
				}
				else if ( !doesDirectoryForFileExist(trimmedOutputFilename) )
				{
					JOptionPane.showMessageDialog(frmTestAppWindow.getContentPane(), "The output file directory does not exist.", "Input Error", JOptionPane.ERROR_MESSAGE);
					hasError = true;
				}
				
				if (trimmedInputFilename.equals(trimmedOutputFilename))
				{
					JOptionPane.showMessageDialog(frmTestAppWindow.getContentPane(), "The input and output file names cannot be the same.", "Input Error", JOptionPane.ERROR_MESSAGE);
					hasError = true;
				}

				if (!hasError)
				{
					// Give warning if output file already exists:
					
					if (doesFileExist(trimmedOutputFilename))
					{
						int option = JOptionPane.showConfirmDialog(frmTestAppWindow.getContentPane(), "The output file already exists.  OK to continue?", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
						
						if (option == JOptionPane.OK_OPTION)
						{
							doRun();
						}
					}
					else
					{
						doRun();
					}
					
				}
			}
		});
		
		panel_2.add(btnRun);
		panel_3.setBounds(22, 31, 732, 89);
		
		frmTestAppWindow.getContentPane().add(panel_3);
		panel_3.setLayout(null);
		txtpnThisProgramConverts.setEditable(false);
		txtpnThisProgramConverts.setBackground(SystemColor.menu);
		txtpnThisProgramConverts.setText("This program converts a detailed input file of ADP-bound employee payroll data to a more summarized output file.  The output file will be summarized by employee file ID and department.");
		txtpnThisProgramConverts.setBounds(12, 13, 708, 63);
		
		panel_3.add(txtpnThisProgramConverts);
		panel_4.setBounds(22, 257, 732, 50);
		
		frmTestAppWindow.getContentPane().add(panel_4);
		panel_4.setLayout(null);
		cbRemoveHrs4.setSelected(true);
		cbRemoveHrs4.setBounds(10, 9, 429, 25);
		
		panel_4.add(cbRemoveHrs4);
	}
	
	
	private void doRun()
	{

        Writer writer = null;
		
        FileInputStream fileInputStream = null;
        DataInputStream dataInputStream = null;
        BufferedReader bufferedReader = null;

        boolean foundGoodHeader = false;

        int inputCount = 0;
        int outputCount = 0;
        
		try
        {
            // Create new output file:

            FileOutputStream outStream = new FileOutputStream(outputFileName.getText().trim());
            writer = new OutputStreamWriter(outStream);

            // Read records in input file:

            fileInputStream = new FileInputStream(inputFileName.getText().trim());
            dataInputStream = new DataInputStream(fileInputStream);
            bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));

            String inputRecord;
            int groupCount = 0;
            String prevFileNumber = "";
            String prevDept = "";
            boolean firstRecord = true;
            
            int fileNumberCol = -1;
            int regHrsCol = -1;
            int otHrsCol = -1;
            int hrs4CodeCol = -1;
            int hrs4AmtCol = -1;
            int deptCol = -1;
            
            String[] inputFields;
            String[] outputFields = null;
            

            while ((inputRecord = bufferedReader.readLine()) != null)
            {
            	
            	// Process the header record:
            	
            	if (firstRecord)
            	{
            		String[] headers = inputRecord.split(",");
            		
            		for (int h = 0; h < headers.length; h++)
            		{
            			String header = headers[h];
            			
            			if (header.equals("File #"))
            			{
            				fileNumberCol = h;
            			}
            			
            			else if (header.equals("Reg Hours"))
            			{
            				regHrsCol = h;
            			}
            			
            			else if (header.equals("O/T Hours"))
            			{
            				otHrsCol = h;
            			}
            			
            			else if (header.equals("Hours 4 Code"))
            			{
            				hrs4CodeCol = h;
            			}
            			
            			else if (header.equals("Hours 4 Amount"))
            			{
            				hrs4AmtCol = h;
            			}
            			
            			else if (header.equals("Temp Dept"))
            			{
            				deptCol = h;
            			}
            		}

            		firstRecord = false;
            		
                	foundGoodHeader = fileNumberCol >= 0 && regHrsCol >= 0 && otHrsCol >= 0 && hrs4CodeCol >= 0 && hrs4AmtCol >= 0 && deptCol >= 0;
                	
                	if (foundGoodHeader)
                	{
                		if (cbRemoveHrs4.isSelected())
                		{
                			String outputRecord = getOutputArrayMinusHrs4ColumnsAsString(headers, hrs4CodeCol, hrs4AmtCol);
                            writer.write(outputRecord + "\r\n");
                		}
                		else
                		{
                            writer.write(inputRecord + "\r\n");
                		}
                        
                		continue;
                	}
                	else
                	{
                		break;
                	}

            	}

            	// Process detail record:
            	
            	inputCount++;

        		inputFields = inputRecord.split(",");
        		String fileNumber = inputFields[fileNumberCol];
        		String dept = inputFields[deptCol];
        		
        		if (!fileNumber.equals(prevFileNumber) || !dept.equals(prevDept))
        		{
        			groupCount = 0;

        			// Write new summary record to the output file (whenever file number or dept changes):
        			
            		if (prevFileNumber.length() > 0 && prevDept.length() > 0)
            		{
            			String outputRecord;
            			
            			if (cbRemoveHrs4.isSelected())
            			{
                			outputRecord = getOutputArrayMinusHrs4ColumnsAsString(outputFields, hrs4CodeCol, hrs4AmtCol);
            			}
            			else
            			{
                    		outputRecord = String.join(",", outputFields);
            			}
            			
                        writer.write(outputRecord + "\r\n");
                        outputCount++;
            		}
        		}
        		
        		groupCount++;
        		
        		// Handle first record in group:
        		
        		if (groupCount == 1)
        		{
        			outputFields = new String[inputFields.length];
        			System.arraycopy(inputFields, 0, outputFields, 0, inputFields.length);
        		}
        		
        		// Handle other records in group:
        		
        		else
        		{
        			// Aggregate the "hours" columns:
        			
        			for (int d = 0; d < inputFields.length; d++)
            		{
            			if (d == regHrsCol || d == otHrsCol || d == hrs4AmtCol)
            			{
            				String inputFieldValueStr = inputFields[d];
            				
            				if (inputFieldValueStr.trim().length() > 0)
            				{
            					BigDecimal inputFieldValue = new BigDecimal(inputFieldValueStr);
            					
            					// Round to 2 decimal places:
            					inputFieldValue = inputFieldValue.setScale(2, RoundingMode.HALF_DOWN);
            					
            					String outputFieldValueStr = outputFields[d];
            					
            					BigDecimal outputFieldValue = new BigDecimal(0);
            					
                				if (outputFieldValueStr.trim().length() > 0)
                				{
                					outputFieldValue = new BigDecimal(outputFieldValueStr);
                					
                					// Round to 2 decimal places:
                					outputFieldValue = outputFieldValue.setScale(2, RoundingMode.HALF_DOWN);
                				}
                				
                				outputFieldValue = outputFieldValue.add(inputFieldValue);
            					
            					// Round to 2 decimal places:
            					outputFieldValue = outputFieldValue.setScale(2, RoundingMode.HALF_DOWN);
                				
                				String newOutputFieldValueStr = outputFieldValue.toString(); 
                				
            					outputFields[d] = newOutputFieldValueStr;
            					
            				}
            				
            			}
            		}
        		
        		}

                prevFileNumber = fileNumber;
                prevDept = dept;
            }
            

			// Write new summary record to the output file (for the last group of records in the input file):
			
    		if (groupCount > 0)
    		{
    			String outputRecord;
    			
    			if (cbRemoveHrs4.isSelected())
    			{
        			outputRecord = getOutputArrayMinusHrs4ColumnsAsString(outputFields, hrs4CodeCol, hrs4AmtCol);
    			}
    			else
    			{
            		outputRecord = String.join(",", outputFields);
    			}
    			
                writer.write(outputRecord + "\r\n");
                outputCount++;
    		}

        }

        catch (IOException e)
        {
            e.printStackTrace();
        }
		
		finally
		{
			try
			{
				if (bufferedReader != null)
				{
		            bufferedReader.close();
				}
				
				if (dataInputStream != null)
				{
		            dataInputStream.close();
				}
				
				if (fileInputStream != null)
				{
		            fileInputStream.close();
				}
				
				if (writer != null)
				{
		            writer.close();
					
				}
			}
			
			catch (IOException e2)
			{
	            e2.printStackTrace();
			}
			
		}
		
		if (foundGoodHeader)
		{
			String msg = "The process has completed.  " + inputCount + " input records.  " + outputCount + " output records.";
			
			JOptionPane.showMessageDialog(frmTestAppWindow.getContentPane(),	msg, "Finished",	JOptionPane.INFORMATION_MESSAGE);

			frmTestAppWindow.dispose();
		}
		else
		{
			JOptionPane.showMessageDialog(frmTestAppWindow.getContentPane(), 
					"The input file doesn't have the expected headers (File #, Reg Hours, O/T Hours, Hours 4 Amount, Temp Dept).", "Input File Error", 
					JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	
	private String getOutputArrayMinusHrs4ColumnsAsString(String[] outputArray, int hrs4CodeCol, int hrs4AmtCol)
	{
		StringBuilder sb = new StringBuilder("");
		
		for (int h = 0; h < outputArray.length; h++)
		{
			if (h != hrs4CodeCol && h != hrs4AmtCol)
			{
				sb.append(outputArray[h]);
				
				if (h < outputArray.length - 1)
				{
					sb.append(",");
				}
			}
		}
		
		return sb.toString();
	}
	
	
	private boolean doesFileExist(String filePath)
	{
		File file = new File(filePath);
		return file.isFile();
	}
	
	
	private boolean doesDirectoryForFileExist(String filePath)
	{
		int lastSlash = filePath.lastIndexOf("\\");
		
		if (lastSlash == -1 )
		{
			return false;
		}
		
		String directoryPath = filePath.substring(0,  lastSlash);
		
		File directory = new File(directoryPath);
		return directory.isDirectory();
	}
}