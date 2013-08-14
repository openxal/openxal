/*
 * Created on Mar 2, 2005
 *
 * Copyright (c) 2001-2005 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 * 
 */
package xal.app.ringmeasurement;

import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.tools.apputils.EdgeLayout;
import xal.tools.apputils.SimpleChartPopupMenu;
import xal.tools.plot.BasicGraphData;
import xal.tools.plot.FunctionGraphsJPanel;
import xal.smf.impl.BPM;
import xal.smf.impl.SCLCavity;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

/**
 * For dispersion display panel.
 * @author Paul Chu
 *
 */
public class DispersionPane extends JPanel implements ActionListener {
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    

	private JButton jButton = null;
	private JPanel jPanel = null;
	private JTable jTable = null;
	BpmTableModel bpmTableModel;
	private JScrollPane jScrollPane = null;
    EdgeLayout edgeLayout = new EdgeLayout();
	
    protected FunctionGraphsJPanel dispPlot;
    private BasicGraphData bpmXData, bpmYData;
    
	static RingDocument myDoc;
	static List<BPM> allBPMs;
	BPM[] bpms;

	/**
	 * This method initializes 
	 * 
	 */
	public DispersionPane(List<BPM> theBpms, RingDocument doc) {
		super();
		allBPMs = theBpms;
    	bpms = new BPM[allBPMs.size()];
    	for (int i=0; i<bpms.length; i++) {
    		bpms[i] =  allBPMs.get(i);
    	}
    	
		myDoc = doc;
		setLayout(edgeLayout);
		initialize();
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		dispPlot = new FunctionGraphsJPanel();
		dispPlot.setName("Dispersion in the Ring");
		this.setSize(700, 600);
        dispPlot.setAxisNames("s(m)", "D(m)");
        dispPlot.addMouseListener( new SimpleChartPopupMenu(dispPlot) );

		edgeLayout.setConstraints(getJButton(), 15, 30, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
        this.add(getJButton());
		edgeLayout.setConstraints(getJScrollPane(), 65, 30, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
        this.add(getJScrollPane());
		edgeLayout.setConstraints(getJPanel(), 55, 300, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
        this.add(getJPanel());
			
	}
	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton("Get Dispersion");
		}
		jButton.setActionCommand("get_dispersion");
		jButton.addActionListener(this);
		return jButton;
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
		}
		jPanel.setPreferredSize(new Dimension(400, 300));
		jPanel.setLayout(new BorderLayout());
		jPanel.add(dispPlot, BorderLayout.CENTER);
		return jPanel;
	}
	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */    
	private JTable getJTable() {
		bpmTableModel = new BpmTableModel();
		for (int i = 0; i < allBPMs.size(); i++) {
			bpmTableModel.addRowName(bpms[i].getId(), i);
		}
		
		if (jTable == null) {
			jTable = new JTable(bpmTableModel);
		}
		jTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		return jTable;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
		}
		jScrollPane.setViewportView(getJTable());
		jScrollPane.setPreferredSize(new Dimension(240, 400));
		jScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		return jScrollPane;
	}
	
	public void actionPerformed(ActionEvent ev) {
        if (ev.getActionCommand().equals("get_dispersion")) {
        	
            double[] xDisp = new double[allBPMs.size()];
            double[] yDisp = new double[allBPMs.size()];
            
        	// we take BPM measurements for 5 different energy settings
        	double[][] bpmX = new double[5][allBPMs.size()];
        	double[][] bpmY = new double[5][allBPMs.size()];

			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < allBPMs.size(); j++) {
					try {
						bpmX[i][j] = bpms[j].getXAvg();
						bpmY[i][j] = bpms[j].getYAvg();
					} catch (ConnectionException e) {
						System.out.println(e);
					} catch (GetException e) {
						System.out.println(e);
					}
				}
				// wait for n seconds for data collection
				
				// change the last SCL cavity amplitude (i.e. change energy)
				SCLCavity sclCav = (SCLCavity) myDoc.getAccelerator()
						.getSequence("SCLHigh").getNodeWithId("SCL_RF:Cav23d");
				try {
					double cavAmp = sclCav.getCavAmpAvg();

					sclCav.setCavAmp(cavAmp);
				} catch (ConnectionException e) {
					System.out.println(e);
				} catch (GetException e) {
					System.out.println(e);
				} catch (PutException e) {
					System.out.println(e);
				}
				// wait for n seconds before taking the next BPM measurement

			}
			
        	// calculate the dispersion from BPM measurements
        	DispMeasurement xDispMeasurement = new DispMeasurement(allBPMs);
        	// set BPM and energy data
        	xDispMeasurement.setBPMDataArray(bpmX);
        	xDisp = xDispMeasurement.getDispersions();
        	
        	DispMeasurement yDispMeasurement = new DispMeasurement(allBPMs);
        	// set BPM and energy data
        	yDispMeasurement.setBPMDataArray(bpmY);
        	yDisp = yDispMeasurement.getDispersions();
        	
        	double[] pos = new double[bpms.length];
        	for (int i=0; i<bpms.length; i++) {
        		pos[i] = bpms[i].getPosition();
        	}
        	bpmXData.addPoint(pos, xDisp);
        	bpmYData.addPoint(pos, yDisp);
        	
        	// update the result table and plot
        	for (int i=0; i<bpms.length; i++) {
        		bpmTableModel.setValueAt(new Double(xDisp[i]), i, 1);
        	}
        	dispPlot.addGraphData(bpmXData);
        	dispPlot.addGraphData(bpmYData);
        }
	}

	class BpmTableModel extends AbstractTableModel {
        
        /** ID for serializable version */
        private static final long serialVersionUID = 1L;
        
		final String[] columnNames = { "BPM", "D(m)" };

		final Object[][] data = new Object[allBPMs.size()][columnNames.length];

		/** Container for row labels */
		private final ArrayList<String> rowNames = new ArrayList<String>(allBPMs.size());

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		@Override
        public String getColumnName(int col) {
			return columnNames[col];
		}

		public String getRowName(int row) {
			return  rowNames.get(row);
		}

		@Override
        public boolean isCellEditable(int row, int col) {
				return false;
		}

		/** method to add a row name */
		public void addRowName(String name, int row) {
			rowNames.add(row, name);
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return rowNames.get(rowIndex);
			} else {
				return data[rowIndex][columnIndex];
			}
		}

		@Override
        public void setValueAt(Object value, int row, int col) {
			if (col > 0) {
				data[row][col] = value;
				
				fireTableCellUpdated(row, col);
				return;
			}
		}
	}
}  //  @jve:decl-index=0:visual-constraint="32,15"
