<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes" 
	    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
	    doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" />
    <xsl:param name="basedir"/>

    <xsl:template match="entry">
	<li class="logitem">
	    <xsl:value-of select="date"/> <xsl:text> </xsl:text> <xsl:value-of select="time"/>
	    by <xsl:value-of select="author"/>
	    <div class="logmessage">
		<xsl:for-each select="./msg"><xsl:value-of select="."/></xsl:for-each>
	    </div>
	    <ul class="logfiles">
		<xsl:for-each select="./file/name">
		    <xsl:variable name="file" select="."/>
		    <li class="logfile"><a href="{$basedir}/{$file}"><xsl:value-of select="."/></a></li>
		</xsl:for-each>
	    </ul>
	</li>
    </xsl:template>

    <xsl:template match="/">
	<html>
	    <head>
		<title>Scilla Changelog</title>
		<link type="text/css" rel="stylesheet" href="homepage/web/css/style.css"/>
	    </head>
	    <body>

		<h2>Scilla Changelog</h2>
		<hr/>
		<ul>
		    <xsl:apply-templates select="/changelog/entry">
			<xsl:sort select="date" data-type="text" order="descending"/>
			<xsl:sort select="time" data-type="text" order="descending"/>
		    </xsl:apply-templates>
		</ul>

	    </body>
	</html>
    </xsl:template>

</xsl:stylesheet>
