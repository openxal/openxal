/*
 * LatticeSynchronizer.java
 *
 * Created on June 2, 2003, 3:55 PM
 */

package xal.sim.slg;

import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlWriter;
import xal.tools.xml.XmlDataAdaptor.WriteException;
import xal.model.elem.IdealMagSteeringDipole;
import xal.model.elem.IdealMagQuad;
import xal.model.elem.sync.IElectromagnet;
import xal.sim.mpx.ModelProxy;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.Magnet;
import xal.smf.impl.RfGap;
import xal.smf.impl.Electrostatic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.NumberFormat;

import org.w3c.dom.Document;
/**
 * A visitor generating an XML document for the lattice. This document
 * is not compatible with the on-line model. To make the document compatible
 * with the on-line model it has to be transformed with an XSL stylesheet.
 *
 * @author  wdklotz
 */
public class LatticeSynchronizer implements Visitor {
	private XmlDataAdaptor docAdptr; //root adaptor for xml-document
	private DataAdaptor latAdptr; //adaptor for lattice tag
	private DataAdaptor seqAdptr; //adaptor for sequence tag
	private DataAdaptor elmAdptr; //adaptor for element tag
	private DataAdaptor parAdptr; //adaptor for parameter tag
	private DataAdaptor comAdptr; //adaptor for comment tag
	private String paramSrc;
//	private static ModelTypeLookUp modelType; //look-up for LANL types
	private static NumberFormat fmt; //number formater
	private static final String docType;
	private static final String seqTag;
	private static final String elmTag;
	private static final String parTag;
	private static final String comTag;
	private static final String dtd;

	static {
		docType= "Lattice";
		seqTag= "Sequence";
		elmTag= "Element";
		parTag= "Parameter";
		comTag= "comment";
		dtd= "Lattice.mod.xal.dtd";
//		modelType= new ModelTypeLookUp();
	}

	/** Creates a new instance of LatticeSynchronizer */
	public LatticeSynchronizer(Lattice lattice) {
		this(lattice, ModelProxy.PARAMSRC_DESIGN);
	}

	/** Creates a new instance of LatticeSynchronizer */
	public LatticeSynchronizer(Lattice lattice, String paramSrc) {
		// use CA ?
		this.paramSrc = paramSrc;
		fmt= Lattice.fmt; // number format is defined in Lattice

		//the xml-document-adaptor: creates the <!DOCTYPE ...> declaration
		docAdptr= XmlDataAdaptor.newEmptyDocumentAdaptor(docType, dtd);

		// the Lattice tag: creates the <Lattice .../> root tag
		latAdptr= docAdptr.createChild(docType);
		latAdptr.setValue("id", lattice.getName());
		latAdptr.setValue( "ver", " " );
		latAdptr.setValue("author", "W.-D. Klotz");

		// the comment tag: creates a <comment ..../> tag
		comAdptr= latAdptr.createChild(comTag);
		comAdptr.setValue("text", "document generated from " + Lattice.version());

		// the Sequence tag: creates the <Sequence id="xxx" ..../> tag
		seqAdptr= latAdptr.createChild("Sequence");
		seqAdptr.setValue("id", lattice.getName());

		// go and visit all lattice elments ...
		LatticeIterator liter= lattice.latticeIterator();
		while (liter.hasNext()) {
			liter.next().accept(this);
		}
	}

	/**
	 * Writes the <Element .../> tag to the xml document.
	 */
	private void writeElementTag(Element e) {
		elmAdptr= seqAdptr.createChild(elmTag);
		elmAdptr.setValue("fam", e.getFam());
		//        elmAdptr.setValue("type",modelType.ValueForKey(e.getType()));
		elmAdptr.setValue("type", e.getType());
		elmAdptr.setValue("id", e.getName());
		elmAdptr.setValue("length", fmt.format(e.getLength()));
		//parameter
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "StartPosition");
		parAdptr.setValue("type", "double");
		parAdptr.setValue("value", fmt.format(e.getStartPosition()));
		//parameter
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "Position");
		parAdptr.setValue("type", "double");
		parAdptr.setValue("value", fmt.format(e.getPosition()));
	}

	/**
	 * Returns the whole lattice document as a string.
	 */
	public String toString() {
		StringWriter sout= new StringWriter();
		docAdptr.writeTo(sout);
		return sout.toString();
	}

	/**
	 * Returns the whole lattice document as a DOM object.
	 */
	public Document getDocument() {
		return docAdptr.document();
	}

	/** Write XML to the specified url */
	public void writeTo(java.io.Writer writer) {
		XmlWriter.writeToWriter(docAdptr.document(), writer);
	}

	/** Convenience method for writing an XML file */
	public void writeTo(File file) throws IOException {
		writeTo(new FileWriter(file));
	}

	/** Write XML to the specified url */
	public void writeToUrlSpec(String urlSpec) throws WriteException {
		try {
			XmlWriter.writeToUrlSpec(docAdptr.document(), urlSpec);
		} catch (Exception excpt) {
			throw new WriteException(excpt);
		}
	}

	/** Write XML to the specified url */
	public void writeToUrl(java.net.URL url) throws WriteException {
		try {
			XmlWriter.writeToUrl(docAdptr.document(), url);
		} catch (Exception excpt) {
			throw new WriteException(excpt);
		}
	}

	/** Writes the parameters of a RFGap lattice element  */
	public void visit(RFGap e) {
		writeElementTag(e);
		RfGap rfgap= (RfGap) e.getAcceleratorNode();
		//parameter
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "Frequency");
		parAdptr.setValue("type", "double");
		parAdptr.setValue("value", Double.toString(getGapFrequencyWrapper(rfgap)));
		//parameter
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "Phase");
		parAdptr.setValue("type", "double");
		parAdptr.setValue("value", Double.toString(getRfGapPhaseAvgWrapper(rfgap)));
		//parameter
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "ETL");
		parAdptr.setValue("type", "double");
		parAdptr.setValue("value", Double.toString(getRfGapE0TLWrapper(rfgap)));
	}

	/** Writes the element- and paramter-tags of a PermMarker lattice element  */
	public void visit(PermMarker e) {
		writeElementTag(e);
	}

	/** Writes the element- and paramter-tags of a SkewSext lattice element  */
	public void visit(SkewSext e) {
		writeElementTag(e);
	}

	/** Writes the element- and paramter-tags of an Octupole lattice element  */
	public void visit(Octupole e) {
		writeElementTag(e);
	}

	/** Writes the element- and paramter-tags of a BCMonitor lattice element  */
	public void visit(BCMonitor e) {
		writeElementTag(e);
	}
	
	
	/** Writes the element- and paramter-tags of a BSMonitor lattice element  */
	public void visit( final BSMonitor element ) {
		writeElementTag( element );
	}
	
	/** Writes the element- and paramter-tags of a HSteerer lattice element  */
	public void visit(HSteerer e) {
		writeElementTag(e);
		Magnet magnet= (Magnet) e.getAcceleratorNode();
		//parameter 
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "MagField");
		parAdptr.setValue("type", "double");
		parAdptr.setValue("value", Double.toString(getFieldWrapper(magnet)));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "EffLength");
		parAdptr.setValue("type", "double");
		double effLen= magnet.getEffLength();
		parAdptr.setValue("value", Double.toString(effLen));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "Orientation");
		parAdptr.setValue("type", "int");
		IElectromagnet elmg= new IdealMagSteeringDipole();
		int orientation= IElectromagnet.ORIENT_NONE;
		if (magnet.isHorizontal()) {
			orientation= IElectromagnet.ORIENT_HOR;
		}
		if (magnet.isVertical()) {
			orientation= IElectromagnet.ORIENT_VER;
		}
		parAdptr.setValue("value", Integer.toString(orientation));
	}

	/** Writes the element- and paramter-tags of a Dipole lattice element  */
	public void visit(Dipole e) {
		writeElementTag(e);
		Magnet magnet= (Magnet) e.getAcceleratorNode();
		//parameter 
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "magField");
		parAdptr.setValue("type", "double");
		parAdptr.setValue("value", Double.toString(getFieldWrapper(magnet)));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "EffLength");
		parAdptr.setValue("type", "double");
		double effLen= magnet.getEffLength() * e.getLength() / magnet.getLength();
		parAdptr.setValue("value", Double.toString(effLen));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "Orientation");
		parAdptr.setValue("type", "int");
		IElectromagnet elmg= new IdealMagSteeringDipole();
		int orientation= IElectromagnet.ORIENT_NONE;
		if (magnet.isHorizontal()) {
			orientation= IElectromagnet.ORIENT_HOR;
		}
		if (magnet.isVertical()) {
			orientation= IElectromagnet.ORIENT_VER;
		}
		parAdptr.setValue("value", Integer.toString(orientation));
	}
	
	@Override
	public void visit(EDipole e) {
		// TODO Auto-generated method stub
		writeElementTag(e);
		xal.smf.impl.EDipole edipole= (xal.smf.impl.EDipole) e.getAcceleratorNode();
		//parameter 
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "magField");
		parAdptr.setValue("type", "double");
		parAdptr.setValue("value", Double.toString(getFieldWrapper(edipole)));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "EffLength");
		parAdptr.setValue("type", "double");
		double effLen= edipole.getEffLength() * e.getLength() / edipole.getLength();
		parAdptr.setValue("value", Double.toString(effLen));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "Orientation");
		parAdptr.setValue("type", "int");
//		IdealEDipole elmg= new IdealEDipole();
		int orientation= edipole.getOrientation();
//		if (edipole.isHorizontal()) {
//			orientation= IElectromagnet.ORIENT_HOR;
//		}
//		if (edipole.isVertical()) {
//			orientation= IElectromagnet.ORIENT_VER;
//		}
		parAdptr.setValue("value", Integer.toString(orientation));
		
	}

	/** Writes the element- and paramter-tags of a VSteerer lattice element  */
	public void visit(VSteerer e) {
		writeElementTag(e);
		Magnet magnet= (Magnet) e.getAcceleratorNode();
		//parameter 
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "MagField");
		parAdptr.setValue("type", "double");
		parAdptr.setValue("value", Double.toString(getFieldWrapper(magnet)));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "EffLength");
		parAdptr.setValue("type", "double");
		double effLen= magnet.getEffLength();
		parAdptr.setValue("value", Double.toString(effLen));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "Orientation");
		parAdptr.setValue("type", "int");
		IElectromagnet elmg= new IdealMagSteeringDipole();
		int orientation= IElectromagnet.ORIENT_NONE;
		if (magnet.isHorizontal()) {
			orientation= IElectromagnet.ORIENT_HOR;
		}
		if (magnet.isVertical()) {
			orientation= IElectromagnet.ORIENT_VER;
		}
		parAdptr.setValue("value", Integer.toString(orientation));
	}
	
	/** Writes the element- and paramter-tags of a EKicker lattice element  */
	public void visit( final EKicker element ) {
		writeElementTag( element );
		final Magnet magnet= (Magnet)element.getAcceleratorNode();
		//parameter
		parAdptr= elmAdptr.createChild( "Parameter" );
		parAdptr.setValue( "name", "MagField" );
		parAdptr.setValue( "type", "double" );
		parAdptr.setValue( "value", Double.toString( getFieldWrapper(magnet) ) );
		//parameter
		parAdptr= elmAdptr.createChild( "Parameter" );
		parAdptr.setValue( "name", "EffLength" );
		parAdptr.setValue( "type", "double" );
		double effLen= magnet.getEffLength();
		parAdptr.setValue( "value", Double.toString( effLen ) );
		//parameter
		parAdptr= elmAdptr.createChild( "Parameter" );
		parAdptr.setValue( "name", "Orientation" );
		parAdptr.setValue( "type", "int" );
		IElectromagnet elmg= new IdealMagSteeringDipole();
		int orientation= IElectromagnet.ORIENT_NONE;
		if ( magnet.isHorizontal() ) {
			orientation= IElectromagnet.ORIENT_HOR;
		}
		if ( magnet.isVertical() ) {
			orientation= IElectromagnet.ORIENT_VER;
		}
		parAdptr.setValue( "value", Integer.toString( orientation ) );
	}

	/** Writes the element- and paramter-tags of a Drift lattice element  */
	public void visit(Drift e) {
		writeElementTag(e);
	}

	/** Writes the element- and paramter-tags of a Quadrupole lattice element  */
	public void visit(Quadrupole e) {
		writeElementTag(e);
		Magnet magnet= (Magnet) e.getAcceleratorNode();
		//parameter 
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "MagField");
		parAdptr.setValue("type", "double");
		parAdptr.setValue("value", Double.toString(getFieldWrapper(magnet)));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "EffLength");
		parAdptr.setValue("type", "double");
		double effLen= magnet.getEffLength() * e.getLength() / magnet.getLength();
		parAdptr.setValue("value", Double.toString(effLen));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "Orientation");
		parAdptr.setValue("type", "int");
		IElectromagnet elmg= new IdealMagQuad();
		int orientation= IElectromagnet.ORIENT_NONE;
		if (magnet.isHorizontal()) {
			orientation= IElectromagnet.ORIENT_HOR;
		}
		if (magnet.isVertical()) {
			orientation= IElectromagnet.ORIENT_VER;
		}
		parAdptr.setValue("value", Integer.toString(orientation));
	}

	/** Writes the element- and paramter-tags of a Quadrupole lattice element  */
	public void visit(EQuad e) {
		writeElementTag(e);
		xal.smf.impl.EQuad magnet= (xal.smf.impl.EQuad) e.getAcceleratorNode();
		//parameter 
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "MagField");
		parAdptr.setValue("type", "double");
		parAdptr.setValue("value", Double.toString(getFieldWrapper(magnet)));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "EffLength");
		parAdptr.setValue("type", "double");
		double effLen= magnet.getEffLength() * e.getLength() / magnet.getLength();
		parAdptr.setValue("value", Double.toString(effLen));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "Orientation");
		parAdptr.setValue("type", "int");
		IElectromagnet elmg= new IdealMagQuad();
		int orientation= IElectromagnet.ORIENT_NONE;
		if (magnet.isHorizontal()) {
			orientation= IElectromagnet.ORIENT_HOR;
		}
		if (magnet.isVertical()) {
			orientation= IElectromagnet.ORIENT_VER;
		}
		parAdptr.setValue("value", Integer.toString(orientation));
	}

	/** Writes the element- and paramter-tags of a Quadrupole lattice element  */
	public void visit(Solenoid e) {
		writeElementTag(e);
		Magnet magnet= (Magnet) e.getAcceleratorNode();
		//parameter 
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "MagField");
		parAdptr.setValue("type", "double");
		parAdptr.setValue("value", Double.toString(getFieldWrapper(magnet)));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
		parAdptr.setValue("name", "EffLength");
		parAdptr.setValue("type", "double");
		double effLen= magnet.getEffLength() * e.getLength() / magnet.getLength();
		parAdptr.setValue("value", Double.toString(effLen));
		//parameter         
		parAdptr= elmAdptr.createChild("Parameter");
/*		parAdptr.setValue("name", "Orientation");
		parAdptr.setValue("type", "int");
		IElectromagnet elmg= new IdealMagQuad();
		int orientation= IElectromagnet.ORIENT_NONE;
		if (magnet.isHorizontal()) {
			orientation= IElectromagnet.ORIENT_HOR;
		}
		if (magnet.isVertical()) {
			orientation= IElectromagnet.ORIENT_VER;
		}
		parAdptr.setValue("value", Integer.toString(orientation));
*/	}

	/** Writes the element- and paramter-tags of a WScanner lattice element  */
	public void visit(WScanner e) {
		writeElementTag(e);
	}
	
	/** Writes the element- and paramter-tags of a WScanner lattice element  */
/*	public void visit(Harp e) {
		writeElementTag(e);
	}	
*/
	/** Writes the element- and paramter-tags of a BPMonitor lattice element  */
	public void visit(BPMonitor e) {
		writeElementTag(e);
	}

	/** Writes the element- and paramter-tags of a BLMonitor lattice element  */
	public void visit(BLMonitor e) {
		writeElementTag(e);
	}

	/** Writes the element- and paramter-tags of a Foil lattice element  */
/*	public void visit(Foil e) {
		writeElementTag(e);
	}
*/
	/** Writes the element- and paramter-tags of a Foil lattice element  */
/*	public void visit(VacuumWindow e) {
		writeElementTag(e);
	}
*/
	/** Writes the element- and paramter-tags of a SkewQuad lattice element  */
	public void visit(SkewQuad e) {
		writeElementTag(e);
	}

	/** Writes the element- and paramter-tags of a Sextupole lattice element  */
	public void visit(Sextupole e) {
		writeElementTag(e);
	}

	/** Writes the element- and paramter-tags of a Marker lattice element  */
	public void visit(Marker e) {
		writeElementTag(e);
	}

/**A wrapper to read the RfGap frequency.*/
	private double getGapFrequencyWrapper(RfGap rfgap) {
		return rfgap.getGapDfltFrequency()*1.e6;
	}

/**A wrapper to read the RfGap average phase.*/
	private double getRfGapPhaseAvgWrapper(RfGap rfgap) {
                // for design values
		if (paramSrc == ModelProxy.PARAMSRC_DESIGN)
			return rfgap.getGapDfltPhase()*Math.PI/180.; 
                // for live values
		else if (paramSrc == ModelProxy.PARAMSRC_LIVE) {
			try {
				return rfgap.getGapPhaseAvg()*Math.PI/180.;
				//				return -99.d;
			} catch (Throwable e) {
				//				throw new Error(e.getMessage());
				if (e.getMessage() != null) {
					System.out.println(e.getMessage());
				} else {
					System.out.println("RfGap.getGapPhaseAvg(): channel access failed: " + rfgap.getId());
				}
				return Math.PI * 0.5; // 90 degrees
			}
                } else if (paramSrc == ModelProxy.PARAMSRC_RF_DESIGN)
			return rfgap.getGapDfltPhase()*Math.PI/180.;
		else
                	return rfgap.getGapDfltPhase()*Math.PI/180.;
	}

/**A wrapper to read the RfGap E0TL.*/
	private double getRfGapE0TLWrapper(RfGap rfgap) {
                // for design values
		if (paramSrc == ModelProxy.PARAMSRC_DESIGN)
                        return rfgap.getGapDfltE0TL()*1.e6;
                // for live values
		else if (paramSrc == ModelProxy.PARAMSRC_LIVE) {
			try {
				return rfgap.getGapE0TL()*1.e6;
				//				return -99.d;
			} catch (Throwable e) {
				//				throw new Error(e.getMessage());
				if (e.getMessage() != null) {
					System.out.println(e.getMessage());
				} else {
					System.out.println("RfGap.getGapE0TL(): channel access failed: " + rfgap.getId());
				}
				return rfgap.getGapDfltE0TL()*1.e6;
			}
		} else if (paramSrc == ModelProxy.PARAMSRC_RF_DESIGN)
			return rfgap.getGapDfltPhase()*Math.PI/180.;
		else
                	return rfgap.getGapDfltE0TL()*1.e6;
	}

/**A wrapper to read the magnet field strength.*/
	private double getFieldWrapper(Magnet magnet) {
                // for design values
		if (paramSrc == ModelProxy.PARAMSRC_DESIGN)
                        return magnet.getDesignField();
                // for live values
		else if (paramSrc == ModelProxy.PARAMSRC_LIVE ||
		         paramSrc == ModelProxy.PARAMSRC_RF_DESIGN) {
			try {
				if (magnet instanceof Electromagnet) {
                                    return ((Electromagnet) magnet).getField();
					//				return -99.d;
				} else {
                                    return magnet.getDesignField();
				}
			} catch (Throwable e) {
				//				throw new Error(e.getMessage());
				if (e.getMessage() != null) {
					System.out.println(e.getMessage());
				} else {
					System.out.println("Electromagnet.getField(): channel access failed: " + magnet.getId());
				}
				return 0.d;
			}
		} 		else
                	return magnet.getDesignField();
	}

	/**A wrapper to read the magnet field strength.*/
	private double getFieldWrapper(Electrostatic magnet) {
                // for design values
		if (paramSrc == ModelProxy.PARAMSRC_DESIGN)
                        return magnet.getDesignField();
                // for live values
		else if (paramSrc == ModelProxy.PARAMSRC_LIVE ||
		         paramSrc == ModelProxy.PARAMSRC_RF_DESIGN) {
			try {
				if (magnet instanceof Electrostatic) {
                                    return magnet.getField();
					//				return -99.d;
				} else {
                                    return magnet.getDesignField();
				}
			} catch (Throwable e) {
				//				throw new Error(e.getMessage());
				if (e.getMessage() != null) {
					System.out.println(e.getMessage());
				} else {
					System.out.println("Electromagnet.getField(): channel access failed: " + magnet.getId());
				}
				return 0.d;
			}
		} 		else
                	return magnet.getDesignField();
	}

} /////////////////////////////////////////
