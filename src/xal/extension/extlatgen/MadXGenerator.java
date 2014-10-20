package xal.extension.extlatgen;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import xal.smf.*;
import xal.smf.impl.Magnet;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.RfGap;
import xal.smf.impl.RfCavity;
import xal.ca.*;
import xal.sim.slg.*; // for lattice generation
import xal.model.probe.*; // Probe for Mad header
// import gov.sns.xal.model.probe.traj.EnvelopeProbeState;
import xal.sim.scenario.Scenario;
import xal.tools.beam.Twiss;
import xal.tools.beam.TraceXalUnitConverter;
import xal.tools.beam.RelativisticParameterConverter;
import xal.tools.beam.CovarianceMatrix;

public class MadXGenerator {
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
	
	/** list of MAD elements */
	private List<MadXElement> MADX_ELEMENTS;
	
	/** beam initial condition */
	protected double beamci[] = { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
    
	/** indicates whether to use design bend angles regardless of the specified data source */
	private boolean _useDesignBendAngles;
	
	
	// static initializer
	static {
		NUMBER_FORMAT = NumberFormat.getNumberInstance();
		((DecimalFormat) NUMBER_FORMAT).setMaximumFractionDigits( 8 );
	}
	
	
	/**
	 * Constructor
	 * @param sequenceChain sequence list
	 * @param envProbe envelope probe
	 */
	public MadXGenerator( java.util.List<AcceleratorSeq> sequenceChain, TransferMapProbe envProbe ) {
		this ( null, sequenceChain, envProbe );
	}
	
	
	/** Constructor */
	public MadXGenerator( java.util.List<AcceleratorSeq> sequenceChain, EnvelopeProbe envProbe ) {
		this( null, sequenceChain, envProbe );
	}
	
	
	/**
	 * Constructor
	 * @param latticeName lattice name (if there is one)
	 * @param sequenceChain sequence list
	 * @param envProbe envelope probe
	 */
	public MadXGenerator( String latticeName, java.util.List<AcceleratorSeq> sequenceChain, TransferMapProbe envProbe ) {
		this( latticeName, sequenceChain, (Probe)envProbe );
	}
    
	
	/** Constructor */
	public MadXGenerator(String latticeName, java.util.List<AcceleratorSeq> sequenceChain, EnvelopeProbe envProbe) {
		this( latticeName, sequenceChain, (Probe)envProbe );
	}
    
    
	/** Constructor */
	public MadXGenerator( final String latticeName, final java.util.List<AcceleratorSeq> sequenceChain, final Probe<?> envProbe) {
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
	 * generate the MAD input file
	 * @param deviceDataSource data source for the device's fields
	 */
	public void createMadInput( final AbstractDeviceDataSource deviceDataSource ) throws IOException {
		createMadInput( deviceDataSource, null );
	}
	
	
	/** add a new MAD element with the specified element name and definition */
	private void addElement( final String elementName, final String definition ) {
		MADX_ELEMENTS.add( new MadXElement( elementName, definition ) );
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
	
	
	/**
	 * generate the MAD input file
	 * @param deviceDataSource data source for the device's fields
	 */
	public void createMadInput( final AbstractDeviceDataSource deviceDataSource, final File outputFile ) throws IOException {
		// select the data source for bends depending on whether the flag has been set to use design bend angles
		final AbstractDeviceDataSource bendDataSource = _useDesignBendAngles ? AbstractDeviceDataSource.getDesignDataSourceInstance() : deviceDataSource;
        
		if (myLatticeName == null) {
			myLatticeName = _sequenceChain.get(0).getId() + "-" + _sequenceChain.get( _sequenceChain.size() - 1 ).getId();
		}
        
		File mad_file = outputFile != null ? outputFile : new File( myLatticeName + ".mad" );
		System.out.println( "Exporting MAD optics to file: " + mad_file.getAbsolutePath() );
		final FileWriter MADX_WRITER = new FileWriter( mad_file );
		final Date today = new Date();
        
//		TraceXalUnitConverter uc = TraceXalUnitConverter.newConverter( 402500000., myProbe.getSpeciesRestEnergy(), myProbe.getKineticEnergy() );
        
		double momentum = RelativisticParameterConverter.computeMomentumFromEnergies( myProbe.getKineticEnergy(), myProbe.getSpeciesRestEnergy() ) / 1.e9;
		System.out.println( "momentum = " + momentum );
        
		// Q = myProbe.getSpeciesCharge()/1.602e-19;
		Q = myProbe.getSpeciesCharge();
        
		final String sourceLabel = deviceDataSource.getLabel();
		MADX_WRITER.write( "TITLE, \"" + sourceLabel + ": " + formatName(myLatticeName) + "  Date created: " + today.toString() + "\";\n\n" );
        
		// no need to put drift spaces in the file
//		int driftCounter = 0;
        
		MADX_ELEMENTS = new ArrayList<MadXElement>();
		
		ArrayList<Double> lat_lengths = new ArrayList<>();
		HashMap<String, Double> elem_pos_map = new HashMap<>();
		ArrayList<String> allNames = new ArrayList<>();
		
		for (int i = 0; i < _sequenceChain.size(); i++) {
			Lattice myLattice = createLattice( _sequenceChain.get(i) );
			lat_lengths.add(myLattice.getLength());
//			int elementCount = myLattice.len();
			LatticeIterator ilat = myLattice.latticeIterator();
			int counter = 1;
			int devTypeInd = 1;
			String devStr = "";
			AcceleratorNode currentThickNode = null;	// there can at most be one thick node at any location
			double currentThickNodePath = 0.0;			// total current path taken through the thick node (only bends modify and use this variable)
			
			while ( ilat.hasNext() ) {
				final Element element = ilat.next();
				final String elementName = element.getName();
				String formattedName = formatName( elementName );
				if (!allNames.contains(formattedName)) {
					allNames.add(formattedName);
				} else {
					formattedName = formattedName.concat("_A");
					allNames.add(formattedName);
				}
				final String elementType = element.getType();
//				System.out.println("element " + elementName + " has type = " + elementType + " thick = " + element.isThick());
				final double elementLength = element.getLength();
				final AcceleratorNode node = element.getAcceleratorNode();
				
				if (elementType.equals("rfgap")) {
//					elem_pos_map.put(formatName(node.getParent().getId()), node.getParent().getPosition());
					elem_pos_map.put(formatName(node.getParent().getId()), element.getPosition());
				} else 
					elem_pos_map.put(formattedName, element.getPosition());

				if ( element != null && element.isThick() ) {
					if ( node != currentThickNode ) {
						currentThickNode = node;
						currentThickNodePath = 0.0;
					}
				}
				
                
				// for regular drift space, diagnostic devices
//				if (elementType.equals("drift")) {
//					addElement( formattedName + driftCounter, "DRIFT, L=" + NUMBER_FORMAT.format(elementLength) );
//					driftCounter++;
//				}
				// for marker
				if ( elementType.equals("pmarker") || elementType.equals("foil") ) {
					if (!element.getName().contains("CENTER")) {
						addElement( formattedName, "MARKER" );
//						elem_pos_map.put(formattedName, element.getPosition());
					}
				}
				// for diagnostic devices (monitors)
				else if ( elementType.equals( "beampositionmonitor" ) || elementType.equals( "beamlossmonitor" ) || elementType.equals( "beamcurrentmonitor" ) || elementType.equals( "wirescanner" ) ) {
					addElement( formattedName, "MONITOR" );
//					elem_pos_map.put(formattedName, element.getPosition());
				}
				// for quads
				else if ( elementType.equals("quadrupole") || elementType.equals( "skewquadrupole" ) ) {
					final double rollAngle = node.getAlign().getRoll() * Math.PI / 180.0;	// get the roll angle in radians
					final double field = getField( node, deviceDataSource );
					
					String definition = "QUADRUPOLE, L=" + NUMBER_FORMAT.format( elementLength ) + ", K1=" + NUMBER_FORMAT.format( Q * field * LIGHT_SPEED / momentum );
					if ( rollAngle != 0.0 )  definition += ", TILT=" + rollAngle;
					addElement( formattedName, definition );
//					elem_pos_map.put(formattedName, element.getPosition());
				}
				// for bending dipole
				else if ( elementType.equals("dipole") ) {
					final xal.smf.impl.Bend bendNode = (xal.smf.impl.Bend)node;
					final double bendMagneticLength = bendNode.getEffLength();
                    
					final double bendAngle = elementLength * bendDataSource.getBendAnglePerLength( bendNode, Q, momentum );
                    
					// if the element is the first for the bend magnet then we apply the entrance angle for this element
					// an element is determined to be the first element of a bend if the current path through the bend is at the beginning (i.e. zero).
					final double entranceAngle = currentThickNodePath == 0.0 ? bendDataSource.getBendEntranceAngle( bendNode, Q, momentum ) : 0.0;
					currentThickNodePath += elementLength;		// advance the path through the bend magnet
					// if the element is the last for the bend magnet then we apply the exit angle for this element
					// an element is determined to be the last element of a bend if the path after having passed through the element equals the magnetic length of the whole bend
					final double lengthThreshold = 0.99999;	// ideally this should be 1.0, but we must allow for numerical precision errors
					final double exitAngle = currentThickNodePath > lengthThreshold * bendMagneticLength ? bendDataSource.getBendExitAngle( bendNode, Q, momentum ) : 0.0;
                    
					final double k1 = bendNode.getQuadComponent();
					
					addElement( formattedName, "SBEND, L=" + NUMBER_FORMAT.format( elementLength ) + ", ANGLE=" + NUMBER_FORMAT.format( bendAngle ) + ", K1=" + NUMBER_FORMAT.format( k1 ) + ", " + "E1=" + NUMBER_FORMAT.format( entranceAngle ) + ", " + "E2=" + NUMBER_FORMAT.format( exitAngle ) );
//					elem_pos_map.put(formattedName, element.getPosition());
				}
				// for solenoid
				else if (elementType.equals("solenoid")) {
					final double field = getField( node, deviceDataSource );
					addElement( formattedName, "SOLENOID, L=" + NUMBER_FORMAT.format(elementLength) + ", KS=" + NUMBER_FORMAT.format( field * LIGHT_SPEED / momentum ) );
//					elem_pos_map.put(formattedName, element.getPosition());
				}
				
				// for horizontal dipole correctors
				else if (elementType.equals("hsteerer")) {
					final xal.smf.impl.HDipoleCorr corrector = (xal.smf.impl.HDipoleCorr)node;
					final double field = getField( node, deviceDataSource );
					final double kick = - field * LIGHT_SPEED * corrector.getEffLength() / momentum;
					addElement( formattedName, "HKICKER, KICK=" + NUMBER_FORMAT.format( kick ) );
//					elem_pos_map.put(formattedName, element.getPosition());
				}
				// for vertical dipole correctors
				else if (elementType.equals("vsteerer")) {
					final xal.smf.impl.VDipoleCorr corrector = (xal.smf.impl.VDipoleCorr)node;
					final double field = getField( node, deviceDataSource );
					final double kick = - field * LIGHT_SPEED * corrector.getEffLength() / momentum;
					addElement( formattedName, "VKICKER, KICK=" + NUMBER_FORMAT.format( kick ) );
//					elem_pos_map.put(formattedName, element.getPosition());
				}
				// for sextupoles
				else if ( elementType.equals( "sextupole" ) ) {
					final double field = getField( node, deviceDataSource );
					final double k2 = Q * field * LIGHT_SPEED / momentum;
					addElement( formattedName, "SEXTUPOLE, L=" + NUMBER_FORMAT.format( elementLength ) + ", K2=" + NUMBER_FORMAT.format( k2 ) );
//					elem_pos_map.put(formattedName, element.getPosition());
				}
                //				// RF Cavities are not handled properly, so comment out the RF Cavity code
                //				// for rf gaps
				else if (elementType.equals("rfgap")) {
					double field = 0.;
					//                					if (deviceDataSource.getLabel().startsWith("DESIGN")) {
					field = ((RfCavity) node.getParent()).getDfltCavAmp();
					//                					} else {
					//                						try {
					//											field = ((RfCavity) node.getParent()).getCavAmpAvg();
					//										} catch (ConnectionException e) {
					//											// TODO Auto-generated catch block
					//											e.printStackTrace();
					//										} catch (GetException e) {
					//											// TODO Auto-generated catch block
					//											e.printStackTrace();
					//										}
					//                					}
					final double phase = ((RfGap) node).getGapDfltPhase();
					addElement( formatName(node.getParent().getId()), "RFCAVITY, L=" + NUMBER_FORMAT.format( node.getParent().getLength()) + ", VOLT=" + NUMBER_FORMAT.format(field) + ", LAG=" + NUMBER_FORMAT.format(phase) + ", HARMON =1, FREQ=" + ((RfCavity) node.getParent()).getCavFreq());
//					elem_pos_map.put(formatName(node.getParent().getId()), node.getParent().getPosition());
				}
				else {
					if ( node != null ) {
						System.out.println( "Ignored element type: " + elementType + ", node: " + node.getId() + ", length: " + node.getLength() );
					}
					else {
						System.out.println( "Ignored element type: " + elementType );
					}
					continue;
				}
				counter++;
			}
		}
		
		// write the MAD element definitions
		for ( final MadXElement element : MADX_ELEMENTS ) {
			MADX_WRITER.write( element.NAME + ": " + element.DEFINITION + ";\n" );
		}
		
		// construct the MAD lines
		final int MAX_LINE_LENGTH = 1250;
		int lineIndex = MAX_LINE_LENGTH;
		final List<List<MadXElement>> lines = new ArrayList<List<MadXElement>>();
		List<MadXElement> line = null;	// current line
		for ( final MadXElement element : MADX_ELEMENTS ) {
			if ( lineIndex >= MAX_LINE_LENGTH ) {
				line = new ArrayList<MadXElement>( MAX_LINE_LENGTH );
				lines.add( line );
				lineIndex = 1;
			}
			line.add( element );
			++lineIndex;
		}
		
		// write the MAD lines
		final int lineCount = lines.size();
		for ( lineIndex = 0 ; lineIndex < lineCount ; lineIndex++ ) {
			final List<MadXElement> theLine = lines.get( lineIndex );
			MADX_WRITER.write( formatName(myLatticeName) + ": SEQUENCE, REFER=CENTER, L=" + lat_lengths.get(lineIndex) + ";\n" );
			final int numELements = theLine.size();
			for ( int index = 0 ; index < numELements ; index++ ) {
				final MadXElement element = theLine.get( index );
				MADX_WRITER.write( "    " + element.NAME + ", AT=" + elem_pos_map.get(element.NAME) + ";\n" );
			}
//			final MadXElement element = theLine.get( numELements - 1 );
//			MADX_WRITER.write( "    " + element.NAME + ");\n" );
			MADX_WRITER.write("ENDSEQUENCE;\n");
		}
//		MADX_WRITER.write( formatName( myLatticeName ) + ": LINE=(" );
//		for ( lineIndex = 0 ; lineIndex < lineCount-1 ; lineIndex++ ) {
//			MADX_WRITER.write( "SEGMENT" + (lineIndex + 1) + "," );
//		}
//		MADX_WRITER.write( "SEGMENT" + lineCount + ");\n" );
        
		final StringBuffer footerBuffer = new StringBuffer();
		footerBuffer.append( "BEAM, PARTICLE=" + myProbe.getSpeciesName() + ", MASS=" + NUMBER_FORMAT.format(myProbe.getSpeciesRestEnergy() / 1.e9) );
//		footerBuffer.append( ", PARTICLE=" + myProbe.
		footerBuffer.append( ", CHARGE=" + NUMBER_FORMAT.format( myProbe.getSpeciesCharge() ) );
		footerBuffer.append( ", ENERGY=" + NUMBER_FORMAT.format( ( RelativisticParameterConverter.computeGammaFromEnergies(myProbe.getKineticEnergy(), myProbe.getSpeciesRestEnergy() ) * myProbe.getSpeciesRestEnergy() ) / 1.e9 ) + ";\n" );
		footerBuffer.append( "USE, sequence=" + formatName(myLatticeName) + ";\n" );
		if ( myProbe instanceof EnvelopeProbe ) {
            CovarianceMatrix covarianceMatrix = ((EnvelopeProbe)myProbe).createProbeState().getCovarianceMatrix();
            
            Twiss[] inputTwiss = covarianceMatrix.computeTwiss();
            
			footerBuffer.append( "   SELECT, flag=twiss, COLUMN = NAME,KEYWORD,S,L,K1,x,y,BETX,ALFX,DX,BETY,ALFY,DY,ENERGY;\n" );
			footerBuffer.append( "   TWISS" );
			footerBuffer.append( ",BETX=" + inputTwiss[0].getBeta() );
			footerBuffer.append( ",ALFX=" + inputTwiss[0].getAlpha() );
			footerBuffer.append( ",BETY=" + inputTwiss[1].getBeta() );
			footerBuffer.append( ",ALFY=" + inputTwiss[1].getAlpha() );
			footerBuffer.append( ",DX=" + 0.0 );
			footerBuffer.append( ",DPX=" + 0.0 );
			footerBuffer.append( ", file='twiss.out';\n" );
		}
		else {
			footerBuffer.append( "   SELECT, Flag=twiss, Class=MONITOR, PATTERN=\"BPM.*\", RANGE=#S/#E, COLUMN=name,s,x,y,betx,bety,alfx,alfy,mux,muy,Dx,Dy;\n" );
			footerBuffer.append( "   twiss, file=twiss.out;\n" );
		}
		footerBuffer.append( "   setplot, post=1, font=-1;\n" );
		footerBuffer.append( "   plot, haxis=s, vaxis1=betx,bety, range=#s/#e, style=100, colour=100, notitle=true;\n" );
		footerBuffer.append( "   plot, haxis=s, vaxis1=x,y, range=#s/#e, style=100, colour=100, notitle=true;\n" );
		footerBuffer.append( "STOP;\n" );
        
		MADX_WRITER.write( footerBuffer.toString() );
        
		MADX_WRITER.close();
        
	}
    
	/** strip the leading sequence and device category identifier (i.e. Ring_Mag:), replace any "-" or ":" with "_" in the device name or beamline name */
	public String formatName( final String name ) {
//		if ( !( name.substring( 0, 3 ).equals( "END" ) ) && !( name.substring( 0, 3 ).equals( "BEG" ) ) ) {
			final String formattedName = name.replaceFirst(".*_.*:", "").replace('-', '_').replace(':', '_').replace(' ', '_');
			return formattedName;
//		}
//		return name;
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
}



/** MAD element name and defintion */
class MadXElement {
	/** name of the MAD element */
	final public String NAME;
	
	/** definition of the MAD element */
	final public String DEFINITION;
	
	
	/** Constructor */
	public MadXElement( final String name, final String definition ) {
		NAME = name;
		DEFINITION = definition;
	}

}
