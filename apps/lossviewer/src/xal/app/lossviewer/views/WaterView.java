/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lossviewer.views;

import xal.app.lossviewer.dndcomponents.LossTable;
import xal.app.lossviewer.dndcomponents.SumTable;
import xal.app.lossviewer.dndcomponents.waterfall.WaterfallLossPlot;
import xal.app.lossviewer.signals.SignalEvent;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import xal.tools.data.DataAdaptor;

/**
 *
 * @author az9
 */
public class WaterView extends DefaultLossView {

//    public void switchLossSignal(String lossSig) {
//        waterPlot.switchLossSignal(lossSig);
//    }

    public String getLossSignal() {
        return waterPlot.getLossSignal();
    }

    protected void init() {
        view = new JSplitPane();
        view.setOneTouchExpandable(true);
        table = new LossTable(this);
        table.addSelectionListener(this);

        waterPlot = new WaterfallLossPlot(this);
        //         addSelectionListener(mpsPanel);


        JScrollPane scr = new JScrollPane(table);
        view.setTopComponent(scr);
        view.setBottomComponent(waterPlot);
    }

    public void signalUpdated(SignalEvent event) {
        super.signalUpdated(event);
        waterPlot.signalUpdated(event);

    }
    protected WaterfallLossPlot waterPlot;

    public void write(DataAdaptor vda) {
        super.write(vda);
        if (waterPlot != null) {
            DataAdaptor da = vda.createChild(waterPlot.dataLabel());
            waterPlot.write(da);
        }
        vda.setValue("LossType", getLossSignal());
    }

    public void update(DataAdaptor vda) {
        super.update(vda);
        DataAdaptor da = vda.childAdaptor(waterPlot.dataLabel());
        if (da != null) {
            waterPlot.update(da);
        }
        if(vda.hasAttribute("LossType")){
            switchLossSignal(vda.stringValue("LossType"));
        }
    }

    public double getWeight(String n) {
        return ((SumTable) table).getWeight(n);
    }
}
