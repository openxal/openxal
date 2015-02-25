package xal.extension.extlatgen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xal.model.probe.EnvelopeProbe;
import xal.model.probe.Probe;
import xal.sim.slg.Element;
import xal.sim.slg.Lattice;
import xal.sim.slg.LatticeError;
import xal.sim.slg.LatticeFactory;
import xal.sim.slg.LatticeIterator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.*;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.beam.TraceXalUnitConverter;

public class ImpactGenerator {
	/** speed of light constant in 10^9 m/s */
	final static double LIGHT_SPEED = 0.2997925;
	
	/** default number format */
	final static NumberFormat NUMBER_FORMAT;
    
	/** Probe for initial condition */
	protected Probe<?> myProbe;

	protected java.util.List<AcceleratorSeq> _sequenceChain = null;
    
	/** for design values */
	public static final int PARAMSRC_DESIGN = 2;
    
	/** for live data from the machine */
	public static final int PARAMSRC_LIVE = 3;
    
	protected String myLatticeName = null;
    
	/** sign of particle charge */
	protected double Q = -1.;
	
	/** beam initial condition */
	protected double beamci[] = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    
	/** indicates whether to use design bend angles regardless of the specified data source */
	private boolean _useDesignBendAngles;
	
	// device types
	/** drift */
	static final int DRIFT = 0;
	/** quadrupole */
	static final int QUAD = 1;
	/** constant focusing */
	static final int CF = 2;
	/** solenoid */
	static final int SOLENOID = 3;
	/** dipole */
	static final int DIPOLE = 4;
	/** DTL */
	static final int DTL = 101;
	/** CCDTL */
	static final int CCDTL = 102;
	/** CCL */
	static final int CCL = 103;
	/** RF cavity */
	static final int SC = 104;
	/** Solenoid with RF cavity */
	static final int SOLRF = 105;
	/** user defined RF cavity */
	static final int EMFLD = 110;
	/** default aperture size = 0.014 m */
	static final double APER = 0.014;
	
	// static initializer
	static {
		NUMBER_FORMAT = NumberFormat.getNumberInstance();
		((DecimalFormat) NUMBER_FORMAT).setMaximumFractionDigits( 8 );
	}
	
	/** Constructor */
	public ImpactGenerator( java.util.List<AcceleratorSeq> sequenceChain, EnvelopeProbe envProbe ) {
		this( null, sequenceChain, envProbe );
	}
	
	/** Constructor */
	public ImpactGenerator(String latticeName, java.util.List<AcceleratorSeq> sequenceChain, EnvelopeProbe envProbe) {
		this( latticeName, sequenceChain, (Probe)envProbe );
	}
    
	/** Constructor */
	public ImpactGenerator( final String latticeName, final java.util.List<AcceleratorSeq> sequenceChain, final Probe<?> envProbe) {
		myLatticeName = latticeName;
		myProbe = envProbe;
		_sequenceChain = sequenceChain;
		_useDesignBendAngles = true;
	}
    
	/** Set whether to use the design bend angles independent of the specified data source */
	public void setUseDesignBendAngles( final boolean useDesignBendAngles ) {
		_useDesignBendAngles = useDesignBendAngles;
	}
	
	
	/** set the beam initial condition */
	public void setBeamCI(double[] newBeamCI) {
		beamci = newBeamCI;
	}
	
	/**
	 * generate the IMPACT input file
	 * @param deviceDataSource data source for the device's fields
	 */
	public void createImpactInput( final AbstractDeviceDataSource deviceDataSource ) throws IOException {
		createImpactInput( deviceDataSource, null );
	}
	
	/**
	 * generate the IMPACT input file
	 * @param deviceDataSource data source for the device's fields
	 */
	public void createImpactInput( final AbstractDeviceDataSource deviceDataSource, final File outputFile ) throws IOException {
		// select the data source for bends depending on whether the flag has been set to use design bend angles
		final AbstractDeviceDataSource bendDataSource = _useDesignBendAngles ? AbstractDeviceDataSource.getDesignDataSourceInstance() : deviceDataSource;
        
		if (myLatticeName == null) {
			myLatticeName = _sequenceChain.get(0).getId() + "-" + _sequenceChain.get( _sequenceChain.size() - 1 ).getId();
		}
        
		File impact_file = outputFile != null ? outputFile : new File( "test.in" );
		System.out.println( "Exporting IMPACT optics to file: " + impact_file.getAbsolutePath() );
		final FileWriter IMPACT_WRITER = new FileWriter( impact_file );
		final Date today = new Date();
        
		double momentum = RelativisticParameterConverter.computeMomentumFromEnergies( myProbe.getKineticEnergy(), myProbe.getSpeciesRestEnergy() ) / 1.e9;
		System.out.println( "momentum = " + momentum );
        
		// Q = myProbe.getSpeciesCharge()/1.602e-19;
		Q = myProbe.getSpeciesCharge();
        
		// single CPU, single core
		IMPACT_WRITER.write("1 1\n");
		// total of 10000 particles
		IMPACT_WRITER.write("6 10000 2 0 2\n");
		
		IMPACT_WRITER.write("64 64 64 1 0.14 0.14 0.1025446\n");
		// 6D Waterbag, 2 charge states
		IMPACT_WRITER.write("3 0 0 2\n");
		// 5000 for each charge state
		IMPACT_WRITER.write("5000 5000\n");
		// beam current for each charge state
		IMPACT_WRITER.write("0.0 0.0\n");
		// q_i/m_i for each charge state
		IMPACT_WRITER.write("1.48852718947e-10 1.533634074e-10\n");
		// sigmax, lambdax, mux, mismatchx, mismatchpx, offsetX, offsetPx
		IMPACT_WRITER.write("\n");
		IMPACT_WRITER.write("\n");
		IMPACT_WRITER.write("\n");
		// 
		IMPACT_WRITER.write("\n");
		
//		int driftCounter = 0;
        
		for (int i = 0; i < _sequenceChain.size(); i++) {
			Lattice myLattice = createLattice( _sequenceChain.get(i) );
			int elementCount = myLattice.len();
			LatticeIterator ilat = myLattice.latticeIterator();
			int counter = 1;
			int devTypeInd = 1;
			String devStr = "";
			AcceleratorNode currentThickNode = null;	// there can at most be one thick node at any location
			double currentThickNodePath = 0.0;			// total current path taken through the thick node (only bends modify and use this variable)
			
			while ( ilat.hasNext() ) {
				final Element element = ilat.next();
				final String elementType = element.getType();
				final double elementLength = element.getLength();
				final AcceleratorNode node = element.getAcceleratorNode();
				
				// for regular drift space, diagnostic devices
				if (elementType.equals("drift")) {
					IMPACT_WRITER.write(NUMBER_FORMAT.format(elementLength) + "\t4\t20\t" + DRIFT + "\t " + APER + "\t/\n");
//					driftCounter++;
				}
				// for quads
				else if ( elementType.equals("quadrupole") || elementType.equals( "skewquadrupole" ) ) {
					final double field = getField( node, deviceDataSource );
					IMPACT_WRITER.write(NUMBER_FORMAT.format(elementLength) + "\t4\t20\t" + QUAD 
							+ NUMBER_FORMAT.format(field/elementLength) + "\t"+ node.getAper().getAperX()
							+ "\t" + node.getAlign().getX() + "\t" + node.getAlign().getY() 
							+ "\t" + node.getAlign().getPitch() + "\t" + node.getAlign().getYaw() + "\t" + node.getAlign().getRoll() + "\t/\n");
				}
				// for solenoid
				else if (elementType.equals("solenoid")) {
					final double field = getField( node, deviceDataSource );
					IMPACT_WRITER.write(NUMBER_FORMAT.format(elementLength) + "\t4\t20" + SOLENOID
							+ NUMBER_FORMAT.format(field) + "\t0\t" + node.getAper().getAperX()
							+ "\t" + node.getAlign().getX() + "\t" + node.getAlign().getY() 
							+ "\t" + node.getAlign().getPitch() + "\t" + node.getAlign().getYaw() + "\t" + node.getAlign().getRoll() + "\t/\n");
				}
				// for bending dipole
				else if ( elementType.equals("dipole") ) {
					IMPACT_WRITER.write(NUMBER_FORMAT.format(elementLength) + "\t10\t20" + DIPOLE
							+ "\t" + ((Bend)node).getDfltBendAngle() + "\t0.0\t150\t" + node.getAper().getAperX()
							+ "\t" + node.getAlign().getX() + "\t" + node.getAlign().getY() 
							+ "\t" + node.getAlign().getPitch() + "\t" + node.getAlign().getYaw() + "\t" + node.getAlign().getRoll() + "\t/\n");
				}
				// for RF cavity
				else if (elementType.equals("rfgap")) {
					double len = node.getParent().getLength();
					double freq = ((RfCavity) node.getParent()).getCavFreq() * 1.e6;
					double phase = ((RfCavity) node.getParent()).getDfltCavPhase();
					IMPACT_WRITER.write(NUMBER_FORMAT.format(len) + "\t10\t20" + SC
							+ "\t" + "1.0\t" + freq + "\t" + phase + "\t" + "1.0\t" + node.getAper().getAperX()
							+ "\t" + node.getAlign().getX() + "\t" + node.getAlign().getY() 
							+ "\t" + node.getAlign().getPitch() + "\t" + node.getAlign().getYaw() + "\t" + node.getAlign().getRoll() + "\t/\n");
				}
				
			}
		}
		
		// output format
		final StringBuffer footerBuffer = new StringBuffer();
		
		
		IMPACT_WRITER.write( footerBuffer.toString() );

		IMPACT_WRITER.close();
	}

	
	/**
	 * create an XAL intermediate lattice
	 * @param accSeq accelerator sequence for the lattice
	 * @return XAL intermediate lattice
	 */
	public Lattice createLattice(AcceleratorSeq accSeq) {
		// create lattice using the (combo) sequence
		LatticeFactory factory = new LatticeFactory();
		factory.setDebug(false);
		factory.setVerbose(false);
		factory.setHalfMag(true);
		Lattice lattice = new Lattice(myLatticeName);
		try {
			lattice = factory.getLattice(accSeq);
			lattice.clearMarkers();
			lattice.joinDrifts();
		} catch (LatticeError lerr) {
			System.out.println(lerr.getMessage());
		}
        
		return lattice;
        
	}

	/** Get the magnet's field */
	private double getField( final AcceleratorNode node, final AbstractDeviceDataSource deviceDataSource ) {
		if ( node instanceof Magnet ) {
			final Magnet magnet = (Magnet)node;
			return deviceDataSource.getField( magnet );
		}
		else {
			return 0.0;
		}
	}
	
}
