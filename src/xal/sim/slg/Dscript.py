#!/usr/bin/env jython
import sys
from java.lang import *
from java.util import *
from java.io import *
from org.xml.sax import *
from gov.sns.xal.smf import *
from gov.sns.xal.smf.impl import *
from gov.sns.xal.smf.impl.qualify import *
from gov.sns.xal.smf.xdxf import *
from gov.sns.xal.smf.parser import *
from gov.sns.xal.smf.data import *
from gov.sns.xal.model import *
from gov.sns.xal.model.xml import *
from gov.sns.tools.beam import *
###################################################################################
false= (1<0)
true=(1>0)

latin="MEBT"
#latin="lanl_MEBT"

#probin="particle"
probin="envelope"

# lattice input locations
lat_file={
	"MEBT":"xml/MEBT_lanl_lattice.xml",
	"lanl_MEBT":"/home/klotz/workspace/xaldev/work/xml/ModelValidation.lat.mod.xal.xml"}

# probe input locations
prb_file={
	"??":"workspace/xaldev/xal_xmls/sns_probes.xml",
	"particle":"/home/klotz/workspace/xaldev/work/xml/ModelValidation.particle.probe.mod.xal.xml",
	"envelope":"/home/klotz/workspace/xaldev/work/xml/ModelValidation.envelope.probe.mod.xal.xml"}

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

	#load the lattice
	try:
		cin=lat_file[latin]
		print "Using '",cin,"' as lattice input"
		print "======================================================"
		lattice = LatticeXmlParser.parse(cin,false)

		#dump current state and content to output
		buffer =    "LATTICE - "+lattice.getType()
		buffer += "\nID        :"+lattice.getId()
		buffer += "\nAuthor    :"+lattice.getAuthor()
		buffer += "\nDate      :"+lattice.getDate()
		buffer += "\nVersion   :"+lattice.getVersion()
		buffer += "\nComments  :"+lattice.getComments()
		buffer += "\nChildren  :"+repr(lattice.getChildCount())
		buffer += "\nLeaves    :"+repr(lattice.getLeafCount())
		buffer += "\nLength    :"+repr(lattice.getLength())
		buffer += "\n================================================"
		print buffer

		cout= PrintWriter(System.out, Boolean("true"))
		#lattice.print(cout)

	except ParsingException:
		print ParsingException.getMessage()	
		sys.exit(-1)
	except Exception:
		print Exception.getMessage()
		sys.exit(-1)
		
	#laod the probe
	try:
		cin=prb_file[probin]
		print "Using '",cin,"' as probe input"
		print "======================================================"
		envProbe=ProbeXmlParser.parse(cin)

		#dump some initial probe parameters
		buffer  = "PROBE - " +repr(envProbe.getComment())
		buffer += "\nBeta               :"+repr(envProbe.getBeta())
		buffer += "\nGamma              :"+repr(envProbe.getGamma())
		buffer += "\nKinetic Energy     :"+repr(envProbe.getKineticEnergy())
		buffer += "\nPosition           :"+repr(envProbe.getPosition())
		buffer += "\nSpecies Charge     :"+repr(envProbe.getSpeciesCharge())
		buffer += "\nSpecies Rest Energy:"+repr(envProbe.getSpeciesRestEnergy())
		buffer += "\n================================================"
		print buffer
	except ParsingException:
		print ParsingException.getMessage()
		sys.exit(-1)
	except Exception:
		print Exception.getMessage()
		sys.exit(-1)

	#propagate the probe
	try:
		lattice.propagate(envProbe)
		envTraj= envProbe.getTrajectory()
		envTraj.setDescription("validation trajectory")
	except ModelException:
		print ModelException.getMessage()
		sys.exit(-1)
	except Exception:
		print Exception.getMessage()
		sys.exit(-1)

	#show results
	if probin == "particle":
		showParticleProbe(envTraj)
	if probin == "envelope":
		showEnvelopeProbe(envTraj)
