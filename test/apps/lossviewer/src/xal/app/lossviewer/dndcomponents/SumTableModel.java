/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lossviewer.dndcomponents;

import xal.app.lossviewer.LossDetector;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author azukov
 */
public class SumTableModel extends DetectorTableModel {
    private static final long serialVersionUID = -1978854954476899411L;
    
    private Map<String,Double> weights = new HashMap<String,Double>();
    private Map<String,LossDetector> detectorMap = new HashMap<String,LossDetector>();
//    public static final Double DEFAULT_WEIGHT = 1.0;

    public SumTableModel(List<String> visibleSignalNames,
            Map<String, Set<String>> allLabelNormalizations,
            Map<String, NumberFormat> allFormats,
            Map<String, String> allLabelNames) {

        super(visibleSignalNames, allLabelNormalizations, allFormats, allLabelNames);
    }

    public void init(Set<LossDetector> s) {
        if (s != null) {
            detectorArray = s.toArray(new LossDetector[]{});
            Map<String,Double> newWeights = new HashMap<String,Double>();
            for(LossDetector ls : s){
                String name = ls.getName();
                detectorMap.put(name,ls);
                if(weights.containsKey(name)){
                    newWeights.put(name,weights.get(name));
                } 
                
            }
            weights.clear();
            weights = newWeights;
        }

    }

    public Object getValueAt(int row, int column) {

        if (detectorArray == null || detectorArray.length == 0) {
            return null;
        }
        String sn = visibleSignalNames.get(column);

        if (sn.equals("weight")) {
            LossDetector ld = detectorArray[row];
            Double w = weights.get(ld.getName());
            if(w==null)
                w=ld.getDefaultWeight();
            return w;
        } else {
            return super.getValueAt(row, column);            

        }

    }
    public void setValueAt(Object aValue, int row, int column){
        String sn = visibleSignalNames.get(column);

        if (sn.equals("weight")) {
            LossDetector ld = detectorArray[row];
            weights.put(ld.getName(), Double.parseDouble(aValue.toString()));
        }
        
    }
    

    public boolean isCellEditable(int row, int column) {
        return getColumnName(column).equals("Weight");
    }
    
    public Map<String,Double> getWeights(){
        return weights;
    }
    
    public Double getWeight(String name){
        Double w = weights.get(name);
        if(w==null)
            w = detectorMap.get(name).getDefaultWeight();
        return w;
    }
    
}
