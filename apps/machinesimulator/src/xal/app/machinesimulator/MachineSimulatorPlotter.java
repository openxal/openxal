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
 *
 */
public class MachineSimulatorPlotter {
	
	public FunctionGraphsJPanel _twissParametersplot;
	
	public String[] _parameterName=new String[]{ "kineticenery","betax","betay","betaz","alphax",
			                                               "alphay","alphaz","gammax","gammay","gammaz",
			                                               "emittancex","emittancey","emittancez","beamsizex",
			                                                "beamsizey","beamsizez","betatronphasex","betatronphasey","betatronphasez"};
	public BasicGraphData[] _twissParameterplotdata = new BasicGraphData[19];
	
	
	/*constructor*/
	public MachineSimulatorPlotter(FunctionGraphsJPanel twissParametersplot){
		_twissParametersplot=twissParametersplot;

	 //  setupPlot(twissParametersplot);
		
	}
	
	public void showtwissplot(List<Double> p,List<Double> x,int i){
		double[] p1=new double[p.size()];
		double[] x1=new double[x.size()];
		for(int j=0;j<p.size();j++){
			p1[j]=p.get(j);
			x1[j]=x.get(j);
		}
		
	_twissParameterplotdata[i].updateValues(p1, x1);
	}
	
     /**setup twissplot*/	
	public void setupPlot(FunctionGraphsJPanel twissParametersplot){
		
		twissParametersplot.setName("");
		twissParametersplot.setAxisNameX("Position(m)");
		twissParametersplot.setAxisNameY("");
		
		for(int i=0;i<19;i++){
		   	_twissParameterplotdata[i]=new BasicGraphData();
				}
		_twissParameterplotdata[0].setGraphColor(new Color(0, 0, 0));
		_twissParameterplotdata[1].setGraphColor(new Color(148, 0, 211));//betax
		_twissParameterplotdata[2].setGraphColor(new Color(255, 69, 0)); //betay
		_twissParameterplotdata[3].setGraphColor(new Color(0, 255, 0));    //betaz
		_twissParameterplotdata[4].setGraphColor(new Color(0, 70, 70));//alphax
		_twissParameterplotdata[5].setGraphColor(new Color(0, 255, 255)); //alphay
		_twissParameterplotdata[6].setGraphColor(new Color(255, 255, 0)); // alphaz
		_twissParameterplotdata[7].setGraphColor(new Color(255, 0, 0));// gammax
		_twissParameterplotdata[8].setGraphColor(new Color(255, 165, 0));//gammay
		_twissParameterplotdata[9].setGraphColor(new Color(0, 0, 255));//gammaz
		_twissParameterplotdata[10].setGraphColor(new Color(225, 150, 0));//emittancex
		_twissParameterplotdata[11].setGraphColor(new Color(120, 225, 255));//emittancey
		_twissParameterplotdata[12].setGraphColor(new Color(50, 25, 175));//emittancez
		_twissParameterplotdata[13].setGraphColor(new Color(120, 0, 140));//beamsizex
		_twissParameterplotdata[14].setGraphColor(new Color(240, 20, 20));//beamsizey
		_twissParameterplotdata[15].setGraphColor(new Color(0, 140, 70));//beamsizez
		_twissParameterplotdata[16].setGraphColor(new Color(120, 70, 70));//betatronphasex
		_twissParameterplotdata[17].setGraphColor(new Color(200, 0, 70));//betatronphasey
		_twissParameterplotdata[18].setGraphColor(new Color(0, 0, 139));//betatronphasez
		
		
		for(int i=0;i<19;i++){_twissParameterplotdata[i].setGraphProperty(_twissParametersplot.getLegendKeyString(), _parameterName[i]);}
	   for(int i=0;i<19;i++){_twissParametersplot.addGraphData(_twissParameterplotdata[i]);}
		
		
		}

}