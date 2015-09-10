#
# This script uses the online model to match
# HEBT transfer line twiss to predetermined values 02/11/04 (S. Danilov)
#
import sys
import math

from jarray import *
from java.lang import *
from java.util import *
from java.io import *

from gov.sns.xal.smf import *
from gov.sns.xal.smf.data import *
from gov.sns.xal.smf.proxy import *
from gov.sns.xal.model import *
from gov.sns.xal.model.probe import *
from gov.sns.xal.model.alg import *
from gov.sns.xal.model.scenario import *
import gov.sns.tools.math.r3.R3;

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()

# read the accelerator & make the sequence

#defaultPath = XMLDataManager.defaultPath()
#accl = XMLDataManager.loadDefaultAccelerator()
workDir = "/user1/chu/"
accl = XMLDataManager.acceleratorWithPath(workDir + "xaldev/xal_xmls/main_lebt-hebt.xal")

#seq = accl.getSequence("HEBT1")
ccl1 = accl.getSequence("CCL1")
cav1 = ccl1.getNodeWithId("CCL1")

# Construct the ON-LINE MODEL ...
model = Scenario.newScenarioFor(ccl1)
initProbe = ProbeFactory.getEnvelopeProbe(ccl1, EnvTrackerAdapt(ccl1))
model.setProbe(initProbe)

# Get the on-line model lattice ...

model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);

# turn off the cavity
model.setModelInput(cav1, RfCavityPropertyAccessor.PROPERTY_AMPLITUDE, 0.)

# set machine values:
model.resync()
model.run()






