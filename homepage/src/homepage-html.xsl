<?xml version="1.0"?>

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>

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
		    <xsl:for-each select="para">
			<P>
			    <xsl:value-of select="." disable-output-escaping="yes"/>
			</P>
		    </xsl:for-each>
		    <UL>
			<xsl:for-each select="release">
			    <LI>
				<xsl:variable name="filebase" select="@filebase"/>
				<A href="{$filebase}.zip"><xsl:value-of select="@filebase"/>.zip</A>
				<P><xsl:value-of select="." disable-output-escaping="yes"/></P>
			    </LI>
			</xsl:for-each>
		    </UL>
		</xsl:for-each>
		<HR/>

		<DIV align="right">
		    Problems with this site?
		    Email <A href="mailto:{$email}?subject={$location}">me</A>!
		    <BR/>$Date: 2002/02/23 15:13:34 $
		</DIV>
	    </BODY>
	</HTML>
    </xsl:template>

</xsl:transform>
