/**
 *
 */
package xal.app.machinesimulator;

import java.awt.Color;
import java.awt.Polygon;
import java.util.List;

import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.plot.IncrementalColors;

/**
 * @author luxiaohan
 * plot the parameters
 */
public class MachineSimulatorPlot{
	/**plot panel*/
	final private FunctionGraphsJPanel PARAMETERS_PLOT;
	/**list of parameters*/
	final private List<Parameter> PARAMETERS;
	
	/**constructor*/
	public MachineSimulatorPlot( final FunctionGraphsJPanel parametersPlot, final List<Parameter>parameters ) {
		PARAMETERS_PLOT = parametersPlot;
		PARAMETERS = parameters;
		
	   configurePlotPanel();	
	}
	
	/**
	 * show the selected parameter's graph 
	 * @param position the position of all the elements in the selected sequence 
	 * @param parameterValues parameter's value of all the elements
	 * @param keypath the key path of parameter which is selected
	 */
	public void showPlot( final List<Double> position, final List<Double> parameterValues,
			final String keyPath, final String legend ){
		double[] position1= new double[position.size()];
		double[] twissParameterValues1= new double[parameterValues.size()];
		for( int index=0; index < position.size(); index++ ){
			position1[index]=position.get( index );
			twissParameterValues1[index]=parameterValues.get( index );
		}
		BasicGraphData parameterPlotData= new BasicGraphData();
		configureGraph( parameterPlotData, keyPath, legend );
		
	   parameterPlotData.updateValues( position1, twissParameterValues1 );
		PARAMETERS_PLOT.addGraphData( parameterPlotData );
	}
	/**
	 * configure the graph with color and line pattern
	 * @param graphData the graph to configure
	 * @param keyPath  the key path to identify which parameter it is
	 */
	
	private void configureGraph( final BasicGraphData graphData, final String keyPath, final String legend ) {
		final String keyPathForOld = keyPath.substring(4);
		final String key = ( keyPath.contains("old") )? keyPathForOld : keyPath;
		int parameterIndex = 0;
		boolean isParameter = false;
		boolean isParameterForOld = false;
		for( int index=0; index < PARAMETERS.size(); index++ ){
			if( PARAMETERS.get( index ).isThisParameter( key ) ) {
				isParameter = true;
				parameterIndex = index;
				if ( keyPath.contains( "old" ) || keyPath.startsWith("1") ) isParameterForOld = true;
			}
		}
			
			if( isParameter ){
				//configure the graphic color
				if ( isParameterForOld ) graphData.setGraphColor( IncrementalColors.getColor( parameterIndex ) );
				else graphData.setGraphColor( IncrementalColors.getColor( parameterIndex+1 ) );
				//configure the graphic name
				graphData.setGraphProperty( PARAMETERS_PLOT.getLegendKeyString(),
						PARAMETERS.get( parameterIndex ).getParameterName( key )+" : "+legend );
				//configure the graphic line and point pattern
				final double pointMarkLeg = 8.0;
				final double offset = - pointMarkLeg / 2.0;// point mark offset
				switch ( PARAMETERS.get( parameterIndex ).getPlane( key ) ) {
				case "X":
					graphData.setLineDashPattern( null );//x plane
					graphData.setGraphPointShape( 
							new java.awt.geom.Ellipse2D.Double( offset, offset, pointMarkLeg, pointMarkLeg ) );
					break;
				case "Y":
					graphData.setLineDashPattern( 3.0f );//y plane
					graphData.setGraphPointShape( 
							new Polygon( new int[]{0, -(int)offset, (int)offset }, new int[]{ (int)offset, -(int)offset, -(int)offset }, 3 ) );
					break;
				case "Z":
					graphData.setLineDashPattern( 11.0f );//z plane
					graphData.setGraphPointShape( 
							new java.awt.geom.Rectangle2D.Double(offset, offset, pointMarkLeg, pointMarkLeg ) );
					break;
				default:
					break;
				}
			}	
	}
	
    /**configure plot panel*/	
	private void configurePlotPanel() {
		
		//labels
		PARAMETERS_PLOT.setName( "" );
		PARAMETERS_PLOT.setAxisNameX( "Position(m)" );
		
		//add legend support
		PARAMETERS_PLOT.setLegendButtonVisible( true );
		PARAMETERS_PLOT.setLegendBackground( Color.white );
		//add popup menu to the plot panel
		SimpleChartPopupMenu.addPopupMenuTo( PARAMETERS_PLOT );

		}

}