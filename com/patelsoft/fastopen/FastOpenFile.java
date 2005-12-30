package com.patelsoft.fastopen;

	//Imports
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.BufferHistory;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.OperatingSystem;
import org.gjt.sp.jedit.View;
	//End of Imports

  /**
    *    A class that handles files generically.
    *
    *    @created 10 Mar 2003
    *    @author Jiger Patel
    *
  */

public class FastOpenFile
{
	private final String name,path;
    private final boolean projectFile;
	private final int hashcode; //Store hashcode of the file since the filename and path are final and cannot be changed so why calculate everytime in hashCode().
	private final String decoratedPath;


	public FastOpenFile(Buffer buffer)
	{
		name = buffer.getName();
		path = buffer.getPath();
		projectFile = false; //Since we can't determine if Buffer represents a file in Project
		hashcode = calculateHashCode();
		decoratedPath = decorate(name, path);
	}//End of FastOpenFile constructor

	public FastOpenFile(projectviewer.vpt.VPTFile file)
	{
		name = file.getName();
		path = file.getNodePath();
		projectFile = true;
		hashcode = calculateHashCode();
		decoratedPath = decorate(name, path);
	}//End of FastOpenFile constructor

	public FastOpenFile(String fileName, String path, boolean projectFile)
	{
		this.name = fileName;
		this.path = path;
		this.projectFile = projectFile;
		hashcode = calculateHashCode();
		decoratedPath = decorate(name, path);
	}

	public FastOpenFile(BufferHistory.Entry entry)
	{
		//,,false,false));
		this.name = MiscUtilities.getFileName(entry.path);
		this.path = entry.path;
		this.projectFile = false;
		hashcode = calculateHashCode();
		decoratedPath = decorate(name, path);
	}

	private int calculateHashCode()
	{
		if(OperatingSystem.isWindows())
		{
			return (this.path.toLowerCase() + this.name).hashCode();
		}
		return (this.path + this.name).hashCode();
	}

	public String getName()
	{
		return this.name;
	}

	public String getPath()
	{
		return this.path;
	}

	public boolean isProjectFile()
	{
		return this.projectFile;
	}

	public Buffer open(View view)
	{
		return jEdit.openFile(view,path);
	}

	public boolean isOpened()
	{
		return (jEdit.getBuffer(path) != null);
	}

	public boolean equals(Object fofile)
	{
            if(fofile != null && fofile instanceof FastOpenFile)
            {
			FastOpenFile f = (FastOpenFile)fofile;
			if(OperatingSystem.isWindows())
			{
                            //System.out.println("In equals OperatingSystem.isWindows() " + OperatingSystem.isWindows() +" file " + f.getName() +" f is Project file " + f.isProjectFile() +" this is prj. file " + projectFile);
                            /*if(f.getName().indexOf("EngineBean") != -1)
                            {
                              //  System.out.println("See ACEngineBean details " +  f.getName() + " " + ((getPath().equalsIgnoreCase(f.getPath()) && getName().equalsIgnoreCase(f.getName()))));
                            }*/
				return (getPath().equalsIgnoreCase(f.getPath()) && getName().equalsIgnoreCase(f.getName()));
			}
			return (getPath().equals(f.getPath()) && getName().equals(f.getName()));
		}
		return super.equals(fofile);
	}

	/*public boolean equals(projectviewer.vpt.VPTFile file) //Overloaded for convinient in Index Mgmt.
	{
		if(file != null)
		{
			if(OperatingSystem.isWindows())
			{
				return (getPath().equalsIgnoreCase(file.getNodePath()) && getName().equalsIgnoreCase(file.getName()) && projectFile);
			}
			return (getPath().equals(file.getNodePath()) && getName().equals(file.getName())  && projectFile);
		}
		return false;
	}

	public boolean equals(Buffer file) //Overloaded for convinient in Index Mgmt.
	{
		if(file != null)
		{
			if(OperatingSystem.isWindows())
			{
				return (getPath().equalsIgnoreCase(file.getPath()) && getName().equalsIgnoreCase(file.getName()) && !projectFile);
			}
			return (getPath().equals(file.getPath()) && getName().equals(file.getName()) && !projectFile);
		}
		return false;
	}

	public boolean equals(BufferHistory.Entry file) //Overloaded for convinient in Index Mgmt.
	{
		if(file != null)
		{
			if(OperatingSystem.isWindows())
			{
				return (getPath().equalsIgnoreCase(file.path) && getName().equalsIgnoreCase(MiscUtilities.getFileName(file.path)) && !projectFile);
			}
			return (getPath().equals(file.path) && getName().equals(MiscUtilities.getFileName(file.path)) && !projectFile);
		}
		return false;
	}*/

	public int hashCode()
	{
		return hashcode;
	}

	public String getDecoratedPath()
	{
		return decoratedPath;
	}


	private String decorate(String name, String path)
	{
		return name + " (" + path + ")";
	}
}//End of class FastOpenFile

