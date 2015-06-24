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
        
        JButton saveButton = new JButton("Save As");
        
        JButton analyzeButton = new JButton("Analyze");
        
        //create a text field with a default file name
        JTextField fileLabel = new JTextField("defaultFile.txt");
        
        JTextField valueLabel = new JTextField("Value");
        
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
        saveButton.setToolTipText("Save Parsed Data to File");
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

                } else {
                	// If the user closes the file chooser before selecting a file, print the following line
                	System.out.println("File Selection Aborted by User\n");
                }
            }
            
        });
        
        /**
         * Upon clicking the save as button, we create a JFileChooser so that the user can easily select the file directory to save the file too. The name of the file is in the label
         */
        saveButton.addActionListener(new ActionListener() {

            @Override 
            public void actionPerformed(ActionEvent event) {
            	
            	// choice is an integer that corresponds to the action selected by the user
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
            
        });
        
        /** We add an action listener to the run button, which signals the method actionPerformed when clicked */
        runButton.addActionListener(new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent event) {
            	//Parser parser = new Parser();
        		try {
					parser.parse(fileSelector.getSelectedFile());
					
				} catch (ParseException | ResourceNotFoundException
						| MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		System.out.println("File Parsed");
        		ArrayList<String> gapList = parser.getGapList();
        		for (String str : gapList) {
        			gapChooser.addItem(str);
        		}
            }
        });
        /** This action listener is called when the "Analyze" button is clicked*/
        analyzeButton.addActionListener(new ActionListener() {
            @Override 
            public void actionPerformed(ActionEvent event) {
            	String data = parser.getValue((String)gapChooser.getSelectedItem(), valueLabel.getText());
            	resultText.setText(data);
            }
        });
        /* 
         * The following four lines of code format the actual GUI container
         * setTitle sets the title of the GUI that is displayed on the top bar
         * setSize sets the size of the box to 500 pixels wide and 100 pixels high
         * setLocationRelativeTo(null) places the GUI in the center of the screen
         * setDefaultCloseOperation sets the default method for exiting the application, which in this case is clicking X.
         */
        setTitle("TTF Parser");
        setSize(750, 150);
        setResizable(false);
        setLocationRelativeTo(null);                              // This line centers the GUI on the screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);                  // Exits application upon clicking the X button on the GUI
        
        // This line calls the createLayout method, which formats how items are displayed on the GUI
        createLayout(fileSelectorButton, runButton, fileLabel, gapLabel, gapChooser, valueLabel, analyzeButton, resultLabel, resultText, saveButton);

    }
    
    /**
     * Creates the layout.
     *
     * @param arg the arg
     */
    // This method defines how the GUI component will be formatted. In this case we use a GroupLayout setup, as it is robust
    private void createLayout(JComponent... arg) {

        Container pane = getContentPane();
        GroupLayout gl = new GroupLayout(pane);
        pane.setLayout(gl);
        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(
        		gl.createSequentialGroup()
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                		.addComponent(arg[0])
                		.addComponent(arg[3])
                		.addComponent(arg[7])
                		)
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                		.addComponent(arg[2],GroupLayout.PREFERRED_SIZE, 400,GroupLayout.PREFERRED_SIZE)
                		.addComponent(arg[4])
                		.addComponent(arg[8])
                )
                .addGroup(gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                		.addComponent(arg[1])
                		.addComponent(arg[5])
                		.addComponent(arg[6])
                )
                
                .addComponent(arg[9])
        );

        gl.setVerticalGroup(
        		gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addGroup(gl.createSequentialGroup()
                		.addComponent(arg[0])
                		.addComponent(arg[3])
                		.addComponent(arg[7])
                		)
                .addGroup(gl.createSequentialGroup()
                		.addComponent(arg[2], GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                		.addComponent(arg[4])
                		.addComponent(arg[8], GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                		)
                .addGroup(gl.createSequentialGroup()
                		.addComponent(arg[1])
                		.addComponent(arg[5], GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                		.addComponent(arg[6])
                )
                .addGroup(gl.createSequentialGroup()
                		.addComponent(arg[9])
                		)
        );


    }
    
    /**
     * Help file.
     *
     * @param args the args
     */
    public static void helpFile(String[] args) {
    	
    }
    
    /**
     * The main method.
     *
     * @param args the arguments
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
    	catch (Exception exception){
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
			System.exit( -1 );
    	}

    }
}


