package test;

import java.io.*;
import java.util.*;

import org.scilla.*;
import org.scilla.info.*;

public class ImageBean {
    private String fname = null;
    private ImageInfo info = null;

    public ImageBean (String fname)
    throws Exception {
	this.fname = fname;

	String source = ConfigProvider.get().getString(Config.SOURCE_DIR_KEY);
	info = (ImageInfo) InfoFactory.get(source + File.separator + fname);
    }

    public String getFileName () {
	return fname;
    }
}
