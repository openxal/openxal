/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xal.app.lossviewer.waterfall;

/**
 *
 * @author az9
 */
public interface WaterfallData {

    public DataSlice getZBand(int j);

    
    int getSizeX();
    int getSizeY();
    double getZByIndex(int x, int y);
    void addData(DataSlice entry);
    void addDataListener(WaterfallDataListener dl);
    void removeDataListener(WaterfallDataListener dl);
    String[] getXNames();
    void setXName(int i, String name);
    String getXName(int i);
    String toString();
}
