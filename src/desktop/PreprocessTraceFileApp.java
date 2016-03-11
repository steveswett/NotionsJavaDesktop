/*
 * PreprocessTraceFileApp
 * 
 * Written: 1/16/16 by S Swett
 * 
 * This program preprocesses a file generated by Basis' settrace verb.  It reads the file generated from 
 * the settrace and writes a revised file.  The revised file's reported run time figures are different.
 * "Per statement trace overhead" is subtracted from each statement's reported execution time.
 *
 *  Progress Bar Info: https://docs.oracle.com/javase/tutorial/uiswing/components/progress.html
 *  
 *  TODO: Once "run" is clicked, it should be more modal.  Maybe the progress bar should be displayed
 *  in a modal dialog.
 *  
 */


package desktop;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
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
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JTextPane;

import java.awt.SystemColor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.JProgressBar;
import javax.swing.SpinnerNumberModel;
import java.awt.Font;

// For progress bar, I added implements PropertyChangeListener
public class PreprocessTraceFileApp implements PropertyChangeListener {

	private JFrame appWindow;
	private final JButton inputChooseButton = new JButton("Choose");
	private final JPanel inputPanel = new JPanel();
	private final JLabel lblInputFileName = new JLabel("Input file name:");
	private final JTextField inputFileName = new JTextField();
	
	private final JPanel outputPanel = new JPanel();
	private final JButton outputChooseButton = new JButton("Choose");
	private final JLabel lblOutputFileName = new JLabel("Output file name:");
	private final JTextField outputFileName = new JTextField();
	private final JPanel runPanel = new JPanel();
	private final JButton runButton = new JButton("Run");
	private final JPanel panel_3 = new JPanel();
	private final JTextPane txtpnThisProgramConverts = new JTextPane();
	private final JPanel optionsPanel = new JPanel();
	private final JSpinner traceOverheadSpinner = new JSpinner();
	private final JLabel lblPerStatementTrace = new JLabel("Per statement trace overhead (ms):");
	private final JLabel lblMaxStmtsTo = new JLabel("Max output file size (GB)");
	private final JSpinner maxOutputSizeSpinner = new JSpinner();
	private final JPanel progressPanel = new JPanel();
	private final JProgressBar progressBar = new JProgressBar();

    private Task task;
    private int inputCount = 0;
    private int outputCount = 0;
    private long inputFileSize = 0L;

	 
    class Task extends SwingWorker<Void, Void> {
        /*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
            
            setProgress(0);
            progressBar.setVisible(true);
            
    		int maxOutputSizeGB = (Integer) maxOutputSizeSpinner.getValue();
    		long maxOutputSize = (long) maxOutputSizeGB * 1024 * 1024 * 1024;
    		String traceOverheadStr = (String) traceOverheadSpinner.getValue();
    		
    		BigDecimal traceOverheadNum = new BigDecimal(traceOverheadStr);
    		traceOverheadNum = traceOverheadNum.setScale(2, RoundingMode.HALF_DOWN);
    		
            Writer writer = null;
    		
            FileInputStream fileInputStream = null;
            DataInputStream dataInputStream = null;
            BufferedReader bufferedReader = null;
            
    		try
            {
                // Create new output file:

                FileOutputStream outStream = new FileOutputStream(outputFileName.getText().trim());
                writer = new OutputStreamWriter(outStream);

                // Read records in input file:

                fileInputStream = new FileInputStream(inputFileName.getText().trim());
                dataInputStream = new DataInputStream(fileInputStream);
                bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
                
                /*
                 	
                 	Example of input records:
                 	
					    0146 SKIP_TRACE:
					        {0.31}{0.31}
					    0150 REM BOGUS
					        {0.48}{0.00}
					    0150 REM addr "OPEN90",err=ignore_addr
					        {0.48}{0.18}
					    0202 OPEN (2)"INVEN"
					        {15.0}{14.4}
					    0203 OPEN (3)"U8"
					        {47.29}{32.29}
					    0204 OPEN (4)"BAR1"
					        {51.54}{4.25}
					    0208 OPEN (8)"MINAD"
					        {64.45}{3.3}
                 	
                 */

                String inputRecord;
                long inputBytesRead = 0L;
                long outputBytesWritten = 0L;
                boolean doneWriting = false;
                boolean isLinux = System.getProperty("os.name").contains("Linux");

                while ((inputRecord = bufferedReader.readLine()) != null)
                {
                	long inputRecordLength = inputRecord.length() + (isLinux ? 2 : 1);
                	inputBytesRead += inputRecordLength;

            		int percentComplete = Math.round( ( (float) inputBytesRead / inputFileSize) * 100);
                	setProgress(percentComplete);
                	
                	int bracesPos = inputRecord.indexOf("}{");
                	
                	if (bracesPos != -1)
                	{
                    	inputCount++;
                		
                    	if (!doneWriting)
                    	{
                        	int lastBracePos = inputRecord.lastIndexOf("}");
                        	String runTimeStr = inputRecord.substring(bracesPos + 2, lastBracePos);
        					BigDecimal runTimeNum = new BigDecimal(runTimeStr);
        					
        					// Round to 2 decimal places:
        					runTimeNum = runTimeNum.setScale(2, RoundingMode.HALF_DOWN);
        					runTimeNum = runTimeNum.subtract(traceOverheadNum);
        					
        					if (runTimeNum.compareTo(BigDecimal.ZERO) < 0)
        					{
        						runTimeNum = BigDecimal.ZERO;
        					}
        					
            				String newRunTimeNumStr = runTimeNum.toString();
            				String outputRecord = inputRecord.substring(0, bracesPos) + "}{" + newRunTimeNumStr + "}";
                            writer.write(outputRecord + "\n");
                        	outputCount++;
                        	outputBytesWritten += outputRecord.length() + (isLinux ? 2 : 1);
                    	}
                    	
                    	// Only end the writing immediately after a run-time stats record
                    	if (outputBytesWritten >= maxOutputSize)
                    	{
                    		doneWriting = true;
                    	}
                    	
                	}
                	else
                	{
                    	if (!doneWriting)
                    	{
                    		writer.write(inputRecord + "\n");
                        	outputBytesWritten += inputRecordLength;
                    	}
                	}

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
            
            return null;
        }
 
        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
        	
        	progressBar.setValue(100);
            
    		NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
    		
    		String fmtInputCount = formatter.format(inputCount);
    		String fmtOutputCount = formatter.format(outputCount);
    		
    		String msg = "The process has completed.  " + fmtInputCount + " input statements.  " + fmtOutputCount + " output statements.";
    		JOptionPane.showMessageDialog(appWindow.getContentPane(),	msg, "Finished",	JOptionPane.INFORMATION_MESSAGE);
    		
    		// appWindow.dispose();
    		
    		// TODO in future use modal dialog so everything is blocked from input.
    		
    		runButton.setEnabled(true);
    		inputChooseButton.setEnabled(true);
    		outputChooseButton.setEnabled(true);
    		inputFileName.setEnabled(true);
    		outputFileName.setEnabled(true);
    		traceOverheadSpinner.setEnabled(true);
    		maxOutputSizeSpinner.setEnabled(true);
    		
    		progressBar.setVisible(false);
    		
        }
    }
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					PreprocessTraceFileApp window = new PreprocessTraceFileApp();
					window.appWindow.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PreprocessTraceFileApp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
		inputFileName.setFont(new Font("Tahoma", Font.PLAIN, 13));
		inputFileName.setBounds(134, 13, 460, 30);
		inputFileName.setColumns(10);
		appWindow = new JFrame();
		appWindow.setTitle("Preprocess Basis Trace File");
		appWindow.setBounds(100, 100, 800, 664);
		appWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		appWindow.getContentPane().setLayout(null);

		
		inputPanel.setBounds(22, 94, 732, 50);
		
		appWindow.getContentPane().add(inputPanel);
		inputPanel.setLayout(null);

		inputChooseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				OpenFileDialog dialog = new OpenFileDialog();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt", "txt");
				dialog.getFileChooser().setFileFilter(filter);
				dialog.setVisible(true);
				
				// The above dialog is modal.  This frame is blocked until the dialog is disposed.
				// At that time, execution resumes here.
				
				String selectedFileName = dialog.getSelectedFileName();
				
				if (selectedFileName.length() > 0)
				{
					inputFileName.setText(selectedFileName);
				}
			}
		});

		inputChooseButton.setBounds(629, 16, 93, 23);
		inputPanel.add(inputChooseButton);
		lblInputFileName.setBounds(10, 20, 96, 14);
		
		inputPanel.add(lblInputFileName);
		
		inputPanel.add(inputFileName);
		outputPanel.setLayout(null);
		outputPanel.setBounds(22, 155, 732, 50);
		
		appWindow.getContentPane().add(outputPanel);

		outputChooseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				OpenFileDialog dialog = new OpenFileDialog();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt", "txt");
				dialog.getFileChooser().setFileFilter(filter);
				dialog.setVisible(true);
				
				// The above dialog is modal.  This frame is blocked until the dialog is disposed.
				// At that time, execution resumes here.
				
				String selectedFileName = dialog.getSelectedFileName();
				
				if (selectedFileName.length() > 0)
				{
					outputFileName.setText(selectedFileName);
				}
			}
		});

		outputChooseButton.setBounds(629, 11, 93, 23);
		
		outputPanel.add(outputChooseButton);
		lblOutputFileName.setBounds(10, 15, 110, 14);
		
		outputPanel.add(lblOutputFileName);
		outputFileName.setColumns(10);
		outputFileName.setBounds(134, 8, 460, 30);
		
		outputPanel.add(outputFileName);
		runPanel.setBounds(22, 340, 732, 40);
		
		appWindow.getContentPane().add(runPanel);

		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				boolean hasError = false;
						
				String trimmedInputFilename = inputFileName.getText().trim();
				
				if (trimmedInputFilename.length() == 0)
				{
					JOptionPane.showMessageDialog(appWindow.getContentPane(), "The input file name is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
					hasError = true;
				}
				else if ( !doesFileExist(trimmedInputFilename) )
				{
					JOptionPane.showMessageDialog(appWindow.getContentPane(), "The input file name does not exist.", "Input Error", JOptionPane.ERROR_MESSAGE);
					hasError = true;
				}

				String trimmedOutputFilename = outputFileName.getText().trim();
				
				if (trimmedOutputFilename.length() == 0)
				{
					JOptionPane.showMessageDialog(appWindow.getContentPane(), "The output file name is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
					hasError = true;
				}
				else if ( !doesDirectoryForFileExist(trimmedOutputFilename) )
				{
					JOptionPane.showMessageDialog(appWindow.getContentPane(), "The output file directory does not exist.", "Input Error", JOptionPane.ERROR_MESSAGE);
					hasError = true;
				}
				
				if (trimmedInputFilename.equals(trimmedOutputFilename))
				{
					JOptionPane.showMessageDialog(appWindow.getContentPane(), "The input and output file names cannot be the same.", "Input Error", JOptionPane.ERROR_MESSAGE);
					hasError = true;
				}

				if (!hasError)
				{
					// Give warning if output file already exists:
					
					if (doesFileExist(trimmedOutputFilename))
					{
						int option = JOptionPane.showConfirmDialog(appWindow.getContentPane(), "The output file already exists.  OK to continue?", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
						
						if (option == JOptionPane.OK_OPTION)
						{
							inputFileSize = getFileSize(trimmedInputFilename);
							doRun();
						}
					}
					else
					{
						inputFileSize = getFileSize(trimmedInputFilename);
						doRun();
					}
					
				}
			}
		});
		
		runPanel.add(runButton);
		panel_3.setBounds(22, 31, 732, 50);
		
		appWindow.getContentPane().add(panel_3);
		panel_3.setLayout(null);
		txtpnThisProgramConverts.setEditable(false);
		txtpnThisProgramConverts.setBackground(SystemColor.menu);
		txtpnThisProgramConverts.setText("This program converts a trace file by subtracting \"per statement\" trace overhead time from each statement's run time.");
		txtpnThisProgramConverts.setBounds(12, 13, 708, 34);
		
		panel_3.add(txtpnThisProgramConverts);
		optionsPanel.setBounds(22, 218, 732, 109);
		
		appWindow.getContentPane().add(optionsPanel);
		optionsPanel.setLayout(null);
		traceOverheadSpinner.setModel(new SpinnerListModel(new String[] {".00", ".01", ".02", ".03", ".04", ".05", ".06", ".07", ".08", ".09"}));
		traceOverheadSpinner.setBounds(268, 21, 56, 31);
		traceOverheadSpinner.setValue(".01");
		
		optionsPanel.add(traceOverheadSpinner);
		lblPerStatementTrace.setBounds(12, 29, 231, 14);
		
		optionsPanel.add(lblPerStatementTrace);
		lblMaxStmtsTo.setBounds(12, 73, 231, 14);
		
		optionsPanel.add(lblMaxStmtsTo);
		maxOutputSizeSpinner.setModel(new SpinnerNumberModel(5, 1, 20, 1));
		maxOutputSizeSpinner.setBounds(268, 65, 56, 31);
		
		optionsPanel.add(maxOutputSizeSpinner);
		progressPanel.setBounds(22, 394, 732, 40);
		
		appWindow.getContentPane().add(progressPanel);
		
		progressPanel.add(progressBar);
		
	}
	
	 
    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
            // taskOutput.append(String.format("Completed %d%% of task.\n", task.getProgress()));
        } 
    }

    
	private void doRun()
	{
		// TODO in future use modal dialog so everything is blocked from input.
		
		runButton.setEnabled(false);
		inputChooseButton.setEnabled(false);
		outputChooseButton.setEnabled(false);
		inputFileName.setEnabled(false);
		outputFileName.setEnabled(false);
		traceOverheadSpinner.setEnabled(false);
		maxOutputSizeSpinner.setEnabled(false);
		
		task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();
	}
	
	
	private boolean doesFileExist(String filePath)
	{
		File file = new File(filePath);
		return file.isFile();
	}
	
	
	private long getFileSize(String filePath)
	{
		File file = new File(filePath);
		return file.length();
	}
	
	
	private boolean doesDirectoryForFileExist(String filePath)
	{
		int lastSlash = filePath.lastIndexOf("\\");   // Windows
		
		if (lastSlash == -1 )
		{
			lastSlash = filePath.lastIndexOf("/");   // Linux
		}
		
		if (lastSlash == -1 )
		{
			return false;
		}
		
		String directoryPath = filePath.substring(0,  lastSlash);
		
		File directory = new File(directoryPath);
		return directory.isDirectory();
	}
	
}