package com.patelsoft.fastopen;

import org.gjt.sp.jedit.gui.HistoryModel;
import org.gjt.sp.jedit.gui.HistoryTextField;
import java.awt.event.KeyEvent;

public class FastOpenTextField extends HistoryTextField
{
	/**
	 * Creates a new history text field.
	 * @param name The history model name
	 * @param instantPopups If true, selecting a value from the history
	 * popup will immediately fire an ActionEvent. If false, the user
	 * will have to press 'Enter' first
	 * @param enterAddsToHistory If true, pressing the Enter key will
	 * automatically add the currently entered text to the history.
	 *
	 */
	public FastOpenTextField(String name, boolean instantPopups, boolean enterAddsToHistory)
	{
		super(name,instantPopups, enterAddsToHistory);
		HistoryModel model = getModel();
		int s = model.size();
		
		setSelectAllOnFocus();
		String lastEntry = model.get(0).toString();
		setText(lastEntry);
	}

}

