/*
 * ArrayMath.java
 *
 * Created on August 20, 2002, 1:11 PM
 */

package xal.tools;

import xal.tools.ArrayTool;

import java.lang.reflect.Array;


/**
 * Provide some simple array math operations.
 *
 * @author  tap
 */
public class ArrayMath {
    
    /** Creates a new instance of ArrayMath */
    protected ArrayMath() {
    }
    
    
    /** 
     * Add a scalar to each element of an array
     * v(i) = array(i) + offset
     */
    public static double[] translate(final double[] array, final double offset) {
        int count = array.length;
        double[] result = new double[count];
        
        for ( int index = 0 ; index < count ; index++ ) {
            result[index] = array[index] + offset;
        }
        
        return result;
    }
    
    
    /** 
     * Transform an array by mutlitplying by a scale and adding an offset
     * v(i) = scale * array(i) + offset
     */
    public static double[] transform(final double[] array, final double scale, final double offset) {
        int count = array.length;
        double[] result = new double[count];
        
        for ( int index = 0 ; index < count ; index++ ) {
            result[index] = scale * array[index] + offset;
        }
        
        return result;
    }
    
    
    /** 
     * Subtract two vectors
     * v(i) = v1(i) - v2(i)
     */
    public static double[] subtract(final double[] vec1, final double[] vec2) {
        int count = vec1.length;
        double[] result = new double[count];
        
        for ( int index = 0 ; index < count ; index++ ) {
            result[index] = vec1[index] - vec2[index];
        }
        
        return result;
    }
    
    
    /** 
     * Subtract two matrices
     * M(i,j) = m1(i,j) - m2(i,j)
     */
    public static double[][] subtract(final double[][] mat1, final double[][] mat2) {
        int dim1 = mat1.length;
        int dim2 = mat1[0].length;
        double[][] result = new double[dim1][dim2];
        
        for ( int ind1 = 0 ; ind1 < dim1 ; ind1++ ) {
            for ( int ind2 = 0 ; ind2 < dim2 ; ind2++ ) {
                result[ind1][ind2] = mat1[ind1][ind2] - mat2[ind1][ind2];
            }
        }
        
        return result;
    }

    
    /**
     * Return the square root of each element:
     * s(i1,i2,...) = v1(i1,i2,...) - v2(i1,i2,...)
     */
    public static Object subtract(Object vec1, Object vec2, int[] dimensions) {
        Object result = Array.newInstance(Double.TYPE, dimensions);
        int[] indices = (int[])Array.newInstance(Integer.TYPE, dimensions.length);
        
        while(true) {
            double val1 = ArrayTool.getDouble(vec1, indices);
            double val2 = ArrayTool.getDouble(vec2, indices);
            
            ArrayTool.setDouble(result, indices, val1 - val2);
            if ( !ArrayTool.increment(indices, dimensions) )  break;
        }
        
        return result;
    }
    
    
    /** 
     * Multiply a vector by a scalar:
     * v(i) = vector(i) * scalar
     */
    public static double[] multiply(final double[] vector, final double scalar) {
        int dim = vector.length;
        double[] result = new double[dim];
        
        for ( int index = 0 ; index < dim ; index++ ) {
            result[index] = scalar * vector[index];
        }
        
        return result;
    }
        
        
    /**
     * Return the square root of each element:
     * m(i1,i2,...) = v(i1,i2,...) * scalar
     */
    public static Object multiply(Object vector, double scalar, int[] dimensions) {
        Object result = Array.newInstance(Double.TYPE, dimensions);
        int[] indices = (int[])Array.newInstance(Integer.TYPE, dimensions.length);
        
        while(true) {
            double value = ArrayTool.getDouble(vector, indices);
            
            ArrayTool.setDouble(result, indices, value * scalar);
            if ( !ArrayTool.increment(indices, dimensions) )  break;
        }
        
        return result;
    }
    
    
    /** 
     * Multiply a vector by a matrix defined by:
     * v(i) = Sum<sub>j</sub>( mat(i,j) * vec(j) )
     */
    public static double[] multiply(final double[][] matrix, final double[] vector) {
        int rows = matrix.length;
        int columns = vector.length;
        double[] result = new double[rows];
        
        for ( int row = 0 ; row < rows ; row++ ) {
            double value = 0;
            double[] matrixRow = matrix[row];
            for ( int column = 0 ; column < columns ; column++ ) {
                value += matrixRow[column] * vector[column];
            }
            result[row] = value;
        }
        
        return result;
    }
    
    
    /** 
     * Calculate the scalar product of two vectors.
     */
    public static double scalarProduct(final double[] vec1, final double[] vec2) {
        int count = vec1.length;
        
        double result = 0;
        for ( int index = 0 ; index < count ; index++ ) {
            result += vec1[index] * vec2[index];
        }
        
        return result;
    }
    
    
    /** 
     * Multiply a column vector by a row vector where the product is a matrix defined by:
     * M(i,j) = columnVector(i) * rowVector(j)
     * Note: This is not a scalar product, nor a vector product, but rather
     * it is a product of a column vector with a row vector which results
     * in a matrix.
     */
    public static double[][] matrixProduct(final double[] columnVector, final double[] rowVector) {
        int numRows = columnVector.length;
        int numColumns = rowVector.length;
        double[][] result = new double[numRows][numColumns];
        
        for ( int row = 0 ; row < numRows ; row++ ) {
            for ( int column = 0 ; column < numColumns ; column++ ) {
                result[row][column] = columnVector[row] * rowVector[column];
            }
        }
        
        return result;
    }
    
    
    /**
     * Return the square root of each element:
     * m(i1,i2,...) = v1(i1,i2,...) * v2(i1,i2,...)
     */
    public static Object elementProduct(Object vec1, Object vec2, int[] dimensions) {
        Object result = Array.newInstance(Double.TYPE, dimensions);
        int[] indices = (int[])Array.newInstance(Integer.TYPE, dimensions.length);
        
        while(true) {
            double val1 = ArrayTool.getDouble(vec1, indices);
            double val2 = ArrayTool.getDouble(vec2, indices);
            
            ArrayTool.setDouble(result, indices, val1 * val2);
            if ( !ArrayTool.increment(indices, dimensions) )  break;
        }
        
        return result;
    }
    
    
    /**
     * Multiply two vectors element by element:
     * m(i) = v1(i) * v2(i) 
     */
    public static double[] elementProduct(final double[] vec1, final double[] vec2) {
        int dim = vec1.length;
        double[] result = new double[dim];
        
        for ( int index = 0 ; index < dim ; index++ ) {
            result[index] = vec1[index] * vec2[index];
        }
        
        return result;
    }
    
    
    /**
     * Return the square root of each element:
     * s(i) = squar_root( vec(i) )
     */
    public static double[] elementSquareRoot(final double[] vector) {
        int dim = vector.length;
        double[] result = new double[dim];
        
        for ( int index = 0 ; index < dim ; index++ ) {
            result[index] = Math.sqrt( vector[index] );
        }
        
        return result;
    }
    
    
    /**
     * Return the square root of each element:
     * s(i1,i2,...) = squar_root( vec(i1,i2,...) )
     */
    public static Object elementSquareRoot(Object vector, int[] dimensions) {
        Object result = Array.newInstance(Double.TYPE, dimensions);
        int[] indices = (int[])Array.newInstance(Integer.TYPE, dimensions.length);
        
        while(true) {
            double value = ArrayTool.getDouble(vector, indices);
            
            ArrayTool.setDouble(result, indices, Math.sqrt(value));
            if ( !ArrayTool.increment(indices, dimensions) )  break;
        }
        
        return result;
    }
    
    
    /**
     * Return the square root of each element:
     * s(i1,i2,...) = squar_root( abs( vec(i1,i2,...) ) )
     */
    public static Object elementSquareRootAbs(Object vector, int[] dimensions) {
        Object result = Array.newInstance(Double.TYPE, dimensions);
        int[] indices = (int[])Array.newInstance(Integer.TYPE, dimensions.length);
        
        while(true) {
            double value = ArrayTool.getDouble(vector, indices);
            
            ArrayTool.setDouble(result, indices, Math.sqrt( Math.abs(value) ));
            if ( !ArrayTool.increment(indices, dimensions) )  break;
        }
        
        return result;
    }
}

