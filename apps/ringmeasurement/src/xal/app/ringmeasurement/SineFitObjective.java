/*
 * SineFitObjective.java
 *
 * Created on February 17, 2005, 2:25 PM
 */

package xal.app.ringmeasurement;

import java.util.*;

import xal.extension.solver.*;

/**
 *
 * @author  Paul Chu
 */
public class SineFitObjective extends Objective {
    
    final double _target;
    final double[] _data;
    long dataLength = 200;
    
    /** Creates a new instance of SineFitObjective */
    public SineFitObjective(String name, final double[] dataArray, final double target) {
        super(name);
        _data = dataArray;
        _target = target;
        dataLength = _data.length;
    }
    
    protected void setDataLength(long len) {
    	dataLength = len;
    }
    
    public double satisfaction(double value) {
        return Math.exp( -Math.abs(_target - value));
    }
    
    public double score(Map<String, Double> inputs) {
        double score = 0.;
        Iterator<String> keyIter = inputs.keySet().iterator();
        
        double A = 0.;
        double w = 0.;
        double b = 0.;
        double d = 0.;
        double c = 0.;
        
        while ( keyIter.hasNext() ) {
            final String key = keyIter.next();
            final double value = (inputs.get( key ) ).doubleValue();
            
            if (key.equals("A"))
                A = (inputs.get( key ) ).doubleValue();
            else if (key.equals("b"))
                b = ( inputs.get( key ) ).doubleValue();
            else if (key.equals("w"))
                w = (inputs.get( key ) ).doubleValue();
            else if (key.equals("c"))
                c = (inputs.get( key ) ).doubleValue();
          else if (key.equals("d"))
          d = (inputs.get( key ) ).doubleValue();
        }
        score = 0.;
        for (int i=0; i<dataLength; i++)
            score += 
                 (((A*Math.exp(-1.*c*i)*Math.sin(2.*Math.PI*(w*i + b)) + d) - _data[i]) *
                 ((A*Math.exp(-1.*c*i)*Math.sin(2.*Math.PI*(w*i + b)) + d) - _data[i]))/(0.5*0.5);
//        	score += 
//        		(((A*Math.exp(-1.*c*i)*Math.sin(2.*Math.PI*(w*i + b)) ) - _data[i]) *
//        				((A*Math.exp(-1.*c*i)*Math.sin(2.*Math.PI*(w*i + b)) ) - _data[i]))/(0.5*0.5);
        
        return score;
    }
    
}
