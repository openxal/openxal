package xal.extension.wirescan.apputils;

import java.util.List;
import java.util.ArrayList;


import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.solver.Scorer;
import xal.extension.solver.Trial;
import xal.extension.solver.Variable;
import xal.extension.solver.Stopper;
import xal.extension.solver.SolveStopperFactory;
import xal.extension.solver.ProblemFactory;
import xal.extension.solver.Solver;
import xal.extension.solver.Problem;
import xal.extension.solver.algorithm.SimplexSearchAlgorithm;
import xal.extension.solver.hint.Hint;
import xal.extension.solver.hint.InitialDelta;

/*
 * The  GaussFitter class fits the waveforms from WireScanData class. 
 * It assumes the Gaussian form of the function.
 */
public class GaussFitter{
	
	private int nIterations = 1000;
	
	private int nGraphPoints = 200;
	
	private double wCoeff = 5.0;
	
	private boolean [] fit_on_arr = {true,true,true,true};
	
	/** Constructor of a default wire scanner data fitter */
	public GaussFitter(){	
	}	
	
	/** Stes the variables that will be used in fitting. The input parameter 
	    is an 4-elements boolean array with true or false for the base line, 
			center position, sigma, and amplitude of the Gaussian approximation. 
	*/ 
	public void setVariablesOn(boolean [] fit_on_arr){
		this.fit_on_arr[0] = fit_on_arr[0];
		this.fit_on_arr[1] = fit_on_arr[1];
		this.fit_on_arr[2] = fit_on_arr[2];
		this.fit_on_arr[3] = fit_on_arr[3];
	}
	
	/** Guess initial Gauss parameters and fit for both planes: X and Y */
	public boolean guessAndFit(WireScanData wsD){
		boolean res_x = this.guessAndFitX(wsD);
		boolean res_y = this.guessAndFitY(wsD);
		if(res_x == true && res_y == true) return true;
		return false;
	}
		
	/** Fit for both planes X and Y without initial guess */
	public boolean fitAgain(WireScanData wsD){
		boolean res_x = this.fitAgainX(wsD);
		boolean res_y = this.fitAgainY(wsD);
		if(res_x == true && res_y == true) return true;
		return false;
	}
	
	/** Guess initial Gauss parameters and fit for X plane */
	public boolean guessAndFitX(WireScanData wsD){
		wsD.getFitWFX().removeAllPoints();
		wsD.getLogFitWFX().removeAllPoints(); 		
		double [] params_arr = this.guessParams(wsD.getRawWFX());
		if(params_arr == null) return false;
		boolean res = this.gaussFit(params_arr,wsD.getRawWFX(),wsD.getFitWFX(),wsD.getLogFitWFX());
		double base = params_arr[0];
		double center = params_arr[1];			
		double sigma = params_arr[2];
		double amp = params_arr[3];
		wsD.setBaseX(base);
		wsD.setCenterX(center);		
		wsD.setSigmaX(sigma);
		wsD.setAmpX(amp);
		if(res == false) return false;
		double [] res_rms_arr = this.getCenterAndSigmaRms(center,sigma,wsD.getRawWFX());
		wsD.setCenterRmsX(res_rms_arr[0]);
		wsD.setSigmaRmsX(	res_rms_arr[1]);	
		return true;
	}
	
	/** Guess initial Gauss parameters and fit for Y plane */
	public boolean guessAndFitY(WireScanData wsD){
		wsD.getFitWFY().removeAllPoints();
		wsD.getLogFitWFY().removeAllPoints(); 		
		double [] params_arr = this.guessParams(wsD.getRawWFY());
		if(params_arr == null) return false;
		boolean res = this.gaussFit(params_arr,wsD.getRawWFY(),wsD.getFitWFY(),wsD.getLogFitWFY());
		double base = params_arr[0];
		double center = params_arr[1];			
		double sigma = params_arr[2];
		double amp = params_arr[3];
		wsD.setBaseY(base);
		wsD.setCenterY(center);		
		wsD.setSigmaY(sigma);
		wsD.setAmpY(amp);
		if(res == false) return false;
		double [] res_rms_arr = this.getCenterAndSigmaRms(center,sigma,wsD.getRawWFY());
		wsD.setCenterRmsY(res_rms_arr[0]);
		wsD.setSigmaRmsY(	res_rms_arr[1]);			
		return true;
	}	
	
	/** Fit for X plane without initial guess */
	public boolean fitAgainX(WireScanData wsD){
		wsD.getFitWFX().removeAllPoints();
		wsD.getLogFitWFX().removeAllPoints(); 
    double [] params_arr = new double[4];
		params_arr[0] = wsD.getBaseX();
		params_arr[1] = wsD.getCenterX();
		params_arr[2] = wsD.getSigmaX();
		params_arr[3] = wsD.getAmpX();	
		if(wsD.getSigmaX() == 0.) params_arr = this.guessParams(wsD.getRawWFX());
		boolean res = this.gaussFit(params_arr,wsD.getRawWFX(),wsD.getFitWFX(),wsD.getLogFitWFX());
		double base = params_arr[0];
		double center = params_arr[1];			
		double sigma = params_arr[2];
		double amp = params_arr[3];
		wsD.setBaseX(base);
		wsD.setCenterX(center);		
		wsD.setSigmaX(sigma);
		wsD.setAmpX(amp);
		if(res == false) return false;
		double [] res_rms_arr = this.getCenterAndSigmaRms(center,sigma,wsD.getRawWFX());
		wsD.setCenterRmsX(res_rms_arr[0]);
		wsD.setSigmaRmsX(	res_rms_arr[1]);		
		return true;		
	}
	
	/** Fit for Y plane without initial guess */
	public boolean fitAgainY(WireScanData wsD){
		wsD.getFitWFY().removeAllPoints();
		wsD.getLogFitWFY().removeAllPoints(); 
    double [] params_arr = new double[4];
		params_arr[0] = wsD.getBaseY();
		params_arr[1] = wsD.getCenterY();
		params_arr[2] = wsD.getSigmaY();
		params_arr[3] = wsD.getAmpY();		
		if(wsD.getSigmaY() == 0.) params_arr = this.guessParams(wsD.getRawWFY());		
		boolean res = this.gaussFit(params_arr,wsD.getRawWFY(),wsD.getFitWFY(),wsD.getLogFitWFY());
		double base = params_arr[0];
		double center = params_arr[1];			
		double sigma = params_arr[2];
		double amp = params_arr[3];
    //System.out.println("debug fit again Y fit base="+base+" center="+center+" sigma="+sigma+" amp="+amp);		
		wsD.setBaseY(base);
		wsD.setCenterY(center);		
		wsD.setSigmaY(sigma);
		wsD.setAmpY(amp);
		if(res == false) return false;
		double [] res_rms_arr = this.getCenterAndSigmaRms(center,sigma,wsD.getRawWFY());
		wsD.setCenterRmsY(res_rms_arr[0]);
		wsD.setSigmaRmsY(	res_rms_arr[1]);	
		return true;		
	}	
	
	private boolean gaussFit( double [] params_arr, final BasicGraphData gD, BasicGraphData fit_gD,BasicGraphData fit_log_gD){		
		final double base = params_arr[0];
		final double center = params_arr[1];			
		final double sigma = params_arr[2];
		final double amp = params_arr[3];
		
		int ind_start0 = gD.getNumbOfPoints();
		int ind_stop0 = 0;
		for(int ix = 0; ix <  gD.getNumbOfPoints() ; ix++){
			double x = gD.getX(ix);
			if( Math.abs(x-center) < wCoeff*sigma){
				if(ind_start0 > ix) ind_start0 = ix;
				if(ind_stop0 < ix) ind_stop0 = ix;
			}
		}
		if( (ind_stop0 - ind_start0) < 3 ) return false;
		
		final int ind_start = ind_start0;
		final int ind_stop = ind_stop0;
		//System.out.println("debug fit x_min="+gD.getX(ind_start)+" x_max="+gD.getX(ind_stop));
		final ArrayList<Variable> variables = new ArrayList<Variable>();
		variables.add(new Variable( "base",   base,   - Double.MAX_VALUE, Double.MAX_VALUE ) );
		variables.add(new Variable( "center", center, - Double.MAX_VALUE, Double.MAX_VALUE ) );
		variables.add(new Variable( "sigma",  sigma,  - Double.MAX_VALUE, Double.MAX_VALUE ) );
		variables.add(new Variable( "amp",    amp,    - Double.MAX_VALUE, Double.MAX_VALUE ) )	;			
		
		Scorer scorer = new Scorer(){
			public double score( final Trial trial, final List<Variable> variables_tmp ){
				double diff = 0.;
				java.util.Map<Variable,java.lang.Number> var_map = trial.getTrialPoint().getValueMap();
				double base0 = base;
				double center0 = center;
				double sigma0 = sigma;
				double amp0 =amp;
				if(var_map.containsKey(variables.get(0))){
					base0 = trial.getTrialPoint().getValue(variables.get(0));
				}
				if(var_map.containsKey(variables.get(1))){
					center0 = trial.getTrialPoint().getValue(variables.get(1));
				}
				if(var_map.containsKey(variables.get(2))){
					sigma0 = trial.getTrialPoint().getValue(variables.get(2));
				}
				if(var_map.containsKey(variables.get(3))){
					amp0 = trial.getTrialPoint().getValue(variables.get(3));
				}
				double y_th,x,y;
				for(int ix = ind_start; ix <= ind_stop; ix++){
					x = gD.getX(ix);
					y = gD.getY(ix);
					y_th = base0 + amp0*Math.exp(-(x-center0)*(x-center0)/(2*sigma0*sigma0));
					diff += (y - y_th)*(y - y_th);
				}
				//System.out.println("debug iteration fit base="+base0+" center="+center0+" sigma="+sigma0+" amp="+amp0);
				return diff;
			}
		};
		//System.out.println("debug init fit base="+base+" center="+center+" sigma="+sigma+" amp="+amp);
		
		Stopper maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper(nIterations); 
		Solver solver = new Solver(new SimplexSearchAlgorithm(),maxSolutionStopper);
		ArrayList<Variable> variables_on = new ArrayList<Variable>();
		for(int iv = 0; iv < 4; iv++){
			if(fit_on_arr[iv]) variables_on.add(variables.get(iv));
		}
		Problem problem = ProblemFactory.getInverseSquareMinimizerProblem(variables_on,scorer,amp*0.0001);
		InitialDelta hint = new InitialDelta();
		hint.addInitialDelta(variables.get(0), amp*0.001);
		hint.addInitialDelta(variables.get(1), sigma*0.05);
		hint.addInitialDelta(variables.get(2), sigma*0.05);
		hint.addInitialDelta(variables.get(3), amp*0.05);
		problem.addHint(hint);	
		solver.solve(problem);
		
		double base0 = base;
		double center0 = center;
		double sigma0 = sigma;
		double amp0 = amp;				
		Trial trial = solver.getScoreBoard().getBestSolution();
		java.util.Map<Variable,java.lang.Number> var_map = trial.getTrialPoint().getValueMap();
		if(var_map.containsKey(variables.get(0))){
			base0 = trial.getTrialPoint().getValue(variables.get(0));
		}
		if(var_map.containsKey(variables.get(1))){
			center0 = trial.getTrialPoint().getValue(variables.get(1));
		}
		if(var_map.containsKey(variables.get(2))){
			sigma0 = trial.getTrialPoint().getValue(variables.get(2));
		}
		if(var_map.containsKey(variables.get(3))){
			amp0 = trial.getTrialPoint().getValue(variables.get(3));
		}		
		params_arr[0] = base0;
		params_arr[1] = center0;
		params_arr[2] = sigma0;
		params_arr[3] = amp0;	
		//System.out.println("debug end fit base="+base0+" center="+center0+" sigma="+sigma0+" amp="+amp0);
		double step = (gD.getX(ind_stop) - gD.getX(ind_start))/(nGraphPoints-1);
		for(int ix = 0; ix < nGraphPoints; ix++){
			double x = gD.getX(ind_start) + step*ix;
			double y = base0 + amp0*Math.exp(-(x-center0)*(x-center0)/(2*sigma0*sigma0));
			fit_gD.addPoint(x,y);
			if(y > 0.){
				fit_log_gD.addPoint(x,Math.log10(y));
			}
		}
		return true;
	}
	
	private double [] getCenterAndSigmaRms(double center, double sigma, BasicGraphData gD){
		double centerRms = 0.;		
		double sigmaRms = 0.;
		double weight = 0.;
		double [] res_arr = new double[2];
		res_arr[0] = centerRms;
		res_arr[1] = sigmaRms;
		for(int ix = 0; ix <  gD.getNumbOfPoints() ; ix++){
			double x = gD.getX(ix);
			double y = gD.getY(ix);
			if(Math.abs(center -x) < sigma*wCoeff && y > 0.){
				weight += y;
				centerRms += x*y;
			}
		}
		if(weight == 0.) return res_arr;
		centerRms = centerRms/weight;
		for(int ix = 0; ix <  gD.getNumbOfPoints() ; ix++){
			double x = gD.getX(ix);
			double y = gD.getY(ix);
			if(Math.abs(center -x) < sigma*wCoeff && y > 0.){
				sigmaRms += (x - centerRms)*(x - centerRms)*y;
			}
		}
		sigmaRms = Math.sqrt(sigmaRms/weight);
		res_arr[0] = centerRms;
		res_arr[1] = sigmaRms;	
		return res_arr;
	}
	
	private double [] guessParams(BasicGraphData gD){
		if(gD.getNumbOfPoints() < 4) return null;
		double x_max = - Double.MAX_VALUE;
		double y_max = - Double.MAX_VALUE;
		double y_min = + Double.MAX_VALUE;
		for(int ix = 0; ix <  gD.getNumbOfPoints() ; ix++){
					double x = gD.getX(ix);
					double y = gD.getY(ix);
					if(y_max < y){
						y_max = y;
						x_max = x;
					}
					if(y_min > y) { 
						y_min = y;
					}
		}
		//System.out.println("debug x_max="+x_max+" y_max="+y_max+" y_min="+y_min);
		double y_level = y_min + (y_max - y_min)*0.7;
		double x_lower = gD.getX(0);
		double x_upper = gD.getX(gD.getNumbOfPoints() -1);
		for(int ix = 0; ix <  (gD.getNumbOfPoints() - 1) ; ix++){
			double x0 = gD.getX(ix);
			double x1 = gD.getX(ix+1);
			double y0 = gD.getY(ix);
			double y1 = gD.getY(ix+1);
			if( (y_level -  y0)*(y_level -  y1) <= 0.){
				if( (y_level -  y0) >= 0.){
					double x = x0 - y0*(x1-x0)/(y1-y0);
					if((x - x_max) < 0. && Math.abs(x_lower - x_max) > Math.abs(x - x_max)){
						x_lower = x;
					}
				}
				if( (y_level -  y0) <= 0.){
					double x = x0 - y0*(x1-x0)/(y1-y0);
					if((x - x_max) > 0. && Math.abs(x_upper - x_max) > Math.abs(x - x_max)){
						x_upper = x;
					}	
				}
			}
		}		
		//System.out.println("debug x_lower="+x_lower+" x_upper="+x_upper);
		double base = 0.;
		double center = (x_upper + x_lower)/2.0;			
		double sigma = (x_upper - x_lower)/2.0;
		double amp = (y_max - y_min);
		if(sigma < 0.) return null;
		double [] res_arr = new double[4];
		res_arr[0] = base;
		res_arr[1] = center;
		res_arr[2] = sigma;
		res_arr[3] = amp;
		//System.out.println("debug base="+base+" center="+center+" sigma="+sigma+" amp="+amp);
		return res_arr;
	}
	
	
	/** Sets the number of iterations diring the fitting */ 
	public void setIterations(int nIterations){
		this.nIterations = nIterations;
	}
	
	/** Sets the number of graph points in the fitting curve */
	public void setGraphPoints(int nGraphPoints){
		this.nGraphPoints = nGraphPoints;
	}	
	
	/** Sets the width coefficient for fitting. The fit will use  
	    - wCoeff*sigma : + wCoeff*sigma region around pick for fitting 
	*/
	public void setWidthCoeff(double wCoeff){
		this.wCoeff = wCoeff;
	}	
	
	/** Returns the number of iterations diring the fitting */ 
	public void getIterations(int nIterations){
		this.nIterations = nIterations;
	}
	
	/** Returns the number of graph points in the fitting curve */
	public void getGraphPoints(int nGraphPoints){
		this.nGraphPoints = nGraphPoints;
	}	
	
	/** Returns the width coefficient for fitting. The fit will use  
	    - wCoeff*sigma : + wCoeff*sigma region around pick for fitting 
	*/
	public double getWidthCoeff(){
		return wCoeff;
	}	
	
	
}
