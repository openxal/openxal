package xal.app.lossviewer.views;

import xal.app.lossviewer.dndcomponents.LossTable;
import xal.extension.application.*;
import xal.app.lossviewer.*;

import xal.app.lossviewer.dndcomponents.lossplot.*;
import xal.app.lossviewer.signals.*;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import xal.tools.data.DataAdaptor;

public class DefaultLossView extends LossView {
	
	protected LossPlot plot=null;
	

	public void signalUpdated(SignalEvent event) {
		if(table!=null){
			AbstractTableModel tm = ((AbstractTableModel)table.getModel());
			tm.fireTableChanged(new TableModelEvent(tm,0,tm.getRowCount()));
			
		}
                if(plot!=null){
                    plot.fireDataChanger();
                }
	}
	
	
	private String listName;
	
	public void setListName(String text) {
		listName = text;
	}
	
	
	/**
	 * Method getRoot
	 *
	 * @return   a View
	 *
	 */
	public View<LossDetector> getRoot() {
		return this;
	}
	
	
	protected JSplitPane view;
	public JComponent getView() {
		
		return view;
	}
	

	protected LossTable table;
	public DefaultLossView() {
            init();
	}
	public void setTitle(String s) {
		super.setTitle(s);
//		label.setText(s);
		
	}
	
	public void write(DataAdaptor vda) {
		super.write(vda);
		vda.setValue("listname", listName);
		
		
		
		DataAdaptor splida = vda.createChild("Split");
		splida.setValue("LastPosition",view.getLastDividerLocation());
		splida.setValue("Position",view.getDividerLocation());
		
		
		
		DataAdaptor da = vda.createChild("Added");
		for (LossDetector det : addedDet) {
			DataAdaptor detda = da.createChild("Detector");
			detda.setValue("name", det.getName());
		}
		da = vda.createChild("Removed");
		for (LossDetector det : removedDet) {
			DataAdaptor detda = da.createChild("Detector");
			detda.setValue("name", det.getName());
		}
		if(table!=null){
                    da = vda.createChild(table.dataLabel());
                    table.write(da);
                }
		if(plot!=null){
                    da = vda.createChild(plot.dataLabel());
                    plot.write(da);
                }
	}
	
	public void update(DataAdaptor vda) {
		super.update(vda);
		try {
			listName = vda.stringValue("listname");
		}
		catch (Exception ex) {
			
		}
		if (listName == null)
			listName = getTitle();
	//	System.out.println(listName);
		
		DataAdaptor da;
		
		try {
			da = vda.childAdaptor("Split");
			view.setDividerLocation(da.intValue("Position"));
			view.setLastDividerLocation(da.intValue("LastPosition"));
			
		}catch(Exception ex){
			
		}
		
		da = vda.childAdaptor("Added");
		if (da != null) {
			List<DataAdaptor> detectors = da.childAdaptors("Detector");
			for (DataAdaptor detector : detectors) {
				String detName = detector.stringValue("name");
				if (detName != null) {
					addedDet.add(((Main)Application.getAdaptor())
								 .getDetector(detName));
				}
			}
		}
		da = vda.childAdaptor("Removed");
		if (da != null) {
			List<DataAdaptor> detectors = da.childAdaptors("Detector");
			for (DataAdaptor detector : detectors) {
				String detName = detector.stringValue("name");
				if (detName != null) {
					removedDet.add(((Main)Application.getAdaptor())
								 .getDetector(detName));
				}
			}
		}
		
		Set<LossDetector> detectors = ((Main)Application.getAdaptor())
			.getSequences().get(listName);
		if(detectors==null)
			detectors = ((Main)Application.getAdaptor())
				.getCombos().get(listName);
		
		setDetectors(detectors);
		
		
		addDetectors(addedDet);
		super.removeDetectors(removedDet);
		
		da = vda.childAdaptor(table.dataLabel());
		if(da!=null)
			table.update(da);
		
                if(plot!=null){
                    da = vda.childAdaptor(plot.dataLabel());
                    if(da!=null)
                            plot.update(da);
                }
		
		
	}
	
	private Collection<LossDetector> addedDet = new HashSet<LossDetector>();
	private Collection<LossDetector> removedDet = new HashSet<LossDetector>();
	
	public void addDetectors(Collection<LossDetector> c) {
		for (LossDetector det : c) {
			if (removedDet.contains(det)) {
				removedDet.remove(det);
			}
			else {
				addedDet.add(det);
			}
		}
		super.addDetectors(c);
		
	}
	public void removeDetectors(Collection<LossDetector> c) {
		for (LossDetector det : c) {
			if (addedDet.contains(det)) {
				addedDet.remove(det);
			}
			else {
				removedDet.add(det);
			}
		}
		super.removeDetectors(c);
		
	}

    protected void init() {
        view = new JSplitPane();
		view.setOneTouchExpandable(true);
		table = new LossTable(this);
		table.addSelectionListener(this);
		
		plot = new LossPlot(this);
		
		JScrollPane scr = new JScrollPane(table);
		view.setTopComponent(scr);
		view.setBottomComponent(plot);
    }

//    @Override
//    public void switchLossSignal(String lossSig) {
//        plot.switchLossSignal(lossSig);
//    }

    @Override
    public String getLossSignal() {
        return plot.getLossSignal();
    }
	
	
}
	
