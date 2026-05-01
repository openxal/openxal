/*
 * TimeStampWindow.java
 *
 * Created on August 9, 2004, 02:25 PM
 */

package xal.app.timestamptest;

import javax.swing.*;

import java.text.NumberFormat;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.tools.apputils.EdgeLayout;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.DecimalField;

/**
 * TimeStampWindow is the window control for the Time Stamp Test application.
 * 
 * @author Paul Chu
 */
public class TimeStampWindow extends AcceleratorWindow {
	static final long serialVersionUID = 0;

	protected TimeStampDocument myDocument;

	private JTabbedPane mainPanel;

	protected JTextField pvText = new JTextField(23);

	protected JTextField textField;

	private JPanel pvControlPane = new JPanel();

	protected Box legendBox = new Box(BoxLayout.Y_AXIS);

	private JButton addPV = new JButton("Add");

	private JButton removeAll = new JButton("Remove All");

	//	private JButton done = new JButton("OK");

	//	private JButton cancel = new JButton("Cancel");

	private PVPanel[] pvPanels;

	private final FunctionGraphsJPanel pvPlot = new FunctionGraphsJPanel();

	private boolean oldDocInd = false;

	JPanel pvPane = new JPanel();

	//    private JScrollPane pvScrollPane;
	private JPanel pvTSPane = new JPanel();

	private JPanel pvPlotPane = new JPanel();

	private ArrayList<String> pvList = new ArrayList<String>();

	int nPV = 0;

	protected JDialog configDialog = new JDialog();

	protected JDialog[] configYDialogs;

	protected DecimalField[] mins, maxs;

	protected double[] dMins, dMaxs;

	protected DecimalField df1;

	protected DecimalField dhr, dmin, dsec;

	NumberFormat numberFormat = NumberFormat.getNumberInstance();

	protected int buffSize = 1000;

	protected int timeInSeconds = 300;

	Color[] colors = { Color.black, Color.blue, Color.green, Color.red,
			Color.magenta, Color.orange, Color.darkGray, Color.pink, 
			Color.yellow, Color.gray, Color.cyan};

	private HashMap<String, JTextField> yRange;
	
	JButton resetAllScales = new JButton("auto-scale Y");
	
	private boolean scaleChanged = false;
	private double globalMin;
	private double globalMax;

	/** Creates a new instance of MainWindow */
	public TimeStampWindow(XalDocument aDocument) {
		super(aDocument);
		myDocument = (TimeStampDocument) aDocument;
		setSize(1040, 600);
		legendBox.setPreferredSize(new Dimension(300, 550));
		legendBox.add(new JLabel("PVs:"));
		mainPanel = new JTabbedPane();

		pvPlot.setSmartGL(false);
		pvPlot.addMouseListener(new SimpleChartPopupMenu(pvPlot));
		//		pvPlot.setLegendButtonVisible(true);
		pvPlot.setGraphBackGroundColor(Color.white);
		pvPlot.setNumberFormatX(new DateGraphFormat("MMM dd, yyyy HH:mm:ss"));

		makeContent();

		// global settings
		Box configBuffer = new Box(BoxLayout.X_AXIS);
		JLabel buffLabel = new JLabel("Buffer size = ");
		//		configBuffer.setLayout(new GridLayout(1, 2));
		numberFormat.setMaximumFractionDigits(0);
		df1 = new DecimalField(buffSize, 5, numberFormat);
		configBuffer.add(buffLabel);
		configBuffer.add(df1);

		Box configDisplayTimeRange = new Box(BoxLayout.X_AXIS);
		//		configDisplayTimeRange.setPreferredSize(new Dimension(300, 20));
		JLabel timeRangeLabel = new JLabel("Display time range = ");
		JLabel tmp = new JLabel(" : ");
		JLabel tmp1 = new JLabel(" : ");
		JLabel tmp2 = new JLabel("sec");
		//		configDisplayTimeRange.setLayout(new GridLayout(1, 6));
		int hour = 0;
		int min = 5;
		int sec = 0;
		timeInSeconds = sec + min * 60 + hour * 3600;
		dhr = new DecimalField(hour, 2, numberFormat);
		dmin = new DecimalField(min, 2, numberFormat);
		dsec = new DecimalField(sec, 2, numberFormat);
		configDisplayTimeRange.add(timeRangeLabel);
		configDisplayTimeRange.add(dhr);
		configDisplayTimeRange.add(tmp);
		configDisplayTimeRange.add(dmin);
		configDisplayTimeRange.add(tmp1);
		configDisplayTimeRange.add(dsec);
		configDisplayTimeRange.add(tmp2);

		JPanel paramConfBtn = new JPanel();
		EdgeLayout edgeLayout3 = new EdgeLayout();
		paramConfBtn.setLayout(edgeLayout3);
		JButton done = new JButton("OK");
		done.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// set buffer size
				buffSize = Math.round((int) df1.getDoubleValue());
				// set display (time) range
				timeInSeconds = Math.round((int) (dsec.getDoubleValue()
						+ dmin.getDoubleValue() * 60 + dhr.getDoubleValue() * 3600));
				// update plot settings
				if (myDocument.cam != null) {
					for (int i = 0; i < myDocument.cam.length; i++) {
						if (buffSize != myDocument.cam[i].getMaxLength()) {
							myDocument.cam[i].setMaxLength(buffSize);
							myDocument.setHasChanges(true);
						}
						if (timeInSeconds != myDocument.timeRange) {
							myDocument.cam[i]
									.setDisplayTimeRange(timeInSeconds);
							myDocument.setHasChanges(true);
						}
					}
				}

				configDialog.setVisible(false);
			}
		});
		edgeLayout3.setConstraints(done, 0, 50, 0, 0, EdgeLayout.LEFT_BOTTOM,
				EdgeLayout.NO_GROWTH);
		paramConfBtn.add(done);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				configDialog.setVisible(false);
			}
		});
		edgeLayout3.setConstraints(cancel, 0, 170, 0, 0,
				EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
		paramConfBtn.add(cancel);

		configDialog.setBounds(300, 300, 400, 300);
		configDialog.setTitle("Configuration");
		EdgeLayout edgeLayout1 = new EdgeLayout();
		configDialog.getContentPane().setLayout(edgeLayout1);
		edgeLayout1.setConstraints(configBuffer, 10, 30, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		configDialog.getContentPane().add(configBuffer);
		edgeLayout1.setConstraints(configDisplayTimeRange, 50, 30, 0, 0,
				EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
		configDialog.getContentPane().add(configDisplayTimeRange);
		edgeLayout1.setConstraints(paramConfBtn, 0, 0, 10, 0,
				EdgeLayout.BOTTOM, EdgeLayout.NO_GROWTH);
		configDialog.getContentPane().add(paramConfBtn);
	}

	/**
	 * Create the main window subviews.
	 */
	protected void makeContent() {

		EdgeLayout edgeLayout = new EdgeLayout();
		pvControlPane.setLayout(edgeLayout);

		JLabel label = new JLabel("PV: ");
		edgeLayout.setConstraints(label, 2, 0, 0, 0, EdgeLayout.LEFT);
		pvControlPane.add(label);
		edgeLayout.setConstraints(pvText, 2, 25, 0, 0, EdgeLayout.LEFT);
		pvControlPane.add(pvText);
		edgeLayout.setConstraints(addPV, 0, 320, 0, 0, EdgeLayout.LEFT);
		pvControlPane.add(addPV);
		edgeLayout.setConstraints(removeAll, 0, 0, 0, 0, EdgeLayout.RIGHT);
		pvControlPane.add(removeAll);

		pvPane.setLayout(new BorderLayout());

		pvTSPane.setPreferredSize(new Dimension(750, 400));
		//        pvTSPane.setLayout(new FlowLayout());
		pvTSPane.setBorder(BorderFactory.createRaisedBevelBorder());

		pvPane.add(pvControlPane, BorderLayout.NORTH);
		//        pvTSPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//        pvScrollPane = new JScrollPane(pvTSPane);
		pvPane.add(pvTSPane, BorderLayout.CENTER);

		pvPlotPane.setLayout(new BorderLayout());
		pvPlotPane.add(pvPlot, BorderLayout.CENTER);
		pvPlotPane.add(legendBox, BorderLayout.EAST);

		mainPanel.addTab("PV List", pvPane);
		mainPanel.addTab("Chart", pvPlotPane);

		getContentPane().add(mainPanel);

		textField = new JTextField();
		getContentPane().add(textField, "South");

		addPV.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addPV();
				textField.setText("adding PV: " + pvText.getText()
						+ " to the list.");
				setupPVPanels();
				pvText.setText("");
				myDocument.setHasChanges(true);
				oldDocInd = false;
			}
		});

		removeAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeAllPVs();
			}
		});

	}

	public JTextField getTextField() {
		return textField;
	}

	public void setupPVPanels() {

		pvTSPane.removeAll();
		legendBox.removeAll();
		legendBox.add(new JLabel("PVs:"));

		pvPanels = new PVPanel[nPV];
		yRange = new HashMap<String, JTextField>(nPV);
		configYDialogs = new JDialog[nPV];
		mins = new DecimalField[nPV];
		maxs = new DecimalField[nPV];
		dMins = new double[nPV];
		dMaxs = new double[nPV];

		for (int i = 0; i < nPV; i++) {
			pvPanels[i] = new PVPanel( (pvList.get(i)));
			pvTSPane.add(pvPanels[i]);
		}

		pvTSPane.updateUI();
	}

	private void addPV() {
		String pvName = pvText.getText();
		pvList.add(pvName);
		nPV = pvList.size();
	}

	/**
	 * remove all the PV panels
	 */
	private void removeAllPVs() {
		myDocument.stopAllMonitors();
		pvTSPane.removeAll();
		pvTSPane.repaint();
		pvList.clear();
		getPlotPanel().removeAllGraphData();
		myDocument.cam = null;
	}

	public ArrayList<String> getPVList() {
		return pvList;
	}

	public PVPanel[] getPVPanels() {
		return pvPanels;
	}

	public void setPVList(ArrayList<String> list) {
		pvList = list;
	}

	protected FunctionGraphsJPanel getPlotPanel() {
		return pvPlot;
	}

	protected int getDisplayTimeRange() {
		return timeInSeconds;
	}

	protected int getBufferSize() {
		return buffSize;
	}

	protected void setBufferSize(int size) {
		buffSize = size;
		df1.setValue(buffSize);
	}

	protected int getTimeRange() {
		return timeInSeconds;
	}

	protected void setTimeRange(int range) {
		timeInSeconds = range;

		double min = timeInSeconds / 60;
		double sec = timeInSeconds - Math.round(min) * 60;
		double hour = min / 60.;
		min = min - Math.round(hour) * 60;

		dsec.setValue(sec);
		dmin.setValue(Math.round(min));
		dhr.setValue(Math.round(hour));
	}

	protected void restore() {
		oldDocInd = true;

		nPV = pvList.size();
		setupPVPanels();
	}

	protected void addLegendButton(String pv, int i) {
		JButton confPV = new JButton(pv);
		final int j = i;
		// add action for the PV configuration button
		confPV.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				configYDialogs[j].setVisible(true);
			}
		});

		legendBox.add(confPV);
		BasicGraphData bgd = myDocument.getCAMonitors()[i].getGraphData();
		Color col = colors[i - (i / colors.length)];
		// assign curve color
		bgd.setGraphColor(col);
		// assign config button the same color as the curve's
		confPV.setForeground(col);

		JTextField rangeDisplay = new JTextField(20);
		rangeDisplay.setPreferredSize(new Dimension(100, 15));
		legendBox.add(rangeDisplay);
		yRange.put(pv, rangeDisplay);

		legendBox.revalidate();
	}

	protected HashMap<String, JTextField> getYRangeField() {
		return yRange;
	}

	protected void setConfigYDialog(int i) {
		configYDialogs[i] = new JDialog();
		configYDialogs[i].setBounds(300, 300, 300, 300);
		configYDialogs[i].setTitle("Y-axis Scaling");
		EdgeLayout edgeLayout1 = new EdgeLayout();
		configYDialogs[i].getContentPane().setLayout(edgeLayout1);

		Box configMin = new Box(BoxLayout.X_AXIS);
		JLabel minLabel = new JLabel("Y-axis min = ");
		//		numberFormat.setMaximumFractionDigits(0);
		mins[i] = new DecimalField(dMins[i], 8, numberFormat);
		configMin.add(minLabel);
		configMin.add(mins[i]);
		Box configMax = new Box(BoxLayout.X_AXIS);
		JLabel maxLabel = new JLabel("Y-axis max = ");
		//		numberFormat.setMaximumFractionDigits(0);
		maxs[i] = new DecimalField(dMaxs[i], 8, numberFormat);
		configMax.add(maxLabel);
		configMax.add(maxs[i]);

		JPanel paramConfBtn = new JPanel();
		EdgeLayout edgeLayout3 = new EdgeLayout();
		paramConfBtn.setLayout(edgeLayout3);
		JButton done = new JButton("OK");
		final int j = i;
		done.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// set min.
				dMins[j] = mins[j].getDoubleValue();
				// set max
				dMaxs[j] = maxs[j].getDoubleValue();
				// update plot settings
				if (myDocument.cam != null) {
					if (dMins[j] != myDocument.cam[j].getYMin()
							|| dMaxs[j] != myDocument.cam[j].getYMax()) {
						if (!scaleChanged) {
							globalMin = pvPlot.getCurrentMinY();
							globalMax = pvPlot.getCurrentMaxY();
							scaleChanged = true;
							pvPlot.setLimitsAndTicksY(globalMin, globalMax, (globalMax-globalMin)/5.);
						}
						myDocument.cam[j].setDisplayLimits(dMins[j], dMaxs[j],
								globalMin, globalMax);
						//						myDocument.setHasChanges(true);
					}
				}

				configYDialogs[j].setVisible(false);
			}
		});
		edgeLayout3.setConstraints(done, 0, 50, 0, 0, EdgeLayout.LEFT_BOTTOM,
				EdgeLayout.NO_GROWTH);
		paramConfBtn.add(done);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				configYDialogs[j].setVisible(false);
			}
		});
		edgeLayout3.setConstraints(cancel, 0, 170, 0, 0,
				EdgeLayout.LEFT_BOTTOM, EdgeLayout.NO_GROWTH);
		paramConfBtn.add(cancel);

		edgeLayout1.setConstraints(configMin, 10, 30, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		configYDialogs[i].getContentPane().add(configMin);
		edgeLayout1.setConstraints(configMax, 50, 30, 0, 0, EdgeLayout.TOP,
				EdgeLayout.NO_GROWTH);
		configYDialogs[i].getContentPane().add(configMax);
		edgeLayout1.setConstraints(paramConfBtn, 0, 0, 10, 0,
				EdgeLayout.BOTTOM, EdgeLayout.NO_GROWTH);
		configYDialogs[i].getContentPane().add(paramConfBtn);
		
	}
	
	protected void addResetButton() {
		resetAllScales.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
//				pvPlot.setExternalGL(null);
				for (int i=0; i<myDocument.cam.length; i++) {
//					myDocument.cam[i].getGraphData().removeAllPoints();
					myDocument.cam[i].resetDisplayLimits();
				}
//				pvPlot.setLimitsAndTicksY(globalMin, globalMax, (globalMax-globalMin)/5.);
			}
		});
		legendBox.add(resetAllScales);
	}
}
