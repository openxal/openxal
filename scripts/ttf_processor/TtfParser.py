from xal.tools.xml import XmlDataAdaptor

from xal.tools import ResourceManager

from java.net import URL

import sys

import os

sys.path.insert(0,"/Users/j72/git/openxal/core/src/xal/tools")

from pythonTools import urlFinder

urlDataFile = urlFinder.fetchURL("sns_pyorbit_linac_structure_untested.xml")

print(urlDataFile)

daptDoc = XmlDataAdaptor.adaptorForUrl(urlDataFile,True)

daptLinac = daptDoc.childAdaptor("SNS_Linac")
lstDaptSeq = daptLinac.childAdaptors("accSeq")
name_array = []
ttf_array = []
ttfs_array = []
beta_array = []
with open("AndreiTTFData.DAT",'w') as Attf:
    Attf.truncate(0)
    for daptSeq in lstDaptSeq:
        strSeqId = daptSeq.stringValue("name")
        dblSeqLng = daptSeq.doubleValue("length")
        
        lstDaptCav = daptSeq.childAdaptors("cavity")
        
        for daptCav in lstDaptCav:
            strCavId = daptCav.stringValue("name")
            dblFreq = daptCav.doubleValue("frequency")
            
            lstDaptRf = daptCav.childAdaptors("rf_gap")
            
            for daptRf in lstDaptRf:
                strRfIf = daptRf.stringValue("name")
                dblBetaMin = daptRf.doubleValue("beta_min")
                dblBetaMax = daptRf.doubleValue("beta_max")
                
                daptT = daptRf.childAdaptor("polyT")
                daptCoeffT = daptT.childAdaptor("coeff")
                coeffT = daptCoeffT.stringValue("arr")
                
                daptS = daptRf.childAdaptor("polyS")
                daptCoeffS = daptS.childAdaptor("coeff")
                coeffS = daptCoeffS.stringValue("arr")
                
                daptTp = daptRf.childAdaptor("polyTP")
                daptCoeffTp = daptTp.childAdaptor("coeff")
                coeffTp = daptCoeffTp.stringValue("arr")
                
                daptTpp = daptRf.childAdaptor("polyTPP")
                daptCoeffTpp = daptTpp.childAdaptor("coeff")
                coeffTpp = daptCoeffTpp.stringValue("arr")
                
                
                name_array.append(str(strRfIf))
                
                ttf_array.append(str(coeffT))
                
                ttfs_array.append(str(coeffS))
                
                beta_array.append(str([dblBetaMin,dblBetaMax]))
                
    '''
    The following code formats the file to contain all the needed data for TTF_Verifier.
    '''
    i=0
    for name in name_array:  #write all of the names on the first line of the file
        if i == len(name_array)-1:
            Attf.write(name)
        else:
            Attf.write(name+',')
        i+=1
    Attf.write('\n')
    i=0
    for ttf_point in ttf_array:  #write all of the ttf points on the second line of the file
        if i == len(ttf_array)-1:
            Attf.write(ttf_point)
        else:
            Attf.write(ttf_point+',')
        i+=1
    Attf.write('\n')
    i=0
    for beta_pair in beta_array:  #write all of the beta pairs on the third line of the file
        if i == len(beta_array)-1:
            Attf.write(beta_pair)
        else:
            Attf.write(beta_pair+'/')
        i+=1
    Attf.write('\n')
    i=0
    for ttfs_point in ttfs_array:
        if i == len(ttfs_array)-1:
            Attf.write(ttfs_point)
        else:
            Attf.write(ttfs_point+',')
        i+=1


    #Process RF gap
    #    parse in polynomial coefficients for T, T', S, S'
    