<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>

    <!-- discard title elements in sections -->
    <xsl:template match="title"/>

    <!-- copy content of html blocks -->
    <xsl:template match="html">
	<xsl:value-of select="." disable-output-escaping="yes"/>
    </xsl:template>

    <!-- a downloadable release from a cvs tag -->
    <xsl:template match="release">
	<p>
	    <xsl:variable name="filebase" select="@filebase"/>
	    <a href="{$filebase}.zip"><xsl:value-of select="@filebase"/>.zip</a>
	    <br />
	    <xsl:value-of select="html" disable-output-escaping="yes"/>
	</p>
    </xsl:template>

    <!-- the first @max entries of external changelog file -->
    <xsl:template match="changelog">
	<xsl:variable name="file" select="@file"/>
	<xsl:variable name="max" select="@max"/>
	<xsl:variable name="dist" select="@dist"/>
	<p>
	    <xsl:choose>
		<xsl:when test="$dist != ''">
		    Last <xsl:value-of select="@max"/>
		    <xsl:text> </xsl:text>
		    <a href="{$dist}">changelog</a> messages.
		</xsl:when>
		<xsl:otherwise>
		    Last <xsl:value-of select="@max"/> changelog messages.
		</xsl:otherwise>
	    </xsl:choose>
	    <ul>
		<xsl:for-each select="document($file)/changelog/entry">
		    <xsl:if test="position() &lt;= $max">
			<xsl:apply-templates select="."/>
		    </xsl:if>
		</xsl:for-each>
	    </ul>
	</p>
    </xsl:template>

    <!-- changelog entries -->
    <xsl:template match="entry">
	<li>
	    <xsl:value-of select="date"/><xsl:text> </xsl:text><xsl:value-of select="time"/>
	    by <xsl:value-of select="author"/>

	    <div>
		<xsl:for-each select="./msg"><xsl:value-of select="."/></xsl:for-each>
	    </div>
	    <ul>
		<xsl:for-each select="./file/name">
		    <li><xsl:value-of select="."/></li>
		</xsl:for-each>
	    </ul>
	</li>
    </xsl:template>

    <!-- html root -->
    <xsl:template match="/">
	<xsl:variable name="location" select="/homepage/location"/>
	<html>
	    <head>
		<title><xsl:value-of select="/homepage/title"/></title>
	    </head>

	    <body>
		<h1><xsl:value-of select="/homepage/title"/></h1>
		<xsl:apply-templates select="/homepage/description"/>

		<!-- menu -->
		<ul>
		    <xsl:for-each select="/homepage/section">
			<xsl:variable name="anchor"><xsl:number/></xsl:variable>
			<li><a name="_{$anchor}" href="#{$anchor}"><xsl:value-of select="title"/></a></li>
		    </xsl:for-each>
		</ul>
		<hr/>

		<xsl:for-each select="/homepage/section">
		    <xsl:variable name="anchor"><xsl:number/></xsl:variable>
		    <h2><a name="{$anchor}" href="#_{$anchor}"><xsl:value-of select="title"/></a></h2>
		    <xsl:apply-templates select="."/>
		</xsl:for-each>
		<hr/>

		<div align="right">
		    <xsl:apply-templates select="/homepage/date"/>
		</div>
	    </body>
	</html>
    </xsl:template>

</xsl:stylesheet>
