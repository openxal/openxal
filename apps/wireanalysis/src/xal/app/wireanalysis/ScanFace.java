/*************************************************************
 //
 // class ScanFace:
 // This class is responsible for the Graphic User Interface
 // components and action listeners.
 //
 /*************************************************************/

package xal.app.wireanalysis;

import xal.ca.ConnectionException;
import xal.ca.PutException;
import xal.tools.apputils.EdgeLayout;
import xal.tools.apputils.NonConsecutiveSeqSelector;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.WireScanner;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class ScanFace extends JPanel{
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    public JPanel mainPanel;
    public JTable wiretable;
    
    GenDocument doc;
    
	private JTable datatable;
	private final Accelerator accl;
	
	private NewWireDataPanel newwiredatapanel;
    
	JScrollPane wirescrollpane;
    DataTableModel wiretablemodel;
	JButton sequencebutton;
	JButton scanbutton;
	JButton abortbutton;
	JButton homebutton;
	ArrayList<AcceleratorNode> openwires;
	
	//Member function Constructor
	public ScanFace(GenDocument aDocument){
        
		doc=aDocument;
		accl = doc.accl;
		
		makeComponents(); //Creation of all GUI components
		addComponents();  //Add all components to the layout and panels
        
		setAction();      //Set the action listeners
    }
    
    public void addComponents(){
		EdgeLayout layout = new EdgeLayout();
		mainPanel.setLayout(layout);
        
		layout.add(sequencebutton, mainPanel, 5, 5, EdgeLayout.LEFT);
		layout.add(scanbutton, mainPanel, 5, 40, EdgeLayout.LEFT);
		layout.add(abortbutton, mainPanel, 5, 70, EdgeLayout.LEFT);
		layout.add(homebutton, mainPanel, 5, 100, EdgeLayout.LEFT);
		layout.add(wirescrollpane, mainPanel,150, 10, EdgeLayout.LEFT);
		layout.add(newwiredatapanel, mainPanel, 0, 190, EdgeLayout.LEFT);
		
		this.add(mainPanel);
    }
    
    public void makeComponents(){
		mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(1140, 745));
		newwiredatapanel = new NewWireDataPanel(doc);
		
		sequencebutton = new JButton("Select Sequence");
		scanbutton = new JButton("Start Scan");
		abortbutton = new JButton("Abort Scan");
		homebutton = new JButton("Send Home");
		
		makeWireTable();
		
		openwires = new ArrayList<AcceleratorNode>();
    }
    
    
    public void setAction(){
        sequencebutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                ArrayList<Object> tabledata = new ArrayList<Object>();
                NonConsecutiveSeqSelector selector = new NonConsecutiveSeqSelector();
                selector.selectSequence();
                ArrayList<Object> seqlistnames = selector.getSeqList();
                Iterator<Object> itr = seqlistnames.iterator();
                while(itr.hasNext()){
                    AcceleratorSeq seq = accl.getSequence( itr.next().toString() );
                    ArrayList<AcceleratorNode> wirelist = (ArrayList<AcceleratorNode>)seq.getNodesOfType("WS");
                    for(AcceleratorNode node : wirelist){
                        if (node instanceof WireScanner ) {
                            openwires.add(node);
                        }
                    }
                }
                wiretablemodel.clearAllData();

				for ( final AcceleratorNode wireNode : openwires ) {
                    tabledata.clear();
                    WireScanner wire = (WireScanner)wireNode;
                    tabledata.add(new String(wire.getId()));
                    tabledata.add(new Boolean(true));
                    tabledata.add(new String("null"));
                    wiretablemodel.addTableData(new ArrayList<Object>(tabledata));
                }
                wiretablemodel.fireTableDataChanged();
            }
        });
        
        scanbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                ArrayList<WireScanner> scanwirelist = new ArrayList<WireScanner>();
                for(int i=0; i<wiretablemodel.getRowCount(); i++){
                    if((Boolean)wiretablemodel.getValueAt(i,1)){
                        scanwirelist.add(new WireScanner((String)wiretablemodel.getValueAt(i,0)));
                        System.out.println("activating wire for " + scanwirelist.get(i));
                    }
                    startScans(scanwirelist);
                }
                
            }
        });
        
        abortbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                ArrayList<WireScanner> abortwirelist = new ArrayList<WireScanner>();
                for(int i=0; i<wiretablemodel.getRowCount(); i++){
                    if((Boolean)wiretablemodel.getValueAt(i,1)){
                        abortwirelist.add(new WireScanner((String)wiretablemodel.getValueAt(i,0)));
                        System.out.println("aborting wire for " + abortwirelist.get(i));
                    }
                    abortScans(abortwirelist);
                }
            }
        });
        
        homebutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                
            }
        });
        
	}
    
	private void startScans(ArrayList<WireScanner> scanwires){
		Iterator<WireScanner> itr = scanwires.iterator();
        while(itr.hasNext()){
            WireScanner ws = itr.next();
            try{
                //					ws.runCommand(ws.SCAN_COMMAND);
                ws.runCommand(WireScanner.CMD.XPRT_SCAN);
            }
            catch(ConnectionException ce){
                System.out.println("Can not connect to " + ws.getId());
            }
            catch(PutException ge){
                System.out.println("Can not put run command arguments for " + ws.getId());
                
            }
            catch (InterruptedException e) {
                System.out.println("Can not put run command arguments for " + ws.getId());
            }
            /*
             catch(InterruptedException ie) {
             System.out.println("InterruptedException" + ws.getId());
             }*/
            
        }
	}
	private void abortScans(ArrayList<WireScanner> scanwires){
		Iterator<WireScanner> itr = scanwires.iterator();
		while(itr.hasNext()){
			WireScanner ws = itr.next();
			try{
                //				ws.runCommand(ws.ABORT_COMMAND);
                ws.runCommand(WireScanner.CMD.ABORT);
			}
			catch(ConnectionException ce){
				System.out.println("Can not connect to " + ws.getId());
			}
			catch(PutException ge){
				System.out.println("Can not put run command arguments for " + ws.getId());
				
            }
			catch (InterruptedException e) {
                System.out.println("Can not put run command arguments for " + ws.getId());
                
			}
			/*
             catch(InterruptedException any) {
             System.out.println("Catching the InterruptedException exception");
             }*/
			
		}
	}
	
	
	public void makeWireTable(){
		String[] colnames = {"Wire Name", "Select", "Progress"};
		
		wiretablemodel = new DataTableModel(colnames, 0);
		
		wiretable = new JTable(wiretablemodel);
		wiretable.getColumnModel().getColumn(0).setMinWidth(120);
		wiretable.getColumnModel().getColumn(1).setMinWidth(30);
		wiretable.getColumnModel().getColumn(2).setMinWidth(100);
		
		wiretable.setPreferredScrollableViewportSize(wiretable.getPreferredSize());
		wiretable.setRowSelectionAllowed(false);
		wiretable.setColumnSelectionAllowed(false);
		wiretable.setCellSelectionEnabled(false);
		
		wirescrollpane = new JScrollPane(wiretable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		wirescrollpane.getVerticalScrollBar().setValue(0);
		wirescrollpane.getHorizontalScrollBar().setValue(0);
		wirescrollpane.setPreferredSize(new Dimension(305, 130));
	}
    
    
    
}








