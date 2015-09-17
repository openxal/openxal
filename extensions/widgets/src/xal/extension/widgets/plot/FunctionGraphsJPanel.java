package xal.extension.widgets.plot;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.image.*;
import java.text.*;
import java.awt.event.*;

/**
 *  This is the base class of the plotting package. It is a sub-class of JPanel
 *  and displays all graphics
 *
 *@author     shishlo, tap
 *@version    1.0
 */
public class FunctionGraphsJPanel extends JPanel implements MouseListener, MouseMotionListener {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    

	/**
	 *  The static field that defines HORIZONTAL line on a graph panel
	 */
	public static int HORIZONTAL = 0;

	/**
	 *  The static field that defines Vertical line on a graph panel
	 */
	public static int VERTICAL = 1;

	private Vector<BasicGraphData> graphDataV = new Vector<BasicGraphData>();
	private Vector<Color> graphColorV = new Vector<Color>();
	private Vector<CurveData> curveDataV = new Vector<CurveData>();
	private ColorSurfaceData colorSurfaceData = null;

	private int nTotalGraphPoints = 0;
	private int nTotalCurvePoints = 0;

	private Color bkgGraphAreaColor = null;
	private Color bkgBorderAreaColor = null;

	private Color gridLineColor = null;

	private Color lineDefaultColor = Color.black;
	private Color lineChoosenColor = Color.red;

	private String nameOfGraph = null;
	private String nameX = null;
	private String nameY = null;

	private Font nameOfGraphFont = null;
	private Font nameXFont = null;
	private Font nameYFont = null;
	private Font numberFont = null;

	private Color nameOfGraphColor = Color.black;
	private Color nameXColor = Color.black;
	private Color nameYColor = Color.black;
	private Color numberColor = Color.black;

	private NumberFormat numberFormatX = new DecimalFormat("0.00E0");
	private NumberFormat numberFormatY = new DecimalFormat("0.00E0");

	private boolean gridLineOnX = true;
	private boolean gridLineOnY = true;

	private boolean gridXmarkerOn = true;
	private boolean gridYmarkerOn = true;

	//scale factors for numbers at the ticks. They are changing the presentation on the screen only
	private double numbMarkScaleX = 1.0;
	private double numbMarkScaleY = 1.0;

	// Vertical and horizontal lines (vector includes Double with y and x coordinates)
	private Vector<Double> vLinesV = new Vector<Double>();
	private Vector<Double> hLinesV = new Vector<Double>();
	private Vector<Color> vLinesColorV = new Vector<Color>();
	private Vector<Color> hLinesColorV = new Vector<Color>();

	private Color defaultVerticalLineColor = Color.cyan;
	private Color defaultHorizontLineColor = Color.cyan;

	//grid limits instances
	private GridLimits innerGridLimits = new SmartGridLimits();
	private boolean useSmartGridLimits = true;
	private GridLimits externalGridLimits = null;
	private Vector<GridLimits> zoomGridLimitsV = new Vector<GridLimits>();

	//off screen image
	private Image offScreenImage_ = null;
	private boolean offScreenImageOn = true;

	//data that are used as temporary for drawing
	private double scaleX = 0.;
	private double scaleY = 0.;

	private int xAxisLength = 0;
	private int yAxisLength = 0;

	private double xMin = 0.;
	private double yMin = 0.;
	private double xMax = 0.;
	private double yMax = 0.;

	private int xLOffSet = 0;
	private int xROffSet = 0;
	private int yUOffSet = 0;
	private int yBOffSet = 0;

	private int screenW = 0;
	private int screenH = 0;

	private int fSizeName = 0;
	private int fSizeX = 0;
	private int fSizeY = 0;

	private int fSizeNumbXhor = 0;
	private int fSizeNumbXver = 0;
	private int fSizeNumbYhor = 0;
	private int fSizeNumbYver = 0;

	//point selected by mouse
	private ClickedPoint clickedPoint = new ClickedPoint();

	private int evntIniX;
	private int evntIniY;
	private boolean mouseDrugged = false;
	private int mouseUsedButton = 0;

	//right now there are four types of task
	//0 - zoom
	//1 - horizontal lines dragging
	//2 - vertical lines dragging
	//3 - legend dragging
	private int mouseDraggedTaskType = -1;

	//dialog related members
	private JDialog axisDialog = null;
	private Object parentFrameOrDialog = null;
	private gridLimitsPanel glPanel = new gridLimitsPanel();

	//graph choosing mode
	private boolean graphChoosingYes = false;
	private boolean graphChosenYes = false;
	private int graphChosenIndex = 0;
	private int graphPointChosenIndex = 0;
	private double choosenX = 0.;
	private double choosenY = 0.;
	private JRadioButton chooseModeButton = new JRadioButton("S", false);
	private boolean chooseModeButtonVisible = true;
	private ActionListener chooseListener = null;

	//graph dragging vertical and horizontal lines mode
	private boolean dragHorLinesModeYes = false;
	private boolean dragVerLinesModeYes = false;
	private JRadioButton dragHorLinesModeButton = new JRadioButton("Y", false);
	private JRadioButton dragVerLinesModeButton = new JRadioButton("X", false);
	private boolean horLinesModeButtonVisible = true;
	private boolean verLinesModeButtonVisible = true;
	private int draggedLinesIndex;
	private ActionListener draggedHorLinesListener = null;
	private ActionListener draggedVerLinesListener = null;
	private ActionEvent draggedHorLinesEvent = null;
	private ActionEvent draggedVerLinesEvent = null;
	private boolean draggedHorLinesMotionListenYes = false;
	private boolean draggedVerLinesMotionListenYes = false;
	private Polygon triangleMarkerLeft;
	private Polygon triangleMarkerRight;

	//Change in VERTICAL and HORIZONTAL limits listeners
	private Vector<ActionListener> horLimListenersV = new Vector<ActionListener>();
	private Vector<ActionListener> verLimListenersV = new Vector<ActionListener>();
	private ActionEvent horLimEvent = null;
	private ActionEvent verLimEvent = null;

	//Legend
	private graphLegend legend;
	private JRadioButton legendButton = new JRadioButton("L", false);
	private boolean legendButtonVisible = true;
	private String legendKeyString = "Legend";

	/**
	 *  The constant defining the legend position at the arbitrary place of the
	 *  graph panel
	 */
	public static final int LEGEND_POSITION_ARBITRARY = 0;

	/**
	 *  The constant defining the legend position at the top left corner of the
	 *  graph panel
	 */
	public static final int LEGEND_POSITION_TOP_LEFT = 1;

	/**
	 *  The constant defining the legend position at the top right corner of the
	 *  graph panel
	 */
	public static final int LEGEND_POSITION_TOP_RIGHT = 2;

	/**
	 *  The constant defining the legend position at the bottom left corner of the
	 *  graph panel
	 */

	public static final int LEGEND_POSITION_BOTTOM_LEFT = 3;
	/**
	 *  The constant defining the legend position at the bottom right corner of the
	 *  graph panel
	 */
	public static final int LEGEND_POSITION_BOTTOM_RIGHT = 4;


	/**
	 *  Constructor for the FunctionGraphsJPanel object
	 */
	public FunctionGraphsJPanel() {
		this.initialSettings();
	}


	/**
	 *  Performs initial settings
	 */
	private void initialSettings() {

		//this two lines do nothing but we need them to get fonts
		GraphicsEnvironment g_env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		g_env.getAvailableFontFamilyNames();

		nameOfGraphFont = this.getFont();
		nameXFont = this.getFont();
		nameYFont = this.getFont();
		numberFont = this.getFont();

		gridXmarkerOn = true;
		gridYmarkerOn = true;

		bkgGraphAreaColor = getBackground();
		bkgBorderAreaColor = getBackground();

		addMouseListener(this);
		addMouseMotionListener(this);

		clickedPoint.xValueText.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
		clickedPoint.yValueText.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
		clickedPoint.zValueText.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
		clickedPoint.xValueLabel.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
		clickedPoint.yValueLabel.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
		clickedPoint.zValueLabel.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));

		glPanel.setFunctionGraphsJPanel(this);

		innerGridLimits.setGridLimitsSwitch(true);
		innerGridLimits.initialize();

		//ActionEvents for VERTICAL and HORIZONTAL limits
		horLimEvent = new ActionEvent(this, HORIZONTAL, "changed");
		verLimEvent = new ActionEvent(this, VERTICAL, "changed");

		//legend
		legend = new graphLegend(this);
		legend.setKeyString(legendKeyString);
		legend.setVisible(legendButton.isSelected());
		legend.POSITION_ARBITRARY = LEGEND_POSITION_ARBITRARY;
		legend.POSITION_TOP_LEFT = LEGEND_POSITION_TOP_LEFT;
		legend.POSITION_TOP_RIGHT = LEGEND_POSITION_TOP_RIGHT;
		legend.POSITION_BOTTOM_LEFT = LEGEND_POSITION_BOTTOM_LEFT;
		legend.POSITION_BOTTOM_RIGHT = LEGEND_POSITION_BOTTOM_RIGHT;

		//buttons
		chooseModeButton.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
		chooseModeButton.setToolTipText("Selection Mode");
		chooseModeButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (chooseModeButton.isSelected()) {
						setChoosingGraphMode();
					} else {
						setDisplayGraphMode();
					}
				}
			});

		dragHorLinesModeButton.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
		dragHorLinesModeButton.setToolTipText("Dragging Horizontal Lines Mode");
		dragHorLinesModeButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (dragHorLinesModeButton.isSelected()) {
						setDraggingHorLinesGraphMode(true);
					} else {
						setDraggingHorLinesGraphMode(false);
					}
				}
			});

		dragVerLinesModeButton.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
		dragVerLinesModeButton.setToolTipText("Dragging Vertical Lines Mode");
		dragVerLinesModeButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (dragVerLinesModeButton.isSelected()) {
						setDraggingVerLinesGraphMode(true);
					} else {
						setDraggingVerLinesGraphMode(false);
					}
				}
			});

		legendButton.setFont(new Font(this.getFont().getFamily(), Font.BOLD, 10));
		legendButton.setToolTipText("Legend");
		legendButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (legendButton.isSelected()) {
						setLegendVisible(true);
					} else {
						setLegendVisible(false);
					}
				}
			});

		setLayout(null);
		add(chooseModeButton);
		add(dragHorLinesModeButton);
		add(dragVerLinesModeButton);
		add(legendButton);

		draggedHorLinesEvent = new ActionEvent(this, HORIZONTAL, "dragging");
		draggedVerLinesEvent = new ActionEvent(this, VERTICAL, "dragging");

		int[] triangleX = new int[3];
		int[] triangleY = new int[3];
		triangleX[0] = -7;
		triangleY[0] = -5;
		triangleX[1] = -7;
		triangleY[1] = 5;
		triangleX[2] = 0;
		triangleY[2] = 0;
		triangleMarkerLeft = new Polygon(triangleX, triangleY, 3);
		triangleMarkerLeft.invalidate();
		triangleX[0] = -5;
		triangleY[0] = 7;
		triangleX[1] = 5;
		triangleY[1] = 7;
		triangleX[2] = 0;
		triangleY[2] = 0;
		triangleMarkerRight = new Polygon(triangleX, triangleY, 3);
		triangleMarkerRight.invalidate();

		//-----------------------------------------------
		//last adjustments
		//-----------------------------------------------

		//set legend button invisible
		setLegendButtonVisible(false);
		setChooseModeButtonVisible(false);
		setHorLinesButtonVisible(false);
		setVerLinesButtonVisible(false);
	}


	/**
	 *  Adds a BasicGraphData instance to the graph panel
	 *
	 *@param  lgd  The BasicGraphData instance
	 *@return      The index of this new data set in the internal array of data
	 *      references
	 */
	public synchronized int addGraphData(BasicGraphData lgd) {
		synchronized (lgd) {
			graphDataV.add(lgd);
			if (graphChoosingYes) {
				graphColorV.add(lineDefaultColor);
			} else {
				graphColorV.add(null);
			}
			lgd.registerInContainer( this );
			updateData();
			return graphDataV.size() - 1;
		}
	}


	/**
	 *  Removes a BasicGraphData instance with a particular index from the graph
	 *  panel
	 *
	 *@param  index  The index of this data set in the graph panel
	 */
	public synchronized void removeGraphData(int index) {
		synchronized (graphDataV) {
			synchronized (graphColorV) {
				if (index < graphDataV.size()) {
					BasicGraphData lgd = graphDataV.get(index);
					lgd.removeContainer(this);
					graphDataV.remove(index);
					graphColorV.remove(index);
					updateData();
				}
			}
		}
	}


	/**
	 *  Removes a BasicGraphData instance from the graph panel
	 *
	 *@param  gd  The BasicGraphData instance
	 */
	public synchronized void removeGraphData(BasicGraphData gd) {
		synchronized (graphDataV) {
			synchronized (graphColorV) {
				int index = graphDataV.indexOf(gd);
				if (index < 0) {
					return;
				}
				removeGraphData(index);
			}
		}
	}


	/**
	 *  Adds all BasicGraphData instances in the vector to the graph panel. All
	 *  previous data will remain on the graph panel.
	 *
	 *@param  gdV  The vector with BasicGraphData instances
	 */
	public synchronized void addGraphData( final Vector<? extends BasicGraphData> gdV ) {
		synchronized (gdV) {
			synchronized (graphDataV) {
				synchronized (graphColorV) {
					for ( final BasicGraphData lgd : gdV ) {
						if ( graphChoosingYes ) {
							graphColorV.add( lineDefaultColor );
						}
						else {
							graphColorV.add( null );
						}
						graphDataV.add( lgd );
						lgd.registerInContainer( this );
					}
					updateData();
				}
			}
		}
	}


	/**
	 *  Sets all BasicGraphData instances in the vector to the graph panel. All
	 *  previous data will be removed from the graph panel
	 *
	 *@param  gdV  The vector with BasicGraphData instances
	 */
	public synchronized void setGraphData( final Vector<? extends BasicGraphData> gdV ) {
		synchronized (gdV) {
			synchronized (graphDataV) {
				synchronized (graphColorV) {
					for (int i = 0, n = graphDataV.size(); i < n; i++) {
						final BasicGraphData lgd = graphDataV.get(i);
						lgd.removeContainer(this);
					}

					graphDataV.clear();
					graphColorV.clear();
					
					for ( final BasicGraphData lgd : gdV ) {
						if (graphChoosingYes) {
							graphColorV.add(lineDefaultColor);
						} else {
							graphColorV.add(null);
						}
						graphDataV.add(lgd);
						lgd.registerInContainer(this);
					}
					updateData();
				}
			}
		}
	}


	/**
	 *  Removes all BasicGraphData instances in the vector from the graph panel
	 *
	 *@param  gdV  The vector with BasicGraphData instances
	 */
	public synchronized void removeGraphData( final Vector<? extends BasicGraphData> gdV ) {
		synchronized (graphDataV) {
			synchronized (graphColorV) {
				for (int i = 0, nGDV = gdV.size(); i < nGDV; i++) {
					BasicGraphData lgd = gdV.get(i);
					int index = graphDataV.indexOf(lgd);
					if (index < 0) {
						continue;
					}
					lgd = graphDataV.get(index);
					lgd.removeContainer(this);
					graphDataV.remove(index);
					graphColorV.remove(index);
				}
				updateData();
			}
		}
	}


	/**
	 *  Removes all BasicGraphData instances from the graph panel
	 */
	public synchronized void removeAllGraphData() {
		synchronized (graphDataV) {
			synchronized (graphColorV) {
				for (int i = 0, n = graphDataV.size(); i < n; i++) {
					BasicGraphData lgd = graphDataV.get(i);
					lgd.removeContainer(this);
				}
				graphDataV.clear();
				graphColorV.clear();
				updateData();
			}
		}
	}


	/**
	 *  Returns the reference to BasicGraphData object with a particular index
	 *
	 *@param  index  The index of BasicGraphData object inside the graph panel
	 *@return        The reference to BasicGraphData object
	 */
	public BasicGraphData getInstanceOfGraphData(int index) {
		if (graphDataV.size() > index) {
			return graphDataV.get(index);
		}
		return null;
	}


	/**
	 *  Returns the vector with refernces to all BasicGraphData objects on this
	 *  graph panel
	 *
	 *@return    The vector with refernces to all BasicGraphData objects
	 */
	public Vector<BasicGraphData> getAllGraphData() {
		Vector<BasicGraphData> tmp = new Vector<BasicGraphData>();
		synchronized (graphDataV) {
			for (int i = 0; i < graphDataV.size(); i++) {
				tmp.add(graphDataV.get(i));
			}
		}
		return tmp;
	}


	/**
	 *  Returns the number of BasicGraphData objects on this graph panel
	 *
	 *@return    The number of BasicGraphData objects
	 */
	public int getNumberOfInstanceOfGraphData() {
		return graphDataV.size();
	}


	/**
	 *  Returns the total number of points in the all BasicGraphData objects
	 *
	 *@return    The total number of points in the all BasicGraphData objects on
	 *      this panel
	 */
	public int getNumbTotalGraphPoints() {
		return nTotalGraphPoints;
	}


	//----------------------------------------------
	//Methods related to the colored surface data
	//(contour plot)
	//----------------------------------------------

	/**
	 *  Sets ColorSurfaceData object that will be plotted on the graph panel
	 *
	 *@param  colorSurfaceData  New ColorSurfaceData object
	 */
	public void setColorSurfaceData(ColorSurfaceData colorSurfaceData) {
		this.colorSurfaceData = colorSurfaceData;
		updateData();
	}


	/**
	 *  Returns the reference to ColorSurfaceData object that is plotted on the
	 *  graph panel
	 *
	 *@return    The reference to ColorSurfaceData object currently plotted on the
	 *      graph panel
	 */
	public ColorSurfaceData getColorSurfaceData() {
		return colorSurfaceData;
	}


	//----------------------------------------------
	//Methods related to the curve data
	//----------------------------------------------

	/**
	 *  Returns the curveData attribute of the FunctionGraphsJPanel object
	 *
	 *@param  i  Description of the Parameter
	 *@return    The curveData value
	 */
	public CurveData getCurveData(int i) {
		return curveDataV.get(i);
	}


	/**
	 *  Returns the allCurveData attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The allCurveData value
	 */
	public Vector<CurveData> getAllCurveData() {
		return new Vector<CurveData>(curveDataV);
	}


	/**
	 *  Adds a feature to the CurveData attribute of the FunctionGraphsJPanel object
	 *
	 *@param  curveData  The feature to be added to the CurveData attribute
	 */
	public void addCurveData(CurveData curveData) {
		curveDataV.add(curveData);
		updateData();
	}


	/**
	 *  Adds a feature to the CurveData attribute of the FunctionGraphsJPanel object
	 *
	 *@param  cdV  The feature to be added to the CurveData attribute
	 */
	public void addCurveData( final Vector<? extends CurveData> cdV ) {
		for ( final CurveData cd : cdV ) {
			if (cd != null) {
				curveDataV.add(cd);
			}
		}
		updateData();
	}


	/**
	 *  Sets the curveData attribute of the FunctionGraphsJPanel object
	 *
	 *@param  cdV  The new curveData value
	 */
	public void setCurveData( final Vector<? extends CurveData> cdV ) {
		curveDataV.clear();
		for ( final CurveData cd : cdV ) {
			if (cd != null) {
				curveDataV.add(cd);
			}
		}
		updateData();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  i  Description of the Parameter
	 */
	public void removeCurveData(int i) {
		if (i < curveDataV.size()) {
			curveDataV.remove(i);
			updateData();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  curveData  Description of the Parameter
	 */
	public void removeCurveData(CurveData curveData) {
		curveDataV.remove(curveData);
		updateData();
	}


	/**
	 *  Description of the Method
	 */
	public void removeAllCurveData() {
		curveDataV.clear();
		updateData();
	}


	//----------------------------------------------
	//----------------------------------------------

	/**
	 *  Sets the graphsDefaultColor attribute of the FunctionGraphsJPanel object
	 *
	 *@param  color  The new graphsDefaultColor value
	 */
	public void setGraphsDefaultColor(Color color) {
		lineDefaultColor = color;
		updateGraphJPanel();
	}


	/**
	 *  Sets the graphLineChoosenColor attribute of the FunctionGraphsJPanel object
	 *
	 *@param  color  The new graphLineChoosenColor value
	 */
	public void setGraphLineChoosenColor(Color color) {
		lineChoosenColor = color;
		updateGraphJPanel();
	}


	/**
	 *  Returns the graphsDefaultColor attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The graphsDefaultColor value
	 */
	public Color getGraphsDefaultColor() {
		return lineDefaultColor;
	}


	/**
	 *  Description of the Method
	 */
	public void resetGraphsDefaultColor() {
		for (int i = 0, n = graphColorV.size(); i < n; i++) {
			graphColorV.set(i, lineDefaultColor);
		}
		updateGraphJPanel();
	}


	/**
	 *  Description of the Method
	 */
	public void removeColorForAllGraphs() {
		for (int i = 0, n = graphColorV.size(); i < n; i++) {
			graphColorV.set(i, null);
		}
		updateGraphJPanel();
	}


	/**
	 *  Returns the graphColor attribute of the FunctionGraphsJPanel object
	 *
	 *@param  index  Description of the Parameter
	 *@return        The graphColor value
	 */
	public Color getGraphColor(int index) {
		if (graphColorV.size() > index) {
			return graphColorV.get(index);
		}
		return null;
	}


	/**
	 *  Sets the graphColor attribute of the FunctionGraphsJPanel object
	 *
	 *@param  index  The new graphColor value
	 *@param  color  The new graphColor value
	 *@return        Description of the Return Value
	 */
	public boolean setGraphColor(int index, Color color) {
		if (graphColorV.size() > index) {
			graphColorV.set(index, color);
			updateGraphJPanel();
			return true;
		}
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  gridXmarkerOnIn  Description of the Parameter
	 */
	public synchronized void xMarkersOn(boolean gridXmarkerOnIn) {
		gridXmarkerOn = gridXmarkerOnIn;
		updateGraphJPanel();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  gridYmarkerOnIn  Description of the Parameter
	 */
	public synchronized void yMarkersOn(boolean gridYmarkerOnIn) {
		gridYmarkerOn = gridYmarkerOnIn;
		updateGraphJPanel();
	}


	//--------------------------------------------------
	//method related to the off screen image drawing
	//--------------------------------------------------

	/**
	 *  Sets the offScreenImageDrawing attribute of the FunctionGraphsJPanel object
	 *
	 *@param  offScreenImageOnIn  The new offScreenImageDrawing value
	 */
	public synchronized void setOffScreenImageDrawing(boolean offScreenImageOnIn) {
		offScreenImageOn = offScreenImageOnIn;
	}


	//--------------------------------------------------
	//methods related to the graph choosing
	//--------------------------------------------------

	/**
	 *  Sets the displayGraphMode attribute of the FunctionGraphsJPanel object
	 */
	public void setDisplayGraphMode() {
		graphChoosingYes = false;
		graphChosenYes = false;
		synchronized (graphDataV) {
			synchronized (graphColorV) {
				for (int i = 0, n = graphColorV.size(); i < n; i++) {
					graphColorV.set(i, null);
				}
			}
		}
		clickedPoint.setDisplayed(false);
		updateGraphJPanel();
		chooseModeButton.setSelected(graphChoosingYes);
	}


	/**
	 *  Sets the choosingGraphMode attribute of the FunctionGraphsJPanel object
	 */
	public void setChoosingGraphMode() {
		graphChoosingYes = true;
		graphChosenYes = false;
		synchronized (graphDataV) {
			synchronized (graphColorV) {
				for (int i = 0, n = graphColorV.size(); i < n; i++) {
					graphColorV.set(i, lineDefaultColor);
				}
			}
		}
		clickedPoint.setDisplayed(false);
		updateGraphJPanel();
		chooseModeButton.setSelected(graphChoosingYes);
	}


	/**
	 *  Sets the chooseModeButtonVisible attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@param  vs  The new chooseModeButtonVisible value
	 */
	public void setChooseModeButtonVisible(boolean vs) {
		chooseModeButtonVisible = vs;
		remove(chooseModeButton);
		if (vs) {
			add(chooseModeButton);
			chooseModeButton.setSelected(graphChoosingYes);
		}
		updateGraphJPanel();
	}


	/**
	 *  Description of the Method
	 */
	private void unChooseGraph() {
		graphChosenYes = false;
		clickedPoint.setDisplayed(false);
		if (!graphChoosingYes) {
			return;
		}
		synchronized (graphDataV) {
			synchronized (graphColorV) {
				for (int i = 0, n = graphColorV.size(); i < n; i++) {
					graphColorV.set(i, lineDefaultColor);
				}
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  iX  Description of the Parameter
	 *@param  iY  Description of the Parameter
	 *@return     Description of the Return Value
	 */
	private boolean chooseGraphFromLegend(int iX, int iY) {
		if (!graphChoosingYes) {
			return false;
		}
		synchronized (graphDataV) {
			graphChosenYes = false;
			graphChosenIndex = -1;
			graphPointChosenIndex = -1;
			Integer Ind = legend.getChoosenGraphIndex(iX, iY);
			if (Ind != null) {
				graphChosenYes = true;
				graphChosenIndex = Ind.intValue();
				for (int i = 0, n = graphColorV.size(); i < n; i++) {
					graphColorV.set(i, lineDefaultColor);
				}
				graphColorV.set(graphChosenIndex, lineChoosenColor);
				graphChosenYes = true;
				clickedPoint.setDisplayed(false);
				if (chooseListener != null) {
					ActionEvent evnt = new ActionEvent(this, 0, "chosen");
					chooseListener.actionPerformed(evnt);
				}
				return true;
			}
		}
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  x  Description of the Parameter
	 *@param  y  Description of the Parameter
	 */
	private void chooseGraph(double x, double y) {
		if (!graphChoosingYes) {
			return;
		}
		boolean success = false;
		synchronized (graphDataV) {
			BasicGraphData gd;
			double minDist = Double.MAX_VALUE;
			double dist = Double.MAX_VALUE;
			double xG;
			double yG;
			double xPos = 0.;
			double yPos = 0.;
			double xCurrMin = getCurrentMinX();
			double yCurrMin = getCurrentMinY();
			double xCurrMax = getCurrentMaxX();
			double yCurrMax = getCurrentMaxY();
			for (int i = 0, ni = graphDataV.size(); i < ni; i++) {
				gd = graphDataV.get(i);
				synchronized (gd) {
					for (int j = 0, nj = gd.getNumbOfPoints(); j < nj; j++) {
						xG = gd.getX(j);
						yG = gd.getY(j);
						if (xG < xCurrMin || xG > xCurrMax ||
								yG < yCurrMin || yG > yCurrMax) {
							continue;
						}
						xG = xG - x;
						yG = yG - y;
						dist = xG * xG + yG * yG;
						if (dist < minDist) {
							graphChosenIndex = i;
							graphPointChosenIndex = j;
							xPos = xG + x;
							yPos = yG + y;
							success = true;
							minDist = dist;
						}
					}
				}
			}
			if (success) {
				for (int i = 0, n = graphColorV.size(); i < n; i++) {
					graphColorV.set(i, lineDefaultColor);
				}
				graphColorV.set(graphChosenIndex, lineChoosenColor);
				graphChosenYes = true;
				choosenX = xPos;
				choosenY = yPos;
				if (colorSurfaceData == null) {
					clickedPoint.updateValues(xPos, yPos);
				} else {
					clickedPoint.updateValues(xPos, yPos, colorSurfaceData.getValue(xPos, yPos));
				}
				clickedPoint.setDisplayed(true);
				if (chooseListener != null) {
					ActionEvent evnt = new ActionEvent(this, 0, "chosen");
					chooseListener.actionPerformed(evnt);
				}
			}
		}
	}


	/**
	 *  Returns the graphChosenIndex attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The graphChosenIndex value
	 */
	public Integer getGraphChosenIndex() {
		if (graphChosenYes) {
			if (graphChosenIndex >= 0) {
				return new Integer(graphChosenIndex);
			}
		}
		return null;
	}


	/**
	 *  Returns the pointChosenIndex attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The pointChosenIndex value
	 */
	public Integer getPointChosenIndex() {
		if (graphChosenYes) {
			if (graphPointChosenIndex >= 0 && graphChosenIndex >= 0) {
				return new Integer(graphPointChosenIndex);
			}
		}
		return null;
	}


	/**
	 *  Returns the pointChosenX attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The pointChosenX value
	 */
	public Double getPointChosenX() {
		if (graphChosenYes) {
			if (graphPointChosenIndex >= 0 && graphChosenIndex >= 0) {
				return new Double(choosenX);
			}
		}
		return null;
	}


	/**
	 *  Returns the pointChosenY attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The pointChosenY value
	 */
	public Double getPointChosenY() {
		if (graphChosenYes) {
			if (graphPointChosenIndex >= 0 && graphChosenIndex >= 0) {
				return new Double(choosenY);
			}
		}
		return null;
	}


	/**
	 *  Adds a feature to the ChooseListener attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@param  al  The feature to be added to the ChooseListener attribute
	 */
	public void addChooseListener(ActionListener al) {
		chooseListener = al;
	}


	//--------------------------------------------------
	//methods related to the grid limits
	//--------------------------------------------------
	/**
	 *  Returns the newGridLimits attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The newGridLimits value
	 */
	public GridLimits getNewGridLimits() {
		return new GridLimits();
	}


	/**
	 *  Returns the currentGL attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The currentGL value
	 */
	public synchronized GridLimits getCurrentGL() {
		if (zoomGridLimitsV.size() == 0) {
			if (externalGridLimits == null) {
				externalGridLimits = new GridLimits();
				externalGridLimits.setNumberFormatX(numberFormatX);
				externalGridLimits.setNumberFormatY(numberFormatY);
				externalGridLimits.setGridLimitsSwitch(true);
				return externalGridLimits;
			} else {
				return externalGridLimits;
			}
		} else {
			return zoomGridLimitsV.lastElement();
		}
	}


	/**
	 *  Sets the GridLimits object to the graph panel as an external grid limits
	 *
	 *@param  GL  The GridLimits object
	 */
	public void setExternalGL(GridLimits GL) {
		externalGridLimits = GL;
		updateGraphJPanel();
	}


	/**
	 *  Sets the boolean value that defines if the smart (slow one) GridLimits
	 *  object will be used as internal GL manager for the graph panel
	 *
	 *@param  smart  The boolean value
	 */
	public void setSmartGL(boolean smart) {
		useSmartGridLimits = smart;
		if (smart) {
			innerGridLimits = new SmartGridLimits();
		} else {
			innerGridLimits = new GridLimits();
		}
		refreshGraphJPanel();
	}


	/**
	 *  Returns the external GridLimits object
	 *
	 *@return    The external GL object
	 */
	public GridLimits getExternalGL() {
		return externalGridLimits;
	}


	/**
	 *  Returns the innerMinX attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The innerMinX value
	 */
	public double getInnerMinX() {
		return innerGridLimits.getMinX();
	}


	/**
	 *  Returns the innerMaxX attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The innerMaxX value
	 */
	public double getInnerMaxX() {
		return innerGridLimits.getMaxX();
	}


	/**
	 *  Returns the innerMinY attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The innerMinY value
	 */
	public double getInnerMinY() {
		return innerGridLimits.getMinY();
	}


	/**
	 *  Returns the innerMaxY attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The innerMaxY value
	 */
	public double getInnerMaxY() {
		return innerGridLimits.getMaxY();
	}


	/**
	 *  Returns the currentMinX attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The currentMinX value
	 */
	public double getCurrentMinX() {
		if (zoomGridLimitsV.size() == 0) {
			if (externalGridLimits == null || externalGridLimits.isSetXmin() == false) {
				return getInnerMinX();
			} else {
				return externalGridLimits.getMinX();
			}
		} else {
			GridLimits gl = zoomGridLimitsV.lastElement();
			if (gl.isSetXmin() == false) {
				return getInnerMinX();
			}
			return gl.getMinX();
		}
	}


	/**
	 *  Returns the currentMaxX attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The currentMaxX value
	 */
	public double getCurrentMaxX() {
		if (zoomGridLimitsV.size() == 0) {
			if (externalGridLimits == null || externalGridLimits.isSetXmax() == false) {
				return getInnerMaxX();
			} else {
				return externalGridLimits.getMaxX();
			}
		} else {
			GridLimits gl = zoomGridLimitsV.lastElement();
			if (gl.isSetXmax() == false) {
				return getInnerMaxX();
			}
			return gl.getMaxX();
		}
	}


	/**
	 *  Returns the currentMinY attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The currentMinY value
	 */
	public double getCurrentMinY() {
		if (zoomGridLimitsV.size() == 0) {
			if (externalGridLimits == null || externalGridLimits.isSetYmin() == false) {
				return getInnerMinY();
			} else {
				return externalGridLimits.getMinY();
			}
		} else {
			GridLimits gl = zoomGridLimitsV.lastElement();
			if (gl.isSetYmin() == false) {
				return getInnerMinY();
			}
			return gl.getMinY();
		}
	}


	/**
	 *  Returns the currentMaxY attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The currentMaxY value
	 */
	public double getCurrentMaxY() {
		if (zoomGridLimitsV.size() == 0) {
			if (externalGridLimits == null || externalGridLimits.isSetYmax() == false) {
				return getInnerMaxY();
			} else {
				return externalGridLimits.getMaxY();
			}
		} else {
			GridLimits gl = zoomGridLimitsV.lastElement();
			if (gl.isSetYmax() == false) {
				return getInnerMaxY();
			}
			return gl.getMaxY();
		}
	}


	//----------------------------------------------------------
	//clear the zoom stack
	//----------------------------------------------------------

	/**
	 *  Description of the Method
	 */
	public void clearZoomStack() {
		zoomGridLimitsV.clear();
		updateGraphJPanel();
	}


	//----------------------------------------------------------
	//convenience methods to define grid limits and ticks
	//----------------------------------------------------------

	/**
	 *  Sets the limitsAndTicksX attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vMin           The new limitsAndTicksX value
	 *@param  step           The new limitsAndTicksX value
	 *@param  nStep          The new limitsAndTicksX value
	 *@param  nMinorTicksIn  The new limitsAndTicksX value
	 */
	public synchronized void setLimitsAndTicksX(double vMin, double step, int nStep, int nMinorTicksIn) {
		if (externalGridLimits == null) {
			externalGridLimits = new GridLimits();
			externalGridLimits.setNumberFormatX(numberFormatX);
			externalGridLimits.setNumberFormatY(numberFormatY);
		}
		externalGridLimits.setLimitsAndTicksX(vMin, step, nStep, nMinorTicksIn);
		updateGraphJPanel();
	}


	/**
	 *  Sets the limitsAndTicksY attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vMin           The new limitsAndTicksY value
	 *@param  step           The new limitsAndTicksY value
	 *@param  nStep          The new limitsAndTicksY value
	 *@param  nMinorTicksIn  The new limitsAndTicksY value
	 */
	public synchronized void setLimitsAndTicksY(double vMin, double step, int nStep, int nMinorTicksIn) {
		if (externalGridLimits == null) {
			externalGridLimits = new GridLimits();
			externalGridLimits.setNumberFormatX(numberFormatX);
			externalGridLimits.setNumberFormatY(numberFormatY);
		}
		externalGridLimits.setLimitsAndTicksY(vMin, step, nStep, nMinorTicksIn);
		updateGraphJPanel();
	}


	/**
	 *  Sets the limitsAndTicksX attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vMin   The new limitsAndTicksX value
	 *@param  step   The new limitsAndTicksX value
	 *@param  nStep  The new limitsAndTicksX value
	 */
	public synchronized void setLimitsAndTicksX(double vMin, double step, int nStep) {
		if (externalGridLimits == null) {
			externalGridLimits = new GridLimits();
			externalGridLimits.setNumberFormatX(numberFormatX);
			externalGridLimits.setNumberFormatY(numberFormatY);
		}
		externalGridLimits.setLimitsAndTicksX(vMin, step, nStep);
		updateGraphJPanel();
	}


	/**
	 *  Sets the limitsAndTicksY attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vMin   The new limitsAndTicksY value
	 *@param  step   The new limitsAndTicksY value
	 *@param  nStep  The new limitsAndTicksY value
	 */
	public synchronized void setLimitsAndTicksY(double vMin, double step, int nStep) {
		if (externalGridLimits == null) {
			externalGridLimits = new GridLimits();
			externalGridLimits.setNumberFormatX(numberFormatX);
			externalGridLimits.setNumberFormatY(numberFormatY);
		}
		externalGridLimits.setLimitsAndTicksY(vMin, step, nStep);
		updateGraphJPanel();
	}


	/**
	 *  Sets the limitsAndTicksX attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vMin           The new limitsAndTicksX value
	 *@param  vMax           The new limitsAndTicksX value
	 *@param  step           The new limitsAndTicksX value
	 *@param  nMinorTicksIn  The new limitsAndTicksX value
	 */
	public synchronized void setLimitsAndTicksX(double vMin, double vMax, double step, int nMinorTicksIn) {
		if (externalGridLimits == null) {
			externalGridLimits = new GridLimits();
			externalGridLimits.setNumberFormatX(numberFormatX);
			externalGridLimits.setNumberFormatY(numberFormatY);
		}
		externalGridLimits.setLimitsAndTicksX(vMin, vMax, step, nMinorTicksIn);
		updateGraphJPanel();
	}


	/**
	 *  Sets the limitsAndTicksY attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vMin           The new limitsAndTicksY value
	 *@param  vMax           The new limitsAndTicksY value
	 *@param  step           The new limitsAndTicksY value
	 *@param  nMinorTicksIn  The new limitsAndTicksY value
	 */
	public synchronized void setLimitsAndTicksY(double vMin, double vMax, double step, int nMinorTicksIn) {
		if (externalGridLimits == null) {
			externalGridLimits = new GridLimits();
			externalGridLimits.setNumberFormatX(numberFormatX);
			externalGridLimits.setNumberFormatY(numberFormatY);
		}
		externalGridLimits.setLimitsAndTicksY(vMin, vMax, step, nMinorTicksIn);
		updateGraphJPanel();
	}


	/**
	 *  Sets the limitsAndTicksX attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vMin  The new limitsAndTicksX value
	 *@param  vMax  The new limitsAndTicksX value
	 *@param  step  The new limitsAndTicksX value
	 */
	public synchronized void setLimitsAndTicksX(double vMin, double vMax, double step) {
		if (externalGridLimits == null) {
			externalGridLimits = new GridLimits();
			externalGridLimits.setNumberFormatX(numberFormatX);
			externalGridLimits.setNumberFormatY(numberFormatY);
		}
		externalGridLimits.setLimitsAndTicksX(vMin, vMax, step);
		updateGraphJPanel();
	}


	/**
	 *  Sets the limitsAndTicksY attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vMin  The new limitsAndTicksY value
	 *@param  vMax  The new limitsAndTicksY value
	 *@param  step  The new limitsAndTicksY value
	 */
	public synchronized void setLimitsAndTicksY(double vMin, double vMax, double step) {
		if (externalGridLimits == null) {
			externalGridLimits = new GridLimits();
			externalGridLimits.setNumberFormatX(numberFormatX);
			externalGridLimits.setNumberFormatY(numberFormatY);
		}
		externalGridLimits.setLimitsAndTicksY(vMin, vMax, step);
		updateGraphJPanel();
	}


	//----------------------------------------------------------
	//methods to control the color
	//----------------------------------------------------------

	/**
	 *  Sets the graphBackGroundColor attribute of the FunctionGraphsJPanel object
	 *
	 *@param  bkgGraphAreaColor  The new graphBackGroundColor value
	 */
	public void setGraphBackGroundColor(Color bkgGraphAreaColor) {
		this.bkgGraphAreaColor = bkgGraphAreaColor;
		updateGraphJPanel();
	}



	/**
	 *  Sets the grid lines color of the FunctionGraphsJPanel. If it is null object
	 *  the color of grid lines will be darker than the background color.
	 *
	 *@param  gridLineColor  The new grid lines color
	 */
	public void setGridLineColor(Color gridLineColor) {
		this.gridLineColor = gridLineColor;
		updateGraphJPanel();
	}


	/**
	 *  Sets the borderBackGroundColor attribute of the FunctionGraphsJPanel object
	 *
	 *@param  bkgBorderAreaColor  The new borderBackGroundColor value
	 */
	public void setBorderBackGroundColor(Color bkgBorderAreaColor) {
		this.bkgBorderAreaColor = bkgBorderAreaColor;
		updateGraphJPanel();
	}


	/**
	 *  Returns the graphBackGroundColor attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@return    The graphBackGroundColor value
	 */
	public Color getGraphBackGroundColor() {
		return bkgGraphAreaColor;
	}


	/**
	 *  Returns the borderBackGroundColor attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@return    The borderBackGroundColor value
	 */
	public Color getBorderBackGroundColor() {
		return bkgBorderAreaColor;
	}


	/**
	 *  Sets the name attribute of the FunctionGraphsJPanel object
	 *
	 *@param  name  The new name value
	 */
	public void setName(String name) {
		nameOfGraph = name;
		updateGraphJPanel();
	}


	/**
	 *  Returns the name attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The name value
	 */
	public String getName() {
		return nameOfGraph;
	}


	/**
	 *  Sets the axisNames attribute of the FunctionGraphsJPanel object
	 *
	 *@param  nameX  The new axisNames value
	 *@param  nameY  The new axisNames value
	 */
	public void setAxisNames(String nameX, String nameY) {
		this.nameX = nameX;
		this.nameY = nameY;
		updateGraphJPanel();
	}


	/**
	 *  Sets the axisNameX attribute of the FunctionGraphsJPanel object
	 *
	 *@param  nameX  The new axisNameX value
	 */
	public void setAxisNameX(String nameX) {
		this.nameX = nameX;
		updateGraphJPanel();
	}


	/**
	 *  Sets the axisNameY attribute of the FunctionGraphsJPanel object
	 *
	 *@param  nameY  The new axisNameY value
	 */
	public void setAxisNameY(String nameY) {
		this.nameY = nameY;
		updateGraphJPanel();
	}


	/**
	 *  Sets the nameFont attribute of the FunctionGraphsJPanel object
	 *
	 *@param  fn  The new nameFont value
	 */
	public void setNameFont(Font fn) {
		nameOfGraphFont = fn;
		updateGraphJPanel();
	}


	/**
	 *  Sets the axisNameFontX attribute of the FunctionGraphsJPanel object
	 *
	 *@param  fnX  The new axisNameFontX value
	 */
	public void setAxisNameFontX(Font fnX) {
		nameXFont = fnX;
		updateGraphJPanel();
	}


	/**
	 *  Sets the axisNameFontY attribute of the FunctionGraphsJPanel object
	 *
	 *@param  fnY  The new axisNameFontY value
	 */
	public void setAxisNameFontY(Font fnY) {
		nameYFont = fnY;
		updateGraphJPanel();
	}


	/**
	 *  Sets the numberFont attribute of the FunctionGraphsJPanel object
	 *
	 *@param  fn  The new numberFont value
	 */
	public void setNumberFont(Font fn) {
		numberFont = fn;
		updateGraphJPanel();
	}


	/**
	 *  Sets the nameColor attribute of the FunctionGraphsJPanel object
	 *
	 *@param  cl  The new nameColor value
	 */
	public void setNameColor(Color cl) {
		nameOfGraphColor = cl;
		updateGraphJPanel();
	}


	/**
	 *  Sets the axisNameColorX attribute of the FunctionGraphsJPanel object
	 *
	 *@param  clX  The new axisNameColorX value
	 */
	public void setAxisNameColorX(Color clX) {
		nameXColor = clX;
		updateGraphJPanel();
	}


	/**
	 *  Sets the axisNameColorY attribute of the FunctionGraphsJPanel object
	 *
	 *@param  clY  The new axisNameColorY value
	 */
	public void setAxisNameColorY(Color clY) {
		nameYColor = clY;
		updateGraphJPanel();
	}


	/**
	 *  Sets the numberColor attribute of the FunctionGraphsJPanel object
	 *
	 *@param  cl  The new numberColor value
	 */
	public void setNumberColor(Color cl) {
		numberColor = cl;
		updateGraphJPanel();
	}


	/**
	 *  Sets the numberFormatX attribute of the FunctionGraphsJPanel object
	 *
	 *@param  df  The new numberFormatX value
	 */
	public void setNumberFormatX(NumberFormat df) {
		glPanel.setNumberFormatX(df);
		numberFormatX = df;
		if (externalGridLimits != null) {
			externalGridLimits.setNumberFormatX(numberFormatX);
		}
		if (!useSmartGridLimits) {
			innerGridLimits.setNumberFormatX(numberFormatX);
		}
		updateGraphJPanel();
	}


	/**
	 *  Sets the numberFormatY attribute of the FunctionGraphsJPanel object
	 *
	 *@param  df  The new numberFormatY value
	 */
	public void setNumberFormatY(NumberFormat df) {
		glPanel.setNumberFormatY(df);
		numberFormatY = df;
		if (externalGridLimits != null) {
			externalGridLimits.setNumberFormatY(numberFormatY);
		}
		if (!useSmartGridLimits) {
			innerGridLimits.setNumberFormatY(numberFormatY);
		}
		updateGraphJPanel();
	}


	/**
	 *  Sets the makrsScaleX attribute of the FunctionGraphsJPanel object
	 *
	 *@param  numbMarkScaleXin  The new makrsScaleX value
	 */
	public void setMakrsScaleX(double numbMarkScaleXin) {
		numbMarkScaleX = numbMarkScaleXin;
		updateGraphJPanel();
	}


	/**
	 *  Sets the makrsScaleY attribute of the FunctionGraphsJPanel object
	 *
	 *@param  numbMarkScaleYin  The new makrsScaleY value
	 */
	public void setMakrsScaleY(double numbMarkScaleYin) {
		numbMarkScaleY = numbMarkScaleYin;
		updateGraphJPanel();
	}


	/**
	 *  Returns the clickedPointObject attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The clickedPointObject value
	 */
	public ClickedPoint getClickedPointObject() {
		return clickedPoint;
	}


	//------------------------------------------------------
	//method related to the vertical and horizontal lines
	//------------------------------------------------------

	/**
	 *  Returns the numberOfVerticalLines attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@return    The numberOfVerticalLines value
	 */
	public synchronized int getNumberOfVerticalLines() {
		return vLinesV.size();
	}


	/**
	 *  Returns the numberOfHorizontalLines attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@return    The numberOfHorizontalLines value
	 */
	public synchronized int getNumberOfHorizontalLines() {
		return hLinesV.size();
	}


	/**
	 *  Adds a feature to the VerticalLine attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@param  x  The feature to be added to the VerticalLine attribute
	 *@return    Description of the Return Value
	 */
	public synchronized int addVerticalLine(double x) {
		vLinesV.add(new Double(x));
		vLinesColorV.add(defaultVerticalLineColor);
		updateGraphJPanel();
		if (draggedVerLinesListener != null) {
			draggedVerLinesListener.actionPerformed(draggedVerLinesEvent);
		}
		return vLinesV.size() - 1;
	}


	/**
	 *  Adds a feature to the HorizontalLine attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@param  y  The feature to be added to the HorizontalLine attribute
	 *@return    Description of the Return Value
	 */
	public synchronized int addHorizontalLine(double y) {
		hLinesV.add(new Double(y));
		hLinesColorV.add(defaultHorizontLineColor);
		updateGraphJPanel();
		if (draggedHorLinesListener != null) {
			draggedHorLinesListener.actionPerformed(draggedHorLinesEvent);
		}
		return hLinesV.size() - 1;
	}


	/**
	 *  Adds a feature to the VerticalLine attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@param  x   The feature to be added to the VerticalLine attribute
	 *@param  cl  The feature to be added to the VerticalLine attribute
	 *@return     Description of the Return Value
	 */
	public synchronized int addVerticalLine(double x, Color cl) {
		vLinesV.add(new Double(x));
		vLinesColorV.add(cl);
		updateGraphJPanel();
		if (draggedVerLinesListener != null) {
			draggedVerLinesListener.actionPerformed(draggedVerLinesEvent);
		}
		return vLinesV.size() - 1;
	}


	/**
	 *  Adds a feature to the HorizontalLine attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@param  y   The feature to be added to the HorizontalLine attribute
	 *@param  cl  The feature to be added to the HorizontalLine attribute
	 *@return     Description of the Return Value
	 */
	public synchronized int addHorizontalLine(double y, Color cl) {
		hLinesV.add(new Double(y));
		hLinesColorV.add(cl);
		updateGraphJPanel();
		if (draggedHorLinesListener != null) {
			draggedHorLinesListener.actionPerformed(draggedHorLinesEvent);
		}
		return hLinesV.size() - 1;
	}


	/**
	 *  Sets the verticalLineValue attribute of the FunctionGraphsJPanel object
	 *
	 *@param  x      The new verticalLineValue value
	 *@param  index  The new verticalLineValue value
	 */
	public synchronized void setVerticalLineValue(double x, int index) {
		if (index < vLinesV.size() && index >= 0) {
			vLinesV.remove(index);
			vLinesV.add(index, new Double(x));
			updateGraphJPanel();
			if (draggedVerLinesListener != null) {
				draggedVerLinesListener.actionPerformed(draggedVerLinesEvent);
			}
		}
	}


	/**
	 *  Sets the horizontalLineValue attribute of the FunctionGraphsJPanel object
	 *
	 *@param  y      The new horizontalLineValue value
	 *@param  index  The new horizontalLineValue value
	 */
	public synchronized void setHorizontalLineValue(double y, int index) {
		if (index < hLinesV.size() && index >= 0) {
			hLinesV.remove(index);
			hLinesV.add(index, new Double(y));
			updateGraphJPanel();
			if (draggedHorLinesListener != null) {
				draggedHorLinesListener.actionPerformed(draggedHorLinesEvent);
			}
		}
	}


	/**
	 *  Sets the verticalLineColor attribute of the FunctionGraphsJPanel object
	 *
	 *@param  cl     The new verticalLineColor value
	 *@param  index  The new verticalLineColor value
	 */
	public synchronized void setVerticalLineColor(Color cl, int index) {
		if (index < vLinesColorV.size() && index >= 0) {
			vLinesColorV.remove(index);
			vLinesColorV.add(index, cl);
			updateGraphJPanel();
		}
	}


	/**
	 *  Sets the horizontalLineColor attribute of the FunctionGraphsJPanel object
	 *
	 *@param  cl     The new horizontalLineColor value
	 *@param  index  The new horizontalLineColor value
	 */
	public synchronized void setHorizontalLineColor(Color cl, int index) {
		if (index < hLinesColorV.size() && index >= 0) {
			hLinesColorV.remove(index);
			hLinesColorV.add(index, cl);
			updateGraphJPanel();
		}
	}


	/**
	 *  Returns the verticalValue attribute of the FunctionGraphsJPanel object
	 *
	 *@param  index  Description of the Parameter
	 *@return        The verticalValue value
	 */
	public synchronized double getVerticalValue(int index) {
		if (index < vLinesV.size() && index >= 0) {
			return vLinesV.get(index).doubleValue();
		}
		return -Double.MAX_VALUE;
	}


	/**
	 *  Returns the horizontalValue attribute of the FunctionGraphsJPanel object
	 *
	 *@param  index  Description of the Parameter
	 *@return        The horizontalValue value
	 */
	public synchronized double getHorizontalValue(int index) {
		if (index < hLinesV.size() && index >= 0) {
			return hLinesV.get(index).doubleValue();
		}
		return -Double.MAX_VALUE;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  index  Description of the Parameter
	 */
	public synchronized void removeVerticalValue(int index) {
		if (index < vLinesV.size() && index >= 0) {
			vLinesV.remove(index);
			vLinesColorV.remove(index);
			updateGraphJPanel();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  index  Description of the Parameter
	 */
	public synchronized void removeHorizontalValue(int index) {
		if (index < hLinesV.size() && index >= 0) {
			hLinesV.remove(index);
			hLinesColorV.remove(index);
			updateGraphJPanel();
		}
	}


	/**
	 *  Description of the Method
	 */
	public synchronized void removeVerticalValues() {
		vLinesV.clear();
		vLinesColorV.clear();
		updateGraphJPanel();
	}


	/**
	 *  Description of the Method
	 */
	public synchronized void removeHorizontalValues() {
		hLinesV.clear();
		hLinesColorV.clear();
		updateGraphJPanel();
	}


	/**
	 *  Sets the draggingHorLinesGraphMode attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@param  dragLinesModeYes  The new draggingHorLinesGraphMode value
	 */
	public synchronized void setDraggingHorLinesGraphMode(boolean dragLinesModeYes) {
		dragHorLinesModeYes = dragLinesModeYes;
		dragHorLinesModeButton.setSelected(dragHorLinesModeYes);
		updateGraphJPanel();
	}


	/**
	 *  Sets the draggingVerLinesGraphMode attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@param  dragLinesModeYes  The new draggingVerLinesGraphMode value
	 */
	public synchronized void setDraggingVerLinesGraphMode(boolean dragLinesModeYes) {
		dragVerLinesModeYes = dragLinesModeYes;
		dragVerLinesModeButton.setSelected(dragVerLinesModeYes);
		updateGraphJPanel();

	}


	/**
	 *  Sets the horLinesButtonVisible attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vs  The new horLinesButtonVisible value
	 */
	public synchronized void setHorLinesButtonVisible(boolean vs) {
		horLinesModeButtonVisible = vs;
		dragHorLinesModeYes = vs;
		dragHorLinesModeButton.setSelected(dragHorLinesModeYes);
		remove(dragHorLinesModeButton);
		if (vs) {
			add(dragHorLinesModeButton);
		}
		updateGraphJPanel();
	}


	/**
	 *  Sets the verLinesButtonVisible attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vs  The new verLinesButtonVisible value
	 */
	public synchronized void setVerLinesButtonVisible(boolean vs) {
		verLinesModeButtonVisible = vs;
		dragVerLinesModeYes = vs;
		dragVerLinesModeButton.setSelected(dragVerLinesModeYes);
		remove(dragVerLinesModeButton);
		if (vs) {
			add(dragVerLinesModeButton);
		}
		updateGraphJPanel();
	}


	/**
	 *  Adds a feature to the DraggedHorLinesListener attribute of the
	 *  FunctionGraphsJPanel object
	 *
	 *@param  draggedHorLinesListenerIn  The feature to be added to the
	 *      DraggedHorLinesListener attribute
	 */
	public synchronized void addDraggedHorLinesListener(ActionListener draggedHorLinesListenerIn) {
		draggedHorLinesListener = draggedHorLinesListenerIn;
	}


	/**
	 *  Adds a feature to the DraggedVerLinesListener attribute of the
	 *  FunctionGraphsJPanel object
	 *
	 *@param  draggedVerLinesListenerIn  The feature to be added to the
	 *      DraggedVerLinesListener attribute
	 */
	public synchronized void addDraggedVerLinesListener(ActionListener draggedVerLinesListenerIn) {
		draggedVerLinesListener = draggedVerLinesListenerIn;
	}


	/**
	 *  Sets the draggedHorLinesMotionListen attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@param  draggedHorLinesMotionListenYesIn  The new draggedHorLinesMotionListen
	 *      value
	 */
	public synchronized void setDraggedHorLinesMotionListen(boolean draggedHorLinesMotionListenYesIn) {
		draggedHorLinesMotionListenYes = draggedHorLinesMotionListenYesIn;
	}


	/**
	 *  Sets the draggedVerLinesMotionListen attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@param  draggedVerLinesMotionListenYesIn  The new draggedVerLinesMotionListen
	 *      value
	 */
	public synchronized void setDraggedVerLinesMotionListen(boolean draggedVerLinesMotionListenYesIn) {
		draggedVerLinesMotionListenYes = draggedVerLinesMotionListenYesIn;
	}


	/**
	 *  Returns the draggedLineIndex attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The draggedLineIndex value
	 */
	public synchronized int getDraggedLineIndex() {
		if (mouseDraggedTaskType == 1 || mouseDraggedTaskType == 2) {
			return draggedLinesIndex;
		}
		return -1;
	}


	/**
	 *  Returns the nearestHorizontalLineIndex attribute of the
	 *  FunctionGraphsJPanel object
	 *
	 *@param  y  Description of the Parameter
	 *@return    The nearestHorizontalLineIndex value
	 */
	private synchronized int getNearestHorizontalLineIndex(double y) {
		int index = -1;
		double d_min = Double.MAX_VALUE;
		double d_minG = Double.MAX_VALUE;
		double d_maxG = Double.MAX_VALUE;
		double d = 0.;
		if (dragHorLinesModeYes) {
			if (hLinesV.size() == 0) {
				return index;
			}
			d_minG = yMin;
			d_maxG = yMax;
			for (int i = 0; i < hLinesV.size(); i++) {
				d = hLinesV.get(i).doubleValue();
				if (d < d_minG) {
					d = d_minG;
				}
				if (d > d_maxG) {
					d = d_maxG;
				}
				d = Math.abs(y - d);
				if (d_min > d) {
					d_min = d;
					index = i;
				}
			}
		}
		return index;
	}


	/**
	 *  Returns the nearestVerticalLineIndex attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@param  x  Description of the Parameter
	 *@return    The nearestVerticalLineIndex value
	 */
	private synchronized int getNearestVerticalLineIndex(double x) {
		int index = -1;
		double d_min = Double.MAX_VALUE;
		double d_minG = Double.MAX_VALUE;
		double d_maxG = Double.MAX_VALUE;
		double d = 0.;
		if (dragVerLinesModeYes) {
			if (vLinesV.size() == 0) {
				return index;
			}
			d_minG = xMin;
			d_maxG = xMax;
			for (int i = 0; i < vLinesV.size(); i++) {
				d = vLinesV.get(i).doubleValue();
				if (d < d_minG) {
					d = d_minG;
				}
				if (d > d_maxG) {
					d = d_maxG;
				}
				d = Math.abs(x - d);
				if (d_min > d) {
					d_min = d;
					index = i;
				}
			}
		}
		return index;
	}


	//-----------------------------------------------
	//methods related to the appearance of the grids
	//-----------------------------------------------

	/**
	 *  Sets the gridLinesVisibleX attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vsbl  The new gridLinesVisibleX value
	 */
	public void setGridLinesVisibleX(boolean vsbl) {
		gridLineOnX = vsbl;
		updateGraphJPanel();
	}


	/**
	 *  Returns the gridLinesVisibleX attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The gridLinesVisibleX value
	 */
	public boolean getGridLinesVisibleX() {
		return gridLineOnX;
	}


	/**
	 *  Sets the gridLinesVisibleY attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vsbl  The new gridLinesVisibleY value
	 */
	public void setGridLinesVisibleY(boolean vsbl) {
		gridLineOnY = vsbl;
		updateGraphJPanel();
	}


	/**
	 *  Returns the gridLinesVisibleY attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The gridLinesVisibleY value
	 */
	public boolean getGridLinesVisibleY() {
		return gridLineOnY;
	}


	//----------------------------------------------
	//methods related to the axis' parameters dialog
	//----------------------------------------------
	/**
	 *  Returns the parentFrameOrDialog attribute of the FunctionGraphsJPanel
	 *  object
	 *
	 *@return    The parentFrameOrDialog value
	 */
	private Component getParentFrameOrDialog() {
		Component cmp = this.getParent();
		while ((!(cmp == null)) && (!(cmp instanceof Frame)) && (!(cmp instanceof Dialog))) {
			cmp = cmp.getParent();
		}
		if (cmp == null) {
			return null;
		}
		return cmp;
	}


	//Method related to VERTICAL and HORIZONTAL limits listeners
	/**
	 *  Adds a feature to the HorLimitsListener attribute of the
	 *  FunctionGraphsJPanel object
	 *
	 *@param  al  The feature to be added to the HorLimitsListener attribute
	 */
	public void addHorLimitsListener(ActionListener al) {
		horLimListenersV.add(al);
		al.actionPerformed(horLimEvent);
		//for ( int k = 0, n = horLimListenersV.size(); k < n; k++ ) {
		//    ( (ActionListener) horLimListenersV.get( k ) ).actionPerformed( horLimEvent );
		//}
	}


	/**
	 *  Adds a feature to the VerLimitsListener attribute of the
	 *  FunctionGraphsJPanel object
	 *
	 *@param  al  The feature to be added to the VerLimitsListener attribute
	 */
	public void addVerLimitsListener(ActionListener al) {
		verLimListenersV.add(al);
		al.actionPerformed(verLimEvent);
		//for ( int k = 0, n = verLimListenersV.size(); k < n; k++ ) {
		//    ( (ActionListener) verLimListenersV.get( k ) ).actionPerformed( verLimEvent );
		//}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  al  Description of the Parameter
	 */
	public void removeHorLimitsListener(ActionListener al) {
		horLimListenersV.remove(al);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  al  Description of the Parameter
	 */
	public void removeVerLimitsListener(ActionListener al) {
		verLimListenersV.remove(al);
	}


	/**
	 *  Returns the horLimitsListeners attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The horLimitsListeners value
	 */
	public Vector<ActionListener> getHorLimitsListeners() {
		return new Vector<ActionListener>(horLimListenersV);
	}


	/**
	 *  Returns the verLimitsListeners attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The verLimitsListeners value
	 */
	public Vector<ActionListener> getVerLimitsListeners() {
		return new Vector<ActionListener>(verLimListenersV);
	}


	/**
	 *  Returns the axisParamDialog attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The axisParamDialog value
	 */
	private JDialog getAxisParamDialog() {
		Object frameOrDialog = getParentFrameOrDialog();
		if (frameOrDialog == null) {
			return null;
		}
		if (parentFrameOrDialog == null) {
			parentFrameOrDialog = frameOrDialog;
		}
		if (parentFrameOrDialog != frameOrDialog) {
			parentFrameOrDialog = frameOrDialog;
			if (parentFrameOrDialog instanceof Frame) {
				axisDialog = new JDialog((Frame) parentFrameOrDialog, true);
			}
			if (parentFrameOrDialog instanceof Dialog) {
				axisDialog = new JDialog((Dialog) parentFrameOrDialog, true);
			}
		}
		if (axisDialog == null) {
			if (parentFrameOrDialog instanceof Frame) {
				axisDialog = new JDialog((Frame) parentFrameOrDialog, true);
			}
			if (parentFrameOrDialog instanceof Dialog) {
				axisDialog = new JDialog((Dialog) parentFrameOrDialog, true);
			}
		}
		if (axisDialog != null) {
			axisDialog.setLocationRelativeTo(this);
		}
		return axisDialog;
	}


	//----------------------------------------------
	//methods related to Legend
	//----------------------------------------------
	/**
	 *  Sets the legendVisible attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vs  The new legendVisible value
	 */
	public void setLegendVisible(boolean vs) {
		legendButton.setSelected(vs);
		legend.setVisible(vs);
		updateGraphJPanel();
	}


	/**
	 *  Returns the legendVisible attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The legendVisible value
	 */
	public boolean isLegendVisible() {
		return legend.isVisible();
	}


	/**
	 *  Sets the legendButtonVisible attribute of the FunctionGraphsJPanel object
	 *
	 *@param  vs  The new legendButtonVisible value
	 */
	public void setLegendButtonVisible(boolean vs) {
		legendButtonVisible = vs;
		remove(legendButton);
		if (vs) {
			add(legendButton);
			legendButton.setSelected(legendButton.isSelected());
		}
		updateGraphJPanel();
	}


	/**
	 *  Sets the legendKeyString attribute of the FunctionGraphsJPanel object
	 *
	 *@param  legendKeyString  The new legendKeyString value
	 */
	public void setLegendKeyString(String legendKeyString) {
		this.legendKeyString = legendKeyString;
		legend.setKeyString(legendKeyString);
	}


	/**
	 *  Returns the legendKeyString attribute of the FunctionGraphsJPanel object
	 *
	 *@return    The legendKeyString value
	 */
	public String getLegendKeyString() {
		return legendKeyString;
	}


	/**
	 *  Sets the legendPosition attribute of the FunctionGraphsJPanel object
	 *
	 *@param  legendPosition  The new legendPosition value
	 */
	public void setLegendPosition(int legendPosition) {
		legend.setPosition(legendPosition);
	}


	/**
	 *  Sets the legendFont attribute of the FunctionGraphsJPanel object
	 *
	 *@param  fnt  The new legendFont value
	 */
	public void setLegendFont(Font fnt) {
		legend.setFont(fnt);
	}


	/**
	 *  Sets the legendColor attribute of the FunctionGraphsJPanel object
	 *
	 *@param  cl  The new legendColor value
	 */
	public void setLegendColor(Color cl) {
		legend.setColor(cl);
	}


	/**
	 *  Sets the legendBackground attribute of the FunctionGraphsJPanel object
	 *
	 *@param  cl  The new legendBackground value
	 */
	public void setLegendBackground(Color cl) {
		legend.setBackground(cl);
	}


	//----------------------------------------------
	//method updates the limits and number of points
	//in all graphs
	//----------------------------------------------

	/**
	 *  Description of the Method
	 */
	private synchronized void updateData() {
		synchronized (graphDataV) {

			if (graphChoosingYes) {
				unChooseGraph();
			}

			double xMinIn = Double.MAX_VALUE;
			double yMinIn = Double.MAX_VALUE;
			double xMaxIn = -Double.MAX_VALUE;
			double yMaxIn = -Double.MAX_VALUE;

			nTotalGraphPoints = 0;
			nTotalCurvePoints = 0;
			int nColorSurfaceSize = 0;
			if (graphDataV.size() > 0 ||
					colorSurfaceData != null || curveDataV.size() > 0) {

				double d;
				BasicGraphData grD = null;
				for (int i = 0; i < graphDataV.size(); i++) {
					grD = graphDataV.get(i);
					if (grD.getNumbOfPoints() > 0) {
						d = grD.getMinX();
						if (d < xMinIn) {
							xMinIn = d;
						}
						d = grD.getMinY();
						if (d < yMinIn) {
							yMinIn = d;
						}
						d = grD.getMaxX();
						if (d > xMaxIn) {
							xMaxIn = d;
						}
						d = grD.getMaxY();
						if (d > yMaxIn) {
							yMaxIn = d;
						}
						nTotalGraphPoints = nTotalGraphPoints + grD.getNumbOfPoints();
					}
				}

				if (colorSurfaceData != null) {
					nColorSurfaceSize = colorSurfaceData.getSizeX() * colorSurfaceData.getSizeY();
					d = colorSurfaceData.getMinX();
					if (d < xMinIn) {
						xMinIn = d;
					}
					d = colorSurfaceData.getMinY();
					if (d < yMinIn) {
						yMinIn = d;
					}
					d = colorSurfaceData.getMaxX();
					if (d > xMaxIn) {
						xMaxIn = d;
					}
					d = colorSurfaceData.getMaxY();
					if (d > yMaxIn) {
						yMaxIn = d;
					}
				}

				if (curveDataV.size() > 0) {
					for (int i = 0; i < curveDataV.size(); i++) {
						CurveData crvD = curveDataV.get(i);
						if(crvD.getSize() > 0){
							d = crvD.getMinX();
							if (d < xMinIn) {
								xMinIn = d;
							}
							d = crvD.getMinY();
							if (d < yMinIn) {
								yMinIn = d;
							}
							d = crvD.getMaxX();
							if (d > xMaxIn) {
								xMaxIn = d;
							}
							d = crvD.getMaxY();
							if (d > yMaxIn) {
								yMaxIn = d;
							}
							nTotalCurvePoints++;
						}
					}
				}

				synchronized (innerGridLimits) {
					if (xMinIn < xMaxIn) {
						xMaxIn = xMaxIn;
						xMinIn = xMinIn;
					}
					if (yMinIn < yMaxIn) {
						yMaxIn = yMaxIn;
						yMinIn = yMinIn;
					}
					innerGridLimits.initialize();
					innerGridLimits.setXmin(xMinIn);
					innerGridLimits.setYmin(yMinIn);
					innerGridLimits.setXmax(xMaxIn);
					innerGridLimits.setYmax(yMaxIn);
					//set the smart limits for inner grid object
					if (nTotalCurvePoints > 0 ||
							nTotalGraphPoints > 0 ||
							nColorSurfaceSize > 0) {
						innerGridLimits.setSmartLimits();
					}
				}
			}
			updateGraphJPanel();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of the Parameter
	 */
	protected void paintComponent(Graphics g) {
		Graphics2D g2D = (Graphics2D) g;

		if (offScreenImageOn) {
			if (offScreenImage_ == null) {
				offScreenImage_ = createVolatileImage(getWidth(), getHeight());
			}
			if (getWidth() != ((VolatileImage) offScreenImage_).getWidth() ||
					getHeight() != ((VolatileImage) offScreenImage_).getHeight()) {
				offScreenImage_ = createVolatileImage(getWidth(), getHeight());
			}

			try {
				do {
					GraphicsConfiguration gc = getGraphicsConfiguration();
					int valCode = ((VolatileImage) offScreenImage_).validate(gc);
					if (valCode == VolatileImage.IMAGE_INCOMPATIBLE) {
						offScreenImage_ = createVolatileImage(getWidth(), getHeight());
					}
					Graphics2D og = (Graphics2D) offScreenImage_.getGraphics();
					synchronized (graphDataV) {
						synchronized (graphColorV) {
							drawGraphicsData(og, getWidth(), getHeight());
						}
					}
					og.dispose();
					g.drawImage(offScreenImage_, 0, 0, this);
				} while (((VolatileImage) offScreenImage_).contentsLost());
			} catch (Exception e) {
				System.out.println("Exception during paintComponent e=" + e);
			}
			return;
		}

		synchronized (graphDataV) {
			synchronized (graphColorV) {
				drawGraphicsData(g2D, getWidth(), getHeight());
			}
		}
	}


	/**
	 *  Update the graph panel, not data
	 */
	private void updateGraphJPanel() {
		repaint(0, 0, 0, this.getWidth(), this.getHeight());
	}


	/**
	 *  Update data and the graph panel
	 */
	public void refreshGraphJPanel() {
		updateData();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g      Description of the Parameter
	 *@param  scr_w  Description of the Parameter
	 *@param  scr_h  Description of the Parameter
	 */
	private void drawGraphicsData(Graphics2D g, int scr_w, int scr_h) {
		screenW = scr_w;
		screenH = scr_h;
		//-----------------------------------------------
		//background color
		//-----------------------------------------------
		Color BackgroundInitial = g.getBackground();

		if (bkgGraphAreaColor != null) {
			g.setBackground(bkgGraphAreaColor);
		} else {
			bkgGraphAreaColor = g.getBackground();
		}
		g.clearRect(0, 0, scr_w, scr_h);

		//-----------------------------------------------
		//current draw color
		//-----------------------------------------------
		Color colorInitial = g.getColor();

		if (nTotalGraphPoints == 0 && colorSurfaceData == null && nTotalCurvePoints == 0) {
			g.setColor(Color.RED);
			g.drawString("NO DATA", scr_w / 2, scr_h / 2);
			//===================================
			//you suppose to see only greed lines
			//return;
			//===================================
		}

		//-----------------------------------------------
		//initial tarnsform
		//-----------------------------------------------
		AffineTransform transInitial = g.getTransform();

		//-----------------------------------------------
		//initial stroke
		//-----------------------------------------------
		Stroke strokeInitial = g.getStroke();

		//-----------------------------------------------
		//initial Font
		//-----------------------------------------------
		Font fontInitial = g.getFont();

		//-----------------------------------------------------
		//definition of the max and min for axises
		//-----------------------------------------------------
		GridLimits currentGridLimitsIn = externalGridLimits;
		if (currentGridLimitsIn != null) {
			currentGridLimitsIn.setGridLimitsSwitch(true);
		} else {
			currentGridLimitsIn = innerGridLimits;
		}

		if (zoomGridLimitsV.size() > 0) {
			if (mouseDrugged && mouseDraggedTaskType == 0) {
				if (zoomGridLimitsV.size() > 1) {
					currentGridLimitsIn = zoomGridLimitsV.get(zoomGridLimitsV.size() - 2);
					currentGridLimitsIn.setGridLimitsSwitch(true);
				}
			} else {
				currentGridLimitsIn = zoomGridLimitsV.lastElement();
				currentGridLimitsIn.setGridLimitsSwitch(true);
			}
		}
		double xMinIn = innerGridLimits.getMinX();
		double yMinIn = innerGridLimits.getMinY();
		double xMaxIn = innerGridLimits.getMaxX();
		double yMaxIn = innerGridLimits.getMaxY();
		if (currentGridLimitsIn != null && currentGridLimitsIn.getGridLimitsSwitch()) {
			if (currentGridLimitsIn.isSetXmin()) {
				xMinIn = currentGridLimitsIn.getMinX();
			}
			if (currentGridLimitsIn.isSetYmin()) {
				yMinIn = currentGridLimitsIn.getMinY();
			}
			if (currentGridLimitsIn.isSetXmax()) {
				xMaxIn = currentGridLimitsIn.getMaxX();
			}
			if (currentGridLimitsIn.isSetYmax()) {
				yMaxIn = currentGridLimitsIn.getMaxY();
			}
		}
		GridLimits currentGridLimits = currentGridLimitsIn;

		if (xMinIn == xMaxIn) {
			xMinIn = xMinIn - 0.5;
			xMaxIn = xMaxIn + 0.5;
		}
		if (yMinIn == yMaxIn) {
			yMinIn = yMinIn - 0.5;
			yMaxIn = yMaxIn + 0.5;
		}

		if (xMinIn == -Double.MAX_VALUE) {
			xMinIn = 0.0;
		}
		if (xMaxIn == Double.MAX_VALUE) {
			xMaxIn = 1.0;
		}
		if (yMinIn == -Double.MAX_VALUE) {
			yMinIn = 0.0;
		}
		if (yMaxIn == Double.MAX_VALUE) {
			yMaxIn = 1.0;
		}

		//set boolean variables about x and y limits changes
		//the call of listeners should occur at the end of this method
		boolean xLimChanged = false;
		boolean yLimChanged = false;

		if (xMin != xMinIn || xMax != xMaxIn) {
			xLimChanged = true;
		}
		if (yMin != yMinIn || yMax != yMaxIn) {
			yLimChanged = true;
		}

		xMin = xMinIn;
		xMax = xMaxIn;
		yMin = yMinIn;
		yMax = yMaxIn;

		//-----------------------------------------------------------
		//definition the graph area bounds (depending on fonts etc.)
		//-----------------------------------------------------------
		fSizeName = 0;
		if (nameOfGraph != null) {
			g.setFont(nameOfGraphFont);
			fSizeName = g.getFontMetrics().getHeight() + 4;
		}
		if (chooseModeButtonVisible) {
			fSizeName = Math.max(fSizeName, (int) chooseModeButton.getPreferredSize().getHeight());
		}
		if (horLinesModeButtonVisible) {
			fSizeName = Math.max(fSizeName, (int) dragHorLinesModeButton.getPreferredSize().getHeight());
		}
		if (verLinesModeButtonVisible) {
			fSizeName = Math.max(fSizeName, (int) dragVerLinesModeButton.getPreferredSize().getHeight());
		}
		if (legendButtonVisible) {
			fSizeName = Math.max(fSizeName, (int) legendButton.getPreferredSize().getHeight());
		}

		fSizeX = 0;
		if (nameX != null) {
			g.setFont(nameXFont);
			fSizeX = g.getFontMetrics().getHeight() + 4;
		}

		fSizeY = 0;
		if (nameY != null) {
			g.setFont(nameYFont);
			fSizeY = g.getFontMetrics().getHeight() + 4;
		}

		fSizeNumbXhor = 0;
		if (gridYmarkerOn) {
			int iS1 = 0;
			int iS2 = 0;
			g.setFont(numberFont);
			iS1 = g.getFontMetrics().stringWidth(currentGridLimits.getNumberFormatX().format(xMax));
			iS2 = g.getFontMetrics().stringWidth(currentGridLimits.getNumberFormatX().format(xMin));
			fSizeNumbXhor = Math.max(iS1, iS2) + 4;
		}

		fSizeNumbXver = 0;
		if (gridXmarkerOn) {
			g.setFont(numberFont);
			fSizeNumbXver = g.getFontMetrics().getHeight();
		}

		fSizeNumbYhor = 0;
		if (gridYmarkerOn) {
			int iS1 = 0;
			int iS2 = 0;
			g.setFont(numberFont);
			iS1 = g.getFontMetrics().stringWidth(currentGridLimits.getNumberFormatY().format(yMax));
			iS2 = g.getFontMetrics().stringWidth(currentGridLimits.getNumberFormatY().format(yMin));
			fSizeNumbYhor = Math.max(iS1, iS2) + 4;
		}

		fSizeNumbYver = 0;
		if (gridYmarkerOn) {
			g.setFont(numberFont);
			fSizeNumbYver = g.getFontMetrics().getHeight();
		}

		//---------------------------------
		//definition of the OffSets
		//---------------------------------
		xLOffSet = 2 + fSizeY + 2 + fSizeNumbYhor + 4 + 5;
		xROffSet = 5;
		yUOffSet = 4 + fSizeName;
		yBOffSet = 2 + fSizeX + fSizeNumbXver + 2 + 5;

		if ((xLOffSet + xROffSet + 4) > scr_w || (yBOffSet + yUOffSet + 4) > scr_h) {
			return;
		}

		//-----------------------------------------------
		//scales and offsets
		//-----------------------------------------------
		xAxisLength = scr_w - xLOffSet - xROffSet;
		yAxisLength = scr_h - yUOffSet - yBOffSet;
		if (xAxisLength < 0 || yAxisLength < 0) {
			return;
		}

		double scaleX_old = scaleX;
		double scaleY_old = scaleY;

		scaleX = xAxisLength / (xMax - xMin);
		scaleY = -yAxisLength / (yMax - yMin);

		if (scaleX != scaleX_old) {
			xLimChanged = true;
		}
		if (scaleY != scaleY_old) {
			yLimChanged = true;
		}

		//------------------------------
		//tick numbers calculation
		//------------------------------
		int nMajorTksX = 4;
		int nMinorTksX = currentGridLimits.getNumMinorTicksX();
		int nMajorTksY = 4;
		int nMinorTksY = currentGridLimits.getNumMinorTicksY();

		if (!currentGridLimits.getMajorTicksOnX()) {
			if (fSizeNumbXhor != 0) {
				nMajorTksX = xAxisLength / (2 * fSizeNumbXhor);
			} else {
				nMajorTksX = 0;
			}
		} else {
			nMajorTksX = currentGridLimits.getNumMajorTicksX();
		}

		if (!currentGridLimits.getMajorTicksOnY()) {
			if (fSizeNumbYver != 0) {
				nMajorTksY = yAxisLength / (2 * fSizeNumbYver);
			} else {
				nMajorTksY = 0;
			}
		} else {
			nMajorTksY = currentGridLimits.getNumMajorTicksY();
		}

		int x1;
		int x2;
		int y1;
		int y2;

		//------------------------------
		//draw the colored surface plot
		//------------------------------
		if (colorSurfaceData != null) {
			int nStripesX = colorSurfaceData.getScreenSizeX();
			int nStripesY = colorSurfaceData.getScreenSizeY();
			double xStripeW = (xMax - xMin) / nStripesX;
			double yStripeW = (yMax - yMin) / nStripesY;
			double x_0;
			double y_0;
			for (int i = 0; i < nStripesX; i++) {
				for (int j = 0; j < nStripesY; j++) {
					x_0 = i * xStripeW + xMin;
					y_0 = yMax - j * yStripeW;
					x1 = getScreenX(x_0);
					x2 = getScreenX(x_0 + xStripeW);
					y1 = getScreenY(y_0);
					y2 = getScreenY(y_0 - yStripeW);
					x_0 += 0.5 * xStripeW;
					y_0 -= 0.5 * yStripeW;
					g.setBackground(colorSurfaceData.getColor(x_0, y_0));
					g.clearRect(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
				}
			}
		}

		//------------------------------
		//draw grid lines
		//------------------------------

		if (gridLineOnX && nMajorTksX > 1) {
			if (gridLineColor == null) {
				g.setColor(bkgGraphAreaColor.darker());
			} else {
				g.setColor(gridLineColor);
			}
			double step = (xMax - xMin) / (nMinorTksX * (nMajorTksX - 1) + nMajorTksX - 1);
			y1 = yUOffSet;
			y2 = scr_h - yBOffSet;
			for (int i = 1, n = nMinorTksX * (nMajorTksX - 1) + nMajorTksX - 1; i < n; i++) {
				x1 = getScreenX(xMin + i * step);
				x2 = x1;
				g.drawLine(x1, y1, x2, y2);
			}
		}

		if (gridLineOnY && nMajorTksY > 1) {
			if (gridLineColor == null) {
				g.setColor(bkgGraphAreaColor.darker());
			} else {
				g.setColor(gridLineColor);
			}
			double step = (yMax - yMin) / (nMinorTksY * (nMajorTksY - 1) + nMajorTksY - 1);
			x1 = xLOffSet;
			x2 = scr_w - xROffSet;
			for (int i = 1, n = nMinorTksY * (nMajorTksY - 1) + nMajorTksY - 1; i < n; i++) {
				y1 = getScreenY(yMin + i * step);
				y2 = y1;
				g.drawLine(x1, y1, x2, y2);
			}
		}

		//-----------------------------------------------
		//start draw curves
		//-----------------------------------------------
		if (nTotalCurvePoints > 0) {
			int pointX;
			int pointY;
			int pointH;
			int pointW;
			Stroke strokeIniCurve = g.getStroke();
			for (int i = 0; i < curveDataV.size(); i++) {
				CurveData crvD = curveDataV.get(i);
				g.setStroke(crvD.getStroke());
				g.setColor(crvD.getColor());
				if (crvD.getSize() > 1) {
					for (int j = 0; j < crvD.getSize() - 1; j++) {
						x1 = getScreenX(crvD.getX(j));
						x2 = getScreenX(crvD.getX(j + 1));
						y1 = getScreenY(crvD.getY(j));
						y2 = getScreenY(crvD.getY(j + 1));
						g.drawLine(x1, y1, x2, y2);
					}
				} else {
					if (crvD.getSize() == 1) {
						pointH = crvD.getLineWidth();
						pointW = crvD.getLineWidth();
						pointX = getScreenX(crvD.getX(0)) - pointW / 2;
						pointY = getScreenY(crvD.getY(0)) - pointH / 2;
						g.fillOval(pointX, pointY, pointW, pointH);
					}
				}
			}
			g.setStroke(strokeIniCurve);
		}

		//-----------------------------------------------
		//start draw graph points, curves, errors etc.
		//-----------------------------------------------


		//------------------------------
		//draw interpolated points graph
		//------------------------------
		for (int i = 0, n = graphDataV.size(); i < n; i++) {
			BasicGraphData lgd = graphDataV.get(i);
			Object localLockObject = lgd.getLockObject();
			synchronized (localLockObject) {
				if (!lgd.getDrawLinesOn()) {
					continue;
				}
				if (lgd.getNumbOfPoints() < 2) {
					continue;
				}
				g.setStroke(lgd.getStroke());
				Color lineColor = graphColorV.get(i);
				if (lineColor == null) {
					if (lgd.getGraphColor() == null) {
						lineColor = lineDefaultColor;
					} else {
						lineColor = lgd.getGraphColor();
					}
				}
				g.setColor(lineColor);
				if (lgd instanceof CubicSplineGraphData) {
					for (int j = 0, nGrPoint = lgd.getNumbOfInterpPoints() - 1; j < nGrPoint; j++) {
						x1 = getScreenX(lgd.getInterpX(j));
						x2 = getScreenX(lgd.getInterpX(j + 1));
						y1 = getScreenY(lgd.getInterpY(j));
						y2 = getScreenY(lgd.getInterpY(j + 1));
						g.drawLine(x1, y1, x2, y2);
					}
				} else {
					for (int j = 0, nGrPoint = lgd.getNumbOfPoints() - 1; j < nGrPoint; j++) {
						x1 = getScreenX(lgd.getX(j));
						x2 = getScreenX(lgd.getX(j + 1));
						y1 = getScreenY(lgd.getY(j));
						y2 = getScreenY(lgd.getY(j + 1));
						g.drawLine(x1, y1, x2, y2);
					}
				}
			}
		}

		g.setStroke(strokeInitial);

		//------------------------------
		//draw true points as points
		//------------------------------
		int ovalX;

		//------------------------------
		//draw true points as points
		//------------------------------
		int ovalY;

		//------------------------------
		//draw true points as points
		//------------------------------
		int ovalW;

		//------------------------------
		//draw true points as points
		//------------------------------
		int ovalH;
		for (int i = 0, n = graphDataV.size(); i < n; i++) {
			BasicGraphData lgd = graphDataV.get(i);
			Object localLockObject = lgd.getLockObject();
			synchronized (localLockObject) {
				if (lgd.getNumbOfPoints() < 1) {
					continue;
				}
				if (!lgd.getDrawPointsOn()) {
					continue;
				}
				Color lineColor = graphColorV.get(i);
				if (lineColor == null) {
					if (lgd.getGraphColor() == null) {
						lineColor = lineDefaultColor;
					} else {
						lineColor = lgd.getGraphColor();
					}
				}
				g.setColor(lineColor);

				if (lgd.getGraphPointShape() == null) {

					ovalW = lgd.getGraphPointSize();
					ovalH = lgd.getGraphPointSize();

					for (int j = 0, nGrPoint = lgd.getNumbOfPoints(); j < nGrPoint; j++) {
						ovalX = getScreenX(lgd.getX(j)) - ovalW / 2;
						ovalY = getScreenY(lgd.getY(j)) - ovalH / 2;
						g.fillOval(ovalX, ovalY, ovalW, ovalH);
					}
				} else {
					for (int j = 0, nGrPoint = lgd.getNumbOfPoints(); j < nGrPoint; j++) {
						ovalX = getScreenX(lgd.getX(j));
						ovalY = getScreenY(lgd.getY(j));
						g.translate(ovalX, ovalY);
						if (lgd.isGraphPointShapeFilled()) {
							g.fill(lgd.getGraphPointShape());
						} else {
							g.draw(lgd.getGraphPointShape());
						}
						g.translate(-ovalX, -ovalY);
					}
				}
			}
		}

		//------------------------------
		//draw errors for true points
		//------------------------------
		int xPosition;

		//------------------------------
		//draw errors for true points
		//------------------------------
		int yLow;

		//------------------------------
		//draw errors for true points
		//------------------------------
		int yUpp;

		for (int i = 0, n = graphDataV.size(); i < n; i++) {
			BasicGraphData lgd = graphDataV.get(i);
			Object localLockObject = lgd.getLockObject();
			synchronized (localLockObject) {
				if (lgd.getNumbOfPoints() < 1) {
					continue;
				}
				if (lgd.getMaxErr() == 0.) {
					continue;
				}
				if (!lgd.getDrawPointsOn()) {
					continue;
				}
				g.setStroke(lgd.getStroke());
				Color lineColor = graphColorV.get(i);
				if (lineColor == null) {
					if (lgd.getGraphColor() == null) {
						lineColor = lineDefaultColor;
					} else {
						lineColor = lgd.getGraphColor();
					}
				}
				g.setColor(lineColor);
				for (int j = 0, nGrPoint = lgd.getNumbOfPoints(); j < nGrPoint; j++) {
					if (lgd.getErr(j) == 0.) {
						continue;
					}
					yLow = getScreenY(lgd.getY(j) - lgd.getErr(j));
					yUpp = getScreenY(lgd.getY(j) + lgd.getErr(j));
					xPosition = getScreenX(lgd.getX(j));
					g.drawLine(xPosition, yLow, xPosition, yUpp);
				}
			}
		}

		g.setStroke(strokeInitial);

		//------------------------------------------
		//draw vertical and horizontal lines
		//------------------------------------------
		if (hLinesV.size() > 0) {
			double yP = 0.;
			x1 = xLOffSet;
			x2 = screenW - xROffSet;
			for (int i = 0, n = hLinesV.size(); i < n; i++) {
				yP = hLinesV.get(i).doubleValue();
				if (yP > yMaxIn) {
					yP = yMaxIn;
				}
				if (yP < yMinIn) {
					yP = yMinIn;
				}
				y1 = getScreenY(yP);
				y2 = y1;
				if (y1 < yUOffSet || y1 > (screenH - yBOffSet)) {
					continue;
				}
				g.setColor( hLinesColorV.get(i) );
				g.drawLine(x1, y1, x2, y2);
			}
		}
		if (vLinesV.size() > 0) {
			double xP = 0.;
			y1 = yUOffSet;
			y2 = screenH - yBOffSet;
			for (int i = 0, n = vLinesV.size(); i < n; i++) {
				xP = vLinesV.get(i).doubleValue();
				if (xP > xMaxIn) {
					xP = xMaxIn;
				}
				if (xP < xMinIn) {
					xP = xMinIn;
				}
				x1 = getScreenX(xP);
				x2 = x1;
				if (x1 < xLOffSet || x1 > (screenW - xROffSet)) {
					continue;
				}
				g.setColor( vLinesColorV.get(i) );
				g.drawLine(x1, y1, x2, y2);
			}
		}

		//------------------------------
		//draw Grid Limits lines
		//------------------------------
		if (mouseDrugged) {
			if (zoomGridLimitsV.size() > 0) {
				GridLimits tmpGL = zoomGridLimitsV.lastElement();
				g.setColor(tmpGL.getColor());
				x1 = getScreenX(tmpGL.getMinX());
				y1 = getScreenY(tmpGL.getMaxY());
				x2 = getScreenX(tmpGL.getMaxX());
				y2 = getScreenY(tmpGL.getMinY());
				g.drawRect(x1, y1, x2 - x1, y2 - y1);
			}
		}

		//----------------------------------------------
		//draw the clicked point cross
		//----------------------------------------------
		if (clickedPoint.isDisplayed) {
			if (colorSurfaceData == null) {
				g.setColor(clickedPoint.pointColor);
			} else {
				g.setColor(Color.white);
			}
			x1 = getScreenX(clickedPoint.getX());
			y1 = getScreenY(clickedPoint.getY());
			g.drawLine(xLOffSet, y1, screenW - xROffSet, y1);
			g.drawLine(x1, yUOffSet, x1, screenH - yBOffSet);
		}

		//---------------------------------------------------------
		//restore initial transform and stroke and others parameter
		//---------------------------------------------------------
		g.setBackground(BackgroundInitial);
		g.setColor(colorInitial);
		g.setTransform(transInitial);
		g.setStroke(strokeInitial);
		g.setFont(fontInitial);

		//---------------------------------------------------------
		//draw the legend
		//---------------------------------------------------------
		legend.drawLegend(g);

		if (bkgBorderAreaColor == null) {
			bkgBorderAreaColor = getBackground();
		} else {
			g.setBackground(bkgBorderAreaColor);
		}

		//-----------------------------------------------
		//Fontmetrics
		//-----------------------------------------------
		FontMetrics fm;

		//-----------------------------------------------
		//start draw grid ticks, names etc.
		//-----------------------------------------------
		g.clearRect(0, 0, scr_w, yUOffSet);
		g.clearRect(0, scr_h - yBOffSet, scr_w, scr_h);
		g.clearRect(0, 0, xLOffSet, scr_h);
		g.clearRect(scr_w - xROffSet, 0, scr_w, scr_h);

		g.setColor(numberColor);
		g.drawRect(xLOffSet, yUOffSet, scr_w - xROffSet - xLOffSet, scr_h - yBOffSet - yUOffSet);

		int xPos;

		int yPos;

		int wLength;

		//draw name of the graph
		if (nameOfGraph != null) {
			g.setColor(nameOfGraphColor);
			g.setFont(nameOfGraphFont);
			yPos = 2 + g.getFontMetrics().getAscent();
			wLength = g.getFontMetrics().stringWidth(nameOfGraph);
			if (wLength < (scr_w - 3)) {
				xPos = (scr_w - wLength) / 2;
				g.drawString(nameOfGraph, xPos, yPos);
			}
		}

		//draw name of the X axis
		if (nameX != null) {
			g.setColor(nameXColor);
			g.setFont(nameXFont);
			yPos = scr_h - 2 - g.getFontMetrics().getDescent();
			wLength = g.getFontMetrics().stringWidth(nameX);
			if (wLength < (scr_w - xLOffSet - xROffSet - 3)) {
				xPos = xLOffSet + (scr_w - xLOffSet - xROffSet - wLength) / 2;
				g.drawString(nameX, xPos, yPos);
			}
		}

		//the x axis by itself with labels
		if (nMajorTksX > 1) {
			g.setFont(numberFont);
			g.setColor(numberColor);
			;
			double step = (xMax - xMin) / (nMinorTksX * (nMajorTksX - 1) + nMajorTksX - 1);
			double numb;
			String numbS;
			yPos = scr_h - yBOffSet;
			for (int k = 0, n = nMinorTksX * (nMajorTksX - 1) + nMajorTksX; k < n; k++) {
				numb = xMin + k * step;
				xPos = getScreenX(numb);
				if (k % (nMinorTksX + 1) == 0) {
					g.drawLine(xPos, yPos, xPos, yPos + 5);
					numbS = currentGridLimits.getNumberFormatX().format(numb * numbMarkScaleX);
					wLength = g.getFontMetrics().stringWidth(numbS);
					if (k != 0 && k != n - 1) {
						if (gridXmarkerOn) {
							g.drawString(numbS, xPos - wLength / 2, yPos + 10 + g.getFontMetrics().getAscent());
						}
					}
					if (k == 0) {
						if (gridXmarkerOn) {
							g.drawString(numbS, xPos, yPos + 10 + g.getFontMetrics().getAscent());
						}
					}
					if (k == n - 1) {
						if (gridXmarkerOn) {
							g.drawString(numbS, xPos - wLength, yPos + 10 + g.getFontMetrics().getAscent());
						}
					}
				} else {
					g.drawLine(xPos, yPos, xPos, yPos + 2);
				}
			}
		}

		//the y axis by itself with labels
		if (nMajorTksY > 1) {
			g.setFont(numberFont);
			g.setColor(numberColor);
			;
			double step = (yMax - yMin) / (nMinorTksY * (nMajorTksY - 1) + nMajorTksY - 1);
			double numb;
			String numbS;
			xPos = xLOffSet;
			for (int k = 0, n = nMinorTksY * (nMajorTksY - 1) + nMajorTksY; k < n; k++) {
				numb = yMin + k * step;
				yPos = getScreenY(numb);
				if (k % (nMinorTksY + 1) == 0) {
					g.drawLine(xPos, yPos, xPos - 5, yPos);
					numbS = currentGridLimits.getNumberFormatY().format(numb * numbMarkScaleY);
					wLength = g.getFontMetrics().stringWidth(numbS);
					if (k != 0 && k != n - 1) {
						if (gridYmarkerOn) {
							g.drawString(numbS, xPos - wLength - 10, yPos + g.getFontMetrics().getAscent() / 2);
						}
					}
					if (k == 0) {
						if (gridYmarkerOn) {
							g.drawString(numbS, xPos - wLength - 10, yPos);
						}
					}
					if (k == n - 1) {
						if (gridYmarkerOn) {
							g.drawString(numbS, xPos - wLength - 10, yPos + g.getFontMetrics().getAscent());
						}
					}
				} else {
					g.drawLine(xPos, yPos, xPos - 2, yPos);
				}
			}
		}

		//draw name of the Y axis
		if (nameY != null) {

			AffineTransform transYaxisDraw = new AffineTransform(
					0.0f, -1.0f, 1.0f, 0.0f, (float) 0, (float) scr_h);
			g.setTransform(transYaxisDraw);

			g.setColor(nameYColor);
			g.setFont(nameYFont);
			yPos = g.getFontMetrics().getAscent() + 2;
			wLength = g.getFontMetrics().stringWidth(nameY);
			if (wLength < (scr_h - yBOffSet - yUOffSet - 3)) {
				xPos = yBOffSet + (scr_h - yBOffSet - yUOffSet - wLength) / 2;
				g.drawString(nameY, xPos, yPos);
			}

			g.setTransform(transInitial);
		}

		//draw the markers for vertical and horizontal lines
		if (dragHorLinesModeYes) {
			double yP = 0.;
			if (hLinesV.size() > 0) {
				x1 = xLOffSet - 2;
				for (int i = hLinesV.size() - 1; i >= 0; i--) {
					yP = hLinesV.get(i).doubleValue();
					if (yP > yMaxIn) {
						yP = yMaxIn;
					}
					if (yP < yMinIn) {
						yP = yMinIn;
					}
					y1 = getScreenY(yP);
					if (y1 < yUOffSet) {
						y1 = yUOffSet;
					}
					if (y1 > (screenH - yBOffSet)) {
						y1 = (screenH - yBOffSet);
					}
					g.setColor( hLinesColorV.get(i) );
					triangleMarkerLeft.translate(x1, y1);
					g.fill(triangleMarkerLeft);
					triangleMarkerLeft.translate(-x1, -y1);
				}
			}
		}
		if (dragVerLinesModeYes) {
			double xP = 0.;
			if (vLinesV.size() > 0) {
				y1 = screenH - yBOffSet + 2;
				for (int i = vLinesV.size() - 1; i >= 0; i--) {
					xP = vLinesV.get(i).doubleValue();
					if (xP > xMaxIn) {
						xP = xMaxIn;
					}
					if (xP < xMinIn) {
						xP = xMinIn;
					}
					x1 = getScreenX(xP);
					if (x1 < xLOffSet) {
						x1 = xLOffSet;
					}
					if (x1 > (screenW - xROffSet)) {
						x1 = (screenW - xROffSet);
					}
					g.setColor( vLinesColorV.get(i) );
					triangleMarkerRight.translate(x1, y1);
					g.fill(triangleMarkerRight);
					triangleMarkerRight.translate(-x1, -y1);
				}
			}
		}

		//---------------------------------------------------------
		//restore initial all parameter
		//---------------------------------------------------------
		g.setColor(colorInitial);
		g.setTransform(transInitial);
		g.setStroke(strokeInitial);
		g.setFont(fontInitial);

		//draw the buttons
		Insets insets = getInsets();
		int ixButton = insets.left;
		int iyButton = insets.top;

		if (chooseModeButtonVisible) {
			chooseModeButton.setBounds(ixButton, iyButton,
					(int) chooseModeButton.getPreferredSize().getWidth(),
					(int) chooseModeButton.getPreferredSize().getHeight());
			ixButton += chooseModeButton.getPreferredSize().getWidth();
		}

		if (horLinesModeButtonVisible) {
			dragHorLinesModeButton.setBounds(ixButton, iyButton,
					(int) dragHorLinesModeButton.getPreferredSize().getWidth(),
					(int) dragHorLinesModeButton.getPreferredSize().getHeight());
			ixButton += dragHorLinesModeButton.getPreferredSize().getWidth();
		}

		if (verLinesModeButtonVisible) {
			dragVerLinesModeButton.setBounds(ixButton, iyButton,
					(int) dragVerLinesModeButton.getPreferredSize().getWidth(),
					(int) dragVerLinesModeButton.getPreferredSize().getHeight());
			ixButton += dragVerLinesModeButton.getPreferredSize().getWidth();
		}
		if (legendButtonVisible) {
			legendButton.setBounds(ixButton, iyButton,
					(int) legendButton.getPreferredSize().getWidth(),
					(int) legendButton.getPreferredSize().getHeight());
			ixButton += legendButton.getPreferredSize().getWidth();
		}

		//commit actions for x and y-limits changes
		if (xLimChanged) {
			for (int k = 0, n = horLimListenersV.size(); k < n; k++) {
				horLimListenersV.get(k).actionPerformed(horLimEvent);
			}
		}
		if (yLimChanged) {
			for (int k = 0, n = verLimListenersV.size(); k < n; k++) {
				verLimListenersV.get(k).actionPerformed(verLimEvent);
			}
		}
	}


	//---------------------------------------------------------
	//EDN of drawing
	//---------------------------------------------------------

	/**
	 *  Returns the screenX attribute of the FunctionGraphsJPanel object
	 *
	 *@param  x  Description of the Parameter
	 *@return    The screenX value
	 */
	public int getScreenX(double x) {
		return ((int) ((x - xMin) * scaleX)) + xLOffSet;
	}


	/**
	 *  Returns the screenY attribute of the FunctionGraphsJPanel object
	 *
	 *@param  y  Description of the Parameter
	 *@return    The screenY value
	 */
	public int getScreenY(double y) {
		return screenH + ((int) ((y - yMin) * scaleY)) - yBOffSet;
	}


	/**
	 *  Returns the fromScreenX attribute of the FunctionGraphsJPanel object
	 *
	 *@param  ix  Description of the Parameter
	 *@return     The fromScreenX value
	 */
	private double getFromScreenX(int ix) {
		if (scaleX != 0.) {
			return xMin + (ix - xLOffSet) / scaleX;
		} else {
			return (xMin + xMax) / 2.0;
		}
	}


	/**
	 *  Returns the fromScreenY attribute of the FunctionGraphsJPanel object
	 *
	 *@param  iy  Description of the Parameter
	 *@return     The fromScreenY value
	 */
	private double getFromScreenY(int iy) {
		if (scaleY != 0.) {
			return yMax + (iy - yUOffSet) / scaleY;
		} else {
			return (yMin + yMax) / 2.0;
		}
	}


	//------------------------------------------
	//methods realted to the mouse events
	//------------------------------------------

	//MouseListener implementation

	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseClicked(MouseEvent e) {
		if (nTotalGraphPoints == 0 && colorSurfaceData == null && nTotalCurvePoints == 0) {
			return;
		}
		mouseUsedButton = e.getButton();
		if (mouseUsedButton == MouseEvent.BUTTON1) {
			if (mouseDrugged == true) {
				return;
			}
			if (e.getClickCount() == 2) {
				int eX = e.getX();
				int eY = e.getY();
				if (eX < xLOffSet && eX > fSizeY &&
						eY > yUOffSet && eY < (screenH - yBOffSet)) {
					//operates with Y-axis
					JDialog dialog = getAxisParamDialog();
					if (dialog == null) {
						return;
					}
					glPanel.setXYchooser(dialog, 1);
					dialog.pack();
					dialog.setVisible(true);
					updateGraphJPanel();
					return;
				}
				if (eX > xLOffSet && eX < (screenW - xROffSet) &&
						eY < (screenH - fSizeX) && eY > (screenH - yBOffSet)) {
					//operates with X-axis
					JDialog dialog = getAxisParamDialog();
					if (dialog == null) {
						return;
					}
					glPanel.setXYchooser(dialog, 0);
					dialog.pack();
					dialog.setVisible(true);
					updateGraphJPanel();
					return;
				}
				if (zoomGridLimitsV.size() > 0) {
					zoomGridLimitsV.removeElementAt(zoomGridLimitsV.size() - 1);
				}
				clickedPoint.setDisplayed(false);
				if (graphChoosingYes) {
					unChooseGraph();
				} else {
					clickedPoint.setDisplayed(false);
				}
				updateGraphJPanel();
			}
			if (e.getClickCount() == 1) {
				int eX = e.getX();
				int eY = e.getY();
				if (eX < xLOffSet || eX > (screenW - xROffSet) ||
						eY < yUOffSet || eY > (screenH - yBOffSet)) {
					clickedPoint.setDisplayed(false);
					if (graphChoosingYes) {
						unChooseGraph();
					}
					updateGraphJPanel();
					return;
				}

				if (legend.isInside(eX, eY)) {
					clickedPoint.setDisplayed(false);
					if (!chooseGraphFromLegend(eX, eY)) {
						if (graphChoosingYes) {
							unChooseGraph();
						}
					}
					updateGraphJPanel();
					return;
				}

				double tmp_x = getFromScreenX(eX);
				double tmp_y = getFromScreenY(eY);
				if (graphChoosingYes) {
					chooseGraph(tmp_x, tmp_y);
				} else {
					if (colorSurfaceData == null) {
						clickedPoint.updateValues(tmp_x, tmp_y);
					} else {
						clickedPoint.updateValues(tmp_x, tmp_y, colorSurfaceData.getValue(tmp_x, tmp_y));
					}
					clickedPoint.setDisplayed(true);
				}
				updateGraphJPanel();
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mousePressed(MouseEvent e) {
		mouseUsedButton = e.getButton();
		evntIniX = e.getX();
		evntIniY = e.getY();
		if (mouseUsedButton == MouseEvent.BUTTON1) {
			mouseDrugged = false;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseReleased(MouseEvent e) {
		mouseUsedButton = e.getButton();
		if (mouseUsedButton == MouseEvent.BUTTON1) {
			if (mouseDrugged == false) {
				mouseDraggedTaskType = -1;
				return;
			}
			mouseDrugged = false;
			if (mouseDraggedTaskType == 1 || mouseDraggedTaskType == 2) {
				if (mouseDraggedTaskType == 1 && draggedHorLinesListener != null && draggedLinesIndex >= 0) {
					draggedHorLinesListener.actionPerformed(draggedHorLinesEvent);
				}
				if (mouseDraggedTaskType == 2 && draggedVerLinesListener != null && draggedLinesIndex >= 0) {
					draggedVerLinesListener.actionPerformed(draggedVerLinesEvent);
				}
			}
			if (mouseDraggedTaskType == 0) {
				GridLimits GL = zoomGridLimitsV.lastElement();
				if (GL != null) {
					int iX = getScreenX(GL.getMinX());
					int eX = getScreenX(GL.getMaxX());
					int iY = getScreenY(GL.getMinY());
					int eY = getScreenY(GL.getMaxY());
					if (Math.abs(iX - eX) < 5 && Math.abs(iY - eY) < 5) {
						zoomGridLimitsV.removeElement(GL);
					} else {
						GL.setSmartLimits();
					}
				}
			}
		}
		mouseDraggedTaskType = -1;
		updateGraphJPanel();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseEntered(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseExited(MouseEvent e) { }


	//MouseMotionListener implementation

	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseDragged(MouseEvent e) {
		if (mouseUsedButton == MouseEvent.BUTTON1) {
			if (mouseDrugged == false) {
				int eX = e.getX();
				int eY = e.getY();
				if (evntIniX < xLOffSet || evntIniX > (screenW - xROffSet) ||
						evntIniY < yUOffSet || evntIniY > (screenH - yBOffSet)) {

					if (!dragHorLinesModeYes && !dragVerLinesModeYes) {
						return;
					}

					if (dragHorLinesModeYes) {
						if (evntIniX > xLOffSet - 10 && evntIniX < (screenW - xLOffSet) &&
								evntIniY > yUOffSet - 5 && evntIniY < (screenH - yBOffSet + 5)) {
							//horizontal lines dragging
							mouseDraggedTaskType = 1;
							draggedLinesIndex = getNearestHorizontalLineIndex(getFromScreenY(evntIniY));
						}
					}
					if (dragVerLinesModeYes) {
						if (evntIniX > xLOffSet - 5 && evntIniX < (screenW - xROffSet + 3) &&
								evntIniY < (screenH - yBOffSet) + 10 && evntIniY > (screenH - yBOffSet)) {
							//vertical lines dragging
							mouseDraggedTaskType = 2;
							draggedLinesIndex = getNearestVerticalLineIndex(getFromScreenX(evntIniX));
						}
					}
				} else {
					if (!legend.isInside(evntIniX, evntIniY)) {
						if (nTotalGraphPoints == 0 && colorSurfaceData == null && nTotalCurvePoints == 0) {
							return;
						}
						//zoom dragging
						mouseDraggedTaskType = 0;
						GridLimits gl = null;
						if (useSmartGridLimits) {
							gl = new SmartFormatGridLimits();
						} else {
							gl = new GridLimits();
							gl.setNumberFormatX(numberFormatX);
							gl.setNumberFormatY(numberFormatY);
						}
						zoomGridLimitsV.add(gl);
						zoomGridLimitsV.lastElement().setXmin(getFromScreenX(evntIniX));
						zoomGridLimitsV.lastElement().setYmin(getFromScreenY(evntIniY));
					} else {
						//legend dragging
						mouseDraggedTaskType = 3;
						legend.memorizePosition();
					}
				}
			}

			mouseDrugged = true;
			int eX = e.getX();
			int eY = e.getY();

			if (mouseDraggedTaskType == 0) {
				GridLimits GL = zoomGridLimitsV.lastElement();
				GL.initialize();

				if (eX > evntIniX) {
					GL.setXmin(getFromScreenX(evntIniX));
					GL.setXmax(getFromScreenX(eX));
				} else {
					GL.setXmin(getFromScreenX(eX));
					GL.setXmax(getFromScreenX(evntIniX));
				}

				if (eY < evntIniY) {
					GL.setYmin(getFromScreenY(evntIniY));
					GL.setYmax(getFromScreenY(eY));
				} else {
					GL.setYmin(getFromScreenY(eY));
					GL.setYmax(getFromScreenY(evntIniY));
				}
			}

			if (mouseDraggedTaskType == 1 && draggedLinesIndex >= 0) {
				hLinesV.remove(draggedLinesIndex);
				hLinesV.add(draggedLinesIndex, new Double(getFromScreenY(eY)));
				if (draggedHorLinesListener != null && draggedHorLinesMotionListenYes) {
					draggedHorLinesListener.actionPerformed(draggedHorLinesEvent);
				}
			}

			if (mouseDraggedTaskType == 2 && draggedLinesIndex >= 0) {
				vLinesV.remove(draggedLinesIndex);
				vLinesV.add(draggedLinesIndex, new Double(getFromScreenX(eX)));
				if (draggedVerLinesListener != null && draggedVerLinesMotionListenYes) {
					draggedVerLinesListener.actionPerformed(draggedHorLinesEvent);
				}
			}

			if (mouseDraggedTaskType == 3) {
				legend.movePosition(eX - evntIniX, eY - evntIniY);
			}

			updateGraphJPanel();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseMoved(MouseEvent e) { }


	//-------------------------------------------------------------
	//Inner class with current x and y values of the point
	//clicked by mouse
	//--------------------------------------------------------------

	/**
	 *  Description of the Class
	 *
	 *@author     shishlo
	 *@version    July 22, 2004
	 */
	public class ClickedPoint {

		/**
		 *  Description of the Field
		 */
		public JTextField xValueText = new JTextField(5);
		/**
		 *  Description of the Field
		 */
		public JTextField yValueText = new JTextField(5);
		/**
		 *  Description of the Field
		 */
		public JTextField zValueText = new JTextField(5);

		/**
		 *  Description of the Field
		 */
		public NumberFormat xValueFormat = new DecimalFormat("0.00E0");
		/**
		 *  Description of the Field
		 */
		public NumberFormat yValueFormat = new DecimalFormat("0.00E0");
		/**
		 *  Description of the Field
		 */
		public NumberFormat zValueFormat = new DecimalFormat("0.00E0");

		/**
		 *  Description of the Field
		 */
		public JLabel xValueLabel = new JLabel(" X= ", SwingConstants.CENTER);
		/**
		 *  Description of the Field
		 */
		public JLabel yValueLabel = new JLabel(" Y= ", SwingConstants.CENTER);
		/**
		 *  Description of the Field
		 */
		public JLabel zValueLabel = new JLabel(" Z= ", SwingConstants.CENTER);

		/**
		 *  Description of the Field
		 */
		public Color pointColor = Color.blue;

		private boolean isDisplayed = false;

		private double x = 0., y = 0., z = 0.;


		/**
		 *  Constructor for the ClickedPoint object
		 */
		public ClickedPoint() {
			xValueText.setBackground(Color.white);
			yValueText.setBackground(Color.white);
			zValueText.setBackground(Color.white);

			xValueText.setHorizontalAlignment(JTextField.CENTER);
			yValueText.setHorizontalAlignment(JTextField.CENTER);
			zValueText.setHorizontalAlignment(JTextField.CENTER);

			xValueText.setEditable(false);
			yValueText.setEditable(false);
			zValueText.setEditable(false);

			xValueText.setText(null);
			yValueText.setText(null);
			zValueText.setText(null);
		}


		/** Set the specified decimal format for the X value label */
		@SuppressWarnings("cast")		// suppress cast warning as we check for it explitly before casting
		public void setDecimalFormatX( final String pattern ) {
			// if the format is already a DecimalFormat just apply the pattern
			if ( xValueFormat instanceof DecimalFormat ) {
				((DecimalFormat)xValueFormat).applyPattern( pattern );
			} else {	// create a new DecimalFormat wit the specified pattern
				xValueFormat = new DecimalFormat( pattern );
			}
		}


		/** Set the specified decimal format for the Y value label */
		@SuppressWarnings("cast")		// suppress cast warning as we check for it explitly before casting
		public void setDecimalFormatY( final String pattern ) {
			// if the format is already a DecimalFormat just apply the pattern
			if ( yValueFormat instanceof DecimalFormat ) {
				((DecimalFormat)yValueFormat).applyPattern( pattern );
			} else {	// create a new DecimalFormat wit the specified pattern
				yValueFormat = new DecimalFormat( pattern );
			}
		}


		/** Set the specified decimal format for the Z value label */
		@SuppressWarnings("cast")		// suppress cast warning as we check for it explitly before casting
		public void setDecimalFormatZ( final String pattern ) {
			// if the format is already a DecimalFormat just apply the pattern
			if ( zValueFormat instanceof DecimalFormat ) {
				((DecimalFormat)zValueFormat).applyPattern( pattern );
			} else {	// create a new DecimalFormat wit the specified pattern
				zValueFormat = new DecimalFormat( pattern );
			}
		}


		/**
		 *  Sets the font attribute of the ClickedPoint object
		 *
		 *@param  fnt  The new font value
		 */
		public void setFont(Font fnt) {
			xValueText.setFont(fnt);
			yValueText.setFont(fnt);
			zValueText.setFont(fnt);
			xValueLabel.setFont(fnt);
			yValueLabel.setFont(fnt);
			zValueLabel.setFont(fnt);
		}


		/**
		 *  Description of the Method
		 *
		 *@param  x  Description of the Parameter
		 *@param  y  Description of the Parameter
		 */
		private void updateValues(double x, double y) {
			xValueText.setText(null);
			yValueText.setText(null);
			zValueText.setText(null);
			xValueText.setText(xValueFormat.format(x));
			yValueText.setText(yValueFormat.format(y));
			this.x = x;
			this.y = y;
		}


		/**
		 *  Description of the Method
		 *
		 *@param  x  Description of the Parameter
		 *@param  y  Description of the Parameter
		 *@param  z  Description of the Parameter
		 */
		private void updateValues(double x, double y, double z) {
			xValueText.setText(null);
			yValueText.setText(null);
			zValueText.setText(null);
			xValueText.setText(xValueFormat.format(x));
			yValueText.setText(yValueFormat.format(y));
			zValueText.setText(zValueFormat.format(z));
			this.x = x;
			this.y = y;
			this.z = z;
		}


		/**
		 *  Sets the displayed attribute of the ClickedPoint object
		 *
		 *@param  isDisplayedIn  The new displayed value
		 */
		private void setDisplayed(boolean isDisplayedIn) {
			isDisplayed = isDisplayedIn;
			if (!isDisplayedIn) {
				xValueText.setText(null);
				yValueText.setText(null);
				zValueText.setText(null);
			}
		}


		/**
		 *  Returns the displayed attribute of the ClickedPoint object
		 *
		 *@return    The displayed value
		 */
		private boolean isDisplayed() {
			return isDisplayed;
		}


		/**
		 *  Returns the x attribute of the ClickedPoint object
		 *
		 *@return    The x value
		 */
		private double getX() {
			return x;
		}


		/**
		 *  Returns the y attribute of the ClickedPoint object
		 *
		 *@return    The y value
		 */
		private double getY() {
			return y;
		}


		/**
		 *  Returns the z attribute of the ClickedPoint object
		 *
		 *@return    The z value
		 */
		private double getZ() {
			return z;
		}
	}


	//-------------------------------------------------------------
	//Inner class with grid limits
	//--------------------------------------------------------------



	//----------------------------------------------
	//dialog panel to chose grid limits
	//----------------------------------------------
	/**
	 *  Description of the Class
	 *
	 *@author     shishlo
	 *@version    July 22, 2004
	 */
	private class gridLimitsPanel extends JPanel {
        /** serialization ID */
        private static final long serialVersionUID = 1L;
        

		private FunctionGraphsJPanel fgp = null;

		//0 - operates with x 1 - with y
		private int xyChooser = 0;

		/**
		 *  Description of the Field
		 */
		private NumberFormat numbFormatX = new DecimalFormat("0.00E0");
		/**
		 *  Description of the Field
		 */
		private NumberFormat numbFormatY = new DecimalFormat("0.00E0");

		private NumberFormat defaultDoubleNumbFormat = new DecimalFormat("0.000E0");
		private NumberFormat defaultIntNumbFormat = new DecimalFormat("###");

		private JButton applyButton = new JButton("APPLY");

		/**
		 *  Description of the Field
		 */
		private JRadioButton customButton = new JRadioButton("CUSTOM", false);
		/**
		 *  Description of the Field
		 */
		private JRadioButton autoButton = new JRadioButton("AUTO", true);

		/**
		 *  Description of the Field
		 */
		private boolean autoScaleOn = true;

		/**
		 *  Description of the Field
		 */
		private JLabel minValLabel = new JLabel("min value=", JLabel.RIGHT);
		/**
		 *  Description of the Field
		 */
		private JLabel maxValLabel = new JLabel("max value=", JLabel.RIGHT);
		/**
		 *  Description of the Field
		 */
		private JLabel nStepLabel = new JLabel("N step = ", JLabel.RIGHT);
		/**
		 *  Description of the Field
		 */
		private JLabel minorTicksLabel = new JLabel(" N minor ticks = ", JLabel.RIGHT);

		/**
		 *  Description of the Field
		 */
		private JTextField minValText = new JTextField(8);
		/**
		 *  Description of the Field
		 */
		private JTextField maxValText = new JTextField(8);
		/**
		 *  Description of the Field
		 */
		private JTextField nStepText = new JTextField(8);
		/**
		 *  Description of the Field
		 */
		private JTextField minorTicksText = new JTextField(8);


		/**
		 *  Constructor for the gridLimitsPanel object
		 */
		private gridLimitsPanel() {
			super();

			minValText.setEditable(false);
			maxValText.setEditable(false);
			nStepText.setEditable(false);
			minorTicksText.setEditable(false);

			minValText.setHorizontalAlignment(JTextField.CENTER);
			maxValText.setHorizontalAlignment(JTextField.CENTER);
			nStepText.setHorizontalAlignment(JTextField.CENTER);
			minorTicksText.setHorizontalAlignment(JTextField.CENTER);

			customButton.setHorizontalTextPosition(SwingConstants.RIGHT);
			autoButton.setHorizontalTextPosition(SwingConstants.RIGHT);

			applyButton.setHorizontalTextPosition(SwingConstants.CENTER);

			minValText.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
			maxValText.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
			nStepText.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
			minorTicksText.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));

			customButton.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
			autoButton.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));

			applyButton.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));

			minValLabel.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
			maxValLabel.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
			nStepLabel.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));
			minorTicksLabel.setFont(new Font(getFont().getFamily(), Font.BOLD, 12));

			ButtonGroup groupB = new ButtonGroup();
			groupB.add(customButton);
			groupB.add(autoButton);

			JPanel temp_panel = new JPanel();
			temp_panel.setLayout(new GridLayout(0, 2, 1, 1));
			temp_panel.add(customButton);
			temp_panel.add(autoButton);
			temp_panel.add(minValLabel);
			temp_panel.add(minValText);
			temp_panel.add(maxValLabel);
			temp_panel.add(maxValText);
			temp_panel.add(nStepLabel);
			temp_panel.add(nStepText);
			temp_panel.add(minorTicksLabel);
			temp_panel.add(minorTicksText);

			setLayout(new BorderLayout());
			add(temp_panel, BorderLayout.CENTER);
			add(applyButton, BorderLayout.SOUTH);

			//actions
			customButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						minValText.setEditable(true);
						maxValText.setEditable(true);
						nStepText.setEditable(true);
						minorTicksText.setEditable(true);
						autoScaleOn = false;
					}
				});

			autoButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						minValText.setEditable(false);
						maxValText.setEditable(false);
						nStepText.setEditable(false);
						minorTicksText.setEditable(false);
						autoScaleOn = true;
					}
				});

			applyButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (autoScaleOn) {
							GridLimits gl = fgp.getCurrentGL();
							if (xyChooser == 0) {
								//gl.initializeX();
								gl.setXminOn(false);
								gl.setXmaxOn(false);
								gl.setMajorTicksOnX(false);
							}
							if (xyChooser == 1) {
								//gl.initializeY();
								gl.setYminOn(false);
								gl.setYmaxOn(false);
								gl.setMajorTicksOnY(false);
							}
						} else {
							GridLimits gl = fgp.getCurrentGL();
							boolean successForMinValue = true;
							boolean successForMaxValue = true;
							boolean successForMajorTicks = true;
							boolean successForMinorTicks = true;
							double minVal = 0.;
							double maxVal = 0.;
							int nStep = 1;
							int nMinorTicks = 4;

							try {
								minVal = Double.parseDouble(minValText.getText());
							} catch (NumberFormatException exc) {
								minValText.setText(null);
								successForMinValue = false;
							}

							try {
								maxVal = Double.parseDouble(maxValText.getText());
							} catch (NumberFormatException exc) {
								maxValText.setText(null);
								successForMaxValue = false;
							}

							try {
								nStep = Integer.parseInt(nStepText.getText());
							} catch (NumberFormatException exc) {
								nStepText.setText(null);
								successForMajorTicks = false;
							}

							try {
								nMinorTicks = Integer.parseInt(minorTicksText.getText());
							} catch (NumberFormatException exc) {
								minorTicksText.setText(null);
								successForMinorTicks = false;
							}

							if (successForMinValue) {
								if (xyChooser == 0) {
									gl.setXmin(minVal);
								}
								if (xyChooser == 1) {
									gl.setYmin(minVal);
								}
							}

							if (successForMaxValue) {
								if (xyChooser == 0) {
									gl.setXmax(maxVal);
								}
								if (xyChooser == 1) {
									gl.setYmax(maxVal);
								}
							}

							if (successForMajorTicks) {
								if (xyChooser == 0) {
									gl.setNumMajorTicksX(nStep + 1);
									gl.setMajorTicksOnX(true);
								}
								if (xyChooser == 1) {
									gl.setNumMajorTicksY(nStep + 1);
									gl.setMajorTicksOnY(true);
								}
							}

							if (successForMinorTicks) {
								if (xyChooser == 0) {
									gl.setNumMinorTicksX(nMinorTicks);
								}
								if (xyChooser == 1) {
									gl.setNumMinorTicksY(nMinorTicks);
								}
							}
						}
						fgp.refreshGraphJPanel();
					}
				});

		}


		/**
		 *  Sets the functionGraphsJPanel attribute of the gridLimitsPanel object
		 *
		 *@param  fgpIn  The new functionGraphsJPanel value
		 */
		private void setFunctionGraphsJPanel(FunctionGraphsJPanel fgpIn) {
			fgp = fgpIn;
		}


		/**
		 *  Sets the numberFormatX attribute of the gridLimitsPanel object
		 *
		 *@param  df  The new numberFormatX value
		 */
		private void setNumberFormatX(NumberFormat df) {
			numbFormatX = df;
		}


		/**
		 *  Sets the numberFormatY attribute of the gridLimitsPanel object
		 *
		 *@param  df  The new numberFormatY value
		 */
		private void setNumberFormatY(NumberFormat df) {
			numbFormatY = df;
		}


		/**
		 *  Sets the xYchooser attribute of the gridLimitsPanel object
		 *
		 *@param  axisDialogIn  The new xYchooser value
		 *@param  xyChooserIn   The new xYchooser value
		 */
		private void setXYchooser(JDialog axisDialogIn, int xyChooserIn) {
			xyChooser = xyChooserIn;
			initSet();
			if (xyChooserIn == 0) {
				axisDialogIn.setTitle("X - Axis Grid");
			}
			if (xyChooserIn == 1) {
				axisDialogIn.setTitle("Y - Axis Grid");
			}
			axisDialogIn.getContentPane().add(this);
		}


		/**
		 *  Description of the Method
		 */
		private void initSet() {
			autoScaleOn = true;

			minValText.setEditable(false);
			maxValText.setEditable(false);
			nStepText.setEditable(false);
			minorTicksText.setEditable(false);

			if (fgp != null && fgp.getCurrentGL() != null) {
				GridLimits gl = fgp.getCurrentGL();
				if (xyChooser == 0) {
					minValText.setText(defaultDoubleNumbFormat.format(gl.getMinX()));
					maxValText.setText(defaultDoubleNumbFormat.format(gl.getMaxX()));
					nStepText.setText(defaultIntNumbFormat.format(gl.getNumMajorTicksX() - 1));
					minorTicksText.setText(defaultIntNumbFormat.format(gl.getNumMinorTicksX()));
				}
				if (xyChooser == 1) {
					minValText.setText(defaultDoubleNumbFormat.format(gl.getMinY()));
					maxValText.setText(defaultDoubleNumbFormat.format(gl.getMaxY()));
					nStepText.setText(defaultIntNumbFormat.format(gl.getNumMajorTicksY() - 1));
					minorTicksText.setText(defaultIntNumbFormat.format(gl.getNumMinorTicksY()));
				}
			} else {

				minValText.setText(null);
				maxValText.setText(null);
				nStepText.setText(null);
				minorTicksText.setText(null);
			}

			customButton.setSelected(false);
			autoButton.setSelected(true);
		}
	}


	//--------------------------------------------
	//graph legend class
	//--------------------------------------------

	/**
	 *  Description of the Class
	 *
	 *@author     shishlo
	 *@version    July 22, 2004
	 */
	private class graphLegend {

		private String legendName = "Legend";

		private FunctionGraphsJPanel fgp = null;

		private boolean showLegend = true;

		private int legend_w, legend_h;

		private Font font = null;

		/**
		 *  Description of the Field
		 */
		private int POSITION_ARBITRARY = 0;
		/**
		 *  Description of the Field
		 */
		private int POSITION_TOP_LEFT = 1;
		/**
		 *  Description of the Field
		 */
		private int POSITION_TOP_RIGHT = 2;
		/**
		 *  Description of the Field
		 */
		private int POSITION_BOTTOM_LEFT = 3;
		/**
		 *  Description of the Field
		 */
		private int POSITION_BOTTOM_RIGHT = 4;

		private int position;
		private int position_x = 0;
		private int position_y = 0;
		private int mem_position_x = 0;
		private int mem_position_y = 0;
		private int legend_H = 0;
		private int legend_W = 0;
		private int line_w = 0;
		private int line_h = 0;

		//colors
		/**
		 *  Description of the Field
		 */
		private Color backGroundColor = null;
		/**
		 *  Description of the Field
		 */
		private Color borderColor = null;

		//array for data indexes
		/**
		 *  Description of the Field
		 */
		private int[] dataIndA = new int[10];
		/**
		 *  Description of the Field
		 */
		private int nDataInd = 0;


		//constructor should be called inside FunctionGraphsJPanel constructor
		/**
		 *  Constructor for the graphLegend object
		 *
		 *@param  fgp  Description of the Parameter
		 */
		private graphLegend(FunctionGraphsJPanel fgp) {
			this.fgp = fgp;
			font = fgp.getFont();
			position = POSITION_ARBITRARY;
			position_x = 0;
			position_y = 0;
		}


		/**
		 *  Sets the keyString attribute of the graphLegend object
		 *
		 *@param  legendKeyString  The new keyString value
		 */
		private void setKeyString(String legendKeyString) {
			legendName = legendKeyString;
		}


		/**
		 *  Sets the font attribute of the graphLegend object
		 *
		 *@param  font  The new font value
		 */
		private void setFont(Font font) {
			this.font = font;
		}


		/**
		 *  Sets the color attribute of the graphLegend object
		 *
		 *@param  cl  The new color value
		 */
		private void setColor(Color cl) {
			borderColor = cl;
		}


		/**
		 *  Sets the background attribute of the graphLegend object
		 *
		 *@param  cl  The new background value
		 */
		private void setBackground(Color cl) {
			backGroundColor = cl;
		}


		/**
		 *  Sets the visible attribute of the graphLegend object
		 *
		 *@param  showLegend  The new visible value
		 */
		private void setVisible(boolean showLegend) {
			this.showLegend = showLegend;
		}


		/**
		 *  Returns the visible attribute of the graphLegend object
		 *
		 *@return    The visible value
		 */
		private boolean isVisible() {
			return showLegend;
		}


		/**
		 *  Sets the position attribute of the graphLegend object
		 *
		 *@param  pos  The new position value
		 */
		private void setPosition(int pos) {
			if (pos == POSITION_TOP_LEFT ||
					pos == POSITION_TOP_RIGHT ||
					pos == POSITION_BOTTOM_LEFT ||
					pos == POSITION_BOTTOM_RIGHT) {
				position = pos;
			} else {
				position = POSITION_ARBITRARY;
				position_x = 0;
				position_y = 0;
			}
		}


		/**
		 *  Description of the Method
		 */
		private void memorizePosition() {
			mem_position_x = position_x;
			mem_position_y = position_y;
		}


		/**
		 *  Description of the Method
		 *
		 *@param  iX  Description of the Parameter
		 *@param  iY  Description of the Parameter
		 */
		private void movePosition(int iX, int iY) {
			if (position != POSITION_ARBITRARY) {
				return;
			}
			position_x = mem_position_x + iX;
			position_y = mem_position_y + iY;
		}


		/**
		 *  Sets the position attribute of the graphLegend object
		 *
		 *@param  iX  The new position value
		 *@param  iY  The new position value
		 */
		private void setPosition(int iX, int iY) {
			if (position != POSITION_ARBITRARY) {
				return;
			}
			position_x = iX - xLOffSet;
			position_y = iY - yUOffSet;
		}


		/**
		 *  Returns the inside attribute of the graphLegend object
		 *
		 *@param  iX  Description of the Parameter
		 *@param  iY  Description of the Parameter
		 *@return     The inside value
		 */
		private boolean isInside(int iX, int iY) {
			if (!showLegend) {
				return false;
			}
			int scr_h = fgp.getHeight();
			int scr_w = fgp.getWidth();
			int xLOffSet = fgp.xLOffSet;
			int yUOffSet = fgp.yUOffSet;
			if (iX > (xLOffSet + position_x) &&
					iX < (xLOffSet + position_x + legend_W) &&
					iY > (yUOffSet + position_y) &&
					iY < (yUOffSet + position_y + legend_H)) {
				return true;
			}
			return false;
		}


		/**
		 *  Returns the choosenGraphIndex attribute of the graphLegend object
		 *
		 *@param  iX  Description of the Parameter
		 *@param  iY  Description of the Parameter
		 *@return     The choosenGraphIndex value
		 */
		private Integer getChoosenGraphIndex(int iX, int iY) {
			if (isInside(iX, iY)) {
				int yUOffSet = fgp.yUOffSet;
				int ind = iY - yUOffSet - position_y;
				if (line_h > 0) {
					ind = (ind / line_h) - 1;
					if (ind >= 0 && ind < nDataInd) {
						ind = dataIndA[ind];
						if (ind < fgp.getNumberOfInstanceOfGraphData()) {
							return new Integer(ind);
						}
					}
				}
			}
			return null;
		}


		/**
		 *  Description of the Method
		 *
		 *@param  g  Description of the Parameter
		 */
		private void drawLegend(Graphics2D g) {

			legend_H = 0;
			legend_W = 0;

			if (!showLegend) {
				return;
			}

			int ovalX;

			int ovalY;

			int ovalW;

			int ovalH;
			int ovalW_max = 0;
			int ovalH_max = 0;

			int scr_h = fgp.getHeight();
			int scr_w = fgp.getWidth();
			int xLOffSet = fgp.xLOffSet;
			int xROffSet = fgp.xROffSet;
			int yUOffSet = fgp.yUOffSet;
			int yBOffSet = fgp.yBOffSet;

			//-----------------------------------------------
			//initial Font,Colors,Stroke
			//-----------------------------------------------
			Font fontInitial = g.getFont();
			Color colorInitial = g.getColor();
			Color backgroundInitial = g.getBackground();
			Stroke strokeInitial = g.getStroke();

			//calculation of the region's size
			g.setFont(font);

			int font_max_H = g.getFontMetrics().getLeading() +
					g.getFontMetrics().getMaxAscent() +
					g.getFontMetrics().getMaxDescent();

			//to draw string legendName at the top
			legend_H = font_max_H;
			legend_W = g.getFontMetrics().stringWidth(legendName);
			String legend = null;

			int nGraphData = fgp.getNumberOfInstanceOfGraphData();
			BasicGraphData gd = null;
			int nCount = 0;
			for (int i = 0; i < nGraphData; i++) {
				gd = fgp.getInstanceOfGraphData(i);
				if (gd.getNumbOfPoints() > 0) {

					if (gd.getGraphPointShape() == null) {
						ovalW = gd.getGraphPointSize();
						ovalH = ovalW;
					} else {
						ovalW = (int) (gd.getGraphPointShape().getBounds().getWidth());
						ovalH = (int) (gd.getGraphPointShape().getBounds().getHeight());
					}

					ovalW_max = Math.max(ovalW_max, ovalW);
					ovalH_max = Math.max(ovalH_max, ovalH);

					legend = "";
					Object legend_obj = gd.getGraphProperty(legendName);
					if (legend_obj != null) {
						legend = legend_obj.toString();
					}
					if (nCount > (dataIndA.length - 1)) {
						int[] tmp = new int[dataIndA.length + 10];
						for (int j = 0; j < dataIndA.length; j++) {
							tmp[j] = dataIndA[j];
						}
						dataIndA = tmp;
					}
					dataIndA[nCount] = i;
					if (legend != null) {
						legend_W = Math.max(legend_W, g.getFontMetrics().stringWidth(legend));
					}

					nCount++;
				}
			}

			nDataInd = nCount;

			//additional width to draw lines
			line_w = 7 * ovalW_max;
			line_h = Math.max(font_max_H, ovalH_max);
			legend_H = line_h * (nDataInd + 1);
			legend_W += line_w;

			//define position
			if (position != POSITION_ARBITRARY) {
				if (position == POSITION_TOP_LEFT) {
					position_x = 0;
					position_y = 0;
				} else if (position == POSITION_TOP_RIGHT) {
					position_x = scr_w - xROffSet - xLOffSet - legend_W;
					position_y = 0;
				} else if (position == POSITION_BOTTOM_LEFT) {
					position_x = 0;
					position_y = scr_h - yUOffSet - yBOffSet - legend_H;
				} else if (position == POSITION_BOTTOM_RIGHT) {
					position_x = scr_w - xROffSet - xLOffSet - legend_W;
					position_y = scr_h - yUOffSet - yBOffSet - legend_H;
				}
			} else {
				if (position_x < 0) {
					position_x = 0;
				}
				if (position_y < 0) {
					position_y = 0;
				}
				if (position_x + legend_W > scr_w - xROffSet - xLOffSet) {
					position_x = scr_w - xROffSet - xLOffSet - legend_W;
					if (position_x < 0) {
						position_x = 0;
					}
				}
				if (position_y + legend_H > scr_h - yUOffSet - yBOffSet) {
					position_y = scr_h - yUOffSet - yBOffSet - legend_H;
					if (position_y < 0) {
						position_y = 0;
					}
				}
			}

			//draw the border
			if (backGroundColor != null) {
				g.setBackground(backGroundColor);
			}
			g.clearRect(position_x + xLOffSet, position_y + yUOffSet, legend_W, legend_H);

			if (borderColor != null) {
				g.setColor(borderColor);
			}
			g.drawRect(position_x + xLOffSet, position_y + yUOffSet, legend_W, legend_H);

			//draw the string legendName
			int wLength = g.getFontMetrics().stringWidth(legendName);
			g.drawString(legendName, position_x + xLOffSet + legend_W / 2 - wLength / 2,
					position_y + yUOffSet + line_h - g.getFontMetrics().getMaxDescent());

			nGraphData = fgp.getNumberOfInstanceOfGraphData();
			gd = null;

			//draw the legend
			nCount = 0;
			for (int i = 0; i < nGraphData; i++) {
				gd = fgp.getInstanceOfGraphData(i);
				if (gd.getNumbOfPoints() > 0) {

					if (gd.getGraphPointShape() == null) {
						ovalW = gd.getGraphPointSize();
						ovalH = ovalW;
					} else {
						ovalW = (int) (gd.getGraphPointShape().getBounds().getWidth());
						ovalH = (int) (gd.getGraphPointShape().getBounds().getHeight());
					}

					legend = "";
					Object legend_obj = gd.getGraphProperty(legendName);
					if (legend_obj != null) {
						legend = legend_obj.toString();
					}
					Color lineColor = fgp.getGraphColor(i);
					if (lineColor == null) {
						if (gd.getGraphColor() == null) {
							lineColor = lineDefaultColor;
						} else {
							lineColor = gd.getGraphColor();
						}
					}
					g.setColor(lineColor);
					if (legend != null) {
						g.drawString(legend, position_x + xLOffSet + line_w,
								position_y + yUOffSet +
								(nCount + 1) * line_h + line_h / 2 +
								g.getFontMetrics().getMaxDescent());
					}

					if (gd.getDrawPointsOn()) {
						if (gd.getGraphPointShape() == null) {
							ovalX = position_x + xLOffSet + line_w / 2 - ovalW / 2;
							ovalY = position_y + yUOffSet + (nCount + 1) * line_h + line_h / 2 - ovalH / 2;
							g.fillOval(ovalX, ovalY, ovalW, ovalH);
						} else {
							ovalX = position_x + xLOffSet + line_w / 2;
							ovalY = position_y + yUOffSet + (nCount + 1) * line_h + line_h / 2;
							g.translate(ovalX, ovalY);
							if (gd.isGraphPointShapeFilled()) {
								g.fill(gd.getGraphPointShape());
							} else {
								g.draw(gd.getGraphPointShape());
							}
							g.translate(-ovalX, -ovalY);
						}
					}

					if (gd.getDrawLinesOn()) {
						g.setStroke(gd.getStroke());
						ovalX = position_x + xLOffSet + ovalW_max;
						ovalY = position_y + yUOffSet + (nCount + 1) * line_h + line_h / 2;
						g.drawLine(ovalX, ovalY, ovalX + line_w - 2 * ovalW_max, ovalY);
						g.setStroke(strokeInitial);
					}
					nCount++;
				}
			}
			//-----------------------------------------------
			//restore initial Font and Color
			//-----------------------------------------------
			g.setFont(fontInitial);
			g.setColor(colorInitial);
			g.setBackground(backgroundInitial);
			g.setStroke(strokeInitial);
		}

	}

}

