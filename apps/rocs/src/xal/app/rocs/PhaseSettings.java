/************************************************************************/
//
// class PhaseSettings_Achr:
// This class reads the arc phase and associated 6 families of quad
// strengths.  The quad strengths should preserve the achromat.
//
/************************************************************************/

package xal.app.rocs;

import xal.extension.application.Application;

import java.math.*;
import java.util.*;
import java.io.*;
import java.net.URL;

public class PhaseSettings{
    
    private static int max = 100;       //max parameters for the array
    private double temp_new, temp_prev; //temporary variables
    private String s;                   //string used with tokenizer
    private int i = 0, j = 0;           //loop counters

    public double phase_x[][] = new double[max][max]; 
    public double phase_y[][] = new double[max][max];
    public double kd[][] = new double[max][max];
    public double kfs[][] = new double[max][max];
    public double kfl[][] = new double[max][max];
    public double kdee[][] = new double[max][max];
    public double kdc[][] = new double[max][max];
    public double kfc[][] = new double[max][max];
    public int imax = 0, jmax = 0;
    public double xgridmin = 0, xgridmax = 0;
    public double ygridmin = 0, ygridmax = 0;
    public double junk;

    //public int test = 0;

    //class member functions
    public void readData(int choice) throws IOException { 


	for(i=0; i<max; i++)
	    for(j=0; j<max; j++){
		phase_x[i][j] = 0.0;
		phase_y[i][j] = 0.0;
	    }
	URL url;
	InputStream is;
	InputStreamReader isr;

	//Begin reading in data and 
	if(choice == 0){
	    url = Application.getAdaptor().getResourceURL( "Phase_623_620_Achromat.dat" );
	    is = url.openStream();
	    isr = new InputStreamReader(is);
	}
	else{
	    url = Application.getAdaptor().getResourceURL( "Phase_623_620_NoAchromat.dat" );
	    is = url.openStream();
	    isr = new InputStreamReader(is);
	}

	BufferedReader br = new BufferedReader(isr); 
        
        s=br.readLine();  //Read in first line
       
	StringTokenizer st = new StringTokenizer(s); //for parsing
	
        //read in first line of data
	//initialize variables for instance [0][0]
	i = 0; 
	j = 0;
	
	phase_x[i][j] = 4*Double.parseDouble(st.nextToken());
	phase_y[i][j] = 4*Double.parseDouble(st.nextToken());

	xgridmin = phase_x[i][j];   //keep track of smallest number
	ygridmin = phase_y[i][j];

        temp_prev=phase_x[i][j];
	
	kd[i][j] =  Double.parseDouble(st.nextToken());
	kfs[i][j] = Double.parseDouble(st.nextToken());
	kfl[i][j] = Double.parseDouble(st.nextToken());
	kdee[i][j] = Double.parseDouble(st.nextToken());
	kdc[i][j] = Double.parseDouble(st.nextToken());
	kfc[i][j] = Double.parseDouble(st.nextToken());	
	
	j++;
	//read in rest of data to the arrays
	int k=0;
	while((s=br.readLine()) != null){
 
		st = new StringTokenizer(s);

	       	temp_new=4*Double.parseDouble(st.nextToken());

		if(temp_new!=temp_prev){ //increment i
		    temp_prev = temp_new;
		    i++;
		    j = 0;           
		}

		phase_x[i][j] = temp_new;
		phase_y[i][j] = 4*Double.parseDouble(st.nextToken());
		
		kd[i][j] =  Double.parseDouble(st.nextToken());
		kfs[i][j] = Double.parseDouble(st.nextToken());
		kfl[i][j] = Double.parseDouble(st.nextToken());
		kdee[i][j] = Double.parseDouble(st.nextToken());
		kdc[i][j] = Double.parseDouble(st.nextToken());
		kfc[i][j] = Double.parseDouble(st.nextToken());	
		imax = i;
		jmax = j;
		j++;

		k++;
	}
	xgridmax = phase_x[i][j-1];
	ygridmax = phase_y[i][j-1];   //keep track of the grids largest #
	
    }//End reading in data.

    //other member functions
    //used to access the min and max values for the data arrays
    //and for the indices
    public int getImax(){
	return imax;
    }
    public int getJmax(){
	return jmax;
    }
    public double getMinx(){
	return xgridmin;
    }
    public double getMaxx(){
	return xgridmax;
    }
    public double getMiny(){
	return ygridmin;
    }
    public double getMaxy(){
	return ygridmax;
    }
    
}
