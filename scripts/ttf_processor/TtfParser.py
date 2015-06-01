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
            
            daptTp = daptRf.childAdaptor("polyTP")
            daptCoeffTp = daptTp.childAdaptor("coeff")
            coeffTp = daptCoeffTp.stringValue("arr")
            
            daptTpp = daptRf.childAdaptor("polyTPP")
            daptCoeffTpp = daptTpp.childAdaptor("coeff")
            coeffTpp = daptCoeffTpp.stringValue("arr")
            
            print("Name: " + str(strRfIf))
            print("T coeff: " + str(coeffT))
            print("TP coeff: " + str(coeffTp))
            print("TPP coeff: " + str(coeffTpp))
        

    #Process RF gap
    #    parse in polynomial coefficients for T, T', S, S'
    