package xal.app.lossviewer.dndcomponents.lossplot;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;

public class TypePopupMenu extends SimpleChartPopupMenu {
    private static final long serialVersionUID = -3061510278482756281L;

    private String type;
    private LossChartAdaptor adaptor;
    private Action visibleAction, limitVisibleAction;

    public TypePopupMenu(Component aChart, LossChartAdaptor anAdaptor, String type) {
        super(aChart, anAdaptor);
        adaptor = anAdaptor;
        this.type = type;

        scaleOnceAction.putValue(Action.NAME, "Scale " + type + " once");
        createDialog();

    }

    protected void update() {

        yAutoScaleAction.putValue(Action.NAME, (chartAdaptor.isYAutoScale()) ? "Freeze " + type + " Scale" : "Autoscale " + type);
        limitVisibleAction.putValue(Action.NAME, (adaptor.isLimitVisible()) ? "Hide limits" : "Show limits");
        visibleAction.putValue(Action.NAME, (adaptor.isVisible()) ? "Hide " + type : "Show " + type);
        pack();
    }

    protected void defineActions() {
        super.defineActions();
        visibleAction = new AbstractAction("Show") {
            private static final long serialVersionUID = 4377386270269629176L;

            public void actionPerformed(ActionEvent e) {
                adaptor.setVisible(!adaptor.isVisible());
            }
        };
        limitVisibleAction = new AbstractAction("Show") {
            private static final long serialVersionUID = 4377386270269629176L;
            public void actionPerformed(ActionEvent e) {
                adaptor.setLimitVisible(!adaptor.isLimitVisible());
            }
        };
    }

    protected void buildMenu() {

        add(visibleAction);
        add(limitVisibleAction);
        add(scaleOnceAction);
        add(yAutoScaleAction);
        add(optionsAction);
    }

    private void createDialog() {
        Window owner = SwingUtilities.windowForComponent(_chart);

        if (owner instanceof Frame) {
            chartDialog = new LossDialog((Frame) owner, _chart, chartAdaptor);
        } else if (owner instanceof Dialog) {
            chartDialog = new LossDialog((Dialog) owner, _chart, chartAdaptor);
        } else {
            chartDialog = new LossDialog(_chart, chartAdaptor);
        }

        ((LossDialog) chartDialog).setType(type);

    }

}
