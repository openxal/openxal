from xal.tools.xml import XmlDataAdaptor

from xal.tools import ResourceManager

from java.net import URL

urlDataFile = ResourceManager.fetchResourceURL("data/Andrei's File Name", self)

daptDoc = XmlDataAdaptor.adaptorForUrl(self, urlDataFile)

daptLinac = daptDoc.childAdaptor("SNS_Linac")
lstDaptSeq = daptLinac.childAdaptors("accSeq")

daptLinac.
for daptSeq in lstDaptSeq:
    strSeqId = daptSeq.stringValue("name")
    dblSeqLng = daptSeq.doubleValue("length")
    
    lstDaptCav = daptSeq.childAdaptors("cavity")

    #Process RF gap
    #    parse in polynomial coefficients for T, T', S, S'
    
    