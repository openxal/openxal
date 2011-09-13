//
//  TestDifferentialVariable.java
//  xal
//
//  Created by Tom Pelaia on 2/17/2011.
//  Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.math;

import xal.tools.math.DifferentialVariable;
import org.junit.*;


/** test the complex number class */
public class TestDifferentialVariable {
    @Test
    public void testAddition() {
        final DifferentialVariable base = DifferentialVariable.getInstance( 3.0, 2.5, 5.0 );
        final DifferentialVariable addend = DifferentialVariable.getInstance( 2.2, 1.0, 0.0 );
        final DifferentialVariable sum = base.plus( addend );
        Assert.assertTrue( sum.getValue() == 5.2 );
        Assert.assertTrue( sum.getDerivative( 0 ) == 3.5 );
        Assert.assertTrue( sum.getDerivative( 1 ) == 5.0 );
    }
}