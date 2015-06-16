# Import tools from XAL
from xal.model.probe import EnvelopeProbe 
from xal.model.alg import EnvTrackerAdapt 
from xal.sim.scenario import AlgorithmFactory 
from xal.sim.scenario import ProbeFactory
from xal.sim.scenario import Scenario 
from xal.smf import AcceleratorNode

#import the XAL hardware objects
from xal.smf import Accelerator
from xal.smf import AcceleratorSeq
from xal.smf import AcceleratorNode
from xal.smf.data import XMLDataManager
from xal.smf.proxy import ElectromagnetPropertyAccessor

strSeqID = 'MEBT'
gblAccelerator = XMLDataManager.loadDefaultAccelerator()
gblSeqTarget = gblAccelerator.getSequence(strSeqID)

algorithm = AlgorithmFactory.createEnvTrackerAdapt(gblSeqTarget)

probe = ProbeFactory.getEnvelopeProbe(gblSeqTarget,algorithm)

model = Scenario.newScenarioFor(gblSeqTarget)

model.setProbe(probe)
model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN)

model.run()

probe = model.getProbe()
trajectory = probe.getTrajectory()

dataFinal = trajectory.finalState()
print(str(dataFinal))
for state in trajectory:
    print(str(state))
