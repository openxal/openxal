/*************************************************************
 //
 // class NewWireDatapanel:
 // This class is responsible for the Graphic User Interface
 // components and action listeners for stored results.
 //
 /*************************************************************/

package xal.app.wireanalysis;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.JOptionPane;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer.*;
import java.net.URL;
import java.util.List;
import java.io.*;
import java.io.File.*;
import javax.swing.text.PlainDocument;
import java.lang.*;

import xal.extension.widgets.swing.*;
import xal.tools.statistics.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.apputils.files.RecentFileTracker;
import xal.extension.widgets.plot.*;
import xal.tools.data.*;
import xal.extension.fit.lsm.*;
import java.text.NumberFormat;
import xal.tools.messaging.*;
import xal.ca.*;


public class NewWireDataPanel extends JPanel{
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    
    public JPanel mainPanel;
	
    private DataTable resultsdatatable;
    
    JButton loadbutton;
	JButton savetofilebutton;
	JButton savetodatabasebutton;
	
	GenDocument doc;
    EdgeLayout layout = new EdgeLayout();
    FunctionGraphsJPanel hdatapanel;
	FunctionGraphsJPanel vdatapanel;
	FunctionGraphsJPanel ddatapanel;
	
    //Member function Constructor
    public NewWireDataPanel(GenDocument aDocument){
		
		doc=aDocument;
        
		makeComponents(); //Creation of all GUI components
		setStyling();     //Set the styling of components
		addComponents();  //Add all components to the layout and panels
        
		setAction();      //Set the action listeners
        
    }
    
    public void addComponents(){
		EdgeLayout layout = new EdgeLayout();
		mainPanel.setLayout(layout);
		layout.add(loadbutton, mainPanel, 5, 25, EdgeLayout.LEFT);
		layout.add(savetofilebutton, mainPanel, 5, 445, EdgeLayout.LEFT);
		layout.add(savetodatabasebutton, mainPanel, 5, 475, EdgeLayout.LEFT);
		layout.add(hdatapanel, mainPanel, 180, 25, EdgeLayout.LEFT);
		layout.add(vdatapanel, mainPanel, 500, 25, EdgeLayout.LEFT);
		layout.add(ddatapanel, mainPanel, 820, 25, EdgeLayout.LEFT);
		this.add(mainPanel);
        
    }
    
    public void makeComponents(){
		mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(1130, 510));
		mainPanel.setBorder(BorderFactory.createTitledBorder("View and Save Wire Data"));
		hdatapanel = new FunctionGraphsJPanel();
		hdatapanel.setPreferredSize(new Dimension(300, 210));
		hdatapanel.setGraphBackGroundColor(Color.WHITE);
		vdatapanel = new FunctionGraphsJPanel();
		vdatapanel.setPreferredSize(new Dimension(300, 210));
		vdatapanel.setGraphBackGroundColor(Color.WHITE);
		ddatapanel = new FunctionGraphsJPanel();
		ddatapanel.setPreferredSize(new Dimension(300, 210));
		ddatapanel.setGraphBackGroundColor(Color.WHITE);
		
		
		loadbutton = new JButton("Load Profiles");
		savetofilebutton = new JButton("Save to File");
		savetodatabasebutton = new JButton("Save to Database With Comment:");
		savetodatabasebutton.setEnabled(false);
        
    }
    
    
    public void setAction(){
		
		loadbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
                
			}
		});
    }
    
    
    
    public void plotData(){
        
		hdatapanel.removeAllGraphData();
        
	}
    
    
    
    public void setStyling(){
		
    }
    
    
}








