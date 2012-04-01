package com.patelsoft.fastopen;

import org.gjt.sp.jedit.View;
/**
  @author Alan Ezust
 */

public class FileOpener extends org.jedit.core.FileOpenerService {
	public void openFile(String fileName, View view) {
		FastOpen fo = new FastOpen(view, fileName);
		fo.showWindow();
	}
}
