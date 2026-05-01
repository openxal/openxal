/*
 * MagnetType.java
 *
 * Created on January 23, 2002, 3:49 PM
 */

package xal.smf.impl.qualify;

/**
 *
 * @author  tap
 */
public interface MagnetType extends ElementType {
    /* poles */
    
    // orientation constants
    public final static int NO_ORIENTATION = 0;
    public final static int HORIZONTAL = 1;
    public final static int VERTICAL = 2;
    
    // magnet type constants
    public final static String DIPOLE = "D";

    public final static String QUADRUPOLE = "Q";
    public final static String QUAD = QUADRUPOLE;

    public final static String SEXTUPOLE = "S";
    public final static String SEXT = SEXTUPOLE;
    
    public final static String OCTUPOLE = "Oct";
    public final static String OCT = OCTUPOLE;
    
    public final static String SOLENOID = "SOL";
    public final static String SOL = SOLENOID;    
    
    public final static String[] poles = {DIPOLE, QUADRUPOLE, SEXTUPOLE, OCTUPOLE};       
    
    // public methods
    public boolean isPole(String compPole);
    public int getOrientation();
    public boolean isHorizontal();
    public boolean isVertical();
    public boolean isSkew();
    public boolean isPermanent();
    public boolean isCorrector(); 
}

