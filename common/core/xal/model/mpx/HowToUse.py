#!/usr/bin/env jython
import sys
from java.lang import *
from java.util import *
from java.io import *
from gov.sns.xal.smf import *
from gov.sns.xal.smf.data import *
from gov.sns.xal.model import *
from gov.sns.xal.model.xml import *
from gov.sns.xal.model.mpx import *
from gov.sns.xal.model.scenario import *
from gov.sns.tools.beam import *
from gov.sns.xal.slg import *
from gov.sns.tools.xml import XmlDataAdaptor

###################################################################################
false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()

# flags to control the script's actions ...
#probin="particle"
probin="envelope"
#useChannelAcces = true;
useChannelAcces = false;

xalDir = "/home/jdg/xaldev"
# probe input locations
prb_file={
	"particle":xalDir + "/xal_xmls/probe.particle.MebtEntrance..xml",
	"envelope":xalDir + "/xal_xmls/probe.envelope.MebtEntrance-adapt.xml"}
print prb_file

def getAccelerator():
	"""Get the XAL datagraph from the default path"""
	defaultPath = XMLDataManager.defaultPath()
	acc = XMLDataManager.loadDefaultAccelerator()
	print "reading accelerator from",defaultPath
	return acc

def showParticleProbe(traj):
	iterState= traj.stateIterator()
	count=0
	print "PARTICLE PROBE RESULTS"
	titles =  "  Position      Energy        x            x'          y           y'"
	titles+=  "\n======================================================================="
	print titles
	while iterState.hasNext():
		count += 1
		if count%10==0:
			print "\n",titles

		state= iterState.next()
		s= state.getPosition()
		W= state.getKineticEnergy()
		phasevect= state.phaseCoordinates()
		x= phasevect.getx()
		xp=phasevect.getxp()
		y =phasevect.gety()
		yp=phasevect.getyp()

		#digits=8
		#buffer = repr(s)[:digits]
		#buffer+= "  "+repr(W)[:digits]
		#buffer+= "  "+repr(x)[:digits]
		#buffer+= "  "+repr(xp)[:digits]
		#buffer+= "  "+repr(y)[:digits]
		#buffer+= "  "+repr(yp)[:digits]

		float="%+010f"
		scien="%+010.3e"
		digits = float % s
		buffer = digits
		digits = scien % W
		buffer += "  "+digits
		digits = float % x
		buffer += "  "+digits
		digits = float % xp
		buffer += "  "+digits
		digits = float % y
		buffer += "  "+digits
		digits = float % yp
		buffer += "  "+digits
		buffer += "  "+state.getElementId()
		print buffer

def showEnvelopeProbe(traj):
	iterState= traj.stateIterator()
	count=0
	print "ENVELOPE PROBE RESULTS"
	titles =  "  Position      eps-x         x            x'        eps-y          y           y'"
	titles+=  "\n===================================================================================="
	print titles
	while iterState.hasNext():
		count += 1
		if count%10==0:
			print "\n",titles

		state= iterState.next()
		s= state.getPosition()
		twiss= state.twissParameters()

		x = twiss[0].getEnvelopeRadius()
		xp= twiss[0].getEnvelopeSlope()
		ex= twiss[0].getEmittance()

		y = twiss[1].getEnvelopeRadius()
		yp= twiss[1].getEnvelopeSlope()
		ey= twiss[1].getEmittance()

		float="%+010f"
		scien="%+010.3e"
		digits = float % s
		buffer = digits
		digits = scien % ex
		buffer += "  "+digits
		digits = float % x
		buffer += "  "+digits
		digits = float % xp
		buffer += "  "+digits
		digits = scien % ey
		buffer += "  "+digits
		digits = float % y
		buffer += "  "+digits
		digits = float % yp
		buffer += "  "+digits
		buffer += "  "+state.getElementId()
		print buffer

if __name__ == '__main__':

	######################################################
	# get the DEFAULT ACCELERATOR ...
	######################################################
	acc=getAccelerator()

	######################################################
	# ... and a SEQUENCE.
	######################################################
	sequence=acc.getSequence("MEBT")

	######################################################
	# Construct the ON-LINE MODEL ...
	######################################################

	try:

		# ... and let the model generate the LATTICE.

		model = Scenario.newScenarioFor(sequence)		

		# Get the on-line model lattice ...
		lattice= model.getLattice()
		#  to dump some lattice info to output.
		buffer =    "LATTICE - "+lattice.getType()

		buffer += "\nID        :"+lattice.getId()
		buffer += "\nAuthor    :"+lattice.getAuthor()
		#buffer += "\nDate      :"+lattice.getDate()
		#buffer += "\nVersion   :"+lattice.getVersion()
		buffer += "\nComments  :"+lattice.getComments()
		buffer += "\nChildren  :"+repr(lattice.getChildCount())
		buffer += "\nLeaves    :"+repr(lattice.getLeafCount())
		buffer += "\nLength    :"+repr(lattice.getLength())
		buffer += "\n================================================"
		print buffer

		cout= PrintWriter(System.out, Boolean("true"))

	except ParsingException:
		print ParsingException.getMessage()	
		sys.exit(-1)
	except Exception:
		print Exception.getMessage()
		sys.exit(-1)
		
	try:
		cin=prb_file[probin]
		print "Using '",cin,"' as probe input"
		print "======================================================"
		
		######################################################
		# Let the model parse the PROBE.
		######################################################
		probeXmlAdaptor = XmlDataAdaptor.adaptorForFile( File(cin), false)
		initProbe = ProbeXmlParser.parseDataAdaptor(probeXmlAdaptor)
		model.setProbe(initProbe)

		# to dump some initial probe parameters to output.
		buffer  = "PROBE - " +repr(initProbe.getComment())
		buffer += "\nBeta               :"+repr(initProbe.getBeta())
		buffer += "\nGamma              :"+repr(initProbe.getGamma())
		buffer += "\nKinetic Energy     :"+repr(initProbe.getKineticEnergy())
		buffer += "\nPosition           :"+repr(initProbe.getPosition())
		buffer += "\nSpecies Charge     :"+repr(initProbe.getSpeciesCharge())
		buffer += "\nSpecies Rest Energy:"+repr(initProbe.getSpeciesRestEnergy())
		buffer += "\n================================================"
		print buffer
	except ParsingException:
		print ParsingException.getMessage()
		sys.exit(-1)
	except Exception:
		print Exception.getMessage()
		sys.exit(-1)

	try: 
		######################################################
		# SYNCHRONIZE the lattice with the real machine.
		######################################################
		if useChannelAcces:
			model.setSynchronizationMode(Scenario.SYNC_MODE_LIVE)
		else:
			model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN)
	except LatticeError:
		print LatticeError.getMessage();
	except ParsingException:
		print ParsingException.getMessage();
	except Exception:
		print Exception.getMessage();

	try:
		######################################################
		# RUN model, i.e. propagate the probe through the lattice ...
		######################################################
		model.run()

		######################################################
		# ... and reget the PROBE WHICH CARRIES RESULTS now ...
		######################################################
		probe=model.getProbe()

		######################################################
		# ... and get RESULTS.
		######################################################
		traj= probe.getTrajectory()
		traj.setDescription("validation trajectory")
	except ModelException:
		print ModelException.getMessage()
		sys.exit(-1)
	except Exception:
		print Exception.getMessage()
		sys.exit(-1)

	######################################################
	# PRINT results.
	######################################################
	if probin == "particle":
		showParticleProbe(traj)
		sys.exit(0)
	if probin == "envelope":
		showEnvelopeProbe(traj)
		sys.exit(0)
