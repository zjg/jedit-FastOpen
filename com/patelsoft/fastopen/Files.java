package com.patelsoft.fastopen;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.BufferHistory;
import org.gjt.sp.jedit.BufferHistory.Entry;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.Log;

import projectviewer.vpt.VPTNode;
import projectviewer.vpt.VPTProject;

public class Files
{
	private boolean PVThere;

	public Files()
	{
		//Test if PV is there or else use Buffers as the base.
		try
		{
			Class.forName("projectviewer.ProjectViewer");
			PVThere = true;
		}
		catch(ClassNotFoundException exp)
		{
			//Ahh no PV.
			PVThere = false;// Sanity sake. What if PV was there when calling the first time. and then the user unloads and deletes PV!!
		}
	}

	/**
	 *  Gets the currentProjectViewer attribute of the FastOpen object
	 *
	 * @return    The currentProjectViewer value
	 */
	public projectviewer.ProjectViewer getCurrentProjectViewer(View view)
	{
		Object obj = projectviewer.ProjectViewer.getViewer(view);
		if(obj instanceof projectviewer.ProjectViewer)
			return (projectviewer.ProjectViewer)obj;
		return null;
	}

	/**
	 *  Gets the currentProject attribute of the FastOpen object
	 * @return    The currentProject value
	 */
	public VPTProject getCurrentProject(View view)
	{
		if(isPVThere())
			return projectviewer.ProjectViewer.getActiveProject(view);
		return null;
	}


	public boolean isPVThere()
	{
		return PVThere;
	}

	public boolean isProjectLoaded(String projectName)
	{
		return projectviewer.ProjectManager.getInstance().isLoaded(projectName);
	}

	void prjFile2FOFile(View view, Collection<FastOpenFile> allFiles)
	{
		if(isPVThere() && atleatOneProject(view))
		{
			//Loop until project is loaded
			while(!isProjectLoaded(getCurrentProject(view).getName()))
			{
				try
				{
					Thread.sleep(5000);
				}
				catch(InterruptedException e)
				{}
			}
			VPTProject project = getCurrentProject(view);
			Log.log(Log.DEBUG,this,"Got project " + project);
			Collection<VPTNode> nodes = project.getOpenableNodes();
			Iterator<VPTNode> iterPrj = nodes.iterator();
			while(iterPrj.hasNext())
			{
				projectviewer.vpt.VPTFile file = (projectviewer.vpt.VPTFile)iterPrj.next();
				FastOpenFile fofile = new FastOpenFile(file);
				boolean added = allFiles.add(fofile);
				if(!added) //Means hashcode is same but status is different
				{
					allFiles.remove(fofile); //Removes the old duplicate class as per FOFile's hashCode impl.
					allFiles.add(fofile);
				}
			}
		}
	}//End of prjFile2FOFile
	
	
	/**
	*	Checks if PV has atleast one project created.
	*/
	private boolean atleatOneProject(View view)
	{
		if(isPVThere())
		{
			if(getCurrentProject(view) != null)
				return true;
		}
		return false;
	}

	void diffPrjFilesWithOpenBuffers(Buffer buffer[],View view,
		Collection<FastOpenFile> allFiles)
	{
		if(buffer == null || buffer.length == 0)
			return;

		if(isPVThere())
		{
			projectviewer.vpt.VPTProject project = getCurrentProject(view);
			if(project != null)
			{
				for(int i = 0; i < buffer.length; i++)
				{
					if(!project.isInProject(buffer[i].getPath()))
					{
						boolean exists = false;
						Iterator<FastOpenFile> iterAllFiles = allFiles.iterator();
						while(iterAllFiles.hasNext())
						{
							FastOpenFile fofile = (FastOpenFile)iterAllFiles.next();
							if(fofile.equals(buffer[i]))
							{
								exists = true;
								break;
							}
						}

						if(!exists)
						{
							FastOpenFile fofile = new FastOpenFile(buffer[i]);
							allFiles.add(fofile);
						}
					}
				}
			}
		}
		else
		{
			for(int i = 0; i < buffer.length; i++)
				allFiles.add(new FastOpenFile(buffer[i]));
		}
	}


	public void bufferEntry2FastOpenFile(List<Entry> files, Collection<FastOpenFile> allFiles)
	{
		Iterator<Entry> iter = files.iterator();
		while(iter.hasNext())
		{
			BufferHistory.Entry entry = (BufferHistory.Entry)iter.next();
			boolean exists = false;
			Iterator<FastOpenFile> iterAllFiles = allFiles.iterator();
			while(iterAllFiles.hasNext())
			{
				FastOpenFile fofile = (FastOpenFile)iterAllFiles.next();
				if(fofile.equals(entry))
				{
					exists = true;
					break;
				}
			}

			if(!exists)
			{
				FastOpenFile fofile = new FastOpenFile(entry);
				allFiles.add(fofile);
			}
		}
	}

	void getRecentFiles(Collection<FastOpenFile> allFiles)
	{
		bufferEntry2FastOpenFile(BufferHistory.getHistory(), allFiles);
	}

}
