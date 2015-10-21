/**
 *
 */
package xal.app.machinesimulator;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.plot.IncrementalColors;

/**
 * @author luxiaohan
 * plot the parameters
 */
public class MachineSimulatorTwissPlot{
	
	private FunctionGraphsJPanel _twissParametersPlot;
	
	/***/
	final private List<Parameter> PARAMETERS;
	
	
	/**constructor*/
	public MachineSimulatorTwissPlot(final FunctionGraphsJPanel twissParametersPlot){
		
		_twissParametersPlot=twissParametersPlot;
		
		PARAMETERS = new ArrayList<Parameter>();
		PARAMETERS.add(new ScalarParameter("Kinetic Energy", "probeState.kineticEnergy"));
		PARAMETERS.add(new VectorParameter("Beta", "twissParameters", "beta"));
		PARAMETERS.add(new VectorParameter("Alpha", "twissParameters", "alpha"));
		PARAMETERS.add(new VectorParameter("Gamma", "twissParameters", "gamma"));
		PARAMETERS.add(new VectorParameter("Emittance", "twissParameters", "emittance"));
		PARAMETERS.add(new VectorParameter("EnvelopeRadius", "twissParameters", "envelopeRadius"));
		PARAMETERS.add(new VectorParameter("BetatronPhase", "betatronPhase"));
		
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
	
	public void configureGraph(final BasicGraphData graphData,final String keyPath){
		for(final Parameter parameter:PARAMETERS){
			if(parameter.isThisParameter(keyPath)){
				System.out.println(parameter.getParameterName(keyPath));
				graphData.setGraphProperty(_twissParametersPlot.getLegendKeyString(), parameter.getParameterName(keyPath));
				// configure the graphic color
				switch (parameter.getLable()){
				case "Kinetic Energy":
					graphData.setGraphColor(IncrementalColors.getColor(0));//kineticEnergy
				case "Beta":
					graphData.setGraphColor(IncrementalColors.getColor(1));//beta
					break;
				case "Alpha":
					graphData.setGraphColor(IncrementalColors.getColor(2));//alpha
					break;
				case "Gamma":
					graphData.setGraphColor(IncrementalColors.getColor(3));//gamma
					break;
				case "Emittance":
					graphData.setGraphColor(IncrementalColors.getColor(4));//emittance
					break;
				case "EnvelopRadius":
					graphData.setGraphColor(IncrementalColors.getColor(5));//envelopeRadius
					break;
				case "BetatronPhase":
					graphData.setGraphColor(IncrementalColors.getColor(6));//betatronPhase
					break;
				default:
					break;
				}
				
				switch (parameter.getPlane(keyPath)) {
				case "X":
					graphData.setLineDashPattern(null);//x plane
					break;
				case "Y":
					graphData.setLineDashPattern(3.0f);//y plane
					break;
				case "Z":
					graphData.setLineDashPattern(11.0f);//z plane
				default:
					break;
				}
			}
		}
		


		
		 //configure the graphic line pattern
		
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
		
// set the graphic name		
//		for(int i=0;i<PARAMETER_NUMBER;i++){
//			_twissParameterPlotData[i].setGraphProperty(twissParametersPlot.getLegendKeyString(), _parameterName[i]);
//		}



}