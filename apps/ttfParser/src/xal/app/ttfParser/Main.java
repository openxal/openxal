/*
 * Main.java
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
import java.io.File;


/**
 * This is the main class for the transit time factor parsing application  
 *
 * @version   0.1  15 June 2015
 * @author  James Ghawaly Jr.
 */

public class Main extends JFrame {

	public Main() {

        initUI();
    }
	// This method initializes the Graphical user Interface (GUI)
    private void initUI() {
    	// Create a button with the title Browse
        JButton fileSelectorButton = new JButton("Browse");          
        // When hovering cursor over the button, display button's purpose
        fileSelectorButton.setToolTipText("Select File from Directory Browser");  
        
        // This is the file chooser menu
        final JFileChooser fileSelector = new JFileChooser();
        
        // We add an action listener to the button, which is signaled when the button is clicked
        fileSelectorButton.addActionListener(new ActionListener() {
            @Override // This method is called when the button is clicked, and executes the required operations
            public void actionPerformed(ActionEvent event) {
            	
            	// choice is an integer that corresponds to the action selected by the user
                int choice = fileSelector.showOpenDialog(Main.this);
                
                // if the choice is valid
                if (choice == JFileChooser.APPROVE_OPTION) {
                	// grab the selected file using java's io File class
                	File file = fileSelector.getSelectedFile();
                	// print the name of the file we are opening
                	System.out.println("Opening File: " + file.getName() + "\n");
                } else {
                	// If the user did not select a file, print the following line
                	System.out.println("File Selection Aborted by User\n");
                }
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
        setSize(500, 100);
        setLocationRelativeTo(null);                              // This line centers the GUI on the screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);                  // Exits application upon clicking the X button on the GUI
        
        // This line calls the createLayout method, which formats how items are displayed on the GUI
        createLayout(fileSelectorButton);
    }
    
    // This method defines how the GUI component will be formatted. In this case we use a GroupLayout setup, as it is robust
    private void createLayout(JComponent... arg) {

        Container pane = getContentPane();
        GroupLayout gl = new GroupLayout(pane);
        pane.setLayout(gl);

        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addComponent(arg[0])
        );

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addComponent(arg[0])
        );
    }
    
    public static void main(String[] args) {
    	
    	try {
    		System.out.println("Launching Application ttfParser...");
    		Parser parseer = new Parser();
    		//System.out.println(parseer.main("sns_pyobit_linac_structure_untested.xml"));
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


