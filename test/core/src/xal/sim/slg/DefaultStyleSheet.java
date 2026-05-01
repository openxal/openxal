package xal.sim.slg;

import java.io.*;
/**
 * @author wdklotz
 * 
 * created May 21, 2003
 * 
 * Deault XSL stylesheet (more or less identical to the file '2LANL.xsl') to transform
 * our native lattice to a lattice compatible with the on-line model. 
 * 
 */
public class DefaultStyleSheet {
	private static String XSLSHEET;
	static {
XSLSHEET   ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
XSLSHEET +="<!--  -->";
XSLSHEET +="<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">";
XSLSHEET +="            <!--xmlns=\"http://www.w3.org/TR/xslt\"-->";
XSLSHEET +="";
XSLSHEET +="<xsl:output method=\"xml\" doctype-system=\"Lattice.mod.xal.dtd\" indent=\"yes\"/>";
XSLSHEET +="";
XSLSHEET +="<!-- suppress text nodes unless requested -->";
XSLSHEET +="<xsl:template match=\"text()\"/>";
XSLSHEET +="";
XSLSHEET +="<!-- default: copy the element with all its attributes -->";
XSLSHEET +="<xsl:template match=\"*|/\">";
XSLSHEET +="    <xsl:copy>";
XSLSHEET +="        <xsl:copy-of select=\"@*\"/>";
XSLSHEET +="        <xsl:apply-templates/>";
XSLSHEET +="    </xsl:copy>";
XSLSHEET +="</xsl:template>";
XSLSHEET +="";
XSLSHEET +="<!-- all mappings to Marker -->";
XSLSHEET +="<xsl:template match=\"Element[@type='marker'] |";
XSLSHEET +="                     Element[@type='pmarker'] |";
XSLSHEET +="                     Element[@type='beampositionmonitor'] | ";
XSLSHEET +="                     Element[@type='beamcurrentmonitor'] |";
XSLSHEET +="                     Element[@type='beamlossmonitor'] |";
XSLSHEET +="                     Element[@type='wirescanner']\">";
XSLSHEET +="	 <xsl:element name=\"Element\">";
XSLSHEET +="	     <xsl:attribute name=\"type\">Marker</xsl:attribute>";
XSLSHEET +="	     <xsl:attribute name=\"id\"><xsl:value-of select=\"@id\"/></xsl:attribute>";
XSLSHEET +="	 </xsl:element>";
XSLSHEET +="</xsl:template>";
XSLSHEET +="";
XSLSHEET +="<xsl:template match=\"Element[@type='drift']\">";
XSLSHEET +="    <!-- use drift element counting to generate drift ids DRx (x=1,2,3,...) -->";
XSLSHEET +="    <xsl:element name=\"Element\">";
XSLSHEET +="        <xsl:attribute name=\"type\">IdealDrift</xsl:attribute>";
XSLSHEET +="        <xsl:attribute name=\"id\">DR<xsl:number count=\"Element[@type='drift']\"/></xsl:attribute>";
XSLSHEET +="        <xsl:element name=\"Parameter\">";
XSLSHEET +="            <xsl:attribute name=\"name\">Length</xsl:attribute>";
XSLSHEET +="            <xsl:attribute name=\"type\">double</xsl:attribute>";
XSLSHEET +="            <xsl:attribute name=\"value\"><xsl:value-of select=\"@length\"/></xsl:attribute>";
XSLSHEET +="        </xsl:element>";
XSLSHEET +="        <xsl:element name=\"Parameter\">";
XSLSHEET +="            <xsl:attribute name=\"name\">SubCount</xsl:attribute>";
XSLSHEET +="            <xsl:attribute name=\"type\">int</xsl:attribute>";
XSLSHEET +="            <xsl:attribute name=\"value\">3</xsl:attribute>";
XSLSHEET +="        </xsl:element>";
XSLSHEET +="    </xsl:element>";
XSLSHEET +="</xsl:template>";
XSLSHEET +="";
XSLSHEET +="<xsl:template match=\"Element[@type='dipole']\">";
XSLSHEET +="    <xsl:element name=\"Element\" use-attribute-sets=\"dipole\">";
XSLSHEET +="    <xsl:element name=\"Parameter\">";
XSLSHEET +="        <xsl:attribute name=\"name\">Length</xsl:attribute>";
XSLSHEET +="        <xsl:attribute name=\"type\">double</xsl:attribute>";
XSLSHEET +="        <xsl:attribute name=\"value\"><xsl:value-of select=\"@length\"/></xsl:attribute>";
XSLSHEET +="    </xsl:element>";
XSLSHEET +="    <xsl:element name=\"Parameter\">";
XSLSHEET +="        <xsl:attribute name=\"name\">SubCount</xsl:attribute>";
XSLSHEET +="        <xsl:attribute name=\"type\">int</xsl:attribute>";
XSLSHEET +="        <xsl:attribute name=\"value\">3</xsl:attribute>";
XSLSHEET +="    </xsl:element>";
XSLSHEET +="    <xsl:apply-templates/>";
XSLSHEET +="    </xsl:element>";
XSLSHEET +="</xsl:template>";
XSLSHEET +="";
XSLSHEET +="<xsl:template match=\"Element[@type='quadrupole']\">";
XSLSHEET +="    <xsl:element name=\"Element\" use-attribute-sets=\"quadrupole\">";
XSLSHEET +="    <xsl:element name=\"Parameter\">";
XSLSHEET +="        <xsl:attribute name=\"name\">Length</xsl:attribute>";
XSLSHEET +="        <xsl:attribute name=\"type\">double</xsl:attribute>";
XSLSHEET +="        <xsl:attribute name=\"value\"><xsl:value-of select=\"@length\"/></xsl:attribute>";
XSLSHEET +="    </xsl:element>";
XSLSHEET +="    <xsl:element name=\"Parameter\">";
XSLSHEET +="        <xsl:attribute name=\"name\">SubCount</xsl:attribute>";
XSLSHEET +="        <xsl:attribute name=\"type\">int</xsl:attribute>";
XSLSHEET +="        <xsl:attribute name=\"value\">3</xsl:attribute>";
XSLSHEET +="    </xsl:element>";
XSLSHEET +="    <xsl:apply-templates/>";
XSLSHEET +="    </xsl:element>";
XSLSHEET +="</xsl:template>";
XSLSHEET +="";
XSLSHEET +="<xsl:template match=\"Element[@type='hsteerer']\">";
XSLSHEET +="    <xsl:element name=\"Element\" use-attribute-sets=\"dipole\">";
XSLSHEET +="    <xsl:element name=\"Parameter\">";
XSLSHEET +="        <xsl:attribute name=\"name\">Length</xsl:attribute>";
XSLSHEET +="        <xsl:attribute name=\"type\">double</xsl:attribute>";
XSLSHEET +="        <xsl:attribute name=\"value\"><xsl:value-of select=\"@length\"/></xsl:attribute>";
XSLSHEET +="    </xsl:element>";
XSLSHEET +="    <xsl:apply-templates/>";
XSLSHEET +="    </xsl:element>";
XSLSHEET +="</xsl:template>";
XSLSHEET +="";
XSLSHEET +="<xsl:template match=\"Element[@type='vsteerer']\">";
XSLSHEET +="    <xsl:element name=\"Element\" use-attribute-sets=\"dipole\">";
XSLSHEET +="    <xsl:element name=\"Parameter\">";
XSLSHEET +="        <xsl:attribute name=\"name\">Length</xsl:attribute>";
XSLSHEET +="        <xsl:attribute name=\"type\">double</xsl:attribute>";
XSLSHEET +="        <xsl:attribute name=\"value\"><xsl:value-of select=\"@length\"/></xsl:attribute>";
XSLSHEET +="    </xsl:element>";
XSLSHEET +="    <xsl:apply-templates/>";
XSLSHEET +="    </xsl:element>";
XSLSHEET +="</xsl:template>";
XSLSHEET +="";
XSLSHEET +="<xsl:template match=\"Element[@type='rfgap']\">";
XSLSHEET +="    <xsl:element name=\"Element\" use-attribute-sets=\"rfgap\">";
XSLSHEET +="    <xsl:apply-templates/>";
XSLSHEET +="    </xsl:element>";
XSLSHEET +="</xsl:template>";
XSLSHEET +="";
XSLSHEET +="<xsl:template match=\"Parameter[@name='StartPosition']\"><!--ignore this parameter--></xsl:template>";
XSLSHEET +="<xsl:template match=\"Parameter[@name='Position']\"><!--ignore this parameter--></xsl:template>";
XSLSHEET +="";
XSLSHEET +="<xsl:attribute-set name=\"dipole\">";
XSLSHEET +="    <xsl:attribute name=\"type\">IdealMagSteeringDipole</xsl:attribute>";
XSLSHEET +="    <xsl:attribute name=\"id\"><xsl:value-of select=\"@id\"/></xsl:attribute>";
XSLSHEET +="</xsl:attribute-set>";
XSLSHEET +="";
XSLSHEET +="<xsl:attribute-set name=\"quadrupole\">";
XSLSHEET +="    <xsl:attribute name=\"type\">IdealMagQuad</xsl:attribute>";
XSLSHEET +="    <xsl:attribute name=\"id\"><xsl:value-of select=\"@id\"/></xsl:attribute>";
XSLSHEET +="</xsl:attribute-set>";
XSLSHEET +="";
XSLSHEET +="<xsl:attribute-set name=\"rfgap\">";
XSLSHEET +="    <xsl:attribute name=\"type\">IdealRfGap</xsl:attribute>";
XSLSHEET +="    <xsl:attribute name=\"id\"><xsl:value-of select=\"@id\"/></xsl:attribute>";
XSLSHEET +="</xsl:attribute-set>";
XSLSHEET +="";
XSLSHEET +="</xsl:stylesheet>";
	}

	public static StringReader toReader() {
		return new StringReader(XSLSHEET);
	}
}
