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

import com.sun.glass.events.KeyEvent;

import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributes;


/**
 * This is the main class for the transit time factor parsing application  .
 *
 * @author  James Ghawaly Jr.
 * @version   0.1  15 June 2015
 */
public class Main extends JFrame {

	private static final long serialVersionUID = 1L;

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
        
        final JButton compareTTFButton =            new JButton("Compare to Andrei's TTF");
        
        final JLabel calcLabel =                    new JLabel("Calculate TTF:      ");

        
        // When hovering cursor over the buttons, display the selected button's purpose
        fileSelectorButton.setToolTipText("Select File from Directory Browser");  
        runButton.setToolTipText("Run the Parser and Create New File");
        analyzeButton.setToolTipText("Retrieve the specified data from the specified gap");
        acceleratorTTFButton.setToolTipText("Generate TTFs for all gaps in a chosen accelerator");
        sequenceTTFButton.setToolTipText("Generate TTFs for all gaps in a chosen sequence");
        gapTTFButton.setToolTipText("Generate TTF for a single chosen gap.");
        compareTTFButton.setToolTipText("Compare integral-calculated TTFs to Andrei's TTFs.");
        generateXDXFButton.setToolTipText("Generate xdxf file as an optics extra input into the accelerator");
        
        runButton.setEnabled(false);
        analyzeButton.setEnabled(false);
        compareTTFButton.setEnabled(false);
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
                	System.out.println("Opening File: " + file.getName());
                	runButton.setEnabled(true);
                } else {
                	// If the user closes the file chooser before selecting a file, print the following line
                	System.out.println("File Selection Aborted by User\n");
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
	        		System.out.println("File Parsed");
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
	                    	System.out.println("Saving to File: " + file.getName());
	                    	String filename = fileLabel.getText();
	                    	parser.pack(new File(file,filename));
	                    } else {
	                    	// If the user closes the file chooser before selecting a file, print the following line
	                    	System.out.println("File Selection Aborted by User");
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
						System.out.println("Size: " + gapTree.size());
						String gapName = gapTree.getGaps().get(0);
						System.out.println(gapName);
						JOptionPane.showMessageDialog(getContentPane(), "TTF Polynomial: " + gapTree.getValue(gapName,"ttf_string") + "\nSTF Polynomial: " + 
													  gapTree.getValue(gapName, "stf_string"), "Gap Polynomials\n", JOptionPane.INFORMATION_MESSAGE);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(getContentPane(), e.getMessage(), e.getClass().getName() + " ERROR", JOptionPane.ERROR_MESSAGE);
					}

                } else {
                	System.out.println("File Selection Aborted by User");
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
						directoryTTF(file);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(getContentPane(), e.getMessage(), e.getClass().getName() + " ERROR", JOptionPane.ERROR_MESSAGE);
					}
        		
        		} else {
        			System.out.println("File Selection Aborted by User");
        		}
        	}
        });
        
        //TODO: Handle SCL and END gaps
        acceleratorTTFButton.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent event) {

        		int choice = acceleratorSelector.showOpenDialog(Main.this);
            	
                if (choice == JFileChooser.APPROVE_OPTION) {
                	// grab the selected file using java's io File class
                	File file = acceleratorSelector.getSelectedFile();
                	
                	try {
						directoryTTF(file);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(getContentPane(), e.getMessage(), e.getClass().getName() + " ERROR", JOptionPane.ERROR_MESSAGE);
					}
                } else {
                	System.out.println("File Selection Aborted by User");
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
        GroupLayout gl2 = createLayout(false,pane2,acceleratorTTFButton,sequenceTTFButton,gapTTFButton,calcLabel,infoBox2,generateXDXFButton,compareTTFButton);
        
        panes.setLayout(gl);
        pane2.setLayout(gl2);
        
        tabPane.addTab("T(k)",panes);
        tabPane.addTab("T(Beta)",pane2);
        
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
	        				.addComponent(arg[6]) // compare button
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
	        				.addComponent(arg[6]) // compare button
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
    
    private DataTree gapTTF(File file) throws IOException {
	    String filePath = file.getAbsolutePath();
	    
		Parser betaParser = new Parser();                                                        // Next three lines: grab the beta mins/maxes for each gap
		betaParser.readBetaConfigFile();
		DataTree betaTree = betaParser.getDataTree();
		
		ElectricFileReader eFR = new ElectricFileReader(filePath);
		List<Double> ZData = eFR.getDblZpoints();                                                // get the list of doubles containing the Z position Data
		List<Double> EFdata = eFR.getDblEField();                                                // get the list of doubles containing the Electric Field Data
		
		//for SCL Only
		int i = 0;
		for(Double dbl:EFdata) {
			EFdata.set(i, -1.0*dbl);
			i++;
		}
		//END SCL Only
		
		TTFTools ttfT = new TTFTools();
		Tools tools = new Tools();
		
		String parsedName = tools.transformName(file.getName());                                  // Change the filename to the name needed for OpenXAL
		
		System.out.println("\nStarting..." + parsedName + "\n");
		
		String bMinStr = betaTree.getValue(parsedName, "beta_min");                               // grab the beta_min from the DataTree
		String freqStr = betaTree.getValue(parsedName, "frequency");                              // grab the frequency from the DataTree
		String bMaxStr = betaTree.getValue(parsedName, "beta_max");                               // grab the beta_max from the DataTree
		
		double betaMin = Double.parseDouble(bMinStr);
		double betaMax = Double.parseDouble(bMaxStr);
		double frequency = Double.parseDouble(freqStr);
		
		double[] betaList = ttfT.linspace(betaMin, betaMax , 100);                                // this makes a 100 length array of evenly spaced number
		double[] ttfList = ttfT.getTTFForBetaRange(ZData, EFdata, true, frequency, betaList);     // evaluates the TTF integral at all betas in betaList
		
		double[] stfList = ttfT.getTTFForBetaRange(ZData, EFdata, false, frequency, betaList);    // evaluates the STF integral at all betas in betaList
		
		//For TTF
		PolynomialFit polyFitTTF = new PolynomialFit(betaList,ttfList);                           // fits a polynomial to the TTFs as a function of beta
		double[] constsTTF = polyFitTTF.getPolyConstants();                                       // return the constants of said polynomial
		String polyStringTTF = polyFitTTF.toStringPolynomialRep(constsTTF);                       // get a polynomial representation of the polynomial
		
		//For STF
		double[] constsSTF = {0.0,0.0,0.0,0.0,0.0};
		String polyStringSTF = "(0.0)+(0.0)x+(0.0)x^2+(0.0)x^3+(0.0)x^4";
		String constsStringSTF = "0.0,0.0,0.0,0.0,0.0";
		
		if(tools.isEndGap(parsedName)) {
			PolynomialFit polyFitSTF = new PolynomialFit(betaList,stfList);                           // do the same for STF as above
			constsSTF = polyFitSTF.getPolyConstants();
			polyStringSTF = polyFitSTF.toStringPolynomialRep(constsSTF);
			constsStringSTF = polyFitSTF.toStringConsts(constsSTF);
		}
		
		String primName = tools.getPrimaryName(parsedName);                                       // Next four lines: get data for input into the DataTree
		String secName = tools.getSecondaryName(primName, parsedName);
		String constsStringTTF = polyFitTTF.toStringConsts(constsTTF);
		
		
		DataTree gapDatTree = new DataTree();
		// BELOW: create a list of all values needed for input into the DataTree
		List<String> currentValueList = new ArrayList<>(Arrays.asList(primName,secName,constsStringTTF,null,constsStringSTF,null,freqStr,bMinStr,bMaxStr,polyStringTTF,polyStringSTF,null,null));
		
		gapDatTree.addListToTree(parsedName, currentValueList);                                   // add the list of data to the DataTree
		
		System.out.println("\nFinished...\n");
		
		return gapDatTree;
    }
    
    private DataTree directoryTTF(File file) throws IOException {
    	String filePathString = file.getAbsolutePath();
    	
    	Path begin = Paths.get(filePathString);
    	
    	// we want to walk through all subdirectories of the chosen directory and calculate the TTFs of the RF Gap files
		Parser betaParser = new Parser();
		betaParser.readBetaConfigFile();
		DataTree betaTree = betaParser.getDataTree();
		DataTree accDatTree = new DataTree();
		
		Tools tools = new Tools();

		Files.walkFileTree(begin, new SimpleFileVisitor<Path>() { 
		    @Override
		    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
		    	File file = filePath.toFile();
		    	//Skip all of the .DS_Store files
		    	if(!file.isHidden()){
		    		String fileName = file.getName();
		    		//This converts the filename to the name required for OpenXAL
		    		String parsedName = tools.transformName(fileName);
		    		
		    		System.out.println("Analyzing: " + filePath.getFileName());
		    		
		    		if(!tools.isEndGap(fileName)) {
		    			//Instantiate a new electric file reader for the file path and grab the Z and EF data
			    		ElectricFileReader eFR = new ElectricFileReader(file.getAbsolutePath());
		    			List<Double> ZData = eFR.getDblZpoints();
						List<Double> EFdata = eFR.getDblEField();
						
						TTFTools ttfT = new TTFTools();
						
						String bMinStr = betaTree.getValue(parsedName, "beta_min");                               // grab the beta_min from the DataTree
						String freqStr = betaTree.getValue(parsedName, "frequency");                              // grab the frequency from the DataTree
						String bMaxStr = betaTree.getValue(parsedName, "beta_max");                               // grab the beta_max from the DataTree
						
						double betaMin = Double.parseDouble(bMinStr);
						double betaMax = Double.parseDouble(bMaxStr);
						double frequency = Double.parseDouble(freqStr);
						
						//Calculate the transit time factor at the beta range
						double[] betaList = ttfT.linspace(betaMin, betaMax, 100); 
						double[] ttfList = ttfT.getTTFForBetaRange(ZData, EFdata, true, frequency, betaList);     // evaluates the TTF integral at all betas in betaList
						double[] stfList = ttfT.getTTFForBetaRange(ZData, EFdata, false, frequency, betaList);    // evaluates the STF integral at all betas in betaList
						
						//For TTF
						PolynomialFit polyFitTTF = new PolynomialFit(betaList,ttfList);                           // fits a polynomial to the TTFs as a function of beta
						double[] constsTTF = polyFitTTF.getPolyConstants();                                       // return the constants of said polynomial
						String polyStringTTF = polyFitTTF.toStringPolynomialRep(constsTTF);                       // get a polynomial representation of the polynomial
						
						//For STF
						PolynomialFit polyFitSTF = new PolynomialFit(betaList,stfList);                           // do the same for STF as above
						double[] constsSTF = polyFitSTF.getPolyConstants();
						String polyStringSTF = polyFitSTF.toStringPolynomialRep(constsSTF);
						
						String primName = tools.getPrimaryName(parsedName);                                       // Next four lines: get data for input into the DataTree
						String secName = tools.getSecondaryName(primName, parsedName);
						String constsStringTTF = polyFitTTF.toStringConsts(constsTTF);
						String constsStringSTF = polyFitSTF.toStringConsts(constsSTF);
						
						// BELOW: create a list of all values needed for input into the DataTree
						List<String> currentValueList = new ArrayList<>(Arrays.asList(primName,secName,constsStringTTF,null,constsStringSTF,null,freqStr,bMinStr,bMaxStr,polyStringTTF,polyStringSTF,null,null));
						
						accDatTree.addListToTree(parsedName, currentValueList);                                   // add the list of data to the DataTree
						
						System.out.println("TTF Calculated...");
		    		}
		    		else if(tools.isEndGap(fileName)) {
		    			System.out.println("Skipped END Gap for now...");
		    		}
		    		else if(fileName.contains("SCL")){
		    			System.out.println("Skipped SCL for now...");
		    		}
					
		    	}
		    	return FileVisitResult.CONTINUE;
		    }
		    	
		});
		return accDatTree;
    }
    
    /**
     * The main method. Sets up the GUI
     *
     * @param args There are no arguments
     */
    public static void main(String[] args) {
    	
    	try {
    		System.out.println("Launching Application ttfParser...");
    		// This line prevents the application from having UI update concurrency issues
	        EventQueue.invokeLater(new Runnable() {               
	        
	            @Override
	            public void run() {
	                @SuppressWarnings("unused")
					Main ex = new Main(); // instantiate the main class
	            }
	        });
	        System.out.println("Application Launched");
    	}
    	// prints an exception and ends the program if there are errors while starting up the application
    	catch (Exception exception){
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			System.exit( -1 );
    	}

    }
}


