<?xml version="1.0"?>

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

    <xsl:template match="title"/>

    <xsl:template match="para">
	<P>
	    <xsl:value-of select="." disable-output-escaping="yes"/>
	</P>
    </xsl:template>

    <xsl:template match="release">
	<P>
	    <xsl:variable name="filebase" select="@filebase"/>
	    <A href="{$filebase}.zip"><xsl:value-of select="@filebase"/>.zip</A>
	    <BR />
	    <xsl:value-of select="." disable-output-escaping="yes"/>
	</P>
    </xsl:template>

    <xsl:template match="changelog">
	<xsl:variable name="file" select="@file"/>
	<xsl:variable name="max" select="@max"/>
	<P>
	    Last <xsl:value-of select="@max"/> changelog messages.
	    <UL>
		<xsl:for-each select="document($file)/changelog/entry">
		    <xsl:if test="position() &lt; $max">
			<xsl:apply-templates select="."/>
		    </xsl:if>
		</xsl:for-each>
	    </UL>
	</P>
    </xsl:template>

    <!-- changelog entries -->
    <xsl:template match="entry">
	<LI>
	    <xsl:value-of select="date"/>
	    <xsl:text> </xsl:text>
	    <xsl:value-of select="time"/>
	    by <xsl:value-of select="author"/>

	    <P>
		<PRE><xsl:for-each select="./msg"><xsl:value-of select="."/></xsl:for-each></PRE>

		<UL>
		    <xsl:for-each select="./file/name">
			<LI><xsl:value-of select="."/></LI>
		    </xsl:for-each>
		</UL>
	    </P>
	</LI>
    </xsl:template>

    <xsl:template match="/">
	<xsl:variable name="location" select="/homepage/location"/>
	<xsl:variable name="email" select="/homepage/email"/>
	<HTML>
	    <HEAD>
		<TITLE><xsl:value-of select="/homepage/title"/></TITLE>
		<LINK rev="made" href="mailto:{$email}" title="{$location}"/>
	    </HEAD>

	    <BODY bgcolor="white">
		<H1><xsl:value-of select="/homepage/title"/></H1>
		<P>
		    <xsl:value-of select="/homepage/description"/>
		</P>

		<!-- menu -->
		<UL>
		    <xsl:for-each select="/homepage/section">
			<xsl:variable name="anchor"><xsl:number/></xsl:variable>
			<LI><A name="_{$anchor}" href="#{$anchor}"><xsl:value-of select="title"/></A></LI>
		    </xsl:for-each>
		</UL>
		<HR/>

		<xsl:for-each select="/homepage/section">
		    <xsl:variable name="anchor"><xsl:number/></xsl:variable>
		    <H2><A name="{$anchor}" href="#_{$anchor}"><xsl:value-of select="title"/></A></H2>
		    <xsl:for-each select=".">
			<xsl:apply-templates select="."/>
		    </xsl:for-each>
		</xsl:for-each>
		<HR/>

		<DIV align="right">
		    Problems with this site?
		    Email <A href="mailto:{$email}?subject={$location}">me</A>!
		    <BR/>$Date: 2002/03/01 15:01:13 $
		</DIV>
	    </BODY>
	</HTML>
    </xsl:template>

</xsl:transform>