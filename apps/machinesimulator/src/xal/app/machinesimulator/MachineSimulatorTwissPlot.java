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
	
	/**the total number of parameters to plot*/
	final private int PARAMETER_NUMBER=19;
	
	/**the parameters' name*/
	final String[] _parameterName=new String[]{ "kineticEnergy*","betay","betaz","betax","alphay",
			                                    "alphaz","alphax","gammay","gammaz","gammax","emittancey",
			                                    "emittancez","emittancex","envelopeRadiusy","envelopeRadiusz",
			                                    "envelopeRadiusx","betatronPhasey","betatronPhasez","betatronPhasex"};
	/**graphs of parameters*/
	final BasicGraphData[] _twissParameterPlotData= new BasicGraphData[PARAMETER_NUMBER];
	
	
	/**constructor*/
	public MachineSimulatorTwissPlot(final FunctionGraphsJPanel twissParametersPlot){

	   setupPlot(twissParametersPlot);
		
	}
	
	/**
	 * show the selected parameter's graph 
	 * @param position the position of all the elements in the selected sequence 
	 * @param twissParameterValues parameter's value of all the elements
	 * @param keypath the key path of parameter which is selected
	 */
	public void showTwissPlot(final List<Double> position,final List<Double> twissParameterValues,final String keypath){
		double[] position1=new double[position.size()];
		double[] twissParameterValues1=new double[twissParameterValues.size()];
		for(int j=0;j<position.size();j++){
			position1[j]=position.get(j);
			twissParameterValues1[j]=twissParameterValues.get(j);
		}
	_twissParameterPlotData[twissParameterIdentify(keypath)].updateValues(position1, twissParameterValues1);
	}


    /**
      * Identify which parameter the keyPath map to
      * @param keyPath the key path
      * @return An index of parameter name array
    */
	private int twissParameterIdentify(final String keyPath){
		int plane=-1;
		int _parameterNameIndex=-1;
	    List<Integer> indexes=new ArrayList<Integer>();
	    indexes.clear();
		final String[] keyParts=keyPath.split("\\.", keyPath.length());
		
     //compare keyPath with parameter's name	
		for(final String keyPart:keyParts){
			if(keyPart.length()==1) {
				 plane=Integer.parseInt(keyPart);
			}
			else{
			  for(int i=0;i<_parameterName.length;i++){
				 if(_parameterName[i].substring(0, _parameterName[i].length()-1).equals(keyPart)) indexes.add(i);				
			   }
			}
		}
     //select the mapped parameter
		if(indexes.size()!=1){
			for(final Integer index:indexes){
				if(index%3==plane) _parameterNameIndex=index.intValue();
			}
		}
		else _parameterNameIndex=indexes.get(0).intValue();
		return _parameterNameIndex;
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
		
		//initialize all the BasicGraphData
		for(int i=0;i<PARAMETER_NUMBER;i++){
		   	_twissParameterPlotData[i]=new BasicGraphData();
				}
		
	   // configure the graphic color
//	   _twissParameterPlotData[0].setGraphColor(IncrementalColors.getColor(0));   //kineticEnergy
		for(int i=1;i<PARAMETER_NUMBER;i++){
			switch ((int)Math.ceil((double)i/3)){
			case 1:
				_twissParameterPlotData[i].setGraphColor(IncrementalColors.getColor(1));//beta
				break;
			case 2:
				_twissParameterPlotData[i].setGraphColor(IncrementalColors.getColor(2));//alpha
				break;
			case 3:
				_twissParameterPlotData[i].setGraphColor(IncrementalColors.getColor(3));//gamma
				break;
			case 4:
				_twissParameterPlotData[i].setGraphColor(IncrementalColors.getColor(4));//emittance
				break;
			case 5:
				_twissParameterPlotData[i].setGraphColor(IncrementalColors.getColor(5));//envelopeRadius
				break;
			case 6:
				_twissParameterPlotData[i].setGraphColor(IncrementalColors.getColor(6));//betatronPhase
				break;
			default:
				break;
			}
		}
			    	
 //configure the graphic line pattern
		for(int i=1;i<PARAMETER_NUMBER;i++){
			switch (i%3) {
			case 1:
				_twissParameterPlotData[i].setLineDashPattern(3.0f);//y plane
				break;
			case 2:
				_twissParameterPlotData[i].setLineDashPattern(11.0f);//z plane
				break;
			default:
				break;
			}
		}

		
// set the graphic name		
		for(int i=0;i<PARAMETER_NUMBER;i++){
			_twissParameterPlotData[i].setGraphProperty(twissParametersPlot.getLegendKeyString(), _parameterName[i]);
		}
// add the graphs to plot panel		
	   for(int i=0;i<PARAMETER_NUMBER;i++){
		   twissParametersPlot.addGraphData(_twissParameterPlotData[i]);
		}
				
	}

}