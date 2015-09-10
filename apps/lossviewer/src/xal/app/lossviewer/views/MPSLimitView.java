/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lossviewer.views;

import xal.app.lossviewer.dndcomponents.LossTable;
import xal.app.lossviewer.dndcomponents.mpspanel.MPSPanel;
import xal.app.lossviewer.signals.SignalEvent;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import xal.tools.data.DataAdaptor;

/**
 *
 * @author az9
 */
public class MPSLimitView extends DefaultLossView {

//    public void switchLossSignal(String lossSig) {
//        if("PulseLoss".equals(lossSig)){
//            mpsPanel.setSignalType(MPSPanel.PULSE_LOSS);
//
//        }
//        else if("Slow60".equals(lossSig)){
//            mpsPanel.setSignalType(MPSPanel.SECOND_LOSS);
//        }
//    }

    public String getLossSignal() {
        String lossSignal="";
        switch(mpsPanel.getSignalType()){
            case MPSPanel.PULSE_LOSS:
                lossSignal = "PulseLoss";
                break;

            case MPSPanel.SECOND_LOSS:
                lossSignal = "Slow60";
                break;
            default:
                break;

        }
        return lossSignal;
    }

    protected void init() {
        view = new JSplitPane();
        view.setOneTouchExpandable(true);
        table = new LossTable(this, "MPSLossTable");
        table.addSelectionListener(this);

        mpsPanel = new MPSPanel(this);
        addSelectionListener(mpsPanel);
        

        JScrollPane scr = new JScrollPane(table);
        view.setTopComponent(scr);
        view.setBottomComponent(mpsPanel);
    }

    public void signalUpdated(SignalEvent event) {
        super.signalUpdated(event);
        mpsPanel.fireDataChanged();

    }
    protected MPSPanel mpsPanel;

    public void write(DataAdaptor vda) {
        super.write(vda);
        if (mpsPanel != null) {
            DataAdaptor da = vda.createChild(mpsPanel.dataLabel());
            mpsPanel.write(da);
        }
    }

    public void update(DataAdaptor vda) {
        super.update(vda);
        DataAdaptor da = vda.childAdaptor(mpsPanel.dataLabel());
        if (da != null) {
            mpsPanel.update(da);
        }
    }
}
