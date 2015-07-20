/*
 * Created on Sep 16, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package xal.tools.math.r3;

import  Jama.EigenvalueDecomposition;



/**
 *  <p>
 *  Essentially this class is just a wrapper over the <i>Jama</i> matrix
 *  package class <code>EigenvalueDecomposition</code>.  Thus, XAL can present
 *  a consistent interface in the event that Jama gets removed/replaced in 
 *  the future.
 *  </p>
 *  <p>
 *  If the matrix <b>A</b> is invertible it can be decomposed as
 *  <br>
 *  <br>
 *  &nbsp; &nbsp; <b>A</b> = <b>VDV</b><sup>-1</sup>
 *  <br>
 *  <br>
 *  where <b>V</b> is an invertible matrix in the special linear group 
 *  <i>SL</i>(3) &sub; <b>R</b><sup>3&times;3</sup> and <b>D</b> is the
 *  the real matrix with 2&times;2 blocks consisting of the real and imaginary parts
 *  of the eigenvalues on the diagonal.  (Each eigenvalue
 *  of matrix <b>A</b> is the diagonal of the Jacobi block.)
 *  </p>
 *  <p>  
 *  The columns
 *  of <b>V</b> are the eigenvectors of <b>A</b> in the sense that <b>AV</b> = <b>VD</b>.  
 *  Note that the matrix <b>V</b> may be badly conditioned, or even singular, so that the above 
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
     *  Get the matrix <b>V</b> of eigenvectors (columns) for the decomposition. Note
     *  that this matrix is the diagonalizing (in the Jacaobi sense) matrix 
     *  for the target matrix <b>A</b>. 
     *  If the target matrix <b>A</b> is symmetric then the returned matrix V will 
     *  be in the special orthogonal group <i>SO</i>(3).
     *  
     *  Note that, in general, this matrix may be ill conditioned.
     *
     *  @return     diagonalizing matrix of <b>A</b> 
     */    
    public R3x3 getEigenvectorMatrix()  {
        Jama.Matrix matV = this.jamaDecomp.getV();
        double[][]  arrV = matV.getArrayCopy();
        
        return new R3x3( arrV );
    }
    
    
    /**
     * Return the matrix <b>D</b> of eigenvalues in the decomposition.  Note that if 
     * target matrix <b>A</b> is symmetric then this matrix will be diagonal.  Otherwise
     * it will be block diagonal in general where the 2x2 blocks are composed of the
     * real and imaginary parts of the eigenvalues as described in the class 
     * documentation.
     *
     * @return      block diagonal matrix <b>D</b> of eigenvalues of <b>A</b>
     */
    public R3x3 getEigenvalueMatrix()   {
        Jama.Matrix     matD = jamaDecomp.getD();
        double[][]      arrD = matD.getArrayCopy();
        
        return new R3x3( arrD );    
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
        double[][]  arrA = matTarget.getArrayCopy();
        Jama.Matrix matA = new Jama.Matrix(arrA);
        
        this.jamaDecomp = matA.eig();
    }

    
}
