from xal.smf.data import XMLDataManager 
from xal.sim.scenario import ProbeFactory
from xal.sim.scenario import Scenario
from xal.smf import AcceleratorSeqCombo
from xal.ca import ChannelFactory
from xal.sim.scenario import AlgorithmFactory
from xal.tools.xml import XmlDataAdaptor
from xal.tools.data import DataAdaptor
from xal.service.pvlogger.sim import PVLoggerDataSource
from java.util import ArrayList
from java.lang import *
from xal.smf import AcceleratorSeqCombo
from xal.sim.scenario import ScenarioGenerator
from xal.model.alg import TransferMapTracker
from xal.tools.beam import PhaseMatrix
from time import gmtime, strftime
import sys, math, os, time
from xal.tools.math import SquareMatrix
#from xal.app.pta import MainApplication
from Jama import Matrix

from xal.extension.solver import Scorer
from xal.extension.solver import Trial
from xal.extension.solver import Variable
from xal.extension.solver import Stopper
from xal.extension.solver import SolveStopperFactory
from xal.extension.solver import ProblemFactory
from xal.extension.solver import Solver
from xal.extension.solver import Problem
from xal.extension.solver.algorithm import SimplexSearchAlgorithm
from xal.extension.solver.hint import Hint
from xal.extension.solver.hint import InitialDelta


generator = ScenarioGenerator(self.seq)
generator.setHalfMag(False)
model = generator.generateScenario()


dv2 = 1.0
dv3 = 1.0

var = ArrayList()
delta_hint = InitialDelta()


var2 = Variable("id2", dv2, -1.0, 2.0)
var.add(var2)
delta_hint.addInitialDelta(var2, 0.01)

var3 = Variable("id3", dv3, -1.0, 2.0)
var.add(var3)
delta_hint.addInitialDelta(var3, 0.01)



class MyScorer(Scorer):

    def __init__(self, variables):
        self.variables = variables
        self.j = 0

    def score(self, trial, variables_in):
        tp = trial.getTrialPoint()

        dv2 = tp.getValue(self.variables.get(0))
        dv3 = tp.getValue(self.variables.get(1))

        print "dv2, dv3 = ", dv2, dv3



        errX = dv2**2 + dv3**2
        
        print errX

        return errX





maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper(10000)
solver = Solver(SimplexSearchAlgorithm(),maxSolutionStopper)

scorer = MyScorer(var)

problem = ProblemFactory.getInverseSquareMinimizerProblem(var,scorer,0.00001)
problem.addHint(delta_hint)
solver.solve(problem)





sys.exit()



IkickIds = ["Ring_Mag:IKickH01","Ring_Mag:IKickH02","Ring_Mag:IKickH03","Ring_Mag:IKickH04","Ring_Mag:IKickV01","Ring_Mag:IKickV02","Ring_Mag:IKickV03","Ring_Mag:IKickV04"]
caF = ChannelFactory.defaultFactory()


m = 0.9393014e9

T1 = 1011.0e6
T2 = 1002.0e6


for id in IkickIds:
    ch = caF.getChannel(id[:9] + "PS_" + id[9:]+":7121:AMPL")
    V1 = ch.getValDbl()
    V2 = V1*math.sqrt((T2*(2*m + T2))/(T1*(2*m + T1)))
    print V1,"  ",V2
    #ch.putVal(V2)
    
    

["HEBT_Mag:PS_QH12t18e","HEBT_Mag:PS_QV13t19","HEBT_Mag:PS_QH20","HEBT_Mag:PS_QV21","HEBT_Mag:PS_QH22","HEBT_Mag:PS_QV23","HEBT_Mag:PS_QH24","HEBT_Mag:PS_QV25t31o","HEBT_Mag:PS_QH26a28a32"]


"""
doc = XmlDataAdaptor.newEmptyDocumentAdaptor()
print doc.name()
runAdaptor = doc.createChild('run')
kd = runAdaptor.createChild('IKickData')  
kch = runAdaptor.createChild("IKickCoeff")




IkickIds = ["Ring_Mag:IKickH01","Ring_Mag:IKickH02","Ring_Mag:IKickH03","Ring_Mag:IKickH04","Ring_Mag:IKickV01","Ring_Mag:IKickV02","Ring_Mag:IKickV03","Ring_Mag:IKickV04"]

for id in IkickIds:


    runAdaptor.childAdaptor('IKickData').createChild(id)
    runAdaptor.childAdaptor("IKickCoeff").createChild(id)

ca = runAdaptor.childAdaptor('IKickData').childAdaptor("Ring_Mag:IKickH01")
print ca.name()
document = ca.document()
print document.getNodeName()

xmlca = XmlDataAdaptor(document)

print xmlca.name()

xmldoc = xmlca.document()

print xmldoc.getNodeName()


sys.exit()
"""
sys.exit()