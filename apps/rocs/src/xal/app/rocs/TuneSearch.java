/***************************************************************
//  
// class TuneSearch: 
// this class reads in data from a data grid file and finds the 
// closest point to the desired key being searched.
// The class will identify the point once found as well as 
// the indecies closest to the desired point.
// This is part of the ringopticspanel package
//
/**************************************************************/

package xal.app.rocs;
import java.math.*;
import java.util.*;
import java.io.*;

public class TuneSearch extends TuneSettings {
    
    //Class for searching the data that has populated the array
    //in class TuneSettings
   
    //variable declarations
    public double key_x = 0, key_y = 0;  //key to find user input points
    public int index = 0, jdex = 0;      //tells us where we are
    public int foundxi = 0,foundyj = 0;  //tells us location of the 
    public int position[] = new int[2];   //desired point

    public int[] searchData(double key_X, double key_Y) 
	throws Exception {
	
	foundxi = 0;
	foundyj = 0;
	index = 0;
	jdex = 0;

	//Read in the data using the class TuneSettings
	readData();
	
	//initialize new variables
	key_x= key_X;   //set key variables to passed in values
	key_y = key_Y;
   
	if(key_x == gridmax){
	      foundxi = (imax);
	      position[0]=foundxi;
	}
	else{
	//loops to find the i and j points
	    for(index = 0; index < (imax); index++ ){
		if(key_x >= tune_x[index][jdex] & key_x 
		   < tune_x[index+1][jdex]){
		    foundxi = index;   //found i index
		    position[0]=foundxi;
		}
	    }
	}
	if(key_y == gridmax){
	      foundyj = (jmax);
	      position[1]=foundyj;
	}
	else{
	    for(jdex = 0; jdex < (jmax); jdex++ ){
		if(key_y>= tune_y[foundxi][jdex] & key_y
		   < tune_y[foundxi][jdex+1]){
		    foundyj = jdex;  //found j index
		    position[1]=foundyj;
		}
	    }
	}
	return position;
    }
    //get methods to access the foundxi, foundyj variables
    public int getXi(){
	return foundxi;
    }
    public int getYj(){
	return foundyj;
    }
}
