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

import os

strSeqID = 'DTL'
gblAccelerator = XMLDataManager.loadDefaultAccelerator()

gblSeqTarget = gblAccelerator.findSequence(strSeqID)

algorithm = AlgorithmFactory.createEnvTrackerAdapt(gblSeqTarget)
algorithm.setMaxIterations(30000)

model = Scenario.newScenarioFor(gblSeqTarget)


probe = ProbeFactory.getEnvelopeProbe(gblSeqTarget,algorithm)

model.setProbe(probe)
model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN)
model.resync()
model.run()

probe = model.getProbe()
trajectory = probe.getTrajectory()

dataFinal = trajectory.finalState()
#print(str(dataFinal.getSigmaX()))

comparisonFile = os.getcwd() + "/" + strSeqID + "_SigmaX_withTESTING.txt"
with open(comparisonFile, 'w') as f2r:
    for state in trajectory:
        covariance = state.getCovarianceMatrix()
        f2r.write(str(covariance.getSigmaX()) + "\n")
