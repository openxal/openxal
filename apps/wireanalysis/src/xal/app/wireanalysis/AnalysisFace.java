/*************************************************************
 //
 // class DataFace:
 // This class is responsible for the Graphic User Interface
 // components and action listeners.
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
import java.util.Date;
import java.util.List;
import java.io.*;
import java.lang.*;

import xal.extension.widgets.swing.*;
import xal.tools.statistics.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.apputils.files.RecentFileTracker;
import xal.extension.widgets.plot.*;
import xal.tools.data.*;
import java.text.NumberFormat;
import xal.tools.messaging.*;
import xal.ca.*;
import xal.extension.wirescan.profile.*;

public class AnalysisFace extends JPanel{
    
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    public JPanel mainPanel;
    public JTable datatable;
    
    private DataTable masterdatatable;
    
    private AnalysisPanel analysispanel;
    private StoredResultsPanel storedresultspanel;
    private DataTable resultsdatatable;
    
    String currentdataname;
    //ArrayList currentdata;
    ArrayList<DataAttribute> attributes;
    double sdata[];
    double data[];
    
    GenDocument doc;
    EdgeLayout layout = new EdgeLayout();
    
    JScrollPane datascrollpane;
    DataTableModel datatablemodel;
    JButton loadbutton;
    JButton analyzebutton;
    ProfileData profiledata;
    //TODO: Add ProfileDataProcessor
    ProfileDataProcessor profiledataprocessor;
    ProfileDataStatistics profiledatastatistics;
    Date localtime;
    
    public AnalysisFace(){}
    //Member function Constructor
    public AnalysisFace(GenDocument aDocument){
		
        doc=aDocument;
        
        makeComponents(); //Creation of all GUI components
        setStyling();     //Set the color for the buttons
        
        setAction();      //Set the action listeners
        
        analysispanel = new AnalysisPanel(doc, datatablemodel);
        storedresultspanel = new StoredResultsPanel(doc);
        addComponents();  //Add all components to the layout and panels
        
        
    }
    
    public void addComponents(){
        
        EdgeLayout layout = new EdgeLayout();
        mainPanel.setLayout(layout);
        
        layout.add(datascrollpane, mainPanel, 180, 10, EdgeLayout.LEFT);
        layout.add(analyzebutton, mainPanel, 10, 50, EdgeLayout.LEFT);
        layout.add(loadbutton, mainPanel, 10, 20, EdgeLayout.LEFT);
		layout.add(analysispanel, mainPanel, 0, 110, EdgeLayout.LEFT);
		layout.add(storedresultspanel, mainPanel, 490, 110, EdgeLayout.LEFT);
        this.add(mainPanel);
        
    }
    
    public void makeComponents(){
		mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(980, 750));

        loadbutton = new JButton("Load Table");
        analyzebutton = new JButton("Get All RMS");
        //	analyzebutton.setEnabled(false);
        makeDataTable();
        
        //currentdata = new ArrayList();
        
        attributes = new ArrayList<DataAttribute>();
        attributes.add(new DataAttribute("file", new String("").getClass(), true) );
        attributes.add(new DataAttribute("wire", new String("").getClass(), true) );
        attributes.add(new DataAttribute("direction", new String("").getClass(), true) );
        attributes.add(new DataAttribute("data", new ArrayList<DataAttribute>().getClass(), false) );
        resultsdatatable = new DataTable("DataTable", attributes);
    }
    
    
    public void setAction(){
        
        loadbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                getMasterDataTable();
                refreshTable();
            }
        });
        
        analyzebutton.addActionListener(new ActionListener(){
            
            @SuppressWarnings ("unchecked") //Had to suppress because valueforkey returns object.
            public void actionPerformed(ActionEvent e) {
                for(int i=0;i<datatable.getRowCount();i++){
                    
                    String filename = (String)datatablemodel.getValueAt(i,0);
                    String wire = (String)datatablemodel.getValueAt(i,1);
                    ArrayList<ArrayList<Double>> wiredata = new ArrayList<ArrayList<Double>>();
                    DataTable masterdatatable = doc.masterdatatable;
                    Map<String, String> bindings = new HashMap<String, String>();
                    bindings.put("file", filename);
                    bindings.put("wire", wire);
                    GenericRecord record =  masterdatatable.record(bindings);
                    wiredata=(ArrayList<ArrayList<Double>>)record.valueForKey("data");
                    
                    ArrayList<Double> slist = wiredata.get(0);
                    ArrayList<Double> sxlist = wiredata.get(1);
                    ArrayList<Double> sylist = wiredata.get(2);
                    ArrayList<Double> szlist = wiredata.get(3);
                    ArrayList<Double> xlist = wiredata.get(4);
                    ArrayList<Double> ylist = wiredata.get(5);
                    ArrayList<Double> zlist = wiredata.get(6);
                    
                    localtime = new Date();
                    //Switching order of data input to make Chris program agree with my data order
                    // Fixed - CKA
                    if(slist.size() > 0){
                        
                        // Create a new profile data record and pack it with the raw data
                        //    CKA Note - There is no longer an initializing factory method for
                        //    for ProfileData.  I am expecting the data structure to change
                        //    so the safest thing to do is pack it manually at this point.
                        profiledata = ProfileData.create(wire, new Date(), slist.size());
                        profiledata.setActuatorPositions(slist);
                        profiledata.setAxisPositions(ProfileData.Angle.HOR, sxlist);
                        profiledata.setAxisPositions(ProfileData.Angle.VER, sylist);
                        profiledata.setAxisPositions(ProfileData.Angle.DIA, szlist);
                        
                        profiledata.setProjection(ProfileData.Angle.HOR, xlist);
                        profiledata.setProjection(ProfileData.Angle.VER, ylist);
                        profiledata.setProjection(ProfileData.Angle.DIA, zlist);
                        
                        
                        // Process the profile data
                        profiledataprocessor = new ProfileDataProcessor(profiledata);
                        profiledataprocessor.processData();
                        
                        ProfileData recPrc = profiledataprocessor.getProcessedData();
                        
                        // Retrieve the statistical information from the processed profile data
                        profiledatastatistics = new ProfileDataStatistics(recPrc);
                        
                        for (ProfileData.Angle view : ProfileData.Angle.values()) { // For each View
                            
                            // Retrieve the statistical attributes of processed data and write to stdout
                            double center = profiledatastatistics.getCenter(view);
                            double stdev  = profiledatastatistics.compStdDev(view);
                            double h      = profiledatastatistics.compAveAxisStepSize(view);
                            double x0     = profiledatastatistics.getAxisOffset(view);
                            
                            System.out.println("\nFor Wire : " + wire);
                            System.out.println(view.toString() + " center and rms (normalized)  : " + center + "   " + stdev);
                            center = x0 + h*center;
                            stdev  = h*stdev;
                            System.out.println(view.toString() + " center and rms (unnormalized): " + center + "   " + stdev);
                            
                            
                            
                            // Store the results
                            // First - We need to pack the processed data into ArrayList container for
                            //    arguments to method AnalysisFace.storeResult()
                            // This does not seem to be working, however?
                            
                            double[] sdata = recPrc.getAxisPositions(view);
                            double[] data  = recPrc.getProjection(view);
                            
                            storeResult(filename, wire, view.toString(), center, stdev, sdata, data);
                            
                            //		            ArrayList<Double> lstPos = new ArrayList<Double>();
                            //		            for (double dblPos : recPrc.getAxisPositions(view))
                            //		                lstPos.add(dblPos);
                            //
                            //		            ArrayList<Double> lstPrj = new ArrayList<Double>();
                            //		            for (double dblPrj : recPrc.getProjection(view))
                            //		                lstPrj.add(dblPrj);
                            //
                            //			    double[] sdata = new double[lstPos.size()];
                            //			    double[] data = new double[lstPos.size()];
                            //
                            //			    for(int k=0; k<sdata.length; k++){
                            //				sdata[k]=((Double)lstPos.get(k)).doubleValue()*h;
                            //				data[k]=((Double)lstPrj.get(k)).doubleValue();
                            //			    }
                            //
                            //		            storeResult(filename, wire, view.toString(), center, stdev, sdata, data);
                        }
                        
                        //		        center = profiledatastatistics.getCenter(ProfileData.Angle.VER);
                        //		        stdev = profiledatastatistics.compStdDev(ProfileData.Angle.VER);
                        //		        storeResult(filename, wire, new String("V"), center, stdev, sylist, ylist);
                        //		        System.out.println("Ver center and rms: " + center + "   " + stdev);
                    }
                    else{
                        System.out.println("\nNo data for wire: " + wire);
                    }
                }
            }
        });
    }
    
    
    public void getMasterDataTable(){
        masterdatatable = doc.masterdatatable;
    }
    
    
    private void refreshTable(){
        datatablemodel.clearAllData();
        
        ArrayList<Object> tabledata = new ArrayList<Object>();
        
        if(masterdatatable.records().size() == 0){
            System.out.println("No data available to load!");
        }
        else{
            Collection<GenericRecord> records = masterdatatable.records();
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
                tabledata.add(new Boolean(false));
                datatablemodel.addTableData(new ArrayList<Object>(tabledata));
            }
            datatablemodel.fireTableDataChanged();
        }
    }
    
    
    public void setStyling(){
		
    }
    
    
    public void makeDataTable(){
        String[] colnames = {"File Name", "Wire", "H Analyze", "V Analyze", "D Analyze"};
        
        datatablemodel = new DataTableModel(colnames, 0);
        
        datatable = new JTable(datatablemodel);
        datatable.getColumnModel().getColumn(0).setMinWidth(185);
        datatable.getColumnModel().getColumn(1).setMinWidth(115);
        datatable.getColumnModel().getColumn(2).setMinWidth(100);
        datatable.getColumnModel().getColumn(3).setMinWidth(100);
        datatable.getColumnModel().getColumn(4).setMinWidth(100);
        
        datatable.setPreferredScrollableViewportSize(datatable.getPreferredSize());
        datatable.setRowSelectionAllowed(false);
        datatable.setColumnSelectionAllowed(false);
        datatable.setCellSelectionEnabled(false);
        
        datascrollpane = new JScrollPane(datatable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        datascrollpane.getVerticalScrollBar().setValue(0);
        datascrollpane.getHorizontalScrollBar().setValue(0);
        datascrollpane.setPreferredSize(new Dimension(650, 100));
        
        ButtonRenderer xbuttonRenderer = new ButtonRenderer();
        datatable.getColumnModel().getColumn(2).setCellRenderer(xbuttonRenderer);
        datatable.getColumnModel().getColumn(2).setCellEditor(xbuttonRenderer);
        ButtonRenderer ybuttonRenderer = new ButtonRenderer();
        datatable.getColumnModel().getColumn(3).setCellRenderer(ybuttonRenderer);
        datatable.getColumnModel().getColumn(3).setCellEditor(ybuttonRenderer);
        ButtonRenderer zbuttonRenderer = new ButtonRenderer();
        datatable.getColumnModel().getColumn(4).setCellRenderer(zbuttonRenderer);
        datatable.getColumnModel().getColumn(4).setCellEditor(zbuttonRenderer);
        
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
            
            String filename = (String)datatablemodel.getValueAt(row,0);
            String wire = (String)datatablemodel.getValueAt(row,1);
            if(column == 2){
                analysispanel.resetCurrentData(filename, wire, new String("H"));
            }
            if(column == 3){
                analysispanel.resetCurrentData(filename, wire, new String("V"));
            }
            if(column == 4){
                analysispanel.resetCurrentData(filename, wire, new String("D"));
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
    
    
    public void storeResult(String filename, String wirename, String direction, double mean, double rms, double[] sdata, double[] data){
        
        GenericRecord record = new GenericRecord(resultsdatatable);
        ArrayList<double[]> results = new ArrayList<double[]>();
        double[] fit = new double[4];
        double[] errors = new double[4];
        fit[0] = rms;
        fit[1] = 0.0;
        fit[2] = mean;
        fit[3] = 0.0;
        errors[0] = 0.0;
        errors[1] = 0.0;
        errors[2] = 0.0;
        errors[3] = 0.0;
        
        results.add(fit);
        results.add(errors);
        results.add(sdata);
        results.add(data);
        
        Map<String, String> bindings = new HashMap<String, String>();
        bindings.put("file", filename);
        bindings.put("wire", wirename);
        bindings.put("direction", direction);
        
        if(resultsdatatable.record(bindings) != null){
            resultsdatatable.remove(resultsdatatable.record(bindings));
        }
	    
        record.setValueForKey(new String(filename), "file");
        record.setValueForKey(new String(wirename), "wire");
        record.setValueForKey(new String(direction), "direction");
        record.setValueForKey(new ArrayList<double[]>(results), "data");
        
        
        System.out.println("Added " + filename + "  " + wirename+ "  " + data);
        resultsdatatable.add(record);
        doc.resultsdatatable = resultsdatatable;
    }
    
}








