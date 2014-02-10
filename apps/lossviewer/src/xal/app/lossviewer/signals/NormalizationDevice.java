/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package xal.app.lossviewer.signals;

/**
 *
 * @author az9
 */
public interface NormalizationDevice extends Comparable<NormalizationDevice>{
    String getName();
    String getChargePV();
    int getID();
    double getScale();
    
}
