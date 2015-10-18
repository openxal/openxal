/**
 * 
 */
package xal.app.machinesimulator;

import java.awt.Color;
import java.util.List;

import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;

/**
 * @author luxiaohan
 * plot the parameters 
 */
public class MachineSimulatorPlotter {
	/** */
	public FunctionGraphsJPanel _twissParametersPlot;
	
	final int PARAMETER_NUMBER=19;
	
	public String[] _parameterName=new String[]{ "kineticenery","betax","betay","betaz","alphax",
			                                               "alphay","alphaz","gammax","gammay","gammaz",
			                                               "emittancex","emittancey","emittancez","beamsizex",
			                                                "beamsizey","beamsizez","betatronphasex","betatronphasey","betatronphasez"};
	public BasicGraphData[] _twissParameterPlotData = new BasicGraphData[PARAMETER_NUMBER];
	
	
	/*constructor*/
	public MachineSimulatorPlotter(FunctionGraphsJPanel twissParametersPlot){
		_twissParametersPlot=twissParametersPlot;

	   setupPlot(twissParametersPlot);
		
	}
	//show the Graphs 
	public void showtwissplot(List<Double> p,List<Double> x,String a){
		double[] p1=new double[p.size()];
		double[] x1=new double[x.size()];
		for(int j=0;j<p.size();j++){
			p1[j]=p.get(j);
			x1[j]=x.get(j);
		}
		int i=0;
		for(int k=0;k<_parameterName.length;k++){if(_parameterName[k].equals(a)) i=k;}
	_twissParameterPlotData[i].updateValues(p1, x1);
	}
	
     /**setup twissplot*/	
	public void setupPlot(FunctionGraphsJPanel twissParametersPlot){
		
		//labels
		twissParametersPlot.setName("");
		twissParametersPlot.setAxisNameX("Position(m)");
		twissParametersPlot.setAxisNameY("");
		
		//add legend support
		twissParametersPlot.setLegendButtonVisible(true);
		twissParametersPlot.setLegendBackground(Color.white);
		//initialize all the BasicGraphData
		for(int i=0;i<PARAMETER_NUMBER;i++){
		   	_twissParameterPlotData[i]=new BasicGraphData();
				}
		// configure the graphic color
		_twissParameterPlotData[0].setGraphColor(new Color(0, 0, 0));   //kineticenery
		_twissParameterPlotData[1].setGraphColor(new Color(148, 0, 211));//betax
		_twissParameterPlotData[2].setGraphColor(new Color(255, 69, 0)); //betay
		_twissParameterPlotData[3].setGraphColor(new Color(0, 255, 0));    //betaz
		_twissParameterPlotData[4].setGraphColor(new Color(0, 70, 70));//alphax
		_twissParameterPlotData[5].setGraphColor(new Color(0, 255, 255)); //alphay
		_twissParameterPlotData[6].setGraphColor(new Color(180, 255, 0)); // alphaz
		_twissParameterPlotData[7].setGraphColor(new Color(255, 0, 0));// gammax
		_twissParameterPlotData[8].setGraphColor(new Color(255, 165, 0));//gammay
		_twissParameterPlotData[9].setGraphColor(new Color(0, 0, 255));//gammaz
		_twissParameterPlotData[10].setGraphColor(new Color(225, 150, 0));//emittancex
		_twissParameterPlotData[11].setGraphColor(new Color(120, 225, 255));//emittancey
		_twissParameterPlotData[12].setGraphColor(new Color(50, 25, 175));//emittancez
		_twissParameterPlotData[13].setGraphColor(new Color(120, 0, 140));//beamsizex
		_twissParameterPlotData[14].setGraphColor(new Color(240, 20, 20));//beamsizey
		_twissParameterPlotData[15].setGraphColor(new Color(0, 140, 70));//beamsizez
		_twissParameterPlotData[16].setGraphColor(new Color(120, 70, 70));//betatronphasex
		_twissParameterPlotData[17].setGraphColor(new Color(200, 0, 70));//betatronphasey
		_twissParameterPlotData[18].setGraphColor(new Color(0, 0, 139));//betatronphasez
		
		
		for(int i=0;i<PARAMETER_NUMBER;i++){_twissParameterPlotData[i].setGraphProperty(_twissParametersPlot.getLegendKeyString(), _parameterName[i]);}
	   for(int i=0;i<PARAMETER_NUMBER;i++){_twissParametersPlot.addGraphData(_twissParameterPlotData[i]);}
		
		
		}

}