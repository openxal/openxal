#==================================================================
# This script 
#==================================================================
import sys
import math

from jarray import *

from java.lang import *
from java.util import *
from java.io import *
from javax.swing import *
from java.awt import *
from java.text import *
from java.awt.event import *


from org.xml.sax import *

from gov.sns.xal.smf import *
from gov.sns.xal.smf.data import *
from gov.sns.xal.model import *
from gov.sns.xal.model.probe import *
from gov.sns.xal.model.alg import *
from gov.sns.xal.model.xml import *
from gov.sns.xal.model.scenario import *
from gov.sns.tools.beam import *
from gov.sns.xal.slg import *
from gov.sns.tools.optimizer import *
from gov.sns.tools.xml import XmlDataAdaptor
from gov.sns.tools.beam import *
from gov.sns.tools.math.r3 import R3;
from gov.sns.xal.smf.impl.qualify import *

from gov.sns.tools.plot import *
from gov.sns.tools.statistics import *


rad2deg = 180./math.pi
false= Boolean("false").booleanValue()
true = Boolean("true").booleanValue()

# method to get the phase in deg:
#time t in [seconds] , f= 402.5 MHz
def getPhase(t):
	ph = t * 360.0 * 402.5e6
	return ph

# method to get time extra time from 1/2 a CCL stripline length
def CCLBPMTime(state):
	gamma = 1. + state.getKineticEnergy()/state.getSpeciesRestEnergy()
	beta = math.sqrt(1. - 1./(gamma*gamma))
	v = IConstants.LightSpeed * beta
	return (0.01887/v)

# method to calculate phases after the trajectory is known
# traj - trajectory object (theModel.getProbe().getTrajectory())
# nodeT0 - accelerator node as an initial time node
# bpms - BPMs accelerator nodes array
# phases - phases array 
def setBPM_Phases(traj,nodeT0,bpms,ph_s):
	t0 = traj.statesForElement(nodeT0.getId())[0].getTime()
	for i in xrange(len(bpms)):
		state = traj.statesForElement(bpms[i].getId())[0]
		t = state.getTime() - t0 - CCLBPMTime(state)
		ph_s[i] = getPhase(t)

#-----------------------------------------------------------------
#Function creates multi-dimensional arrays like a[i][k][j]
#a = getMultDimArray(5,10,2)
#or
#a = getMultDimArray(*[5,10,2])
#a[1][2][1] = 0 etc.
#------------------------------------------------------------------
def getMultDimArray(*dims):
	res = []	
	if len(dims) == 1:
		for j in xrange(dims[0]):
			res.append(0.0)
	else:
		dims_rest = dims[1:len(dims)]
		for j in xrange(dims[0]):
			res.append(getMultDimArray(*dims_rest))
	return res

#-----------------------------------------------
# read the accelerator & make the sequence
#-----------------------------------------------
#defaultPath = XMLDataManager.defaultPath()
#accl = XMLDataManager.loadDefaultAccelerator()

workDir = "/home/shishlo/xaldev/xal_xmls/"
accl = XMLDataManager.acceleratorWithPath(workDir + "main_lebt-hebt.xal")

#+++++++++++++++++++++++++++++++++++++++++++++
#   USER HAS TO MODIFY THIS LINEs
#+++++++++++++++++++++++++++++++++++++++++++++
lst = ArrayList()
ccl3 = accl.getSequence("CCL3")
ccl4 = accl.getSequence("CCL4")
lst.add(ccl3)
lst.add(ccl4)
#+++++++++++++++++++++++++++++++++++++++++++++
seq = AcceleratorSeqCombo("SEQUENCE", lst)

# Construct the ON-LINE MODEL ...
theModel = Scenario.newScenarioFor(seq)

# Get the on-line Model lattice ...
theModel.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);

# set machine values:
theModel.resync()
	
# set the probe.
#------------------------------------------------------------------------
# starting probe input file location
#prb_file= workDir + "CCL3Entrance.probe"
#probeXmlAdaptor = XmlDataAdaptor.adaptorForFile( File(prb_file), false)
#initProbe = ProbeXmlParser.parseDataAdaptor(probeXmlAdaptor)
#------------------------------------------------------------------------
tracker = EnvTrackerAdapt(seq)
tracker.setRfGapPhaseCalculation(true)
initProbe = ProbeFactory.getEnvelopeProbe(seq,tracker)
#------------------------------------------------------------------------
#for debug purpose only
#ProbeXmlWriter.writeXml(initProbe,"probe_test.xml")
#------------------------------------------------------------------------
theModel.setProbe(initProbe)
theProbe = theModel.getProbe()


#get all cavities
kq = KindQualifier("Rfcavity");
cavs = seq.getAllInclusiveNodesWithQualifier(kq)

#cavity to scan
theCav = cavs[0]

#the node to take time relative to this
nodeT0 = theCav.getAllNodes()[0]
print "The zero time of flight node: ",nodeT0.getId()

#cavities deactivation
#set amplitudes of all others cavities to 0
cavs.remove(theCav)
for cav in cavs:
	cav.updateDesignAmp(0.0)	

#get all BPMs after nodeT0
kq = KindQualifier("BPM");
bpms_nodes_all = seq.getAllInclusiveNodesWithQualifier(kq)
print "============ALL===BPMs================="
for bpm in bpms_nodes_all:
	print "==BPM node=",bpm.getId()	

#+++++++++++++++++++++++++++++++++++++++++++++
#   USER HAS TO MODIFY THE FOLLOWING FRAGMENT
#+++++++++++++++++++++++++++++++++++++++++++++
#The BPMs that will be considered
bpms_nodes_to_use = [ "CCL_Diag:BPM312" ,\
		      "CCL_Diag:BPM402" ,\
		      "CCL_Diag:BPM409" ,\
		      "CCL_Diag:BPM411"]
#The output files will be generated for
#       the folowing BPMs pairs:
bpms_pairs = [[0,3],\
	      [0,2],\
	      [1,2],\
	      [1,3]]

file_names = ["ccl3_bpm312_bpm411_dt.dat",\
	      "ccl3_bpm312_bpm409_dt.dat",\
	      "ccl3_bpm402_bpm409_dt.dat",\
	      "ccl3_bpm402_bpm411_dt.dat"]
#+++++++++++++++++++++++++++++++++++++++++++++

bpms = []
for bpm_name in bpms_nodes_to_use:
	for bpm in bpms_nodes_all:
		if bpm.getId() == bpm_name:
			bpms.append(bpm)
print "==========Included===BPMs================="
for bpm in bpms:
	print "==BPM node=",bpm.getId()	

if len(bpms) < 2:
	print "====STOP===="
	print "The number of BPMs less than 2"
	print "The Time of Flight Method can not be used!"
	sys.exit(1)

#-------------------------------------------------------------
# get nominal energy in, phase and cavity amplitude
#-------------------------------------------------------------
WIn0 = initProbe.getKineticEnergy();
phase0 = theCav.getDfltCavPhase();
amp0 = theCav.getDfltCavAmp();
print "Nominal values: W0 (eV) = ", WIn0, "  phase (deg)= " , phase0, "  Amp (MV/m)", amp0

#-------------------------------------------------------------
#                   ARRAYS DEFINITIONS
#-------------------------------------------------------------

# amplitude factors to run at
ampFactors = [0.95, 0.96, 0.97, 0.98, 0.99, 1.0, 1.01, 1.02, 1.03, 1.04, 1.05]
#ampFactors = [0.95, 1.0, 1.05]

# Input energy offsets (MeV)
deltaEs = [-0.05, 0., 0.05]
for i_eng in xrange(len(deltaEs)):
	deltaEs[i_eng] = deltaEs[i_eng] * 1.0e+6

# Phase Scan Offsets parameters
deltaPhis_min  = -5.0
deltaPhis_max  =  5.0
deltaPhis_step =  1.0
nSteps = 1 + int((deltaPhis_max - deltaPhis_min)/deltaPhis_step)
deltaPhis = getMultDimArray(nSteps)
for i in xrange(len(deltaPhis)):
	 deltaPhis[i] = deltaPhis_min + deltaPhis_step*i


#phases_off_design[index of BPM]
#   for the cavity off and on and
#   design values of amplitude and energy 
phases_off_design = getMultDimArray(len(bpms))
phases_on_design  = getMultDimArray(len(bpms))

#phases_off_design[index of init. energy][index of BPM]
#   for the cavity off and different initial energies
phases_off = getMultDimArray(len(deltaEs),len(bpms))

#phases_off_design[index of init. energy]
#                 [index of amplitude]
#                 [index of phase shift]
#                 [index of BPM]
phases = getMultDimArray(len(deltaEs),\
			 len(ampFactors),\
			 len(deltaPhis),\
			 len(bpms))


#-----------------------------------------------------------------
#    1. STAGE - The Cavity is Off - All Params have design values 
#-----------------------------------------------------------------
theProbe.reset()
theModel.resync()
theModel.run()
traj = theModel.getProbe().getTrajectory()
setBPM_Phases(traj,nodeT0,bpms,phases_on_design)
print "1. STAGE - The Cavity is Off - DONE!"


#-----------------------------------------------------------------
#    2. STAGE - The Cavity is On - All Others Params have design values 
#-----------------------------------------------------------------
theProbe.reset()
theCav.updateDesignAmp(0.0)
theModel.resync()
theModel.run()
traj = theModel.getProbe().getTrajectory()
setBPM_Phases(traj,nodeT0,bpms,phases_off_design)
print "2. STAGE - The Cavity is On - DONE!"

for i in xrange(len(bpms)):
	print "BPM : ",bpms[i].getId()," phase(off - on)=", phases_off_design[i] - phases_on_design[i]


#-----------------------------------------------------------------
#    3. STAGE - The Cavity is Off - scan over the initial energy
#-----------------------------------------------------------------

count = 0
count_max = len(deltaEs)

theCav.updateDesignAmp(0.0)

for i_eng in xrange(len(deltaEs)):
	WIn = WIn0 + deltaEs[i_eng]
	theProbe.reset()
	theProbe.setKineticEnergy(WIn)
	theModel.resync()
	theModel.run()
	traj = theModel.getProbe().getTrajectory()
	setBPM_Phases(traj,nodeT0,bpms,phases_off[i_eng])
	count += 1
	print "Calculation with cavity OFF #",count," from ",count_max," - DONE!"

print "3. STAGE - The Cavity is off - DONE!"

#-----------------------------------------------------------------------
#    4. STAGE - The Cavity is on - scan over energy, amplitude and phase
#-----------------------------------------------------------------------

print "4. STAGE - The scan over energy, amplitude and phase - START! Please wait!"

count = 0
count_max = len(deltaEs)*len(ampFactors)*len(deltaPhis)

for i_amp in xrange(len(ampFactors)):
	amp = amp0 * ampFactors[i_amp]
	for i_eng in xrange(len(deltaEs)):
		WIn = WIn0 + deltaEs[i_eng]
		for i_ph in xrange(len(deltaPhis)):
			phase = phase0 + deltaPhis[i_ph]
			theProbe.reset()
			theProbe.setKineticEnergy(WIn)
			theCav.updateDesignPhase(phase)
			theCav.updateDesignAmp(amp)
			theModel.resync()
			theModel.run()
			traj = theModel.getProbe().getTrajectory()
			setBPM_Phases(traj,nodeT0,bpms,phases[i_eng][i_amp][i_ph])
			for i_bpm in xrange(len(bpms)):
				ph = phases[i_eng][i_amp][i_ph][i_bpm]
				ph = phases_off[i_eng][i_bpm] - ph
				ph = ph - (phases_off_design[i_bpm] - phases_on_design[i_bpm])
				phases[i_eng][i_amp][i_ph][i_bpm] = ph
			count += 1
			if (count % 30) == 0:
				print "Calculation #",count," from ",count_max," - DONE!"

#--------------------------------------------
#    5. STAGE - PLOT GRAPHs AND SHOW THEM
#--------------------------------------------

print "5. STAGE - Plot graphs. - START!"

jf = JFrame("dT - data generation. Keep this frame!")
jf.pack()
jf.setSize(Dimension(600,400))
n_y = len(bpms_pairs)
if (n_y % 2) == 0:
	n_y = int(n_y / 2)
else:
	n_y = int(n_y / 2) + 1	
jf.getContentPane().setLayout(GridLayout(n_y,2))

for pair in bpms_pairs:
	bpm_ind_0 = pair[0]
	bpm_ind_1 = pair[1]
	bpm_name_0 = bpms_nodes_to_use[pair[0]]
	bpm_name_1 = bpms_nodes_to_use[pair[1]]
	JP = FunctionGraphsJPanel()
	JP.setOffScreenImageDrawing(true)
	JP.setName("Scan for "+bpm_name_0+" and "+bpm_name_1)
	JP.setAxisNames("dPhy1, [dgr]","dPhy2, [dgr]")
	JP.setNumberFormatX(DecimalFormat("##0.0"))
	JP.setNumberFormatY(DecimalFormat("##0.0"))
	
	for i_eng in xrange(len(deltaEs)):
		for i_amp in xrange(len(ampFactors)):
			graph = BasicGraphData()
			color = IncrementalColors.getColor(i_amp)
			graph.setGraphColor(color)
			graph.setDrawPointsOn(false)
			for i_ph in xrange(len(deltaPhis)):
				x = phases[i_eng][i_amp][i_ph][bpm_ind_0]
				y = phases[i_eng][i_amp][i_ph][bpm_ind_1]
				graph.addPoint(x,y)
			JP.addGraphData(graph)
	jf.getContentPane().add(JP)


#action after window closing
class customWindowAdapter(WindowAdapter):
    def windowClosing(self,obj):
        sys.exit(0)
	
jf.addWindowListener(customWindowAdapter())
	
#show graphs
jf.setVisible(true)

print "5. STAGE - Plot graphs. - DONE!"

#--------------------------------------------
#    6. STAGE - DUMP the dT Procedure files
#--------------------------------------------

print "6. STAGE - Preparing and writing the files for Scan1D dT analysis. - START!"

Angle_arr = getMultDimArray(len(bpms_pairs),len(ampFactors))
A_arr = getMultDimArray(len(bpms_pairs),len(ampFactors),2,2)
delta_phi_arr = getMultDimArray(len(bpms_pairs),2)

x_arr = [0.0, 0.0]
y_arr = [0.0, 0.0]

for i_pair in xrange(len(bpms_pairs)):
	pair = bpms_pairs[i_pair]
	bpm_ind_0 = pair[0]
	bpm_ind_1 = pair[1]
	bpm_0 = bpms[bpm_ind_0]
	bpm_1 = bpms[bpm_ind_1]
	for i_amp in xrange(len(ampFactors)):
		mapEst = MapEstimator(2,2)
		for i_eng in xrange(len(deltaEs)):
			dE = deltaEs[i_eng]
			for i_ph in xrange(len(deltaPhis)):
				phase = deltaPhis[i_ph]
				y_arr[0] = phase
				y_arr[1] = dE
				x_arr[0] = phases[i_eng][i_amp][i_ph][bpm_ind_0]
				x_arr[1] = phases[i_eng][i_amp][i_ph][bpm_ind_1]
				mapEst.addSample(x_arr,y_arr)
		a_matr = mapEst.mapEstimate()
		#------------------------------------------------------
		# a(1,1) and a(1,2) elements have [eV/rad] dimension
		#------------------------------------------------------
		a_matr[1][0] = a_matr[1][0] * (180./math.pi)
		a_matr[1][1] = a_matr[1][1] * (180./math.pi)
		A_arr[i_pair][i_amp] = a_matr
		Angle_arr[i_pair][i_amp] = math.atan(-a_matr[1][0]/a_matr[1][1]) * (180./math.pi)
		delta_phi_arr[i_pair][0] = phases_off_design[bpm_ind_0] - phases_on_design[bpm_ind_0]
		delta_phi_arr[i_pair][1] = phases_off_design[bpm_ind_1] - phases_on_design[bpm_ind_1]
		#print "BPMs=",bpms_nodes_to_use[pair[0]]," ",bpms_nodes_to_use[pair[1]],
		#print " amp= %13.4g"%(ampFactors[i_amp]),
		#print " A = %13.4g %13.4g  %13.4g  %13.4g  "% (a_matr[0][0],a_matr[0][1],a_matr[1][0],a_matr[1][1])


cav_name = theCav.getId()

for i_pair in xrange(len(bpms_pairs)):
	pair = bpms_pairs[i_pair]
	bpm_ind_0 = pair[0]
	bpm_ind_1 = pair[1]
	bpm_0 = bpms[bpm_ind_0]
	bpm_1 = bpms[bpm_ind_1]
	bpm_name_0 = bpm_0.getId()
	bpm_name_1 = bpm_1.getId()
	file_name = file_names[i_pair]
	file = open(file_name, 'wt')
	file.write("% CAVITY name = "+cav_name+"\n")
	file.write("% bpm_1 : "+bpm_name_0+"\n")
	file.write("% bpm_2 : "+bpm_name_1+"\n")
	file.write("% (amp E/E0) (arctg(d(phi_1)/d(phi2)), deg) (a11) (a12) (a21,eV/rad) (a22.eV/rad)\n")
	file.write("% first line (phi_off - phi_on) for bpm 1 and 2 \n")
	file.write(" %7.4f"%(delta_phi_arr[i_pair][0])+" %7.4f"%(delta_phi_arr[i_pair][1]) + "\n")
	for i_amp in xrange(len(ampFactors)):
	      buffer = " %12.5g "%(ampFactors[i_amp])
	      buffer = buffer + " %12.5g "% (Angle_arr[i_pair][i_amp])
	      buffer = buffer + " %12.5g "% (A_arr[i_pair][i_amp][0][0])
	      buffer = buffer + " %12.5g "% (A_arr[i_pair][i_amp][0][1])
	      buffer = buffer + " %12.5g "% (A_arr[i_pair][i_amp][1][0])
	      buffer = buffer + " %12.5g "% (A_arr[i_pair][i_amp][1][1])
	      buffer = buffer + "\n"
	      file.write(buffer)
	file.write(" ")
	file.close()
	
print "6. STAGE - Preparing and writing the files for Scan1D dT analysis. - DONE!"

#sys.exit(1)

