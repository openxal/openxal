//
// DifferentiableSymbol.java: Source file for 'DifferentiableSymbol'
// Project xal
//
// Created by Tom Pelaia II on 5/3/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.math.differential;


/** Operation for a symbol */
abstract class DifferentiableSymbol extends DifferentiableOperation {
    /** get the operation precedence */
    protected int getPrecedence() {
        return DifferentiableOperation.SYMBOL_PRECEDENCE;
    }
}
