//
// MachineRecorder.java: Source file for 'MachineRecorder'
// Project xal
//
// Created by Tom Pelaia on 6/16/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinerecorder;

import xal.tools.data.*;
import xal.smf.*;

import java.util.*;


/** MachineRecorder model */
public class MachineRecorder implements DataListener {
 	/** the data adaptor label used for archiving */
	static public final String DATA_LABEL = "MachineRecorder";
    
    
    /** groups of channels to display together on one view */
    private final List<DisplayGroup> DISPLAY_GROUPS;
    
    /** current accelertor */
    private Accelerator _accelerator;
    
    
	/** Constructor */
    public MachineRecorder() {
        DISPLAY_GROUPS = new ArrayList<DisplayGroup>();
    }
    
    
    /** set a new accelerator */
    public void setAccelerator( final Accelerator accelertor ) {
        _accelerator = accelertor;
    }
    
    
    /** get the current accelerator */
    public Accelerator getAccelerator() {
        return _accelerator;
    }
    
    
    /** record events for channels in the groups */
    public void record() {
        System.out.println( "Recording a new session..." );
    }
    
    
    /** play back the recorded events */
    public void play() {
        System.out.println( "Playing recorded events..." );
    }
    
    
    /** record events for channels in the groups */
    public void stop() {
        System.out.println( "Stop recording or playing..." );
    }
    
    
    /** move the current pointer to the event preceding the current event */
    public void moveToPrecedingEvent() {
        System.out.println( "Move to preceding event..." );
    }
    
    
    /** move the current pointer to the event after the current event */
    public void moveToNextEvent() {
        System.out.println( "Move to next event..." );
    }
    
    
    /** move the current pointer to the event corresponding to the preceding flag */
    public void moveToPrecedingFlag() {
        System.out.println( "Move to preceding flag..." );
    }
    
    
    /** move the current pointer to the event corresponding to the next flag */
    public void moveToNextFlag() {
        System.out.println( "Move to next flag..." );
    }
    
    
    /** get the list of display groups */
    public List<DisplayGroup> getDisplayGroups() {
        return DISPLAY_GROUPS;
    }
    
    
    /** get the group at the specified index */
    public DisplayGroup getDisplayGroupAt( final int index ) {
        return DISPLAY_GROUPS.get( index );
    }
    
    
    /** add the new display group */
    public void addDisplayGroup( final DisplayGroup group ) {
        DISPLAY_GROUPS.add( group );
    }
    
    
    /** add the new display group */
    public void addDisplayGroupAt( final DisplayGroup group, final int index ) {
        DISPLAY_GROUPS.add( index, group );
    }
    
    
    /** remove the display group */
    public void removeDisplayGroupAt( final int index ) {
        DISPLAY_GROUPS.remove( index );
    }
	
    
    /** provides the name used to identify the class in an external data source. */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /** Instructs the receiver to update its data based on the given adaptor. */
    public void update( final DataAdaptor adaptor ) {
    }
    
    
    /** Instructs the receiver to write its data to the adaptor for external storage. */
    public void write( final DataAdaptor adaptor ) {
    }
}
