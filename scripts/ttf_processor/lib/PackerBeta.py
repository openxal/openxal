from xal.tools.xml import XmlDataAdaptor

from java.net import URL

from java.net import URI

from java.io import File

import sys

import os

from time import sleep

sys.path.insert(0,"/Users/j72/git/openxal/core/src/xal/tools")

from pythonTools import urlFinder

def wanted(seq_name):
    if (seq_name.startswith("MEBT")) or (seq_name.startswith("DTL")) or (seq_name.startswith("CCL")) or (seq_name.startswith("SCL")):
        return True
    else:
        return False

urlToFile = urlFinder.fetchURL("snsTTFpolys.xdxf") # Url for sns_TTF file

file_url = urlFinder.fetchURL("sns.xdxf") # Url to main xdxf file

andreiDataFile = urlFinder.fetchURL("AndreiTTFData.DAT")
# Open the file and grab the names (n_arrn), the ttf data (ttf_arr), and the beta pairs (b_arr)
with open(andreiDataFile,'r') as f2r:
    i=0
    for line in f2r:
        if i == 0:       # line one: names
            n_arr = line
        elif i == 1:     # line two: ttf polys
            ttf_arr = line
        elif i == 2:     # line three: beta pairs SKIP
            print('')
        elif i == 3:     # line four: stf polys
            stf_arr = line
        elif i == 4:
            ttfp_arr = line
        elif i == 5:
            stfp_arr = line
        else:
            pass
        i+=1

n_arr = n_arr.split(',') # break the names up by commas
n_arr[-1]=n_arr[-1].rstrip() # strip newline character from last name in array

ttf_arr = ttf_arr.split(',')  # split the ttf by commas
ttf_arr[-1] = ttf_arr[-1].rstrip()  # strip newline character from last ttf in array
for l in ttf_arr:
    ttf_arr[ttf_arr.index(l)] = l.strip()
for l in ttf_arr:
    ttf_arr[ttf_arr.index(l)] = ' '.join(l.split()).replace(' ',', ')

stf_arr = stf_arr.split(',')  # split the stf by commas
stf_arr[-1] = stf_arr[-1].rstrip()  # strip newline character from last stf in array
for l2 in stf_arr:
    stf_arr[stf_arr.index(l2)] = l2.strip()
for l2 in stf_arr:
    stf_arr[stf_arr.index(l2)] = ' '.join(l2.split()).replace(' ',', ')

ttfp_arr = ttfp_arr.split(',')  # split the ttfp by commas
ttfp_arr[-1] = ttfp_arr[-1].rstrip()  # strip newline character from last ttfp in array
for l3 in ttfp_arr:
    ttfp_arr[ttfp_arr.index(l3)] = l3.strip()
for l3 in ttfp_arr:
    ttfp_arr[ttfp_arr.index(l3)] = ' '.join(l3.split()).replace(' ',', ')

stfp_arr = stfp_arr.split(',')  # split the stfp by commas
stfp_arr[-1] = stfp_arr[-1].rstrip()  # strip newline character from last stfp in array
for l4 in stfp_arr:
    stfp_arr[stfp_arr.index(l4)] = l4.strip()
for l4 in stfp_arr:
    stfp_arr[stfp_arr.index(l4)] = ' '.join(l4.split()).replace(' ',', ')

for name in n_arr:
    if name.startswith('MEBT'):
        new_name = name.replace(":","_")
        cav_id = new_name[4]
        gap_id = new_name[-1]
        new_name=new_name.replace(cav_id+"_",'_')
        new_name = new_name.replace("Rg"+gap_id,"RF:Bnch"+'0'+cav_id+":Rg0"+gap_id)
        n_arr[n_arr.index(name)] = new_name
    elif name.startswith('DTL'):
        new_name = name.replace(":","_")
        cav_id = new_name[3]
        try:
            int(new_name[-2])
            gap_id = new_name[-2]+new_name[-1]
        except Exception:
            gap_id = new_name[-1]
        new_name = new_name.replace(cav_id+"_","_")
        if len(gap_id)==2:
            new_name = new_name.replace('Rg'+gap_id,'RF:Cav0'+cav_id+':Rg'+gap_id)
        else:
            new_name = new_name.replace('Rg'+gap_id,'RF:Cav0'+cav_id+':Rg0'+gap_id)
        n_arr[n_arr.index(name)] = new_name
    elif name.startswith('CCL'):
        new_name = name.replace(":","_")
        cav_id = new_name[3]
        try:
            int(new_name[-2])
            gap_id = new_name[-2]+new_name[-1]
        except Exception:
            gap_id = new_name[-1]
        new_name = new_name.replace(cav_id+"_","_")
        if len(gap_id)==2:
            new_name = new_name.replace('Rg'+gap_id,'RF:Cav0'+cav_id+':Rg'+gap_id)
        else:
            new_name = new_name.replace('Rg'+gap_id,'RF:Cav0'+cav_id+':Rg0'+gap_id)
        n_arr[n_arr.index(name)] = new_name
    elif name.startswith('SCL'):
        new_name = name.replace('SCL','SCL_RF')
        gap_id = new_name[-1]
        new_name = new_name.replace('Rg'+gap_id,'Rg0'+gap_id)
        n_arr[n_arr.index(name)] = new_name
    else:
        pass
  
daptDoc = XmlDataAdaptor.adaptorForUrl(file_url,False)

daptWrite = XmlDataAdaptor.newEmptyDocumentAdaptor()

daptLinac = daptDoc.childAdaptor("xdxf")

lstDaptSeq = daptLinac.childAdaptors("sequence")

primseq=[]
subseq=[]
nodes=[]
box = dict()
box2 = dict()

# START1-------------------------------------
for seq in lstDaptSeq: # go through all of the sequences in the list of primary sequences
    seqid = seq.stringValue('id')
    if wanted(seqid): # check if we are interested in this sequence
        primseq.append(seqid)
        if seq.childAdaptors("sequence"): # if the current sequence has child adaptors
            lstSubSeq = seq.childAdaptors("sequence") # create a list of the subsequences
            for subSeq in lstSubSeq:      # go through all of the subsequences
                box2[subSeq.stringValue('id')]=seqid # add the subsequences to the dictionary and the sequence id
                lstNodes = subSeq.childAdaptors('node')
                subseqid = subSeq.stringValue('id')
                
                for node in lstNodes:
                    nodeid = node.stringValue('id')
                    nodetype = node.stringValue('type')
                    if nodetype == 'RG':
                        box[nodeid]=subseqid
        else:
            '''# commented out material uses the fact that sns.xdxf does not have child subsequences
            lstNodes = seq.childAdaptors('node')
            
            for node in lstNodes:

                nodeid = node.stringValue('id')
                nodetype = node.stringValue('type')
                if nodetype == "RG":

                    box[nodeid]=seqid
            '''
            lstNodes = seq.childAdaptors('node')
            
            for node in lstNodes:

                nodeid = node.stringValue('id')
                subseqid = nodeid[:-5]
                box2[subseqid]=seqid
                nodetype = node.stringValue('type')
                if nodetype == "RG":

                    box[nodeid]=subseqid
    else:
        pass

# END1-------------------------------------------

primary = daptWrite.createChild('xdxf')
primary.setValue('date',"06.12.2015")
primary.setValue('system',"sns")
primary.setValue('ver',"2.0.0")

# START2-----------------------------------------
for seq in primseq: # go through all the names of the primary sequences: MEBT, DTL, CCL, SCLMED, SCLHIGH, etc.
    seq_node = primary.createChild('sequence')   # create a child of the xdxf node
    seq_node.setValue('id',seq)                  # set the id of this child to the name of the primary sequence
seqChildren = primary.childAdaptors('sequence')  # a list of the primary sequences (daughters of xdxf)

# END2-------------------------------------------


# START3-----------------------------------------
for thing in seqChildren:          # go through all of the primary sequences
    thingsname = thing.stringValue('id')    # this is the current primary sequences name
    if thingsname.startswith('MEBT'):       # check if the current primary sequence is of type MEBT
        for a,b in box2.iteritems():        # go through the dictionary containing all of the subsequences (branches)
            if b == "MEBT":                 # if it is a MEBT subsequence, make it a child of the primary sequence
                c_subseq = thing.createChild('sequence') # create the child subsequence
                c_subseq.setValue('id',a)   # set the name of the subsequence
            else:
                pass
    elif thingsname.startswith('SCLMed'):    # check if the current primary sequence is of type SCL
        for c,d in box2.iteritems():      # go through the dictionary containing all of the subsequences (branches)
            if d == "SCLMed":             # if it is a SCLMed subsequence, make it a child of the primary sequence
                c_subseq = thing.createChild('sequence')
                c_subseq.setValue('id',c)
            else:
                pass
    elif thingsname.startswith('SCLHigh'):
        for c,d in box2.iteritems():
            if d == "SCLHigh":
                c_subseq = thing.createChild('sequence')
                c_subseq.setValue('id',c)
            else:
                pass
    elif thingsname.startswith('DTL1'):
        for c,d in box2.iteritems():
            if d == "DTL1":
                c_subseq = thing.createChild('sequence')
                c_subseq.setValue('id',c)
            else:
                pass
    elif thingsname.startswith('DTL2'):
        for c,d in box2.iteritems():
            if d == "DTL2":
                c_subseq = thing.createChild('sequence')
                c_subseq.setValue('id',c)
            else:
                pass
    elif thingsname.startswith('DTL3'):
        for c,d in box2.iteritems():
            if d == "DTL3":
                c_subseq = thing.createChild('sequence')
                c_subseq.setValue('id',c)
            else:
                pass
    elif thingsname.startswith('DTL4'):
        for c,d in box2.iteritems():
            if d == "DTL4":
                c_subseq = thing.createChild('sequence')
                c_subseq.setValue('id',c)
            else:
                pass
    elif thingsname.startswith('DTL5'):
        for c,d in box2.iteritems():
            if d == "DTL5":
                c_subseq = thing.createChild('sequence')
                c_subseq.setValue('id',c)
            else:
                pass
    elif thingsname.startswith('DTL6'):
        for c,d in box2.iteritems():
            if d == "DTL6":
                c_subseq = thing.createChild('sequence')
                c_subseq.setValue('id',c)
            else:
                pass
    elif thingsname.startswith('CCL1'):
        for c,d in box2.iteritems():
            if d == "CCL1":
                c_subseq = thing.createChild('sequence')
                c_subseq.setValue('id',c)
            else:
                pass
    elif thingsname.startswith('CCL2'):
        for c,d in box2.iteritems():
            if d == "CCL2":
                c_subseq = thing.createChild('sequence')
                c_subseq.setValue('id',c)
            else:
                pass
    elif thingsname.startswith('CCL3'):
        for c,d in box2.iteritems():
            if d == "CCL3":
                c_subseq = thing.createChild('sequence')
                c_subseq.setValue('id',c)
            else:
                pass
    elif thingsname.startswith('CCL4'):
        for c,d in box2.iteritems():
            if d == "CCL4":
                c_subseq = thing.createChild('sequence')
                c_subseq.setValue('id',c)
            else:
                pass
    else:
        pass

# END3-------------------------------------------

# START4-----------------------------------------
for n,p in box.iteritems():
    
    for seq in primary.childAdaptors("sequence"):
        if seq.childAdaptors("sequence"):
            subSeqlst = seq.childAdaptors("sequence")
            for subSeq in subSeqlst:
                subseqid2 = subSeq.stringValue('id')
                if subseqid2 == p:
                    node_node = subSeq.createChild("node")
                    node_node.setValue('id',n)
                    att = node_node.createChild('attributes')
                    gap = att.createChild('rfgap')
                    for name_1 in n_arr:
                        #print('n: '+n+"name_1: "+name_1)
                        if name_1==n:
                            print("HAPPINESS, name_1: "+name_1+' n: '+n)
                            ttfpoly = ttf_arr[n_arr.index(name_1)]
                            stfpoly = stf_arr[n_arr.index(name_1)]
                            ttfp_poly = ttfp_arr[n_arr.index(name_1)]
                            stfp_poly = stfp_arr[n_arr.index(name_1)]
                            print(ttfp_poly)
                            gap.setValue('ttfCoeffs',ttfpoly)
                            gap.setValue('stfCoeffs',stfpoly)
                            gap.setValue('ttfpCoeffs',ttfp_poly)
                            gap.setValue('stfpCoeffs',stfp_poly)
                            break
        else:
            current_id = seq.stringValue('id')
            if current_id == p:
                node_node = seq.createChild("node")
                node_node.setValue('id',n)
                att = node_node.createChild('attributes')
                gap = att.createChild('rfgap')
                for name_1 in n_arr:
                    
                    if name_1==n:
                        print'here'
                        ttfpoly = ttf_arr[n_arr.index(name_1)]
                        stfpoly = stf_arr[n_arr.index(name_1)]
                        ttfp_poly = ttfp_arr[n_arr.index(name_1)]
                        stfp_poly = stfp_arr[n_arr.index(name_1)]
                        gap.setValue('ttfCoeffs',ttfpoly)
                        gap.setValue('stfCoeffs',stfpoly)
                        gap.setValue('ttfpCoeffs',ttfp_poly)
                        gap.setValue('stfpCoeffs',stfp_poly)
                        break
daptWrite.writeTo(File(urlToFile))
'''
for f,g in box.iteritems():
    print(f,g)
'''
# END4-------------------------------------------
