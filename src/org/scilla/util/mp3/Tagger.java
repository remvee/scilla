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
 * @version $Revision: 1.1 $
 */
public class Tagger
{

    static Map commandMap = new HashMap();
    static
    {
	commandMap.put("-nop", NopCommand.class);
	commandMap.put("-echo", EchoCommand.class);
	commandMap.put("-help", HelpCommand.class);
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

    public Tagger (String[] args)
    throws Exception
    {
	// read options
	int argI = 0;
	for (; argI < args.length; argI++)
	{
	    String arg = args[argI];
	    if (! arg.startsWith("-")) break;

	    if (commandMap.containsKey(arg))
	    {
		// get command
		Class clazz = (Class) commandMap.get(arg);
		if (clazz == null) throw new Exception("command unknown: "+arg);
		Command cmd = (Command) clazz.newInstance();

		// configure command
		while (cmd.needMoreParameters())
		{
		    argI++;
		    if (argI >= args.length) throw new Exception("param expected for: "+arg);
		    cmd.addParameter(args[argI]);
		}

		// add to command queue
		commands.add(cmd);
	    }
	    else if (optionMap.containsKey(arg))
	    {
		options.add(arg);
	    }
	    else
	    {
		throw new Exception("option unknown: "+arg);
	    }
	}

	// determine current tag info
    }

    public void display ()
    {
	System.out.println("TODO display");
    }

    public void execute ()
    throws Exception
    {
	// execute commands
	Iterator it = commands.iterator();
	while (it.hasNext())
	{
	    Command cmd = (Command) it.next();
	    cmd.execute(null, null);
	}

	// write tags if modified
    }

    public boolean hasOption (String name) { return options.contains(name); }

    public static void main (String[] args)
    throws Exception
    {
	// no parameters, give usage message
	if (args.length == 0)
	{
	    System.out.println(getUsage());
	    return;
	}

	Tagger tagger = new Tagger(args);
	if (! tagger.hasOption("-quiet")) tagger.display();
	tagger.execute();
    }

    public static String getUsage ()
    throws Exception
    {
	Vector v; Iterator it;
	StringBuffer out = new StringBuffer();

	out.append("Available commands:\n");
	v = new Vector(commandMap.keySet());
	Collections.sort(v);
	it = v.iterator();
	while (it.hasNext())
	{
	    String name = (String) it.next();
	    Class clazz = (Class) commandMap.get(name);
	    Command cmd = (Command) clazz.newInstance();
	    out.append("  "); out.append(name); out.append('\n');
	    out.append("    "); out.append(cmd.getDescription()); out.append('\n');
	}

	out.append("Available options:\n");
	v = new Vector(optionMap.keySet());
	Collections.sort(v);
	it = v.iterator();
	while (it.hasNext())
	{
	    String name = (String) it.next();
	    String descr = (String) optionMap.get(name);
	    out.append("  "); out.append(name); out.append('\n');
	    out.append("    "); out.append(descr); out.append('\n');
	}

	return out.toString();
    }
}


abstract class Command
{
    public abstract String getDescription ();
    public abstract void execute (ID3v1 v1tag, ID3v2 v2tag)
    throws Exception;

    String[] args = null;
    int argN = 0;
    public boolean needMoreParameters ()
    {
	return args != null && argN < args.length;
    }
    public void addParameter (String arg)
    {
	args[argN++] = arg;
    }

    Set options = null;
    public void setOptions (Set set)
    {
	options = set;
    }
}

class NopCommand extends Command
{
    public String getDescription () { return "No OPeration."; }
    public void execute (ID3v1 v1tag, ID3v2 v2tag)
    throws Exception
    {
	System.out.println("No OPeration");
    }
}

class EchoCommand extends Command
{
    public EchoCommand () { args = new String[1]; }
    public String getDescription () { return "Echo P1."; }
    public void execute (ID3v1 v1tag, ID3v2 v2tag)
    throws Exception
    {
	System.out.println(args[0]);
    }
}

class HelpCommand extends Command
{
    public String getDescription () { return "Display help message."; }
    public void execute (ID3v1 v1tag, ID3v2 v2tag)
    throws Exception
    {
	System.out.println(Tagger.getUsage());
    }
}
