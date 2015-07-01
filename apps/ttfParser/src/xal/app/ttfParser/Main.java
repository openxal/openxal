/*
 * Main.java - Main file for the ttfParser application, sets up the GUI and its functions
 * @author James Ghawaly Jr.
 * Created on Mon June 15 13:23:35 EDT 2015
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ttfParser;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This is the main class for the transit time factor parsing application  .
 *
 * @author  James Ghawaly Jr.
 * @version   0.1  15 June 2015
 */
public class Main extends JFrame {

	/**
	 * Instantiates a new main.
	 */
	public Main() {

        initUI();
    }
	
	/**
	 * Initiates the user interface
	 */
	// This method initializes the Graphical user Interface (GUI)
    private void initUI() {

    	Parser parser = new Parser();
    	
    	// Create a button with the title Browse
        JButton fileSelectorButton = new JButton("Browse");  

        JButton runButton = new JButton("Run");
        
        JButton analyzeButton = new JButton("Analyze");
        
        runButton.setEnabled(false);
        analyzeButton.setEnabled(false);
        
        //create a text field with a default file name
        JTextField fileLabel = new JTextField("name of file to save to.xdxf");
        
        JTextField valueLabel = new JTextField("Value Tag");
        
        //create a text area with instructions for how to use the program
        JTextArea infoBox = new JTextArea("- To Parse a file, select the 'Browse' button and choose your file, then select 'Run.' An option to save the new file will be given.\n \n- To retrieve a particular value from a specific gap; after running the parser, type the tag of the value that you want to retrieve into the 'Value Tag' text area, choose the gap from the drop down menu and select 'Analyze.'\n \n- Possible choices are: ttf, stf, ttfp, stfp.\n \n- See the README for more information.");
        infoBox.setEditable(false);
        infoBox.setWrapStyleWord(true);
        infoBox.setLineWrap(true);
        
        //create a label for the results of a value point analysis
        JLabel resultLabel = new JLabel("Result: ");
        
        JTextField resultText = new JTextField("...");
        resultText.setEditable(false);
        
        JLabel gapLabel = new JLabel("Choose RF Gap: ");
        
        //create a drop down menu that eventually contains all of the gaps in the accelerator
        JComboBox gapChooser = new JComboBox();

     // When hovering cursor over the buttons, display the selected button's purpose
        fileSelectorButton.setToolTipText("Select File from Directory Browser");  
        runButton.setToolTipText("Run the Parser and Create New File");
        analyzeButton.setToolTipText("Retrieve the specified data from the specified gap");
        gapChooser.setToolTipText("Choose a Gap From the Drop-down Menu to Analyze");
        valueLabel.setToolTipText("Type the Tag of the Value You Want to Get From the Selected Gap; options: ttf, stf, ttfp, stfp");
        
        // This is the file chooser menu, it brings up a directory explorer
        final JFileChooser fileSelector = new JFileChooser();
        
        final JFileChooser saveSelector = new JFileChooser();
        saveSelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
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
                    analyzeButton.setEnabled(true);

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
            	try {
            	String data = parser.getValue((String)gapChooser.getSelectedItem(), valueLabel.getText());
            	resultText.setText(data);
            	JOptionPane.showMessageDialog(getContentPane(), data);
            	} catch (ArrayIndexOutOfBoundsException e) {
            		JOptionPane.showMessageDialog(getContentPane(), "The Value ID chosen does not exist, valid IDs are: ttf, stf, ttfp, stfp", e.getClass().getName() + " ERROR", JOptionPane.ERROR_MESSAGE);
            	}
            }
        });
        /* 
         * The following four lines of code format the actual GUI container
         * setTitle sets the title of the GUI that is displayed on the top bar
         * setSize sets the size of the box to 750 pixels wide and 160 pixels high
         * setLocationRelativeTo(null) places the GUI in the center of the screen
         * setDefaultCloseOperation sets the default method for exiting the application, which in this case is clicking X.
         */
        setTitle("TTF Parser");
        setSize(750, 255); //160
        setResizable(false);
        setLocationRelativeTo(null);                              // This line centers the GUI on the screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);                  // Exits application upon clicking the X button on the GUI
        
        // This line calls the createLayout method, which formats how items are displayed on the GUI
        createLayout(fileSelectorButton, runButton, fileLabel, gapLabel, gapChooser, valueLabel, analyzeButton, resultLabel, resultText, infoBox);

    }
    
    /**
     * This method defines how the GUI component will be formatted. In this case we use a GroupLayout setup, as it is robust
     *
     * @param arg the list of components in the GUI
     */
    private void createLayout(JComponent... arg) {

        Container pane = getContentPane();
        GroupLayout gl = new GroupLayout(pane);
        pane.setLayout(gl);
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
	                Main ex = new Main(); // instantiate the main class
	                ex.setVisible(true); // shows the GUI
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


