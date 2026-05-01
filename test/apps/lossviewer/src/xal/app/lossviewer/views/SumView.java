/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lossviewer.views;

import xal.app.lossviewer.dndcomponents.sumpanel.SumPanel;
import xal.app.lossviewer.dndcomponents.SumTable;
import xal.app.lossviewer.signals.SignalEvent;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import xal.tools.data.DataAdaptor;

/**
 *
 * @author az9
 */
public class SumView extends DefaultLossView {

//    public  void switchLossSignal(String lossSig){
//
//    }
    public String getLossSignal() {
        return sumPanel.getLossSignal();
    }

    protected void init() {
        view = new JSplitPane();
        view.setOneTouchExpandable(true);
        table = new SumTable(this, "SumTable");
        table.addSelectionListener(this);

        sumPanel = new SumPanel(this);
        //         addSelectionListener(mpsPanel);

        JScrollPane scr = new JScrollPane(table);
        view.setTopComponent(scr);
        view.setBottomComponent(sumPanel);
    }

    public void signalUpdated(SignalEvent event) {
        super.signalUpdated(event);
        sumPanel.fireDataChanged();
    //       System.out.println("Event "+System.currentTimeMillis());

    }
    protected SumPanel sumPanel;

    public void write(DataAdaptor vda) {
        super.write(vda);
        if (sumPanel != null) {
            DataAdaptor da = vda.createChild(sumPanel.dataLabel());
            sumPanel.write(da);
        }
        vda.setValue("LossType", getLossSignal());
    }

    public void update(DataAdaptor vda) {
        super.update(vda);
        DataAdaptor da = vda.childAdaptor(sumPanel.dataLabel());
        if (da != null) {
            sumPanel.update(da);
        }
        String type = vda.stringValue("LossType");
        if(type!=null){
            sumPanel.switchLoss(type);
        }
    }

    public double getWeight(String n) {
        return ((SumTable) table).getWeight(n);
    }
}
