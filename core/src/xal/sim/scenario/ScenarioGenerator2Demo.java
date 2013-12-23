package xal.sim.scenario;
import java.io.IOException;
import java.io.File;

import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.xml.LatticeXmlWriter;
import xal.sim.scenario.NewAndImprovedScenarioElementMapping;
import xal.sim.scenario.ScenarioGenerator2;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;


public class ScenarioGenerator2Demo {


	public static void main(String[] args) throws InstantiationException, ModelException {
		System.out.println("Running\n");
		
		Accelerator accelerator = loadAccelerator();
		
		new File("sgtest/new").mkdirs();
		new File("sgtest/old").mkdirs();
		
		for (AcceleratorSeq sequence : accelerator.getSequences()) {			
			// Generates lattice from SMF accelerator
			Scenario scenario = Scenario.newScenarioFor(sequence);
			
			ScenarioGenerator sg = new ScenarioGenerator(sequence);
			Scenario scenarioOld = sg.generateScenario();		
			//Scenario scenario = Scenario.newAndImprovedScenarioFor(sequence);
			//ScenarioGenerator2 sg2 = new ScenarioGenerator2(sequence, NewAndImprovedScenarioElementMapping.getInstance());			
			
			// Outputting lattice elements
			saveLattice(scenario.getLattice(), "sgtest/new/lattice-"+sequence.getId()+".xml");
			saveLattice(scenarioOld.getLattice(), "sgtest/old/lattice-"+sequence.getId()+".xml");			
		}
	}

	private static void saveLattice(Lattice lattice, String file) {			
		try {
			LatticeXmlWriter.writeXml(lattice, file);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
	}

	private static Accelerator loadAccelerator() {
		/* Loading SMF model */		
		Accelerator accelerator = XMLDataManager.loadDefaultAccelerator();
		//Accelerator accelerator = XMLDataManager.acceleratorWithPath("../main.xal");
		if (accelerator == null)
		{			
			throw new Error("Accelerator is empty. Could not load the default accelerator.");
		} 					
				
		return accelerator;		
	}
}
