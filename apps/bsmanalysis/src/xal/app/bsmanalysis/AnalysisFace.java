/*
 * AnalysisFace.java
 *
 */
package xal.app.bsmanalysis;

import xal.extension.widgets.swing.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.messaging.*;
import xal.ca.*;
import xal.tools.data.*;
import java.text.NumberFormat;
import java.util.Iterator; 

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;

import xal.model.xml.*;
import xal.extension.widgets.plot.*;
import java.text.NumberFormat;
import xal.extension.widgets.swing.DecimalField;
import xal.tools.apputils.EdgeLayout;
import xal.tools.beam.*;
import java.text.DecimalFormat;
import xal.extension.solver.*;
//import xal.tools.formula.*;
import xal.extension.solver.hint.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.market.*;
import xal.extension.solver.solutionjudge.*;
import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.extension.widgets.apputils.SimpleProbeEditor;

import xal.tools.apputils.files.RecentFileTracker;

/**
 * Loads BSM files, contains package for performing analysis as subpanel. 
 * @author  cp3, + Emily G. 
 */
 
public class AnalysisFace extends JPanel{
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    AnalysisPanel analysispanel;
    private StoredResultsPanel resultspanel;
    
    EdgeLayout layout = new EdgeLayout();
    
    GenDocument doc;
     
    JPanel mainPanel;	
    JButton openbutton;
    JButton clearbutton; 
    RecentFileTracker ft;
    JFileChooser fc;
    ArrayList<String> filesopened;
    //ArrayList attributes;
    
    JScrollPane datascrollpane;
    public DataTableModel datatablemodel;
    public JTable datatable;
    
    
    //Constructor
    public AnalysisFace(GenDocument aDocument) {   
	doc=aDocument;
	
	setLayout(layout);
	init();
	setAction();		
	addcomponents();
    }
    
    
    //Initialization of components on AnalysisFace
     public void init(){
	mainPanel = new JPanel();
	mainPanel.setPreferredSize(new Dimension(1150,850));
	openbutton = new JButton("Add New BSM File");
	clearbutton = new JButton("Clear Table"); 
	
	fc = new JFileChooser();
	ft = new RecentFileTracker(1, this.getClass(), "bsmfile");
	ft.applyRecentFolder(fc);
	
	filesopened = new ArrayList<String>();
	makeDataTable();
	
	analysispanel = new AnalysisPanel(doc, datatablemodel);
	resultspanel = new StoredResultsPanel(doc);
	
    }
    
    
    //Set the action listeners	
     public void setAction(){
	openbutton.addActionListener(new ActionListener(){
	    public void actionPerformed(ActionEvent e) {
		int returnValue = fc.showOpenDialog(AnalysisFace.this);
		if(returnValue == JFileChooser.APPROVE_OPTION){
		    File file = fc.getSelectedFile();
		    ft.cacheURL(file);
		    
		    HashMap<String, Object> newdata = new HashMap<String, Object>(parseFile(file));
		    
		    String name = new String(file.toString());
		    String[] tokens;
		    tokens=name.split("/");
		    String filename = new String(tokens[tokens.length-1]);
		    
		    if(!filesopened.contains(filename)){
			System.out.println("Opening file: " + filename);
			filesopened.add(filename);
			
			toTable(filename, newdata);
			datatablemodel.fireTableDataChanged();
			doc.masterDataMap.put(filename, newdata); //fills the master map 	   
		    }
		    else{
			System.out.println("File " + filename + " has already been opened.");
		    }
		}
		else{
		    System.out.println("Open command cancelled by user.");
		}
	    }
	});
	
	clearbutton.addActionListener(new ActionListener(){	
	    public void actionPerformed(ActionEvent e) {
		clearTable(); //clears all information in the table and masterDataMap
		filesopened.clear();
	    }
	});
     }
    
     
    //Add all components to the layout and panels 
    public void addcomponents(){
	
	layout.setConstraints(mainPanel, 0, 0, 0, 0, EdgeLayout.ALL_SIDES, EdgeLayout.GROW_BOTH);
	this.add(mainPanel);
	
	EdgeLayout newlayout = new EdgeLayout();
	mainPanel.setLayout(newlayout);
	GridLayout initgrid = new GridLayout(6, 4);
	
	newlayout.setConstraints(openbutton, 20, 20, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(openbutton);
	
	newlayout.setConstraints(clearbutton, 50, 20, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(clearbutton);
	
	newlayout.setConstraints(datascrollpane, 5, 230, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(datascrollpane);
	
	newlayout.setConstraints(analysispanel, 100, 10, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(analysispanel);
	
	newlayout.setConstraints(analysispanel.summarypanel, 100, 525, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(analysispanel.summarypanel);
	
	newlayout.setConstraints(resultspanel, 255, 600, 0, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainPanel.add(resultspanel);
    }
 
    
    //Fills the table with the filename, phase, and stepsize for the given file.   	
    public void toTable (String filename, HashMap<String, Object> newdata){
	    ArrayList<Object> tabledata = new ArrayList<Object>();
	    tabledata.add(filename);
	    tabledata.add(newdata.get("phase"));
	    tabledata.add(newdata.get("stepsize"));
	    tabledata.add(new Boolean(false));
	    datatablemodel.addTableData(new ArrayList<Object>(tabledata));
	    datatablemodel.fireTableDataChanged();
    }
    
    
    //Creates the table 
    public void makeDataTable(){
	String[] colnames = {"File Name", "Phi Ref", "Phi Step", "Analyze"};
	
	datatablemodel = new DataTableModel(colnames, 0);
	
	datatable = new JTable(datatablemodel);
	datatable.getColumnModel().getColumn(0).setMinWidth(175);
	datatable.getColumnModel().getColumn(1).setMinWidth(120);
	datatable.getColumnModel().getColumn(2).setMinWidth(125);
	datatable.getColumnModel().getColumn(3).setMinWidth(125);
	
	datatable.setPreferredScrollableViewportSize(datatable.getPreferredSize());
	datatable.setRowSelectionAllowed(false);
	datatable.setColumnSelectionAllowed(false);
	datatable.setCellSelectionEnabled(false);

	datascrollpane = new JScrollPane(datatable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	datascrollpane.getVerticalScrollBar().setValue(0);
	datascrollpane.getHorizontalScrollBar().setValue(0);
	datascrollpane.setPreferredSize(new Dimension(570, 100));	

	ButtonRenderer buttonRenderer = new ButtonRenderer();
	datatable.getColumnModel().getColumn(3).setCellRenderer(buttonRenderer);
	datatable.getColumnModel().getColumn(3).setCellEditor(buttonRenderer);
    }
    
    
    //Renderer for doing the last column of the results table
    class ButtonRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor,ActionListener{
        /** serialization ID */
        private static final long serialVersionUID = 1L;
        
	public JButton theButton;
	protected static final String EDIT = "edit";
	
	
	public ButtonRenderer(){
	    theButton = new JButton("Analyze");
	    theButton.setActionCommand(EDIT);
	    theButton.addActionListener(this);
	}
	
	
	public Component getTableCellEditorComponent(JTable table, 
	Object agent, boolean isSelected, int row, int column){

	    //Gets the filename of the row selected by the analyze button
	    String filename = (String)datatablemodel.getValueAt(row,0);
	    
	    if(column == 3){
		    analysispanel.resetCurrentData(filename);
	    }
	    analysispanel.plotDataImage(); //plots the color image
	    return theButton;
	    
	}
	
	
	public void actionPerformed(ActionEvent e) {
	    if (EDIT.equals(e.getActionCommand())) {
		    fireEditingStopped(); //Make the renderer reappear.
	    }
	}
	
	
	public Object getCellEditorValue(){
	    return "";
	}
	
	
	public boolean isCellEditable(){
	    return true;
	}
	
	
	public Component getTableCellRendererComponent(JTable table,
					       Object value, 
					       boolean isSelected, 
					       boolean hasFocus,
					       int row, 
					       int column){
	    return theButton;
	}
	
    } 
	
    
    public HashMap<String, Object> parseFile(File newfile){
	
	ParseBSMFile parsefile = new ParseBSMFile();
	HashMap<String, Object> newdata = new HashMap<String, Object>();
	
	try{
	    newdata = parsefile.parseFile(newfile);
	}
	catch(IOException e){
	    System.out.println("Warning, returning empty data set.");
	}
	
	return newdata;
    }
    
    
    //Clears all information in the table, masterDataMap, and analysispanel
    public void clearTable(){		
	    if(doc.masterDataMap.size() == 0){
		    System.out.println("No files to remove.");
	    }
	    else{
		 doc.masterDataMap.clear();
		 datatablemodel.clearAllData();
		 analysispanel.setDefaults();
		 analysispanel.datapanel.removeAllGraphData();
		 analysispanel.datapanel.setColorSurfaceData(null);
	    }
    }
       
}

