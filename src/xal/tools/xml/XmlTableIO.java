/*
 * XmlTableIO.java
 *
 * Created on February 18, 2003, 12:54 PM
 */

package xal.tools.xml;

import xal.tools.URLUtil;
import xal.tools.data.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

/**
 * Static methods for reading and writing tables from/to XML files.
 *
 * @author  tap
 */
public class XmlTableIO {    
    /** Creates a new instance of XmlTableReader */
    protected XmlTableIO() {}
    
    
    /** Read the table group from the specified file into editContext without XML validation. */
    static public void readTableGroupFromFile( final EditContext editContext, final String tableGroup, final File file ) throws URLUtil.FilePathException {
        readTableGroupFromUrl( editContext, tableGroup, URLUtil.urlSpecForFile( file ) );
    }
    
    
    /** Read the table group from the URL file into editContext without XML validation. */
    static public void readTableGroupFromUrl( final EditContext editContext, final String tableGroup, final String urlSpec ) {
        readTableGroupFromUrl( editContext, tableGroup, urlSpec, false );
    }
    
    
    /** Read the table group from the URL file into editContext with the specified XML validation flag. */
    static public void readTableGroupFromUrl( final EditContext editContext, final String tableGroup, final String urlSpec, final boolean isValidating ) {
        final DataAdaptor docAdaptor = XmlDataAdaptor.adaptorForUrl( urlSpec, isValidating );
		editContext.importTablesFromDataAdaptor( docAdaptor, tableGroup );
    }


    /** Write all tables associated with the specified group in editContext to an XML file. */
    static public void writeTableGroupToFile( final EditContext editContext, final String group, final File file ) throws URLUtil.FilePathException {
        writeTableGroupToUrl(editContext, group, URLUtil.urlSpecForFile(file));
    }
    
    
    /** Write all tables associated with the specified group in editContext to an XML file. */
    static public void writeTableGroupToUrl( final EditContext editContext, final String group, final String urlSpec ) {
        final XmlDataAdaptor docAdaptor = XmlDataAdaptor.newEmptyDocumentAdaptor();
		editContext.writeGroupToDataAdaptor( docAdaptor, group );
        docAdaptor.writeToUrlSpec( urlSpec );
    }
}



/* ---------------------------------------
 * Sample table file to parse or write:

<tablegroup>
<table name="twiss">
    <schema>
        <attribute name="alpha" type="java.lang.Double"/>
        <attribute name="beta" type="java.lang.Double"/>
        <attribute name="nodeId" type="java.lang.String" isPrimaryKey="true"/>
    </schema>
    <record alpha="1.1" beta="5.1" nodeId="MEBT_Diag:BPM04"/>
    <record alpha="2.7" beta="13.2" nodeId="MEBT_Diag:BPM06"/>
    <record alpha="-5.1" beta="20.8" nodeId="MEBT_Diag:BPM08"/>
    <record alpha="1.1" beta="10.2" nodeId="MEBT_Diag:BPM10"/>
</table>
<table name="bpmdata">
    <schema>
        <attribute name="xAvg" type="java.lang.Double"/>
        <attribute name="yAvg" type="java.lang.Double"/>
        <attribute name="nodeId" type="java.lang.String" isPrimaryKey="true"/>
    </schema>
    <record xAvg="1.1" yAvg="5.1" nodeId="MEBT_Diag:BPM01"/>
    <record xAvg="2.7" yAvg="13.2" nodeId="MEBT_Diag:BPM06"/>
    <record xAvg="-5.1" yAvg="20.8" nodeId="MEBT_Diag:BPM08"/>
</table>
</tablegroup>
*/

