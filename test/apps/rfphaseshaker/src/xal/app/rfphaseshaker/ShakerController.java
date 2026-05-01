/*
 *  ShakerController.java
 *
 *  Created on Feb. 26 2009
 */
package xal.app.rfphaseshaker;

import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;
import java.io.*;
import java.util.*;

import xal.smf.AcceleratorSeq;
import xal.smf.impl.qualify.KindQualifier;
import xal.smf.impl.qualify.TypeQualifier;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.AcceleratorNode;
import xal.smf.impl.BPM;
import xal.smf.impl.RfCavity;

import xal.tools.text.ScientificNumberFormat;
import xal.extension.widgets.swing.DoubleInputTextField;

import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.plot.GraphDataOperations;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;

/**
 *  ShakerController is a controller to perform the shake measurements and simulations.
 *
 *@author     shishlo
 */

public class ShakerController{
	
	private Vector<AcceleratorSeq> accSeqV = null;
	
	//accelerator calculator 
	private AccCalculator accCalc = null;	

	private DevTreeNode rootBPMTreeNode = new DevTreeNode();
	private DevTreeNode rootRFTreeNode = new DevTreeNode();	

	//-----------The GUI elements-------------------
	private JPanel mainPanel = new JPanel();
	private DevTree bpmTree = null;		
	private DevTree rfTree = null;
	
	private TitledBorder treesBorder = null;
	private JLabel rfColumnLabel = new  JLabel(" RF Cavities ");
	private JLabel bpmColumnLabel = new  JLabel("  BPMs  ");	
		
	private JButton calculateShake_Button = new JButton("Run Model");	
	private JButton measureShake_Button = new JButton("Shake RF");
	private JButton stopMeasure_Button = new JButton("Stop Shaking");	
	
	public DoubleInputTextField phaseShiftTextFiled = new DoubleInputTextField(5);
	public DoubleInputTextField sleepTimeTextFiled = new DoubleInputTextField(5);
	public DoubleInputTextField numbAvgTextFiled = new DoubleInputTextField(3);

	private JLabel phaseShifLabel = new  JLabel("  Shake Phase [deg]=");
	private JLabel sleepTimeLabel = new  JLabel("  Sleep Time [sec]=");
	private JLabel numbAvgLabel = new  JLabel("  N averaging =");
	
	//graph panel
	public FunctionGraphsJPanel fGraphPanel = new FunctionGraphsJPanel();
	private JLabel posLabel = new  JLabel("Position [m] =");
	private JLabel slopeLabel = new  JLabel("   Slope =");
	private JButton saveASCII_Button = new JButton("Export ASCII");
	
	private  BasicGraphData designGD = new BasicGraphData();
	private  BasicGraphData experGD = new BasicGraphData();	

	//local data file
	private File dataFile = null;
	
	//message text field. It is actually message text field from MainWindow
	private JTextField messageTextLocal = new JTextField();		
	
	/**
	*  Costructor
	*/
	public 	ShakerController(Vector<AcceleratorSeq> accSeqV_in){
		bpmTree = new DevTree(rootBPMTreeNode);		
		rfTree = new DevTree(rootRFTreeNode);
		
		//make accelerator calculator 
		accCalc = new AccCalculator(rootRFTreeNode,rootBPMTreeNode);
		initAccSeqV(accSeqV_in);

		//make tree
		JScrollPane rfTreeView = new JScrollPane(rfTree);
		JScrollPane bpmTreeView = new JScrollPane(bpmTree);
		
		JPanel cntrPanel = new JPanel(new BorderLayout());
		
		JPanel cntrl_0_Panel = new JPanel(new GridLayout(5,2));
		cntrl_0_Panel.add(phaseShifLabel);
		cntrl_0_Panel.add(phaseShiftTextFiled);
		cntrl_0_Panel.add(sleepTimeLabel);
		cntrl_0_Panel.add(sleepTimeTextFiled);
		cntrl_0_Panel.add(numbAvgLabel);
		cntrl_0_Panel.add(numbAvgTextFiled);
		cntrl_0_Panel.add(calculateShake_Button);
		cntrl_0_Panel.add(measureShake_Button);
		JPanel empty_Panel = new JPanel();
		cntrl_0_Panel.add(empty_Panel);
		cntrl_0_Panel.add(stopMeasure_Button);
		
		cntrPanel.add(cntrl_0_Panel,BorderLayout.NORTH);
		phaseShiftTextFiled.setNumberFormat(new DecimalFormat("##.#"));
		sleepTimeTextFiled.setNumberFormat(new DecimalFormat("##.#"));
		numbAvgTextFiled.setNumberFormat(new DecimalFormat("###"));
		phaseShiftTextFiled.setValue(3.0);
		sleepTimeTextFiled.setValue(5.0);
		numbAvgTextFiled.setValue(1.0);
		
		//make Tree panel
		JPanel treePanel = new JPanel(new BorderLayout());
		
		JPanel tree0Panel = new JPanel(new GridLayout(1,2));
		tree0Panel.add(rfColumnLabel);
		tree0Panel.add(bpmColumnLabel);
		
		rfColumnLabel.setForeground(Color.blue);
		bpmColumnLabel.setForeground(Color.blue);
		
		JPanel tree1Panel = new JPanel(new GridLayout(1,2));
		tree1Panel.add(rfTreeView);
		tree1Panel.add(bpmTreeView);
		
		treePanel.add(tree0Panel,BorderLayout.NORTH);
		treePanel.add(tree1Panel,BorderLayout.CENTER);
		
		calculateShake_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					rfTree.setEnabled(false);
					bpmTree.setEnabled(false);
					messageTextLocal.setText(null);
					designGD.removeAllPoints();
					accCalc.caclulatePhaseResponse(phaseShiftTextFiled.getValue(),designGD);
					fGraphPanel.refreshGraphJPanel();	
					rfTree.setEnabled(true);
					bpmTree.setEnabled(true);					
				}
			});
		
		measureShake_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					rfTree.setEnabled(false);
					bpmTree.setEnabled(false);	
					messageTextLocal.setText(null);					
					Runnable measure = new Runnable(){
						public void run(){
							measureShake_Button.setEnabled(false);	
							stopMeasure_Button.setEnabled(true);
							calculateShake_Button.setEnabled(false);
							experGD.removeAllPoints();							
							int nAvg = (int) numbAvgTextFiled.getValue();
							accCalc.measurePhaseResponse(phaseShiftTextFiled.getValue(),sleepTimeTextFiled.getValue(), nAvg,experGD);
							fGraphPanel.refreshGraphJPanel();	
							rfTree.setEnabled(true);
							bpmTree.setEnabled(true);	
							stopMeasure_Button.setEnabled(false);
							measureShake_Button.setEnabled(true);
							calculateShake_Button.setEnabled(true);
						}
					};
					Thread thread = new Thread(measure);
					thread.start();
				}
			});
		
		stopMeasure_Button.setEnabled(false);
		stopMeasure_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Thread thread = accCalc.getRunningThread();
					if(thread != null && thread.isAlive()){
						thread.interrupt();
					}
				}
			});
		
		//set up graphs
		JPanel graphPanel = new JPanel(new BorderLayout());
		JPanel low_graphPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,5,1));	
		
		low_graphPanel.add(posLabel);
		low_graphPanel.add(fGraphPanel.getClickedPointObject().xValueText);
	  low_graphPanel.add(slopeLabel);	
		low_graphPanel.add(fGraphPanel.getClickedPointObject().yValueText);
		low_graphPanel.add(new JLabel("      "));
		low_graphPanel.add(saveASCII_Button);
		
		fGraphPanel.getClickedPointObject().xValueText.setColumns(9);
		fGraphPanel.getClickedPointObject().yValueText.setColumns(10);
		
//		fGraphPanel.getClickedPointObject().xValueFormat.applyPattern("###.##");
//		fGraphPanel.getClickedPointObject().yValueFormat.applyPattern("###.###");
		fGraphPanel.getClickedPointObject().setDecimalFormatX("###.##");
		fGraphPanel.getClickedPointObject().setDecimalFormatY("###.###");

		graphPanel.add(fGraphPanel,BorderLayout.CENTER);
		graphPanel.add(	low_graphPanel,BorderLayout.SOUTH);	
		
		fGraphPanel.setOffScreenImageDrawing(true);
		fGraphPanel.setLegendButtonVisible(true);
		fGraphPanel.setChooseModeButtonVisible(true);
		SimpleChartPopupMenu.addPopupMenuTo(fGraphPanel);	
		
		fGraphPanel.addGraphData(designGD);
		fGraphPanel.addGraphData(experGD);
		
		fGraphPanel.setName("Phase Response for RF Phase Shaking");
		fGraphPanel.setAxisNames("distance s, m","delta(BPM Phase)/delta(RF Phase), a.u.");
		
		designGD.setGraphProperty(fGraphPanel.getLegendKeyString(),"Design Response");
		experGD.setGraphProperty(fGraphPanel.getLegendKeyString(),"BPM Response");

		designGD.setImmediateContainerUpdate(false);
		experGD.setImmediateContainerUpdate(false);	
		
		designGD.setDrawLinesOn(true);
		experGD.setDrawLinesOn(false);
		
		designGD.setDrawPointsOn(false);
		experGD.setDrawPointsOn(true);
		
		designGD.setGraphColor(Color.BLUE);
		experGD.setGraphColor(Color.BLACK);
		
		experGD.setGraphPointSize(8);
		
		designGD.setLineThick(3);
		experGD.setLineThick(3);	
		
		//save data to ASCII file
		saveASCII_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BasicGraphData gd = getChoosenDraphData();
					if (gd != null) {
						JFileChooser ch = new JFileChooser();
						ch.setDialogTitle("Export to ASCII");
						if (dataFile != null) {
							ch.setSelectedFile(dataFile);
						}
						int returnVal = ch.showSaveDialog(mainPanel);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							try {
								dataFile = ch.getSelectedFile();
								BufferedWriter out = new BufferedWriter(new FileWriter(dataFile));
								int nP = gd.getNumbOfPoints();
								for (int i = 0; i < nP; i++) {
									out.write(" " + gd.getX(i) + " " + gd.getY(i) + " " + gd.getErr(i));
									out.newLine();
								}
								out.flush();
								out.close();
							} catch (IOException exp) {
								Toolkit.getDefaultToolkit().beep();
								System.out.println(exp.toString());
							}
						}
						messageTextLocal.setText(null);
					} else {
						messageTextLocal.setText(null);
						messageTextLocal.setText("Please choose graph first. Use S-button on the graph panel.");
						Toolkit.getDefaultToolkit().beep();
					}
				}
			});
		
		//make tree-graph panel
		JSplitPane tree_and_graph_panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		Border border = BorderFactory.createEtchedBorder();	
		treesBorder =BorderFactory.createTitledBorder(border, "RF to Shake and BPMs");
		treePanel.setBorder(treesBorder);		
		cntrPanel.setBorder(border);
		
		JPanel cntrl_and_tree_Panel = new JPanel(new BorderLayout());
		cntrl_and_tree_Panel.add(cntrPanel,BorderLayout.NORTH);
		cntrl_and_tree_Panel.add(treePanel,BorderLayout.CENTER);
		
		
		tree_and_graph_panel.add(cntrl_and_tree_Panel);
		
		tree_and_graph_panel.add(graphPanel);
		tree_and_graph_panel.setDividerLocation(0.3);
			
		//make main panel
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(tree_and_graph_panel,BorderLayout.CENTER);		

	}
	
	/**
	 *  Sets up accelerator sequences for shaking.
	 */
	public void initAccSeqV(Vector<AcceleratorSeq> accSeqV_in){
		accCalc.setAccSeqV(accSeqV_in);
		rootBPMTreeNode.children.clear();
		rootRFTreeNode.children.clear();
		
		accSeqV = accSeqV_in;
		AcceleratorSeqCombo accSeqGlobal = new AcceleratorSeqCombo("linac",accSeqV);
		TypeQualifier bpm_qualifier = KindQualifier.qualifierWithStatusAndType(true, "BPM");
		TypeQualifier rf_qualifier = KindQualifier.qualifierWithStatusAndType(true, "Rfcavity");
		
		//make BPM tree 
		for(AcceleratorSeq accSeq : accSeqV){
			DevTreeNode accSeqTreeNode = new DevTreeNode();
			accSeqTreeNode.accSeq = accSeq;
			accSeqTreeNode.parentNode = rootBPMTreeNode;
			accSeqTreeNode.position = accSeqGlobal.getPosition(accSeq);
			rootBPMTreeNode.children.add(accSeqTreeNode);
			java.util.List<AcceleratorNode> lst = accSeq.getAllInclusiveNodesWithQualifier(bpm_qualifier);
			for(AcceleratorNode node : lst){
				DevTreeNode accNodeTreeNode = new DevTreeNode();
				accNodeTreeNode.accNode = node;
				accNodeTreeNode.parentNode = accSeqTreeNode;
				int orientation =  ((BPM) node).getBucket("bpm").getAttr("orientation").getInteger();
				double length = ((BPM) node).getBucket("bpm").getAttr("length").getDouble();				
				accNodeTreeNode.position = accSeqGlobal.getPosition(node) - orientation*length/2;
				accSeqTreeNode.children.add(accNodeTreeNode);
			}	
		}
		
		//make tree for RF
		for(AcceleratorSeq accSeq : accSeqV){
			DevTreeNode accSeqTreeNode = new DevTreeNode();
			accSeqTreeNode.accSeq = accSeq;
			accSeqTreeNode.parentNode = rootRFTreeNode;
			accSeqTreeNode.position = accSeqGlobal.getPosition(accSeq);
			rootRFTreeNode.children.add(accSeqTreeNode);
			java.util.List<AcceleratorNode> lst = accSeq.getAllInclusiveNodesWithQualifier(rf_qualifier); 
			for(AcceleratorNode node : lst){
				DevTreeNode accNodeTreeNode = new DevTreeNode();
				accNodeTreeNode.accNode = node;
				accNodeTreeNode.parentNode = accSeqTreeNode;
				accNodeTreeNode.position = accSeqGlobal.getPosition(node);
				accSeqTreeNode.children.add(accNodeTreeNode);
				RfCavity rfCav = (RfCavity) node;
				accNodeTreeNode.hashT.clear();
				accNodeTreeNode.hashT.put("designPhase",new Double(rfCav.getDfltCavPhase()));
				accNodeTreeNode.hashT.put("designAmp",new Double(rfCav.getDfltCavAmp()));
				accNodeTreeNode.hashT.put("freq", new Double(1.0e+6*rfCav.getCavFreq()));
			}	
		}
		((DefaultTreeModel) bpmTree.getModel()).reload();
		((DefaultTreeModel) rfTree.getModel()).reload();
	}		
	
	
	/**
	 *  Returns the panel with GUI elements.
	 */
	public JPanel getJPanel(){
		return mainPanel;
	}	
	
	/**
	 *  Returns the vector with accelerator sequences.
	 */
	public Vector<AcceleratorSeq> getAccSeqV(){
		return accSeqV;
	}		
	
	/**
	 *  Returns the RF Tree Root Node.
	 */
	public DevTreeNode getRFRootNode(){
		return rootRFTreeNode;
	}			
	
	/**
	 *  Returns the BPM Tree Root Node.
	 */
	public DevTreeNode getBPMRootNode(){
		return rootBPMTreeNode;
	}			
		
	/**
	 *  Returns the design graph.
	 */
	public BasicGraphData getDesignGraph(){
		return designGD;
	}			
	
	/**
	 *  Returns the measured graph.
	 */
	public BasicGraphData getMeasuredGraph(){
		return experGD;
	}			
	
	
	/**
	 *  Returns the chosen  BasicGraphData object
	 *
	 *@return    The  chosen  BasicGraphData object
	 */
	private BasicGraphData getChoosenDraphData() {
		BasicGraphData gd = null;
		Integer Ind = fGraphPanel.getGraphChosenIndex();
		if (Ind != null && Ind.intValue() >= 0) {
			gd = fGraphPanel.getInstanceOfGraphData(Ind.intValue());
			return gd;
		} else {
			Vector<BasicGraphData> gdV = fGraphPanel.getAllGraphData();
			if (gdV.size() == 1) {
				gd = gdV.get(0);
				return gd;
			} else {
				if (gdV.size() == 0) {
					return null;
				}
				Vector<BasicGraphData> gdInsideV =
						GraphDataOperations.getDataInsideRectangle(gdV,
						fGraphPanel.getCurrentMinX(),
						fGraphPanel.getCurrentMaxX(),
						fGraphPanel.getCurrentMinY(),
						fGraphPanel.getCurrentMaxY());
				if (gdInsideV.size() == 1) {
					return gdInsideV.get(0);
				}
			}
		}
		return null;
	}
	
	/**
	* Connects the local text message field with the outside field
	*/
  public void setMessageText(JTextField messageTextLocal){
		this.messageTextLocal.setDocument(messageTextLocal.getDocument());
		accCalc.setMessageText(messageTextLocal);
	}		
	
	public void setFontForAll(Font fnt) {
		
		bpmTree.setFontForAll(fnt);
		rfTree.setFontForAll(fnt);
		
		calculateShake_Button.setFont(fnt);
		posLabel.setFont(fnt);
	  slopeLabel.setFont(fnt);	
		saveASCII_Button.setFont(fnt);	
	
		rfColumnLabel.setFont(fnt);
		bpmColumnLabel.setFont(fnt);
		
		phaseShifLabel.setFont(fnt);
		phaseShiftTextFiled.setFont(fnt);
		sleepTimeLabel.setFont(fnt);
		sleepTimeTextFiled.setFont(fnt);
		numbAvgLabel.setFont(fnt);
		numbAvgTextFiled.setFont(fnt);
		measureShake_Button.setFont(fnt);	
		stopMeasure_Button.setFont(fnt);
		
		
		
		fGraphPanel.getClickedPointObject().xValueText.setFont(fnt);
		fGraphPanel.getClickedPointObject().yValueText.setFont(fnt);	
		
		treesBorder.setTitleFont(fnt);
		
	}	
	
}
