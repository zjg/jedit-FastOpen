package com.patelsoft.fastopen;

import java.util.*;
import org.gjt.sp.jedit.*;

import projectviewer.vpt.VPTProject;
// import projectviewer.*;
// import projectviewer.config.*;
// import projectviewer.vpt.*;

public class Files
{
	private boolean PVThere;
	FastOpen fo;

	public Files(FastOpen fo)
	{
		this.fo = fo;
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
		{
			return (projectviewer.ProjectViewer)obj;
		}
		return null;
	}

	/**
	 *  Gets the currentProject attribute of the FastOpen object
	 *
	 * @return    The currentProject value
	 */
	public VPTProject getCurrentProject(View view)
	{
		if(isPVThere())
		{
			return projectviewer.PVActions.getCurrentProject(view);
		}
		return null;
	}


	public boolean isPVThere()
	{
		return PVThere;
	}

	/**
	 *  Description of the Method
	 *
	 * @param  projectfiles  Description of the Parameter
	 * @return               Description of the Return Value
	 */
	void prjFile2FOFile(View view, Collection allFiles)
	{
		if(isPVThere())
		{
			//Iterator iter = ((projectviewer.vpt.VPTProject)getCurrentProject(view)).getOpenableNodes().iterator();
			Iterator iter = (getCurrentProject(view)).getOpenableNodes().iterator();
			while(iter.hasNext())
			{
				allFiles.add(new FastOpenFile((projectviewer.vpt.VPTFile)iter.next()));
			}
		}
	}

		/**
	*  Description of the Method
	*
	* @param  project  Description of the Parameter
	* @param  buffer   Description of the Parameter
	* @return          Description of the Return Value
	*/
	void diffPrjFilesWithOpenBuffers(Buffer buffer[],View view, Collection allFiles)
	{
		if(buffer == null || buffer.length == 0)
		{
			return;
		}

		for(int i = 0; i < buffer.length; i++)
		{
			if(isPVThere())
			{
				projectviewer.vpt.VPTProject project = getCurrentProject(view);
				if(project != null)
				{
					//if(!project.isInProject(buffer[i].getPath()))
					if(!project.isInProject(buffer[i].getPath()))
					{
						allFiles.add(new FastOpenFile(buffer[i]));
					}
				}
			}
			else
			{
				allFiles.add(new FastOpenFile(buffer[i]));
			}
		}
	}


	public void bufferEntry2FastOpenFile(List files, Collection allFiles)
	{
		Iterator iter = files.iterator();
		while(iter.hasNext())
		{
			BufferHistory.Entry entry = (BufferHistory.Entry)iter.next();
			allFiles.add(new FastOpenFile(MiscUtilities.getFileName(entry.path),entry.path,false,false));
		}
	}
}
