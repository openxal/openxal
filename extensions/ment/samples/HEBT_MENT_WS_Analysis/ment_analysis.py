#------------------------------------------------
# This script will read the input files with profiles for
# HEBT1 WS 01,02,03,04,09 and calculate the phase density 
# at the position of the first WS (WS01). The result will 
# be dumped to the file "out.txt"
#------------------------------------------------

# plane shoudl be x or y
plane = "x"

# set up the PV Logger ID
pvLogId = 13813588

# set eKin in eV
eKin = 910.0*1.0e+6

from javax.swing import *
from java.io import *
from gov.sns.tools.plot import *
from gov.sns.tools.ment import *
import sys, os

imput_files = []
imput_files.append(["HEBT_Diag:WS01","ws01_x.dat"])
imput_files.append(["HEBT_Diag:WS02","ws02_x.dat"])
imput_files.append(["HEBT_Diag:WS03","ws03_x.dat"])
imput_files.append(["HEBT_Diag:WS04","ws04_x.dat"])
#imput_files.append(["HEBT_Diag:WS09","ws9_x.dat"])

wsId0 = "HEBT_Diag:WS01"

m = Ment()
matr = Matrix("HEBT1",pvLogId, eKin, 0.939301400e9, -1.0)


for [wsId,file_name] in imput_files:
	fl = open(file_name,"r")
	lns = fl.readlines()
	fl.close()
	x_arr = []
	sx_arr = []
	for ln in lns:
		res = ln.split()
		if(len(res) == 2):
			x_arr.append(float(res[0])/1000.)
			sx_arr.append(float(res[1]))
	matr.setElemId(wsId0,wsId)
	if(plane == "x"):
		m.addProfile(x_arr, sx_arr, matr.ax, matr.bx, matr.cx, matr.dx)
	else:
		m.addProfile(x_arr, sx_arr, matr.ay, matr.by, matr.cy, matr.dy)
	
print "Start the MENT calculation! Please, wait!"

m.calculate(10)

print "Application finished!"
print "Stop!"
sys.exit()

