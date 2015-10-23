/**
 *
 */
package xal.app.machinesimulator;

import java.awt.Color;
import java.util.List;

import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.plot.IncrementalColors;

/**
 * @author luxiaohan
 * plot the parameters
 */
public class MachineSimulatorTwissPlot{
	/**plot panel*/
	private FunctionGraphsJPanel _twissParametersPlot;
	/**list of parameters*/
	final private List<Parameter> PARAMETERS;
	
	
	/**constructor*/
	public MachineSimulatorTwissPlot(final FunctionGraphsJPanel twissParametersPlot,final List<Parameter>... parameters){
		_twissParametersPlot=twissParametersPlot;
		PARAMETERS=parameters;
		
	   setupPlot(twissParametersPlot);
		
	}
	
	/**
	 * show the selected parameter's graph 
	 * @param position the position of all the elements in the selected sequence 
	 * @param twissParameterValues parameter's value of all the elements
	 * @param keypath the key path of parameter which is selected
	 */
	public void showTwissPlot(final List<Double> position,final List<Double> twissParameterValues,final String keyPath){
		double[] position1=new double[position.size()];
		double[] twissParameterValues1=new double[twissParameterValues.size()];
		for(int j=0;j<position.size();j++){
			position1[j]=position.get(j);
			twissParameterValues1[j]=twissParameterValues.get(j);
		}
		BasicGraphData twissParameterPlotData= new BasicGraphData();
		configureGraph(twissParameterPlotData, keyPath);
		
	   twissParameterPlotData.updateValues(position1, twissParameterValues1);
		_twissParametersPlot.addGraphData(twissParameterPlotData);
	}
	/**
	 * configure the graph with color and line pattern
	 * @param graphData the graph to configure
	 * @param keyPath  the key path to identify which parameter it is
	 */
	
	public void configureGraph(final BasicGraphData graphData,final String keyPath){
		for(int parameterIndex=0;parameterIndex<PARAMETERS.size();parameterIndex++){
			if(PARAMETERS.get(parameterIndex).isThisParameter(keyPath)){
				//configure the graphic name
				graphData.setGraphProperty(_twissParametersPlot.getLegendKeyString(), PARAMETERS.get(parameterIndex).getParameterName(keyPath));
				// configure the graphic color
				graphData.setGraphColor(IncrementalColors.getColor(parameterIndex));
				//configure the graphic line pattern
				switch (PARAMETERS.get(parameterIndex).getPlane(keyPath)) {
				case "X":
					graphData.setLineDashPattern(null);//x plane
					break;
				case "Y":
					graphData.setLineDashPattern(3.0f);//y plane
					break;
				case "Z":
					graphData.setLineDashPattern(11.0f);//z plane
					break;
				default:
					break;
				}
			}
		}
		
	}
	
     /**setup twiss plot
      *@param twissParametersPlot the plot panel
      */	
	public void setupPlot(final FunctionGraphsJPanel twissParametersPlot){
		
		//labels
		twissParametersPlot.setName("");
		twissParametersPlot.setAxisNameX("Position(m)");
		
		//add legend support
		twissParametersPlot.setLegendButtonVisible(true);
		twissParametersPlot.setLegendBackground(Color.white);

		}

}