package xal.app.lossviewer.dndcomponents;

import xal.app.lossviewer.*;
import java.util.*;

public class SelectionEvent<DetectorType> {
	
	private Collection<DetectorType>command;
	
	private SelectionHandler<DetectorType> source;
	private Collection<SelectionHandler<DetectorType>> handlers = new HashSet<SelectionHandler<DetectorType>>();
	
	public SelectionEvent(Set<DetectorType> selection, SelectionHandler<DetectorType> source){
		this.command=selection;
		this.source = source;
		handlers.add(source);
	}
	
	public void addProcessedHandler(SelectionHandler<DetectorType> sh) {
		handlers.add(sh);
	}
	
	public boolean contains(SelectionHandler<DetectorType> sh) {
		return handlers.contains(sh);
	}
	
	
	/**
	 * Returns Command
	 *
	 * @return    a  String
	 */
	public Collection<DetectorType> getSelection() {
		return command;
	}
	
	
	/**
	 * Returns Source
	 *
	 * @return    an Object
	 */
	public Object getSource() {
		return source;
	}
}
