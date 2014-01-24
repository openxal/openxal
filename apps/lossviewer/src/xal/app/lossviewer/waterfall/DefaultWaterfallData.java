/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xal.app.lossviewer.waterfall;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author az9
 */
public class DefaultWaterfallData implements WaterfallData {
    private int nX;
    private int nY;
    private List<DataSlice> data = null;
    private List<WaterfallDataListener> listeners;
    private String[] names;


    public DefaultWaterfallData(int nx, int ny){
        this.nX=nx;
        this.nY=ny;
        data = new ArrayList<DataSlice>(ny);
        listeners = new ArrayList<WaterfallDataListener>();
        double[] empty = new double[nx];
        names = new String[nx];
        for(int i =0; i<nx;i++){
            names[i]="Data "+i;
        }
        for(int i=0;i<ny;i++){
            data.add(new DataSlice(0,empty));
        }

    }

    public int getSizeX() {
        return nX;
    }

    public int getSizeY() {
        return nY;
    }

    public double getZByIndex(int x, int y) {
        return data.get(y).data[x];
    }

    public synchronized void  addData(DataSlice newData) {

        data.add(newData);
        data.remove(0);
        for(WaterfallDataListener dl : listeners){
            dl.dataAdded(newData);
        }
    }

    public void addDataListener(WaterfallDataListener dl) {
        listeners.add(dl);
    }

    public void removeDataListener(WaterfallDataListener dl) {
        listeners.remove(dl);
    }

    public DataSlice getZBand(int j) {
        return data.get(j);
    }

    public String[] getXNames(){
        return names;
    }

    public void setXName(int i, String name) {
        names[i]=name;
    }

    public String getXName(int i) {
        return names[i];
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<data.size();i++){
            DataSlice ds = data.get(i);
            sb.append(new Date(ds.timestamp)+" {");
            for(int j=0;j<names.length;j++){
                sb.append(names[j]+" "+ds.data[j]+",");
            }
            sb.append("}\n");
        }

        return sb.toString();
    }





}
