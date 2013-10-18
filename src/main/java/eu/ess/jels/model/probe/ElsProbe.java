package eu.ess.jels.model.probe;

import xal.model.probe.EnvelopeProbe;
import xal.tools.annotation.AProperty.Units;
import xal.tools.beam.Twiss;
import Jama.Matrix;

public class ElsProbe extends EnvelopeProbe {
	private Matrix envelope = new Matrix(9,1,0.0);
	private Matrix normalized_emmitance = new Matrix(3,1,0.0);
	private double dlbFreq;
	private double dblCurrent;
	
	/*@Override
	public Trajectory createTrajectory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProbeState createProbeState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Probe copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ProbeState readStateFrom(DataAdaptor container)
			throws ParsingException {
		// TODO Auto-generated method stub
		return null;
	}*/
	
    /**
     * Set the bunch arrival time frequency.
     * 
     * @param f     new bunch frequency in <b>Hz</b>
     */
    public void setBunchFrequency(double f) {
        dlbFreq = f;
    }
 
    /**
     *  Set the total beam current.
     * 
     * @param   I   new beam current in <bold>Amperes</bold>
     */
    public void setBeamCurrent(double I)    { 
        dblCurrent = I; 
    };
    
    /**
     * Returns the bunch frequency, that is the frequency of 
     * the bunches need to create the beam current.
     * 
     * The bunch frequency f is computed from the beam current 
     * I and bunch charge Q as 
     *  
     *      f = I/Q
     *      
     * @return  bunch frequency in Hertz
     */
	@Units( "Hz" )
    public double getBunchFrequency()  {
        return this.dlbFreq;
    };
    
    /** 
     * Returns the total beam current 
     * 
     * @return  beam current in <b>amps</b>
     */
	@Units( "amps" )
    public double getBeamCurrent() { 
        return dblCurrent;  
     }


	public void initFromTwiss(Twiss[] twiss)
	{
		twiss[2].setTwiss(twiss[2].getAlpha(), twiss[2].getBeta()/Math.pow(getGamma(), 2), twiss[2].getEmittance());
		for (int i = 0; i<3; i++) {
			envelope.set(3*i+0,0,twiss[i].getBeta());
			envelope.set(3*i+1,0,twiss[i].getAlpha());
			envelope.set(3*i+2,0,twiss[i].getGamma());
			normalized_emmitance.set(i,0,twiss[i].getEmittance()*getBeta()*getGamma());
		}		
	}
	
	public Matrix getEnvelope() {
		return envelope;
	}

	public void setEnvelope(Matrix envelope) {
		this.envelope = envelope;
	}

	@Override
	public Twiss[] getTwiss()
	{
		Twiss[] twiss = new Twiss[3];
		for (int i=0; i<3; i++)
			twiss[i] = new Twiss(envelope.get(3*i+1,0), envelope.get(3*i,0), normalized_emmitance.get(i,0)/(getBeta()*getGamma()));
		twiss[2].setTwiss(twiss[2].getAlpha(), twiss[2].getBeta()*Math.pow(getGamma(), 2), twiss[2].getEmittance());
		return twiss;
	}
}
