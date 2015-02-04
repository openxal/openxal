/*
 * Ensemble.java
 *
 * Created on September 17, 2002, 3:28 PM
 *
 *  Modified:
 *      02/06/03 CKA - added electromagnetic properties
 */

package xal.tools.beam.ens;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;

import java.util.TreeSet;
import java.util.Iterator;
import java.util.Comparator;


import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.IConstants;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.math.r3.R3;


/**
 *  Represents an ensemble of charged particles and the collective properties associated
 *  with such physical object.
 *
 * @author  CKAllen
 */
public class Ensemble implements Serializable {

    
    /** Serialization version */
    private static final long serialVersionUID = 1L;


    
    /*
     *  Helper Classes
     */


    /**
     *  Implements Comparator interface for the internal TreeSet container which stores
     *  the Particle objects of the Ensemble.  This comparator sorts the particles by the
     *  l2 norm of particles' phase space coordinates.  Thus, particles closer to the origin
     *  are located first in the tree.
     */
    class SortByNorm implements Comparator<Particle>, Serializable {
        
        /** Serialization version */
        private static final long serialVersionUID = 1L;

        /**
         * Compares the phase norms of the two particles.
         * 
         * @since Apr 19, 2011
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Particle p1, Particle p2)  {
            
            PhaseVector v1 = p1.getPhase();
            PhaseVector v2 = p2.getPhase();
            
            double n1 = v1.norm2();
            double n2 = v2.norm2();
            
            if (n1 < n2)  return -1;
            //if (n1 == n2) return 0;
            else          return +1;
        };
        
        
        /*
         * Object Overrides
         */
        
        /**
         * Equality of comparitors. This is also part of the <code>Comparitor</code>
         * interface.
         * 
         * @since Apr 19, 2011
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        /**
         *
         * @see java.lang.Object#hashCode()
         *
         * @since  Feb 3, 2015   by Christopher K. Allen
         */
        @Override
        public int hashCode() {
            return super.hashCode();
        };
        
        
    };
    
    
    /*
     *  Global Operations
     */
    
    /**
     *  Creates an Ensemble from a file store.
     *
     *  @param  strUrl  the URL of the file store
     *
     *  @return     new Ensemble instance corresponding to file store
     *
     *  @exception  IOException     unable to read file at URL
     */
    public static Ensemble restoreUrl(String strUrl)  
        throws IOException  
    {
        Ensemble        ensNew = new Ensemble();
        
        if (!ensNew.load(strUrl))
            throw new IOException("Ensemble#restoreUrl() - unable to read file " + strUrl);
        
        return ensNew;
    }
       
    
    /*
     *  Attributes
     */
    
    /** The container for the ensemble coordinates */
    private TreeSet<Particle>     m_setEns;
    
    
    
    /*
     *  Ensemble Initialization
     */
    
    /** 
     *  Creates a new instance of Ensemble 
     */
    public Ensemble() {
        m_setEns = new TreeSet<Particle>(new SortByNorm());
    };
    
    /**
     *  Create a deep copy clone of an ensemble object
     *  @param  ens ensemble object to be deep copied
     */
    public Ensemble(Ensemble ens)   {
        m_setEns = this.deepCopyParticles(ens);
    };
    
    
    /**
     *  Create a deep copy object of this ensemble
     */
    public Ensemble deepCopy()  {
        return new Ensemble(this);
    };
    

    /**
     *  Add a particle to ensemble
     */
    public void add(Particle p) {
        m_setEns.add(p);
    };

    
    /**
     *  Iterate through ensemble
     */
    public Iterator<Particle> iterator()  {
        return m_setEns.iterator();
    };

    
    
    /*
     *  Statistical Properties
     */

    
    /**
     *  Get size of ensemble
     */
    public int  getCount()  {
        return m_setEns.size();
    };
    

    /**
     *  Compute the centroid of the ensemble
     *
     *  @return     homogeneous phase space coordinates of ensemble centroid
     */
    public PhaseVector phaseMean() {
        int         N      = this.getCount();
        PhaseVector vecSum = new PhaseVector();
        Iterator<Particle> iter = this.iterator();
        
        while (iter.hasNext())  {
            Particle p = iter.next();
            vecSum.plusEquals(p.getPhase());
        }
        
        vecSum.timesEquals(1.0/N);
        return vecSum;
    };

    
    /**
     *  Get the correlation matrix of the ensemble in homogeneous coordinates
     *
     *  @return         the 7x7 correlation matrix of the ensemble distribution
     */
    public CovarianceMatrix phaseCovariance()    {
        int             N        = this.getCount();
        CovarianceMatrix     matSigma = new CovarianceMatrix();
        Iterator<Particle> iter = this.iterator();
        
        while (iter.hasNext()) {
            Particle p = iter.next();
            
            PhaseVector  z  = p.getPhase();
            PhaseMatrix  matOuter = z.outerProd(z);
        
            matSigma.plusEquals(matOuter);
        }
        
        matSigma.timesEquals(1.0/N);
        return matSigma;
    };
    
    
    /**
     *  Compute the rms emittances in each phase plane
     *
     *  @return     three-element array containing (ex,ey,ez)
     */
    public double[] rmsEmittances()  {
        double[] arrEmit = new double[3];
        PhaseMatrix matSig = this.phaseCovariance();

        arrEmit[0] = Math.sqrt( matSig.getElem(0,0)*matSig.getElem(1,1) - matSig.getElem(0,1)*matSig.getElem(1,0) );
        arrEmit[1] = Math.sqrt( matSig.getElem(2,2)*matSig.getElem(3,3) - matSig.getElem(2,3)*matSig.getElem(3,2) );
        arrEmit[2] = Math.sqrt( matSig.getElem(4,4)*matSig.getElem(5,5) - matSig.getElem(4,5)*matSig.getElem(5,4) );
        
        return arrEmit;
    };
    
    
    
    /*
     *  Electromagnetic Properties
     */
    
    
    /**
     *  Get the total current of the ensemble.  
     *  <p>
     *  NOTE:
     *      If the momentum components of the particle phases are not the velocities,
     *      the return value must be scaled.  For example, if the trace
     *      space values are used then the returned value must be multiplied by 
     *      beta*c.
     */
    public R3 totalCurrent()  { 
        double          q;          // particle charge
        PhaseVector     vecPhase;   // particle phase coordinates
        double          Ix, Iy, Iz; // current components
        Iterator<Particle> iter;       // ensemble particle iterator
       
        Ix = Iy = Iz = 0.0;
        iter = this.iterator();
        
        while (iter.hasNext())  {
            Particle    p = iter.next();
            
            vecPhase = p.getPhase();
            q        = p.getCharge();
            
            Ix += q*vecPhase.getxp();
            Iy += q*vecPhase.getyp();
            Iz += q*vecPhase.getzp();
        }
        
        return new R3(Ix, Iy, Iz);
    };
    
    /**
     *  Get the total charge of the ensemble
     */
    public double totalCharge()   { 
        double      Q;          // total ensemble charge
        Iterator<Particle> iter;       // ensemble particle iterator
       
        Q    = 0.0;
        iter = this.iterator();
        
        while (iter.hasNext())  {
            Particle    p = iter.next();
            
            Q += p.getCharge();
        }
        
        return Q;
    };

    /**
     *  Computes the Coulomb potential of the ensemble at the given field point.
     * <p>
     *  The potentials from every ensemble particle are summed at the field point
     *  and no averaging is performed.  Thus, vary large potential values may occur
     *  if the field point is sufficiently near an ensemble particle.
     *
     *  @param  ptFld   field point to evaluate the potential
     *
     *  @return         the coulomb potential in MKS units
     */
    public double   potentialSummation(R3 ptFld) {
        double      dblVol;         // coulomb potential
        Iterator<Particle> iter;           // ensemble particle iterator
       
        dblVol = 0.0;
        iter   = this.iterator();
        
        while (iter.hasNext())  {
            Particle    p = iter.next();
            
            dblVol += p.electricPotential(ptFld);
        }
        return dblVol;
    }
        
    /**
     *  Computes the electric potential from a quadrupole multipole expansion.  This 
     *  approximation is accurate at points outside the distribution, becoming more 
     *  accurate as the distance increases.  Therefore, note that returned potential 
     *  is inaccurate for field points inside the particle expanse, with a singularity 
     *  located at the ensemble centroid.
     *  <p>
     *  Use this method when the ensemble particles all have the same charge, since the
     *  covariance matrix is used to compute the moments of the multipole expansion.  The
     *  covariance matrix is with respect to the phase coordinates and, thus, all particles
     *  receive equal weighting.
     *  <p>
     *  This method uses the multipoles up to the quadrupole moment, as taken from the
     *  covariance matrix.  The expansion is taken from the centroid of the distribution,
     *  as such the dipole moments are zero at that point.  Thus, the result potential field
     *  appears to be generated from the centroid as a point particle along with the quadrupole
     *  fields due to the cross moments.
     *  <p>
     *  This method is intended for determining the potential at many fields points for
     *  the same ensemble configuration.  To avoid repeated calculations of the covariance
     *  matrix (using getSigma()) this matrix should be called once and passed to this
     *  method on each invocation with the same ensemble configuration.  
     *
     *  @param  pt      field point to evaluate the potential
     *
     *  @return         potential from quadrupole expansion of the ensemble
     */
    public double   potentialQuadExpansion(R3 pt, double Q, PhaseMatrix matSigma)   {

        // Get the field and source points then compute the distance
        double      xf, yf, zf;     // field points
        double      xc, yc, zc;     // ensemble centroid location
        double      dx, dy, dz;     // coordinate displacements of the field and centroid points
        double      R;              // distance from the field point to the centroid
        double      R2, R5;         // distance squared, distance to the fifth power
        
        xf = pt.get1();               yf = pt.get2();               zf = pt.get3();
        xc = matSigma.getElem(6, 0);  yc = matSigma.getElem(6, 2);  zc = matSigma.getElem(6, 4);
        dx = xf - xc;                 dy = yf - yc;                 dz = zf - zc;
        
        R2 = dx*dx + dy*dy + dz*dz;
        R  = Math.sqrt( R2 );
        R5 = R2*R2*R;

        
        // Compute the central second moments
        double      xx,  xy,  yy,  yz,  zx,  zz;    // second moments of the ensemble
        double      xxc, xyc, yyc, yzc, zxc, zzc;   // central second moments of the ensemble
        
        xx = matSigma.getElem(0, 0);    xxc = xx - xc*xc;
        xy = matSigma.getElem(0, 2);    xyc = xy - xc*yc;
        yy = matSigma.getElem(2, 2);    yyc = yy - yc*yc;
        yz = matSigma.getElem(2, 4);    yzc = yz - yc*zc;
        zx = matSigma.getElem(4, 0);    zxc = zx - zc*xc;
        zz = matSigma.getElem(4, 4);    zzc = zz - zc*zc;
        
        
        // Compute the quadrupole expansion potential
        double      V;          // electric potential
        
        V  = (3.0*dx*dx - R2)*xxc + (3.0*dy*dy - R2)*yyc + (3.0*dz*dz - R2)*zzc;
        V += 3.0*(dx*dy*xyc + dy*dz*yzc + dz*dx*zxc);
        V /= R5;
        V += 1.0/R;

        V *= Q/(4.0*Math.PI*IConstants.Permittivity);
        
        return V;
    }
        
    
    /*
     *  persistence Methods
     */
    
    
    /**
     *  Populate the ensemble from a data file.  Particle objects are loaded from
     *  persistent phase space data in file.
     *
     *  @param  strFile descriptor of file containing persistent data
     *
     *  @return         true if successfully recovered ensemble from file
     */
    public boolean load(String strFile)  {
        return this.load( new File(strFile) );
    }
    
    
    /**
     *  Populate the ensemble from a data file.  Particle objects are loaded from
     *  persistent phase space data in file.
     *
     *  @param  file    descriptor of file containing persistent data
     *
     *  @return         true if successfully recovered ensemble from file
     */
    @SuppressWarnings("unchecked")
    public boolean load(File file)  {
        try {
            FileInputStream     is = new FileInputStream(file);
            ObjectInputStream   p  = new ObjectInputStream(is);
        
            m_setEns = (TreeSet<Particle>)p.readObject();
            is.close();
            return true;
            
        } catch (FileNotFoundException e) {
            return false;
            
        } catch (IOException e) {
            return false;
            
        } catch (ClassNotFoundException e) {
            return true;
        
        }
        
    };
    
    /**
     *  Save ensemble state to persisten disk file.  All ensemble particles have
     *  phase space coordinates saved.
     *
     *  @param  file    file containing persistent data
     *
     *  @return         true if successfully saved ensemble to file
     */
    public boolean save(File file)  {
        try {
            FileOutputStream    os = new FileOutputStream( file );
            ObjectOutputStream  p  = new ObjectOutputStream( os );

            p.writeObject(m_setEns); 
            p.flush();
            os.close();
            return true;
            
        } catch (FileNotFoundException e) {
            return false;
            
        } catch (IOException e) {
            return false;
            
        }
    };
    
    
    
    
    /*
     *  Testing and Debugging
     */
    
    
    /**
     *  Print out contents of the ensemble. 
     *  WARING - since typical ensembles contain >10^3 particles this method
     *          could absorb a large amount of system resources
     */
    public void print(PrintWriter os)   {
        int      iPar = 0;
        Iterator<Particle> iter = this.iterator();
        
        os.println("  Ensemble Size = " + this.getCount());
        while (iter.hasNext()) {
            Particle p = iter.next();
            
            os.println("  Particle " + iPar);
            p.print(os);
            
            iPar++;
        }
        
    }
    
    
    
    /**
     *  Test driver for testing Ensemble class.
     */
    static public void main(String arg[])   {
        PrintWriter     osLog = new PrintWriter(System.out);

        // Test persistent storage mechanism
        testPersistence(osLog);
        osLog.close();
    }
    
    
    /**
     *  Test the file persistence mechanism. 
     *
     *  @param  osLog   output stream to send logging information
     */
    static public void testPersistence(PrintWriter osLog) {
        final String    strFileTest = "TestCoords.ens";  // persistence file name

        
        // Create test ensemble 
        Ensemble ens1 = new Ensemble();

        Particle p1 = new Particle();
        Particle p2 = new Particle();
        
        p1.setMass(1.0);
        p1.setCharge(1.0);
        p1.getPhase().setElem(0, 0.001);
        p1.getPhase().setElem(1, 0.020);
        
        p2.setMass(1.0);
        p2.setCharge(-1.0);
        p2.getPhase().setElem(2, -0.001);
        p2.getPhase().setElem(3, -0.020);
        
        ens1.add(p1);
        ens1.add(p2);
        
        // Save test ensemble to disk
        File    fileSave = new File(strFileTest);
        if (!ens1.save( fileSave )) {
            osLog.println("Ensemble save failed.");
            return;
        }

        
        // Recover test ensemble from disk
        File     fileLoad = new File(strFileTest);
        Ensemble ens2 = new Ensemble();
        
        if (!ens2.load(fileLoad)) {
            osLog.println("Ensemble recovery failed.");
            return;
        }
        
        
        // Print out test ensemble 
        osLog.println("Original Ensemble");
        ens1.print(osLog);
        osLog.println("");
        
        osLog.println("Recovered Ensemble");
        ens2.print(osLog);
        osLog.flush();
    }





    /*
     *  Support Functions
     */
   

    
    /**
     *  Make a deep copy of the ensemble's particle container.
     *
     *  @param  ens     ensemble whose particles are to be cloned
     *
     *  @return         TreeSet container of cloned particles
     */
    protected TreeSet<Particle> deepCopyParticles(Ensemble ens)   {
        TreeSet<Particle>  set = new TreeSet<Particle>();
        Iterator<Particle> iter = ens.iterator();
        while (iter.hasNext())  {
            Particle p = iter.next();
            
            set.add( new Particle(p) );
        }
        
        return set;
    };
    
    
}
