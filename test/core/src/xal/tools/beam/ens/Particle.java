/*
 * Particle.java
 *
 * Created on September 17, 2002, 4:21 PM
 */

package xal.tools.beam.ens;

import java.io.PrintWriter;

import xal.tools.beam.IConstants;
import xal.tools.beam.PhaseVector;
import xal.tools.math.r3.R3;


/**
 * Represents a particle in six-dimesional phase space.
 *
 * @author  CKAllen
 */
public class Particle implements java.io.Serializable {

    
    /** Serialization version */
    private static final long serialVersionUID = 1L;

    
    /*
     *  Mathematical and Physical Constants
     */
    
    /** Coefficient for electrical properties */
    public static final double s_dblFacElec = 1.0/(4.0*Math.PI*IConstants.Permittivity);
    
    /** Coefficient for magnetic properties */
    public static final double s_dblFacMag = 1.0/(4.0*Math.PI*IConstants.Permeability);
        

    /*
     *  Particle Attributes
     */
    
    /** Particle mass */
    private double  m_dblMass = 0.0;
    
    /** Particle charge */
    private double  m_dblChrg = 0.0;
    
    /** Particle (homogeneous) phase space coordinates */
    private PhaseVector  m_vecPhase = null;
    
    

    
    /** 
     *  Creates a new instance of Particle 
     */
    public Particle() {
        m_vecPhase = new PhaseVector();
    };
    
    /** 
     *  Creates a new instance of Particle
     *  @param  q   particle charge
     *  @param  m   particle mass
     *  @param  z   phase space coordinates (homogeneous coordinates)
     */
    public Particle(double q, double m, PhaseVector z) {
        m_dblChrg = q;
        m_dblMass = m;
        m_vecPhase = z;
    };
    
    /**
     *  Creates a new instance, a deep copy, of this object
     *  @param  p   particle object to be deep copied
     */
    public Particle(Particle p) {
        this.m_dblChrg = p.m_dblChrg;
        this.m_dblMass = p.m_dblMass;
        this.m_vecPhase = new PhaseVector(p.m_vecPhase);
    }
    
    
    /**
     *  Perform a deep copy of this particle object
     */
    public Particle copy()  {
        return new Particle(this);
    };

    
    
    /*
     *  Particle Properties
     */
    
    /**
     *  Set mass of particle
     */
    public void setMass(double m)   { m_dblMass = m; };
    
    /**
     *  Set charge of particle
     */
    public void setCharge(double q) { m_dblChrg = q; };
    
    /**
     *  Set (homogeneous) phase space coordinates of particle
     */
    public void setPhase(PhaseVector z)  { m_vecPhase = z; };
    
    /**
     *  Get charge of particle
     */
    public double getCharge()   { return m_dblChrg; };
    
    /**
     *  Get mass of particle
     */
    public double getMass()     { return m_dblMass; };
    
    /**
     *  Get the position vector of the particle
     */
    public R3   getPosition()   { return this.getPhase().getPosition(); };
    
    /**
     *  Get the velocity vector of the particle
     */
    public R3   getMomentum()   { return this.getPhase().getMomentum(); };
    
    /**
     *  Get the entire (homogeneous) phase space coordinates of particle
     */
    public PhaseVector getPhase()   { return m_vecPhase; };
    
    
    
    /*
     *  Electromagnetic Properties
     */
    
    
    /**
     *  Computes the Coulomb potential of the particle at the given field point.
     *  Note that very large potential exist sufficiently close to the particle.
     *
     *  To avoid numerical singularites the particle is assumed to have a finite
     *  radius equal to the "classical proton radius", which is ~1e-18.  Thus, the 
     *  potential of a uniform sphere is substituted for field points closer than
     *  this radius.
     *
     *  @param  ptFld   field point to evaluate the potential
     *
     *  @return         the coulomb potential in volts
     */
    public double   electricPotential(R3 ptFld) {
        return this.electricPotential(ptFld, IConstants.ProtonRadius);
    }
    
    
    /**
     *  Computes electric potential assuming the particle was an uniform sphere of 
     *  charge with radius R.  Giving the particle a finite size removes the singularity
     *  in the potential at the particle position.
     *
     *  This function is also useful in averaging the potential over grids.  By giving the
     *  radius the same order as the grid dimensions, the particle appears to smear over
     *  the grid.  That is, the radius is a numerical tuning parameter for averaging.
     *
     *  @param  ptFld       field point to evaluate the potential
     *  @param  R           radius of finite particle
     *
     *  @return             electric potential of "smeared" particle at field point in volts
     */
    public double electricPotential(R3 ptFld, double R) {
        
        // Compute distance between field and source point
        R3          ptSrc;      // source location of this charge
        R3          vecDis;     // displacement vector between source and field points
        double      dblDis;     // distance between field and source point
        
        ptSrc  = getPosition();
        vecDis = ptFld.minus(ptSrc);
        dblDis = vecDis.norm2();
        
        
        // Return coulomb potential if field point is more distant than R
        if (dblDis > R)         
            return s_dblFacElec*getCharge()/dblDis;
        
        
        // Field point is within R, must use potential of a sphere
        double      r;          // normalized distance, must be <1
        double      V;          // electric potential
        
        r = dblDis/R;
        V = 0.5*s_dblFacElec*getCharge()/R;
        V = V*(3.0 - r*r);
        
        return V;
    };
    
    
    /**
     *  Computes the Coulomb electric field of the particle at the given field point.
     *  Note that very large fiels exist sufficiently close to the particle.
     *
     *  To avoid numerical singularites the particle is assumed to have a finite
     *  radius equal to the "classical proton radius", which is ~1e-18.  Thus, the 
     *  field of a uniform sphere is substituted for field points closer than
     *  this radius.
     *
     *  @param  ptFld   field point to evaluate the potential
     *
     *  @return         electric field vector in volts/meter
     */
    public R3   electricField(R3 ptFld) {
        return this.electricField(ptFld, IConstants.ProtonRadius);
    }
    
    
    /**
     *  Computes electric field assuming the particle is an uniform sphere of 
     *  charge with radius R.  Giving the particle a finite size removes the singularity
     *  in the field at the particle position.
     *
     *  This function is also useful in averaging the field over grids.  By giving the
     *  radius the same order as the grid dimensions, the particle appears to smear over
     *  the grid.  That is, the radius is a numerical tuning parameter for averaging.
     *
     *  @param  ptFld       field point to evaluate the electric field
     *  @param  R           radius of finite particle
     *
     *  @return             electric field vector of "smeared" particle in volts/meter
     */
    public R3   electricField(R3 ptFld, double R) {
        
        // Compute distance between field and source point
        R3          ptSrc;      // source location of this charge
        R3          vecDis;     // displacement vector between source and field points
        double      dblDis;     // distance between field and source point
        
        ptSrc  = getPosition();
        vecDis = ptFld.minus(ptSrc);
        dblDis = vecDis.norm2();
        

        // Compute the electric field
        double      dblCoef;            // scalar coefficient of displacement vector
        
        dblCoef = s_dblFacElec*getCharge();
        
        if (dblDis > R) {       // Return coulomb electric field if field point is more distant than R
            dblCoef *= 1.0/(dblDis*dblDis*dblDis);
            return vecDis.times(dblCoef);
        }

        
        // Field point is within R, must use field of a sphere
        dblCoef *= 1.0/(R*R*R);
        
        return vecDis.times(dblCoef);
    }    
    
    
    /**
     *  Computes magnetic field assuming the particle is an uniform sphere of 
     *  charge with radius R.  Note that we compute magnetic field H, and not the
     *  magnetic flux vector B.  Giving the particle a finite size removes the singularity
     *  in the field at the particle position.  Note that very large fiels exist sufficiently 
     *  close to the particle.
     *
     *  To avoid numerical singularites the particle is assumed to have a finite
     *  radius equal to the "classical proton radius", which is ~1e-18.  Thus, the 
     *  field of a uniform sphere is substituted for field points closer than
     *  this radius.
     *
     *  Note that if the momentum coordinates of the particle's phase space vector are
     *  (x',y',z') then the returned field must be multiplied by beta-c, since the 
     *  mechanical momentum coordinates are assumed in the field calculations.
     *
     *  @param  ptFld       field point to evaluate the electric field
     *
     *  @return             magnetic field vector of "smeared" particle in Amperes/Meter
     */
    public R3   magneticField(R3 ptFld) {
        return magneticField(ptFld, IConstants.ProtonRadius);
    }    
    
    
    /**
     *  Computes magnetic field assuming the particle is an uniform sphere of 
     *  charge with radius R.  Note that we compute magnetic field H, and not the
     *  magnetic flux vector B.  Giving the particle a finite size removes the singularity
     *  in the field at the particle position.
     *
     *  Note that if the momentum coordinates of the particle's phase space vector are
     *  (x',y',z') then the returned field must be multiplied by beta-c, since the 
     *  mechanical momentum coordinates are assumed in the field calculations.
     *
     *  This function is also useful in averaging the field over grids.  By giving the
     *  radius the same order as the grid dimensions, the particle appears to smear over
     *  the grid.  That is, the radius is a numerical tuning parameter for averaging.
     *
     *  @param  ptFld       field point to evaluate the electric field
     *  @param  R           radius of finite particle
     *
     *  @return             magnetic field vector of "smeared" particle in Amperes/Meter
     */
    public R3   magneticField(R3 ptFld, double R) {
        
        // Compute distance between field and source point
        R3          ptSrc;      // source location of this charge
        R3          vecVel;     // velocity vector of particle
        R3          vecDis;     // displacement vector between source and field points
        double      dblDis;     // distance between field and source point
        
        ptSrc  = getPosition();
        vecVel = getMomentum();
        vecDis = ptFld.minus(ptSrc);
        dblDis = vecDis.norm2();
        

        // Compute the magnetic field
        double      dblCoef;            // scalar coefficient of field vector
        R3          H;                  // magnetic field vector
        
        dblCoef = getCharge()/(4.0*Math.PI);
        H = vecVel.times(vecDis);
        
        if (dblDis > R) {       // Return Biot/Savart magnetic field if field point > R
            dblCoef *= 1.0/(dblDis*dblDis*dblDis);
            return H.times(dblCoef);
        }

        
        // Field point is within R, must use field of a sphere
        dblCoef *= 1.0/(R*R*R);
        
        return H.times(dblCoef);
    }    
    
    

    
    /*
     *  Debugging
     */
    
    /**
     * Print out the properties of this particle.
     * 
     * @param os    output stream receiving text description
     *
     * @author Christopher K. Allen
     * @since  Apr 15, 2011
     */
    public  void print(PrintWriter os) {
        
        os.println("  mass   = " + this.getMass() );
        os.println("  charge = " + this.getCharge() );
        os.print("  coords = "); this.getPhase().println(os);
    };
        
}
