# This scripts library is a place for functions and classes related to the
# magnet scaling

import sys
import math
import time
import random

from xjava.lang import *
from xjava.util import *
from xjava.swing import *

class EnergyScalingLinacRingSNS:
	"""
	This class provides functionality for energy calculation between two
	parts of the SNS accelerator Linac and Ring. In the linac we have H- 
	and in the ring we have protons. We assume the velocities of these
	particles are the same, because there is no acceleration in the Ring
	of beam transport lines.
	We use the following formulas
	Tkin = mass*(gamma -1)
	momentum = mass*gamma*beta
	(Tkin + mass)^2 = mass^2 + momentum^2
	formula for Tkin to avoid problems with small Tkin
	Tkin = momentum^2/(sqrt(mass^2 + momentum^2) + mass)
	"""
	def __init__(self):
		self.mass_hm = 9.39294E8
		self.mass_p = 9.38272E8
		
	def eKin_Hm_to_P(self,eKin_in):
		"""
		We assume energy in eV like everywhere in XAL Online Model
		"""
		eKin_out = eKin_in*(self.mass_p/self.mass_hm)
		return eKin_out
		
	def eKin_P_to_Hm(self,eKin_in):
		"""
		We assume energy in eV like everywhere in XAL Online Model
		"""
		eKin_out = eKin_in*(self.mass_hm/self.mass_p)
		return eKin_out
		
	def momentum_Hm_to_P(self,momentum_in):
		"""
		We assume energy in eV and momentum in eV/c like everywhere in XAL Online Model
		"""		
		return momentum_in*(self.mass_p/self.mass_hm)
		
	def momentum_P_to_Hm(self,momentum_in):
		"""
		We assume energy in eV and momentum in eV/c like everywhere in XAL Online Model
		"""
		return momentum_in*(self.mass_hm/self.mass_p)
		
	def momentum_from_eKin_Hm(self,eKin):
		"""
		We assume energy in eV like everywhere in XAL Online Model
		"""
		momentum = math.sqrt(eKin*(eKin + 2*self.mass_hm))
		return momentum
		
	def momentum_from_eKin_P(self,eKin):
		"""
		We assume energy in eV like everywhere in XAL Online Model
		"""
		momentum = math.sqrt(eKin*(eKin + 2*self.mass_p))
		return momentum	
		
	def eKin_from_momentum_Hm(self,momentum):
		"""
		We assume energy in eV and momentum in eV/c like everywhere in XAL Online Model
		"""
		eKin = momentum**2/(math.sqrt(momentum**2 + self.mass_hm**2) + self.mass_hm)
		return 	eKin
		
	def eKin_from_momentum_P(self,momentum):
		"""
		We assume energy in eV momentum in eV/c like everywhere in XAL Online Model
		"""
		eKin = momentum**2/(math.sqrt(momentum**2 + self.mass_p**2) + self.mass_p)
		return eKin
		
if __name__ == "__main__":
	"""
	Tests for 1 GeV Linac to Ring and back
	"""
	energyScaler = EnergyScalingLinacRingSNS()
	eKin_linac = 1000.0 # MeV
	eKin_ring = energyScaler.eKin_Hm_to_P(eKin_linac*1.0e+6)/1.0e+6
	print " linac eKin = %10.6f "%eKin_linac,"  ring eKin = = %10.6f "%eKin_ring
	eKin_linac = energyScaler.eKin_P_to_Hm(eKin_ring*1.0e+6)/1.0e+6
	print " linac eKin = %10.6f "%eKin_linac,"  ring eKin = = %10.6f "%eKin_ring
	momentum_linac = energyScaler.momentum_from_eKin_Hm(eKin_linac*1.0e+6)/1.0e+6
	momentum_ring = energyScaler.momentum_from_eKin_P(eKin_ring*1.0e+6)/1.0e+6
	print " linac momentum = %10.6f "%momentum_linac,"  ring momentum = = %10.6f "%momentum_ring
	momentum_ring = energyScaler.momentum_Hm_to_P(momentum_linac*1.0e+6)/1.0e+6
	print " linac momentum = %10.6f "%momentum_linac,"  ring momentum = = %10.6f "%momentum_ring
	momentum_linac = energyScaler.momentum_P_to_Hm(momentum_ring*1.0e+6)/1.0e+6
	print " linac momentum = %10.6f "%momentum_linac,"  ring momentum = = %10.6f "%momentum_ring
	eKin_linac = energyScaler.eKin_from_momentum_Hm(momentum_linac*1.0e+6)/1.0e+6
	eKin_ring = energyScaler.eKin_from_momentum_P(momentum_ring*1.0e+6)/1.0e+6
	print " linac eKin = %10.6f "%eKin_linac,"  ring eKin = = %10.6f "%eKin_ring
	