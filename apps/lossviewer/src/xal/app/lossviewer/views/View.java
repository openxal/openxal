package gov.sns.apps.lossviewer2.views;

import gov.sns.apps.lossviewer2.*;
import gov.sns.apps.lossviewer2.signals.*;
import gov.sns.tools.data.*;

import java.awt.Container;
import java.awt.Rectangle;
import java.util.*;
import javax.swing.*;

public interface View<DetectorType> extends DataListener,SignalListener {
	
	public void setListName(String text);
	public Set<DetectorType> getDetectors();
	public void setDocument(LV2Document doc);
	public Rectangle getParentBounds();
	public void setParentBounds(Rectangle r);
	public void setTitle(String title);
	public String getTitle();
	public void setDetectors(Set<DetectorType> detSet);
	public void addDetectors(Collection<DetectorType> l);
	public void removeDetectors(Collection<DetectorType> l);
	public void addViewListener(ViewListener vl);
	public void removeViewListener(ViewListener vl);
	public void removeAllViewListeners();
	public JComponent getView();
	public void setParentContainer(Container parent);
	public LV2Document getDocument();

    List<String> getFilter();

    void setFilter(List<String> filter);

    String getLossSignal();

    void switchLossSignal(String lossSig);

    public void switchToReference(String s);
    public String getReferenceName();

	
}
