

package xal.app.rocs;
import java.math.*;
import java.util.*;
import java.io.*;
import java.net.URL;

public class ChromSettings{
    
    //Class for reading in data from file "Chrom_623_620.dat"
    private static int max = 100;
    private double temp_new, temp_prev;
    private String s;
    private int i = 0, j = 0; //loop counters

    public double chrom_x[][] = new double[max][max]; 
    public double chrom_y[][] = new double[max][max];
    public double kfl, kfs, kd, kdee, kdc, kfc;
    public double sext1[][] = new double[max][max];
    public double sext2[][] = new double[max][max];
    public double sext3[][] = new double[max][max];
    public double sext4[][] = new double[max][max];
    public int imax = 0, jmax = 0;
    public double gridmin = 0, gridmax = 0;

    //class member functions
    public void readData() throws IOException { 
	i=0;
	j=0;

	URL url = getClass().getResource("resources/Chrom_623_620.dat");
	InputStream is = url.openStream();
	InputStreamReader isr = new InputStreamReader(is);
	BufferedReader br = new BufferedReader(isr);
       
        s=br.readLine();  //Read in first line
	
	
	StringTokenizer st = new StringTokenizer(s); // for parsing
	
        //read in first line of data, corresponding to quad settings
	
    
	s=br.readLine();
	st = new StringTokenizer(s);
	chrom_x[i][j] = Double.parseDouble(st.nextToken());
        temp_prev= chrom_x[i][j];
        chrom_y[i][j] = Double.parseDouble(st.nextToken());
	gridmin = chrom_x[i][j];
	gridmax = chrom_x[i][j];
        sext1[i][j] = Double.parseDouble(st.nextToken());
        sext2[i][j] = Double.parseDouble(st.nextToken());
        sext3[i][j] = Double.parseDouble(st.nextToken());
        sext4[i][j] = Double.parseDouble(st.nextToken());

	//System.out.println(chrom_x[i][j] + " " + sext4[i][j]);
	j++;

	 while((s=br.readLine()) != null){
                
	     st = new StringTokenizer(s);
	     
	     temp_new=Double.parseDouble(st.nextToken());
	     
	     if(temp_new!=temp_prev){ //increment i
		 temp_prev = temp_new;
		 i++;
		 j = 0;           
	     }
	     
	     chrom_x[i][j] = temp_new;
	     chrom_y[i][j] = Double.parseDouble(st.nextToken());
	     sext1[i][j] =  Double.parseDouble(st.nextToken());
	     sext2[i][j] = Double.parseDouble(st.nextToken());
	     sext3[i][j] = Double.parseDouble(st.nextToken());
	     sext4[i][j] = Double.parseDouble(st.nextToken());
	     
	     //Test data output
	     /*
	       System.out.println(chrom_x[i][j] + "  " +
	       chrom_y[i][j] + "  " +
	       sext1[i][j] + "  " +
	       sext2[i][j] + "  " +
	       sext3[i][j] + "  " +
	       sext4[i][j]);
	     */
	     imax = i;
	     jmax = j;
	     j++;
	     if(chrom_x[i][j] <= gridmin) gridmin = chrom_x[i][j];
	     if(chrom_x[i][j] >= gridmax) gridmax = chrom_x[i][j];
	 }
	 
	 //keep track of i and j variable maximum
    }

    //other member functions
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




