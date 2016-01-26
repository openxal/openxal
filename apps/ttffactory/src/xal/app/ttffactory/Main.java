/*
 * Main.java - Main file for the ttfFactory application, sets up the GUI and its functions
 * @author James Ghawaly Jr.
 * Created on Mon June 15 13:23:35 EDT 2015
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ttffactory;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import xal.model.ModelException;
import xal.tools.math.fnc.poly.RealUnivariatePolynomial;
import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;


/**
 * This is the main class for the transit time factor parsing application  .
 *
 * @author  James Ghawaly Jr.
 * @version   0.1  15 June 2015
 */
public class Main extends JFrame {
	
	//establish some global constants
	private static final long serialVersionUID   = 1L;
	private static final PrintStream PRINTER     = System.out;
	private DataTree lastDataTree                = null;
	private DataTree lastBetaTree                = null;
	private static final Tools generalTools      = new Tools();
	private static final int OUTSPACE            = 100;
	private static DataTree andreiFixed    = null;

	/**
	 * Instantiates a new main.
	 */
	public Main() {
        initUI();
    }
	
	/**
	 * This method initializes the Graphical user Interface (GUI)
	 */
    private void initUI() {

    	final Parser parser = new Parser();
    	
    	// Initiate GUI Buttons
    	
        final JButton fileSelectorButton =          new JButton("Browse");  

        final JButton runButton =                   new JButton("Run");
        
        final JButton analyzeButton =               new JButton("Analyze");
        
        final JButton acceleratorTTFButton =        new JButton("Accelerator");
        
        final JButton sequenceTTFButton =           new JButton("Sequence");
        
        final JButton gapTTFButton =                new JButton("Gap");
        
        final JButton generateXDXFButton =          new JButton("Generate XDXF File");
        
        final JLabel calcLabel =                    new JLabel("Calculate TTF:      ");
        
        final JButton envelopeButton =              new JButton("Get Envelope");
        
        // When hovering cursor over the buttons, display the selected button's purpose
        fileSelectorButton.setToolTipText("Select File from Directory Browser");  
        runButton.setToolTipText("Run the Parser and Create New File");
        analyzeButton.setToolTipText("Retrieve the specified data from the specified gap");
        acceleratorTTFButton.setToolTipText("Generate TTFs for all gaps in a chosen accelerator");
        sequenceTTFButton.setToolTipText("Generate TTFs for all gaps in a chosen sequence");
        gapTTFButton.setToolTipText("Generate TTF for a single chosen gap.");
        envelopeButton.setToolTipText("Get the envelope Sigma X, Y, Z");
        generateXDXFButton.setToolTipText("Generate xdxf file as an optics extra input into the accelerator");
        
        runButton.setEnabled(false);
        analyzeButton.setEnabled(false);
        generateXDXFButton.setEnabled(false);
        
        //create a text field with a default file name
        final JTextField fileLabel = new JTextField("name of file to save to.xdxf");
        
        //JTextField valueLabel = new JTextField("Value Tag");
        String[] tagOptions = {"ttf", "stf", "ttfp", "stfp"};
        final JComboBox<String> valueLabel = new JComboBox<String>(tagOptions);
        
        //create a text area with instructions for how to use the program on the T(k) Tab
        final JTextArea infoBox = new JTextArea("- To parse a file, select the 'Browse' button and choose your file, then select 'Run.' An option to save the new file will be given.\n "
        		                              + "\n- To retrieve a particular value from a specific gap; after running the parser, type the tag of the value that you want to retrieve into the 'Value Tag' text area, choose the gap from the drop down menu and select 'Analyze.'\n "
        		                              + "\n- Possible choices are: ttf, stf, ttfp, stfp.\n \n- See the README for more information.");
        infoBox.setEditable(false);
        infoBox.setWrapStyleWord(true);
        infoBox.setLineWrap(true);

        //create a text area with instructions for how to use the program on the T(Beta) tab
        final JTextArea infoBox2 = new JTextArea("- To calculate transit time factors, use the buttons on the 'Calculate TTF' row.\n" +
        										 "- To compare calculated transit time factors to Andrei's, choose 'Compare to Andrei's TTF'\n"+
        										 "- To generate an xdxf file of integral-calculated TTF polynomials, choose 'Generate XDXF File'\n");
        infoBox2.setEditable(false);
        infoBox2.setWrapStyleWord(true);
        infoBox2.setLineWrap(true);
        
        //create a label for the results of a value point analysis
        final JLabel resultLabel = new JLabel("Result: ");
        
        final JTextField resultText = new JTextField("...");
        resultText.setEditable(false);
        
        final JLabel gapLabel = new JLabel("Choose RF Gap: ");
        
        //create a drop down menu that eventually contains all of the gaps in the accelerator
        final JComboBox<String> gapChooser = new JComboBox<String>();
        
        //INITIATE ALL FILE CHOOSERS
        // This is the file chooser menu, it brings up a directory explorer
        final JFileChooser fileSelector = new JFileChooser();
        
        final JFileChooser saveSelector = new JFileChooser();
        saveSelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        final JFileChooser gapSelector = new JFileChooser();
        
        final JFileChooser acceleratorSelector = new JFileChooser();
        acceleratorSelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        final JFileChooser sequenceSelector = new JFileChooser();
        sequenceSelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        final JFileChooser generateSelector = new JFileChooser();
        generateSelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
//        final JFileChooser compareSelector = new JFileChooser();
        
        gapChooser.setToolTipText("Choose a Gap From the Drop-down Menu to Analyze");
        valueLabel.setToolTipText("Type the Tag of the Value You Want to Get From the Selected Gap; options: ttf, stf, ttfp, stfp");
        
      //----------------------------------------------------------------------------------------------- START ACTIONS
        
        
        // We add an action listener to the file selector button, which signals the method actionPerformed when clicked
        fileSelectorButton.addActionListener(new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent event) {
            	
            	// choice is an integer that corresponds to the action selected by the user
                int choice = fileSelector.showOpenDialog(Main.this);
                
                // if the choice is valid
                if (choice == JFileChooser.APPROVE_OPTION) {
                	// grab the selected file using java's io File class
                	File file = fileSelector.getSelectedFile();
                	// print the name of the file we are opening
                	PRINTER.println("Opening File: " + file.getName());
                	runButton.setEnabled(true);
                } else {
                	// If the user closes the file chooser before selecting a file, print the following line
                	PRINTER.println("File Selection Aborted by User\n");
                }
                getContentPane().revalidate();
            }
            
        });

        // We add an action listener to the run button, which signals the method actionPerformed when clicked 
        runButton.addActionListener(new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent event) {
            	//Parser parser = new Parser();
        		try {
					parser.parse(fileSelector.getSelectedFile());
	        		PRINTER.println("File Parsed");
	        		ArrayList<String> gapList = parser.getGapList();
	        		// This adds the gaps to the gap chooser
	        		for (String str : gapList) {
	        			gapChooser.addItem(str);
	        		}
	        		
	        		int optionResult = JOptionPane.showConfirmDialog(getContentPane(), "Would you like to save the parsed data to a new file?");
	        		// allow the analyze button to be pressed
                    analyzeButton.setEnabled(true);
                    getContentPane().revalidate();
                    
	        		// If the user chooses to save the parsed data to a file, run this block of code
	        		if (optionResult == JOptionPane.YES_OPTION) {
	                    int choice = saveSelector.showOpenDialog(Main.this);
	                    // if the choice is valid
	                    if (choice == JFileChooser.APPROVE_OPTION) {
	                    	// grab the selected file using java's io File class
	                    	File file = saveSelector.getSelectedFile();
	                    	// print the name of the file we are opening
	                    	PRINTER.println("Saving to File: " + file.getName());
	                    	String filename = fileLabel.getText();
	                    	parser.pack(new File(file,filename), parser.getDataTree(),false);
	                    } else {
	                    	// If the user closes the file chooser before selecting a file, print the following line
	                    	PRINTER.println("File Selection Aborted by User");
	                    }
	                    
	        		}
	        		
	        		// The following code block creates an error dialog box if an error of the types listed below is encountered
				} catch (ParseException | ResourceNotFoundException
						| MalformedURLException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage(), e.getClass().getName() + " ERROR", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}

            }
        });
        
        // This action listener is called when the "Analyze" button is clicked
        analyzeButton.addActionListener(new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent event) {
            	String valueChoice = (String) valueLabel.getSelectedItem();
            	String gapChoice = (String) gapChooser.getSelectedItem();
            	String data = parser.getValue(gapChoice, valueChoice);
            	resultText.setText(data);
            	JTextField infoText = new JTextField(data);
            	infoText.setEditable(false);
            	JOptionPane.showMessageDialog(getContentPane(), infoText,valueChoice + " for " + gapChoice,JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        //TODO: Handle SCL and END gaps
        gapTTFButton.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent event) {
        		int choice = gapSelector.showOpenDialog(Main.this);

                if (choice == JFileChooser.APPROVE_OPTION) {
                	// grab the selected file using java's io File class
                	File file = gapSelector.getSelectedFile();
                	try {
						DataTree gapTree = gapTTF(file);

						String gapName = gapTree.getGaps().get(0);

						JOptionPane.showMessageDialog(getContentPane(), 
													"TTF Polynomial: "   + gapTree.getValue(gapName,"ttf_string")     + 
													"\nSTF Polynomial: " + gapTree.getValue(gapName, "stf_string")    +
													"\nTTFP Polynomial"  + gapTree.getValue(gapName, "ttfp_string")   +
													"\nSTFP Polynomial"  + gapTree.getValue(gapName, "stfp_string")   ,
													"Gap Polynomials\n", JOptionPane.INFORMATION_MESSAGE        );
						
						setLastTree(gapTree);
						generateXDXFButton.setEnabled(true);
					} catch (IOException | InvalidGapException | IndexOutOfBoundsException e) {
						JOptionPane.showMessageDialog(getContentPane(), e.getMessage(), e.getClass().getName() + " ERROR", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					} 

                } else {
                	PRINTER.println("File Selection Aborted by User");
                }
        		
        	}
        });
        
        //TODO: Handle SCL and END gaps
        sequenceTTFButton.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent event) {
        		
        		int choice = sequenceSelector.showOpenDialog(Main.this);
        		
        		if(choice == JFileChooser.APPROVE_OPTION) {
        			File file = sequenceSelector.getSelectedFile();
        			
	        		try {
						DataTree seqTree = directoryTTF(file);

						setLastTree(seqTree);
						
						generateXDXFButton.setEnabled(true);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(getContentPane(), e.getMessage(), e.getClass().getName() + " ERROR", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
        		
        		} else {
        			PRINTER.println("File Selection Aborted by User");
        		}
        	}
        });
        
        //TODO: Handle SCL and END gaps
        acceleratorTTFButton.addActionListener(new ActionListener() {
        	
	        /* (non-Javadoc)
	         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	         */
	        @Override
        	public void actionPerformed(ActionEvent event) {

        		int choice = acceleratorSelector.showOpenDialog(Main.this);
            	
                if (choice == JFileChooser.APPROVE_OPTION) {
                	// grab the selected file using java's io File class
                	File file = acceleratorSelector.getSelectedFile();
                	
                	try {
						directoryTTF(file);
						DataTree gapTree = directoryTTF(file);
						
						setLastTree(gapTree);
						
						generateXDXFButton.setEnabled(true);
					} catch (IOException | IndexOutOfBoundsException e) {
						JOptionPane.showMessageDialog(getContentPane(), e.getMessage(), e.getClass().getName() + " ERROR", JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
                } else {
                	PRINTER.println("File Selection Aborted by User");
                }
        	}
        });
        
        generateXDXFButton.addActionListener(new ActionListener() {
        	
	        /* (non-Javadoc)
	         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	         */
	        @Override
        	public void actionPerformed(ActionEvent event) {

        		int choice = generateSelector.showOpenDialog(Main.this);
            	
                if (choice == JFileChooser.APPROVE_OPTION) {
                	// grab the selected file using java's io File class
                	File file = generateSelector.getSelectedFile();
                	// print the name of the file we are opening
                	PRINTER.println("\nSaving to File: " + file.getName() + "\n");
                	
                	String filename = JOptionPane.showInputDialog(getContentPane(), "Type the name of the file to save to.", "Save to XDXF", JOptionPane.INFORMATION_MESSAGE);

                	parser.pack(new File(file,filename),getLastTree(),true);
                	PRINTER.println("Finished Saving to File\n");
                } else {
                	PRINTER.println("\nFile Selection Aborted by User\n");
                }
        	}
        });
        
        envelopeButton.addActionListener(new ActionListener() {
        	
	        /* (non-Javadoc)
	         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	         */
	        @Override
        	public void actionPerformed(ActionEvent event) {

        		String seqOption = JOptionPane.showInputDialog(getContentPane(), "Type the name of the sequence/combosequence to get envelope for\n Possible Options: DTL, SCLHigh, SCLMed, MEBT, CCL, CCL1, etc.", "Sequence Selector"
        				, JOptionPane.QUESTION_MESSAGE);
        		
        		try {
					generalTools.envelopeComparison(seqOption);
				} catch (InstantiationException | ModelException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage(), e.getClass().getName() + " ERROR", JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
				}
        	}
        });
        //----------------------------------------------------------------------------------------------- END ACTIONS
        JTabbedPane tabPane = new JTabbedPane();

    	JPanel panes = new JPanel();
    	JPanel pane2 = new JPanel();
    	
        // This line calls the createLayout method, which formats how items are displayed on the GUI
        GroupLayout gl = createLayout(true, panes, fileSelectorButton, runButton, fileLabel, gapLabel, gapChooser, valueLabel, analyzeButton, resultLabel, resultText, infoBox);
        //                                                  0                     1            2           3        4             5                 6  
        GroupLayout gl2 = createLayout(false,pane2,acceleratorTTFButton,sequenceTTFButton,gapTTFButton,calcLabel,infoBox2,generateXDXFButton,envelopeButton);
        
        panes.setLayout(gl);
        pane2.setLayout(gl2);
        
        tabPane.addTab("T(Beta)",pane2);
        tabPane.addTab("T(k)",panes);
        
        paint(tabPane);
        
    }
    
    /**
     * This method defines how the GUI component will be formatted. In this case we use a GroupLayout setup, as it is robust
     *
     * @param arg the list of components in the GUI
     */
    private GroupLayout createLayout(Boolean tabChoice, Container cont, JComponent... arg) {
    	GroupLayout gl = null;
    	if(tabChoice) {
    		gl = new GroupLayout(cont);
	        
	        gl.setAutoCreateGaps(true);
	        gl.setAutoCreateContainerGaps(true);
	
	        gl.setHorizontalGroup(
	        		gl.createParallelGroup()
	        		.addComponent(arg[9],GroupLayout.PREFERRED_SIZE, 740,GroupLayout.PREFERRED_SIZE)
	                .addGroup(gl.createSequentialGroup()
	                		.addComponent(arg[0]) // browse button
	                		.addComponent(arg[2]) // file chosen label
	                		.addComponent(arg[1]) // run button
	                		)
	                .addGroup(gl.createSequentialGroup()
	                		.addComponent(arg[6]) // analyze button
	                		.addComponent(arg[4]) // gapChooser
	                		.addComponent(arg[5]) // 
	                )
	
	        );
	        gl.setVerticalGroup(
	        		gl.createSequentialGroup()
	        		.addComponent(arg[9],GroupLayout.PREFERRED_SIZE, 150,GroupLayout.PREFERRED_SIZE)
	                .addGroup(gl.createParallelGroup()
	                		.addComponent(arg[0]) // browse button
	                		.addComponent(arg[2]) // file chosen label
	                		.addComponent(arg[1]) // run button
	                		)
	                .addGroup(gl.createParallelGroup()
	                		.addComponent(arg[6]) // analyze button
	                		.addComponent(arg[4]) // gapChooser
	                		.addComponent(arg[5]) // 
	                		)
	        );
    	} else {
    		gl = new GroupLayout(cont);
    		
    		gl.setAutoCreateGaps(true);
	        gl.setAutoCreateContainerGaps(true);
	        
	        gl.setHorizontalGroup(
	        		gl.createParallelGroup()
	        		.addComponent(arg[4],GroupLayout.PREFERRED_SIZE, 750,GroupLayout.PREFERRED_SIZE) // info box
	        		.addGroup(gl.createSequentialGroup()
	        				.addComponent(arg[3]) // calc label
	        				.addComponent(arg[0]) // accelerate TTF button
	        				.addComponent(arg[1]) // sequence TTF button
	        				.addComponent(arg[2]) // gap TTF Button
	        				)
	        		.addGroup(gl.createSequentialGroup()
	        				.addComponent(arg[5]) // generate button
	        				.addComponent(arg[6]) // envelope button
	        				)
	        		);
	        gl.setVerticalGroup(
	        		gl.createSequentialGroup()
	        		.addComponent(arg[4],GroupLayout.PREFERRED_SIZE, 70,GroupLayout.PREFERRED_SIZE) // info box
	        		.addGroup(gl.createParallelGroup()
	        				.addComponent(arg[3]) // calc label
	        				.addComponent(arg[0]) // accelerate TTF button
	        				.addComponent(arg[1]) // sequence TTF button
	        				.addComponent(arg[2]) // gap TTF Button
	        				)
	        		.addGroup(gl.createParallelGroup()
	        				.addComponent(arg[5]) // generate button
	        				.addComponent(arg[6]) // envelope button
	        				)
	        		);
    	}
    	
        return gl;
    }
    
    
    /**
     * This method actually paints the GUI onto the screen
     *
     * @param comp the component containing all of the GUI features to be added to the frame
     */
    private void paint(JComponent comp) {
    	JFrame frame = new JFrame();
        frame.add(comp, BorderLayout.CENTER);
        frame.setTitle("TTF Parser");
        frame.setSize(780, 300);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);                              // This line centers the GUI on the screen
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);                  // Exits application upon clicking the X button on the GUI
        frame.setVisible(true);
    }
    
    /**
     * Gap ttf. Handles the gapTTF button
     *
     * @param file the file to calculate the TTF/etc.
     * @return the data tree containing data for the gap
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InvalidGapException the invalid gap exception
     */
    private DataTree gapTTF(File file) throws IOException, InvalidGapException {
	    String filePath = file.getAbsolutePath();
	    Path filePathPath = file.toPath();
	    String fileName = file.getName();
	    
	    Boolean inner = false;
	    Boolean end = false;
	    Boolean ccl = false;
	    
		Parser betaParser = new Parser();                                                        // Next three lines: grab the beta mins/maxes for each gap
		betaParser.readBetaConfigFile();
		DataTree betaTree = betaParser.getDataTree();
		
		setLastBetaTree(betaTree);
	    
	    DataTree gapDatTree = new DataTree();
	    
	    if (generalTools.isInnerGap(fileName))    { inner = true; }
	    if (generalTools.isEndGap(fileName))      { end = true;   }
	    if (fileName.startsWith("CCl"))           { ccl = true;   }
	    
	    if (!inner && !end && !ccl) {
	    	
	    	gapDatTree = normalGapTTF(filePath,fileName,betaTree);
			
	    } else if(inner && !ccl && !end) {
	    	String fileNameOuter = fileName.substring(0, fileName.lastIndexOf("IN")) + "END.DAT";
	    	String strPathOuter = "/" + filePathPath.subpath(0, filePathPath.getNameCount()-1).toString() + "/" + fileNameOuter;
	    	PRINTER.println(filePath);
	    	DataTree thisDatTree = endGapTTF(filePath,fileName,strPathOuter,fileNameOuter,betaTree);
    	    
//			String gapName = thisDatTree.getGaps().get(0);
			
			Set<Entry<String,List<String>>> curEntSet = thisDatTree.getEntrySet();
			
			for (Entry<String,List<String>> entry:curEntSet) {
				gapDatTree.addListToTree(entry.getKey(), entry.getValue());
			} 
			
	    } else if(ccl && !end) {
	    	
	    	gapDatTree = normalGapTTF(filePath,fileName,betaTree);
	    	
		} else if(end)   {
	    	throw new InvalidGapException("The chosen gap is an END gap, thus it cannot be analyzed. Choose 'INNER' version of same gap.");
	    }
	    PRINTER.println("\nFinished...\n");
	    
		return gapDatTree;
    }
    
    /**
     * Directory ttf. This iterates through all files in the chosen directory and calculates the TTF/etc. for every EField gap file in the directory
     *
     * @param file the chosen directory
     * @return the data tree containing all data for every gap in the directory (accelerator or sequence)
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private DataTree directoryTTF(File file) throws IOException {
    	String filePathString = file.getAbsolutePath();
    	
    	Path begin = Paths.get(filePathString);
    	
		DataTree accDatTree = new DataTree();
		
		// we want to walk through all subdirectories of the chosen directory and calculate the TTFs of the RF Gap files
		Files.walkFileTree(begin, new SimpleFileVisitor<Path>() { 
		    @Override
		    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
		    	File file = filePath.toFile();
		    	//Skip all of the .DS_Store files
		    	if(!file.isHidden()){
		    		String stringLocalFilePath = file.getAbsolutePath();
		    		String fileName = file.getName();
		    		
		    		//This converts the filename to the name required for OpenXAL
		    		String parsedName = generalTools.transformName(fileName);

		    		Boolean inner = false;
		    	    Boolean end = false;
		    	    Boolean ccl = false;
		    	    Boolean scl = false;

		    		Parser betaParser = new Parser();                                                        // Next three lines: grab the beta mins/maxes for each gap
		    		betaParser.readBetaConfigFile();
		    		DataTree betaTree = betaParser.getDataTree();
		    		
		    		setLastBetaTree(betaTree);
		    	    PRINTER.println(parsedName);
		    	    if (generalTools.isInnerGap(fileName)) { inner = true; }
		    	    if (generalTools.isEndGap(fileName))   { end   = true; }
		    	    if (parsedName.startsWith("CCL"))      { ccl   = true; }
		    	    if (parsedName.startsWith("SCL"))      { scl   = true; }
		    	    
		    	    if (!inner && !end && !ccl && !scl) {
		    	    	DataTree gapDatTree = normalGapTTF(stringLocalFilePath,fileName, betaTree);
		    	    
//						String gapName = gapDatTree.getGaps().get(0);   //get the entry
						
						Set<Entry<String,List<String>>> curEntSet = gapDatTree.getEntrySet(); // get the set from the entry
						
						for (Entry<String,List<String>> entry:curEntSet) {                    // add entry to accelerator data tree
							accDatTree.addListToTree(entry.getKey(), entry.getValue());
						}
		    			
		    	    } else if(inner && !ccl && !end && !scl) {
		    	    	String fileNameOuter = fileName.substring(0, fileName.lastIndexOf("IN")) + "END.DAT";
		    	    	String strPathOuter = "/" + filePath.subpath(0, filePath.getNameCount()-1).toString() + "/" + fileNameOuter;
		    	    	
		    	    	DataTree gapDatTree = endGapTTF(stringLocalFilePath,fileName,strPathOuter,fileNameOuter,betaTree);
			    	    
//						String gapName = gapDatTree.getGaps().get(0);
						
						Set<Entry<String,List<String>>> curEntSet = gapDatTree.getEntrySet();
						
						for (Entry<String,List<String>> entry:curEntSet) {
							accDatTree.addListToTree(entry.getKey(), entry.getValue());
						}
		    			
		    	    } else if(ccl && !end && !scl) {
		    	    	String fileNameOuter = fileName.substring(0, fileName.lastIndexOf(".D")) + "END.DAT";
		    	    	String strPathOuter = "/" + filePath.subpath(0, filePath.getNameCount()-1).toString() + "/" + fileNameOuter;
		    	    	
		    	    	DataTree innerGapDatTree = normalGapTTF(stringLocalFilePath,fileName, betaTree);;
		    	    	DataTree outerGapDatTree = endGapTTF(stringLocalFilePath,fileName,strPathOuter,fileNameOuter,betaTree);
		    	    	
		    	    	String[] nameList = generalTools.getCCLNameList(parsedName); //nameList = {END, IN, IN, IN, IN, IN, IN, END};
		    	    	
//		    	    	Set<Entry<String,List<String>>> curInnerEntSet = innerGapDatTree.getEntrySet();
//		    	    	Set<Entry<String,List<String>>> curOuterEntSet = outerGapDatTree.getEntrySet();
						
		    	    	Entry<String,List<String>> innerEnt = innerGapDatTree.getFirstEntry();
		    	    	Entry<String,List<String>> outerEnt = outerGapDatTree.getFirstEntry();
		    	    	int i = 0;
		    	    	for (String name:nameList) {
		    	    		PRINTER.println("WORKING: " + name);
		    	    		if (i == 0 || i == 7) {
		    	    			accDatTree.addListToTree(name, outerEnt.getValue());
		    	    			PRINTER.println("END");
		    	    		} else {
		    	    			accDatTree.addListToTree(name, innerEnt.getValue());
		    	    		}
		    	    		i++;
		    	    	}
						
		    		} else if (scl && !end && !inner && !ccl) {
		    			DataTree gapDatTree = normalGapTTF(stringLocalFilePath,fileName, betaTree);
						
						Entry<String,List<String>> ent = gapDatTree.getFirstEntry();
						
						String[] nameList = generalTools.getSCLNameList(parsedName);
						
		    	    	for (String name:nameList) {
		    	    		if(name != null){
		    	    			accDatTree.addListToTree(name, ent.getValue());
		    	    			}
		    	    	}
		    	    	
		    		} else if(scl && !end && !ccl && inner) {
		    			String fileNameOuter = fileName.substring(0, fileName.lastIndexOf("IN")) + "END.DAT";
		    	    	String strPathOuter = "/" + filePath.subpath(0, filePath.getNameCount()-1).toString() + "/" + fileNameOuter;
		    	    	
		    	    	DataTree gapDatTree = endGapTTF(stringLocalFilePath,fileName,strPathOuter,fileNameOuter,betaTree);
						
		    	    	Entry<String,List<String>> ent = gapDatTree.getFirstEntry();
		    	    	
		    	    	String[] nameList = generalTools.getSCLNameList(parsedName);
						
		    	    	for (String name:nameList) {
		    	    		if(name != null) {
		    	    			accDatTree.addListToTree(name, ent.getValue());
		    	    		}
		    	    	}
		    	    	
		    		}
		    	    
		    	    else if(end)   { }
					
		    	}
		    	PRINTER.println("\nFinished...\n");
		    	
		    	return FileVisitResult.CONTINUE;
		    }
		    	
		});
		return accDatTree;
    }
    
    /**
     * Normal gap ttf. This method calculates the TTF/STF/etc. of the given non-end gap
     *
     * @param filePath the string path to the file
     * @param fileName the string name of the file
     * @return the data tree containing all data for the gap
     * @throws ParseException Signaled when a file cannot be parsed
     * @throws ResourceNotFoundException Signaled if a particular resource is not found
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private DataTree normalGapTTF(String filePath,String fileName,DataTree betaTree) throws ParseException, ResourceNotFoundException, IOException {
    	DataTree datTree = new DataTree();
    	
		
		ElectricFieldReader eFR = new ElectricFieldReader(filePath);
		List<Double> ZData = eFR.getDblZpoints();                                                // get the list of doubles containing the Z position Data
		List<Double> EFdata = eFR.getDblEField();                                                // get the list of doubles containing the Electric Field Data

		TTFTools ttfT = new TTFTools();
		
		String parsedName = generalTools.transformName(fileName);                                  // Change the filename to the name needed for OpenXAL
		
		PRINTER.println("\nAnalyzing..." + parsedName + "\n");
		
		String bMinStr = betaTree.getValue(parsedName, "beta_min");                               // grab the beta_min from the DataTree
		String freqStr = betaTree.getValue(parsedName, "frequency");                              // grab the frequency from the DataTree
		String bMaxStr = betaTree.getValue(parsedName, "beta_max");                               // grab the beta_max from the DataTree
		
		double betaMin = Double.parseDouble(bMinStr);
		double betaMax = Double.parseDouble(bMaxStr);
		double frequency = Double.parseDouble(freqStr);
		
		double[] betaList = ttfT.linspace(betaMin, betaMax , OUTSPACE);                                // this makes a 100 length array of evenly spaced number
		double[] ttfList = ttfT.getTTFForBetaRange(ZData, EFdata, frequency, betaList);     // evaluates the TTF integral at all betas in betaList
		double[] ttfpList = ttfT.getTTFPForBetaRange(ZData, EFdata, frequency, betaList);     // evaluates the TTF integral at all betas in betaList
		
		//For TTF
		PolynomialFit polyFitTTF = new PolynomialFit(betaList,ttfList);                           // fits a polynomial to the TTFs as a function of beta
		double[] constsTTF = polyFitTTF.getPolyConstants();                                       // return the constants of said polynomial
		String polyStringTTF = polyFitTTF.toStringPolynomialRep(constsTTF);                       // get a polynomial representation of the polynomial
		String constsStringTTF = polyFitTTF.toStringConsts(constsTTF);
		
		//For TTFP
		PolynomialFit polyFitTTFP = new PolynomialFit(betaList,ttfpList);                           // fits a polynomial to the TTFs as a function of beta
		double[] constsTTFP = polyFitTTFP.getPolyConstants();                                       // return the constants of said polynomial
		String polyStringTTFP = polyFitTTFP.toStringPolynomialRep(constsTTFP);                       // get a polynomial representation of the polynomial
		String constsStringTTFP = polyFitTTFP.toStringConsts(constsTTFP);
				
		//For STF: If not an end gap, these values are zero
		String polyStringSTF = "(0.0)+(0.0)x+(0.0)x^2+(0.0)x^3+(0.0)x^4";
		String constsStringSTF = "0.0,0.0,0.0,0.0,0.0";
		
		//For STFP: If not an end gap, these values are zero
		String polyStringSTFP = "(0.0)+(0.0)x+(0.0)x^2+(0.0)x^3+(0.0)x^4";
		String constsStringSTFP = "0.0,0.0,0.0,0.0,0.0";
		String primName = generalTools.getPrimaryName(parsedName);     
		String secName = generalTools.getSecondaryName(primName, parsedName);

		// BELOW: create a list of all values needed for input into the DataTree
		List<String> currentValueList = new ArrayList<>(Arrays.asList(primName,secName,constsStringTTF,constsStringTTFP,constsStringSTF,constsStringSTFP,freqStr,bMinStr,bMaxStr,polyStringTTF,polyStringSTF,polyStringTTFP,polyStringSTFP));
		
		datTree.addListToTree(parsedName, currentValueList);                                   // add the list of data to the DataTree
		return datTree;
    }
    
    /**
     * End gap ttf. This is for calculating the TTF/STF/etc. of an end gap. It matches the end gap to the corresponding half-gap
     *
     * @param filePath the string path to the file
     * @param fileName the string name of the file
     * @return the data tree holding the information for the end gap
     * @throws ParseException Signals that there is a problem parsing either the beta or E-Field file
     * @throws ResourceNotFoundException Signals that a specific resource was not found
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private DataTree endGapTTF(String filePathInner,String fileNameInner, String filePathOuter, String fileNameOuter, DataTree betaTree) throws ParseException, ResourceNotFoundException, IOException {
    	DataTree datTree = new DataTree();														 // DataTree that will hold the final data
		
		ElectricFieldReader eFRInner = new ElectricFieldReader(filePathInner);                     // The electric file reader for the inner gap
		List<Double> ZDataInner = eFRInner.getDblZpoints();                                      // The z-point data for the inner gap
		List<Double> EFdataInner = eFRInner.getDblEField();                                      // The electric field data for the inner gap
		
		ElectricFieldReader eFROuter = new ElectricFieldReader(filePathOuter);                     // The electric file reader for the outer gap
		List<Double> ZDataOuter = eFROuter.getDblZpoints();                                      // The z-point data for the outer gap
		List<Double> EFdataOuter = eFROuter.getDblEField();                                      // The electric field data for the outer gap
		
		TTFTools ttfT = new TTFTools();
		
		String parsedNameInner = generalTools.transformName(fileNameInner);                             // Change the filename to the name needed for OpenXAL
		
		PRINTER.println("\nAnalyzing..." + parsedNameInner + " END GAP\n");
		
		String bMinStr = betaTree.getValue(parsedNameInner, "beta_min");                               // grab the beta_min from the DataTree
		String freqStr = betaTree.getValue(parsedNameInner, "frequency");                              // grab the frequency from the DataTree
		String bMaxStr = betaTree.getValue(parsedNameInner, "beta_max");                               // grab the beta_max from the DataTree
		
		double betaMin   = Double.parseDouble(bMinStr);
		double betaMax   = Double.parseDouble(bMaxStr);
		double frequency = Double.parseDouble(freqStr);
		
		//this makes a 100 length array of evenly spaced betas
		double[] betaList = ttfT.linspace(betaMin, betaMax , OUTSPACE); 
		//                                                                                                   INNER GAPS
		double[] ttfListInner  =  ttfT.getTTFForBetaRange(ZDataInner, EFdataInner, frequency, betaList);     // evaluates the TTF integral at all betas in betaList
		double[] stfListInner  =  ttfT.getSTFForBetaRange(ZDataInner, EFdataInner, frequency, betaList);     // evaluates the STF integral at all betas in betaList
		double[] ttfpListInner =  ttfT.getTTFPForBetaRange(ZDataInner, EFdataInner, frequency, betaList);    // evaluates the TTFP integral at all betas in betaList
		double[] stfpListInner =  ttfT.getSTFPForBetaRange(ZDataInner, EFdataInner, frequency, betaList);    // evaluates the STFP integral at all betas in betaList
		
		//																								     OUTER GAPS
		double[] ttfListOuter  =  ttfT.getTTFForBetaRange(ZDataOuter, EFdataOuter, frequency, betaList);     // evaluates the TTF integral at all betas in betaList
		double[] stfListOuter  =  ttfT.getSTFForBetaRange(ZDataOuter, EFdataOuter, frequency, betaList);     // evaluates the STF integral at all betas in betaList
		double[] ttfpListOuter =  ttfT.getTTFPForBetaRange(ZDataOuter, EFdataOuter, frequency, betaList);    // evaluates the TTFP integral at all betas in betaList
		double[] stfpListOuter =  ttfT.getSTFPForBetaRange(ZDataOuter, EFdataOuter, frequency, betaList);    // evaluates the STFP integral at all betas in betaList
		
		double[] ttfList  		=  		new double[ttfListInner.length];
		double[] stfList  		=  		new double[stfListInner.length];
		double[] ttfpList 		= 		new double[ttfpListInner.length];
		double[] stfpList 		= 		new double[stfpListInner.length];
		
		for (int i = 0;i<ttfListInner.length;i++) {
			ttfList[i] =  (ttfListInner[i] + ttfListOuter[i])/2.0;
			stfList[i] =  (stfListInner[i] - stfListOuter[i])/2.0;
			
			ttfpList[i] = (ttfpListInner[i] + ttfpListOuter[i])/2.0;
			stfpList[i] = (stfpListInner[i] - stfpListOuter[i])/2.0;
		}
		
		//TTF
		PolynomialFit polyFitTTF = new PolynomialFit(betaList,ttfList);                           // fits a polynomial to the TTFs as a function of beta
		double[] constsTTF = polyFitTTF.getPolyConstants();                                       // return the constants of said polynomial
		String polyStringTTF = polyFitTTF.toStringPolynomialRep(constsTTF);                       // get a polynomial representation of the polynomial
		String constsStringTTF = polyFitTTF.toStringConsts(constsTTF);							  // get a string representation of the constants
		
		//STF
		PolynomialFit polyFitSTF = new PolynomialFit(betaList,stfList);                           // fits a polynomial to the TTFs as a function of beta
		double[] constsSTF = polyFitSTF.getPolyConstants();                                       // return the constants of said polynomial
		String polyStringSTF = polyFitSTF.toStringPolynomialRep(constsSTF);                       // get a polynomial representation of the polynomial
		String constsStringSTF = polyFitSTF.toStringConsts(constsSTF);							  // get a string representation of the constants
		
		//TTFP
		PolynomialFit polyFitTTFP = new PolynomialFit(betaList,ttfpList);                         // fits a polynomial to the TTFs as a function of beta
		double[] constsTTFP = polyFitTTFP.getPolyConstants();                                     // return the constants of said polynomial
		String polyStringTTFP = polyFitTTFP.toStringPolynomialRep(constsTTFP);                    // get a polynomial representation of the polynomial
		String constsStringTTFP = polyFitTTFP.toStringConsts(constsTTFP);						  // get a string representation of the constants
		
		//STFP
		PolynomialFit polyFitSTFP = new PolynomialFit(betaList,stfpList);                         // fits a polynomial to the TTFs as a function of beta
		double[] constsSTFP = polyFitSTFP.getPolyConstants();                                     // return the constants of said polynomial
		String polyStringSTFP = polyFitSTFP.toStringPolynomialRep(constsSTFP);                    // get a polynomial representation of the polynomial
		String constsStringSTFP = polyFitSTFP.toStringConsts(constsSTFP);						  // get a string representation of the constants
		
		String primName = generalTools.getPrimaryName(parsedNameInner);     
		String secName = generalTools.getSecondaryName(primName, parsedNameInner);

		// BELOW: create a list of all values needed for input into the DataTree
		List<String> currentValueList = new ArrayList<>(Arrays.asList(primName,secName,constsStringTTF,constsStringTTFP,constsStringSTF,constsStringSTFP,freqStr,bMinStr,bMaxStr,polyStringTTF,polyStringSTF,polyStringTTFP,polyStringSTFP));
			
		datTree.addListToTree(parsedNameInner, currentValueList);                                   // add the list of data to the DataTree
		
		return datTree;
    }
    // THIS COMPARE METHOD IS BROKEN, DO NOT USE IT EVER
    /**
     * Compare two lists of doubles by calculating the L2 Norm error between them THIS METHOD DOES NOT WORK AND IS NOT USED
     *
     * @param dblPoly1 the double array containing the coefficients of the first polynomial (Our polys)
     * @param dblPoly2 the double array containing the coefficients of the second polynomial (andrei's polys)
     * @param evalMat  the double array containing the values to evaluate the polynomials at
     * @return the double L2 Norm error between the two polynomials
     */
    @SuppressWarnings("unused")
    private double comparePolys(double[] dblPoly1, double[] dblPoly2, double[] evalMat, double frequency) {
           
    	TTFTools ttfTools = new TTFTools();
    	double[] evalMat2 = ttfTools.kCalcArray(evalMat, frequency);               //generate an array containing k rather than beta
    	
    	RealUnivariatePolynomial poly1 = new RealUnivariatePolynomial(dblPoly1);   //the first polynomial
    	RealUnivariatePolynomial poly2 = new RealUnivariatePolynomial(dblPoly2);   //the second polynomial
    	
    	double firstX = evalMat[0];                                                //the first x in evalMat 
    	double lastX  = evalMat[evalMat.length-1];                                 //the last x in evalMat
    	double delta = Math.abs(lastX-firstX)/evalMat.length;                      //the change in x needed for calculating the L2NormError
    	
    	double[] yList1 = new double[evalMat.length];                              //instantiate the double array to hold the evaluations of poly1 at each x in evalMat
    	double[] yList2 = new double[evalMat.length];                              //instantiate the double array to hold the evaluations of poly2 at each x in evalMat
    	
    	for(int i=0;i<evalMat.length;i++) {                                        //populate the y lists
    		yList1[i] = poly1.evaluateAt(evalMat[i]);
    		yList2[i] = poly2.evaluateAt(evalMat2[i]);                             //we must evaluate Andrei's polynomials at k not Beta
    	}
    	
    	return generalTools.l2NormError(yList1, yList2, delta);                    //evaluate the L2 Norm error for yList1 and yList2
    }
    
    /**
     * Sets the last data tree to the last data that was calculated
     *
     * @param lastDatTree the new last tree
     */
    private void setLastTree(DataTree lastDatTree) {
    	this.lastDataTree = lastDatTree;
    }
    
    /**
     * gets the last data tree to the last data that was calculated
     *
     * @param lastDatTree the new last tree
     */
    private DataTree getLastTree() {
    	return this.lastDataTree;
    }
    
    /**
     * Sets the last beta tree.
     *
     * @param lastBetTree the new last beta tree
     */
    private void setLastBetaTree(DataTree lastBetTree) {
    	this.lastBetaTree= lastBetTree;
    }
    
    /**
     * Gets the last beta tree.
     *
     * @return the last beta tree
     */
    @SuppressWarnings("unused")
    private DataTree getLastBetaTree() {
    	return this.lastBetaTree;
    }
    
    /**
     * Sets andreis data tree with fixed name
     *
     * @param andreiTree the datatree to set as andreiFixed
     */
    @SuppressWarnings("unused")
    private void setAndreiTree(DataTree andreiTree) {
    	Main.andreiFixed = andreiTree;
    }
    
    /**
     * Gets andreis data tree with fixed name
     *
     * @return the last beta tree
     */
    @SuppressWarnings("unused")
    private DataTree getAndreiTree() {
    	return Main.andreiFixed;
    }

    /**
     * The main method. Sets up the GUI
     *
     * @param args There are no arguments
     */
    public static void main(String[] args) {
    	
    	try {
    		PRINTER.println("Launching Application ttfParser...");
    		// This line prevents the application from having UI update concurrency issues
	        EventQueue.invokeLater(new Runnable() {               
	        
	            @Override
	            public void run() {
	                @SuppressWarnings("unused")
					Main ex = new Main(); // instantiate the main class
	            }
	        });
	        PRINTER.println("Application Launched");
    	}
    	// prints an exception and ends the program if there are errors while starting up the application
    	catch (Exception exception){
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			System.exit( -1 );
    	}

    }
}

class InvalidGapException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidGapException(String message){
	     super(message);
	  }

}

