package eu.ess.jels;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;

import xal.model.IComponent;
import xal.model.IComposite;
import xal.model.IElement;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.Sector;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.elem.ElementSeq;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.Probe;
import xal.model.xml.LatticeXmlWriter;
import xal.sim.scenario.DefaultElementMapping;
import xal.sim.scenario.ElementMapping;
import xal.sim.scenario.ElsElementMapping;
import xal.sim.scenario.Scenario;
import xal.sim.scenario.ScenarioGenerator2;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.Twiss;
import xal.tools.xml.XmlDataAdaptor;
import eu.ess.jels.model.alg.ElsTracker;
import eu.ess.jels.model.probe.ElsProbe;

public abstract class TestCommon {
	protected Probe probe;
	protected ElementMapping elementMapping;
	protected double beta0,gamma0;
	
	public TestCommon(Probe probe, ElementMapping elementMapping)
	{
		this.probe = probe;
		this.elementMapping = elementMapping;
		beta0 = probe.getBeta();
		gamma0 = probe.getGamma();
	}
	

	@Parameters
	public static Collection<Object[]> probes() {
		return Arrays.asList(new Object[][]{
				{setupProbeViaJavaCalls(), DefaultElementMapping.getInstance()},
				{setupElsProbeViaJavaCalls(), ElsElementMapping.getInstance()}});
	}

	
	
	public static EnvelopeProbe setupProbeViaJavaCalls() {
		// Envelope probe and tracker
		EnvelopeTracker envelopeTracker = new EnvelopeTracker();			
		envelopeTracker.setRfGapPhaseCalculation(true);
		envelopeTracker.setUseSpacecharge(false);
		envelopeTracker.setEmittanceGrowth(false);
		envelopeTracker.setStepSize(0.004);
		envelopeTracker.setProbeUpdatePolicy(Tracker.UPDATE_EXIT);
		
		EnvelopeProbe envelopeProbe = new EnvelopeProbe();
		envelopeProbe.setAlgorithm(envelopeTracker);
		envelopeProbe.setSpeciesCharge(-1);
		envelopeProbe.setSpeciesRestEnergy(9.3829431e8);
		envelopeProbe.setKineticEnergy(3e6);//energy
		envelopeProbe.setPosition(0.0);
		envelopeProbe.setTime(0.0);		
				
		/*
		number of particles = 1000
		beam current in A = 0
		Duty Cycle in %= 4
		normalized horizontal emittance in m*rad= 0.2098e-6
		normalized vertical emittance in m*rad = 0.2091e-6
		normalized longitudinal emittance in m*rad = 0.2851e-6
		kinetic energy in MeV = 3
		alfa x = -0.1763
		beta x in m/rad = 0.2442
		alfa y = -0.3247
		beta y in m/rad = 0.3974
		alfa z = -0.5283
		beta z in m/rad = 0.8684
		 */
		double beta_gamma = envelopeProbe.getBeta() * envelopeProbe.getGamma();
		
		envelopeProbe.initFromTwiss(new Twiss[]{new Twiss(-0.1763,0.2442,0.2098e-6 / beta_gamma),
				  new Twiss(-0.3247,0.3974,0.2091e-6 / beta_gamma),
				  new Twiss(-0.5283,0.8684,0.2851e-6 / beta_gamma)});
		envelopeProbe.setBeamCurrent(0.0);
		envelopeProbe.setBunchFrequency(4.025e8);//frequency
		
		/*CovarianceMatrix cov = ((EnvelopeProbe)envelopeProbe).getCovariance().computeCovariance();
		cov.setElem(4, 4, cov.getElem(4,4)/Math.pow(envelopeProbe.getGamma(),2));
		cov.setElem(5, 5, cov.getElem(5,5)*Math.pow(envelopeProbe.getGamma(),2));
		for (int i=0; i<6; i++) {
			System.out.println();
			for (int j=0; j<6; j++)
				System.out.printf("%E ",cov.getElem(i,j));
		}*/
		
		return envelopeProbe;
	}

	public static ElsProbe setupElsProbeViaJavaCalls() {
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
		elsProbe.setSpeciesRestEnergy(9.3829431e8);
		elsProbe.setKineticEnergy(3e6);//energy
		elsProbe.setPosition(0.0);
		elsProbe.setTime(0.0);		
				
		/*
		number of particles = 1000
		beam current in A = 0
		Duty Cycle in %= 4
		normalized horizontal emittance in m*rad= 0.2098e-6
		normalized vertical emittance in m*rad = 0.2091e-6
		normalized longitudinal emittance in m*rad = 0.2851e-6
		kinetic energy in MeV = 3
		alfa x = -0.1763
		beta x in m/rad = 0.2442
		alfa y = -0.3247
		beta y in m/rad = 0.3974
		alfa z = -0.5283
		beta z in m/rad = 0.8684
		 */
		double beta_gamma = elsProbe.getBeta() * elsProbe.getGamma();
	
		
		elsProbe.initFromTwiss(new Twiss[]{new Twiss(-0.1763,0.2442,0.2098e-6 / beta_gamma),
										  new Twiss(-0.3247,0.3974,0.2091e-6 / beta_gamma),
										  new Twiss(-0.5283,0.8684,0.2851e-6 / beta_gamma)});
		elsProbe.setBeamCurrent(0.0);
		elsProbe.setBunchFrequency(4.025e8);//frequency
		
		return elsProbe;
	}


	public static void saveLattice(Lattice lattice, String file) {				
		try {
			LatticeXmlWriter.writeXml(lattice, file);
		} catch (IOException e1) {
			e1.printStackTrace();			
		}
	}
	
	public static void saveSequence(AcceleratorSeq sequence, String file)
	{
		XmlDataAdaptor da = XmlDataAdaptor.newDocumentAdaptor(sequence, null);
		try {
			da.writeTo(new File(file));
		} catch (IOException e1) {
			e1.printStackTrace();			
		}
	}
	
	
	public void run(AcceleratorSeq sequence) throws ModelException
	{
		// Generates lattice from SMF accelerator
		//Scenario scenario = Scenario.newScenarioFor(sequence);		
		//Scenario scenario = Scenario.newAndImprovedScenarioFor(sequence);
		ScenarioGenerator2 sg2 = new ScenarioGenerator2(sequence, elementMapping);
		sg2.setHalfMag(false);
		Scenario scenario = sg2.generateScenario();

		// Outputting lattice elements
		//new File("temp/bendtest").mkdirs();
		//saveLattice(scenario.getLattice(), "lattice.xml");
		//saveLattice(escenario.getLattice(), "elattice.xml");
		//saveSequence(sequence, "temp/bendtest/seq.xml");
		
		scenario.setProbe(probe);									
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);					
		scenario.resync();
		
		// Prints transfer matrices		
		/*Iterator<IComponent> it = scenario.getLattice().globalIterator();
		while (it.hasNext()) {
			IComponent comp = it.next();
			if (comp instanceof IElement) {
				IElement el = (IElement)comp;
				el.transferMap(probe, el.getLength()).getFirstOrder().print();
				if (el instanceof xal.model.elem.IdealRfGap) {
					xal.model.elem.IdealRfGap gap = (xal.model.elem.IdealRfGap)el;
					System.out.printf("gap phase=%f E0TL=%E\n", gap.getPhase()*180./Math.PI, gap.getETL());
				}
			}
		}*/
			
		
		scenario.run();	
	}

	public void printResults(double elsPosition, double[] elsSigma, double[] elsBeta) {
		// Getting results		
		//Matrix envelope = probe.getEnvelope();
		//System.out.printf("%E %E %E\n", envelope.get(0,0), envelope.get(3,0), envelope.get(6,0));
		System.out.printf("Results of %s:\n", elementMapping.getClass());
				
		Twiss[] t;
		if (probe instanceof ElsProbe)
			t = ((ElsProbe)probe).getTwiss();
		else 
			t = ((EnvelopeProbe)probe).getCovariance().computeTwiss();
		
		
		double[] beta = new double[3];
		for (int i=0; i<3; i++) beta[i] = t[i].getBeta();
				
		beta[2]/=Math.pow(probe.getGamma(),2);
		
		double[] sigma = new double[3];
		for (int i=0; i<3; i++)
			sigma[i] = t[i].getEnvelopeRadius();		
		System.out.printf("alpha %E %E %E\n", t[0].getAlpha(), t[1].getAlpha(), t[2].getAlpha());		
		System.out.printf("beta %E %E %E\n", beta[0], beta[1], beta[2]);
		System.out.printf("emittance %E %E %E\n", t[0].getEmittance(), t[1].getEmittance(), t[2].getEmittance());
		
		System.out.printf("%E %E %E %E\n",probe.getPosition(), sigma[0], sigma[1], sigma[2]);
		
		
		/*CovarianceMatrix cov = ((EnvelopeProbe)probe).getCovariance().computeCovariance();
		cov.setElem(4, 4, cov.getElem(4,4)/Math.pow(probe.getGamma(),2));
		cov.setElem(5, 5, cov.getElem(5,5)*Math.pow(probe.getGamma(),2));
		for (int i=0; i<6; i++) {
			System.out.println();
			for (int j=0; j<6; j++)
				System.out.printf("%E ",cov.getElem(i,j));
		}*/
		
	}
	
	private double tr(double x)
	{
		return Math.signum(x)*Math.pow(10, (int)Math.log10(Math.abs(x)));
	}
	
	protected void checkResults(double[][] cov) {
		double[] alpha = new double[3],beta=new double[3],emit=new double[3], det=new double[3];
		for (int i=0; i<3; i++) 
			det[i]=Math.sqrt(cov[2*i+0][2*i+0]*cov[2*i+1][2*i+1]-cov[2*i+1][2*i+0]*cov[2*i+0][2*i+1]);
		for (int i=0; i<3; i++) {
			alpha[i]=-cov[2*i+1][2*i+0]/det[i];
			beta[i]=cov[2*i+0][2*i+0]/det[i];
			emit[i]=Math.sqrt(det[i]);
		}
		beta[2]*=Math.pow(probe.getGamma(),2);
		System.out.printf("Tracewin alpha: %E %E %E\n",alpha[0],alpha[1],alpha[2]);
		System.out.printf("Tracewin beta: %E %E %E\n",beta[0],beta[1],beta[2]);
		System.out.printf("Tracewin emit: %E %E %E\n",emit[0],emit[1],emit[2]);
		
		Twiss[] t;
		if (probe instanceof ElsProbe)
			t = ((ElsProbe)probe).getTwiss();
		else 
			t = ((EnvelopeProbe)probe).getCovariance().computeTwiss();
		
		System.out.printf("differences ");
		for (int i = 0; i<3; i++) {
			System.out.printf("%c:%.0g %.0g %.0g ",'x'+i, tr(t[i].getAlpha()-alpha[i]), tr(t[i].getBeta()-beta[i]), tr(t[i].getEmittance()-emit[i]));	
		}
		System.out.printf("\n\n");
	}

}
