package eu.ess.jels;

import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.event.*;

import eu.ess.jels.model.alg.ElsTracker;
import eu.ess.jels.model.probe.ElsProbe;
import eu.ess.jels.model.probe.GapEnvelopeProbe;

import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.*;

import xal.application.*;
import xal.model.IElement;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.probe.*;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.xml.LatticeXmlWriter;
import xal.model.xml.ParsingException;
import xal.model.xml.ProbeXmlParser;
import xal.model.xml.ProbeXmlWriter;
import xal.model.alg.*;
import xal.sim.scenario.*;
import xal.smf.data.XMLDataManager;
import xal.smf.impl.Dipole;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.Magnet;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.qualify.AndTypeQualifier;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.smf.impl.qualify.KindQualifier;
import xal.smf.impl.qualify.OrTypeQualifier;
import xal.smf.impl.qualify.QualifierFactory;
import xal.smf.impl.qualify.TypeQualifier;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.smf.proxy.RfCavityPropertyAccessor;
import xal.smf.proxy.RfGapPropertyAccessor;
import xal.smf.*;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;
import xal.tools.plot.*;


public class JElsDemo {


	public static void main(String[] args) throws InstantiationException, ModelException {
		System.out.println("Running\n");
		
		AcceleratorSeq sequence = loadAcceleratorSequence();
				
		// Generates lattice from SMF accelerator
		Scenario scenario = new ScenarioGenerator2(sequence, TWElementMapping.getInstance()).generateScenario();
		Probe probe = setupOpenXALProbe();
		
		/*Scenario scenario = new ScenarioGenerator2(sequence, ElsElementMapping.getInstance()).generateScenario();
		Probe probe = setupElsProbe();*/
		// Outputting lattice elements
		//saveLattice(scenario.getLattice(), "lattice.xml");

		
		// Creating a probe
		//Probe probe = loadProbeFromModelParams(sequence);		
		//Probe probe = loadProbeFromXML();
		
		//saveProbe(probe, "envelopeProbe.xml");				
		scenario.setProbe(probe);			
		
				
		// Setting up synchronization mode
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);					
		scenario.resync();
				
		// Running simulation
		scenario.run();
		
		// Getting results
		Trajectory trajectory = probe.getTrajectory();
		
		EnvelopeProbeState ps = (EnvelopeProbeState) trajectory.stateAtPosition(0); // I was forced to cast from ProbeState to EnvelopeProbeState
		System.out.println(ps.getElementId());
		Iterator<ProbeState> iterState= trajectory.stateIterator();
     
		int ns= trajectory.numStates();
		
		double [] s = new double[ns];
		double [] bx = new double[ns];
		double [] sx = new double[ns];
		double [] by = new double[ns];
		double [] bz = new double[ns];
		double [] w = new double[ns];
        BasicGraphData myDataX = new BasicGraphData();
		int i = 0;
    	while (iterState.hasNext())
     	{
    		ps = (EnvelopeProbeState) iterState.next();
		        
		    s[i]= ps.getPosition() ;
		    String elem=ps.getElementId() ;
		    Twiss[] twiss;	
		    
			if (probe instanceof ElsProbe)
				twiss = ps.getTwiss();
			else 
				twiss = ps.twissParameters();
			
			
		    bx[i] = twiss[0].getBeta();
		    sx[i] = twiss[0].getEnvelopeRadius();
		    by[i] = twiss[1].getBeta();
		    bz[i] = twiss[2].getBeta();
		    w[i] = ps.getKineticEnergy()/1.e6;
		    System.out.printf("%E %E %E %E %E %E %E\n", ps.getPosition(),
		    		twiss[0].getEnvelopeRadius(), twiss[1].getEnvelopeRadius(), twiss[2].getEnvelopeRadius(),
		    		twiss[0].getBeta(), twiss[1].getBeta(), twiss[2].getBeta()/Math.pow(ps.getGamma(), 2));
		    i=i+1;
		}
    	final JFrame frame = new JFrame();
    	FunctionGraphsJPanel plot = new FunctionGraphsJPanel();
     	plot.setVisible(true);
  	    myDataX.addPoint(s, sx);
     	plot.addGraphData(myDataX);
     	plot.setAxisNames("position", "sigma_x");
     	plot.refreshGraphJPanel();
     	frame.setSize(500,500);
        frame.addMouseListener(new MouseAdapter(){
             public void mouseClicked(MouseEvent e) {
                 System.out.println(e.getPoint().getX());
                 System.out.println(e.getPoint().getY());
             }
          });
         frame.add(plot);
         frame.setVisible(true);
         frame.setDefaultCloseOperation(frame.DISPOSE_ON_CLOSE); 
     	System.out.println("End of the program");
     	
     	System.out.println(ElectromagnetPropertyAccessor.PROPERTY_FIELD);
		
	}

	private static Probe setupOpenXALProbe() {
		EnvelopeTracker envelopeTracker = new EnvelopeTracker();			
		envelopeTracker.setRfGapPhaseCalculation(true);
		envelopeTracker.setUseSpacecharge(false);
		envelopeTracker.setEmittanceGrowth(false);
		envelopeTracker.setStepSize(0.004);
		envelopeTracker.setProbeUpdatePolicy(Tracker.UPDATE_EXIT);
		
		EnvelopeProbe envelopeProbe = new GapEnvelopeProbe();
		envelopeProbe.setAlgorithm(envelopeTracker);
		envelopeProbe.setSpeciesCharge(-1);
		envelopeProbe.setSpeciesRestEnergy(9.38272013e8);		
		//envelopeProbe.setSpeciesRestEnergy(9.3829431e8);
		//envelopeProbe.setSpeciesRestEnergy(9.39294e8);
		envelopeProbe.setKineticEnergy(2500000);//energy
		envelopeProbe.setPosition(0.0);
		envelopeProbe.setTime(0.0);		
	
		double beta_gamma = envelopeProbe.getBeta() * envelopeProbe.getGamma();
		
		
		/*envelopeProbe.initFromTwiss(new Twiss[]{new Twiss(-1.62,0.155,3.02e-6/ beta_gamma),
				  new Twiss(3.23,0.381,3.46e-6/ beta_gamma),
				  new Twiss(0.0196,0.5844,3.8638e-6/ beta_gamma)});*/
		
		envelopeProbe.initFromTwiss(new Twiss[]{new Twiss(-0.1763,0.2442,0.2098e-6*1e-6 / beta_gamma),
				  new Twiss(-0.3247,0.3974,0.2091e-6*1e-6 / beta_gamma),
				  new Twiss(-0.5283,0.8684,0.2851e-6*1e-6 / beta_gamma)});
		envelopeProbe.setBeamCurrent(0.0);
		envelopeProbe.setBunchFrequency(4.025e8);//frequency
		
		return envelopeProbe;
	}
	
	public static ElsProbe setupElsProbe() {
		// Envelope probe and tracker
		ElsTracker elsTracker = new ElsTracker();			
		elsTracker.setRfGapPhaseCalculation(false);
		/*envelopeTracker.setUseSpacecharge(false);
		envelopeTracker.setEmittanceGrowth(false);
		envelopeTracker.setStepSize(0.004);*/
		elsTracker.setProbeUpdatePolicy(Tracker.UPDATE_EXIT);
		
		ElsProbe elsProbe = new ElsProbe();
		elsProbe.setAlgorithm(elsTracker);
		elsProbe.setSpeciesCharge(-1);
		//elsProbe.setSpeciesRestEnergy(9.3829431e8);
		elsProbe.setSpeciesRestEnergy(9.38272013e8);	
		elsProbe.setKineticEnergy(2500000);//energy
		elsProbe.setPosition(0.0);
		elsProbe.setTime(0.0);		
				
		double beta_gamma = elsProbe.getBeta() * elsProbe.getGamma();
	
		
		elsProbe.initFromTwiss(new Twiss[]{new Twiss(-0.1763,0.2442,0.2098e-6*1e-6 / beta_gamma),
										  new Twiss(-0.3247,0.3974,0.2091e-6*1e-6 / beta_gamma),
										  new Twiss(-0.5283,0.8684,0.2851e-6*1e-6 / beta_gamma)});
		elsProbe.setBeamCurrent(0.0);
		elsProbe.setBunchFrequency(4.025e8);//frequency
		
		return elsProbe;
	}

	private static Probe loadProbeFromXML() {
		try {			
			Probe probe = ProbeXmlParser.parse("envelopeProbe2.xml");
			//Probe probe = ProbeXmlParser.parse("particleProbe.xml");
			//Probe probe = ProbeXmlParser.parse("synchronousProbe.xml");				
			//Probe probe = ProbeXmlParser.parse("transferMapProbe.xml");
			//Probe probe= ProbeXmlParser.parse("twissProbe.xml");
			//Probe probe = ProbeXmlParser.parse("diagnosticProbe.xml");
			return probe;
		} catch (ParsingException e1) {
			e1.printStackTrace();
		}		
		return null;
	}

	private static void saveProbe(Probe probe, String file) {
		probe.setComment("IL|18/7/2013|Test probe");
		
		try {
			ProbeXmlWriter.writeXml(probe, file);			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	private static Probe loadProbeFromModelParams(AcceleratorSeq sequence) throws InstantiationException {
		// Algorithms that use envelope probe
		Tracker tracker = AlgorithmFactory.createEnvelopeTracker(sequence);
		//Tracker tracker = AlgorithmFactory.createEnvelopeBacktracker(sequence);
		//Tracker tracker = AlgorithmFactory.createEnvelopeTrackerPmqDipole(sequence);
		//Tracker tracker = AlgorithmFactory.createTrace3dTracker(sequence);		
		//Tracker tracker = AlgorithmFactory.createEnvTrackerAdapt(sequence);		
		Probe envelopeProbe = ProbeFactory.getEnvelopeProbe(sequence, tracker);		
			
		// Other probes, that can be loaded from model.params
		Tracker particleTracker = AlgorithmFactory.createParticleTracker(sequence);
		Probe particleProbe = ProbeFactory.createParticleProbe(sequence, particleTracker);

		Tracker transferMapTracker = AlgorithmFactory.createTransferMapTracker(sequence);
		Probe transferMapProbe = ProbeFactory.getTransferMapProbe(sequence, transferMapTracker);

		// Other probes, where only algorithm is loadede from model.params
		Tracker synchronousTracker = AlgorithmFactory.createSynchronousTracker(sequence);
		Probe synchronousProbe = new SynchronousProbe();
		synchronousProbe.setAlgorithm(synchronousTracker);
				
		Tracker twissTracker = AlgorithmFactory.createTwissTracker(sequence);
		TwissProbe twissProbe = new TwissProbe();
		twissProbe.setTwiss(new Twiss3D(new Twiss(0,0,0),new Twiss(0,0,0),new Twiss(0,0,0)));
		twissProbe.setAlgorithm(twissTracker);

		Tracker diagnosticTracker = AlgorithmFactory.createTrackerFor(sequence, DiagnosticTracker.class);
		Probe diagnosticProbe = new DiagnosticProbe();
		diagnosticProbe.setAlgorithm(diagnosticTracker);		
		
		return envelopeProbe;
	}

	private static void saveLattice(Lattice lattice, String file) {		
		lattice.setAuthor("ESSS");
		lattice.setComments("IL|18/7/2013|Testing output of a lattice");
		try {
			LatticeXmlWriter.writeXml(lattice, file);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
	}

	private static AcceleratorSeq loadAcceleratorSequence() {
		/* Loading SMF model */				
		Accelerator accelerator = XMLDataManager.acceleratorWithUrlSpec(JElsDemo.class.getResource("main.xal").toString());
				
		if (accelerator == null)
		{			
			throw new Error("Accelerator is empty. Could not load the default accelerator.");
		} 			
		return accelerator;
		/* Selects a section */
		/*List<AcceleratorSeq> seqs  = accelerator.getAllSeqs();
		for (AcceleratorSeq seq : seqs)
			if ("mebt".equals(seq.getId())) return seq;
		return null;*/
		/*
		AcceleratorSeq sequence = accelerator.findSequence( "mebt" );
		//AcceleratorSeq sequence = accelerator.findSequence( "HEBT2" );				
		return sequence;
*/		
		/* We can instead build lattice for the whole accelerator */
		//return accelerator;
	}
}
