package xal.extension.widgets.plot;

import java.awt.*;

/**
 * This class keeps incremental colors. 
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public class IncrementalColors{

    static private int number = 10;
    static private Color [] colors = null;

    static {
	colors = new Color[number];
	colors[0]  = new Color(  0,  0,255);
	colors[1]  = new Color(  0, 88,255);
	colors[2]  = new Color(121,  0,255);
	colors[3]  = new Color(255,  0,243);
	colors[4]  = new Color(255,  0, 79);
	colors[5]  = new Color(255,100,  0);
	colors[6]  = new Color( 11,172,  9);
	colors[7]  = new Color(246,180, 13);
	colors[8]  = new Color(160, 32,240);
	colors[9]  = new Color(  0,255,  0);
    }

    /* Constructor. */    
    private IncrementalColors(){
    }

    /* Returns color according to an integer index. */  
    static public Color getColor(int indIn){
	int ind = indIn % number;
	return colors[ind];
    }
    
    /** set the color of index i to your favorite color */
    
    static public void setColor(int index, Color color) {
	    colors[index] = color;
    }
}


