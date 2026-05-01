/*
 * Constants.java
 *
 * Created on January 22, 2003, 6:08 PM
 */

package xal.tools.beam;

/**
 *
 * @author  Christopher Allen
 */
public interface IConstants {
    
    /*
     *  Physical Constants
     */
    
    /** Speed of light in a vacuum (meters/second) */
    static public final double LightSpeed = 299792458;   
    
    /** The unit electric charge (Farads) */
    static public final double UnitCharge = 1.602e-19;
    
    /** Electric permittivity of free space (Farad/meter) */
    static public final double Permittivity = 8.854187817e-12;
  
    /** Magnetic permeability of free space (Henries/meter) */
    static public final double Permeability = 4.0*Math.PI*1.0e-7;
    
    
    
    /** Rest mass of an electron (Kilograms) */
    static public final double ElectronMass = 9.109e-31;
    
    /** Rest mass of a proton (Kilograms) */
    static public final double ProtonMass = 1.6762e-27;
    
    /** Rest energy of an electron  (electron volts) */
    static public final double ElectronEnergy = 5.11e5;
    
    /** Rest energy of a proton (electron volts) */
    static public final double ProtonEnergy = 9.38272e8;
    
    
    
    /** Bohr radius of a hydrogen atom (meters) */
    static public final double BohrRadius = 5.29177e-11;
    
    /** "Classical" radius of a proton: radius where electrical energy equals rest energy */
    static public final double ProtonRadius = 9.1740e-19;
    
}
