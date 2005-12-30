package com.patelsoft.fastopen;

import java.util.Set;

/**
*	@author jiger
*	@version 1.0
*	@created 14/Dec/2004
*/


public interface IndexManager
{
	public Set getCollectedFiles();
	public void stop();
	public void suggestReindex();
	public void addIndexListener(IndexListener listener);
	public void removeIndexListener(IndexListener listener);
}




