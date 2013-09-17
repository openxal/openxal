//class SearchTune that requires class TuneSettings to function
//this class will search for the nearest points related to user
//given input
package xal.app.rocs;
import java.math.*;
import java.util.*;
import java.io.*;

public class SearchTune extends TuneSettings {
    
    //Class for searching the data that has populated the array
    //in class TuneSettings
   
    //variable declarations
    public double key_x = 0, key_y = 0;
    public int index = 0, jdex = 0;
    public int foundxi = 0;
    public int foundyj = 0;

    public int searchData(double key_X, double key_Y) 
	throws Exception {
	
	//Read in the data using the class TuneSettings
	readData();
	
	//initialize new variables
	key_x= key_X;
	key_y = key_Y;

	//error handling for numbers out of grid range
	// if(key_x < gridmin | key_y < gridmin){
	//  System.out.println("A number entered is below grid range");
	//  return -1;
	//	}
	//if(key_x > gridmax | key_y > gridmax){
	//   System.out.println("A number entered is above grid range");
	//   return -1;
	// }
   
	//loops to find the i and j points
	for(index = 0; index < (imax); index++ ){
	    if(key_x == gridmax)
	      foundxi = (imax);
	    if(key_x >= tune_x[index][jdex] & key_x < tune_x[index+1][jdex])
		foundxi = index;   //found i index
	}
	for(jdex = 0; jdex < (jmax); jdex++ ){
	    if(key_y == gridmax)
	      foundyj = (jmax-1);
	    if(key_y>= tune_y[foundxi][jdex] & key_y< tune_y[foundxi][jdex+1])
		foundyj = jdex;  //found j index
	}

	//Test output for data
	System.out.println("The nearest point is " +
		   tune_x[foundxi]
		   [foundyj] +" " +
		   tune_y[foundxi]
		   [foundyj]);
	return 0;
    }
    public int getXi(){
	return foundxi;
    }
    public int getYj(){
	return foundyj;
    }
}
