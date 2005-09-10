package com.patelsoft.fastopen;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.ColorWellButton;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.util.*;


public class FastOpenPlugin extends EditPlugin
{
	public final static String NAME = "fastopen";
	private static Hashtable viewsToFastOpen = new Hashtable(5);

	public final static String OPEN_FILES_FIRST = "OPEN_FILES_FIRST";
	public final static String OPEN_FILES_LAST ="OPEN_FILES_LAST";
	public final static String OPEN_FILES_NOPREF =null;
	static WindowAdapter wa;

	public void start()
	{
		wa = new FastOpenWindowAdapter();
		super.start();
	}

	public void stop()
	{
		Enumeration iter = viewsToFastOpen.keys();
		while(iter.hasMoreElements())
		{
			View v = (View)iter.nextElement();
			v.removeWindowListener(wa);
		} 
		super.stop();
	}

	public static FastOpen getFastOpenInstance(View view)
	{
		FastOpen fopen = (FastOpen)viewsToFastOpen.get(view);
		if (fopen == null)
		{
			fopen = new FastOpen(view);
			viewsToFastOpen.put(view,fopen);
			view.addWindowListener(wa);
		}
		return fopen;
	}

	/* public void createMenuItems(Vector menuItems)
{
		menuItems.addElement(GUIUtilities.loadMenuItem("fastopen.show"));
}

	public void createOptionPanes(OptionsDialog dialog)
{
		dialog.addOptionPane(new FastOpenOptionPane());
} */


	/* public void handleMessage(EBMessage ebmess)
	   {
	    if (ebmess instanceof ViewUpdate)
	    {
	        ViewUpdate vu = (ViewUpdate)ebmess;
	        if (((ViewUpdate)ebmess).getWhat() == ViewUpdate.CLOSED)
	        {
	            FastOpen fopen =(FastOpen)viewsToFastOpen.remove(vu.getView());
	            if(fopen != null)
	            {
	                fopen.killWindow();
	                System.gc();
	            }
	        }
	        else if (((ViewUpdate)ebmess).getWhat() == ViewUpdate.CREATED)
	        {
	            getFastOpenInstance(vu.getView());
	        }
	    }
	      }
	*/

	public static class FastOpenOptionPane extends AbstractOptionPane
	{
		JCheckBox chkSort,chkDontShowOpenFiles,chkIgnoreCase,chkPattFromSelectedText, chkShowRecentFiles;
		JRadioButton radioOpenFilesFirst,radioOpenFilesLast,radioNoPref;
		ButtonGroup bg = new ButtonGroup();
		ColorWellButton btnOpenFilesColor, btnNonPrjFilesColor;

		public FastOpenOptionPane()
		{
			super(NAME);
		}

		protected void _init()
		{
			chkSort = new JCheckBox("Sort Files");
			chkDontShowOpenFiles = new JCheckBox("Don't show Open files");
			chkIgnoreCase = new JCheckBox("Ignore Case in Search",true);
			chkPattFromSelectedText = new JCheckBox("Pattern from Selected text",true);
			chkShowRecentFiles = new JCheckBox("Search Recent Files",true);

			radioOpenFilesFirst = new JRadioButton("Show open files First");
			radioOpenFilesLast = new JRadioButton("Show open files Last");
			radioNoPref = new JRadioButton("No Preference",true);

			radioOpenFilesFirst.setActionCommand(FastOpenPlugin.OPEN_FILES_FIRST);
			radioOpenFilesLast.setActionCommand(FastOpenPlugin.OPEN_FILES_LAST);
			radioNoPref.setActionCommand(FastOpenPlugin.OPEN_FILES_NOPREF);


			bg.add(radioOpenFilesFirst);
			bg.add(radioOpenFilesLast);
			bg.add(radioNoPref);

			JPanel panel = new JPanel(new GridLayout(4,1));
			JPanel panelChk = new JPanel(new GridLayout(0,4));
			JPanel panelColors = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JPanel pnlRadios = new JPanel(new FlowLayout(FlowLayout.LEFT));

			panelChk.add(chkSort);
			panelChk.add(chkDontShowOpenFiles);
			panelChk.add(chkIgnoreCase);
			panelChk.add(chkPattFromSelectedText);
			panelChk.add(chkShowRecentFiles);

			panel.add(panelChk);

			pnlRadios.add(radioOpenFilesFirst);
			pnlRadios.add(radioOpenFilesLast);
			pnlRadios.add(radioNoPref);

			panel.add(pnlRadios);

			//Setup Color Component
			/* FastOpenColorRenderer forend = new FastOpenColorRenderer();
				forend.addColor("FastOpen.openFiles.foregroundcolor","Open Files Color",FastOpen.openFilesForeground);
				forend.addColor("FastOpen.nonprjOpenFiles.foregroundcolor","Non-Project files Color",FastOpen.nonprjopenFilesForeground); */

			btnOpenFilesColor = new ColorWellButton(FastOpen.openFilesForeground);
			panelColors.add(new JLabel("Open files Foreground :"));
			panelColors.add(btnOpenFilesColor);
			btnNonPrjFilesColor = new ColorWellButton(FastOpen.nonprjopenFilesForeground);
			panelColors.add(new JLabel("Non-Project files Foreground :"));
			panelColors.add(btnNonPrjFilesColor);

			panel.add(panelColors);
			addComponent(panel);
		}

		public void init()
		{
			super.init();
			chkSort.setSelected(jEdit.getBooleanProperty("fastopen.sortFiles"));
			chkDontShowOpenFiles.setSelected(jEdit.getBooleanProperty("fastopen.hideOpenFiles"));
			if(jEdit.getProperty("fastopen.ignorecase") == null)
			{
				jEdit.setBooleanProperty("fastopen.ignorecase", true);
			}
			chkIgnoreCase.setSelected(jEdit.getBooleanProperty("fastopen.ignorecase"));
			chkPattFromSelectedText.setSelected(jEdit.getBooleanProperty("fastopen.patternFromSelectedText"));
			chkShowRecentFiles.setSelected(jEdit.getBooleanProperty("fastopen.showrecentfiles", true));
			Enumeration enumElements =bg.getElements();
			while(enumElements.hasMoreElements())
			{
				AbstractButton btn  = (AbstractButton)enumElements.nextElement();
				if (btn.getActionCommand().equals(jEdit.getProperty("fastopen.filesOrder")))
				{
					btn.setSelected(true);
				}
			}
		}

		public void save()
		{
			if(chkSort != null) //Hack becoz jedit calls init only if the plugin option is selected but calls save irrespective of option selection. Funny but true.
			{
				jEdit.setBooleanProperty("fastopen.sortFiles",chkSort.isSelected());
				jEdit.setBooleanProperty("fastopen.hideOpenFiles",chkDontShowOpenFiles.isSelected());
				jEdit.setBooleanProperty("fastopen.ignorecase",chkIgnoreCase.isSelected());
				jEdit.setBooleanProperty("fastopen.patternFromSelectedText",chkPattFromSelectedText.isSelected());
				jEdit.setBooleanProperty("fastopen.showrecentfiles",chkShowRecentFiles.isSelected());
				jEdit.setProperty("fastopen.filesOrder",bg.getSelection().getActionCommand());
				jEdit.setColorProperty("fastopen.openFiles.foregroundcolor",btnOpenFilesColor.getSelectedColor());
				jEdit.setColorProperty("fastopen.nonprjOpenFiles.foregroundcolor",btnNonPrjFilesColor.getSelectedColor());
				FastOpen.openFilesForeground = btnOpenFilesColor.getSelectedColor();
				FastOpen.nonprjopenFilesForeground = btnNonPrjFilesColor.getSelectedColor();
			}
		}//End of save
	} //End of FastOpenOptionPane

	class FastOpenWindowAdapter extends WindowAdapter
	{
		//We add view listener to prevent FO to extend from EBPlugin
		public void windowClosed(WindowEvent evt)
		{
			//Log.log(Log.DEBUG,this.getClass(),"Removing FO instance on View close");
			FastOpen hashFopen =(FastOpen)viewsToFastOpen.remove(evt.getWindow());
			if(hashFopen != null)
			{
				hashFopen.killWindow();
				hashFopen = null;
				System.gc();
			}
			else
			{
				Log.log(Log.ERROR,this.getClass(),"Got no view for View close?? How come??");
			}
		}
	}
}//End of class FastOpenPlugin
