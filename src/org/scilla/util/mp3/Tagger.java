/*
 * scilla
 *
 * Copyright (C) 2001  R.W. van 't Veer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 */

package org.scilla.util.mp3;

import java.io.*;
import java.util.*;

import org.scilla.util.mp3.id3v2.*;

/**
 * MP3 tag commandline utillity.
 *
 * @author Remco van 't Veer
 * @version $Revision: 1.9 $
 */
public class Tagger {
    static Map commandMap = new HashMap();
    static
    {
        commandMap.put("-help", HelpCommand.class);
        commandMap.put("-1to2", V1ToV2Command.class);
        commandMap.put("-2to1", V2ToV1Command.class);
        commandMap.put("-v2settextframe", V2SetTextFrameCommand.class);
        commandMap.put("-v2addtextframe", V2AddTextFrameCommand.class);
        commandMap.put("-v2deltextframe", V2DelTextFrameCommand.class);
        commandMap = Collections.unmodifiableMap(commandMap);
    }

    static Map optionMap = new HashMap();
    static
    {
        optionMap.put("-verbose", "Give verbose messages.");
        optionMap.put("-quiet", "Give no informative messages or warnings.");
        optionMap = Collections.unmodifiableMap(optionMap);
    }

    private List commands = new Vector();
    private Set options = new HashSet();
    private String filename = null;
    private ID3v1 v1tag = null;
    private ID3v2 v2tag = null;
    boolean v1tagModified = true;
    boolean v2tagModified = true;

    public Tagger (String[] args)
    throws Exception {
        // read options
        int argI = 0;
        for (; argI < args.length; argI++) {
            String arg = args[argI];
            if (! arg.startsWith("-")) {
                break;
	    }

            if (commandMap.containsKey(arg)) {
                // get command
                Class clazz = (Class) commandMap.get(arg);
                if (clazz == null) {
                    throw new Exception("command unknown: "+arg);
		}
                Command cmd = (Command) clazz.newInstance();

                // configure command
                while (cmd.needMoreParameters()) {
                    argI++;
                    if (argI >= args.length) {
                        throw new Exception("param expected for: "+arg);
		    }
                    cmd.addParameter(args[argI]);
                }

                // add to command queue
                commands.add(cmd);
            } else if (optionMap.containsKey(arg)) {
                options.add(arg);
            } else {
                throw new Exception("option unknown: "+arg);
            }
        }

        // determine input file
        if (argI != args.length-1) {
            throw new Exception("input file expected");
	}
        filename = args[argI];

        // determine current tag info
        File f = new File(filename);
        v1tag = new ID3v1(f);
        v2tag = new ID3v2(f);
    }

    public void display () {
        System.out.println("ID3v1: "+v1tag);
        System.out.println("ID3v2: "+v2tag);
    }

    public void execute ()
    throws Exception {
        // execute commands
        Iterator it = commands.iterator();
        while (it.hasNext()) {
            Command cmd = (Command) it.next();
            cmd.execute(v1tag, v2tag);
        }

        // write tags if modified
        if (v1tagModified || v1tagModified) {
            File oldf = new File(filename);
            File newf = new File(filename+".new");

            if (v2tagModified && v2tag.getFrames().size() > 0) {
                InputStream in = new FileInputStream(oldf);
                OutputStream out = new FileOutputStream(newf);

                ID3v2 dummy = new ID3v2(oldf);
                dummy.readTag(in);
                if (! dummy.hasTag()) {
                    in = new FileInputStream(oldf);
		}
                System.out.println("writing v2 tag");
                v2tag.writeTag(out);

                byte[] b = new byte[4096];
                int n;
                while ((n = in.read(b, 0, b.length)) != -1) {
                    out.write(b, 0, n);
		}
                out.close();
                in.close();
            } else {
                InputStream in = new FileInputStream(oldf);
                OutputStream out = new FileOutputStream(newf);
                byte[] b = new byte[4096];
                int n;
                while ((n = in.read(b, 0, b.length)) != -1) {
                    out.write(b, 0, n);
		}
                out.close();
                in.close();
            }

            if (v1tagModified) {
                System.out.println("writing v1 tag");
                RandomAccessFile f = new RandomAccessFile(newf, "rw");
                try {
                    f.seek(v1tag.fileHasTag() ? f.length() - 128 : f.length());
                    f.write(v1tag.getFromID3Tag());
                } finally {
                    f.close();
                }
            }
        }
    }

    public boolean hasOption (String name) {
        return options.contains(name);
    }

    public static void main (String[] args)
    throws Exception {
        // no parameters, give usage message
        if (args.length == 0) {
            System.out.println(getUsage());
            return;
        }

        Tagger tagger = new Tagger(args);
        tagger.execute();
        if (! tagger.hasOption("-quiet")) {
            tagger.display();
	}
    }

    public static String getUsage ()
    throws Exception {
        List l;
        StringBuffer out = new StringBuffer();

        out.append("Available commands:\n");
        l = new Vector(commandMap.keySet());
        Collections.sort(l);
	for (Iterator it = l.iterator(); it.hasNext();) {
            String name = (String) it.next();
            Class clazz = (Class) commandMap.get(name);
            Command cmd = (Command) clazz.newInstance();
            out.append("  ");
            out.append(name);
            out.append('\n');
            out.append("    ");
            out.append(cmd.getDescription());
            out.append('\n');
        }

        out.append("Available options:\n");
        l = new Vector(optionMap.keySet());
        Collections.sort(l);
	for (Iterator it = l.iterator(); it.hasNext();) {
            String name = (String) it.next();
            String descr = (String) optionMap.get(name);
            out.append("  ");
            out.append(name);
            out.append('\n');
            out.append("    ");
            out.append(descr);
            out.append('\n');
        }

        return out.toString();
    }
}


abstract class Command {
    public abstract String getDescription ();
    public abstract void execute (ID3v1 v1tag, ID3v2 v2tag)
    throws Exception;

    String[] args = null;
    int argN = 0;
    public boolean needMoreParameters () {
        return args != null && argN < args.length;
    }
    public void addParameter (String arg) {
        args[argN++] = arg;
    }
    Set options = null;
    public void setOptions (Set set) {
        options = set;
    }
}

class HelpCommand extends Command {
    public String getDescription () {
        return "Display help message.";
    }
    public void execute (ID3v1 v1tag, ID3v2 v2tag)
    throws Exception {
        System.out.println(Tagger.getUsage());
    }
}

class V1ToV2Command extends Command {
    public String getDescription () {
        return "Write ID3v2 tag from available ID3v1 tag.";
    }
    public void execute (ID3v1 v1tag, ID3v2 v2tag)
    throws Exception {
        v2tag.setFrame(new TextFrame("TALB", ID3v1.CHAR_ENCODING, v1tag.getAlbum()));
        v2tag.setFrame(new TextFrame("TIT2", ID3v1.CHAR_ENCODING, v1tag.getTitle()));
        v2tag.setFrame(new TextFrame("TPE1", ID3v1.CHAR_ENCODING, v1tag.getArtist()));
        v2tag.setFrame(new TextFrame("TYER", ID3v1.CHAR_ENCODING, v1tag.getYear()));
        v2tag.setFrame(new TextFrame("TRCK", ID3v1.CHAR_ENCODING, v1tag.getTrkNum()+""));
        v2tag.setFrame(new TextFrame("TCON", ID3v1.CHAR_ENCODING, "("+v1tag.getGenre()+")"));
        v2tag.setFrame(new TextFrame("TXXX", ID3v1.CHAR_ENCODING, "ID3v1Comment", v1tag.getComment()));
    }
}

class V2ToV1Command extends Command {
    public String getDescription () {
        return "Write ID3v1 tag from available ID3v2 tag.";
    }
    public void execute (ID3v1 v1tag, ID3v2 v2tag)
    throws Exception {
        TextFrame f;
        f = (TextFrame) v2tag.getFrame("TALB");
        if (f != null) {
            v1tag.setAlbum(f.getText());
	}
        f = (TextFrame) v2tag.getFrame("TIT2");
        if (f != null) {
            v1tag.setTitle(f.getText());
	}
        f = (TextFrame) v2tag.getFrame("TPE1");
        if (f != null) {
            v1tag.setArtist(f.getText());
	}
        // TODO or TPE2, TPE3, TPE4

        f = (TextFrame) v2tag.getFrame("TYER");
        if (f != null) {
            v1tag.setYear(f.getText());
	}
        // TODO TRCK
        // TODO TCON
        // TODO COMM/TXXX
    }
}

class V2SetTextFrameCommand extends Command {
    public V2SetTextFrameCommand () {
        args = new String[2];
    }
    public String getDescription () {
        return
            "Create text frame with P1 as identifier and P2 as content.  All\n"+
            "other frames with the given identifier will be deleted.";
    }
    public void execute (ID3v1 v1tag, ID3v2 v2tag)
    throws Exception {
        v2tag.setFrame(new TextFrame(args[0], null, args[1]));
    }
}

class V2AddTextFrameCommand extends Command {
    public V2AddTextFrameCommand () {
        args = new String[2];
    }
    public String getDescription () {
        return "Create text frame with P1 as identifier and P2 as content.";
    }
    public void execute (ID3v1 v1tag, ID3v2 v2tag)
    throws Exception {
        v2tag.addFrame(new TextFrame(args[0], null, args[1]));
    }
}

class V2DelTextFrameCommand extends Command {
    public V2DelTextFrameCommand () {
        args = new String[1];
    }
    public String getDescription () {
        return "Create text frame with P1 as identifier.";
    }
    public void execute (ID3v1 v1tag, ID3v2 v2tag)
    throws Exception {
        v2tag.removeFrames(args[0]);
    }
}
