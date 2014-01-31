/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lossviewer.dndcomponents;

import xal.app.lossviewer.views.LossView;

import java.util.Map;
import xal.tools.data.DataAdaptor;

/**
 *
 * @author azukov
 */
public class SumTable extends LossTable {
    private static final long serialVersionUID = 5568996378691513488L;

    private String dataLabel = "SumTable";

    public String dataLabel() {

        return dataLabel;
    }

    public SumTable(LossView v) {
        this(v, null);
    }

    public SumTable(LossView v, String label) {
        super(v, label);
    }

    protected void initializeModleSelectionAndMenu() {
        //ugly!!!
        model = new SumTableModel(visibleSignalNames, allLabelNormalizations, allFormats, allLabelNames);

        setModel(model);
        initilizeSelectionModel();
        initializeMenu();
    }

    protected void initilizeColumnNames() {
        super.initilizeColumnNames();
        allLabelNames.put("weight", "Weight");
    }

    @Override
    protected void initializeMenu() {

        super.initializeMenu();
        visibleSignalNames.add("weight");
    }
    
    @Override
    public void write(DataAdaptor da){
        super.write(da);
        Map<String,Double> weights = ((SumTableModel)model).getWeights();
        DataAdaptor wda = da.createChild("Weights");
        
        for(String key : weights.keySet()){
            DataAdaptor w = wda.createChild("Weight");
            w.setValue("name", key);
            w.setValue("weight",weights.get(key));
        }
        
    }
    

    @Override
    public void update(DataAdaptor da){
        super.update(da);
        Map<String,Double> weights = ((SumTableModel)model).getWeights();
        
        DataAdaptor wda = da.childAdaptor("Weights");
        for(DataAdaptor w : wda.childAdaptors("Weight")){
            String key = w.stringValue("name");
            double weight = w.doubleValue("weight");
            weights.put(key, weight);
        }
        
    }
    public double getWeight(String name){
       return ((SumTableModel)model).getWeight(name);
        
    }
}
