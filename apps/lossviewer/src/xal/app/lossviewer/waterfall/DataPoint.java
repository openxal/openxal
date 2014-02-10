/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xal.app.lossviewer.waterfall;

import java.awt.Color;
import java.util.Date;

/**
 *
 * @author az9
 */
public class DataPoint {
    public String name;
    public double value;
    public Date date = null;
    public Color color;
    public DataPoint(String name, double value, long tst, Color color){
        this.name=name;
        this.value=value;
        if(tst!=0){
            this.date = new Date(tst);
        }
        this.color = color;

    }
    public String toString(){
       return name+" "+date+" "+value + " "+color;
    }
}
