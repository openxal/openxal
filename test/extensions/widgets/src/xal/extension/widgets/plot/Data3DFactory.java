package xal.extension.widgets.plot;

import java.util.*;
import java.awt.*;

/**
 * This class is a 3D data factory class for colored surface data used 
 * in the FunctionGraphsJPanel class.
 * By default it returns the SmoothData3D instance. 
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public class Data3DFactory {

    /**  The factory constructor.*/   
    private Data3DFactory(){
    }

    /**  Returns the SmoothData3D instance. */   
    static public ColorSurfaceData getDefaultData3D(int nx, int ny){
	if(nx < 3) nx = 3;
	if(ny < 3) ny = 3;
	return new SmoothData3D(nx,ny);
    }

    /**  Returns the 3d data instance defined by name.
     *   The name could be "smooth", "linear", "point like", and "black&white". */   
    static public ColorSurfaceData getData3D(int nx, int ny, String dataName){
	if(dataName.equals("smooth")){ 
	    if(nx < 3) nx = 3;
	    if(ny < 3) ny = 3;
	    return new SmoothData3D(nx,ny);
	}
	if(dataName.equals("linear")){ 
	    if(nx < 2) nx = 2;
	    if(ny < 2) ny = 2;
	    return new LinearData3D(nx,ny);
	}
	if(dataName.equals("point like")){ 
	    return new PointLike3D(nx,ny);
	}
	if(dataName.equals("black&white")){ 
	    return new BlackAndWhite3D(nx,ny);
	}
	return null;
    }

}
