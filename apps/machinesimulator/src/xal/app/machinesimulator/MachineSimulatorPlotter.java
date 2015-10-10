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
		_twissParameterplotdata[0].setGraphColor(Color.black);
		_twissParameterplotdata[1].setGraphColor(new Color(148, 0, 211));//betax
		_twissParameterplotdata[2].setGraphColor(new Color(255, 69, 0)); //betay
		_twissParameterplotdata[3].setGraphColor(new Color(0, 0, 0));    //betaz
		_twissParameterplotdata[4].setGraphColor(new Color(47, 255, 225));//alphax
		_twissParameterplotdata[5].setGraphColor(new Color(0, 255, 255)); //alphay
		_twissParameterplotdata[6].setGraphColor(new Color(255, 255, 0)); // alphaz
		_twissParameterplotdata[7].setGraphColor(Color.red);              // gammax
		_twissParameterplotdata[8].setGraphColor(Color.red);              //gammay
		_twissParameterplotdata[9].setGraphColor(Color.red);              //gammaz
		_twissParameterplotdata[10].setGraphColor(Color.red);
		_twissParameterplotdata[11].setGraphColor(Color.red);
		_twissParameterplotdata[12].setGraphColor(Color.red);
		_twissParameterplotdata[13].setGraphColor(Color.red);
		_twissParameterplotdata[14].setGraphColor(Color.red);
		_twissParameterplotdata[15].setGraphColor(Color.red);
		_twissParameterplotdata[16].setGraphColor(Color.red);
		_twissParameterplotdata[17].setGraphColor(Color.red);
		_twissParameterplotdata[18].setGraphColor(Color.red);
		
		
		for(int i=0;i<19;i++){_twissParameterplotdata[i].setGraphProperty(_twissParametersplot.getLegendKeyString(), _parameterName[i]);}
	   for(int i=0;i<19;i++){_twissParametersplot.addGraphData(_twissParameterplotdata[i]);}
		
		
		}

}