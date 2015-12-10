/**
 *
 */
package xal.app.machinesimulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.plot.IncrementalColors;

/**
 * @author luxiaohan
 * plot the parameters
 */
public class MachineSimulatorTwissPlot{
	/**plot panel*/
	final private FunctionGraphsJPanel TWISS_PARAMETERS_PLOT;
	/**list of parameters*/
	final private List<Parameter> PARAMETERS;
	
	/**constructor*/
	public MachineSimulatorTwissPlot( final FunctionGraphsJPanel twissParametersPlot,
			final List<ScalarParameter>scalarParameters, final List<VectorParameter>vectorParameters  ) {
		TWISS_PARAMETERS_PLOT=twissParametersPlot;
		PARAMETERS=new ArrayList<Parameter>( scalarParameters.size() + vectorParameters.size() );
		//put scalar and vector parameter together
		PARAMETERS.addAll( scalarParameters );
		PARAMETERS.addAll( vectorParameters );
		
	   configurePlotPanel();	
	}
	
	/**
	 * show the selected parameter's graph 
	 * @param position the position of all the elements in the selected sequence 
	 * @param twissParameterValues parameter's value of all the elements
	 * @param keypath the key path of parameter which is selected
	 */
	public void showTwissPlot( final List<Double> position, final List<Double> twissParameterValues,
			final String keyPath, final String legend ){
		double[] position1= new double[position.size()];
		double[] twissParameterValues1= new double[twissParameterValues.size()];
		for( int index=0; index < position.size(); index++ ){
			position1[index]=position.get( index );
			twissParameterValues1[index]=twissParameterValues.get( index );
		}
		BasicGraphData twissParameterPlotData= new BasicGraphData();
		configureGraph( twissParameterPlotData, keyPath, legend );
		
	   twissParameterPlotData.updateValues( position1, twissParameterValues1 );
		TWISS_PARAMETERS_PLOT.addGraphData( twissParameterPlotData );
	}
	/**
	 * configure the graph with color and line pattern
	 * @param graphData the graph to configure
	 * @param keyPath  the key path to identify which parameter it is
	 */
	
	private void configureGraph( final BasicGraphData graphData, final String keyPath, final String legend ) {
		final String keyPathForOld = keyPath.substring(4);
		int parameterIndex = 0;
		boolean isParameter = false;
		boolean isParameterForOld = false;
		for( int index=0; index<PARAMETERS.size(); index++ ){
			if( PARAMETERS.get( index ).isThisParameter( keyPath ) ) {
				parameterIndex = index;
				isParameter = true;
			}
			else if( PARAMETERS.get( index ).isThisParameter( keyPathForOld ) ) {
				parameterIndex = index;
				isParameterForOld = true;
			}
		}
			
			if( isParameter ){			
				//configure the graphic name
				graphData.setGraphProperty( TWISS_PARAMETERS_PLOT.getLegendKeyString(),
						PARAMETERS.get( parameterIndex ).getParameterName( keyPath )+" : "+legend );
				// configure the graphic color
				graphData.setGraphColor( IncrementalColors.getColor( parameterIndex ) );
				//configure the graphic line pattern
				switch ( PARAMETERS.get( parameterIndex ).getPlane( keyPath ) ) {
				case "X":
					graphData.setLineDashPattern( null );//x plane
					break;
				case "Y":
					graphData.setLineDashPattern( 3.0f );//y plane
					break;
				case "Z":
					graphData.setLineDashPattern( 11.0f );//z plane
					break;
				default:
					break;
				}
			}
			//configure the graph for old simulation results
			else if ( isParameterForOld ) {
				graphData.setGraphProperty( TWISS_PARAMETERS_PLOT.getLegendKeyString(),
						PARAMETERS.get( parameterIndex ).getParameterName( keyPathForOld )+" : "+legend );
				graphData.setGraphColor( IncrementalColors.getColor( parameterIndex+1 ) );
				
				//configure the graphic line pattern
				switch ( PARAMETERS.get( parameterIndex ).getPlane( keyPathForOld ) ) {
				case "X":
					graphData.setLineDashPattern( null );//x plane for old
					break;
				case "Y":
					graphData.setLineDashPattern( 3.0f );//y plane for old
					break;
				case "Z":
					graphData.setLineDashPattern( 11.0f );//z plane for old
					break;
				default:
					break;
				}
			}
			//configure the graph which isn't twiss parameter
			else {
				graphData.setGraphProperty( TWISS_PARAMETERS_PLOT.getLegendKeyString(), legend );
				graphData.setGraphColor( IncrementalColors.getColor( 10 ) );
				//configure the graphic line pattern
				switch ( keyPath.substring(0, 1).toUpperCase() ) {
				case "X":
					graphData.setLineDashPattern( null );//x plane					
					break;
				case "Y":
					graphData.setLineDashPattern( 3.0f );//y plane					
					break;
				case "Z":
					graphData.setLineDashPattern( 11.0f );//z plane
					break;
				default:
					break;
				}
			}
		
	}
	
    /**configure plot panel*/	
	private void configurePlotPanel() {
		
		//labels
		TWISS_PARAMETERS_PLOT.setName( "" );
		TWISS_PARAMETERS_PLOT.setAxisNameX( "Position(m)" );
		
		//add legend support
		TWISS_PARAMETERS_PLOT.setLegendButtonVisible( true );
		TWISS_PARAMETERS_PLOT.setLegendBackground( Color.white );
		//add popup menu to the plot panel
		SimpleChartPopupMenu.addPopupMenuTo( TWISS_PARAMETERS_PLOT );

		}
	
	/**set the name of the plot panel*/
	public void setName( final String name ) {
		TWISS_PARAMETERS_PLOT.setName( name );
	}

}