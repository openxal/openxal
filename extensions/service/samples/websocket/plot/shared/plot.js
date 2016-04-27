// namespace for creating HTML substitutes for plot elements
var PLOT_HTML_NAME_SPACE = "http://www.w3.org/1999/xhtml";
//var PLOT_HTML_NAME_SPACE;


// set the namespace URI for creating HTML substitutes for plot elements
function setPlotHTMLNameSpace( nameSpaceURI ) {
	PLOT_HTML_NAME_SPACE = nameSpaceURI;
}


// Plot adaptor
function PlotAdaptor() {}
PlotAdaptor.prototype.draw = function( graph, PlotDataSource ) {}


// Line Plot type adaptor
LinePlotAdaptor.prototype = new PlotAdaptor();
LinePlotAdaptor.prototype.constructor = LinePlotAdaptor;
function LinePlotAdaptor() {}

// draw on the graph a line plot of the specified data
LinePlotAdaptor.prototype.draw = function( graph, series ) {
	var canvas = graph.plotCanvas;
	if ( canvas ) {
		var context = canvas.getContext( "2d" );
		context.save();
		
		var xAxis = graph.bottomAxis;
		var yAxis = graph.leftAxis;
		
		var plotData = series.data;
		var pointCount = plotData.pointCount();
		
		if ( pointCount > 0 ) {
			context.strokeStyle = series.getLineColor();
			context.lineWidth = series.getLineWidth();
			context.beginPath();
			
			var x0 = plotData.getX( 0 );
			var y0 = plotData.getY( 0 );
			context.moveTo( xAxis.toCanvasCoordinate( x0 ), yAxis.toCanvasCoordinate( y0 ) );
			for ( var index = 1 ; index < pointCount ; index++ ) {
				var x = xAxis.toCanvasCoordinate( plotData.getX( index ) );
				var y = yAxis.toCanvasCoordinate( plotData.getY( index ) );
				context.lineTo( x, y );
			}
			context.stroke();
			context.restore();
		}
	}
}
var LINE_PLOT = new LinePlotAdaptor();


// Scatter Plot type adaptor
ScatterPlotAdaptor.prototype = new PlotAdaptor();
ScatterPlotAdaptor.prototype.constructor = ScatterPlotAdaptor;
function ScatterPlotAdaptor() {}

// draw on the graph a line plot of the specified data
ScatterPlotAdaptor.prototype.draw = function( graph, series ) {
	var canvas = graph.plotCanvas;
	if ( canvas ) {
		var context = canvas.getContext( "2d" );
		context.save();
		
		var xAxis = graph.bottomAxis;
		var yAxis = graph.leftAxis;
		
		var plotData = series.data;
		var pointCount = plotData.pointCount();
		
		if ( pointCount > 0 ) {
			context.fillStyle = series.getLineColor();
			context.beginPath();
			var pointSize = series.getPointWidth();
			
			for ( var index = 0 ; index < pointCount ; index++ ) {
				var x = xAxis.toCanvasCoordinate( plotData.getX( index ) );
				var y = yAxis.toCanvasCoordinate( plotData.getY( index ) );
				context.fillRect( x - pointSize / 2, y - pointSize / 2, pointSize, pointSize );
			}
			context.restore();
		}
	}
}
var SCATTER_PLOT = new ScatterPlotAdaptor();


// Bar Chart type adaptor
BarChartAdaptor.prototype = new PlotAdaptor();
BarChartAdaptor.prototype.constructor = BarChartAdaptor;
function BarChartAdaptor() {}

// draw on the graph a line plot of the specified data
BarChartAdaptor.prototype.draw = function( graph, series ) {
	var canvas = graph.plotCanvas;
	if ( canvas ) {
		var context = canvas.getContext( "2d" );
		context.save();
		
		var xAxis = graph.bottomAxis;
		var yAxis = graph.leftAxis;
		
		var plotData = series.data;
		var pointCount = plotData.pointCount();
		
		if ( pointCount > 0 ) {
			context.fillStyle = series.getLineColor();
			context.beginPath();
			
			var pointWidth = series.getPointWidth();
			var xBase = xAxis.toCanvasCoordinate( 0.0 );
			var yBase = yAxis.toCanvasCoordinate( 0.0 );
			for ( var index = 0 ; index < pointCount ; index++ ) {
				var x = xAxis.toCanvasCoordinate( plotData.getX( index ) );
				var y = yAxis.toCanvasCoordinate( plotData.getY( index ) );
				var height = y - yBase;
				// rectangles must always be drawn with a nonnegative height
				if ( height > 0 ) {
					context.fillRect( x - pointWidth / 2, yBase, pointWidth, height );
				}
				else {
					context.fillRect( x - pointWidth / 2, y, pointWidth, -height );
				}
			}
			context.restore();
		}
	}
}
var BAR_CHART = new BarChartAdaptor();



// two dimensional point
function Point2D( x, y ) {
	this.x = x;
	this.y = y;
}



// Plot data source
function PlotDataSource() {}
PlotDataSource.prototype.pointCount = function() { return 0; }
PlotDataSource.prototype.getX = function( index ) { return 0; }
PlotDataSource.prototype.getY = function( index ) { return 0; }


// Waveform data
WaveformPlotDataSource.prototype = new PlotDataSource();
WaveformPlotDataSource.prototype.constructor = WaveformPlotDataSource;
function WaveformPlotDataSource( waveform ) {
	this.waveform = waveform;
}
WaveformPlotDataSource.prototype.setWaveform = function( waveform ) { this.waveform = waveform; }
WaveformPlotDataSource.prototype.getWaveform = function() { return this.waveform; }
WaveformPlotDataSource.prototype.pointCount = function() { return this.waveform.length; }
WaveformPlotDataSource.prototype.getX = function( index ) { return index; }
WaveformPlotDataSource.prototype.getY = function( index ) { return this.waveform[index]; }


// XY data
XYPlotDataSource.prototype = new PlotDataSource();
XYPlotDataSource.prototype.constructor = XYPlotDataSource;
function XYPlotDataSource() {
	this.points = new Array();
}
XYPlotDataSource.prototype.addPoint2D = function( point ) {
	this.points.push( point );
}
XYPlotDataSource.prototype.addPoint = function( x, y ) {
	this.points.push( new Point2D( x, y ) );
}
XYPlotDataSource.prototype.addPoints = function( points ) {
	for ( var index = 0 ; index < points.length ; index++ ) {
		this.points.push( points[index] );
	}
}
XYPlotDataSource.prototype.removeAllPoints = function() {
	this.points = new Array();
}
XYPlotDataSource.prototype.pointCount = function() { return this.points.length; }
XYPlotDataSource.prototype.getX = function( index ) { return this.points[index].x; }
XYPlotDataSource.prototype.getY = function( index ) { return this.points[index].y; }



// Plot series
function PlotSeries( plotAdaptor, data, color ) {
	this.plotAdaptor = plotAdaptor;
	this.data = data;
	this.pointColor = color;
	this.lineColor = color;
	this.lineWidth = 4.0;
	this.pointWidth = 4.0;
}
PlotSeries.prototype.getLineWidth = function() { return this.lineWidth; }
PlotSeries.prototype.setLineWidth = function( width ) { this.lineWidth = width; }
PlotSeries.prototype.getPointWidth = function() { return this.pointWidth; }
PlotSeries.prototype.setPointWidth = function( width ) { this.pointWidth = width; }
PlotSeries.prototype.getPointColor = function() { return this.pointColor; }
PlotSeries.prototype.setPointColor = function( color ) { this.pointColor = color; }
PlotSeries.prototype.getLineColor = function() { return this.lineColor; }
PlotSeries.prototype.setLineColor = function( color ) { this.lineColor = color; }



// orientation adaptor for an axis
function AxisOrientationAdaptor() {
	this.plotOffset = 0.0;
	this.plotScale = 0.0;
	this.lengthLabel = null;
	this.tickLabelAlignment = "center";
}


AxisOrientationAdaptor.prototype.getMajorTicks = function( grid ) { return 4; }
AxisOrientationAdaptor.prototype.setMajorTicks = function( count, grid ) {}
AxisOrientationAdaptor.prototype.getTickLabelMaxWidth = function( axis ) { return 0.0; }
AxisOrientationAdaptor.prototype.getTickLabelEdge = function( axis ) { return 0.0; }
AxisOrientationAdaptor.prototype.drawTickLabel = function( context, label, position, edge, maxWidth ) {}
AxisOrientationAdaptor.prototype.defaultLabel = function() { return ""; }
AxisOrientationAdaptor.prototype.prepareTickLabelContext = function() {}
AxisOrientationAdaptor.prototype.drawLabel = function( context, axis ) {}



// inherit from the orientation adaptor
LeftAxisAdaptor.prototype = new AxisOrientationAdaptor();
LeftAxisAdaptor.prototype.constructor = LeftAxisAdaptor;

// left orientation adaptor for an axis
function LeftAxisAdaptor() {
	this.className = "LeftAxis";
	this.plotOffset = 1.0;
	this.plotScale = -1.0;
	this.lengthLabel = "height";	
	this.tickLabelAlignment = "right";
}

LeftAxisAdaptor.prototype.getMajorTicks = function( grid ) { return grid.yMajorTicks; }
LeftAxisAdaptor.prototype.setMajorTicks = function( count, grid ) { grid.yMajorTicks = count; }
LeftAxisAdaptor.prototype.getTickLabelMaxWidth = function( axis ) { return axis.canvas.width / 2; }
LeftAxisAdaptor.prototype.getTickLabelEdge = function( axis ) { return axis.canvas.width; }

LeftAxisAdaptor.prototype.drawTickLabel = function( context, label, position, edge, maxWidth ) {
	context.fillText( label, edge, position, maxWidth );
}

LeftAxisAdaptor.prototype.defaultLabel = function() { return "Vertical"; }

LeftAxisAdaptor.prototype.prepareTickLabelContext = function( context ) {
	context.textAlign = "right";
}

LeftAxisAdaptor.prototype.drawLabel = function( context, axis ) {
	var fontSize = axis.fontSize;
	context.font = fontSize + "px sans-serif";
	var label = axis.getLabel();
	var textWidth = context.measureText( label ).width;
	var textCenter = - ( textWidth + context.canvas.height ) / 2;
	context.save();
	context.rotate( -Math.PI / 2 );
	context.fillText( label, textCenter, fontSize );
	context.restore();
}



// inherit from the orientation adaptor
BottomAxisAdaptor.prototype = new AxisOrientationAdaptor();
BottomAxisAdaptor.prototype.constructor = BottomAxisAdaptor;


// bottom orientation adaptor for an axis
function BottomAxisAdaptor() {
	this.className = "BottomAxis";
	this.plotOffset = 0.0;
	this.plotScale = 1.0;
	this.lengthLabel = "width";
	this.tickLabelAlignment = "left";
}


BottomAxisAdaptor.prototype.getMajorTicks = function( grid ) { return grid.xMajorTicks; }
BottomAxisAdaptor.prototype.setMajorTicks = function( count, grid ) { grid.xMajorTicks = count; }
BottomAxisAdaptor.prototype.getTickLabelMaxWidth = function( axis ) { return axis.canvas.width / ( 2 * axis.grid.xMajorTicks ); }
BottomAxisAdaptor.prototype.getTickLabelEdge = function( axis ) { return 0; }

BottomAxisAdaptor.prototype.drawTickLabel = function( context, label, position, edge, maxWidth ) {
	context.fillText( label, position, edge, maxWidth );
}

BottomAxisAdaptor.prototype.defaultLabel = function() { return "Horizontal"; }

BottomAxisAdaptor.prototype.prepareTickLabelContext = function( context ) {
	context.textBaseline = "top";	// for bottom axis
}

BottomAxisAdaptor.prototype.drawLabel = function( context, axis ) {
	var fontSize = axis.fontSize;
	context.font = fontSize + "px sans-serif";
	var label = axis.getLabel();
	var textWidth = context.measureText( label ).width;
	var textCenter = ( context.canvas.width - textWidth ) / 2;
	context.save();
	context.fillText( label, textCenter, context.canvas.height );
	context.restore();
}




// Axis Constructor
function GraphAxis( grid, orientation ) {
	switch( orientation ) {
		case 1:			// horizontal
			this.orientationAdaptor = new BottomAxisAdaptor();
			break;
		case 2:			// vertical
			this.orientationAdaptor = new LeftAxisAdaptor();
			break;
		default:
			break;
	}
	this.setNeedsDisplay();
	this.grid = grid;
	this.setRange( -1.0, 1.0 );
	this.setLabel( this.orientationAdaptor.defaultLabel() );
	this.makeView();
	this.fontSize = 15;
}


// get the axis label
GraphAxis.prototype.getLabel = function() {
	return this._label;
}


// set the axis label
GraphAxis.prototype.setLabel = function( label ) {
	this._label = label;
	this.setNeedsDisplay();
}


// get the axis font size
GraphAxis.prototype.getFontSize = function() {
	return this.fontSize;
}


// set the axis font size
GraphAxis.prototype.setFontSize = function( theFontSize ) {
	this.fontSize = theFontSize;
	this.setNeedsDisplay();
}


// set the range
GraphAxis.prototype.setRange = function( lowerLimit, upperLimit ) {
	this._lowerLimit = lowerLimit;
	this._upperLimit = upperLimit;
	this.setNeedsDisplay();
}


// get the lower limit
GraphAxis.prototype.getLowerLimit = function() { return this._lowerLimit; }


// get the upper limit
GraphAxis.prototype.getUpperLimit = function() { return this._upperLimit; }


// set the number of major ticks
GraphAxis.prototype.setMajorTicks = function( count ) {
	this.orientationAdaptor.setMajorTicks( count, this.grid );
	this.setNeedsDisplay();
}


// set the length in pixels
GraphAxis.prototype.setLength = function( length ) { 
	this._length = length;
	this.canvas.setAttribute( this.orientationAdaptor.lengthLabel, length );
	this.setNeedsDisplay();
}


// get the height
GraphAxis.prototype.getLength = function() { return this._length; }


// get the DOM view for the axis
GraphAxis.prototype.getView = function() {
	return this.canvas;
}


// create a DOM view for the graph
GraphAxis.prototype.makeView = function() {
	this.canvas = window.document.createElementNS( PLOT_HTML_NAME_SPACE, "canvas" );
	this.canvas.setAttribute( "class", this.orientationAdaptor.className );
	this.canvas.setAttribute( "width", "50" );
	this.canvas.setAttribute( "height", "50" );
}


// convert a data point to a canvas coordinate
GraphAxis.prototype.toCanvasCoordinate = function( q ) {
	var lowerLimit = this.getLowerLimit();
	var upperLimit = this.getUpperLimit();
	var scale = this.orientationAdaptor.plotScale / ( upperLimit - lowerLimit );
	return this._length * ( this.orientationAdaptor.plotOffset + scale * ( q - lowerLimit ) );
}


// mark the view for display refresh
GraphAxis.prototype.setNeedsDisplay = function() {
	this.needsDisplay = true;
}


// mark the view for display refresh
GraphAxis.prototype.displayIfNeeded = function() {
	if ( this.needsDisplay ) {
		this.draw();
	}
}


// mark the view for display refresh
GraphAxis.prototype.draw = function() {
	var canvas = this.canvas;
	if ( canvas ) {
		var context = canvas.getContext( "2d" );
		context.clearRect( 0, 0, canvas.width, canvas.height );

		if ( context.fillText ) {
			context.save();
			
			this.orientationAdaptor.drawLabel( context, this );
			
			this.orientationAdaptor.prepareTickLabelContext( context );
			
			context.font = "large";
			var maxWidth = this.orientationAdaptor.getTickLabelMaxWidth( this );
			var edge = this.orientationAdaptor.getTickLabelEdge( this );
			var numTicks = this.orientationAdaptor.getMajorTicks( this.grid );
			var lowerLimit = this.getLowerLimit();
			var upperLimit = this.getUpperLimit();
			var qStep = ( upperLimit - lowerLimit ) / numTicks;
			for ( q = lowerLimit ; q < upperLimit ; q += qStep ) {
				var position = this.toCanvasCoordinate( q );
				this.orientationAdaptor.drawTickLabel( context, q, position, edge, maxWidth );
			}
			
			context.restore();
		}
	}
	this.needsDisplay = false;
}




// Grid Constructor
function GraphGrid() {
	this.setNeedsDisplay();
	this.color = "gray";
	this.xMajorTicks = 4;
	this.yMajorTicks = 4;
	this.minorTicks = 5;
	this.makeView();
	this.setVisible( true );
}


// set the ticks
GraphGrid.prototype.setTicks = function( xMajorTicks, yMajorTicks, minorTicks ) {
	this.xMajorTicks = xMajorTicks;
	this.yMajorTicks = yMajorTicks;
	this.minorTicks = minorTicks;
	this.setNeedsDisplay();
}


// set the grid's color
GraphGrid.prototype.setColor = function( color ) {
	this.color = color;
}


// set the background color
GraphGrid.prototype.setBackgroundColor = function( color ) {
	this.canvas.style.backgroundColor = color;
}


// set the height
GraphGrid.prototype.__defineSetter__( "height", function( height ) {  this.setHeight( height ); } );


// set the height
GraphGrid.prototype.setHeight = function( height ) {
	this._height = height;
	this.canvas.setAttribute( "height", height );
	this.setNeedsDisplay();
}


// get the height
GraphGrid.prototype.__defineGetter__( "height", function() { return this._height; } );


// set the width
GraphGrid.prototype.__defineSetter__( "width", function( width ) {  this.setWidth( width ); } );


// set the width
GraphGrid.prototype.setWidth = function( width ) {
	this._width = width;
	this.canvas.setAttribute( "width", width );
	this.setNeedsDisplay();
}


// get the width
GraphGrid.prototype.__defineGetter__( "width", function() { return this._width; } );


// get the DOM view for the graph
GraphGrid.prototype.getView = function() {
	return this.canvas;
}


// create a DOM view for the graph
GraphGrid.prototype.makeView = function() {
	this.canvas = window.document.createElementNS( PLOT_HTML_NAME_SPACE, "canvas" );
	this.canvas.setAttribute( "class", "Grid" );
}


// show the grid
GraphGrid.prototype.setVisible = function( visibility ) {
	this.visible = visibility;
	this.setNeedsDisplay();
}


// mark the view for display refresh
GraphGrid.prototype.setNeedsDisplay = function() {
	this.needsDisplay = true;
}


// mark the view for display refresh
GraphGrid.prototype.displayIfNeeded = function() {
	if ( this.needsDisplay ) {
		this.draw();
	}
}


// draw the grid
GraphGrid.prototype.draw = function() {
	var canvas = this.canvas;
	if ( canvas ) {
		var context = canvas.getContext( "2d" );
		context.clearRect( 0, 0, canvas.width, canvas.height );
		
		if ( this.visible ) {
			context.save();
			
			// make the grid
			context.strokeStyle = this.color;
			context.beginPath();
			
			var minorTicks = this.minorTicks;
			var xParts = this.xMajorTicks;
			var yParts = this.yMajorTicks;
			
			for ( var part = 0 ; part < yParts ; part++ ) {
				var y = part * canvas.height / yParts;
				context.moveTo( 0, y );
				context.lineTo( canvas.width, y );
			}
			for ( var part = 0 ; part < xParts ; part++ ) {
				var x = part * canvas.width / xParts;
				context.moveTo( x, 0 );
				context.lineTo( x, canvas.height );
			}
			context.stroke();
			
			// draw the major ticks
			context.strokeStyle = "black";
			context.beginPath();
			
			var majorTickSize = 10;
			var minorTickSize = majorTickSize / 2;
			for ( var part = 0 ; part < yParts ; part++ ) {
				var y = part * canvas.height / yParts;
				context.moveTo( 0, y );
				context.lineTo( majorTickSize, y );
				
				// draw minor ticks
				for ( var minorPart = 0 ; minorPart < minorTicks ; minorPart++ ) {
					var minorY = y + minorPart * canvas.height / yParts / minorTicks;
					context.moveTo( 0, minorY );
					context.lineTo( minorTickSize, minorY );
				}
			}
			for ( var part = 0 ; part < xParts ; part++ ) {
				var x = part * canvas.width / xParts;
				context.moveTo( x, canvas.height );
				context.lineTo( x, canvas.height - majorTickSize );
				
				// draw minor ticks
				for ( var minorPart = 0 ; minorPart < minorTicks ; minorPart++ ) {
					var minorX = x + minorPart * canvas.width / xParts / minorTicks;
					context.moveTo( minorX, canvas.height );
					context.lineTo( minorX, canvas.height - minorTickSize );
				}
			}	
			context.stroke();		
			context.restore();
		}
	}
	this.needsDisplay = false;
}




// Graph Constructor
function Graph( width, height ) {
	this.grid = new GraphGrid();
	this.bottomAxis = new GraphAxis( this.grid, 1 );
	this.leftAxis = new GraphAxis( this.grid, 2 );
	
	this.makeView();
	
	this.width = width != null ? width : 480;
	this.height = height != null ? height : 320;
	
	this.series = new Array();
}


// replace the specified placeholder node with this graph
Graph.prototype.replaceNode = function( placeholder ) {
	placeholder.parentNode.replaceChild( this.getView(), placeholder );
}


// replace the specified placeholder node with this graph
Graph.prototype.replaceNodeWithID = function( placeholderID ) {
	var placeholder = window.document.getElementById( placeholderID );
	this.replaceNode( placeholder );
}


// set the height
Graph.prototype.__defineSetter__( "height", function( height ) { this.setHeight( height ); } );


// set the height
Graph.prototype.setHeight = function( height ) {
	this._height = height;
	this.graphNode.style.height = (height + this.bottomAxis.getView().height) + "px";
	this.plotCanvas.setAttribute( "height", height );
	this.grid.height = height;
	this.leftAxis.setLength( height );
}


// get the height
Graph.prototype.__defineGetter__( "height", function() { return this._height; } );



// set the width
Graph.prototype.__defineSetter__( "width", function( width ) { this.setWidth( width ); } );


// set the width
Graph.prototype.setWidth = function( width ) {
	this._width = width;
	this.graphNode.style.width = (width + this.leftAxis.getView().width) + "px";
	this.grid.width = width;
	this.plotCanvas.setAttribute( "width", width );
	this.bottomAxis.setLength( width );
}


// get the width
Graph.prototype.__defineGetter__( "width", function() { return this._width; } );


// get the DOM view for the graph
Graph.prototype.getView = function() {
	return this.graphNode;
}
	

// create a DOM view for the graph
Graph.prototype.makeView = function() {
	//window.console.log( "making the view..." );
	this.graphNode = window.document.createElementNS( PLOT_HTML_NAME_SPACE, "div" );
	this.graphNode.setAttribute( "class", "Graph" );
	
	this.graphNode.appendChild( this.leftAxis.getView() );
	this.graphNode.appendChild( this.bottomAxis.getView() );
	
	this.plotAreaNode = window.document.createElementNS( PLOT_HTML_NAME_SPACE, "div" );
	this.graphNode.appendChild( this.plotAreaNode );
	this.plotAreaNode.setAttribute( "class", "PlotArea" );
	this.plotAreaNode.style.left = this.leftAxis.getView().getAttribute( "width" ) + "px";
	this.plotAreaNode.style.bottom = this.bottomAxis.getView().getAttribute( "height" ) + "px";
	
	this.plotAreaNode.appendChild( this.grid.getView() );
	
	this.plotCanvas = window.document.createElementNS( PLOT_HTML_NAME_SPACE, "canvas" );
	this.plotCanvas.setAttribute( "class", "Plot" );
	this.plotAreaNode.appendChild( this.plotCanvas );
}


// set the vertical limits
Graph.prototype.setYLimits = function( lowerLimit, upperLimit ) {
	this.leftAxis.setRange( lowerLimit, upperLimit );
}


// set the vertical limits
Graph.prototype.setXLimits = function( lowerLimit, upperLimit ) {
	this.bottomAxis.setRange( lowerLimit, upperLimit );
}


// set the background color
Graph.prototype.setBackgroundColor = function( color ) {
	this.grid.setBackgroundColor( color );
}


// draw the graph elements if needed
Graph.prototype.refresh = function() {
	this.clearPlot();
	this.plotAllSeries();
	this.leftAxis.displayIfNeeded();
	this.bottomAxis.displayIfNeeded();
	this.grid.displayIfNeeded();
}


// clear the plot
Graph.prototype.clearPlot = function() {
	var canvas = this.plotCanvas;
	if ( canvas ) {
		var context = canvas.getContext( "2d" );
		context.clearRect( 0, 0, canvas.width, canvas.height );
	}
}


// set the visibility of the grid
Graph.prototype.setGridVisible = function( visibility ) {
	this.grid.setVisible( visibility );
}


// remove all series
Graph.prototype.removeAllSeries = function() {
	this.series = new Array();
	this.clearPlot();
}


// add the series
Graph.prototype.addSeries = function( series ) {
	this.series.push( series );
}


// add the plot series
Graph.prototype.addPlotSeries = function( plotAdaptor, dataSource, pointColor ) {
	this.addSeries( new PlotSeries( plotAdaptor, dataSource, pointColor ) );
}


// plot all the series
Graph.prototype.plotAllSeries = function() {
	for ( var index = 0 ; index < this.series.length ; index++ ) {
		this.plotSeries( this.series[index] );
	}
}


// plot a single series
Graph.prototype.plotSeries = function( series ) {
	var plotAdaptor = series.plotAdaptor;
	plotAdaptor.draw( this, series );
}
