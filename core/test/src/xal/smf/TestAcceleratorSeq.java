//
//  TestAcceleratorSeq.java
//  xal
//
//  Created by Tom Pelaia on 8/24/2011.
//  Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.smf;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.smf.impl.BPM;
import xal.test.ResourceManager;


/** test the AcceleratorSeq class */
public class TestAcceleratorSeq {
    /** default accelerator */
    static private Accelerator DEFAULT_ACCELERATOR;
    
    
    @BeforeClass
    public static void commonSetup() {
        DEFAULT_ACCELERATOR = ResourceManager.getTestAccelerator();
    }
    
    
    @AfterClass
    public static void commonCleanup() {
        DEFAULT_ACCELERATOR = null;
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
    /** test fetching nodes by qualifier using a strong typing during the fetch */
    public void testNodeFetchingByStrongType() {
        final AcceleratorSeq ring = DEFAULT_ACCELERATOR.findSequence( "Ring" );
        final List<BPM> nodes = ring.getNodesOfClassWithStatus( BPM.class, true );
        Assert.assertTrue( nodes.size() > 0 );
        for ( final BPM node : nodes ) {
            Assert.assertTrue( node instanceof BPM );
        }
    }
}