# This script has harmonics fitting class that will fit the RF phase scan data.
# It also includes several auxiliary classes and functions

import sys
import math
import time
import random

from java.lang import *
from java.util import *
from javax.swing import *

from xal.extension.widgets.plot import BasicGraphData

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

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

def makePhaseNear(phase, phase0):
	""" It will add or substruct any amount of 360. from phase to get close to phase0 """
	n = int(phase0/360.)
	phase = phase%360.
	min_x = 1.0e+38
	n_min = 0
	for i0 in range(5):
		i = i0 - 3
		d = math.fabs(phase + 360.*(i+n) - phase0)
		if(d < min_x):
			n_min = i
			min_x = d
	return (phase + 360.*(n_min+n))
	
def makePhaseNear180(phase, phase0):
	""" It will add or substruct any amount of 180. from phase to get close to phase0 """
	n = int(phase0/180.)
	phase = phase%180.
	min_x = 1.0e+38
	n_min = 0
	for i0 in range(5):
		i = i0 - 3
		d = math.fabs(phase + 180.*(i+n) - phase0)
		if(d < min_x):
			n_min = i
			min_x = d
	return (phase + 180.*(n_min+n))	

def calculateAvgErr(val_arr):
	#calculates average and statistical errors of the value from the array of values
	n_vals = len(val_arr)
	if(n_vals == 0): return (0.,0.)
	if(n_vals == 1): return (val_arr[0],0.)
	avg = 0.
	avg2 = 0.
	for val in val_arr:
		avg += val
		avg2 += val*val
	avg /= n_vals
	avg2 /= n_vals
	err = math.sqrt(math.fabs(avg2 - avg*avg)/(n_vals-1))
	return (avg,err)

class HramonicsFunc:
	""" 
	Calculates sum of harmonics.
	Formula is V(phase) = Amp*cos(h*math.pi*phase/180. + math.pi*phase_shift/180.)
	param_arr = [base,amp1,phase1,amp2,phase2 ...]
	"""
	def __init__(self, param_arr = [0.,1.,0.]):
		self.param_arr = param_arr[:]
		self.coeff = math.pi/180.

	def setParamArr(self,param_arr):
		self.param_arr = param_arr[:]
		
	def getParamArr(self):
		return self.param_arr
		
	def parsePramArr(self,txt):
		res_arr = txt.split()
		param_arr = []
		for val_txt in res_arr:
			param_arr.append(float(val_txt))
		self.setParamArr(param_arr)
		
	def getTxtParamArr(self):
		txt = ""
		for param in self.param_arr:
			txt += " "+str(param)
		return txt

	def scaleAmps(self,coeff):
		for i in range(1,len(self.param_arr),2):
			self.param_arr[i] = coeff*self.param_arr[i]

	def getValue(self,phase):
		base = self.param_arr[0]
		s = base
		for i in range((len(self.param_arr)-1)/2):
			amp = self.param_arr[2*i+1]
			phase_shift = self.param_arr[2*i+2]
			s += amp*math.cos(((i+1)*phase + phase_shift)*self.coeff)
		return s
		
	def getDerivative(self,phase):
		s = 0.
		for i in range((len(self.param_arr)-1)/2):
			amp = self.param_arr[2*i+1]
			phase_shift = self.param_arr[2*i+2]
			s += -amp*(i+1)*self.coeff*math.sin(((i+1)*phase + phase_shift)*self.coeff)
		return s	
		
	def findMin(self):
		if(len(self.param_arr) == 1): return 0.
		phase_min =  makePhaseNear(-self.param_arr[2] + 180.,0.)
		phase_step = 20.
		delta = 0.001
		phase_0 = phase_min - phase_step
		phase_1 = phase_min + phase_step
		v0 = self.getDerivative(phase_0)
		v1 = self.getDerivative(phase_1)
		if(v1*v0 >= 0.):
			print "debug problem with finding the min of the phase-scan. HramonicsFunc."
			phase_step = 0.1
			min_pos = 0.
			min_val = 1.0e+46
			phase = -180.
			while(phase < 180.):
				val = self.getValue(phase)
				if(min_val > val):
					min_val = val
					min_pos = phase
				phase += phase_step
			return min_pos
		phase = 0.
		while(math.fabs(phase_0-phase_1) > delta):
			phase = (phase_0+phase_1)/2.0
			v = self.getDerivative(phase)
			if(v == 0.):
				return phase
			if(v1*v < 0.):
				v0 = v
				phase_0 = phase
			else:
				v1 = v
				phase_1 = phase
		return phase			
		
	def findMax(self):
		if(len(self.param_arr) == 1): return 0.
		phase_max =  makePhaseNear(-self.param_arr[2],0.)
		phase_step = 20.
		delta = 0.001
		phase_0 = phase_max - phase_step
		phase_1 = phase_max + phase_step
		v0 = self.getDerivative(phase_0)
		v1 = self.getDerivative(phase_1)
		if(v1*v0 >= 0.):
			print "debug problem with finding the max of the phase-scan. HramonicsFunc."
			phase_step = 0.1
			max_pos = 0.
			max_val = 1.0e+46
			phase = -180.
			while(phase < 180.):
				val = self.getValue(phase)
				if(max_val < val):
					max_val = val
					max_pos = phase
				phase += phase_step
			return max_pos
		phase = 0.
		while(math.fabs(phase_0-phase_1) > delta):
			phase = (phase_0+phase_1)/2.0
			v = self.getDerivative(phase)
			if(v == 0.):
				return phase
			if(v1*v < 0.):
				v0 = v
				phase_0 = phase
			else:
				v1 = v
				phase_1 = phase
		return phase			

class HarmScorer(Scorer):
	""" 
	Calculate the difference between model and measured points.
	variables is Java's ArrayList()
	param_arr is a Vector wit (amp,phase_shift) pairs
	with ParamProxy istances.
	The first vector element is the ParamProxy for the base value.
	Formula is V(phase) = Amp*cos(h*math.pi*phase/180. + math.pi*phase_shift/180.)
	"""
	def __init__(self, gd, variables):
		self.gd = gd
		self.variables = variables
		param_arr = []
		for i in range(self.variables.size()):
			param_arr.append(0.)
		self.harm_func = HramonicsFunc(param_arr)
		self.param_arr = self.harm_func.getParamArr()
		
	def score(self,trial,variables_in):	
		self.setTrialParams(trial)
		return self.getDiff2()
		
	def getDiff2(self):
		#-----calculate diff==========================
		err2 = 0.
		for i in range(self.gd.getNumbOfPoints()):
			err = self.gd.getY(i) - self.harm_func.getValue(self.gd.getX(i))
			err2 += err*err
		return err2/(self.gd.getNumbOfPoints())
		
	def setTrialParams(self,trial):
		#------set up harmonics function parameters from Trial map
		var_map = trial.getTrialPoint().getValueMap()
		for i in range(self.variables.size()):
			var = self.variables.get(i)
			if(var_map.containsKey(var)): 
					self.param_arr[i] =  trial.getTrialPoint().getValue(var)	
		for i in range(1,len(self.param_arr),2):
			self.param_arr[i] = math.fabs(self.param_arr[i])
			self.param_arr[i+1] = makePhaseNear(self.param_arr[i+1],0.)
		
	def copyToExternalParams(self,param_arr):
		for i in range(len(self.param_arr)):
			param_arr[i] = self.param_arr[i]
			
	def copyFromExternalParams(self,param_arr):
		for i in range(len(self.param_arr)):
			self.param_arr[i] = param_arr[i]
			
	def getNextHarm_Amp_Shift(self):
		delta_max = 0.
		phase_shift = 0.
		for i in range(self.gd.getNumbOfPoints()):
			err = (self.gd.getY(i) - self.harm_func.getValue(self.gd.getX(i)))
			#print "debug diff x=",self.gd.getX(i)," err=",err
			if(delta_max < err):
				phase_shift = - self.gd.getX(i)
				delta_max = err
		delta_max = math.fabs(delta_max)
		n_harm = (len(self.param_arr)-1)/2 + 1
		phase_shift = makePhaseNear(n_harm*phase_shift,0.)
		return (delta_max,phase_shift)
		
	def getHarmFunc(self):
		return self.harm_func
		
	def getParamsArrCopy(self):
		return self.harm_func.getParamArr()[:]

class HarmonicsAnalyzer:
	"""
	This class will analize the rf phase scan data.
	Harmonics in a form: V(phase) = A*cos(h*math.pi*phase/180. + math.pi*phase_shift/180.) 
	"""
	def __init__(self, n_harm = 1):
		self.n_iter = 500
		self.n_harm = n_harm
		self.harm_func = None
		self.param_arr = []
		for i in range(2*self.n_harm + 1):
			self.param_arr.append(0.)

	def analyzeData(self,gd):
		""" 
		Analysis of the BasicGraphData() instance with data.
		Returns the error value.
		"""
		if(gd.getNumbOfPoints() == 0): return 0.
		y_max = gd.getMaxY()
		y_min = gd.getMinY()
		amp0 = (y_max - y_min)/2.0
		amp_step = 0.05*amp0 
		phase_step = 3.0
		self.param_arr[0] = (y_max + y_min)/2.0
		for i_h in range(self.n_harm+1):
			param_local_arr = self.param_arr[0:2*i_h+1]
			#print "debug i_h=",i_h," =====start ==== param_arr=",param_local_arr 
			variables = ArrayList()	
			delta_hint = InitialDelta()
			var = Variable("base",param_local_arr[0], - Double.MAX_VALUE, Double.MAX_VALUE)
			variables.add(var)
			delta_hint.addInitialDelta(var,amp_step)
			for i_fit in range(i_h):
				var = Variable("amp"+str(i_fit),param_local_arr[2*i_fit+1], - Double.MAX_VALUE, Double.MAX_VALUE)
				variables.add(var)
				delta_hint.addInitialDelta(var,amp_step)
				var = Variable("phase"+str(i_fit),param_local_arr[2*i_fit+2], - Double.MAX_VALUE, Double.MAX_VALUE)
				variables.add(var)
				delta_hint.addInitialDelta(var,phase_step)
			scorer = HarmScorer(gd,variables)
			self.harm_func = scorer.getHarmFunc()
			if(i_h == 0):
				scorer.copyFromExternalParams(self.param_arr)
				(amp,shift) = scorer.getNextHarm_Amp_Shift()
				#print "debug zero harm (amp,shift)=",(amp,shift)
				self.param_arr[2*i_h+1] = amp
				self.param_arr[2*i_h+2] = shift
				#print "debug i_h=",i_h," =====final ==== param_arr=", self.param_arr		
				continue
			maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper((i_h+1)*self.n_iter) 
			solver = Solver(SimplexSearchAlgorithm(),maxSolutionStopper)
			problem = ProblemFactory.getInverseSquareMinimizerProblem(variables,scorer,0.0001)
			problem.addHint(delta_hint)
			solver.solve(problem)
			#------- get results
			trial = solver.getScoreBoard().getBestSolution()
			err2 = scorer.score(trial,variables)				
			scorer.copyToExternalParams(self.param_arr)
			#print "debug i_h=",i_h," =====final err2=",math.sqrt(err2),"==== param_arr=", self.param_arr				
			if(i_h != self.n_harm):
				(amp,shift) = scorer.getNextHarm_Amp_Shift()
				#print "debug last step (amp,shift)=",(amp,shift)
				self.param_arr[2*i_h+1] = amp
				self.param_arr[2*i_h+2] = shift	
				#print "debug i_h=",i_h," ===== last step==== param_arr=", self.param_arr	
		return math.sqrt(err2)
	
	def getHrmonicsFunction(self):
		return self.harm_func
	
	def getParamsArr(self):
		if(self.harm_func != null):
			return self.harm_func.getParamArr()
		else:
			return [0.]
	
	def getPositionOfMin(self):
		return self.harm_func.findMin()
		
	def getPositionOfMax(self):
		return self.harm_func.findMax()	
	
def TestHarmFunc(param_arr,x):
	""" param_arr = [base,(amp,shift)...] """
	coeff = math.pi/180.
	y = param_arr[0]
	for i0 in range((len(param_arr)-1)/2):
		y += param_arr[2*i0+1]*math.cos(((i0+1)*x+param_arr[2*i0+2])*coeff)
	return y
	
#---------------------------------------------------
#          TEST SCRIPT
#---------------------------------------------------
if __name__=="__main__":
	gd = BasicGraphData()
	n_points = 36
	step = 360./(n_points + 1)
	param_arr = [12.0,2.0,-25.0,0.2,+30.]
	#param_arr = [12.0,2.0,-25.0]
	print "initial params=",param_arr
	x = -180.
	for i in range(n_points):
		x += step
		y = TestHarmFunc(param_arr,x)+0.001*2*(random.random()-0.5)
		gd.addPoint(x,y)
		print "init x=",x," y=",y
	
	harmonicsAnalyzer = HarmonicsAnalyzer(2)
	err = harmonicsAnalyzer.analyzeData(gd)
	
	print "=================res================"
	print "res_arr=",harmonicsAnalyzer.getParamsArr()
	print "min pos =",harmonicsAnalyzer.getPositionOfMin()
	print "avg. error = ",err
	
	#if you want to measure the speed, please comment out sys.exit(1)
	sys.exit(1)

	count = 0
	while(1 < 2):
		time_start = time.time()
		count += 1
		harmonicsAnalyzer.analyzeData(gd)
		harmonicsAnalyzer.getPositionOfMin()
		time_elaps =time.time() -  time_start
		print "count=",count," speed[count/sec]=",1./time_elaps
		
	
