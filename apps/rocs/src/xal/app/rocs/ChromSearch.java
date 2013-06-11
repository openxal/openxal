package xal.app.rocs;

import java.math.*;
import java.util.*;
import java.io.*;

public class ChromSearch extends ChromSettings {
    
    //Class for searching the data that has populated the array
    //in class TuneSettings
   
    //variable declarations
    public double key_x = 0, key_y = 0;
    public int index = 0, jdex = 0;
    public int foundxi = 0;
    public int foundyj = 0;
    public int position[] = new int[2];

    public int[] searchData(double key_X, double key_Y) 
	throws Exception {

	foundxi = 0;
	foundyj = 0;
	index = 0;
	jdex = 0;

	//Read in the data using the class TuneSettings
	readData();
	
	//initialize new variables
	key_x = key_X;
	key_y = key_Y;

	if(key_x == gridmax){
	    foundxi = (imax);
	    position[0]=foundxi;
	}
	else
	//loops to find the i and j points
	for(index = 0; index < (imax); index++ ){
	    if(key_x >= chrom_x[index][jdex] & 
	       key_x < chrom_x[index+1][jdex]){
		foundxi = index;   //found i index
		position[0]=foundxi;
	    }
	}
	if(key_y == gridmax){
	    foundyj = (jmax);
	    position[1]=foundyj;
	}
	else
	    for(jdex = 0; jdex < (jmax); jdex++ ){
		if(key_y>= chrom_y[foundxi][jdex] & 
		   key_y < chrom_y[foundxi][jdex+1]){
		    foundyj = jdex;  //found j index
		    position[1]=foundyj;
		}
	    }
	return position;
    }
    public int getXi(){
	return foundxi;
    }
    public int getYj(){
	return foundyj;
    }
}
