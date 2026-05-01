//
// DisplayGroupConfigController.java: Source file for 'DisplayGroupConfigController'
// Project xal
//
// Created by Tom Pelaia on 6/28/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinerecorder;

import javax.swing.*;
import java.awt.event.*;
import java.util.*;

import xal.extension.bricks.WindowReference;
import xal.extension.widgets.swing.KeyValueTableModel;
import xal.smf.*;
import xal.extension.widgets.smf.NodeChannelSelector;


/** DisplayGroupConfigController */
public class DisplayGroupConfigController {
    /** window reference */
    final private WindowReference WINDOW_REFERENCE;
    
    /** table model for the display group's channels */
    final private KeyValueTableModel<NodeChannelRef> GROUP_CHANNEL_TABLE_MODEL;
    
    /** display group to configure */
    private DisplayGroup _displayGroup;
    
    
	/** Constructor */
    public DisplayGroupConfigController( final WindowReference windowReference ) {
        WINDOW_REFERENCE = windowReference;

        GROUP_CHANNEL_TABLE_MODEL = new KeyValueTableModel<NodeChannelRef>();

        final JTable GROUP_CHANNEL_TABLE = (JTable)windowReference.getView( "Group Channel Table" );
        GROUP_CHANNEL_TABLE.setModel( GROUP_CHANNEL_TABLE_MODEL );
        
        final JButton addGroupChannelButton = (JButton)windowReference.getView( "AddGroupChannelButton" );
        addGroupChannelButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                if ( _displayGroup != null ) {
                    final AcceleratorSeq sequence = _displayGroup.getSequence();
                    if ( sequence != null ) {
                        final NodeChannelSelector channelSelector = NodeChannelSelector.getInstanceFromNodes( sequence.getAllInclusiveNodes( true ), (JFrame)WINDOW_REFERENCE.getWindow(), "Select Channels" );
                        final List<NodeChannelRef> channelRefs = channelSelector.showDialog();
                        final int row = GROUP_CHANNEL_TABLE.getSelectedRow();
                        if ( row >= 0 ) {
                            final int channelIndex = GROUP_CHANNEL_TABLE.convertRowIndexToModel( row );
                            _displayGroup.addChannelRefsAt( channelRefs, channelIndex );
                        }
                        else {
                            _displayGroup.addChannelRefs( channelRefs );
                        }
                        GROUP_CHANNEL_TABLE_MODEL.setRecords( _displayGroup.getChannelRefs() );
                    }
                    else {
                        JOptionPane.showMessageDialog( windowReference.getWindow(), "You must first select a sequence before attempting to add channels.", "Can't Add Channels", JOptionPane.WARNING_MESSAGE );
                    }
                }
            }
        });

        
        final JButton deleteGroupChannelButton = (JButton)windowReference.getView( "DeleteGroupChannelButton" );
        deleteGroupChannelButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                if ( _displayGroup != null ) {
                    final int row = GROUP_CHANNEL_TABLE.getSelectedRow();
                    if ( row >= 0 ) {
                        final int channelIndex = GROUP_CHANNEL_TABLE.convertRowIndexToModel( row );
                        _displayGroup.removeChannelRefAt( channelIndex );
                    }
                    GROUP_CHANNEL_TABLE_MODEL.setRecords( _displayGroup.getChannelRefs() );
                }
            }
        });
    }
    
    
    /** set the display group to configure */
    public void setDisplayGroup( final DisplayGroup displayGroup ) {
        _displayGroup = displayGroup;
        
        if ( displayGroup != null ) {
            GROUP_CHANNEL_TABLE_MODEL.setRecords( displayGroup.getChannelRefs() );
        }
        else {
            GROUP_CHANNEL_TABLE_MODEL.setRecords( new ArrayList<NodeChannelRef>() );
        }
    }
    
    
    /** get the display group being configured */
    public DisplayGroup getDisplayGroup() {
        return _displayGroup;
    }
}
