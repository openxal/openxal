package xal.app.lossviewer.dndcomponents;
import xal.app.lossviewer.*;
import xal.app.lossviewer.views.*;
import java.util.*;

public interface SelectionHandler<DetectorType> {
	public void addSelectionListener(SelectionHandler<DetectorType> s);
	public void removeSelectionListener(SelectionHandler<DetectorType> s);
	public void removeAllSelectionListeners();
	public void fireSelectionUpdate(SelectionEvent<DetectorType> event);
	public void processSelectionEvent(SelectionEvent<DetectorType> event);
	public Collection<DetectorType> getSelection();
	public void setSelection(Collection<DetectorType> se);
	public View<DetectorType> getRoot();
	
}
