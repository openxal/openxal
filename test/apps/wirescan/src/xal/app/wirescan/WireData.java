/*
 * WireData.java
 */

package xal.app.wirescan;


/**
 * This class stores data from the wirescanner to be used elsewhere.
 *
 * @author	S. Bunch
 * @version	1.0
 */
public class WireData {
	/** Constant for the maximum number of points. Really should use dynamic arrays instead of fixed sized arrays. */
	static private final int MAX_POINTS = 512;
	
	// **************** Warning ***************** 
	// Regardless of what the array allocations are, several of these arrays are assigned directly from the channel arrays so the array size is the size of those channel arrays.
	// It isn't clear why we need to assign array allocations here.
	
	/** Raw X Values */
	protected double [] vvalues;
	/** Raw Y Values */
	protected double [] dvalues;
	/** Raw Z Values */
	protected double [] hvalues;
	/** Raw X Values Sampled*/
	protected double [] vvaluesS = new double[MAX_POINTS];
	/** Raw Y Values Sampled*/
	protected double [] dvaluesS = new double[MAX_POINTS];
	/** Raw Z Values Sampled*/
	protected double [] hvaluesS = new double[MAX_POINTS];
       	/** Position Values */
	protected double [] position = new double[MAX_POINTS];
	/** Fitted X Values */
	protected double [] vfit = new double[MAX_POINTS];
	/** Fitted Y Values */
	protected double [] dfit = new double[MAX_POINTS];
	/** Fitted Z Values */
	protected double [] hfit = new double[MAX_POINTS];
        
	protected double[][] pos = new double[6][MAX_POINTS];
	
	private final WirePanel myWirePanel;
        
	/** RMS X Sigma */
	protected double xsigmam;
	/** RMS Y Sigma */
	protected double ysigmam;
	/** RMS Z Sigma */
	protected double zsigmam;
	/** Fitted X Sigma */
	protected double xsigmaf;
	/** Fitted Y Sigma */
	protected double ysigmaf;
	/** Fitted Z Sigma */
	protected double zsigmaf;
	/** RMS X Area */
	protected double xaream;
	/** RMS Y Area */
	protected double yaream;
	/** RMS Z Area */
	protected double zaream;
	/** Fitted X Area */
	protected double xareaf;
	/** Fitted Y Area */
	protected double yareaf;
	/** Fitted Z Area */
	protected double zareaf;
	/** RMS X Amplitude */
	protected double xamplm;
	/** RMS Y Amplitude */
	protected double yamplm;
	/** RMS Z Amplitude */
	protected double zamplm;
	/** Fitted X Amplitude */
	protected double xamplf;
	/** Fitted Y Amplitude */
	protected double yamplf;
	/** Fitted X Amplitude */
	protected double zamplf;
	/** RMS X Mean*/
	protected double xmeanm;
	/** RMS Y Mean*/
	protected double ymeanm;
	/** RMS Z Mean*/
	protected double zmeanm;
	/** Fitted X Mean*/
	protected double xmeanf;
	/** Fitted Y Mean*/
	protected double ymeanf;
	/** Fitted Z Mean*/
	protected double zmeanf;
	/** RMS X Offset*/
	protected double xoffsetm;
	/** RMS Y Offset*/
	protected double yoffsetm;
	/** RMS Z Offset*/
	protected double zoffsetm;
	/** Fitted X Offset*/
	protected double xoffsetf;
	/** Fitted Y Offset*/
	protected double yoffsetf;
	/** Fitted Z Offset*/
	protected double zoffsetf;
	/** RMS X Slope */
	protected double xslopem;
	/** RMS Y Slope */
	protected double yslopem;
	/** RMS Z Slope */
	protected double zslopem;
	/** Fitted X Slope */
	protected double xslopef;
	/** Fitted Y Slope */
	protected double yslopef;
	/** Fitted Z Slope */
	protected double zslopef;
	/** Number of steps taken during scan */
	protected int nsteps;
	
        String name;
	
	private int vCounter = 0;
	private int dCounter = 0;
	private int hCounter = 0;

	/** The constructor to initialize WireData*/
	public WireData(WirePanel wp) {
            myWirePanel = wp;
		vvalues = new double[0];
		dvalues = new double[0];
		hvalues = new double[0];
		
		resetData();
                
		nsteps = 0;
		xsigmaf = 0.0;
		ysigmaf = 0.0;
		zsigmaf = 0.0;
		xsigmam = 0.0;
		ysigmam = 0.0;
		zsigmam = 0.0;
		xareaf = 0.0;
		yareaf = 0.0;
		zareaf = 0.0;
		xaream = 0.0;
		yaream = 0.0;
		zaream = 0.0;
		xamplf = 0.0;
		yamplf = 0.0;
		zamplf = 0.0;
		xamplm = 0.0;
		yamplm = 0.0;
		zamplm = 0.0;
		xmeanf = 0.0;
		ymeanf = 0.0;
		zmeanf = 0.0;
		xmeanm = 0.0;
		ymeanm = 0.0;
		zmeanm = 0.0;
		xoffsetf = 0.0;
		yoffsetf = 0.0;
		zoffsetf = 0.0;
		xoffsetm = 0.0;
		yoffsetm = 0.0;
		zoffsetm = 0.0;
		xslopef = 0.0;
		yslopef = 0.0;
		zslopef = 0.0;
		xslopem = 0.0;
		yslopem = 0.0;
		zslopem = 0.0;
	}

	/**
	 * Returns the number of series.
	 * Part of the ChartDataModel interface
	 * @return			Number of series to be plotted
	 * @see WireDataTableModel
	 */
	public int getNumSeries() {
		return 6;
	}

	/**
	 * Returns the  y data points for a series.
	 * Part of the ChartDataModel interface
	 * @param series		The series to plot
	 * @return			Depending on series, returns the double array to plot.  Possibilities are:
	 				<ul>
		<li>vvalues
		<li>dvalues
		<li>hvalues
		<li>Xfit data
		<li>Yfit data
		<li>Zfit data
	</ul>
	 * @see WireDataTableModel
	 */
	public double[] getYSeries(int series) {
            switch(series) {
		case 0:
			return vvaluesS;
		case 1:
			return dvaluesS;
		case 2:
			return hvaluesS;
		case 3:
			return vfit;
		case 4:
			return dfit;
		case 5:
			return hfit;
		default :
			return vvalues;
            }                
	}
                
 	/**
	 * Returns the  x data points for a series.
	 * Part of the ChartDataModel interface 
	 * @see WireDataTableModel
	 * @return	Wirescanner position double array
	 */
	public double[] getXSeries(int index) {
            switch(index) {
                case 0:
                    return pos[0];
                case 1:
                    return pos[1];
                case 2:
                    return pos[2];
                case 3:
                    return pos[3];
                case 4:
                    return pos[4];
                case 5:
                    return pos[5];
               default:
                    return pos[0];
            }
	}
                
        public void setDataSourceName(String name) {
            this.name = name;
        }
        
        public String getDataSourceName() {
            return name;
        }
        
        public String[] getPointLabels() {
            return null;
        }
        
        public String[] getSeriesLabels() {
            String[] labels = {"Vert.", "Diag.", "Hori.", "VFIT", "DFIT", "HFIT"};
            return labels;
        }
        
        public int addPoint(int counter, double xData, double yData, int dataInd) {
	    int defCounter = counter;
	    switch(dataInd) {
                case 0: 
		    if (vCounter == counter) {
                    	vvaluesS[counter] = yData;
                    	pos[0][counter] = xData;
		    	vCounter = vCounter + 1;
		    } else {
			defCounter = vCounter;
		    }
                    break;
		
                case 1: 
		    if (dCounter == counter) {
                    	dvaluesS[counter] = yData;
                    	pos[1][counter] = xData;
		    	dCounter = dCounter + 1;
		    } else {
			defCounter = dCounter;
		    }
                    break;
		    
                case 2: 
		    if (hCounter == counter) {
                    	hvaluesS[counter] = yData;
                    	pos[2][counter] = xData;
		    	hCounter = hCounter + 1;
		    } else {
			defCounter = hCounter;
		    }

                    break;
		    
            }
	    return defCounter;           
        }
	
	public void resetData() {
		
		for (int i=0; i<6; i++) {
			pos[i] = new double[MAX_POINTS];
			for (int j=0 ; j< MAX_POINTS ; j++) {
				pos[i][j] = j/1.;
			}
		}
		
		vvaluesS = new double[MAX_POINTS];
		dvaluesS = new double[MAX_POINTS];
		hvaluesS = new double[MAX_POINTS];
		vfit = new double[MAX_POINTS];
		dfit = new double[MAX_POINTS];
		hfit = new double[MAX_POINTS];
		for (int i=0; i<MAX_POINTS; i++) {
			vvaluesS[i] = Double.MAX_VALUE;
			dvaluesS[i] = Double.MAX_VALUE;
			hvaluesS[i] = Double.MAX_VALUE;
			vfit[i] = Double.MAX_VALUE;
			dfit[i] = Double.MAX_VALUE;
			hfit[i] = Double.MAX_VALUE;
		}
		
	}
	
}
