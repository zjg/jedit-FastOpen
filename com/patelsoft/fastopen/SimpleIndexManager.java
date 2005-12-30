package com.patelsoft.fastopen;


import org.gjt.sp.jedit.*;
import java.util.*;

/**
*   This class contains the Default FastOpen behaviour to collect files everytime from sources on each call.
*
*	@author jiger
*	@version 1.0
*	@created 15/Dec/2004
*/


public class SimpleIndexManager extends AbstractIndexManager
{
	/**
	*	Constructor for SimpleIndexManager
	*
	*/
	public SimpleIndexManager(View view, Files files)
	{
		super(view, files);
	}//End of constructor

	public Set getCollectedFiles()
	{
		super.queryFilesFromSource();
		return allfiles;//We can set super.allfiles to NULL beforen returning the allfiles but we are not doing so inorder to benefit from the checking that the source querying logic does inorder to prevent frequent Object creation if the files already exists in allfiles.
	}

	public void stop()
	{
		//Nothing to stop.
	}

	public void suggestReindex()
	{
		//Ignore since we anyways query on each getCollectedFiles call.
	}

}




