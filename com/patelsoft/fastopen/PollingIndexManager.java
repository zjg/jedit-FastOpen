package com.patelsoft.fastopen;

import java.util.Set;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.*;

/**
*
*	@author jiger
*	@version 1.0
*	@created 14/Dec/2004
*/

public class PollingIndexManager extends AbstractIndexManager implements Runnable
{
	Thread thread;
	boolean interrupted;
	boolean collecting;

	/**
	*	Constructor for PollingIndexManager
	*/
	public PollingIndexManager(View view, Files files)
	{
		super(view, files);

		thread = new Thread(this);
		thread.setDaemon(true);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}//End of constructor

	public void run()
	{
		while(!interrupted)
		{
			try
			{
				collectFiles();
				Thread.sleep(jEdit.getIntegerProperty("fastopen.indexing.freq",10)*1000);
			}
			catch(java.lang.InterruptedException e)
			{
				interrupted = true;
			}
		}
	}//End of main()

	private void collectFiles()
	{
		if(collecting) //Check if somebody is running this method already
		{
			System.out.println("Currently collecting. ReIndex request ignored.");
			return;
		}
        long start = System.currentTimeMillis();
		collecting = true;
		queryFilesFromSource();
		long end = System.currentTimeMillis();
		Log.log(Log.MESSAGE, this, "Polling Thread ran and collected "+(allfiles != null? allfiles.size():0) + " files in " + (end-start)/1000 +" secs");
		collecting = false;
	}

	public Set<FastOpenFile> getCollectedFiles()
	{
		return allfiles;
	}

	public void stop()
	{
		interrupted = true;
	}

	public void suggestReindex()
	{
        System.out.println("----------------------Re-Index suggested----------------Re-Indexing");
		allfiles.clear();
		collectFiles();
	}
}




