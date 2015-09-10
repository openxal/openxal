package xal.extension.wirescan.apputils;


import xal.extension.widgets.plot.BasicGraphData;
import java.awt.Color;


/*
 * The  WireScanData class keeps the WS raw waveforms and related data such as
 * the parent WS file, PV Log Id, Fitting curves, logarithmic representations of
 * the waveforms.
 */
public class WireScanData{
	
	//the raw waveform for X direction 
	private BasicGraphData wfX = new BasicGraphData();
	//the raw waveform for Y direction 		
	private BasicGraphData wfY = new BasicGraphData();
	
	//Wire scanner Id
	private String wsId = "none";
	
	//PV logger Id
	private int pvlogId = -1;
	
	//name of WS file 
	private String ws_file_name = "none";
	
	//fitting parameters	
	private double sigmaX = 0.;
	private double sigmaY = 0.;
	
	private double centerX = 0.;
	private double centerY = 0.;	
	
	private double baseX = 0.;
	private double baseY = 0.;
	
	private double ampX = 0.;
	private double ampY = 0.;	
	
	private double sigmaRmsX = 0.;
	private double sigmaRmsY = 0.;
	
	private double centerRmsX = 0.;
	private double centerRmsY = 0.;	

	private double sigmaRmsErrX = 0.;
	private double sigmaRmsErrY = 0.;
	
	//the log(wf) logarithms of the raw waveform
	private BasicGraphData log_wfX = new BasicGraphData();
	private BasicGraphData log_wfY = new BasicGraphData();
	
	//fitted curve
	private BasicGraphData fit_wfX = new BasicGraphData();
	private BasicGraphData fit_wfY = new BasicGraphData();
	
	//logarithm of fitted curve
	private BasicGraphData log_fit_wfX = new BasicGraphData();
	private BasicGraphData log_fit_wfY = new BasicGraphData();
	
	/** Constructor of an empty wire scanner data object */
	public WireScanData(){		
		setLegendToGraphs();
	}
	
	private void setLegendToGraphs(){
		String str = " file=" + ws_file_name + "ws=" + wsId + " pvLog= " + pvlogId + " dir= ";
		wfX.setGraphProperty("Legend",str + "X  raw");
		wfY.setGraphProperty("Legend",str + "Y  raw");
		log_wfX.setGraphProperty("Legend",str + "X  log(raw)");
		log_wfY.setGraphProperty("Legend",str + "Y  log(raw)");
		fit_wfX.setGraphProperty("Legend",str + "X  Gauss Fit");
		fit_wfY.setGraphProperty("Legend",str + "Y  Gauss Fit");
		log_fit_wfX.setGraphProperty("Legend",str + "X  log(Gauss Fit)");
		log_fit_wfY.setGraphProperty("Legend",str + "Y  log(Gauss Fit)");
		
		wfX.setGraphColor(Color.black);
		wfY.setGraphColor(Color.black);
		log_wfX.setGraphColor(Color.black);
		log_wfY.setGraphColor(Color.black);
		fit_wfX.setGraphColor(Color.red);
		fit_wfY.setGraphColor(Color.red);
		log_fit_wfX.setGraphColor(Color.red);
		log_fit_wfY.setGraphColor(Color.red);
		
		
		wfX.setDrawLinesOn(false);
		wfY.setDrawLinesOn(false);
		log_wfX.setDrawLinesOn(false);
		log_wfY.setDrawLinesOn(false);
		fit_wfX.setDrawPointsOn(false);
		fit_wfY.setDrawPointsOn(false);
		log_fit_wfX.setDrawPointsOn(false);
		log_fit_wfY.setDrawPointsOn(false);	
		
		wfX.setGraphPointSize(4);
		wfY.setGraphPointSize(4);
		log_wfX.setGraphPointSize(4);
		log_wfY.setGraphPointSize(4);
		fit_wfX.setLineThick(2);
		fit_wfY.setLineThick(2);
		log_fit_wfX.setLineThick(2);
		log_fit_wfY.setLineThick(2);	
	}
	
	/** Returns the Id of the Wire Scanner */
	public String getId(){
		return wsId;
	}
	
	/** Sets the Id of the Wire Scanner */
	public void setId(String wsId){
		this.wsId = wsId;
		setLegendToGraphs();
	}
	
	/** Returns the PV Logger Id of the scan */
	public int getPVLogId(){
		return pvlogId;
	}
	
	/** Sets the PV Logger Id of the scan */
	public void setPVLogId(int pvlogId){
		this.pvlogId = pvlogId;
		setLegendToGraphs();		
	}
	
	/** Returns the name of WS data file */	
	public String getWSFileName(){
		return ws_file_name;
	}
	
	/** Sets the name of WS data file */	
	public void setWSFileName(String ws_file_name){
		this.ws_file_name = ws_file_name;
		setLegendToGraphs();		
	}
	
	/** Returns a reference to the BasicGraphData instance with the raw waveform for X-direction */		
	public BasicGraphData getRawWFX(){
		return wfX;
	}
	
	/** Returns a reference to the BasicGraphData instance with the raw waveform for Y-direction */		
	public BasicGraphData getRawWFY(){
		return wfY;
	}	
	
	/** Returns a reference to the BasicGraphData instance with the log of raw waveform for X-direction */		
	public BasicGraphData getLogRawWFX(){
		return log_wfX;
	}
	
	/** Returns a reference to the BasicGraphData instance with the log of raw waveform for Y-direction */		
	public BasicGraphData getLogRawWFY(){
		return log_wfY;
	}	

	/** Returns a reference to the BasicGraphData instance with the fitting waveform for X-direction */	
	public BasicGraphData getFitWFX(){
		return fit_wfX;
	}
	
	/** Returns a reference to the BasicGraphData instance with the fitting waveform for Y-direction */		
	public BasicGraphData getFitWFY(){
		return fit_wfY;
	}	
	
	/** Returns a reference to the BasicGraphData instance with the log of fitting waveform for X-direction */	
	public BasicGraphData getLogFitWFX(){
		return log_fit_wfX;
	}
	
	/** Returns a reference to the BasicGraphData instance with the log of fitting waveform for Y-direction */		
	public BasicGraphData getLogFitWFY(){
		return log_fit_wfY;
	}	
	
	
	/** Returns the sigma parameter of the Gaussian fit for X-direction */
	public double getSigmaX(){
		return sigmaX;
	}
	
	/** Returns the sigma parameter of the Gaussian fit for Y-direction */	
	public double getSigmaY(){
		return sigmaY;
	}
	
	/** Returns the base parameter of the Gaussian fit for X-direction */
	public double getBaseX(){
		return baseX;
	}
	
	/** Returns the base parameter of the Gaussian fit for Y-direction */	
	public double getBaseY(){
		return baseY;
	}

	/** Returns the amp parameter of the Gaussian fit for X-direction */
	public double getAmpX(){
		return ampX;
	}
	
	/** Returns the amp parameter of the Gaussian fit for Y-direction */	
	public double getAmpY(){
		return ampY;
	}	
	
	/** Returns the position of the center  parameter of the Gaussian fit for X-direction */		
	public double getCenterX(){
		return centerX;
	}
	
	/** Returns the position of the center  parameter of the Gaussian fit for Y-direction */		
	public double getCenterY(){
		return centerY;
	}
	
	/** Sets the sigma parameter of the Gaussian fit for X-direction */
	public void setSigmaX(double sigmaX){
		this.sigmaX = sigmaX;
	}
	
	/** Sets the sigma parameter of the Gaussian fit for Y-direction */	
	public void setSigmaY(double sigmaY){
		this.sigmaY = sigmaY;
	}
		
	/** Sets the base parameter of the Gaussian fit for X-direction */
	public void setBaseX(double baseX){
		this.baseX = baseX;
	}
	
	/** Sets the base parameter of the Gaussian fit for Y-direction */	
	public void setBaseY(double baseY){
		this.baseY = baseY;
	}

	/** Sets the amp parameter of the Gaussian fit for X-direction */
	public void setAmpX(double ampX){
		this.ampX = ampX;
	}
	
	/** Sets the amp parameter of the Gaussian fit for Y-direction */	
	public void setAmpY(double ampY){
		this.ampY = ampY;
	}	
		
	/** Sets the position of the center  parameter of the Gaussian fit for Y-direction */	
	public void setCenterX(double centerX){
		this.centerX = centerX;
	}
	
	/** Sets the position of the center  parameter of the Gaussian fit for Y-direction */	
	public void setCenterY(double centerY){
		this.centerY = centerY;
	}
	
	/** Returns the rms sigma parameter for X-direction */
	public double getSigmaRmsX(){
		return sigmaRmsX;
	}
	
	/** Returns the rms sigma parameter for X-direction */	
	public double getSigmaRmsY(){
		return sigmaRmsY;
	}	
	
	/** Sets the sigma rms parameter for X-direction */
	public void setSigmaRmsX(double sigmaRmsX){
		this.sigmaRmsX = sigmaRmsX;
	}
	
	/** Sets the sigma rms parameter for Y-direction */	
	public void setSigmaRmsY(double sigmaRmsY){
		this.sigmaRmsY = sigmaRmsY;
	}	
		
	/** Returns the rms center parameter for X-direction */
	public double getCenterRmsX(){
		return centerRmsX;
	}
	
	/** Returns the rms center parameter for X-direction */	
	public double getCenterRmsY(){
		return centerRmsY;
	}	
	
	/** Sets the center rms parameter for X-direction */
	public void setCenterRmsX(double centerRmsX){
		this.centerRmsX = centerRmsX;
	}
	
	/** Sets the center rms parameter for Y-direction */	
	public void setCenterRmsY(double centerRmsY){
		this.centerRmsY = centerRmsY;
	}		
	
}

