/*
 * Created on Sep 16, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package xal.tools.math.r3;

import  Jama.EigenvalueDecomposition;



/**
 *  Encapsulates the results of an eigenvalue decomposition operation on
 *  a R3x3 matrix object.
 * 
 *  Essentially this class is just a wrapper over the <i>Jama</i> matrix
 *  package class <code>EigenvalueDecomposition</code>.  Thus, XAL can present
 *  a consistent interface in the event that Jama gets removed/replaced in 
 *  the future.
 * 
 *  If the matrix A is symmetric it can be decomposed as
 * 
 *      A = V*D*V'
 * 
 *  where R is an orthogonal matrix in SO(3), D is the real diagonal 
 *  matrix of eigenvales of A, and the prime indicates transposition.  
 *  
 *  If A is not symmetric then the decomposition is (loosely)
 *  
 *      A = V*D*V^-1
 *  
 *  where D is now block diagonal with the real eigenvalues in 1x1 blocks
 *  and complex eigenvalues x + iy in 2x2 blocks {{x, y},{-y, x}}.  The columns
 *  of V are the eigenvalues of A in the sense that A*V = V*D.  Note that the
 *  matrix V may be badly conditioned, or even singular, so that the above 
 *  equation may not be valid.
 *  
 * @author Christopher K. Allen
 */

public class R3x3EigenDecomposition {
    
    
    
    /*
     *  Local Attributes 
     */
     
    /** the eigenvalue decomposition results */ 
    private EigenvalueDecomposition     jamaDecomp;


    
    
    

    /*
     * Initialization
     */
     
     
    /**
     * Package constructor for <code>R3x3JacobiDecomposition</code> objects.
     * 
     * @param matTarget     target matrix to factorize
     * 
     * @throws  IllegalArgumentException    matrix is not symmetric or zero eigenvalue
     */
    public R3x3EigenDecomposition(R3x3 matTarget) throws IllegalArgumentException {
        if (!matTarget.isSymmetric())
            throw new IllegalArgumentException("R3x3JacobiDecomposition: Target matrix is not symmetric");
        
        this.decompose(matTarget);
    }

    
    
    /*
     *  Data Query
     */
     

    /**
     * Get the real part of the eigenvalues. 
     */
    public double[] getRealEigenvalues()  {
        return jamaDecomp.getRealEigenvalues();
    }
    
    /**
     * Get the imaginary parts of the eigenvalues.
     */
    public double[] getImagEigenvalues()   {
        return jamaDecomp.getImagEigenvalues();
    }


    /**
     *  Get the matrix V of eigenvector (columns) for the decomposition. Note
     *  that this matrix is the diagonalizing matrix for the target matrix A. 
     *  If the target matrix A is symmetric then the returned matrix V will 
     *  be in the special orthogonal group SO(3).
     *  
     *  Note that, in general, this matrix may be badly conditioned.
     *
     *  @return     diagonalizing matrix of A 
     */    
    public R3x3 getEigenvectorMatrix()  {
        return new R3x3( jamaDecomp.getV() );
    }
    
    
    /**
     * Return the matrix D of eigenvalues in the decomposition.  Note that if 
     * target matrix A is symmetric then this matrix will be diagonal.  Otherwise
     * it will be block diagonal in general where the 2x2 blocks are composed of the
     * real and imaginary parts of the eigenvalues as described in the class 
     * documentation.
     *
     * @return      block diagonal matrix D of eigenvalues of A
     */
    public R3x3 getEigenvalueMatrix()   {
        return new R3x3( jamaDecomp.getD() );    
    }
    


    /*
     * Internal Support
     */

    /**
     * Decompose the target matrix into its factors using the
     * <code>Jama</code> encapsulated objects.
     * 
     * @param   matTarget   matrix to decompose
     */
    private void    decompose(R3x3 matTarget)   {
        this.jamaDecomp = matTarget.getMatrix().eig();
    }

    
}
