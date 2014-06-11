package xal.app.injdumpwizard.utils;

import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;

import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;

/**
 *  This is a display of two graphs with X and Y orbits
 *
 *@author     shishlo
 */
public class  TwoGraph_Wrapper {

	//JPanel with all GUI elements
	private JPanel wrapperPanel = new JPanel(new BorderLayout());
	private TitledBorder wrapperBorder = null;

	private  BasicGraphData gdExpX = new BasicGraphData();
	private  BasicGraphData gdExpY = new BasicGraphData();
	private  BasicGraphData gdModelX = new BasicGraphData();
	private  BasicGraphData gdModelY = new BasicGraphData();
	
	private  FunctionGraphsJPanel gpX = new FunctionGraphsJPanel();
	private  FunctionGraphsJPanel gpY = new FunctionGraphsJPanel();
	
	public TwoGraph_Wrapper(){

		Border border = BorderFactory.createEtchedBorder();
		wrapperBorder = BorderFactory.createTitledBorder(border, "H and V Orbits");
		wrapperPanel.setBorder(wrapperBorder);

	
		JPanel graphsPanel = new JPanel(new GridLayout(1, 2, 1, 1));
		graphsPanel.add(gpX);
		graphsPanel.add(gpY);	

		wrapperPanel.add(graphsPanel,BorderLayout.CENTER);
				
		
		gpX.setOffScreenImageDrawing(true);
		gpY.setOffScreenImageDrawing(true);
		
		gpX.setLegendButtonVisible(true);
		gpY.setLegendButtonVisible(true);
		
		SimpleChartPopupMenu.addPopupMenuTo(gpX);
		SimpleChartPopupMenu.addPopupMenuTo(gpY);
		
		gpX.addGraphData(gdExpX);
		gpX.addGraphData(gdModelX);
		
		gpY.addGraphData(gdExpY);
		gpY.addGraphData(gdModelY);
		
		//graphs properties
		gpX.setName("Hor. Orbit. BPM00-QV01-BPM01a-DCH/V01-BPM01b-WS01-BPM01c-Dump");
		gpY.setName("Ver. Orbit. BPM00-QV01-BPM01a-DCH/V01-BPM01b-WS01-BPM01c-Dump");
		gpX.setAxisNames("IDmp+ Position, m","X, mm");
		gpY.setAxisNames("IDmp+ Position, m","Y, mm");
		
		//data lines properties
		gdExpX.setGraphProperty(gpX.getLegendKeyString(),"Measured Horizontal");
		gdExpY.setGraphProperty(gpY.getLegendKeyString(),"Measured Vertical");
		gdModelX.setGraphProperty(gpX.getLegendKeyString(),"Simulated Horizontal");
		gdModelY.setGraphProperty(gpY.getLegendKeyString(),"Simulated Vertical");
		
		gdExpX.setImmediateContainerUpdate(false);
		gdExpY.setImmediateContainerUpdate(false);
		gdModelX.setImmediateContainerUpdate(false);
		gdModelY.setImmediateContainerUpdate(false);
		
		gdExpX.setDrawLinesOn(false);
		gdExpY.setDrawLinesOn(false);
		gdExpX.setGraphColor(Color.RED);
		gdExpY.setGraphColor(Color.RED);
		gdExpX.setGraphPointSize(10);
		gdExpY.setGraphPointSize(10);
		
		gdModelX.setLineThick(2);
		gdModelY.setLineThick(2);
		gdModelX.setGraphPointSize(8);
		gdModelY.setGraphPointSize(8);
		gdModelX.setGraphColor(Color.BLUE);
		gdModelY.setGraphColor(Color.BLUE);
	}

	/**
	* Returns the panel with all GUI elements
	*/
	public JPanel getJPanel(){
		return wrapperPanel;
	}
	
	public BasicGraphData getExpGraphX(){
		return gdExpX;
	}
	
	public BasicGraphData getExpGraphY(){
		return gdExpY;
	}
	
	public BasicGraphData getModelGraphX(){
		return gdModelX;
	}
	
	public BasicGraphData getModelGraphY(){
		return gdModelY;
	}	
	
	/**
	* Update graphs
	*/
	public void updateGraphs(){
		gpX.refreshGraphJPanel();		
		gpY.refreshGraphJPanel();
	}	
	
	
}
