/*
 * PotentialGrid.java
 *
 * Created on January 28, 2003, 4:55 PM
 *
 * Modified:
 *      02/05/03    - CKA
 */

package xal.tools.math.r3;



/**
 *  Solves Poisson's equation for a potential function defined on a grid in R3.  
 *  The grid object then solved the discretized version of
 *
 *          Div Grad <italic>Val</italic> = - <italic>Src</italic>
 *
 *  where <italic>Val</italic> is potential value on the grid and <italic>Src</italic>
 *  is a source function.
 *
 *  NOTES:
 *  -   The vector field derived from the potential function is calculated by 
 *      taking the gradient of the interpolated potential.  
 *
 *  -   Potential values at grid points may be set manuallly.
 *
 *  -   Potential values at grid points can be determined by solving Poisson's equation.
 *      The values of the source term must be set at each grid point, along with the type
 *      of field point (open, dirichlet, neumann).  
 *
 *
 * @author  Christopher Allen
 */
public class PoissonGrid extends Grid implements java.io.Serializable {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    


    /*
     *  Grid Point Type Enumerations
     */
    
    /** Grid point type is undefined */
    public static final int     PT_UNDEFINED = 0;
    
    /** Grid point is in an open region */
    public static final int     PT_OPEN = 1;
    
    /** Grid point is a boundary point with Dirichlet boundary conditions */
    public static final int     PT_DIRICHLET = 2;
    
    /** Grid point with Neumann boundary conditions, gradient in the first direction */
    public static final int     PT_NEUMANN1 = 3;
    
    /** Grid point with Neumann boundary conditions, gradient in the second direction */
    public static final int     PT_NEUMANN2 = 4;
    
    /** Grid point with Neumann boundary conditions, gradient in the third direction */
    public static final int     PT_NEUMANN3 = 5;
    
    
    /*
     *  Internal Data Structure
     */
    
    /**
     *  Contains the values stored at grid points
     */
    private class Point extends Grid.GridPt implements java.io.Serializable {
        /** ID for serializable version */
        private static final long serialVersionUID = 1L;
        
        
        /** grid point type */
        int     type = PT_UNDEFINED;
    
        /** source value at grid point */
        double  src = 0.0;
        
        /** auxiliary storage at grid point */
        double  aux = 0.0;
    }
    
    /**
     *  Grid cell data structure
     */
    private class Cell extends Grid.GridCell implements java.io.Serializable {
        /** ID for serializable version */
        private static final long serialVersionUID = 1L;        
    }
    

    
    
    /*
     *  Grid Attributes
     */
    
    /** number if relaxation iterations to achieve current solution */
    private int         m_intSolnIter = 0;
    
    /** residual error in the solution of Poisson's equation */
    private double      m_dblSolnErr = 0.0;

    
    
    /*
     *  Grid Initialization
     */
    
    /** 
     *  Allocate a new PotentialGrid 
     *
     *  @param  n1  number of grid points in first dimension
     *  @param  n2  number of grid points in second dimension
     *  @param  n3  number of grid points in third dimension
     *
     *  @exception  GridException   invalid size vector encountered
     */
    public PoissonGrid(int n1, int n2, int n3) throws GridException {
        super(n1, n2, n3);
    }
    
    /**
     *  Set the potential value at grid point given by index
     *
     *  @param  i       x dimension index of grid point
     *  @param  j       y dimension index of grid point
     *  @param  k       z dimension index of grid point
     *  @param  dblPot  value of the potential at grid point (i,j,k)
     */
    public void setPtPotential(int i, int j, int k, double dblPot)    {
        super.setPtValue(i,j,k, dblPot);
    }
    
    /**
     *  Set the type of the grid point given by index
     *
     *  @param  i       x dimension index of grid point
     *  @param  j       y dimension index of grid point
     *  @param  k       z dimension index of grid point
     *  @param  enmType type enumeration of grid point (i,j,k)
     */
    public void setPtType(int i, int j, int k, int enmType)    {
        Point  pt = this.getPt(i,j,k);
        
        pt.type = enmType;
    }
    
    /**
     *  Set the source value at grid point given by index
     *
     *  @param  i       x dimension index of grid point
     *  @param  j       y dimension index of grid point
     *  @param  k       z dimension index of grid point
     *  @param  dblSrc  value of the driving source at grid point (i,j,k)
     */
    public void setPtSource(int i, int j, int k, double dblSrc)    {
        Point  pt = this.getPt(i,j,k);
        
        pt.src = dblSrc;
    }
    
    
    
    /*
     *  Grid Operations
     */

    /**
     *  Solve for the potential on the grid using a gauss-seidel relaxation technique.
     *
     *  @param  intIterMax      maximum number of iterations
     *  @param  dblErrMax       maximum residual error
     *
     *  @return                 the residual error in the solution
     *
     *  @exception GridException  procedure did not converge to prescribed residual error
     */
    public double   solveCartesian(int intIterMax, double dblErrMax) throws GridException {
        int             i,j,k;          // loop control variables for the three dimensions
        int             I,J,K;          // loop boundaries for the three dimensions
        double          dblValNew;      // new potential value
        double          dblValPrev;     // previous potential value
        double[]        arrWts;         // array of grid weights
        Point           gpt;            // current grid point


        //  Initialize relaxation
        arrWts = this.relaxWeightsCartesian();
        m_dblSolnErr  = 0.0;
        m_intSolnIter = 0;

        I    = super.getGridSize().geti() - 1;
        J    = super.getGridSize().getj() - 1;
        K    = super.getGridSize().getk() - 1;

        //  Begin relaxation
        do      {

            // Average each grid point
            m_dblSolnErr = 0.0;

            for (i=1; i<I; i++)
                for (j=1; j<J; j++)
                    for (k=1; k<K; k++) {
                        gpt = getPt(i,j,k);

                        if (gpt.type==PT_DIRICHLET) continue;

                        dblValPrev =  gpt.val;
                        dblValNew  = (getPt(i+1,j,k).val + getPt(i-1,j,k).val)*arrWts[1]
                                   + (getPt(i+1,j,k).val - getPt(i-1,j,k).val)*arrWts[2]
                                   + (getPt(i,j+1,k).val + getPt(i,j-1,k).val)*arrWts[3]
                                   + (getPt(i,j,k+1).val + getPt(i,j,k-1).val)*arrWts[4]
                                   +  gpt.src*arrWts[0];

                        gpt.aux = dblValNew;

                        m_dblSolnErr += (dblValNew - dblValPrev)*(dblValNew - dblValPrev);
                    }

            // Set the new potential
            for (i=0; i<I; i++)
                for (j=0; j<J; j++)
                    for (k=0; k<K; k++) {
                        gpt = getPt(i,j,k);

                        if (gpt.type==PT_DIRICHLET) continue;

                        gpt.val =  gpt.aux;
                    }

        } while (m_dblSolnErr>dblErrMax && m_intSolnIter++<intIterMax);

        return m_dblSolnErr;
    }

    /**
     *  Solve for the potential on the grid using a gauss-seidel relaxation technique.
     *
     *  @param  intIterMax      maximum number of iterations
     *  @param  dblErrMax       maximum residual error
     *
     *  @return                 the residual error in the solution
     *
     *  @exception GridException  procedure did not converge to prescribed residual error
     */
    public double   solveCylindrical(int intIterMax, double dblErrMax) throws GridException {
        int             i,j,k;          // loop control variables for the three dimensions
        int             I,J,K;          // loop boundaries for the three dimensions
        double          dblValNew;      // new potential value
        double          dblValPrev;     // previous potential value
        double[]        arrWts;         // array of grid weights
        R3              pt;             // coordinates of a point on the grid
        Point           gpt;            // current grid point


        //  Initialize relaxation
        m_dblSolnErr  = 0.0;
        m_intSolnIter = 0;

        I    = super.getGridSize().geti() - 1;
        J    = super.getGridSize().getj() - 1;
        K    = super.getGridSize().getk() - 1;

        //  Begin relaxation
        do      {

            // Average each grid point
            m_dblSolnErr = 0.0;

            for (i=1; i<I; i++) {
                pt     = super.compPtCoords(i, 0, 0);
                arrWts = this.relaxWeightsCylindrical(pt.get1());
                
                for (j=1; j<J; j++)
                    for (k=1; k<K; k++) {
                        gpt = getPt(i,j,k);

                        if (gpt.type==PT_DIRICHLET) continue;

                        dblValPrev =  gpt.val;
                        dblValNew  = (getPt(i+1,j,k).val + getPt(i-1,j,k).val)*arrWts[1]
                                   + (getPt(i+1,j,k).val - getPt(i-1,j,k).val)*arrWts[2]
                                   + (getPt(i,j+1,k).val + getPt(i,j-1,k).val)*arrWts[3]
                                   + (getPt(i,j,k+1).val + getPt(i,j,k-1).val)*arrWts[4]
                                   +  gpt.src*arrWts[0];

                        gpt.aux = dblValNew;

                        m_dblSolnErr += (dblValNew - dblValPrev)*(dblValNew - dblValPrev);
                    }   // for k
            }           // for i

            
            // Process the theta=2Pi points
            for (i=1; i<I; i++) {
                pt     = super.compPtCoords(i, 0, 0);
                arrWts = this.relaxWeightsCylindrical(pt.get1());
                
                for (k=1; k<K; k++) {
                    
                    gpt = getPt(i, J, k);
                    
                    if (gpt.type == PT_DIRICHLET) continue;
                    
                        dblValPrev =  gpt.val;
                        dblValNew  = (getPt(i+1,J,k).val + getPt(i-1,J,k).val)*arrWts[1]
                                   + (getPt(i+1,J,k).val - getPt(i-1,J,k).val)*arrWts[2]
                                   + (getPt(i,0,k).val + getPt(i,J-1,k).val)*arrWts[3]
                                   + (getPt(i,J,k+1).val + getPt(i,J,k-1).val)*arrWts[4]
                                   +  gpt.src*arrWts[0];

                        gpt.aux = dblValNew;

                        m_dblSolnErr += (dblValNew - dblValPrev)*(dblValNew - dblValPrev);
                    
                }       // for k
            }           // for i
                    
            
            // Process the zero radius points
            R3      vecRes;         // vector of grid resolutions
            double  hr2, hz2;       // squares of the grid resolutions in r and z directions
            
            vecRes = super.getGridResolution();
            hr2    = vecRes.get1()*vecRes.get1();
            hz2    = vecRes.get3()*vecRes.get3();
            
            if (super.getGridOrigin().get1() == 0.0)    {
                R3  h = super.getGridResolution();
                
                for (k=1; k<K; k++) {
                    gpt = getPt(0, 0, k);

                    if (gpt.type==PT_DIRICHLET) continue;

                    dblValPrev = gpt.val;
                    dblValNew  = 0.0;
                    for (j=0; j<=J; j++) 
                        dblValNew += getPt(1, j, k).val;
                    dblValNew *= hz2/( (hr2 + hz2)*(J+1) );
                        
                    dblValNew  += (getPt(0,0,k+1).val + getPt(0,0,k-1).val)*( hr2/(2.0*(hr2+hz2)))
                               +  gpt.src*((hr2*hz2)/(2.0*(hr2+hz2)));

                    for (j=0; j<=J; j++)
                        getPt(0,j,k).aux = dblValNew;

                    m_dblSolnErr += (dblValNew - dblValPrev)*(dblValNew - dblValPrev);
                }
            }

            // Set the new potential
            for (i=1; i<I; i++)
                for (j=1; j<J; j++)
                    for (k=1; k<K; k++) {
                        gpt = getPt(i,j,k);

                        if (gpt.type==PT_DIRICHLET) continue;

                        gpt.val =  gpt.aux;
                    }

        } while (m_dblSolnErr>dblErrMax && m_intSolnIter++<intIterMax);

        return m_dblSolnErr;
    }
    
    
    /*
     *  Field Quantities
     */
    
    /**
     *  Compute and return interpolated potential at a point pt within grid definition.  
     *  The potential is calculated by linear interpolation of the potential values at
     *  each vertex of the cell containing point pt.
     *
     *  NOTES:
     *      - The grid may be in any coordinate system
     *      - The potential V must be determined for all points on the grid.
     *
     *  @param  pt      coordinates of field point within grid
     *
     *  @return         interpolated potential at pt
     *
     *  @exception  GridException   point pt is outside grid domain
     */
    public double   potential(R3 pt) throws GridException    {
        return super.interpolateValue(pt);
    }
    
    /**
     *  Compute and return the field in cartesian coordinates at point pt as generated 
     *  by the potential values on the grid.  
     *  
     *  NOTES:
     *      - The coordinates of the grid are assumed to be (x1,x2,x3)=(x,y,z)
     *      - The potential V must be determined for all points on the grid.
     *      - The generated field is given by F=-grad V where V is the potential on the grid.
     *
     *  @param  pt      field point to compute field
     *
     *  @return         vector field at point pt in cartesian (Fx,Fy,Fz)
     *
     *  @exception  GridException   field undefined or pt is outside grid
     */
    public R3   fieldCartesian(R3 pt)   throws GridException {
        R3      vecRes  = super.getGridResolution();
        R3      vecGrad = super.interpolateGradient(pt);

        vecGrad.set1( -vecGrad.get1()/vecRes.get1() );
        vecGrad.set2( -vecGrad.get2()/vecRes.get2() );
        vecGrad.set3( -vecGrad.get3()/vecRes.get3() );

        return vecGrad;
    }
        
    /**
     *  Compute and return the field in cylindrical coordinates at point pt as generated 
     *  by the potential values on the grid.  
     *
     *  NOTES:
     *     - The grid coordinates are assumed to be (x1,x2,x3)=(r,theta,z)
     *     - The potential V must be determined for all points on the grid.
     *     - The generated field is given by F=-grad V where V is the potential on the grid.
     *
     *  @param  pt      field point to compute field
     *
     *  @return         vector field at point pt in cylindrical (Fr,Ftheta,Fz)
     *
     *  @exception  GridException   field undefined or pt is outside grid
     */
    public R3 fieldCylindrical(R3 pt) throws GridException {
        R3      vecRes  = super.getGridResolution();
        R3      vecGrad = super.interpolateGradient(pt);

        vecGrad.set1( -vecGrad.get1()/vecRes.get1() );
        vecGrad.set2( -vecGrad.get2()/(vecRes.get2()*pt.get1()) );
        vecGrad.set3( -vecGrad.get3()/vecRes.get3() );
        return vecGrad;
    }    

    /**
     *  Compute and return the field in spherical coordinates at point pt as generated 
     *  by the potential values on the grid.  
     *
     *  NOTES:
     *     - The grid coordinates are assumed to be (x1,x2,x3)=(R,theta,phi)
     *     - The potential V must be determined for all points on the grid.
     *     - The generated field is given by F=-grad V where V is the potential on the grid.
     *
     *  @param  pt      field point to compute field
     *
     *  @return         vector field at point pt in spherical (FR,Ftheta,Fphi)
     *
     *  @exception  GridException   field undefined or pt is outside grid
     */
    public R3 fieldSpherical(R3 pt) throws GridException {
        R3      vecRes  = super.getGridResolution();
        R3      vecGrad = super.interpolateGradient(pt);

        vecGrad.set1( -vecGrad.get1()/vecRes.get1() );
        vecGrad.set2( -vecGrad.get2()/(vecRes.get2()*pt.get1()) );
        vecGrad.set3( -vecGrad.get3()/(vecRes.get3()*pt.get1()*Math.sin(pt.get2())) );
        return vecGrad;
    }    

    
    /*
     *  Internal Support - Grid Manipulation
     */
    
    
    /**
     *  Return grid point object located with index (i,j,k)
     */
    protected Point getPt(int i, int j, int k)   {
        return (Point)super.getGridPt(i, j, k);
    }

    
    /**  
     *  Override base class allocator in order to allocate modified grid point objects.  
     *  Allocates the array of grid points for the grid.
     *  
     *  @param  n1      size of the first dimension
     *  @param  n2      size of the second dimension
     *  @param  n3      size of the third dimension
     *
     *  @return         array of grid point objects
     *
     *  @exception  GridException   unable to allocate array
     */
    @Override
    protected Grid.GridPt[][][] allocatePts(int n1, int n2, int n3) throws GridException {
        return new Point[n1][n2][n3];
    }
    
    /**
     *  Override base class allocator in order to allocate modified grid cell objects.  
     *  Allocates the array of grid cells for the grid
     *
     *  @param  n1      size of the first dimension
     *  @param  n2      size of the second dimension
     *  @param  n3      size of the third dimension
     *  
     *  @return         array of grid cell objects
     *
     *  @exception  GridException   unable to allocate array
     */
    @Override
    protected Grid.GridCell[][][] allocateCells(int n1, int n2, int n3) throws GridException {
        return new Cell[n1][n2][n3];
    }


    
    /*
     *  Internal Support - Field Solver
     */
    
    /**
     *  Compute the weighting coefficients of this grid for a Gauss-Seidel relaxation 
     *  solution technique for Poisson's equation.  The coefficients are determined for
     *  a cartesian coordinate system where the grid dimensions are assumed
     *  (x1,x2,x3)=(x,y,z).
     *
     *  @return     vector (ws,w1,w2,w3) of weights in the 1st, 2nd, 3rd dimensions
     *              and source term weight
     */
    protected double[]  relaxWeightsCartesian()   {
        
        // Get the grid resolutions and their squares
        R3          vecRes;         // vector of grid spacings
        double      s1, s2, s3;     // squares of the grid point distances in all directions
        double      sD;             // exterior squared average of grid resolutions
        
        vecRes = super.getGridResolution();

        s1 = vecRes.get1()*vecRes.get1();
        s2 = vecRes.get2()*vecRes.get2();
        s3 = vecRes.get3()*vecRes.get3();
        
        sD = s1*s2 + s1*s3 + s2*s3;
        
        // Compute the weighting coefficients in cartesian coordinates
        double[]    arrW;   // gauss-seidel weights
        
        arrW = new double[4];
        arrW[0] = s1*s2*s3/(2.0*sD);
        arrW[1] = s2*s3/(2.0*sD);
        arrW[2] = s1*s3/(2.0*sD);
        arrW[3] = s1*s2/(2.0*sD);
        
        return arrW;
    }
        
    /**
     *  Compute the weighting coefficients of this grid for a Gauss-Seidel relaxation 
     *  solution technique for Poisson's equation.  The coefficients are determined for
     *  a cylindrical coordinate system where the grid dimensions are assumed
     *  (x1,x2,x3)=(r,theta,z).
     *
     *  @param  r   the radius at which the weights are determined
     *
     *  @return     vector (ws,w1,w1p,w2,w3)
     */
    protected double[]    relaxWeightsCylindrical(double r)   {
        
        // Get the grid resolutions and their squares
        R3          vecRes;         // vector of grid spacings
        double      r2;             // square of the radius
        double      s1, s2, s3;     // squares of the grid point distances in all directions
        double      sD;             // exterior squared average of grid resolutions
        
        vecRes = super.getGridResolution();

        r2 = r*r;
        
        s1 = vecRes.get1()*vecRes.get1();
        s2 = vecRes.get2()*vecRes.get2();
        s3 = vecRes.get3()*vecRes.get3();
        
        sD = s1*s2 + s1*s3/r2 + s2*s3;
        
        // Compute the weighting coefficients in cartesian coordinates
        double[]    arrW;   // gauss-seidel weights
        
        arrW = new double[5];
        arrW[0] = s1*s2*s3/(2.0*sD);
        arrW[1] = s2*s3/(2.0*sD);
        arrW[2] = arrW[1] * vecRes.get1()/(2.0*r);
        arrW[3] = s1*s3/(2.0*r2*sD);
        arrW[4] = s1*s2/(2.0*sD);
        
        return arrW;
    }
    
    
    
}
