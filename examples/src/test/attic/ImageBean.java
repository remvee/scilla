package test;

import java.io.*;
import java.util.*;

import org.scilla.*;
import org.scilla.info.*;

public class ImageBean {
    private String fname = null;
    private ImageInfo info = null;

    public ImageBean () {
	// empty
    }

    public ImageBean (String fname)
    throws Exception {
	setFileName(fname);
    }

    public String getFileName () {
	return fname;
    }
    public void setFileName (String fname)
    throws Exception {
	this.fname = fname;
	String f = AppConfig.getSourceDir() + File.separator + fname;
	info = (ImageInfo) InfoFactory.get(f);
    }

    public int getWidth () {
	return info.getWidth();
    }

    public int getHeight () {
	return info.getHeight();
    }
}
