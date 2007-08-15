package com.patelsoft.fastopen;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.ColorWellButton;
import org.gjt.sp.util.Log;


public class FastOpenPlugin extends EditPlugin
{
	public final static String NAME = "fastopen";
	static Hashtable viewsToFastOpen = new Hashtable(5);

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
		viewsToFastOpen.clear();
		super.stop();
	}

	/**
	 * @param view The View we activated FastOpen from.
	 * @return A lightweight JPanel wrapper around the FastOpen instance for this View.
	 */
	public static FastOpen getFastOpenInstance(View view)
	{
		/*DockableWindowManager dwm = view.getDockableWindowManager();
		FastOpen fopen = null;
		if (dwm != null) {
			fopen = (FastOpen) dwm.getDockable("fastopen");
		}
		if (fopen == null)
		{
			fopen = new FastOpen(view);
			viewsToFastOpen.put(view,fopen);
			view.addWindowListener(wa);
		}*/
		
		/*The above code should not be used. It creates multiple FastOpen objects per invocation of FO window. The correct way is to lookup viewsToFastOpen map and use it as done below.*/
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
		JSlider indexingFreq;
		JSpinner txtdelay;

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

			txtdelay = new JSpinner(new SpinnerNumberModel(jEdit.getDoubleProperty("fastopen.search.delay",2), 0.5,10,0.5));

			indexingFreq = new JSlider(5,300);//Seconds. 5 sec. to 5 min. anything above this would be useless.
			indexingFreq.setPaintLabels(true);
			indexingFreq.setPaintLabels(true);
			Dictionary tables = new Hashtable();
			tables.put(new Integer(5),new JLabel("5 sec."));
			tables.put(new Integer(60),new JLabel("1 min."));
			tables.put(new Integer(120),new JLabel("2 min."));
			tables.put(new Integer(180),new JLabel("3 min."));
			tables.put(new Integer(240),new JLabel("4 min."));
			tables.put(new Integer(300),new JLabel("5 min."));

			indexingFreq.setLabelTable(tables);

			final JLabel freqVal = new JLabel(indexingFreq.getValue() +"");
			// Register a change listener
			indexingFreq.addChangeListener(new ChangeListener(){
				// This method is called whenever the slider's value is changed
				public void stateChanged(ChangeEvent evt)
				{
					JSlider slider = (JSlider)evt.getSource();
					if (!slider.getValueIsAdjusting())
					{
						// Get new value
						freqVal.setText(slider.getValue()+" sec.");
					}
				}
			});

			radioOpenFilesFirst.setActionCommand(FastOpenPlugin.OPEN_FILES_FIRST);
			radioOpenFilesLast.setActionCommand(FastOpenPlugin.OPEN_FILES_LAST);
			radioNoPref.setActionCommand(FastOpenPlugin.OPEN_FILES_NOPREF);


			bg.add(radioOpenFilesFirst);
			bg.add(radioOpenFilesLast);
			bg.add(radioNoPref);

			JPanel panel = new JPanel(new GridLayout(5,1));
			JPanel panelChk = new JPanel(new GridLayout(0,3));
			JPanel panelColors = new JPanel(new FlowLayout(FlowLayout.LEFT));
			//final JPanel pnlRadios = new JPanel(new FlowLayout(FlowLayout.LEFT));
			final JPanel pnlRadios = new JPanel(new GridLayout(0,3));
			pnlRadios.setBorder(new EtchedBorder());
			JPanel pnlslider = new JPanel(new BorderLayout());

			JPanel pnldelay = new JPanel();
			((FlowLayout)pnldelay.getLayout()).setAlignment(FlowLayout.LEFT);
			JLabel lbldelay = new JLabel("Delay before searching (in seconds)");
			pnldelay.add(lbldelay);
			pnldelay.add(txtdelay);


			panelChk.add(chkDontShowOpenFiles);
			panelChk.add(chkIgnoreCase);
			panelChk.add(chkPattFromSelectedText);
			panelChk.add(chkShowRecentFiles);
			panelChk.add(chkSort);
			panel.add(panelChk);

			chkSort.addItemListener(new ItemListener()
				{
					public void itemStateChanged(ItemEvent e)
					{
						if(e.getStateChange() == ItemEvent.SELECTED)
						{
							enableDisableChildren(pnlRadios, true);
						}
						else
						{
							enableDisableChildren(pnlRadios, false);
						}
					}
				}
			);
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


			pnlslider.add(new JLabel("Indexing frequency "), BorderLayout.WEST);
			pnlslider.add(indexingFreq, BorderLayout.CENTER);
			pnlslider.add(freqVal, BorderLayout.EAST);

			panel.add(panelColors);
			panel.add(pnlslider);
			panel.add(pnldelay);
			addComponent(panel);
		}

		public void init()
		{
			System.out.println("Init called");
			super.init();
			chkSort.setSelected(jEdit.getBooleanProperty("fastopen.sortFiles"));

			enableDisableChildren(radioOpenFilesFirst.getParent(), jEdit.getBooleanProperty("fastopen.sortFiles"));

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

			indexingFreq.setValue(jEdit.getIntegerProperty("fastopen.indexing.freq",10));//10 sec. default.

			txtdelay.setValue(new Double(jEdit.getDoubleProperty("fastopen.search.delay",2)));//1 sec. default.
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
				jEdit.setIntegerProperty("fastopen.indexing.freq",indexingFreq.getValue());

				double delay = 2.0;

				if(txtdelay.getValue() instanceof Double)
				{
					delay = ((Double)txtdelay.getValue()).doubleValue();
				}
				else if(txtdelay.getValue() instanceof Integer)
				{
					delay = ((Integer)txtdelay.getValue()).doubleValue();
				}


				jEdit.setDoubleProperty("fastopen.search.delay",delay);
				FastOpen.openFilesForeground = btnOpenFilesColor.getSelectedColor();
				FastOpen.nonprjopenFilesForeground = btnNonPrjFilesColor.getSelectedColor();
			}

		}//End of save


		// enable (or disable) all children of a component
		void enableDisableChildren(Container container, boolean isEnabled)
		{
			// get an array of all the components in this container
			Component[] components = container.getComponents();
			// for each element in the container enable/disable it
			for (int i = 0; i < components.length; i++)
			{
				components[i].setEnabled(isEnabled);
			}
		}
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
