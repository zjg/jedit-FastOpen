package com.patelsoft.fastopen;

    //Imports
        import java.util.*;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.jEdit;
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

    public FastOpenFile(Buffer buffer)
    {
        name = buffer.getName();
        path = buffer.getPath();
        projectFile = false;// Default value to make compiler happy. Also since there is no setProjectFile method this wud anyways be the default behaviour so let the compiler feel happy :)
        hashcode = (this.path+this.name).hashCode();
    }//End of FastOpenFile constructor

    public FastOpenFile(projectviewer.vpt.VPTFile file)
    {
        name = file.getName();
        path = file.getNodePath();
        projectFile = true;
        hashcode = (this.path+this.name).hashCode();
    }//End of FastOpenFile constructor

    public FastOpenFile(String fileName, String path, boolean projectFile, boolean isOpen)
    {
        this.name = fileName;
        this.path = path;
        this.projectFile = projectFile;
        hashcode = (this.path+this.name).hashCode();
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
        if(fofile!= null && fofile instanceof FastOpenFile)
        {
            FastOpenFile f = (FastOpenFile)fofile;
            if(OperatingSystem.isWindows())
            {
                return (getPath().equalsIgnoreCase(f.getPath()) && getName().equalsIgnoreCase(f.getName()));
            }
            else
            {
                return (getPath().equals(f.getPath()) && getName().equals(f.getName()));
            }
        }
        return super.equals(fofile);
    }

    public int hashCode()
    {
        return hashcode;
    }
}//End of class FastOpenFile

