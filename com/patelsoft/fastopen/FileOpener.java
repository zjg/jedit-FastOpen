package com.patelsoft.fastopen;

import org.gjt.sp.jedit.View;
/** Used by ErrorList 2.0 to open files it has a name but no path for.
  @author Alan Ezust
  @since jEdit 5.0pre1
 */

public class FileOpener extends org.jedit.core.FileOpenerService {
	public void openFile(String fileName, View view) {
		FastOpen fo = new FastOpen(view, fileName);
		fo.showWindow();
	}
}
