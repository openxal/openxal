from javax.swing import *
from java.io import *
from gov.sns.tools.plot import *
from gov.sns.tools.ment import *
import sys, os

##////////////////////////////////////////process data and initiate ment//////////////////////////////
sd = ScanData()
sd.addDataFromFile(File("2010.04.18.17.53.38.txt"))

for i in range(0,4):
	wsId = sd.getWScanId(i)
	data = sd.getScan_x(i)
	num = data.getNumbOfPoints()
	fl_out = open("ws0"+str(i+1)+"_x.dat","w")
	for j in range(num):
		x = data.getX(j)/1000
		sx = data.getY(j)
		ln = " %9.6f  %12.5g "%(x,sx)
		fl_out.write(ln+"\n")
	fl_out.close()

	data = sd.getScan_y(i)
	num = data.getNumbOfPoints()	
	fl_out = open("ws0"+str(i+1)+"_y.dat","w")
	for j in range(num):
		x = data.getX(j)/1000
		sx = data.getY(j)
		ln = " %9.6f  %12.5g "%(x,sx)
		fl_out.write(ln+"\n")
	fl_out.close()


print "Stop."
sys.exit(1)


