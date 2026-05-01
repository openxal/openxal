/*
 * BeamPositionFace.java
 *
 */
package xal.app.rtbtwizard;

import xal.extension.widgets.swing.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.messaging.*;
import xal.ca.*;
import xal.tools.data.*;
import java.text.NumberFormat;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;

import xal.model.xml.*;
//import xal.tools.optimizer.*;
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
 * Performs profile analysis
 * @author  cp3
 */

public class ProfileFace extends JPanel{
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    
    private AnalysisPanel analysispanel;
    private StoredResultsPanel resultspanel;
    
    EdgeLayout layout = new EdgeLayout();
    
    GenDocument doc;
    
    JPanel mainPanel;
    JButton openbutton;
    JButton clearbutton;
    RecentFileTracker ft;
    JFileChooser fc;
    ArrayList<String> filesopened;
    ArrayList<DataAttribute> attributes;
    
    DataTable masterdatabase;
    HashMap<String, Integer> pvloggermap;
    Object[][] tabledata = new Object[8][3];
    
    JScrollPane datascrollpane;
    public DataTableModel datatablemodel;
    public JTable datatable;
    
    public ProfileFace(GenDocument aDocument, JPanel mainpanel) {
        doc=aDocument;
        setPreferredSize(new Dimension(960,800));
        setLayout(layout);
        init();
        setAction();
        addcomponents();
    }
    public ProfileFace(GenDocument aDocument) {
        doc=aDocument;
    }
    
    
    public void addcomponents(){
        
        layout.setConstraints(mainPanel, 0, 0, 0, 0, EdgeLayout.ALL_SIDES, EdgeLayout.GROW_BOTH);
        this.add(mainPanel);
        
        EdgeLayout newlayout = new EdgeLayout();
        mainPanel.setLayout(newlayout);
        GridLayout initgrid = new GridLayout(6, 4);
        
        newlayout.setConstraints(openbutton, 5, 20, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(openbutton);
        newlayout.setConstraints(clearbutton, 45, 20, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(clearbutton);
        newlayout.setConstraints(datascrollpane, 5, 230, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(datascrollpane);
        newlayout.setConstraints(analysispanel, 120, 2, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(analysispanel);
        newlayout.setConstraints(resultspanel, 120, 500, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(resultspanel);
        
    }
    
    public void init(){
        
        mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(950,600));
        openbutton = new JButton("Add New Wirescan File");
        clearbutton = new JButton("Clear Loaded Data");
        
        fc = new JFileChooser();
        ft = new RecentFileTracker(1, this.getClass(), "wsfile");
        ft.applyRecentFolder(fc);
        
        filesopened = new ArrayList<String>();
        makeDataTable();
        
        pvloggermap = new HashMap<String, Integer>();
        attributes = new ArrayList<DataAttribute>();
        attributes.add(new DataAttribute("file", new String("").getClass(), true) );
        attributes.add(new DataAttribute("wire", new String("").getClass(), true) );
        attributes.add(new DataAttribute("data", new ArrayList<DataAttribute>().getClass(), false) );
        masterdatabase = new DataTable("DataTable", attributes);
        
        analysispanel = new AnalysisPanel(doc, datatablemodel);
        resultspanel = new StoredResultsPanel(doc);
        
    }
    
    
    
    public void setAction(){
        
        openbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int returnValue = fc.showOpenDialog(ProfileFace.this);
                if(returnValue == JFileChooser.APPROVE_OPTION){
                    File file = fc.getSelectedFile();
                    ft.cacheURL(file);
                    
                    ArrayList<Object> newdata = new ArrayList<Object>(parseFile(file));
                    
                    String name = new String(file.toString());
                    String[] tokens;
                    tokens=name.split("/");
                    String filename = new String(tokens[tokens.length-1]);
                    Integer pvloggerid = (Integer)newdata.get(newdata.size()-1);
                    System.out.println("file and pv are " + filename + "  " + pvloggerid);
                    pvloggermap.put(filename, pvloggerid);
                    //doc.masterpvloggermap = pvloggermap;
                    
                    newdata.remove(newdata.size()-1);
                    
                    if(!filesopened.contains(filename)){
                        System.out.println("Opening file: " + filename);
                        filesopened.add(filename);
                        toMasterDatabase(filename, newdata);
                        refreshTable();
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
                clearTable();
                filesopened.clear();
            }
        });
        
    }
    
    
    @SuppressWarnings("unchecked") //map.get returns object and cannot be cast
    public void toMasterDatabase(String filename, ArrayList<Object> data){
        Iterator<Object> itr = data.iterator();
        while(itr.hasNext()){
            HashMap<String, Object> map = new HashMap<String, Object>();
            //HashMap datamap = new HashMap();
            ArrayList<ArrayList<Double>> rawdata = new ArrayList<ArrayList<Double>>();
            ArrayList<ArrayList<Double>> paramdata = new ArrayList<ArrayList<Double>>();
            
            map = (HashMap<String, Object>)itr.next();
            String name = new String((String)map.get("name"));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("sdata")));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("xdata")));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("ydata")));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("zdata")));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("sxdata")));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("sydata")));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("szdata")));
            paramdata.addAll(new ArrayList<ArrayList<Double>>((ArrayList)map.get("fitparams")));
            
            //datamap.put("rms", paramdata);
            
            GenericRecord record = new GenericRecord(masterdatabase);
            record.setValueForKey(filename, "file");
            record.setValueForKey(name, "wire");
            record.setValueForKey(rawdata, "data");
            record.setValueForKey(paramdata, "rms");
            
            masterdatabase.add(record);
        }
        //Update in the GenDocument file
        doc.wiredatabase = masterdatabase;
    }
    
    private void refreshTable(){
        datatablemodel.clearAllData();
        
        ArrayList<Object> tabledata = new ArrayList<Object>();
        
        if(masterdatabase.records().size() == 0){
            System.out.println("No data available to load!");
        }
        else{
            Collection<GenericRecord> records = masterdatabase.records();
            Iterator<GenericRecord> itr = records.iterator();
            while(itr.hasNext()){
                tabledata.clear();
                GenericRecord record = itr.next();
                String filename=(String)record.valueForKey("file");
                String wire = (String)record.valueForKey("wire");
                
                tabledata.add(new String(filename));
                tabledata.add(new String(wire));
                tabledata.add(new Boolean(false));
                tabledata.add(new Boolean(false));
                datatablemodel.addTableData(new ArrayList<Object>(tabledata));
            }
            datatablemodel.fireTableDataChanged();
        }
    }
    
    
    public DataTableModel getDataTableModel(){
        System.out.println("datatablemodel " + datatablemodel);
        return datatablemodel;
    }
    
    /*
     public void clearTable(){
     if(datatable.records().size() == 0){
     System.out.println("No files to remove!");
     }
     else{
     Collection records = datatable.records();
     Iterator itr = records.iterator();
     while(itr.hasNext()){
     GenericRecord record = (GenericRecord)itr.next();
     datatable.remove(record);
     }
     //doc.datatable = datatable;
     }
     //datatablemodel.clearAllData();
     }*/
    
    public void makeDataTable(){
        String[] colnames = {"File Name", "Wire", "Analyze H", "Analyze V"};
        
        datatablemodel = new DataTableModel(colnames, 0);
        
        datatable = new JTable(datatablemodel);
		datatable.getColumnModel().getColumn(0).setMinWidth(285);
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
		datascrollpane.setPreferredSize(new Dimension(680, 100));

        ButtonRenderer xbuttonRenderer = new ButtonRenderer();
        datatable.getColumnModel().getColumn(2).setCellRenderer(xbuttonRenderer);
        datatable.getColumnModel().getColumn(2).setCellEditor(xbuttonRenderer);
        ButtonRenderer ybuttonRenderer = new ButtonRenderer();
        datatable.getColumnModel().getColumn(3).setCellRenderer(ybuttonRenderer);
        datatable.getColumnModel().getColumn(3).setCellEditor(ybuttonRenderer);
        
    }
    
    
    //Renderer for doing the last column of the results table
    class ButtonRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor,ActionListener{
        
        /** ID for serializable version */
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
            
            String filename = (String)datatablemodel.getValueAt(row,0);
            String wire = (String)datatablemodel.getValueAt(row,1);
            if(column == 2){
                analysispanel.resetCurrentData(filename, wire, new String("H"));
            }
            if(column == 3){
                analysispanel.resetCurrentData(filename, wire, new String("V"));
            }
            
            analysispanel.setNewDataFlag(true);
            analysispanel.plotData();
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
	
    
    public ArrayList<Object> parseFile(File newfile){
        ParseWireFile parsefile = new ParseWireFile();
        ArrayList<Object> newdata = new ArrayList<Object>();
        try{
            newdata = parsefile.parseFile(newfile);
        }
        catch(IOException e){
            System.out.println("Warning, returning empty data set.");
        }
        return newdata;
    }
    
    
    public void clearTable(){
        if(masterdatabase.records().size() == 0){
            System.out.println("No files to remove!");
        }
        else{
            Collection<GenericRecord> records = masterdatabase.records();
            Iterator<GenericRecord> itr = records.iterator();
            while(itr.hasNext()){
                GenericRecord record = itr.next();
                masterdatabase.remove(record);
            }
            doc.wiredatabase = masterdatabase;
        }
        datatablemodel.clearAllData();
        
    } 
    
}

