package xal.app.lossviewer.dndcomponents.lossplot;

import xal.app.lossviewer.*;
import xal.app.lossviewer.signals.*;

import java.awt.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import org.jfree.chart.axis.*;
import org.jfree.data.xy.*;

import java.util.List;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

public class TypeDataSet extends XYSeriesCollection implements LossChartAdaptor,DataListener {
    private static final long serialVersionUID = -2618010493689240660L;
	
	
	public String dataLabel() {
		
		return "TypeDataSet";
	}
	
	
	public void update(DataAdaptor adaptor) {
		try {
			setMinYLimit(adaptor.doubleValue("Minimum"));
			setMaxYLimit(adaptor.doubleValue("Maximum"));
			setVisible(adaptor.booleanValue("Visible"));
			isLimitVisible = adaptor.booleanValue("LimitsVisible");
			setYNumMajorTicks(adaptor.intValue("Ticks"));
			setYAutoScale(adaptor.booleanValue("AutoScale"));
		}
		catch (Exception ex) {
			
		}
		plot.reinitialize();
		
	}
	
	
	public void write(DataAdaptor adaptor) {
		adaptor.setValue("name", typeName);
		adaptor.setValue("Visible", isVisible);
		adaptor.setValue("LimitsVisible", isLimitVisible);
		adaptor.setValue("Minimum", getMinYLimit());
		adaptor.setValue("Maximum", getMaxYLimit());
		adaptor.setValue("AutoScale", isYAutoScale());
		adaptor.setValue("Ticks", getYNumMajorTicks());
	}
	
	
	
	MySeries typeData;
	String typeName;
	ArrayList<LossDetector> detectors = new ArrayList<LossDetector>();
//	ArrayList<Integer> detectorIndices = new ArrayList<Integer>();
	
	private JMenu menu;
	
	private NumberAxis axis;
	
	private AxisLocation location;
	
	private Component owner;
	
	private int numMajorTicks;
	
	private double maxLimit;
	
	private double minLimit;
	
	private double FREEZING_FACTOR=1.2;
	
	private boolean isVisible=true;
	
	private LossPlot plot;
	
	private boolean isLimitVisible = true;
	
	TypeDataSet(String typeName, LossPlot plot) {
		this.plot = plot;
		this.typeName = typeName;
		typeData = new MySeries(typeName);
		
//		setVisible(true);
		this.addSeries(typeData);
		owner = plot.getChartPanel();
		initializeMenu();
		typeData.setNotify(false);
		
	}
	Set<String> normalization;
	
	public Set<String> getNormalization() {
		return normalization;
	}
	public void setNormalization(Set<String>  currentNormalization) {
		normalization = currentNormalization;
		
		String normaString = null;
		if(normalization.contains("LMT")){
			normaString = typeName + ",%";
		}
		else {
			normaString = typeName + ",Rad";
			
			if (normalization.contains("CHRG")) {
				normaString = normaString + "/C";
			}
			if (normalization.contains("DST1")) {
				normaString = normaString + "*d";
			}
			else if (normalization.contains("DST2")) {
				normaString = normaString + "*d^2";
			}
		}
		axis.setLabel(normaString);
		
	}
	
	public List<LossDetector> getDetectors() {
		return detectors;
		
	}
	public String getType() {
		return typeName;
	}
	public int size() {
		return detectors.size();
	}
	
	
	private NumberFormat df = new DecimalFormat("0.00E00");
	
	public void removeAllDetectors() {
		detectors.clear();
		//	detectorIndices.clear();
		this.removeSeries(typeData);
		typeData = new MySeries(typeName);
		this.addSeries(typeData);
		typeData.setNotify(false);
	}
	
	public ValueAxis getAxis() {
		
		return axis;
	}
	
	public void setAxis(NumberAxis axis, Paint p, AxisLocation loc) {
		this.axis = axis;
		axis.setNumberFormatOverride(df);
		axis.setTickLabelFont(axis.getTickLabelFont().deriveFont(Font.BOLD, 14.0f));
		axis.setAxisLinePaint(p);
		axis.setTickLabelPaint(p);
		
		this.location = loc;
	}
	public AxisLocation getAxisLocation() {
		return location;
	}
	
	
	private void initializeMenu() {
		menu = new JMenu(typeName);
		final TypePopupMenu m = new TypePopupMenu(owner, this, typeName);
		MenuElement[] elems = m.getSubElements();
		
		for (MenuElement elem : elems) {
			try {
				menu.add((JMenuItem)elem);
			}
			catch (ClassCastException cce) {
				
			}
		}
		
		menu.addMenuListener(new MenuListener(){
				public void menuSelected(MenuEvent e) {
					m.update();
				}
				public void menuDeselected(MenuEvent e) {}
				public void menuCanceled(MenuEvent e) {}
			});
	}
	
	
	
	public void addDetector(int detectorIndex, LossDetector detector) {
		
		detectors.add(detector);
		//	detectorIndices.add(detectorIndex);
		typeData.add(detectorIndex, 0, false);
	}
        public void updateDetectors(String parName) {
            updateDetectors(parName, true);
        }
	public void updateDetectors(String parName, boolean noNegatives) {
		
		for (int i=0;i < detectors.size();i++) {
			LossDetector det = detectors.get(i);
			ScalarSignalValue sv = (ScalarSignalValue)det.getValue(parName, normalization);
			
			if (sv != null) {
				
				double v = sv.getValue();
				if (v <= 0 && noNegatives) {
					v = -v;
				}
				typeData.updateByIndex(i, v);
			}
			else {
				typeData.updateByIndex(i, 0.0);
			}
			
		}
		typeData.setNotify(true);
		//      typeData.fireSeriesChanged();
		typeData.setNotify(false);
		
		
	}
	
	public JMenu getMenu() {
		
		return menu;
	}
	
	public boolean isVisible() {
		return isVisible;
	}
	public void setVisible(boolean state) {
		if (state != isVisible) {
			isVisible = state;
			axis.setVisible(isVisible);
//			if(state){
//				this.addSeries(typeData);
//			}
//			else {
//				this.removeSeries(typeData);
//			}
			plot.reinitialize();
		}
	}
	
	
	public boolean isLimitVisible() {
		return isLimitVisible;
	}
	public void setLimitVisible(boolean state) {
		if (state != isLimitVisible) {
			isLimitVisible = state;
			
			plot.reinitialize();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/////////////ChartPopUpAdaptor
	
	
	
	/**
	 * Get the chart component.
	 * @return The chart as a component.
	 */
	public Component getChartComponent() {
		
		return owner;
	}
	
	
	
	/**
	 * Get the minimum value of y that is visible.
	 * @return the minimum value of y that is visible
	 */
	public double getMinYLimit() {
		
		return axis.getLowerBound();
	}
	
	/**
	 * Set the minimum value of y that is visible.
	 * @param lowerLimit the minimum value of y that is visible
	 */
	public void setMinYLimit(double lowerLimit) {
		minLimit = lowerLimit;
		axis.setLowerBound(lowerLimit);
	}
	
	/**
	 * Get the maximum value of y that is visible.
	 * @return the maximum value of y that is visible
	 */
	public double getMaxYLimit() {
		
		return axis.getUpperBound();
	}
	
	/**
	 * Set the maximum value of y that is visible.
	 * @param upperLimit the maximum value of y that is visible
	 */
	public void setMaxYLimit(double upperLimit) {
		maxLimit = upperLimit;
		axis.setUpperBound(upperLimit);
	}
	
	/**
	 * Scale the x and y axes once so all points fit on the chart then keep
	 * the axes' scales fixed.
	 */
	public void scaleXandY() {
		setYAutoScale(true);
		setYAutoScale(false);
	}
	
	
	
	/**
	 * Get the state of y-axis auto-scaling
	 * @return true if the y-axis has auto-scaling enabled; false if not
	 */
	public boolean isYAutoScale() {
		
		return axis.isAutoRange();
	}
	
	/**
	 * Set the auto-scale state of the y-axis
	 * @param state true to enable y-axis auto-scaling; false to disable auto-scaling
	 */
	public void setYAutoScale(boolean state) {
		axis.setAutoRange(state);
		if (state == false) { //we are freezing
			axis.setUpperBound(axis.getUpperBound() * FREEZING_FACTOR);
			setYNumMajorTicks(numMajorTicks);
		}
		else {
			axis.setAutoTickUnitSelection(true, true);
		}
	}
	
	
	
	/**
	 * Get the number of minor ticks per major step on the y-axis.
	 * @return the number of minor ticks
	 */
	public int getYNumMinorTicks() {
		
		return 0;
	}
	
	/**
	 * Set the number of minor ticks on the y-axis.
	 * @param count the number of minor ticks
	 */
	public void setYNumMinorTicks(int count) {
		
	}
	
	/**
	 * Get the number of minor ticks on the y-axis.
	 * @return the number of major ticks
	 */
	public int getYNumMajorTicks() {
		
		return numMajorTicks;
	}
	
	/**
	 * Set the number of major ticks on the y-axis.
	 * @param count the number of major ticks
	 */
	public void setYNumMajorTicks(int count) {
		numMajorTicks = count;
		if (numMajorTicks > 0) {
			axis.setTickUnit(new NumberTickUnit((getMaxYLimit() - getMinYLimit()) / (numMajorTicks - 1)), true, true);
		}
		else {
			axis.setAutoTickUnitSelection(true, true);
		}
	}
	
	
	
	/**
	 * Get the visibility state of the y-axis grid.
	 * @return true if the grid is visible
	 */
	public boolean isYGridVisible() {
		
		return true;
	}
	
	/**
	 * Set the visibility of the y-axis grid.
	 * @param visibility true to enable the grid; false to disable the grid
	 */
	public void setYGridVisible(boolean visibility) {
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/////////////////////
	/////// X axis not used
	
	/**
	 * Get the visibility state of the x-axis grid.
	 * @return true if the grid is visible
	 */
	public boolean isXGridVisible() {
		// TODO: Implement this method
		return false;
	}
	
	/**
	 * Set the visibility of the x-axis grid.
	 * @param visibility true to enable the grid; false to disable the grid
	 */
	public void setXGridVisible(boolean visibility) {
		// TODO: Implement this method
	}
	
	
	/**
	 * Get the number of minor ticks per major step on the x-axis.
	 * @return the number of minor ticks
	 */
	public int getXNumMinorTicks() {
		// TODO: Implement this method
		return 0;
	}
	
	/**
	 * Set the number of minor ticks on the x-axis.
	 * @param count number of minor ticks
	 */
	public void setXNumMinorTicks(int count) {
		// TODO: Implement this method
	}
	
	/**
	 * Get the number of major ticks on the x-axis.
	 * @return the spacing per minor tick
	 */
	public int getXNumMajorTicks() {
		// TODO: Implement this method
		return 0;
	}
	
	/**
	 * Set the number of major ticks on the x-axis.
	 * @param count the desired number of major ticks
	 */
	public void setXNumMajorTicks(int count) {
		// TODO: Implement this method
	}
	
	
	/**
	 * Get the state of x-axis auto-scaling
	 * @return true if the x-axis has auto-scaling enabled; false if not
	 */
	public boolean isXAutoScale() {
		// TODO: Implement this method
		return false;
	}
	
	/**
	 * Set the auto-scale state of the x-axis
	 * @param state true to enable x-axis auto-scaling; false to disable auto-scaling
	 */
	public void setXAutoScale(boolean state) {
		// TODO: Implement this method
	}
	
	
	/**
	 * Get the minimum value of x that is visible.
	 * @return the minimum value of x that is visible
	 */
	public double getMinXLimit() {
		// TODO: Implement this method
		return 0;
	}
	
	/**
	 * Set the minimum value of x that is visible.
	 * @param lowerLimit the minimum value of x that is visible
	 */
	public void setMinXLimit(double lowerLimit) {
		// TODO: Implement this method
	}
	
	/**
	 * Get the maximum value of x that is visible.
	 * @return the maximum value of x that is visible
	 */
	public double getMaxXLimit() {
		// TODO: Implement this method
		return 0;
	}
	
	/**
	 * Set the maximum value of x that is visible.
	 * @param upperLimit the maximum value of x that is visible
	 */
	public void setMaxXLimit(double upperLimit) {
		// TODO: Implement this method
	}
	
	
	
	
}

class MySeries extends XYSeries {
    private static final long serialVersionUID = 3840484779229701051L;
	public MySeries(String s) {
		super(s);
	}
	
}
