package xal.app.lossviewer.views;

import xal.app.lossviewer.*;
import xal.app.lossviewer.dndcomponents.*;

import java.awt.*;

import java.util.*;
import java.util.ArrayList;
import javax.swing.*;

import java.util.List;
import xal.tools.data.DataAdaptor;

public abstract class LossView implements SelectionHandler<LossDetector>, View<LossDetector> {

    private String title;
    protected SortedSet<LossDetector> filteredDetectors;
    protected SortedSet<LossDetector> detectors;
    private Rectangle parentBounds;
    protected LV2Document document;
    private Container parentContainer;
    private String referenceName;
    private String lossSignal;

    public void switchLossSignal(String lossSig) {
        lossSignal = lossSig;
        fireViewUpdated(new ViewEvent("switchloss", this, lossSig));
    }

    public abstract String getLossSignal();

    public Set<LossDetector> getDetectors() {

        return filteredDetectors;
    }

    public void setDocument(LV2Document doc) {
        document = doc;
    }

    public LV2Document getDocument() {
        return document;
    }

    public Rectangle getParentBounds() {
        JComponent view = getView();
        if (view == null) {
            return parentBounds;
        }

        if (parentContainer == null) {
            return parentBounds;
        }
        return parentContainer.getBounds();
    }

    public void setParentBounds(Rectangle r) {
        parentBounds = r;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDetectors(Set<LossDetector> detSet) {
        setDetectors(detSet, new ViewEvent(null, this));
    }

    public void setDetectors(Set<LossDetector> detSet, ViewEvent ve) {
        filteredDetectors = new TreeSet<LossDetector>(LossDetector.getComparator());
        detectors = new TreeSet<LossDetector>(LossDetector.getComparator());
        if (detSet != null) {
            detectors.addAll(detSet);
            for (LossDetector ld : detSet) {
                if (filter(ld)) {
                    filteredDetectors.add(ld);
                }
            }
        }
        fireViewUpdated(ve);
    }

    public void addDetectors(Collection<LossDetector> l) {
        boolean changed = false;

        for (LossDetector e : l) {
            if (!detectors.contains(e)) {
                detectors.add(e);

            }
            if (!filteredDetectors.contains(e) && filter(e)) {
                filteredDetectors.add(e);
                changed = true;
            }
        }
        if (changed) {
            fireViewUpdated();
        }
    }

    public void removeDetectors(Collection<LossDetector> l) {
        boolean changed = false;

        for (LossDetector e : l) {
            if (detectors.contains(e)) {
                detectors.remove(e);

            }
        }

        for (LossDetector e : l) {
            if (filteredDetectors.contains(e)) {
                filteredDetectors.remove(e);
                changed = true;
            }
        }
        if (changed) {
            fireViewUpdated();
        }
    }
    java.util.List<ViewListener> viewListeners = new ArrayList<ViewListener>();

    protected void fireViewUpdated() {
        fireViewUpdated(new ViewEvent(null, this));
    }

    protected void fireViewUpdated(ViewEvent ev) {
        for (ViewListener viewListener : viewListeners) {
            viewListener.processViewEvent(ev);
        }
    }

    public void addViewListener(ViewListener vl) {
        viewListeners.add(vl);
    }

    public void removeViewListener(ViewListener vl) {
        viewListeners.remove(vl);
    }

    public void removeAllViewListeners() {
        viewListeners.clear();
    }

    public abstract JComponent getView();

    public String dataLabel() {
        return "LossView";
    }

    public void setParentContainer(Container parent) {
        parentContainer = parent;
    }

    public void write(DataAdaptor vda) {
        vda.setValue("name", getTitle());
        DataAdaptor bda = vda.createChild("Bounds");
        Rectangle bounds = getParentBounds();
        //	System.out.println(bounds);
        bda.setValue("height", bounds.height);
        bda.setValue("width", bounds.width);
        bda.setValue("x", bounds.x);
        bda.setValue("y", bounds.y);

        DataAdaptor fdas = vda.createChild("Filters");
        for (String s : filteredType) {
            DataAdaptor fda = fdas.createChild("Filter");
            fda.setValue("type", s);
        }

    }

    public void update(DataAdaptor vda) {
        setTitle(vda.stringValue("name"));
        DataAdaptor bda = vda.childAdaptor("Bounds");
        if (bda == null) {
            setParentBounds((Rectangle) document.get("Defaultview.bounds"));
        } else {
            int height = bda.intValue("height");
            int width = bda.intValue("width");
            int x = bda.intValue("x");
            int y = bda.intValue("y");
            setParentBounds(new Rectangle(x, y, width, height));
        }
        DataAdaptor fdas = vda.childAdaptor("Filters");
        if (fdas != null) {
            for (DataAdaptor fda : fdas.childAdaptors()) {

                String type = fda.stringValue("type");
                if (type != null) {
                    filteredType.add(type);
                }
            }
        }


    }
//selection handler implementaion
    private Collection<LossDetector> selection;

    public Collection<LossDetector> getSelection() {
        return selection;
    }

    public void setSelection(Collection<LossDetector> se) {
        selection = se;
    }
    List<SelectionHandler<LossDetector>> selectionListeners = new ArrayList<SelectionHandler<LossDetector>>();

    public void addSelectionListener(SelectionHandler<LossDetector> s) {
        selectionListeners.add(s);
    }

    public void removeSelectionListener(SelectionHandler<LossDetector> s) {
        selectionListeners.remove(s);
    }

    public void removeAllSelectionListeners() {
        selectionListeners.clear();
    }

    public void fireSelectionUpdate(SelectionEvent<LossDetector> event) {
        for (SelectionHandler<LossDetector> e : selectionListeners) {
            e.processSelectionEvent(event);
        }
    }

    public void processSelectionEvent(SelectionEvent<LossDetector> event) {

        if (event.contains(this)) {
            return;
        }
        //	System.out.println(this+ " got seleEv");
        setSelection(event.getSelection());
        event.addProcessedHandler(this);
        fireSelectionUpdate(event);

    }
    List<String> filteredType = new ArrayList<String>();

    public void setFilter(List<String> filter) {
        filteredType = filter;
        setDetectors(detectors);
    }

    public List<String> getFilter() {
        return filteredType;
    }

    protected boolean filter(LossDetector ld) {
        if (filteredType == null) {
            return true;
        }

        if (ld == null) {
            return true;
        }
        String type = ld.getType();
        if (type == null) {
            return true;
        }
        return !filteredType.contains(type);
    }

    public void switchToReference(String s) {
        boolean refOn = false;
        if ("NONE".equals(s)) {
            referenceName = "";
        } else {
            referenceName = s;
            refOn = true;
        }
        List<LossDetector> ref = document.getReferences().get(s);

        Set<String> names = new HashSet<String>();
        Set<LossDetector> newDetectors = new HashSet<LossDetector>();
        for (LossDetector ld : detectors) {
            names.add(ld.getName());
        }
        for (LossDetector ld : ref) {
            if (names.contains(ld.getName())) {
                newDetectors.add(ld);
            }
        }
        if (refOn) {
            setDetectors(newDetectors, new ViewEvent("referenceON", this, lossSignal));
        } else {
            setDetectors(newDetectors, new ViewEvent("referenceOFF", this, lossSignal));
        }


    }

    public String getReferenceName() {
        return referenceName;
    }
}
