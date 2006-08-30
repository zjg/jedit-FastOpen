package com.patelsoft.fastopen;

import org.gjt.sp.jedit.*;
import java.util.*;

/**
*
*	@author jiger
*	@version 1.0
*	@created 15/Dec/2004
*/


public abstract class AbstractIndexManager implements IndexManager
{
	Files files;
	View view;
	Set allfiles;
	List listeners;
	protected final int INDEXING_STARTED=1;
	protected final int INDEXING_COMPLETED=2;


	/**
	*	Constructor for AbstractIndexManager
	*
	*/
	public AbstractIndexManager(View view,Files files)
	{
		super();
		this.view = view;
		this.files = files;
		allfiles = new HashSet(1,1.0f);
		listeners = new ArrayList();
	}//End of constructor

	/**
	*   Since collecting files strategy is same no matter the type of IndexManager. Ths generic piece of code is put into this class.
	*/
	protected void queryFilesFromSource()
	{
		//System.out.println("Inside queryFilesFromSource allfiles size " + allfiles.size());
		notifyListeners(INDEXING_STARTED);
		Set tmp_set = new HashSet(20,1.0f);
		if(allfiles != null && allfiles.size() > 0)
		{
			tmp_set.addAll(allfiles);
		}
		//boolean hideOpenFiles = jEdit.getBooleanProperty("fastopen.hideOpenFiles");
		//start = System.currentTimeMillis();
		files.prjFile2FOFile(view,tmp_set);

		files.diffPrjFilesWithOpenBuffers(jEdit.getBuffers(),view, tmp_set);

		if(jEdit.getBooleanProperty("fastopen.showrecentfiles"))
		{
			files.getRecentFiles(tmp_set);
		}

		allfiles = tmp_set;
		notifyListeners(INDEXING_COMPLETED);
	}


	public void addIndexListener(IndexListener listener)
	{
		if(listener != null)
		{
			listeners.add(listener);
		}
	}

	public void removeIndexListener(IndexListener listener)
	{
		if(listener != null)
		{
			listeners.remove(listener);
		}
	}

	public void notifyListeners(int activity)
	{
		if(activity == INDEXING_STARTED)
		{
			Iterator iterListeners = listeners.iterator();
			while(iterListeners.hasNext())
			{
				IndexListener listener = (IndexListener)iterListeners.next();
				listener.indexingStarted(this);
			}
		}
		else if(activity == INDEXING_COMPLETED)
		{
			Iterator iterListeners = listeners.iterator();
			while(iterListeners.hasNext())
			{
				IndexListener listener = (IndexListener)iterListeners.next();
				listener.indexingCompleted(this);
			}
		}
		else
		{
			System.out.println("Invalid ACTIVITY");
		}
	}
}






