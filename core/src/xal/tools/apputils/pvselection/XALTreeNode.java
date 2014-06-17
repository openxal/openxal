/*
 * @(#)XALTreeNode.java          0.5 11/28/2002
 *
 * Copyright (c) 2002-2003 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */
 
package xal.tools.apputils.pvselection;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import xal.smf.*;
import xal.ca.*;

/** 
 * Generate sequence, device type, device, and PV tree structure 
 * @version   0.5  28 Nov 2002
 * @author C.M. Chu
 */

public class XALTreeNode extends HandleNode {
	private static final long serialVersionUID = 0L;
	
    AcceleratorSeq[] allSeqs;

    public XALTreeNode(){
	super("empty");
    }

    public XALTreeNode(Accelerator acc, String title)
    {
	super(title);   
	allSeqs = acc.getSequences().toArray(new AcceleratorSeq[0]);
        defineSeqNodes();
    }

    public void setTitle(String title){
	setUserObject(title);
	defineSeqNodes(); 
    }

    public void setAccelerator(Accelerator acc){
       allSeqs = acc.getSequences().toArray(new AcceleratorSeq[0]); 
       defineSeqNodes();
    }
  
    private void defineSeqNodes() {
		// collecting all sequences
		for (int i=0; i<allSeqs.length; i++) {
			if( !"Bnch".equals( allSeqs[i].getType() ) ) {
				Vector<String> typeV = new Vector<String>();
				java.util.List<AcceleratorNode> nodes = allSeqs[i].getAllNodes();
				for ( final AcceleratorNode node : nodes ) {
					String type = node.getType();
					if( !typeV.contains(type) )
						typeV.addElement( type );
			    }
				add( new SeqNode( allSeqs[i].getId(), typeV, allSeqs[i] ) );
		    }
	    }
    }     
}


class SeqNode extends HandleNode {
	private static final long serialVersionUID = 0L;

    String sid;

    private SeqNode(){}

    public SeqNode(String seq, Vector<String> types, AcceleratorSeq accSeq ) {
		sid = seq;
        defineTypeNodes( types,accSeq );
    }

    private void defineTypeNodes( Vector<String> types, AcceleratorSeq accSeq ) {
		for (int j=0; j<types.size(); j++) {
			// try vector instead of hashtable
			java.util.List<AcceleratorNode> nodesOfType = accSeq.getAllNodesOfType( types.elementAt(j) );
			Vector<String> devIdV = new Vector<String>();
			Vector<AcceleratorNode> deviceV = new Vector<AcceleratorNode>();
			for (int jj=0; jj<nodesOfType.size(); jj++) {
				AcceleratorNode accNode = nodesOfType.get( jj );
				if ( accNode.getStatus() ) {
					devIdV.add( accNode.getId() );
					deviceV.add( accNode );
				}
		    }

			add( new TypeNode( types.elementAt(j), devIdV, deviceV ) );

	    }

    }

    public String toString()
    {
		TreeNode parent = getParent();
		if (parent == null)
			return ("Device Types:");
		else
			return sid;
    }

}


class TypeNode extends HandleNode {
	private static final long serialVersionUID = 0L;

    private TypeNode(){}

    public TypeNode( String type, Vector<String> devIds, Vector<AcceleratorNode> devs ) {
		super(type);
        defineHandleNodes(devIds,devs);
    }

    private void defineHandleNodes( Vector<String> devIds, Vector<AcceleratorNode> devs ) {
		for (int k=0; k<devs.size(); k++) {
			Collection<String> handlesOfNode = devs.elementAt(k).getHandles();
			Vector<String> handleV = new Vector<String>();
			for ( final String handle : handlesOfNode ) {
				handleV.addElement( handle );
		    }

			add( new DeviceNode( devIds.elementAt(k), handleV, devs.elementAt(k) ) );
	    }
    }
}

class DeviceNode extends HandleNode {
	private static final long serialVersionUID = 0L;

    private DeviceNode(){}

    public DeviceNode( String devId, Vector<String> handles, AcceleratorNode dev ) {
		super(devId);
        defineDeviceNodes( handles,dev );
    }

    private void defineDeviceNodes( Vector<String> handles, AcceleratorNode dev ) {
		for (int k=0; k < handles.size(); k++) {
			Channel channel = dev.getChannel( handles.elementAt(k) );
			if(channel != null){
				HandleNode h_node =  new HandleNode( handles.elementAt(k) );
				h_node.setAsSignal(true);
				h_node.setChannel(channel);

				// get the channel name
				h_node.setSignalName( dev.getChannel( handles.elementAt(k) ).getId() );
				//h_node.setSignalName(dev.getId()+":"+(String)(handles.elementAt(k)));

				add(h_node);
			}
	    }
    }
}

