package eu.ess.jels;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.junit.runners.Parameterized.Parameters;

import xal.model.IComponent;
import xal.model.IElement;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.Probe;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.xml.LatticeXmlWriter;
import xal.sim.scenario.DefaultElementMapping;
import xal.sim.scenario.ElementMapping;
import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.Twiss;
import xal.tools.xml.XmlDataAdaptor;
import eu.ess.jels.model.alg.ElsTracker;
import eu.ess.jels.model.elem.els.ElsElementMapping;
import eu.ess.jels.model.elem.els.IdealRfGap;
import eu.ess.jels.model.elem.jels.JElsElementMapping;
import eu.ess.jels.model.probe.ElsProbe;

public abstract class TestCommon {
	protected Probe probe;
	protected ElementMapping elementMapping;
	protected Scenario scenario;
	protected double initialEnergy;
	
	public TestCommon(Probe probe, ElementMapping elementMapping)
	{
		System.out.printf("\nResults of %s:\n", elementMapping.getClass());
		probe.reset();
		this.initialEnergy = probe.getKineticEnergy();
		this.probe = probe;
		this.elementMapping = elementMapping;
	}
	

	@Parameters
	public static Collection<Object[]> probes() {
		//double energy = 3e6, frequency = 4.025e8, current = 0;
		double energy = 2.5e9, frequency = 4.025e8, current = 0;
		return Arrays.asList(new Object[][]{
					{setupOpenXALProbe(energy, frequency, current), JElsElementMapping.getInstance()},
					{setupElsProbe(energy, frequency, current), ElsElementMapping.getInstance()},					
					{setupOpenXALProbe(energy, frequency, current), DefaultElementMapping.getInstance()},
				});
	}
	
	public static EnvelopeProbe setupOpenXALProbe(double energy, double frequency, double current) {
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
		envelopeProbe.setKineticEnergy(energy);//energy
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
		
		envelopeProbe.initFromTwiss(new Twiss[]{new Twiss(-0.1763,0.2442,0.2098e-6*1e-6 / beta_gamma),
				  new Twiss(-0.3247,0.3974,0.2091e-6*1e-6 / beta_gamma),
				  new Twiss(-0.5283,0.8684,0.2851e-6*1e-6 / beta_gamma)});
		envelopeProbe.setBeamCurrent(current);
		envelopeProbe.setBunchFrequency(frequency);//frequency
		
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

	public static ElsProbe setupElsProbe(double energy, double frequency, double current) {
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
		elsProbe.setKineticEnergy(energy);//energy
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
	
		
		elsProbe.initFromTwiss(new Twiss[]{new Twiss(-0.1763,0.2442,0.2098e-6*1e-6 / beta_gamma),
										  new Twiss(-0.3247,0.3974,0.2091e-6*1e-6 / beta_gamma),
										  new Twiss(-0.5283,0.8684,0.2851e-6*1e-6 / beta_gamma)});
		elsProbe.setBeamCurrent(current);
		elsProbe.setBunchFrequency(frequency);//frequency
		
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
		
		scenario = Scenario.newScenarioFor(sequence,  elementMapping);

		// Outputting lattice elements
		//new File("temp/").mkdirs();
		//saveLattice(scenario.getLattice(), "temp/lattice.xml");
		
		scenario.setProbe(probe);									
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);					
		scenario.resync();
		
		scenario.run();
		
		// Prints transfer matrices		
		//printTransferMatrices(scenario);
	}
	
	public void printTransferMatrices(Scenario scenario) throws ModelException
	{
		Iterator<IComponent> it = scenario.getLattice().globalIterator();
		PhaseMap pm = PhaseMap.identity();
		while (it.hasNext()) {
			IComponent comp = it.next();
			if (comp instanceof IElement) {
				IElement el = (IElement)comp;
				//el.transferMap(probe, el.getLength()).getFirstOrder().print();
				pm = pm.compose(el.transferMap(probe, el.getLength()));
				if (el instanceof xal.model.elem.IdealRfGap) {
					xal.model.elem.IdealRfGap gap = (xal.model.elem.IdealRfGap)el;
					System.out.printf("gap phase=%f E0TL=%E\n", gap.getPhase()*180./Math.PI, gap.getETL());
				}
				if (el instanceof IdealRfGap) {
					IdealRfGap gap = (IdealRfGap)el;
					System.out.printf("gap phase=%f E0TL=%E\n", gap.getPhase()*180./Math.PI, gap.getETL());
				}
				if (el instanceof eu.ess.jels.model.elem.jels.IdealRfGap) {
					eu.ess.jels.model.elem.jels.IdealRfGap gap = (eu.ess.jels.model.elem.jels.IdealRfGap)el;
					System.out.printf("gap phase=%f E0TL=%E\n", 360+Math.IEEEremainder(gap.getPhase()*180./Math.PI, 360), gap.getETL());
				}
			}
		}
		pm.getFirstOrder().print(new PrintWriter(System.out));
	}
	

	public void printResults() {
		// Getting results		
		//Matrix envelope = probe.getEnvelope();
		//System.out.printf("%E %E %E\n", envelope.get(0,0), envelope.get(3,0), envelope.get(6,0));
		System.out.printf("Results:\n");
				
		Twiss[] t;
		if (probe instanceof ElsProbe)
			t = ((ElsProbe)probe).getTwiss();
		else 
			t = ((EnvelopeProbe)probe).getCovariance().computeTwiss();
		
		
		double[] beta = new double[3];
		for (int i=0; i<3; i++) beta[i] = t[i].getBeta();
				
		beta[2]/=Math.pow(probe.getGamma(),2);

		System.out.printf("alpha: %E %E %E\n", t[0].getAlpha(), t[1].getAlpha(), t[2].getAlpha());		
		System.out.printf("beta: %E %E %E\n", beta[0], beta[1], beta[2]);
		System.out.printf("emittance: %E %E %E\n", t[0].getEmittance(), t[1].getEmittance(), t[2].getEmittance());
		
		double[] sigma = new double[3];
		for (int i=0; i<3; i++)
			sigma[i] = t[i].getEnvelopeRadius();		

		
		System.out.printf("sigma: %E %E %E %E\n",probe.getPosition(), sigma[0], sigma[1], sigma[2]);		
	}
	
	public void checkELSResults(double elsPosition, double[] elsSigma, double[] elsBeta)
	{
		Twiss[] t;
		if (probe instanceof ElsProbe)
			t = ((ElsProbe)probe).getTwiss();
		else 
			t = ((EnvelopeProbe)probe).getCovariance().computeTwiss();
		
		elsBeta[2]*=Math.pow(probe.getGamma(),2);
		elsSigma[2]*=probe.getGamma();
		
		double e = 0.,e0 = 0.;
		for (int i=0; i<3; i++) {			
			e+=Math.pow(elsSigma[i]-t[i].getEnvelopeRadius()*1e3,2);
			e0+=Math.pow(elsSigma[i],2);
		}
		
		double b = 0.,b0 = 0.;
		for (int i=0; i<3; i++) {
			b+=Math.pow(elsBeta[i]-t[i].getBeta(),2);
			b0+=Math.pow(elsBeta[i],2);
		}		
		
		//System.out.printf("ELS results diff: %E %E %E\n", Math.abs(elsPosition-probe.getPosition())/elsPosition, Math.sqrt(e/e0), Math.sqrt(b/b0));
	}
	
	public void checkTWTransferMatrix(double T[][]) throws ModelException
	{		
		Iterator<IComponent> it = scenario.getLattice().globalIterator();		
		Trajectory trajectory = probe.getTrajectory();
		// TODO REMOVE WORKAROUND there's a bug in OpenXal trajectory iterator gives incorrect order of elements
		//Iterator<ProbeState> it2 = trajectory.stateIterator();
		
		Probe probe = this.probe.copy();
		PhaseMap pm = PhaseMap.identity();		
		
		ProbeState initialState = null;
		ProbeState ps = null;
		
		while (it.hasNext()) {
			IComponent comp = it.next();
			if (comp instanceof IElement) {				
				IElement el = (IElement)comp;				
				//el.transferMap(probe, el.getLength()).getFirstOrder().print();
				ProbeState psnext = trajectory.stateForElement(el.getId());
				if (ps != null) {
					probe.applyState(ps);
					PhaseMap element_pm = el.transferMap(probe, el.getLength());
					
					if (!(elementMapping instanceof ElsElementMapping))
						ROpenXal2TW(ps.getGamma(), psnext.getGamma(), element_pm);					
					
					pm = element_pm.compose(pm);
				}
				ps = psnext;
				
				if (initialState == null) initialState = ps;
			}
		}
		
		// transform T
	/*	if (!(elementMapping instanceof ElsElementMapping)) {
			double gamma_start = initialState.getGamma();
			double gamma_end = ps.getGamma();
			
			for (int i=0; i<6; i++) {
				T[i][4]/=gamma_start;
				T[i][5]*=gamma_start;
				T[4][i]*=gamma_end;
				T[5][i]/=gamma_end;
			}			
		}*/
		//pm.getFirstOrder().print();
		double T77[][] = new double[7][7];		
		for (int i=0; i<6; i++)
			for (int j=0; j<6; j++)
				T77[i][j] = T[i][j];
		PhaseMatrix pm77 = new PhaseMatrix(T77);
		double n = pm.getFirstOrder().minus(pm77).norm2()/pm77.norm2();
		//pm.getFirstOrder().minus(new PhaseMatrix(new Matrix(T77))).print();
		System.out.printf("TW transfer matrix diff: %E\n",n);
	}
	
	
	private void ROpenXal2TW(double gamma_start, double gamma_end, PhaseMap pm) {		
		PhaseMatrix r = pm.getFirstOrder();
		
		for (int i=0; i<6; i++) {
			r.setElem(i, 4, r.getElem(i,4)*gamma_start);
			r.setElem(i, 5, r.getElem(i,5)/gamma_start);
			r.setElem(4, i, r.getElem(4,i)/gamma_end);
			r.setElem(5, i, r.getElem(5,i)*gamma_end);			
		}			
		pm.setLinearPart(r);
	}


	private double tr(double x, double y)
	{
		//return Math.signum(x-y)*Math.pow(10, (int)Math.log10(Math.abs((x-y)/x)));
		return (x-y)/y;
	}
	
	protected void checkTWResults(double gamma, double[][] cov) {
		/*double[] alpha = new double[3],beta=new double[3],emit=new double[3], det=new double[3], sigma=new double[3], gama=new double[3];
			
		for (int i=0; i<3; i++) 
			det[i]=Math.sqrt(cov[2*i+0][2*i+0]*cov[2*i+1][2*i+1]-cov[2*i+1][2*i+0]*cov[2*i+0][2*i+1]);		
		for (int i=0; i<3; i++) {
			alpha[i]=-cov[2*i+1][2*i+0]/det[i];
			beta[i]=cov[2*i+0][2*i+0]/det[i];
			emit[i]=det[i];
		}
				
		System.out.printf("Tracewin alpha: %E %E %E\n",alpha[0],alpha[1],alpha[2]);
		System.out.printf("Tracewin beta: %E %E %E\n",beta[0],beta[1],beta[2]);
		System.out.printf("Tracewin emit: %E %E %E\n",emit[0],emit[1],emit[2]);*/
		
		/*Twiss[] t;
		if (probe instanceof ElsProbe)
			t = ((ElsProbe)probe).getTwiss();
		else 
			t = ((EnvelopeProbe)probe).getCovariance().computeTwiss();*/
			
		/*
		System.out.printf("TW results differences:\n");
		for (int i = 0; i<3; i++) {
			System.out.printf("%c:%.0g %.0g %.0g ",'x'+i, tr(t[i].getAlpha(),alpha[i]), tr(t[i].getBeta(),beta[i]), tr(t[i].getEmittance(),emit[i]));	
		}*/
		System.out.printf("TW gamma diff: %.2g\n", tr(probe.getGamma(),gamma));
		
		
		// transform cov
		for (int i=0; i<6; i++) {
			cov[i][4]*=gamma;
			cov[i][5]/=gamma;
			cov[4][i]*=gamma;				
			cov[5][i]/=gamma;
		}

		double cov77[][] = new double[7][7];		
		for (int i=0; i<6; i++)
			for (int j=0; j<6; j++)
				cov77[i][j] = cov[i][j];
		CovarianceMatrix pcov = ((EnvelopeProbe)probe).getCovariance();
		PhaseMatrix pcov77 = new PhaseMatrix(cov77);
		double n = pcov.minus(new PhaseMatrix(cov77)).norm2()/pcov77.norm2();
		//pm.getFirstOrder().minus(new PhaseMatrix(new Matrix(T77))).print();
		System.out.printf("TW cov matrix diff: %E\n",n);
	}

}
