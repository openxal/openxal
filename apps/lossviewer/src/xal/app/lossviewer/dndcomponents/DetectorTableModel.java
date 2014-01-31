/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lossviewer.dndcomponents;


import xal.app.lossviewer.LossDetector;
import xal.app.lossviewer.signals.ScalarSignalValue;
import xal.app.lossviewer.views.View;
import xal.app.lossviewer.views.ViewEvent;
import xal.app.lossviewer.views.ViewListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author azukov
 */
public class DetectorTableModel extends AbstractTableModel
        implements ViewListener {
    private static final long serialVersionUID = 7038424815194779758L;

    protected List<String> visibleSignalNames;
    Map<String,Set<String>> allLabelNormalizations;
    Map<String,NumberFormat> allFormats;
    Map<String,String> allLabelNames;

    public void setVisibleSignalNames(List<String> visibleSignalNames){
        this.visibleSignalNames=visibleSignalNames;
    }
    
    public DetectorTableModel(List<String> visibleSignalNames,
            Map<String,Set<String>> allLabelNormalizations,
            Map<String,NumberFormat> allFormats,
            Map<String,String> allLabelNames) {
        this.visibleSignalNames = visibleSignalNames;
        this.allLabelNormalizations=allLabelNormalizations;
        this.allFormats=allFormats;
        this.allLabelNames=allLabelNames;
    }

    public int[] getSelectedIndices(Collection<LossDetector> s) {
        ArrayList<Integer> ind = new ArrayList<Integer>();
        for (int i = 0; i < detectorArray.length; i++) {
            if (s.contains(detectorArray[i])) {
                ind.add(i);
            }
        }
        int[] result = new int[ind.size()];
        for (int i = 0; i < ind.size(); i++) {
            result[i] = ind.get(i);

        }
        return result;
    }

    public void processViewEvent(ViewEvent event) {
        init(event.getSource().getDetectors());
        fireTableChanged(null);
    }

    public int getRowCount() {

        if (detectorArray != null && detectorArray.length != 0) {
            return detectorArray.length;
        } else {
            return 1;
        }
    }

    public int getColumnCount() {

        return visibleSignalNames.size();
    }
    protected LossDetector[] detectorArray;

    public void init(Set<LossDetector> s) {
        if (s != null) {
            detectorArray = s.toArray(new LossDetector[]{});
        }

    }

    public Object getValueAt(int row, int column) {

        if (detectorArray == null || detectorArray.length == 0) {
            return null;
        }
        LossDetector det = detectorArray[row];
        String sn = visibleSignalNames.get(column);

        if (sn.equals("name")) {
            return det.getShortName();
        } else {
            ScalarSignalValue sv = (ScalarSignalValue) det.getValue(sn, allLabelNormalizations.get(sn));
            if (sv != null) {
                double v = sv.getValue();
                NumberFormat form = allFormats.get(sn);
                if (form != null) {
                    return form.format(v);
                } else {
                    return v;
                }
            } else {
                return null;
            }
        }


    }

    public String getColumnName(int column) {
        return allLabelNames.get(visibleSignalNames.get(column));
    }

    public Set<LossDetector> getSelectedRows(int[] ind) {

        if (detectorArray == null || detectorArray.length == 0) {
            return null;
        }
        HashSet<LossDetector> sel = new HashSet<LossDetector>();
        for (int i = 0; i < ind.length; i++) {
            sel.add(detectorArray[ind[i]]);

        }
        return sel;
    }
}
