<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
            <!--xmlns="http://www.w3.org/TR/xslt"-->

<xsl:output method="xml" doctype-system="Lattice.mod.xal.dtd" indent="yes"/>

<!-- suppress text nodes unless requested -->
<xsl:template match="text()"/>

<!-- default: copy the element with all its attributes -->
<xsl:template match="*|/">
    <xsl:copy>
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates/>
    </xsl:copy>
</xsl:template>
    
<!-- all mappings to Marker -->
<xsl:template match="Element[@type='marker'] |
                     Element[@type='pmarker'] |
                     Element[@type='beampositionmonitor'] | 
                     Element[@type='beamcurrentmonitor'] |
                     Element[@type='beamlossmonitor'] |
                     Element[@type='wirescanner']">
	 <xsl:element name="Element">
	     <xsl:attribute name="type">Marker</xsl:attribute>
	     <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
	 </xsl:element>
</xsl:template>

<xsl:template match="Element[@type='drift']">
    <!-- use drift element counting to generate drift ids DRx (x=1,2,3,...) -->
    <xsl:element name="Element">
        <xsl:attribute name="type">IdealDrift</xsl:attribute>
        <xsl:attribute name="id">DR<xsl:number count="Element[@type='drift']"/></xsl:attribute>
        <xsl:element name="Parameter">
            <xsl:attribute name="name">Length</xsl:attribute>
            <xsl:attribute name="type">double</xsl:attribute>
            <xsl:attribute name="value"><xsl:value-of select="@length"/></xsl:attribute>
        </xsl:element>
        <xsl:element name="Parameter">
            <xsl:attribute name="name">SubCount</xsl:attribute>
            <xsl:attribute name="type">int</xsl:attribute>
            <xsl:attribute name="value">3</xsl:attribute>
        </xsl:element>
    </xsl:element>
</xsl:template>

<xsl:template match="Element[@type='dipole']">
    <xsl:element name="Element" use-attribute-sets="dipole">
    <xsl:element name="Parameter">
        <xsl:attribute name="name">Length</xsl:attribute>
        <xsl:attribute name="type">double</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@length"/></xsl:attribute>
    </xsl:element>
    <xsl:element name="Parameter">
        <xsl:attribute name="name">SubCount</xsl:attribute>
        <xsl:attribute name="type">int</xsl:attribute>
        <xsl:attribute name="value">3</xsl:attribute>
    </xsl:element>
    <xsl:apply-templates/>
    </xsl:element>
</xsl:template>

<xsl:template match="Element[@type='quadrupole']">
    <xsl:element name="Element" use-attribute-sets="quadrupole">
    <xsl:element name="Parameter">
        <xsl:attribute name="name">Length</xsl:attribute>
        <xsl:attribute name="type">double</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@length"/></xsl:attribute>
    </xsl:element>
    <xsl:element name="Parameter">
        <xsl:attribute name="name">SubCount</xsl:attribute>
        <xsl:attribute name="type">int</xsl:attribute>
        <xsl:attribute name="value">3</xsl:attribute>
    </xsl:element>
    <xsl:apply-templates/>
    </xsl:element>
</xsl:template>

<xsl:template match="Element[@type='hsteerer']">
    <xsl:element name="Element" use-attribute-sets="dipole">
    <xsl:element name="Parameter">
        <xsl:attribute name="name">Length</xsl:attribute>
        <xsl:attribute name="type">double</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@length"/></xsl:attribute>
    </xsl:element>
    <xsl:apply-templates/>
    </xsl:element>
</xsl:template>

<xsl:template match="Element[@type='vsteerer']">
    <xsl:element name="Element" use-attribute-sets="dipole">
    <xsl:element name="Parameter">
        <xsl:attribute name="name">Length</xsl:attribute>
        <xsl:attribute name="type">double</xsl:attribute>
        <xsl:attribute name="value"><xsl:value-of select="@length"/></xsl:attribute>
    </xsl:element>
    <xsl:apply-templates/>
    </xsl:element>
</xsl:template>

<xsl:template match="Element[@type='rfgap']">
    <xsl:element name="Element" use-attribute-sets="rfgap">
    <xsl:apply-templates/>
    </xsl:element>
</xsl:template>

<xsl:template match="Parameter[@name='StartPosition']"><!--ignore this parameter--></xsl:template>
<xsl:template match="Parameter[@name='Position']"><!--ignore this parameter--></xsl:template>

<xsl:attribute-set name="dipole">
    <xsl:attribute name="type">IdealMagDipole</xsl:attribute>
    <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="quadrupole">
    <xsl:attribute name="type">IdealMagQuad</xsl:attribute>
    <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="rfgap">
    <xsl:attribute name="type">IdealRfGap</xsl:attribute>
    <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
</xsl:attribute-set>
    
</xsl:stylesheet>
