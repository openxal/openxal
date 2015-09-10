import sys
import math

from java.lang import *
from java.util import *

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()

# global data:

bpms = []
readingHArrays = false
readingVArrays = false
readingAArrays = false
xTurnDat = []
xValDat = [] 
yTurnDat = []
yValDat = []
ampTurnDat = []
ampValDat = []

namelist = ArrayList()
turndatalist = ArrayList()
xdatalist = ArrayList()
ydatalist = ArrayList()
ampdatalist =ArrayList()
name = ""
header = ""

def readFile(fName):

    global readingHArrays
    global readingVArrays
    global readingAArrays
    global name
    global true
    global false
     
    try:
        iFile = open(fName,"r")
    except IOError:
        print "No data file named ", fName
        sys.exit(2)
    iFile.readline()
    iFile.readline()
    iFile.readline()
    iFile.readline()
    iFile.readline()
    iFile.readline()
    iFile.readline()
    for line in iFile.readlines():
	s = String(line[0:-1])   # strip  \n
	tokens = s.split("\\s+")

	nValues = len(tokens)
       
	if(nValues < 1):
		continue # skip blank lines

	firstName = String(tokens[0])
	secondName = String(tokens[1])
    
	if(secondName.startsWith("BPM")):
          
            if(readingHArrays):
                dumpxData(xValDat)
            elif(readingVArrays):
                dumpyData(yValDat)
            elif(readingAArrays):
                dumpaData(ampValDat)

            direction = String(tokens[4])
            if(direction.equals("HORIZONTAL")):
                readingHArrays = true
                readingVArrays = false
                readingAArrays = false
            if(direction.equals("VERTICAL")):
                readingVArrays = true
                readingHArrays = false
                readingAArrays = false
            if(direction.equals("AMPLITUDE")):
                readingVArrays = false
                readingHArrays = false
                readingAArrays = true
            
            xTurnDat = []
            xValDat = []
            yTurnDat = []
            yValDat = []
            ampTurnDat = []
            ampValDat = []
            name = tokens[3]

        if(secondName.startsWith("WAVEFORM")):
            continue
        if (nValues == 3):
            if(readingHArrays):
                xTurnDat.append(tokens[1])
                xValDat.append(tokens[2])
            elif(readingVArrays):
                yTurnDat.append(tokens[1])
                yValDat.append(tokens[2])
            elif(readingAArrays):
                ampTurnDat.append(tokens[1])
                ampValDat.append(tokens[2])
            
    dumpaData(ampValDat)
	

def dumpxData(xValDat):
    global name
    global turns
    print 'Found BPM ', name
    
    if(len(xValDat)>0):
        namelist.add(name)
        xdatalist.add(xValDat)
    turns=len(xValDat)

def dumpyData(yValDat):
    if(len(yValDat)>0):
        ydatalist.add(yValDat)

def dumpaData(ampValDat):
    if(len(ampValDat)>0):
        ampdatalist.add(ampValDat)
      


def dump2Scan1D(ofile):
    
    buffer = StringBuffer()
    global bpms
    float1="%8.4f"
    float2 ="%10.8f"	
    int1 = "%3i"
    space = "  "
    i=0.
    index = 0
    
    for name in namelist:
        buffer = "% data #" + int1%index + "  Legend = Time: 01.01.01 00:00 "
        buffer = buffer + " xPV=ICS_Tim:RTDLGen:StoredTurns "
        buffer = buffer + " yPV=RTBT_Diag:" + name + ":xAvg\n"
        ofile.write(buffer)
        index = index + 1
        buffer = "% data #" + int1%index + "  Legend = Time: 01.01.01 00:00 "
        buffer = buffer + " xPV=ICS_Tim:RTDLGen:StoredTurns "
        buffer = buffer + " yPV=RTBT_Diag:" + name + ":yAvg\n"
        ofile.write(buffer)
        index = index + 1

    buffer = "% x/data #\t"
    for i in range(0,turns-1):
       buffer = buffer + int1%i + "\t"

    buffer = buffer + "\n"
    ofile.write(buffer)

    buffer =""  
    for i in range(0,turns-1):
        j = 0
        buffer = ""
        buffer = buffer + int1%i
        for xdata in xdatalist:
            ydata = ydatalist.get(j)
            buffer = buffer + "  " +  float1%xdata[i] + "  "
            buffer = buffer + float1%ydata[i]
            j=j+1
        ofile.write(buffer)
        ofile.write("\n")

    
# main program
 
if len(sys.argv) < 2 :
    fName = raw_input("enter a file name: ")
else:
    fName = sys.argv[1]


readFile(fName)
ofile = open('RingScan1D.txt', 'wt')

dump2Scan1D(ofile)
ofile.close()

