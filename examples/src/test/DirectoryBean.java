package test;

import java.io.*;
import java.util.*;

import org.scilla.*;
import org.scilla.util.*;
import org.scilla.info.*;

public class DirectoryBean {

    public DirectoryBean () {
	// empty
    }

    public DirectoryBean (String path)
    throws Exception {
	if (path == null || path.length() == 0) {
	    path = "";
	}
	this.path = path;

	// basename
	int i = path.lastIndexOf(File.separator);
	name = i != -1 ? path.substring(i+1) : path;

	scan(false);
    }

    public String getName () {
	return name;
    }
    private String name = null;

    public void setPath (String path)
    throws Exception {
	if (path == null || path.length() == 0) {
	    path = "";
	}
	this.path = path;

	scan(true);
    }
    public String getPath () {
	return path;
    }
    private String path = null;

    public String getParentPath ()
    throws Exception {
	int i = path.lastIndexOf('/');
	if (i != -1) {
	    return path.substring(0, i);
	}
	return null;
    }

    private void scan (boolean includeDirectories)
    throws Exception {
	////////////////////////////////////
	Map typemap = new HashMap();
	///////////////////////////////////////

	String pathname = AppConfig.getSourceDir() + File.separator + path;

	String[] files = (new File(pathname)).list();
	Arrays.sort(files);

	for (int i = 0; i < files.length; i++) {
	    if (files[i].startsWith(".")) {
		continue;
	    }

	    String fname = pathname + File.separator + files[i];
	    File f = new File(fname);

	    //////////////////////////////
	    // build type-key maps and lists
	    if (f.isDirectory() && includeDirectories) {
		String type = "directory";

		List list = (List) lists.get(type);
		if (list == null) {
		    list = new ArrayList();
		    lists.put(type, list);
		}
		list.add(new DirectoryBean(path + File.separator + files[i]));
	    } else {
		Info info = InfoFactory.get(fname);
		if (info != null) {

		    String type = null;
		    if (info instanceof AudioInfo) {
			type = "audio";
		    } else if (info instanceof ImageInfo) {
			type = "image";
		    }

		    List list = (List) lists.get(type);
		    if (list == null) {
			list = new ArrayList();
			lists.put(type, list);
		    }
		    list.add(info);

		    List all = (List) lists.get("all");
		    if (all == null) {
			all = new ArrayList();
			lists.put("all", all);
		    }
		    all.add(info);

		    Map keymap = (Map) typemap.get(type);
		    if (keymap == null) {
			keymap = new HashMap();
			typemap.put(type, keymap);
		    }

		    for (Iterator it = info.entrySet().iterator(); it.hasNext();) {
			Map.Entry me = (Map.Entry) it.next();
			Object key = me.getKey();
			Object val = me.getValue();

			Set values = (Set) keymap.get(key);
			if (values == null) {
			    values = new HashSet();
			    keymap.put(key, values);
			}
			values.add(val);
		    }
		}
	    }
	}

	//////////////////////////////////
	// count maps for type-key pairs
	for (Iterator it0 = typemap.entrySet().iterator(); it0.hasNext();) {
	    Map.Entry me0 = (Map.Entry) it0.next();
	    String type = (String) me0.getKey();
	    Map keymap = (Map) me0.getValue();

	    Map countmap = (Map) count.get(type);
	    if (countmap == null) {
		countmap = new HashMap();
		count.put(type, countmap);
	    }

	    for (Iterator it1 = keymap.entrySet().iterator(); it1.hasNext();) {
		Map.Entry me1 = (Map.Entry) it1.next();
		String key = (String) me1.getKey();
		Set set = (Set) me1.getValue();

		countmap.put(key, new Integer(set.size()));
	    }
	}

	//////////////////////////////////
	// sum maps for type-key pairs of type integer
	{
	    // TODO find fast dynamic solution!
	    int audioLenght = 0;
	    List audioList = (List) lists.get("audio");
	    if (audioList != null) {
		for (Iterator it = audioList.iterator(); it.hasNext();) {
		    AudioInfo info = (AudioInfo) it.next();
		    audioLenght += info.getLength();
		}
	    }
	    Map audioSums = new HashMap();
	    audioSums.put("length", new Integer(audioLenght));
	    sums.put("audio", audioSums);
	}

	//////////////////////////////////
	// count maps for lists
	{
	    Map listCounts = new HashMap();
	    for (Iterator it = lists.entrySet().iterator(); it.hasNext();) {
		Map.Entry me = (Map.Entry) it.next();
		String type = (String) me.getKey();

		if (me.getValue() instanceof List) {
		    List list = (List) me.getValue();
		    listCounts.put(type, new Integer(list.size()));
		}
	    }
	    lists.put("count", listCounts);
	}
    }

    private Map count = new HashMap();
    public Map getCount () {
	return count;
    }
    private Map lists = new HashMap();
    public Map getList () {
	return lists;
    }
    private Map sums = new HashMap();
    public Map getSum () {
	return sums;
    }
}
