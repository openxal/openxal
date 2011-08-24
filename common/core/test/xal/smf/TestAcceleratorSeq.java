//
//  TestComplex.java
//  xal
//
//  Created by Tom Pelaia on 2/17/2011.
//  Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.smf;

import xal.smf.data.XMLDataManager;
import xal.smf.impl.*;

import java.util.*;
import org.junit.*;


/** test the complex number class */
public class TestAcceleratorSeq {
    /** default accelerator */
    final private Accelerator DEFAULT_ACCELERATOR;
    
    
    /** Constructor */
    public TestAcceleratorSeq() {
        DEFAULT_ACCELERATOR = XMLDataManager.loadDefaultAccelerator();
    }
    
    
    @Test
    /** test that we got a default accelerator */
    public void testForDefaultAccelerator() {
        Assert.assertTrue( DEFAULT_ACCELERATOR != null );
    }
    
    
    @Test
    /** test fetching nodes by type and verify that we got just that type */
    public void testNodeFetchingByType() {
        final AcceleratorSeq ring = DEFAULT_ACCELERATOR.findSequence( "Ring" );
        final List<AcceleratorNode> nodes = ring.getNodesOfType( "BPM" );
        Assert.assertTrue( nodes.size() > 0 );
        for ( final AcceleratorNode node : nodes ) {
            Assert.assertTrue( node instanceof BPM );
        }
    }
    
    
    @Test
    /** test fetching nodes by type using a strong typing during the fetch */
    public void testNodeFetchingByStrongType() {
        final AcceleratorSeq ring = DEFAULT_ACCELERATOR.findSequence( "Ring" );
        final List<BPM> nodes = ring.<BPM>getNodesOfType( "BPM" );
        Assert.assertTrue( nodes.size() > 0 );
    }
}