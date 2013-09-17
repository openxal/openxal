/************************************************************************
//
// class TuneSettings:
// This class reads in Data from a data file and stored that data in
// 8 seperate arrays. The data file must have 8 columns. There
// is no set limit for the length of the file.
// The length of the file will be determined while reading in the data
// as well as the max and min values that are found in the file and the
// highest possible indicies that can be accessed.
// There are also addition methods for accessing the aforementioned values.
//
/************************************************************************/

package xal.app.rocs;
import java.math.*;
import java.util.*;
import java.io.*;
import java.net.URL;

public class TuneSettings{
    
    //Class for reading in data from file "TuneGridMaster.dat"
    private static int max = 100;       //max parameters for the array
    private double temp_new, temp_prev; //temporary variables
    private String s;                   //string used with tokenizer
    private int i = 0, j = 0;           //loop counters

    public double tune_x[][] = new double[max][max]; 
    public double tune_y[][] = new double[max][max];
    public double kd[][] = new double[max][max];
    public double kfs[][] = new double[max][max];
    public double kfl[][] = new double[max][max];
    public double kdee[][] = new double[max][max];
    public double kdc[][] = new double[max][max];
    public double kfc[][] = new double[max][max];
    public int imax = 0, jmax = 0;
    public double gridmin = 0, gridmax = 0;

    //class member functions
    public void readData() throws IOException { 
	i = 0; 
	j = 0;
	
	//Begin reading in data and       
	URL url = getClass().getResource("resources/tuneGridMaster.dat");
	InputStream is = url.openStream();
	InputStreamReader isr = new InputStreamReader(is);
	BufferedReader br = new BufferedReader(isr);
        
        s=br.readLine();  //Read in first line
       
	StringTokenizer st = new StringTokenizer(s); //for parsing
	
        //read in first line of data
	//initialize variables for instance [0][0]
	tune_x[i][j] = Double.parseDouble(st.nextToken());
	gridmin = tune_x[i][j];   //keep track of smallest number
        temp_prev=tune_x[i][j];
	
	tune_y[i][j] = Double.parseDouble(st.nextToken());
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

	       	temp_new=Double.parseDouble(st.nextToken());

		if(temp_new!=temp_prev){ //increment i
		    temp_prev = temp_new;
		    i++;
		    j = 0;           
		}

		tune_x[i][j] = temp_new;
		tune_y[i][j] = Double.parseDouble(st.nextToken());
		kd[i][j] =  Double.parseDouble(st.nextToken());
		kfs[i][j] = Double.parseDouble(st.nextToken());
		kfl[i][j] = Double.parseDouble(st.nextToken());
		kdee[i][j] = Double.parseDouble(st.nextToken());
		kdc[i][j] = Double.parseDouble(st.nextToken());
		kfc[i][j] = Double.parseDouble(st.nextToken());	
		imax = i; //imax = 17 with current data grid
		jmax = j; //jmax = 16 with current data grid
		j++;

		k++;
	}
	gridmax = tune_x[i][j-1];   //keep track of the grids largest #
	
    }//End reading in data.

    //other member functions
    //used to access the min and max values for the data arrays
    // and for the indecies
    public int getImax(){
	return imax;
    }
    public int getJmax(){
	return jmax;
    }
    public double getMin(){
	return gridmin;
    }
    public double getMax(){
	return gridmax;
    }
}
