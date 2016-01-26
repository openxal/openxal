from xal.tools.xml import XmlDataAdaptor

from java.net import URL

from java.net import URI

from java.io import File

import sys

import os

sys.path.insert(0,"/Users/j72/git/openxal/core/src/xal/tools")

from pythonTools import urlFinder

urlToFile = urlFinder.fetchURL("sns_TTF.xdxf") # Url for sns_TTF file
#-----------------------------------------------------------------------------------------------------
file_url = urlFinder.fetchURL("AndreiTTFData.DAT") # Url to Andrei's data

def getFrequency(seq_n):
    if seq_n =="SCL":
        freq = "805000000"
    elif seq_n == "CCL":
        freq = "805000000"
    elif seq_n == "DTL":
        freq = "402500000"
    elif seq_n == "MEBT":
        freq = "402500000"
    else:
        print("FAILURE: Not valid sequence, cannot get frequency.")
    return freq

# Open the file and grab the names (n_arrn), the ttf data (ttf_arr), and the beta pairs (b_arr)
with open(file_url,'r') as f2r:
    i=0
    for line in f2r:
        if i == 0:
            n_arrn = line
        elif i == 1:
            ttf_arr = line
        elif i == 2:
            b_arr = line
        elif i == 3:
            ttfs_arr = line
        else:
            pass
        i+=1

n_arr=[]
n_arrn = n_arrn.split(',') # break the names up by commas
n_arrn[-1]=n_arrn[-1].rstrip() # strip newline character from last name in array

for name in n_arrn:
    n_arr.append(name.replace(":","_")) # replace all of the colons with underscores

ttf_arr = ttf_arr.split(',')  # split the ttfs by commas
ttf_arr[-1] = ttf_arr[-1].rstrip()  # strip newline character from last ttf in array

ttfs_arr = ttfs_arr.split(',')  # split the ttfs by commas
ttfs_arr[-1] = ttfs_arr[-1].rstrip()  # strip newline character from last ttf in array

b_arr = b_arr.split('/')  # split the beta pairs by /
b_arr[-1]=b_arr[-1].rstrip()  # strip the newline character from last beta pair in array
#-----------------------------------------------------------------------------------------------------


for name in n_arr:   # go through all the names in the name array 

    if name.startswith("SCL"):    # if it is an SCL cavity, we extract the cavity number and letter with this method
        cav_name = name[:name.find('R')].replace('_','').replace('Cav','') # changes 'SCL_Cav14b_Rg1' to 'SCL14b'
        if cav_name[3]=='0':                     # if it is 'SCL_Cav04b_Rg1', it changes it to 'SCL4b'
            cav_name = cav_name.replace('0','')
        
        else:
            pass
        n_arr[n_arr.index(name)] = cav_name + "_Rg" + name[name.find('Rg_'):]
print(n_arr)
'''

daptLinac = daptDoc.childAdaptor("SNS_LINAC")
lstDaptSeq = daptLinac.childAdaptors("accSeq")
gap_name_history = []
with open(urlToFile, 'w') as f3r:
    f3r.truncate()
for sequence in lstDaptSeq:   # go through all the sequences in the list of sequences
    lstCavSeq = sequence.childAdaptors("cavity")    # create a list of all the cavities in this sequence
    for cavity in lstCavSeq:     # go through all of the cavities in this sequence
        cav_name = cavity.stringValue("name")  # the name of the current cavity is the string value of the cavities name
        for name in n_arr:      # go through all of the names in the name array to find a name that matches the cavity name
            if name.startswith(cav_name):  # if the beginning of the current name in the file matches the cavity name
                if name in gap_name_history: # if the name is already used
                    pass
                else:
                    
                    gap = cavity.createChild('rfgap')   # create a child rfgap
                    gap_name =name[name.find('Rg'):]
                    gap.setValue('name', gap_name)   # set the name of the gap
                    gap_name_history.append(name)   # append the current name in the name array to the history

daptDoc.writeTo(File(urlToFile))
'''
for name in n_arr:
    if name.startswith('SCL'):
        name.replace('_','')
    else:
        pass

daptDoc = XmlDataAdaptor.adaptorForUrl(urlToFile,False)
daptLinac = daptDoc.childAdaptor("SNS_LINAC")
lstDaptSeq = daptLinac.childAdaptors("accSeq")
for seq in lstDaptSeq:
    lstDaptCavities = seq.childAdaptors("cavity")
    for cavity in lstDaptCavities:
        lstDaptRfGaps = cavity.childAdaptors("rfgap")
        for gap in lstDaptRfGaps:
            gap_id = gap.stringValue("name")
            full_name = cavity.stringValue('name')+gap_id
            for name in n_arr:
                if full_name == name:
                    ind = n_arr.index(name)
                    c_ttf_poly = ttf_arr[ind]
                    c_ttfs_poly = ttfs_arr[ind]
                    

#daptDoc.writeTo(File(urlToFile))