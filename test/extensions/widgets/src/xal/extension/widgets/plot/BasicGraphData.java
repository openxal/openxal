package xal.extension.widgets.plot;

import java.util.*;
import java.awt.*;

/**
 *  This class is a container class for data used in the FunctionGraphsJPanel
 *  class. This class interpolates y-values by the linear interpolation. For
 *  spline interpolation use the <code> CubicSplineGraphData </code> class.
 *
 *@author     A. Shishlo
 *@version    1.0
 *@see        CubicSplineGraphData
 */

public class BasicGraphData {

	//-----------------------------------------------------
	//general properties
	//-----------------------------------------------------

	/**
	 *  Description of the Field
	 */
	protected HashMap<Object,Object> propertyMap = new HashMap<Object,Object>();

	/**
	 *  Description of the Field
	 */
	protected Object lockUpObj = new Object();

	//-----------------------------------------------------
	//members defining data
	//-----------------------------------------------------

	/**
	 *  Description of the Field
	 */
	protected Vector<Object> graphDataContainerV;

	/**
	 *  Description of the Field
	 */
	protected boolean immediateContainerUpdate = true;

	/**
	 *  Description of the Field
	 */
	protected Vector<XYpoint> xyPointV;

	/**
	 *  Description of the Field
	 */
	protected Vector<XYpoint> xyInterpPointV;

	/**
	 *  Description of the Field
	 */
	protected int nInterpPoints;

	/**
	 *  Description of the Field
	 */
	protected double xMax, yMax, xMin, yMin, errYmax;

	//-----------------------------------------------------
	//members related to the graph presentation
	//-----------------------------------------------------

	/**
	 *  Description of the Field
	 */
	protected Color color = null;

	/**
	 *  Description of the Field
	 */
	protected String name = null;

	/**
	 *  Description of the Field
	 */
	protected boolean drawLinesOn = true;

	/**
	 *  Description of the Field
	 */
	protected boolean drawPointsOn = true;

	/**
	 *  Description of the Field
	 */
	protected int pointSize = 6;

	/**
	 *  Description of the Field
	 */
	protected int lineThick = 1;

	/**
	 *  Description of the Field
	 */
	protected BasicStroke lineStroke = new BasicStroke(1.0f);

	/**
	 *  Description of the Field
	 */
	protected Shape markShape = null;

	/**
	 *  Description of the Field
	 */
	protected boolean markShapeFilled = true;


	//-----------------------------------------------------
	//constructor
	//-----------------------------------------------------

	/**
	 *  data set constructor
	 */
	public BasicGraphData() {
		nInterpPoints = 100;
		init(50, nInterpPoints);
	}


	/**
	 *  data set constructor with defined initial capacity for number of (x,y)
	 *  points and interpolated points
	 *
	 *@param  nPoint           Description of the Parameter
	 *@param  nInterpPointsIn  Description of the Parameter
	 */
	public BasicGraphData(int nPoint, int nInterpPointsIn) {
		nInterpPoints = nInterpPointsIn;
		init(nPoint, nInterpPoints);
	}


	/**
	 *  initializes graph data containers
	 *
	 *@param  nPoint         Description of the Parameter
	 *@param  nInterpPoints  Description of the Parameter
	 */
	protected void init(int nPoint, int nInterpPoints) {
		graphDataContainerV = new Vector<Object>();
		xyPointV = new Vector<XYpoint>(nPoint, 50);
		xyInterpPointV = new Vector<XYpoint>(nInterpPoints, 50);
		xMax = -Double.MAX_VALUE;
		xMin = Double.MAX_VALUE;
		yMax = -Double.MAX_VALUE;
		yMin = Double.MAX_VALUE;
		errYmax = 0.0;
	}


	/**
	 *  add (x,y) point to the data set
	 *
	 *@param  x  The feature to be added to the Point attribute
	 *@param  y  The feature to be added to the Point attribute
	 */
	public void addPoint(double x, double y) {
		addPoint(x, y, 0.0);
	}


	/**
	 *  add (x,y, error of y) point to the data set
	 *
	 *@param  x    The feature to be added to the Point attribute
	 *@param  y    The feature to be added to the Point attribute
	 *@param  err  The feature to be added to the Point attribute
	 */
	public void addPoint(double x, double y, double err) {
		synchronized (lockUpObj) {
			xyPointV.add(new XYpoint(x, y, err));
			if ( x < xMax ) {
				Collections.sort( xyPointV, new CompareX() );
			}
			this.calculateRepresentation();
			this.updateData();
		}
		this.updateContainer();
	}


	/**
	 *  add an array of (x,y) points to the data set
	 *
	 *@param  x  The feature to be added to the Point attribute
	 *@param  y  The feature to be added to the Point attribute
	 */
	public void addPoint(double[] x, double[] y) {
		synchronized (lockUpObj) {
			xyPointV.removeAllElements();
			for (int i = 0; i < x.length; i++) {
				xyPointV.add(new XYpoint(x[i], y[i], 0.0));
			}
			Collections.sort( xyPointV, new CompareX() );
			this.calculateRepresentation();
			this.updateData();
		}
		this.updateContainer();
	}


	/**
	 *  add an array of (x,y, error of y) points to the data set
	 *
	 *@param  x    The feature to be added to the Point attribute
	 *@param  y    The feature to be added to the Point attribute
	 *@param  err  The feature to be added to the Point attribute
	 */
	public void addPoint(double[] x, double[] y, double[] err) {
		synchronized (lockUpObj) {
			xyPointV.removeAllElements();
			for (int i = 0; i < x.length; i++) {
				xyPointV.add(new XYpoint(x[i], y[i], Math.abs(err[i])));
			}
			Collections.sort( xyPointV, new CompareX() );
			this.calculateRepresentation();
			this.updateData();
		}
		this.updateContainer();
	}


	/**
	 *  update all points if they do exist or create new if they do not
	 *
	 *@param  x    Description of the Parameter
	 *@param  y    Description of the Parameter
	 *@param  err  Description of the Parameter
	 */
	public void updateValues(double[] x, double[] y, double[] err) {
		synchronized (lockUpObj) {
			if (x.length != y.length || y.length != err.length) {
				return;
			}
			if (x.length != xyPointV.size()) {
				xyPointV.removeAllElements();
				for (int i = 0; i < x.length; i++) {
					xyPointV.add(new XYpoint(x[i], y[i], Math.abs(err[i])));
				}
			} else {
				for (int i = 0; i < x.length; i++) {
					xyPointV.get(i).setXY(x[i], y[i], Math.abs(err[i]));
				}
			}
			Collections.sort( xyPointV, new CompareX() );
			this.calculateRepresentation();
			this.updateData();
		}
		this.updateContainer();
	}


	/**
	 *  update all points if they do exist or create new if they do not
	 *
	 *@param  x  Description of the Parameter
	 *@param  y  Description of the Parameter
	 */
	public void updateValues(double[] x, double[] y) {
		synchronized (lockUpObj) {
			if (x.length != y.length) {
				return;
			}
			if (x.length != xyPointV.size()) {
				xyPointV.removeAllElements();
				for (int i = 0; i < x.length; i++) {
					xyPointV.add(new XYpoint(x[i], y[i], 0.));
				}
			} else {
				for (int i = 0; i < x.length; i++) {
					xyPointV.get(i).setXY(x[i], y[i], 0.);
				}
			}
			Collections.sort( xyPointV, new CompareX() );
			this.calculateRepresentation();
			this.updateData();
		}
		this.updateContainer();
	}


	/**
	 *  update the y-array with errors into the data set
	 *
	 *@param  y    Description of the Parameter
	 *@param  err  Description of the Parameter
	 */
	public void updateValuesY(double[] y, double[] err) {
		synchronized (lockUpObj) {
			if (xyPointV.size() != y.length || xyPointV.size() != err.length) {
				return;
			}
			for (int i = 0; i < y.length; i++) {
				xyPointV.get(i).setY(y[i], Math.abs(err[i]));
			}
			this.calculateRepresentation();
			this.updateData();
		}
		this.updateContainer();
	}


	/**
	 *  update the y-array into the data set
	 *
	 *@param  y  Description of the Parameter
	 */
	public void updateValuesY(double[] y) {
		synchronized (lockUpObj) {
			if (xyPointV.size() != y.length) {
				return;
			}
			for (int i = 0; i < y.length; i++) {
				xyPointV.get(i).setY(y[i]);
			}
			this.calculateRepresentation();
			this.updateData();
		}
		this.updateContainer();
	}


	/**
	 *  update the y-value with certain index into the data set
	 *
	 *@param  index  Description of the Parameter
	 *@param  y      Description of the Parameter
	 */
	public void updateValueY(int index, double y) {
		synchronized (lockUpObj) {
			if (index < 0 || index >= xyPointV.size()) {
				return;
			}
			xyPointV.get(index).setY(y);
			this.calculateRepresentation();
			this.updateData();
		}
		this.updateContainer();
	}


	/**
	 *  update the y-value and x-value with certain index into the data set
	 *
	 *@param  index  Description of the Parameter
	 *@param  x      Description of the Parameter
	 *@param  y      Description of the Parameter
	 */
	public void updatePoint(int index, double x, double y) {
		synchronized (lockUpObj) {
			if (index < 0 || index >= xyPointV.size()) {
				return;
			}
			xyPointV.remove(index);
			xyPointV.add(new XYpoint(x, y, 0.0));
			Collections.sort( xyPointV, new CompareX() );
			this.calculateRepresentation();
			this.updateData();
		}
		this.updateContainer();
	}


	/**
	 *  update the y-value, x-value and error with certain index into the data set
	 *
	 *@param  index  Description of the Parameter
	 *@param  x      Description of the Parameter
	 *@param  y      Description of the Parameter
	 *@param  err    Description of the Parameter
	 */
	public void updatePoint(int index, double x, double y, double err) {
		synchronized (lockUpObj) {
			if (index < 0 || index >= xyPointV.size()) {
				return;
			}
			xyPointV.remove(index);
			xyPointV.add(new XYpoint(x, y, err));
			Collections.sort( xyPointV, new CompareX() );
			this.calculateRepresentation();
			this.updateData();
		}
		this.updateContainer();
	}


	/**
	 *  remove a point from the data set
	 *
	 *@param  index  Description of the Parameter
	 */
	public void removePoint(int index) {
		synchronized (lockUpObj) {
			if (index >= 0 && index < xyPointV.size()) {
				xyPointV.remove(index);
				this.calculateRepresentation();
				this.updateData();
			}
		}
		this.updateContainer();
	}


	/**
	 *  remove all points from the data set
	 */
	public void removeAllPoints() {
		synchronized (lockUpObj) {
			if (getNumbOfPoints() == 0) {
				return;
			}
			xyPointV.clear();
			this.calculateRepresentation();
			this.updateData();
		}
		this.updateContainer();
	}


	//This method can be overridden in subclasses to provide data for
	//different schema of interpolation
	//For linear class no additional operations needed
	/**
	 *  Description of the Method
	 */
	protected void calculateRepresentation() {

	}


	//Calculates the value of the function at specific point
	//This method can be overridden in subclasses to provide different schema of interpolation

	/**
	 *  get y-value for certain x-value
	 *
	 *@param  x  Description of the Parameter
	 *@return    The valueY value
	 */
	public double getValueY(double x) {
		synchronized (lockUpObj) {
			if (xyPointV.size() == 0) {
				return Double.MIN_VALUE;
			}
			if (xyPointV.size() == 1) {
				return xyPointV.get(0).getY();
			}
			int i = 0;
			for (i = 0; i < xyPointV.size(); i++) {
				if (x < xyPointV.get(i).getX()) {
					break;
				}
			}
			if (i == 0) {
				i = 1;
			}
			if (i == xyPointV.size()) {
				i = xyPointV.size() - 1;
			}
			XYpoint p1 = xyPointV.get(i - 1);
			XYpoint p2 = xyPointV.get(i);
			if (p2.getX() == p1.getX()) {
				return (p1.getY() + p2.getY()) / 2.0;
			}
			double u = (x - p1.getX()) / (p2.getX() - p1.getX());
			return (p1.getY() + u * (p2.getY() - p1.getY()));
		}
	}


	//Calculates the derivative of the function
	//This method can be overridden in subclasses to provide different schema of interpolation
	/**
	 *  get y'-value for certain x-value
	 *
	 *@param  x  Description of the Parameter
	 *@return    The valueDerivativeY value
	 */
	public double getValueDerivativeY(double x) {
		synchronized (lockUpObj) {
			if (xyPointV.size() == 0) {
				return Double.MIN_VALUE;
			}
			if (xyPointV.size() == 1) {
				return 0.0;
			}
			int i = 0;
			for (i = 0; i < xyPointV.size(); i++) {
				if (x < xyPointV.get(i).getX()) {
					break;
				}
			}
			if (i == 0) {
				i = 1;
			}
			if (i == xyPointV.size()) {
				i = xyPointV.size() - 1;
			}
			XYpoint p1 = xyPointV.get(i - 1);
			XYpoint p2 = xyPointV.get(i);
			if (p2.getX() == p1.getX()) {
				return 0.0;
			}
			return (p2.getY() - p1.getY()) / (p2.getX() - p1.getX());
		}
	}


	/**
	 *  Returns the capacity attribute of the BasicGraphData object
	 *
	 *@return    The capacity value
	 */
	protected int getCapacity() {
		return xyPointV.capacity();
	}


	/**
	 *  returns the number of data points
	 *
	 *@return    The numbOfPoints value
	 */
	public int getNumbOfPoints() {
		synchronized (lockUpObj) {
			return xyPointV.size();
		}
	}


	/**
	 *  returns the number of data points in the interpolation
	 *
	 *@return    The numbOfInterpPoints value
	 */
	public int getNumbOfInterpPoints() {
		synchronized (lockUpObj) {
			return xyInterpPointV.size();
		}
	}


	/**
	 *  returns x-value from the container
	 *
	 *@param  index  Description of the Parameter
	 *@return        The x value
	 */
	public double getX(int index) {
		synchronized (lockUpObj) {
			return xyPointV.get(index).getX();
		}
	}


	/**
	 *  returns y-value from the container
	 *
	 *@param  index  Description of the Parameter
	 *@return        The y value
	 */
	public double getY(int index) {
		return xyPointV.get(index).getY();
	}


	/**
	 *  returns x-value for a particular point in the interpolating data set
	 *
	 *@param  index  Description of the Parameter
	 *@return        The interpX value
	 */
	public double getInterpX(int index) {
		synchronized (lockUpObj) {
			return xyInterpPointV.get(index).getX();
		}
	}


	/**
	 *  returns interpolated y-value from the interpolating data set
	 *
	 *@param  index  Description of the Parameter
	 *@return        The interpY value
	 */
	public double getInterpY(int index) {
		synchronized (lockUpObj) {
			return xyInterpPointV.get(index).getY();
		}
	}


	/**
	 *  returns the error value for certain index
	 *
	 *@param  index  Description of the Parameter
	 *@return        The err value
	 */
	public double getErr(int index) {
		synchronized (lockUpObj) {
			return xyPointV.get(index).getYerr();
		}
	}


	/**
	 *  returns the maximal y-error
	 *
	 *@return    The maxErr value
	 */
	public double getMaxErr() {
		synchronized (lockUpObj) {
			return errYmax;
		}
	}


	/**
	 *  returns the minimal x
	 *
	 *@return    The minX value
	 */
	public double getMinX() {
		synchronized (lockUpObj) {
			return xMin;
		}
	}


	/**
	 *  returns the maximal x
	 *
	 *@return    The maxX value
	 */
	public double getMaxX() {
		synchronized (lockUpObj) {
			return xMax;
		}
	}


	/**
	 *  returns the minimal y
	 *
	 *@return    The minY value
	 */
	public double getMinY() {
		synchronized (lockUpObj) {
			return yMin;
		}
	}


	/**
	 *  returns the maximal y
	 *
	 *@return    The maxY value
	 */
	public double getMaxY() {
		synchronized (lockUpObj) {
			return yMax;
		}
	}


	/**
	 *  Description of the Method
	 */
	protected void updateData() {

		if (xyPointV.size() == 0) {
			return;
		}

		xMin = xyPointV.firstElement().getX();
		xMax = xyPointV.lastElement().getX();
		XYpoint pMaxErr = Collections.max( xyPointV, new CompareErr() );
		errYmax = pMaxErr.getYerr();

		//------------------------------------------------------------------------
		//it will work only first time when nInterpPoints != xyInterpPointV.size()
		//------------------------------------------------------------------------
		int nInterp = nInterpPoints;
		int nInterpSize = xyInterpPointV.size();
		for (int i = 0; i < (nInterp - nInterpSize); i++) {
			xyInterpPointV.add(new XYpoint(0.0, 0.0));
		}

		double step = (xMax - xMin) / (nInterp - 1);
		double x;
		double y;

		yMax = -Double.MAX_VALUE;
		yMin = Double.MAX_VALUE;

		for (int i = 0; i < nInterp; i++) {
			x = xMin + i * step;
			y = this.getValueY(x);
			if (y < yMin) {
				yMin = y;
			}
			if (y > yMax) {
				yMax = y;
			}
			xyInterpPointV.get(i).setXY(x, y);
		}

		for (int i = 0, nPoints = xyPointV.size(); i < nPoints; i++) {
			x = xyPointV.get(i).getX();
			y = xyPointV.get(i).getY();
			if (y < yMin) {
				yMin = y;
			}
			if (y > yMax) {
				yMax = y;
			}
		}

		yMin = yMin - errYmax;
		yMax = yMax + errYmax;
	}


	/**
	 *  registers this data set into a graph container. Warning: do not use this
	 *  method. This method is supposed to be used by the FunctionGraphsJPanel
	 *  class. Use the addGraphData - method of the FunctionGraphsJPanel class
	 *
	 *@param  gdc  Description of the Parameter
	 */
	public void registerInContainer(FunctionGraphsJPanel gdc) {
		graphDataContainerV.add(gdc);
	}


	/**
	 *  returns the number of graph containers where this data set has been
	 *  registered
	 *
	 *@return    The numberOfGraphDataContainers value
	 */
	public int getNumberOfGraphDataContainers() {
		return graphDataContainerV.size();
	}


	/**
	 *  removes this data set from a graph container. Warning: do not use this
	 *  method. This method is supposed to be used by the FunctionGraphsJPanel
	 *  class. Use the removeGraphData - method of the FunctionGraphsJPanel class
	 *
	 *@param  obj  Description of the Parameter
	 */
	public void removeContainer(Object obj) {
		graphDataContainerV.removeElement(obj);
	}


	/**
	 *  returns the graph containers where this data set has been registered
	 *
	 *@param  index  Description of the Parameter
	 *@return        The graphDataContainer value
	 */
	public FunctionGraphsJPanel getGraphDataContainer(int index) {
		if (index < graphDataContainerV.size()) {
			return ((FunctionGraphsJPanel) graphDataContainerV.get(index));
		}
		return null;
	}


	/**
	 *  calls the method <code> refreshGraphJPanel() </code> of all graph
	 *  containers <code> FunctionGraphsJPanel </code> where this data set has been
	 *  registered.
	 */
	protected void updateContainer() {
		if (immediateContainerUpdate) {
			for (int i = 0; i < graphDataContainerV.size(); i++) {
				((FunctionGraphsJPanel) graphDataContainerV.get(i)).refreshGraphJPanel();
			}
		}
	}


	/**
	 *  sets the immediate graph container update if data has been changed.
	 *
	 *@param  immediateContainerUpdate  The new immediateContainerUpdate value
	 */
	public void setImmediateContainerUpdate(boolean immediateContainerUpdate) {
		this.immediateContainerUpdate = immediateContainerUpdate;
		if (immediateContainerUpdate) {
			updateContainer();
		}
	}


	/**
	 *  returns true if data changes cause the immediate graph container update.
	 *
	 *@return    The immediateContainerUpdate value
	 */
	public boolean getImmediateContainerUpdate() {
		return immediateContainerUpdate;
	}


	//----------------------------------------------
	//methods related to the graphical representation
	//----------------------------------------------
	/**
	 *  sets the color of the graph
	 *
	 *@param  color  The new graphColor value
	 */
	public void setGraphColor(Color color) {
		synchronized (lockUpObj) {
			this.color = color;
		}
		updateContainer();
	}


	/**
	 *  sets the name of the graph
	 *
	 *@param  name  The new graphName value
	 */
	public void setGraphName(String name) {
		synchronized (lockUpObj) {
			this.name = name;
		}
		updateContainer();
	}


	/**
	 *  returns the color of the graph
	 *
	 *@return    The graphColor value
	 */
	public Color getGraphColor() {
		synchronized (lockUpObj) {
			return color;
		}
	}


	/**
	 *  returns the name of the graph
	 *
	 *@return    The graphName value
	 */
	public String getGraphName() {
		synchronized (lockUpObj) {
			return name;
		}
	}


	/**
	 *  sets the (key,value) of the data set properties
	 *
	 *@param  keyObj   The new graphProperty value
	 *@param  propObj  The new graphProperty value
	 */
	public void setGraphProperty(Object keyObj, Object propObj) {
		synchronized (lockUpObj) {
			propertyMap.put(keyObj, propObj);
		}
	}


	/**
	 *  sets the value of the data set property by the key-value
	 *
	 *@param  keyObj  Description of the Parameter
	 *@return         The graphProperty value
	 */
	public Object getGraphProperty(Object keyObj) {
		synchronized (lockUpObj) {
			return propertyMap.get(keyObj);
		}
	}


	/**
	 *  returns the number of pairs (key,value)
	 *
	 *@return    The graphPropertySize value
	 */
	public int getGraphPropertySize() {
		synchronized (lockUpObj) {
			return propertyMap.size();
		}
	}


	/**
	 *  returns the set of the keys
	 *
	 *@return    The graphPropertyKeys value
	 */
	public Set<Object> getGraphPropertyKeys() {
		synchronized (lockUpObj) {
			return propertyMap.keySet();
		}
	}


	/**
	 *  sets "draw lines" on/off
	 *
	 *@param  drawLinesOn  The new drawLinesOn value
	 */
	public void setDrawLinesOn(boolean drawLinesOn) {
		synchronized (lockUpObj) {
			this.drawLinesOn = drawLinesOn;
		}
		updateContainer();
	}


	/**
	 *  returns the "draw lines on/off" state
	 *
	 *@return    The drawLinesOn value
	 */
	public boolean getDrawLinesOn() {
		synchronized (lockUpObj) {
			return drawLinesOn;
		}
	}


	/**
	 *  sets "draw points" on/off
	 *
	 *@param  drawPointsOn  The new drawPointsOn value
	 */
	public void setDrawPointsOn(boolean drawPointsOn) {
		synchronized (lockUpObj) {
			this.drawPointsOn = drawPointsOn;
		}
		updateContainer();
	}


	/**
	 *  returns the "draw points on/off" state
	 *
	 *@return    The drawPointsOn value
	 */
	public boolean getDrawPointsOn() {
		synchronized (lockUpObj) {
			return drawPointsOn;
		}
	}


	/**
	 *  sets the size of the point during drawing. This parameter is used if no
	 *  external shape is set, and the filled circle is used to represent points.
	 *
	 *@param  pointSize  The new graphPointSize value
	 */
	public void setGraphPointSize(int pointSize) {
		this.pointSize = pointSize;
	}


	/**
	 *  returns the size of the point during drawing. This parameter is used if no
	 *  external shape is set, and the filled circle is used to represent points.
	 *
	 *@return    The graphPointSize value
	 */
	public int getGraphPointSize() {
		return pointSize;
	}


	/**
	 *  sets the shape of the point during drawing. The input parameter could be
	 *  <code> null </code>. By default it will be a circle.
	 *
	 *@param  markShape  The new graphPointShape value
	 */
	public void setGraphPointShape(Shape markShape) {
		this.markShape = markShape;
	}


	/**
	 *  returns the shape of the point during drawing.
	 *
	 *@return    The graphPointShape value
	 */
	public Shape getGraphPointShape() {
		return markShape;
	}


	/**
	 *  sets the filling shape property of the point during drawing. By default it
	 *  will be true.
	 *
	 *@param  fillShape  The new graphPointFillingShape value
	 */
	public void setGraphPointFillingShape(boolean fillShape) {
		markShapeFilled = fillShape;
	}


	/**
	 *  returns the filling shape property of the point during drawing.
	 *
	 *@return    The graphPointShapeFilled value
	 */
	public boolean isGraphPointShapeFilled() {
		return markShapeFilled;
	}


	/**
	 *  sets the line thickness during drawing. The default value is 1.
	 *
	 *@param  lineThick  The new lineThick value
	 */
	public void setLineThick(int lineThick) {
		setLineStroke( (float)lineThick, lineStroke.getDashArray() );
	}


	/**
	 *  returns the line thickness during drawing.
	 *
	 *@return    The lineThick value
	 */
	public int getLineThick() {
		return lineThick;
	}
	
	
	/** 
	 * Set the line stroke dash pattern.
	 * @param dashPattern array of dash segment lengths
	 */
	public void setLineDashPattern( final float ... dashPattern ) {
		setLineStroke( (float)this.lineThick, dashPattern );
	}
	
	
	/** 
	 * Set the line stroke (both width and dash pattern).
	 * @param width line width (aka thickness)
	 * @param dashPattern array of dash segment lengths
	 */
	public void setLineStroke( final float width, final float ... dashPattern ) {
		this.lineThick = (int)width;
		this.lineStroke = new BasicStroke( width, this.lineStroke.getEndCap(), this.lineStroke.getLineJoin(), this.lineStroke.getMiterLimit(), dashPattern, this.lineStroke.getDashPhase() );
	}
	

	/**
	 *  returns the stroke for drawing.
	 *
	 *@return    The stroke value
	 */
	protected BasicStroke getStroke() {
		return lineStroke;
	}


	/**
	 *  Returns the lock object of the BasicGraphData object
	 *
	 *@return    The lock object
	 */
	public Object getLockObject() {
		return lockUpObj;
	}



	//----------------------------------------------
	//inner classes
	//----------------------------------------------

	/**
	 *  Description of the Class
	 *
	 *@author     shishlo
	 *@version    August 3, 2004
	 */
	protected class XYpoint {

		/**
		 *  Description of the Field
		 */
		public double x, y;
		/**
		 *  Description of the Field
		 */
		public double yErr;


		/**
		 *  Constructor for the XYpoint object
		 */
		public XYpoint() {
			x = 0.0;
			y = 0.0;
			yErr = 0.0;
		}


		/**
		 *  Constructor for the XYpoint object
		 *
		 *@param  x  Description of the Parameter
		 *@param  y  Description of the Parameter
		 */
		public XYpoint(double x, double y) {
			this.x = x;
			this.y = y;
			this.yErr = 0.0;
		}


		/**
		 *  Constructor for the XYpoint object
		 *
		 *@param  x     Description of the Parameter
		 *@param  y     Description of the Parameter
		 *@param  yErr  Description of the Parameter
		 */
		public XYpoint(double x, double y, double yErr) {
			this.x = x;
			this.y = y;
			this.yErr = Math.abs(yErr);
		}


		/**
		 *  Sets the xY attribute of the XYpoint object
		 *
		 *@param  x  The new xY value
		 *@param  y  The new xY value
		 */
		public void setXY(double x, double y) {
			this.x = x;
			this.y = y;
			this.yErr = 0.0;
		}


		/**
		 *  Sets the xY attribute of the XYpoint object
		 *
		 *@param  x     The new xY value
		 *@param  y     The new xY value
		 *@param  yErr  The new xY value
		 */
		public void setXY(double x, double y, double yErr) {
			this.x = x;
			this.y = y;
			this.yErr = yErr;
		}


		/**
		 *  Sets the y attribute of the XYpoint object
		 *
		 *@param  y  The new y value
		 */
		public void setY(double y) {
			this.y = y;
			this.yErr = 0.0;
		}


		/**
		 *  Sets the y attribute of the XYpoint object
		 *
		 *@param  y     The new y value
		 *@param  yErr  The new y value
		 */
		public void setY(double y, double yErr) {
			this.y = y;
			this.yErr = yErr;
		}


		/**
		 *  Returns the x attribute of the XYpoint object
		 *
		 *@return    The x value
		 */
		public double getX() {
			return x;
		}


		/**
		 *  Returns the y attribute of the XYpoint object
		 *
		 *@return    The y value
		 */
		public double getY() {
			return y;
		}


		/**
		 *  Returns the yerr attribute of the XYpoint object
		 *
		 *@return    The yerr value
		 */
		public double getYerr() {
			return yErr;
		}

	}


	/**
	 *  Description of the Class
	 *
	 *@author     shishlo
	 *@version    August 3, 2004
	 */
	protected class CompareX implements Comparator<XYpoint> {
		/**
		 *  Description of the Method
		 *
		 *@param  obj1  Description of the Parameter
		 *@param  obj2  Description of the Parameter
		 *@return       Description of the Return Value
		 */
		public int compare( final XYpoint obj1, final XYpoint obj2 ) {
			if ( obj1.getX() > obj2.getX() ) {
				return 1;
			}
			else if ( obj1.getX() < obj2.getX() ) {
				return -1;
			}
			return 0;
		}
	}


	/**
	 *  Description of the Class
	 *
	 *@author     shishlo
	 *@version    August 3, 2004
	 */
	protected class CompareY implements Comparator<XYpoint> {
		/**
		 *  Description of the Method
		 *
		 *@param  obj1  Description of the Parameter
		 *@param  obj2  Description of the Parameter
		 *@return       Description of the Return Value
		 */
		public int compare( final XYpoint obj1, final XYpoint obj2 ) {
			if ( obj1.getY() > obj2.getY() ) {
				return 1;
			}
			else if ( obj1.getY() < obj2.getY() ) {
				return -1;
			}
			return 0;
		}
	}


	/**
	 *  Description of the Class
	 *
	 *@author     shishlo
	 *@version    August 3, 2004
	 */
	protected class CompareErr implements Comparator<XYpoint> {
		/**
		 *  Description of the Method
		 *
		 *@param  obj1  Description of the Parameter
		 *@param  obj2  Description of the Parameter
		 *@return       Description of the Return Value
		 */
		public int compare( final XYpoint obj1, final XYpoint obj2 ) {
			if ( obj1.getYerr() > obj2.getYerr() ) {
				return 1;
			}
			else if ( obj1.getYerr() < obj2.getYerr() ) {
				return -1;
			}
			return 0;
		}
	}


	//----------------------------------------
	//MAIN test method for debugging
	//----------------------------------------
	/**
	 *  this is a test method
	 *
	 *@param  args  Description of the Parameter
	 */
	public static void main(String args[]) {
		BasicGraphData spl = new BasicGraphData();
		int nPoint = 20;
		double[] xV = new double[nPoint];
		double[] yV = new double[nPoint];
		System.out.println("Added ====As an example sin(x) has been used=======");
		System.out.println("Added ====x====  ====y=====");
		for (int i = 0; i < nPoint; i++) {
			xV[i] = 0.3 * i;
			yV[i] = Math.sin(xV[i]);
			spl.addPoint(xV[i], yV[i]);
			System.out.println(xV[i] + " " + yV[i]);
		}

		double x;
		double y;
		double yp;
		int NgraphPoint = 50;
		System.out.println("==BasicGraphData results========");
		System.out.println("====x====  ====y=====   ====derivative y====");
		double step = (spl.getMaxX() - spl.getMinX()) / NgraphPoint;

		for (int i = 0; i < NgraphPoint; i++) {
			x = spl.getMinX() + step * i + 0.5 * step;
			y = spl.getValueY(x);
			yp = spl.getValueDerivativeY(x);
			System.out.println(x + "  " + y + "  " + yp);
		}

		System.out.println("Stop.");
	}

}

