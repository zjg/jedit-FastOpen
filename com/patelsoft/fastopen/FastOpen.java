package com.patelsoft.fastopen;

import java.awt.event.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.*;
import gnu.regexp.*;
import java.awt.Color;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.BorderLayout;


public class FastOpen extends JPanel implements ActionListener
{
	private final String TITLE = "Fast Open " + jEdit.getProperty("plugin.com.patelsoft.fastopen.FastOpenPlugin.version");
	View view;
	JTextField txtfilename;
	JList  jlist;
	List _foundfileslist;
	JDialog mainWindow = null;
	JScrollPane scroller = null;
	JComboBox projectCombo;
	ProjectSwitchListener vListener;
	Comparator comparator = new FastOpenComparator();
	public static Color openFilesForeground = jEdit.getColorProperty("fastopen.openFiles.foregroundcolor", Color.black);
	public static Color nonprjopenFilesForeground = jEdit.getColorProperty("fastopen.nonprjOpenFiles.foregroundcolor", Color.green.darker());
	final RE reLineNo = new UncheckedRE("(.*):([0-9]+)");
	Files files = new Files(this);
	Set allfiles = new HashSet(20,1.0f);
	private final String noWordSep;



	/**
	 *  Constructor for the FastOpen object
	 *
	 * @param  view  Description of the Parameter
	 */
	public FastOpen(View view)
	{
		this.view = view;
		noWordSep = view.getBuffer().getProperty("noWordSep") + ".:-" + File.separator;
		setupFastOpen();
	}//End of FastOpen constructor

	public void fireMouseEvent(final boolean later) {
		Runnable r = new Runnable() {
			public void run() {
				if (later) try {Thread.sleep(1000); } catch (InterruptedException ie) {}
				MouseEvent me = new MouseEvent(view, MouseEvent.MOUSE_CLICKED, 
					0, 0, 0, 0, 1, false, MouseEvent.BUTTON1);
				view.getTextArea().getPainter().dispatchEvent(me);
			}
		};
		if (later) {
			Thread t = new Thread(r);
			t.start();
		}
		else {
			r.run();
		}
	}

	/**  Shows the Main FastOpen window  */
	public void showWindow()
	{
		if(mainWindow != null)
		{
			/* Simulates a mouse click, so that plugins like navigator remember
			 where we were when we did fastopen and can bring us back */
			fireMouseEvent(false);
			/*
			  if(files.getCurrentProject(view) == null)
			   {
				JOptionPane.showMessageDialog(view, "No Project currently Active.Please select some project from the ProjectViewer(except \"All Projects\") to get the FastOpen window", TITLE, JOptionPane.INFORMATION_MESSAGE);
				return;
			      }
			   */
			if(jEdit.getBooleanProperty("fastopen.patternFromSelectedText"))
			{
				String txtSelection = getFileAtCaret();
				int lno = -1;
				List vecContent = parseFileLnoPattern(txtSelection);

				if(vecContent != null)
				{
					//txtSelection = (String)vecContent.get(0);
					lno =((Integer)vecContent.get(1)).intValue();
				}


				List matchingfiles = retrieveMatchingFiles(txtSelection,view);

				if(matchingfiles.size() == 1)
				{
					//Only one matching file so why show the mainWindow
					openFile((FastOpenFile)matchingfiles.iterator().next());
					
					if(lno != -1)
					{
						gotoLine(lno);
					}
					return;
				}

				txtfilename.setText((txtSelection == null?null:txtSelection.trim()));
			}

			GUIUtilities.loadGeometry(mainWindow, "fastopen.window");

			//if(projectCombo.getItemCount() == 0)// Prevents loading of projects in project combo
			//{
			if(files.isPVThere())
			{
				loadProjectsInCombo();
			}
			//}

			txtfilename.selectAll();
			mainWindow.setVisible(true);
		}
		else
		{
			// log that mainWindow is null.
			Log.log(Log.ERROR, this.getClass(), "MainWindow of FastOpen is null???. Who did this??");
		}
	}


	/**  Closes the FastOpen window */
	public void closeMainWindow()
	{
		if(mainWindow != null)
		{
			allfiles.clear();
			mainWindow.setVisible(false);
		}
		else
		{
			Log.log(Log.ERROR, this.getClass(), "Main Window is null. Who did this?? Can't close mainWindow");
		}
	}


	/**  Description of the Method */
	private void setupFastOpen()
	{
		setLayout(new BorderLayout());
		//txtfilename = new FastOpenTextField(20);
		txtfilename = new FastOpenTextField("fastopen.patterns", false, true);
		this.setNextFocusableComponent(txtfilename);
		txtfilename.addActionListener(this);
		txtfilename.getDocument().addDocumentListener(
			new DocumentListener()
			{
				public void insertUpdate(DocumentEvent e)
				{
					findfile();
				}


				public void changedUpdate(DocumentEvent e)
				{
					findfile();
				}


				public void removeUpdate(DocumentEvent e)
				{
					try
					{
						if(e.getDocument().getText(0, e.getDocument().getLength()).trim().length() == 0)
						{
							jlist.setListData(new String[0]);
						}

						else
						{
							findfile();
						}
					}

					catch(BadLocationException badle)
					{}
				}

			}

		);
		Action down_Action =
			new AbstractAction("DownArrow")
			{
				public void actionPerformed(ActionEvent e)
				{
					moveListDown();
				}
			};

		Action up_Action =
			new AbstractAction("UpArrow")
			{
				public void actionPerformed(ActionEvent e)
				{
					moveListUp();
				}
			};

		//Below steps are used to receive notifications of the KeyStrokes we are interested in unlike ActionListener/KeyListener
		//which is fired irrespective of the kind of KeyStroke.
		InputMap inputMap = txtfilename.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		KeyStroke up_arrow = KeyStroke.getKeyStroke("UP");
		KeyStroke down_arrow = KeyStroke.getKeyStroke("DOWN");

		inputMap.put(up_arrow, up_Action.getValue(Action.NAME));
		inputMap.put(down_arrow, down_Action.getValue(Action.NAME));

		ActionMap actionMap = txtfilename.getActionMap();
		actionMap.put(up_Action.getValue(Action.NAME), up_Action);
		actionMap.put(down_Action.getValue(Action.NAME), down_Action);

		JPanel pnlNorth = new JPanel(new BorderLayout());

		pnlNorth.add(txtfilename, BorderLayout.CENTER);

		if(files.isPVThere())
		{
			vListener = new ProjectSwitchListener();
			projectCombo = new JComboBox();
			projectCombo.addItemListener(vListener);
			loadProjectsInCombo();//For dockables which does not call showWindow.
			pnlNorth.add(projectCombo, BorderLayout.EAST);
		}

		this.add(pnlNorth, BorderLayout.NORTH);
		jlist = new JList();
		jlist.setCellRenderer(new FastOpenRenderer());
		jlist.addKeyListener(new ListHandler());
		jlist.addMouseListener(new ListHandler());
		scroller = new JScrollPane(jlist);
		this.add(scroller, BorderLayout.CENTER);

		//createMainWindow
		mainWindow = new JDialog(view, this.TITLE, false);
		mainWindow.setSize(554, 182);//Default size for new FastOpen installation.
		mainWindow.addNotify();
		mainWindow.setDefaultCloseOperation(mainWindow.HIDE_ON_CLOSE);
		mainWindow.getContentPane().add(this, BorderLayout.CENTER);
		mainWindow.addComponentListener(
			new ComponentAdapter()
			{
				public void componentResized(ComponentEvent e)
				{
					GUIUtilities.saveGeometry(mainWindow, "fastopen.window");
				}


				public void componentMoved(ComponentEvent e)
				{
					GUIUtilities.saveGeometry(mainWindow, "fastopen.window");
				}
			}

		);
		ActionListener escapeAction =
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					closeMainWindow();
				}
			};

		KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		mainWindow.getRootPane().registerKeyboardAction(escapeAction, escapeKeyStroke, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}//End of setupFastOpen


	/**  Description of the Method */
	private void moveListDown()
	{
		int selectedIndex = jlist.getSelectedIndex();
		int listSize = jlist.getModel().getSize();

		if(listSize > 1 && selectedIndex >= 0 && (selectedIndex + 1) < listSize)
		{
			selectedIndex++;
			jlist.setSelectedIndex(selectedIndex);
			jlist.ensureIndexIsVisible(selectedIndex);
		}
	}


	/**  Description of the Method */
	private void moveListUp()
	{
		int selectedIndex = jlist.getSelectedIndex();
		int listSize = jlist.getModel().getSize();

		if(listSize > 1 && (selectedIndex - 1) >= 0)
		{
			selectedIndex--;
			jlist.setSelectedIndex(selectedIndex);
			jlist.ensureIndexIsVisible(selectedIndex);
		}
	}


	/**  Description of the Method */
	void killWindow()
	{
		//        System.out.println("Killing Main Window");
		if(mainWindow != null)
		{
			mainWindow.dispose();
			mainWindow = null;
		}

		//pv = null;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void actionPerformed(ActionEvent evt)
	{
		//For enter keys pressed inside txtfilename

		int selectedIndex = jlist.getSelectedIndex();
		int listSize = jlist.getModel().getSize();

		if(selectedIndex != -1 && listSize != 0 && selectedIndex < listSize)
		{
			List vec = parseFileLnoPattern(getFilePattern());

			if(vec != null)
			{
				openFile(jlist.getSelectedIndex(),((Integer)vec.get(1)).intValue());
			}

			else
			{
				openFile(jlist.getSelectedIndex());
			}

			closeMainWindow();
		}
	}




	/**  Description of the Method */
	private void findfile()
	{
		String txtToFind = getFilePattern();

		if(txtToFind != null)
		{
			//	Log.log(Log.DEBUG, this.getClass(), "Trying to retrieveFiles with text " + txtToFind);
			List foundfileslist = retrieveMatchingFiles(txtToFind,view);
			if(foundfileslist.size() > 0)
			{
				if(txtfilename.getForeground() == Color.red)
				{
					txtfilename.setForeground(jEdit.getColorProperty("view.fgColor", Color.black));
				}

				if(_foundfileslist != null)
				{
					_foundfileslist.clear();
				}
				_foundfileslist = foundfileslist;
				foundfileslist = null;

				jlist.setListData(extractPathFromPrjDir(_foundfileslist));
				//    this.add(scroller, BorderLayout.CENTER);
				jlist.setSelectedIndex(0);
			}
			else
			{
				//Log.log(Log.NOTICE, FastOpen.class, "No matching file found.");
				jlist.setListData(new String[0]);
				txtfilename.setForeground(Color.red);
				foundfileslist.clear();
				//_foundfileslist.trimToSize();
			}
		}

	}


	/**
	 *  Description of the Method
	 *
	 * @param  event  Description of the Parameter
	 */
	private void onefileselection(AWTEvent event)
	{
		openFile(((JList)event.getSource()).getSelectedIndex());
	}


	/**
	 *  Description of the Method
	 *
	 * @param  matchingfileindex  Description of the Parameter
	 * @return                    Description of the Return Value
	 */
	private Buffer openFile(int matchingfileindex)
	{
		return openFile((FastOpenFile)_foundfileslist.get(matchingfileindex));
		
	}

	private Buffer openFile(int matchingfileindex, int lineNo)
	{
		Buffer buf =openFile((FastOpenFile)_foundfileslist.get(matchingfileindex));
		gotoLine(lineNo);
		return buf;
	}

	private String getFilePattern()
	{
		try
		{
			return txtfilename.getDocument().getText(0, txtfilename.getDocument().getLength());
		}

		catch(BadLocationException e)
		{
			Log.log(Log.DEBUG, this.getClass(), "Caught BadLocationException. Returning.");
		}

		return null;
	}

	private void gotoLine(final int lineNo)
	{
		VFSManager.runInAWTThread(
			new Runnable()
			{
				public void run()
				{
					JEditTextArea txtArea = view.getTextArea();

					if(lineNo <= txtArea.getLineCount())
					{
						txtArea.setCaretPosition(txtArea.getLineStartOffset(lineNo - 1));
					}

					else
					{
						Log.log(Log.DEBUG, this.getClass(), "Ignoring linecounbt " + lineNo);
					}
				}
			}

		);
	}

	/**
	 *  Description of the Method
	 *
	 * @param  pf  Description of the Parameter
	 * @return     Description of the Return Value
	 */
	public Buffer openFile(FastOpenFile pf)
	{
		Buffer b = pf.open(this.view);
		// FIXME: for some reason this event never reaches Navigator.
		fireMouseEvent(true);
		return b;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  foundfileslist  Description of the Parameter
	 * @return                 Description of the Return Value
	 */
	private String[] extractPathFromPrjDir(List foundfileslist)
	{
		String paths[] = new String[foundfileslist.size()];

		Iterator iter = foundfileslist.iterator();
		for(int i=0;iter.hasNext();i++)
		{
			FastOpenFile pf = (FastOpenFile)iter.next();
			paths[i] = getDecoratedPath(pf);
		}

		return paths;
	}


	/**
	 *  Gets the decoratedPath attribute of the FastOpen object
	 *
	 * @param  pf  Description of the Parameter
	 * @return     The decoratedPath value
	 */
	public String getDecoratedPath(FastOpenFile pf)
	{
		if(pf == null)
		{
			return null;
		}

		return pf.getName() + " (" + pf.getPath() + ")";
	}


	/**
	 *  Gets the fileAtCaret attribute of the FastOpen object
	 *
	 * @return    The fileAtCaret value
	 */
	private String getFileAtCaret()
	{
		JEditTextArea textArea = view.getTextArea();

		if(textArea.getSelectionCount() > 0)
		{
			return textArea.getSelectedText(textArea.getSelection()[0]);
		}

		else
		{
			int line = textArea.getCaretLine();
			int lineLength = textArea.getLineLength(line);

			if(lineLength == 0)
			{
				return null;
			}

			String lineText = textArea.getLineText(line);
			int lineStart = textArea.getLineStartOffset(line);
			int offset = textArea.getCaretPosition() - lineStart;

			if(offset == lineLength)
			{
				//--offset;
				return null;
			}

			int wordStart = TextUtilities.findWordStart(lineText, offset, noWordSep);
			int wordEnd = TextUtilities.findWordEnd(lineText, offset + 1, noWordSep);
			return textArea.getText(lineStart + wordStart, wordEnd - wordStart);
		}
	}

	/**
	 *  Description of the Method
	 *
	 * @param  txtSelection  Description of the Parameter
	 * @param  lno           Description of the Parameter
	 */
	List parseFileLnoPattern(String txtSelection)
	{
		if((txtSelection != null && txtSelection.trim().length() != 0) && reLineNo.isMatch(txtSelection))
		{
			REMatch match = reLineNo.getMatch(txtSelection);

			List vecReturn = new ArrayList(2);
			vecReturn.add(0,match.toString(1));
			vecReturn.add(1,new Integer(match.toString(2)));
			return vecReturn;
		}

		return null;
	}

	/**  Description of the Method */
	private void loadProjectsInCombo()
	{
		vListener.pause();

		if(projectCombo.getItemCount() != 0)
		{
			projectCombo.removeAllItems();
		}
		Iterator iter = projectviewer.ProjectManager.getInstance().getProjects();
		int currProjectIdx = 0;
		projectviewer.vpt.VPTProject currPrj = (projectviewer.vpt.VPTProject)files.getCurrentProject(view);

		for(int i = 1; iter.hasNext(); i++)
		{
			projectviewer.vpt.VPTProject nextItem = (projectviewer.vpt.VPTProject)iter.next();
			//			if (!nextItem.getName().equals(pv.ALL_PROJECTS))
			//		{
			projectCombo.addItem(nextItem.getName());
			if(currPrj != null && nextItem.getName().equals(currPrj.getName()))
			{
				currProjectIdx = i;
			}
		}
		projectCombo.setSelectedIndex(currProjectIdx - 1);
		vListener.resume();
	}

	/**
	 *  Description of the Method
	 *
	 * @param  project     Description of the Parameter
	 * @param  fileToFind  Description of the Parameter
	 * @return             Description of the Return Value
	 */
	/*	public List retrieveMatchingFiles(String fileToFind, View view)
		{
			//Log.log(Log.DEBUG,this.getClass(),"Got fileToFind "+ fileToFind);
			if(fileToFind != null)
			{
				List foundfileslist = new ArrayList();
				//System.out.println( "Trying to match inside retrieveMatchingFiles " + fileToFind);
				Set allfiles = new TreeSet(comparator);

	////				long start = System.currentTimeMillis();
				Collection dataFiles = files.prjFile2FOFile(view);
				if(dataFiles != null)
				{
					allfiles.addAll(dataFiles);
				}

	//				long end = System.currentTimeMillis();
				System.out.println("Time taken to get projects and add " + (end-start) + " ms");

	//				start = System.currentTimeMillis();
				dataFiles =files.diffPrjFilesWithOpenBuffers(jEdit.getBuffers(),view);
				if(dataFiles != null)
				{
					allfiles.addAll(dataFiles);
				}
	//				end = System.currentTimeMillis();
				System.out.println("Time taken to diff projectfile with open buffers and add " + (end-start) + " ms");


	//				start = System.currentTimeMillis();
				if(jEdit.getBooleanProperty("fastopen.showrecentfiles"))
				{
					dataFiles =getRecentFiles();
					if(dataFiles != null)
					{
						allfiles.addAll(dataFiles);
					}
				}
	//				end = System.currentTimeMillis();
				System.out.println("Time taken to get recentfiles and add " + (end-start) + " ms");


				System.out.println("Total files collected " + allfiles.size());
				//Iterator iterPrjFiles = project.getFiles().iterator();
				Iterator iterPrjFiles = allfiles.iterator();

				List vecPattern = parseFileLnoPattern(fileToFind);
				if(vecPattern != null)
				{
					fileToFind = (String)vecPattern.get(0);
				}
				try
				{
					RE re = null;
					if(jEdit.getBooleanProperty("fastopen.ignorecase"))
					{
						re = new RE(MiscUtilities.globToRE("^" + fileToFind), RE.REG_ICASE);
					}
					else
					{
						re = new RE(MiscUtilities.globToRE("^" + fileToFind));
					}

					while(iterPrjFiles.hasNext())
					{
						FastOpenFile file = (FastOpenFile)iterPrjFiles.next();
						if(jEdit.getBooleanProperty("fastopen.hideOpenFiles"))
						{
							if(file.isOpened())
							{
								continue;
							}
						}

						if(re.getMatch(file.getName()) != null)
						{
							foundfileslist.add(file);
						}
					}
					//End of while
					iterPrjFiles = null;

					//if(jEdit.getBooleanProperty("fastopen.sortFiles"))
					//{
					//	Collections.sort(foundfileslist, comparator);
					//}
				}
				catch(REException e)
				{
					txtfilename.setForeground(Color.red);
					return foundfileslist;
				}

				return foundfileslist;
			}
			else
			{
				return new ArrayList(0);
			}
		}
	 */


	public List retrieveMatchingFiles(String fileToFind, View view)
	{

		//We try to collect files just once and hold on to them until the user closes FO. This reduces Object creation and hopefully increases performance.

		//Log.log(Log.DEBUG,this.getClass(),"Got fileToFind "+ fileToFind);
		//long start,end;
		if(fileToFind != null)
		{
			if(allfiles.size() == 0)
			{
				//start = System.currentTimeMillis();
				files.prjFile2FOFile(view,allfiles);

				//end = System.currentTimeMillis();
				//System.out.println("Time taken to get projects and add " + (end-start) + " ms but before adding to addFiles");

				//long end2 = System.currentTimeMillis();
				//System.out.println("Time taken to get projects and add " + (end2-end) + " ms total time is " + (end2-start));

				//start = System.currentTimeMillis();
				files.diffPrjFilesWithOpenBuffers(jEdit.getBuffers(),view, allfiles);

				//end = System.currentTimeMillis();
				//System.out.println("Time taken to diff projectfile with open buffers and add " + (end-start) + " ms");

				//start = System.currentTimeMillis();

				if(jEdit.getBooleanProperty("fastopen.showrecentfiles"))
				{
					getRecentFiles(allfiles);
				}

				//end = System.currentTimeMillis();
				//System.out.println("Time taken to get recentfiles and add " + (end-start) + " ms");
				//System.out.println("Total files collected " + allfiles.size());
			}
			//System.out.println("Completed allfiles for filetoFind " + fileToFind + " " + allfiles);


			List vecPattern = parseFileLnoPattern(fileToFind);

			if(vecPattern != null)
			{
				fileToFind = (String)vecPattern.get(0);
			}

			List foundfileslist = new ArrayList(allfiles.size()); //Initializating Collections is a Performance Optimization since the need for expansion is done away. Setting foundfileslist's size to max allfiles.size() since thats the max it can go(in case of say regexp '*') anyways. unused elements are as it  is NULLs.
			try
			{
				RE re = null;

				if(jEdit.getBooleanProperty("fastopen.ignorecase"))
				{
					re = new RE(MiscUtilities.globToRE("^" + fileToFind), RE.REG_ICASE);
				}
				else
				{
					re = new RE(MiscUtilities.globToRE("^" + fileToFind));
				}

				//start = System.currentTimeMillis();

				final boolean hideOpenFiles = jEdit.getBooleanProperty("fastopen.hideOpenFiles"); //Moved outside the while loop to improve performance instead of repeated seaching in jEdit's hash properties when it is not going to change between call.
				Iterator iterPrjFiles = allfiles.iterator();
				while(iterPrjFiles.hasNext())
				{
					FastOpenFile file = (FastOpenFile)iterPrjFiles.next();

					if(hideOpenFiles)
					{
						if(file.isOpened())
						{
							continue;
						}
					}

					if(re.getMatch(file.getName()) != null)
					{
						//System.out.println("Duplicate file exists for file "+ file + " "+ (foundfileslist.contains(file)));
						foundfileslist.add(file);
					}
				}//End of while

				//System.out.println("Final foundfileslist "+ foundfileslist);
				//end = System.currentTimeMillis();

				//System.out.println("Time taken to collect matching files " + (end-start) + " ms");

				iterPrjFiles = null;

				//start = System.currentTimeMillis();

				if(jEdit.getBooleanProperty("fastopen.sortFiles"))
				{
					Collections.sort(foundfileslist, comparator);
				}
				//end = System.currentTimeMillis();
				//System.out.println("Time taken to sort found files " + (end-start) + " ms");
			}
			catch(REException e)
			{
				txtfilename.setForeground(Color.red);
				return null;
			}
			return foundfileslist;
		}
		else
		{
			return new ArrayList(0);
		}
	}



	private void getRecentFiles(Collection allFiles)
	{
		files.bufferEntry2FastOpenFile(BufferHistory.getHistory(), allFiles);
	}




	//Inner Classes start
	/**
	 *  Description of the Class
	 *
	 * @author     jiger
	 * @created    February 24, 2003
	 */

	class ListHandler extends KeyAdapter implements MouseListener
	{
		/**
		 *  Description of the Method
		 *
		 * @param  event  Description of the Parameter
		 */
		public void keyPressed(KeyEvent event)
		{
			if(event.getKeyCode() == event.VK_ENTER)
			{
				handleListEvents(event);
			}
		}


		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void mouseClicked(MouseEvent e)
		{
			if(e.getClickCount() == 2)
			{
				handleListEvents(e);
			}
		}


		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void mouseEntered(MouseEvent e)
		{}


		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void mouseExited(MouseEvent e)
		{}


		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void mousePressed(MouseEvent e)
		{}


		/**
		 *  Description of the Method
		 *
		 * @param  e  Description of the Parameter
		 */
		public void mouseReleased(MouseEvent e)
		{}


		/**
		 *  Description of the Method
		 *
		 * @param  event  Description of the Parameter
		 */
		private void handleListEvents(AWTEvent event)
		{
			txtfilename.grabFocus();
			txtfilename.selectAll();
			onefileselection(event);
			closeMainWindow();
		}
	}//End of class ListHandler


	/**
	 *  Description of the Class
	 *
	 * @author     jiger
	 * @created    February 24, 2003
	 */

	class ProjectSwitchListener implements ItemListener
	{
		private boolean paused;


		/**  Description of the Method */
		public void pause()
		{
			paused = true;
		}


		/**  Description of the Method */
		public void resume()
		{
			paused = false;
		}



		/**
		 *  Description of the Method
		 *
		 * @param  evt  Description of the Parameter
		 */
		public void itemStateChanged(ItemEvent evt)
		{
			//Log.log(Log.DEBUG,this,"Inside itemStateChanged paused " + paused +" event is ItemEvent.SELECTED "+ (evt.getStateChange() == ItemEvent.SELECTED));
			if(!paused && evt.getStateChange() == ItemEvent.SELECTED)
			{
				String newProject = (String)evt.getItem();
				projectviewer.config.ProjectViewerConfig.getInstance().setLastProject(newProject);
				//Set PV(if there is one) to the new Project.
				projectviewer.ProjectViewer pv = projectviewer.ProjectViewer.getViewer(view);

				if(pv != null)
				{
					pv.setRootNode(projectviewer.ProjectManager.getInstance().getProject(newProject));
					mainWindow.toFront();//when PV is updating itself, FO loses focus. Hopefully this call should work in some jdk/platforms if implemented properly by the platform's jdk.
				}

				if(_foundfileslist != null)
				{
					_foundfileslist.clear();
				}

				jlist.setListData(new String[0]);
				allfiles.clear(); //Very important. Not having this, means that retrieveMatchingFiles will not search the newly selected Project at all!!
				//updateTitle();

				txtfilename.grabFocus();
				txtfilename.selectAll();
			}
		}

	}//End of ProjectSwitchListener


	/**
	 *  FastOpen comparator class to compare two Project files for various stuff
	 *  like already open, etc
	 *
	 * @author     jiger
	 * @created    February 24, 2003
	 */

	class FastOpenComparator implements Comparator
	{
		Comparator collator = java.text.Collator.getInstance();


		/**
		 *  Compares 2 files for sorting purpose.
		 *
		 * @param  obj1  FastOpenFile 1
		 * @param  obj2  FastOpenFile 1
		 * @return       a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
		 */
		public int compare(Object obj1, Object obj2)
		{
			if(obj1 instanceof FastOpenFile && obj2 instanceof FastOpenFile)
			{
				FastOpenFile pf1 = (FastOpenFile)obj1;
				FastOpenFile pf2 = (FastOpenFile)obj2;
				String fileOrder = jEdit.getProperty("fastopen.filesOrder");

				if(fileOrder != null && fileOrder.equals(FastOpenPlugin.OPEN_FILES_FIRST))
				{
					if(pf1.isOpened() && !pf2.isOpened())
					{
						return -1;
					}
					else if(!pf1.isOpened() && pf2.isOpened())
					{
						return 1;
					}
					else
					{
						return collator.compare(getDecoratedPath(pf1).toLowerCase(), getDecoratedPath(pf2).toLowerCase()); //Paths are converted to lowercase for comparision to simulate case-insensivitivity becoz not doing so would lead to showing the same file twice if the path contains mixed case letters. For e.g jEdit Recent files would store path as C:/dir/path_to_file.txt and Projectviewer would as c:\dir/path_to_file.txt (notice the c drive case).
					}
				}
				else if(fileOrder != null && fileOrder.equals(FastOpenPlugin.OPEN_FILES_LAST))
				{
					if(pf1.isOpened() && !pf2.isOpened())
					{
						return 1;
					}

					else if(!pf1.isOpened() && pf2.isOpened())
					{
						return -1;
					}

					else
					{
						return collator.compare(getDecoratedPath(pf1).toLowerCase(), getDecoratedPath(pf2).toLowerCase());
					}
				}
				else
				{
					return collator.compare(getDecoratedPath(pf1).toLowerCase(), getDecoratedPath(pf2).toLowerCase());
				}
			}
			else
			{
				return collator.compare(obj1, obj2);
			}
		}

	}//End of class FastOpenComparator


	/**
	 *  Renderer for Matching File List.
	 *
	 * @author     jiger
	 * @created    February 24, 2003
	 */

	class FastOpenRenderer extends DefaultListCellRenderer
	{
		/**
		 *  Gets the listCellRendererComponent attribute of the FastOpenRenderer
		 *  object
		 *
		 * @param  list          Description of the Parameter
		 * @param  value         Description of the Parameter
		 * @param  index         Description of the Parameter
		 * @param  isSelected    Description of the Parameter
		 * @param  cellHasFocus  Description of the Parameter
		 * @return               The listCellRendererComponent value
		 */
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			FastOpenFile file = (FastOpenFile)_foundfileslist.get(index);

			if(file.isOpened())
			{
				if(file.isProjectFile())
				{
					comp.setForeground(FastOpen.openFilesForeground);
				}

				else
				{//Open, Non-Project file

					comp.setForeground(FastOpen.nonprjopenFilesForeground);
				}

				return comp;
			}

			return comp;
		}

	}//End of class FastOpenRenderer

	//Inner Classes Ends.
}

