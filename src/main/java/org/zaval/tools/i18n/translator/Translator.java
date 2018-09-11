/*
 * Copyright (C) 2001-2002  Zaval Creative Engineering Group (http://www.zaval.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * (version 2) as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.zaval.tools.i18n.translator;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.zaval.awt.AlignConstants;
import org.zaval.awt.BorderedPanel;
import org.zaval.awt.ContextMenu;
import org.zaval.awt.ContextMenuBar;
import org.zaval.awt.EmulatedTextField;
import org.zaval.awt.GraphTree;
import org.zaval.awt.IELabel;
import org.zaval.awt.LevelTree;
import org.zaval.awt.ResizeLayout;
import org.zaval.awt.Resizer;
import org.zaval.awt.ResultField;
import org.zaval.awt.SimpleScrollPanel;
import org.zaval.awt.SpeedButton;
import org.zaval.awt.StatusBar;
import org.zaval.awt.StatusBarElement;
import org.zaval.awt.StatusBarStubbElement;
import org.zaval.awt.TextAlignArea;
import org.zaval.awt.Toolbar;
import org.zaval.awt.ToolkitResolver;
import org.zaval.awt.dialog.EditDialog;
import org.zaval.awt.dialog.MessageBox2;
import org.zaval.awt.peer.TreeNode;
import org.zaval.io.IniFile;
import org.zaval.io.InputIniFile;
import org.zaval.tools.i18n.translator.generated.JavaParser;
import org.zaval.tools.i18n.translator.generated.UtfParser;
import org.zaval.util.SafeResourceBundle;

import javax.swing.JOptionPane;

class Translator extends Frame implements AWTEventListener {
	private MessageBox2 closeDialog;
	private MessageBox2 delDialog;
	private MessageBox2 errDialog;
	private MessageBox2 repDialog;

	private EmulatedTextField keyName;
	private Button keyInsertButton;
	private Button keyDeleteButton;
	private Button dropComment;
	private IELabel keynLab;
	private GraphTree tree;
	private Panel textPanel;
	private List<LangState> langStates = new ArrayList<>();
	private String lastDirectory = ".";

	// Options
	private boolean keepLastDir = true; // Keep last directory
	private boolean omitSpaces = true; // remove spaces in keys
	private boolean autoExpandTF = true; // auto-expand text areas
	private boolean allowDot = true;
	private boolean allowUScore = true;

	private MenuItem newBundleMenu;
	private MenuItem openBundleMenu;
	private MenuItem openBundleMenuP;
	private MenuItem saveBundleMenu;
	private MenuItem saveAsBundleMenu;
	private MenuItem genMenu;
	private MenuItem parseMenu;
	private MenuItem saveXmlBundleMenu;
	private MenuItem saveUtfBundleMenu;
	private MenuItem loadXmlBundleMenu;
	private MenuItem loadUtfBundleMenu;
	private MenuItem saveXmlBundleMenuP;
	private MenuItem saveUtfBundleMenuP;
	private MenuItem loadXmlBundleMenuP;
	private MenuItem loadUtfBundleMenuP;
	private MenuItem loadJarMenu;
	private MenuItem closeMenu;
	private MenuItem exitMenu;
	private MenuItem newLangMenu;
	private Menu langMenu;
	private Menu fileMenu;
	private MenuItem delMenu;
	private MenuItem insMenu;
	private MenuItem renMenu;
	private MenuItem editCopyMenu;
	private MenuItem editCutMenu;
	private MenuItem editPasteMenu;
	private MenuItem editDeleteMenu;
	private MenuItem searchMenu;
	private MenuItem searchAgainMenu;
	private MenuItem replaceToMenu;
	private MenuItem aboutMenu;
	private MenuItem expandTreeMenu;
	private MenuItem collapseTreeMenu;
	private MenuItem expandNodeMenu;
	private MenuItem collapseNodeMenu;
	private CheckboxMenuItem hideTransMenu;
	private MenuItem statisticsMenu;
	private CheckboxMenuItem showNullsMenu;

	// Options
	private CheckboxMenuItem keepLastDirMenu;
	private CheckboxMenuItem omitSpacesMenu;
	private CheckboxMenuItem autoExpandTFMenu;
	private CheckboxMenuItem allowDotMenu;
	private CheckboxMenuItem allowUScoreMenu;

	// Context menus
	private MenuItem ctNewMenu;
	private MenuItem ctNodeExpandMenu;
	private MenuItem ctNodeCollapseMenu;
	private MenuItem ctNodeDeleteMenu;
	private MenuItem ctNodeRenameMenu;

	private EmulatedTextField commField;
	private IELabel sbl1;
	private IELabel sbl2;

	private ToolkitResolver imgres;
	private boolean exitInitiated = true;
	private boolean isDirty;
	private String wasSelectedKey;
	private String SYS_DIR;
	private BundleManager bundle = new BundleManager();
	private final Panel pane = new Panel();
	private Toolbar tool;
	private SimpleScrollPanel scrPanel;

	private final String[] CLOSE_BUTTONS = new String[3];
	private final String[] YESNO_BUTTONS = new String[2];
	private final String[] DELETE_BUTTONS = new String[3];
	private final String[] DELETE_BUTTONS2 = new String[2];
	private final String[] DELETE_BUTTONS3 = new String[2];
	private final String[] REPLACE_BUTTONS = new String[3];

	private MenuItem[] tbar2menu;

	private static final int MAX_PICK_LENGTH = 40;
	private List<String> pickList = new ArrayList<>(8);
	private int nullsCount;
	private int notCompletedCount;

	// search
	private String searchCriteria;
	private String lastKeyFound;
	private boolean searchRegex;
	private boolean searchData = true;
	private boolean searchMask;
	private boolean searchCase = true;
	private boolean replacePrompt = true;
	private boolean replaceAll;
	private String replaceTo;
	private BundleItem curItemForReplace;
	private LangItem curLangForReplace;

	private final List<Component> tabOrder = new ArrayList<>();

	public Translator(String s, SafeResourceBundle res) {
		init(s, res);
		onNewBundle();
	}

	public Translator(String s, SafeResourceBundle res, String bundleName) {
		init(s, res);
		clear();
		readResources(bundleName, false);
	}

	private void init(String s, SafeResourceBundle res) {
		SYS_DIR = s;
		rcTable = res;

		Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);

		CLOSE_BUTTONS[0] = RC("dialog.button.yes");
		CLOSE_BUTTONS[1] = RC("dialog.button.no");
		CLOSE_BUTTONS[2] = RC("dialog.button.cancel");

		REPLACE_BUTTONS[0] = RC("dialog.button.yes");
		REPLACE_BUTTONS[1] = RC("dialog.button.no");
		REPLACE_BUTTONS[2] = RC("dialog.button.cancel");

		YESNO_BUTTONS[0] = RC("dialog.button.yes");
		YESNO_BUTTONS[1] = RC("dialog.button.no");

		DELETE_BUTTONS[0] = RC("dialog.button.delete.all");
		DELETE_BUTTONS[1] = RC("dialog.button.delete.this");
		DELETE_BUTTONS[2] = RC("dialog.button.cancel");

		DELETE_BUTTONS2[0] = DELETE_BUTTONS[0];
		DELETE_BUTTONS2[1] = DELETE_BUTTONS[2];
		DELETE_BUTTONS3[0] = DELETE_BUTTONS[1];
		DELETE_BUTTONS3[1] = DELETE_BUTTONS[2];

		imgres = new ToolkitResolver();
		this.setLayout(new BorderLayout(0, 0));
		add("Center", pane);

		tool = new Toolbar();
		tool.add(91, new IELabel(RC("menu.file") + ":"));
		tool.add(0, new SpeedButton(imgres.getImage(SYS_DIR + "new.gif", this)));
		tool.add(1, new SpeedButton(imgres.getImage(SYS_DIR + "load.gif", this)));
		tool.add(2, new SpeedButton(imgres.getImage(SYS_DIR + "save.gif", this)));
		tool.add(3, new SpeedButton(imgres.getImage(SYS_DIR + "saveas.gif", this)));
		tool.add(92, new IELabel("+"));
		tool.add(4, new SpeedButton(imgres.getImage(SYS_DIR + "deploy.gif", this)));
		tool.add(5, new SpeedButton(imgres.getImage(SYS_DIR + "import.gif", this)));
		tool.add(93, new IELabel(RC("menu.edit") + ":"));
		tool.add(6, new SpeedButton(imgres.getImage(SYS_DIR + "newlang.gif", this)));
		tool.add(7, new SpeedButton(imgres.getImage(SYS_DIR + "del.gif", this)));
		tool.add(94, new IELabel(RC("menu.help") + ": "));
		tool.add(8, new SpeedButton(imgres.getImage(SYS_DIR + "about.gif", this)));
		add("North", tool);

		setIconImage(imgres.getImage(SYS_DIR + "jrc-editor.gif"));

		StatusBar panel3 = new StatusBar/*Panel*/();
		sbl1 = new IELabel();
		panel3.add(new StatusBarElement(sbl1, 20));
		sbl2 = new IELabel();
		panel3.add(new StatusBarElement(sbl2, 80));
		StatusBarElement se = new StatusBarStubbElement(new Panel(), 0, new Dimension(22, 19));
		se.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 2));
		se.setType(0);
		panel3.add(se);
		add("South", panel3);

		tree = new GraphTree();
		tree.setResolver(imgres);
		tree.setBackground(Color.white);
		ContextMenuBar mbar = new ContextMenuBar(this);

		ContextMenu _ctNewMenu = new ContextMenu("");
		ctNewMenu = new MenuItem(RC("tools.translator.menu.insert"));
		_ctNewMenu.add(ctNewMenu);
		mbar.add(_ctNewMenu);

		ContextMenu _ctNodeMenu = new ContextMenu("");
		ctNewMenu = new MenuItem(RC("tools.translator.menu.insert"));
		_ctNodeMenu.add(ctNewMenu);
		ctNodeExpandMenu = new MenuItem(RC("tools.translator.menu.expand"));
		_ctNodeMenu.add(ctNodeExpandMenu);
		ctNodeCollapseMenu = new MenuItem(RC("tools.translator.menu.collapse"));
		_ctNodeMenu.add(ctNodeCollapseMenu);
		ctNodeDeleteMenu = new MenuItem(RC("tools.translator.menu.delete"));
		_ctNodeMenu.add(ctNodeDeleteMenu);
		ctNodeRenameMenu = new MenuItem(RC("tools.translator.menu.rename"));
		_ctNodeMenu.add(ctNodeRenameMenu);
		mbar.add(_ctNodeMenu);
		tree.setMenuBar(mbar);

		pane.setLayout(new BorderLayout());
		Panel mainPanel = new BorderedPanel(BorderedPanel.RAISED2/*SUNKEN*/);
		GridBagLayout gbl = new GridBagLayout();
		Panel keyPanel = new Panel(gbl);

		IELabel keyLabel = new IELabel(RC("tools.translator.label.key"));
		constrain(keyPanel, keyLabel, 0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0.0, 0.0, 5, 5, 5, 5);

		keyName = new EmulatedTextField();
		keyName.setBackground(Color.white);
		constrain(keyPanel, keyName, 1, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 1.0, 1.0, 5, 5, 5, 5);

		keyInsertButton = new Button(RC("tools.translator.label.insert"));
		constrain(keyPanel, keyInsertButton, 2, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0.0, 0.0, 5, 5, 5, 5);

		keyDeleteButton = new Button(RC("tools.translator.label.delete"));
		constrain(keyPanel, keyDeleteButton, 3, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0.0, 0.0, 5, 5, 5, 5);

		pane.add(keyPanel, "South");
		pane.add(mainPanel, "Center");
		ResizeLayout resizeLayout = new ResizeLayout();
		Resizer rss = new Resizer();
		textPanel = new Panel();
		scrPanel = new SimpleScrollPanel(textPanel);
		setBackground(Color.lightGray);
		mainPanel.setLayout(resizeLayout);
		mainPanel.add(tree);
		mainPanel.add(scrPanel);
		mainPanel.add(rss);
		GridBagLayout textLayout = new GridBagLayout();
		textPanel.setLayout(textLayout);

		tabOrder.add(tree);
		tabOrder.add(keyName);
		tabOrder.add(keyInsertButton);
		tabOrder.add(keyDeleteButton);

		MenuBar menuBar = new MenuBar();
		fileMenu = new Menu(RC("menu.file"));
		newBundleMenu = new MenuItem(RC("tools.translator.menu.new.bundle"), new MenuShortcut(KeyEvent.VK_N));
		openBundleMenu = new MenuItem(RC("tools.translator.menu.open"), new MenuShortcut(KeyEvent.VK_O));
		saveBundleMenu = new MenuItem(RC("tools.translator.menu.save"), new MenuShortcut(KeyEvent.VK_S));
		saveBundleMenu.disable();
		saveAsBundleMenu = new MenuItem(RC("tools.translator.menu.saveas"));
		saveAsBundleMenu.disable();
		closeMenu = new MenuItem(RC("tools.translator.menu.close"));
		closeMenu.disable();
		exitMenu = new MenuItem(RC("menu.exit"));

		Menu editMenu = new Menu(RC("menu.edit"));

		editCopyMenu = new MenuItem(RC("tools.translator.menu.edit.copy") /* , new MenuShortcut(KeyEvent.VK_C) */ );
		editCutMenu = new MenuItem(RC("tools.translator.menu.edit.cut") /* , new MenuShortcut(KeyEvent.VK_X) */ );
		editPasteMenu = new MenuItem(RC("tools.translator.menu.edit.paste") /* , new MenuShortcut(KeyEvent.VK_V) */ );
		editDeleteMenu = new MenuItem(RC("tools.translator.menu.edit.delete"));
		searchMenu = new MenuItem(RC("menu.search"));
		searchAgainMenu = new MenuItem(RC("menu.searchagain"), new MenuShortcut(KeyEvent.VK_F));
		replaceToMenu = new MenuItem(RC("menu.replace"));

		newLangMenu = new MenuItem(RC("tools.translator.menu.new.lang"), new MenuShortcut(KeyEvent.VK_L));
		delMenu = new MenuItem(RC("tools.translator.menu.delete"), new MenuShortcut(KeyEvent.VK_D));
		insMenu = new MenuItem(RC("tools.translator.menu.insert"), new MenuShortcut(KeyEvent.VK_I));
		renMenu = new MenuItem(RC("tools.translator.menu.rename"), new MenuShortcut(KeyEvent.VK_R));

		Menu treeMenu = new Menu(RC("menu.tree"));
		expandNodeMenu = new MenuItem(RC("tools.translator.menu.node.expand") /*, new MenuShortcut(KeyEvent.VK_PLUS)*/);
		collapseNodeMenu = new MenuItem(RC("tools.translator.menu.node.collapse") /*, new MenuShortcut(KeyEvent.VK_MINUS)*/ );
		expandTreeMenu = new MenuItem(RC("tools.translator.menu.expand"));
		collapseTreeMenu = new MenuItem(RC("tools.translator.menu.collapse"));
		hideTransMenu = new CheckboxMenuItem(RC("tools.translator.menu.hide.completed"));

		Menu viewMenu = new Menu(RC("menu.options"));
		statisticsMenu = new MenuItem(RC("tools.translator.menu.statistics"));
		showNullsMenu = new CheckboxMenuItem(RC("tools.translator.menu.nulls"), false);
		langMenu = new Menu(RC("tools.translator.menu.showres"));
		langMenu.disable();
		Menu optionsMenu = new Menu/*Item*/(RC("tools.translator.menu.options"));
		keepLastDirMenu = new CheckboxMenuItem(RC("tools.translator.menu.options.keeplastdir"), true);
		omitSpacesMenu = new CheckboxMenuItem(RC("tools.translator.menu.options.omitspaces"), true);
		autoExpandTFMenu = new CheckboxMenuItem(RC("tools.translator.menu.options.autofit"), true);
		allowDotMenu = new CheckboxMenuItem(RC("tools.translator.menu.options.allowdot"), true);
		allowUScoreMenu = new CheckboxMenuItem(RC("tools.translator.menu.options.allowuscore"), true);
		omitSpacesMenu.disable();

		Menu helpMenu = new Menu(RC("menu.help"));
		aboutMenu = new MenuItem(RC("menu.about"));

		Menu toolMenu = new Menu(RC("tools.translator.menu.tools"));
		genMenu = new MenuItem(RC("tools.translator.menu.generate"));
		genMenu.disable();
		parseMenu = new MenuItem(RC("tools.translator.menu.parse"));
		saveXmlBundleMenu = new MenuItem(RC("tools.translator.menu.save.xml"));
		saveUtfBundleMenu = new MenuItem(RC("tools.translator.menu.save.utf"));
		loadXmlBundleMenu = new MenuItem(RC("tools.translator.menu.load.xml"));
		loadUtfBundleMenu = new MenuItem(RC("tools.translator.menu.load.utf"));

		saveXmlBundleMenuP = new MenuItem(RC("tools.translator.menu.save.xml.part"));
		saveUtfBundleMenuP = new MenuItem(RC("tools.translator.menu.save.utf.part"));
		loadXmlBundleMenuP = new MenuItem(RC("tools.translator.menu.load.xml.part"));
		loadUtfBundleMenuP = new MenuItem(RC("tools.translator.menu.load.utf.part"));
		openBundleMenuP = new MenuItem(RC("tools.translator.menu.load.part"));

		loadJarMenu = new MenuItem(RC("tools.translator.menu.load.jar"));

		fileMenu.add(newBundleMenu);
		fileMenu.add(openBundleMenu);
		fileMenu.add(saveBundleMenu);
		fileMenu.add(saveAsBundleMenu);
		fileMenu.add(closeMenu);
		fileMenu.addSeparator();

		editMenu.add(newLangMenu);
		editMenu.addSeparator();
		editMenu.add(editCopyMenu);
		editMenu.add(editCutMenu);
		editMenu.add(editPasteMenu);
		editMenu.add(editDeleteMenu);
		editMenu.addSeparator();
		editMenu.add(insMenu);
		editMenu.add(delMenu);
		editMenu.add(renMenu);
		editMenu.addSeparator();
		editMenu.add(searchMenu);
		editMenu.add(searchAgainMenu);
		editMenu.add(replaceToMenu);

		treeMenu.add(expandNodeMenu);
		treeMenu.add(collapseNodeMenu);
		treeMenu.addSeparator();
		treeMenu.add(expandTreeMenu);
		treeMenu.add(collapseTreeMenu);
		treeMenu.add(hideTransMenu);

		viewMenu.add(langMenu);
		viewMenu.add(showNullsMenu);
		viewMenu.add(statisticsMenu);
		viewMenu.addSeparator();
		viewMenu.add(optionsMenu);

		optionsMenu.add(keepLastDirMenu);
		optionsMenu.add(omitSpacesMenu);
		optionsMenu.add(autoExpandTFMenu);
		optionsMenu.add(allowDotMenu);
		optionsMenu.add(allowUScoreMenu);

		toolMenu.add(loadJarMenu);
		toolMenu.addSeparator();
		toolMenu.add(loadXmlBundleMenu);
		toolMenu.add(loadUtfBundleMenu);
		toolMenu.add(saveXmlBundleMenu);
		toolMenu.add(saveUtfBundleMenu);
		toolMenu.addSeparator();
		toolMenu.add(openBundleMenuP);
		toolMenu.add(loadXmlBundleMenuP);
		toolMenu.add(loadUtfBundleMenuP);
		toolMenu.add(saveXmlBundleMenuP);
		toolMenu.add(saveUtfBundleMenuP);
		toolMenu.addSeparator();
		toolMenu.add(genMenu);
		toolMenu.add(parseMenu);

		helpMenu.add(aboutMenu);

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(viewMenu);
		menuBar.add(treeMenu);
		menuBar.add(toolMenu);
		menuBar.add(helpMenu);
		setMenuBar(menuBar);

		delDialog = new MessageBox2(this);
		delDialog.setIcon(imgres.getImage(SYS_DIR + "ogo.gif", delDialog));
		delDialog.setTitle(RC("dialog.title.warning"));
		delDialog.setButtons(DELETE_BUTTONS);
		delDialog.addListener(this);

		closeDialog = new MessageBox2(this);
		closeDialog.setText(RC("tools.translator.message.save"));
		closeDialog.setTitle(RC("dialog.title.warning"));
		closeDialog.setIcon(imgres.getImage(SYS_DIR + "ogo.gif", closeDialog));
		closeDialog.setButtons(CLOSE_BUTTONS);
		closeDialog.addListener(this);

		repDialog = new MessageBox2(this);
		repDialog.setText("");
		repDialog.setTitle(RC("dialog.title.warning"));
		repDialog.setIcon(imgres.getImage(SYS_DIR + "ogo.gif", repDialog));
		repDialog.setButtons(REPLACE_BUTTONS);
		repDialog.addListener(this);

		errDialog = new MessageBox2(this);
		errDialog.setText("");
		errDialog.setTitle(RC("dialog.title.warning"));
		errDialog.setIcon(imgres.getImage(SYS_DIR + "Stop.gif", errDialog));
		String[] OK_BUT = { RC("dialog.button.ok") };
		errDialog.setButtons(OK_BUT);

		tbar2menu = new MenuItem[] {
			newBundleMenu,
			openBundleMenu,
			saveBundleMenu,
			saveAsBundleMenu,
			genMenu,
			parseMenu,
			newLangMenu,
			delMenu,
			aboutMenu };
	}

	@Override
	public boolean handleEvent(Event e) {
		if (e.id == Event.WINDOW_DESTROY) {
			onClose();
			return true;
		}

		if (e.target == tree) {
			if (e.id == TranslatorConstants.REMOVE_REQUIRED) {
				onDeleteKey();
			}
			if ((e.target == tree) && (!Objects.equals(wasSelectedKey, tree.getSelectedText()))) {
				setTranslations();
				invokeAutoFit();
			}
			// actually, we should just list all events that tree can handle itself.
		}
		return super.handleEvent(e);
	}

	@Override
	public void eventDispatched(AWTEvent event) {
		if (event.getID() != KeyEvent.KEY_TYPED) {
			return;
		}
		if (!(event instanceof KeyEvent)) {
			return;
		}
		KeyEvent ke = (KeyEvent) event;
		if (ke.getKeyChar() != '\t') {
			return;
		}
		moveFocus();
	}

	@Override
	public boolean keyDown(Event e, int key) {
		if ((e.target == keyName) && (key == Event.ENTER)) {
			onInsertKey();
			return true;
		}
		else if ((e.target instanceof Button) && (key == Event.ENTER)) {
			action(e, null);
			return true;
		}
		// to be corrected if keyName wants to receive "Enter" from JTextField
		return false;
	}

	@Override
	public boolean action(Event e, Object arg) {
		if (e.target instanceof Toolbar) {
			int pos = Integer.parseInt((String) arg);
			if ((pos < 0) || (pos >= tbar2menu.length)) {
				return false;
			}
			e.target = tbar2menu[pos];
		}

		if (e.target == statisticsMenu) {
			onStatistics();
		}
		if (e.target == searchMenu) {
			onSearch();
		}
		if (e.target == searchAgainMenu) {
			onSearchAgain();
		}
		if (e.target == replaceToMenu) {
			onReplace();
		}

		if ((e.target == expandNodeMenu) || (e.target == ctNodeExpandMenu)) {
			expand(tree.getSelectedNode());
		}
		if ((e.target == collapseNodeMenu) || (e.target == ctNodeCollapseMenu)) {
			collapse(tree.getSelectedNode());
		}
		if (e.target == expandTreeMenu) {
			tree.expandAll();
			tree.repaint();
		}
		if (e.target == collapseTreeMenu) {
			tree.collapseAll();
			tree.repaint();
		}
		if (e.target == hideTransMenu) {
			hideTranslated(hideTransMenu.getState());
		}
		if (e.target == dropComment) {
			commField.setText("");
			setTranslations();
		}

		if (e.target == editCopyMenu) {
			Component ccur = getFocusOwner();
			if (ccur instanceof EmulatedTextField) {
				EmulatedTextField cur = (EmulatedTextField) ccur;
				cur.blCopy();
			}
			else if (ccur instanceof TextField) {
				TextField cur = (TextField) ccur;
				Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection s2 = new StringSelection(cur.getSelectedText());
				c.setContents(s2, s2);
			}
		}
		if (e.target == editCutMenu) {
			Component ccur = getFocusOwner();
			if (ccur instanceof EmulatedTextField) {
				EmulatedTextField cur = (EmulatedTextField) ccur;
				cur.blCopy();
				cur.blDelete();
			}
			else if (ccur instanceof TextField) {
				TextField cur = (TextField) ccur;
				Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection s2 = new StringSelection(cur.getSelectedText());
				c.setContents(s2, s2);
				if (!cur.getSelectedText().isEmpty()) {
					cur.setText(cur.getText().substring(0, cur.getSelectionStart()) + cur.getText().substring(cur.getSelectionEnd()));
				}
			}
		}
		if (e.target == editPasteMenu) {
			Component ccur = getFocusOwner();
			if (ccur instanceof EmulatedTextField) {
				EmulatedTextField cur = (EmulatedTextField) ccur;
				cur.blPaste();
			}
			else if (ccur instanceof TextField) {
				TextField cur = (TextField) ccur;

				Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable t = c.getContents("e");

				String nt = "";
				if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					try {
						nt = (String) t.getTransferData(DataFlavor.stringFlavor);
					}
					catch (Exception ex) {
					}
				}

				if (!cur.getSelectedText().isEmpty()) {
					cur.setText(cur.getText().substring(0, cur.getSelectionStart()) + nt + cur.getText().substring(cur.getSelectionEnd()));
				}
				else {
					cur.setText(cur.getText().substring(0, cur.getCaretPosition()) + nt + cur.getText().substring(cur.getCaretPosition()));
				}
			}
		}
		if (e.target == editDeleteMenu) {
			Component ccur = getFocusOwner();
			if (ccur instanceof EmulatedTextField) {
				EmulatedTextField cur = (EmulatedTextField) ccur;
				cur.blDelete();
			}
			else if (ccur instanceof TextField) {
				TextField cur = (TextField) ccur;
				if (!cur.getSelectedText().isEmpty()) {
					cur.setText(cur.getText().substring(0, cur.getSelectionStart()) + cur.getText().substring(cur.getSelectionEnd()));
				}
			}
		}

		if (e.target == newLangMenu) {
			onNewResource();
		}
		if ((e.target == insMenu) || (e.target == ctNewMenu)) {
			onNewKey();
		}
		if ((e.target == renMenu) || (e.target == ctNodeRenameMenu)) {
			onRenameKey();
		}
		if (e.target == newBundleMenu) {
			onNewBundle();
		}
		if (e.target == closeMenu) {
			exitInitiated = false;
			onClose();
		}
		if (e.target == openBundleMenu) {
			onOpen(false);
		}
		if (e.target == saveBundleMenu) {
			onSave();
		}
		if (e.target == saveAsBundleMenu) {
			onSaveAs();
		}
		if (e.target == exitMenu) {
			onClose();
		}
		if (e.target == keyInsertButton) {
			onInsertKey();
		}
		if ((e.target == keyDeleteButton) || (e.target == delMenu) || (e.target == ctNodeDeleteMenu)) {
			onDeleteKey();
		}
		if (e.target == genMenu) {
			onGenCode();
		}
		if (e.target == parseMenu) {
			onParseCode();
		}
		if (e.target == aboutMenu) {
			onAbout();
		}

		if (e.target == loadXmlBundleMenu) {
			onLoadXml(false);
		}
		if (e.target == saveXmlBundleMenu) {
			onSaveXml(false);
		}
		if (e.target == loadUtfBundleMenu) {
			onLoadUtf(false);
		}
		if (e.target == saveUtfBundleMenu) {
			onSaveUtf(false);
		}

		if (e.target == openBundleMenuP) {
			onOpen(true);
		}
		if (e.target == loadXmlBundleMenuP) {
			onLoadXml(true);
		}
		if (e.target == saveXmlBundleMenuP) {
			onSaveXml(true);
		}
		if (e.target == loadUtfBundleMenuP) {
			onLoadUtf(true);
		}
		if (e.target == saveUtfBundleMenuP) {
			onSaveUtf(true);
		}

		if (e.target == loadJarMenu) {
			onLoadJar();
		}

		if ((e.target instanceof CheckboxMenuItem) && (e.target == showNullsMenu)) {
			setIndicators(tree.getRootNode());
			tree.repaint();
		}
		if (e.target instanceof CheckboxMenuItem) {
			for (int i = 0; i < langStates.size(); i++) {
				LangState ls = getLangState(i);
				if (e.target == ls.box) {
					ls.hidden = !ls.hidden;
					ls.tf.setVisible(!ls.hidden);
					ls.label.setVisible(!ls.hidden);
					setIndicators(tree.getRootNode());
					textPanel.invalidate();
					validate();
				}
				ls.box.setState(!ls.hidden);
			}
		}
		if ((e.target == closeDialog) && (e.arg instanceof Button)) {
			if (!((Button) e.arg).getLabel().equals(CLOSE_BUTTONS[2])) {
				if (((Button) e.arg).getLabel().equals(CLOSE_BUTTONS[0])) {
					onSave();
				}
				if (exitInitiated) {
					finish();
				}
				else {
					clear();
				}
			}
			exitInitiated = true;
		}
		if ((e.target == delDialog) && (e.arg instanceof Button) && ((Button) e.arg).getLabel().equals(DELETE_BUTTONS[0])) {
			String key = tree.getSelectedText();
			// remove all subkeys
			if (key != null) {
				isDirty = true;
				TreeNode tn = tree.getNode(key);
				if (tn != null) {
					tn = tn.parent;
				}
				bundle.getBundle().removeKeysBeginningWith(key);

				tree.remove(key); // kill children
				removeLeafs(key); // clean leafs out of model
				adjustIndicator(tn);
				tree.repaint();
				wasSelectedKey = null;
				setTranslations();
			}
		}
		if ((e.target == delDialog) && (e.arg instanceof Button) && ((Button) e.arg).getLabel().equals(DELETE_BUTTONS[1])) {
			// Only this
			String key = tree.getSelectedText();
			if (key != null) {
				isDirty = true;
				TreeNode tn = tree.getNode(key);
				if (tn == null) {
					return true;
				}

				// Not an leaf => don't touch tree but update model
				bundle.getBundle().removeKey(key);
				if ((tree.enumChild(tn) == null) || (tree.enumChild(tn).length == 0)) {
					tree.remove(key);
					removeLeafs(key);
				}
				tree.selectNode(tn.parent);

				adjustIndicator(tn);
				tree.repaint();

				wasSelectedKey = null;
				setTranslations();
				textPanel.invalidate();
				validate();
			}
		}

		if (e.target == repDialog) {
			if ((e.arg instanceof Button) && ((Button) e.arg).getLabel().equals(REPLACE_BUTTONS[0])) {
				makeReplaceImpl();
			}
			else if ((e.arg instanceof Button) && ((Button) e.arg).getLabel().equals(REPLACE_BUTTONS[2])) {
				replaceTo = null;
			}
		}

		if (e.target == keepLastDirMenu) {
			keepLastDir = keepLastDirMenu.getState();
		}
		if (e.target == omitSpacesMenu) {
			omitSpaces = omitSpacesMenu.getState();
		}
		if (e.target == autoExpandTFMenu) {
			autoExpandTF = autoExpandTFMenu.getState();
		}
		if (e.target == allowDotMenu) {
			allowDot = allowDotMenu.getState();
		}
		if (e.target == allowUScoreMenu) {
			allowUScore = allowUScoreMenu.getState();
		}

		if (e.target instanceof MenuItem) {
			String lbl = ((MenuItem) e.target).getLabel();
			for (String path : pickList) {
				String patz = stretchPath(path);
				if (patz.equals(lbl)) {
					clear();
					readResources(path, false);
					break;
				}
			}
		}
		sbl1.setText(" " + getVisLangCount() + "/" + bundle.getBundle().getLangCount() + ", " + bundle.getBundle().getItemCount() + " ");
		return true;
	}

	private void setTranslations() {
		String newKey = tree.getSelectedText();
		setTranslations(newKey);
	}

	private void setTranslations(String newKey) {
		if (wasSelectedKey != null) {
			for (int i = 0; i < langStates.size(); i++) {
				LangState ls = getLangState(i);
				if (ls.hidden) {
					continue;
				}
				String trans = ls.tf.getText();
				BundleItem bi = bundle.getBundle().getItem(wasSelectedKey);
				if (bi == null) {
					// do not add the translation implicitely - disable
					// the language field
					ls.tf.setVisible(false);
					ls.label.setVisible(false);
					commField.setEnabled(false);
				}
				else {
					if ((bi.getTranslation(ls.name) == null) || !bi.getTranslation(ls.name).equals(trans)) {
						isDirty = true;
					}
					bundle.getBundle().updateValue(wasSelectedKey, ls.name, trans);
				}
			}
			String comm = commField == null ? null : commField.getText();
			if ((comm != null) && (comm.trim().isEmpty())) {
				comm = null;
			}
			BundleItem bi = bundle.getBundle().getItem(wasSelectedKey);
			if (bi != null) {
				bi.setComment(comm);
			}
			adjustIndicator(tree.getNode(wasSelectedKey));
			setIndicators(tree.getNode(wasSelectedKey));
			tree.repaint();
		}
		if (newKey == null) {
			return;
		}

		BundleItem bi = bundle.getBundle().getItem(newKey);
		for (int i = 0; i < langStates.size(); i++) {
			LangState ls = getLangState(i);
			String ss = bi == null ? null : bi.getTranslation(ls.name);
			if (ss == null) {
				ss = "";
			}
			if (bi == null) {
				ls.tf.setVisible(false);
				ls.label.setVisible(false);
				commField.setEnabled(false);
			}
			else {
				ls.tf.setVisible(!ls.hidden);
				ls.label.setVisible(!ls.hidden);
				commField.setEnabled(true);
			}
			ls.tf.setText(ss);
		}
		String commText = bi == null ? " ** " + RC("tools.translator.message.noentry") + " **" : bi.getComment();
		if (commField != null) {
			commField.setText(commText == null ? "" : commText);
		}
		keynLab.setText("Key: " + newKey);
		keynLab.repaint();
		sbl2.setText(newKey);
		adjustIndicator(tree.getNode(newKey));

		wasSelectedKey = newKey;
		String startValue = wasSelectedKey + ".";
		keyName.setText(startValue);
		tree.repaint();
	}

	private String getValidKey() {
		String key = keyName.getText();
		if (key == null) {
			return null;
		}
		while (key.endsWith(".")) {
			key = key.substring(0, key.length() - 1);
		}
		if (key.length() <= 0) {
			return null;
		}
		String illegalChar = "";
		if (key.indexOf('=') >= 0) {
			illegalChar = "=";
		}
		if (key.indexOf('#') >= 0) {
			illegalChar = "#";
		}
		if (illegalChar.isEmpty()) {
			return bundle.replace(key, "..", "");
		}
		MessageBox2 mess = new MessageBox2(this);
		mess.setTitle(RC("dialog.title.warning"));
		mess.setText(bundle.replace(RC("tools.translator.message.illchar"), "[%illchar%]", illegalChar));
		mess.setIcon(imgres.getImage(SYS_DIR + "stop.gif", mess));
		mess.setButtons(RC("dialog.button.ok"));
		mess.show();
		return null;
	}

	private void onInsertKey() {
		if (bundle.getBundle().getLangCount() == 0) {
			return;
		}
		String key = getValidKey();
		if (key != null) {
			addToTree(key);
			bundle.getBundle().addKey(key);
			bundle.getBundle().resort();
			commField.setText("");
			isDirty = true;
			tree.selectNodeAndOpen(key);
			tree.repaint();
			setTranslations();
			saveBundleMenu.enable();
			saveAsBundleMenu.enable();
			genMenu.enable();
			setIndicators(tree.getRootNode());
			isDirty = true;

			textPanel.invalidate();
			validate();
		}
		syncToolbar();
	}

	private void onDeleteKey() {
		String key = tree.getSelectedText();
		if (key == null) {
			return;
		}
		delDialog.setText(bundle.replace(RC("tools.translator.message.delkey"), "[%key%]", key));

		TreeNode tn = tree.getNode(key);
		if (tn == null) {
			return;
		}
		boolean hasChilds = (tree.enumChild(tn) != null) && (tree.enumChild(tn).length != 0);
		BundleItem bi = bundle.getBundle().getItem(key);
		if ((bi != null) && hasChilds) {
			delDialog.setButtons(DELETE_BUTTONS);
		}
		else if ((bi != null) && !hasChilds) {
			delDialog.setButtons(DELETE_BUTTONS3);
		}
		else if (bi == null) {
			delDialog.setButtons(DELETE_BUTTONS2);
		}
		delDialog.show();
	}

	private void onClose() {
		setTranslations();
		if (isDirty) {
			closeDialog.show();
		}
		else if (exitInitiated) {
			finish();
		}
		else {
			clear();
			exitInitiated = true;
		}
	}

	private void finish() {
		hide();
		saveIni();
		System.exit(0);
	}

	private void onSave() {
		if (bundle.getBundle().getLangCount() == 0) {
			return;
		}
		String fn = bundle.getBundle().getLanguage(0).getLangFile();
		if (fn == null) {
			onSaveAs();
			return;
		}
		setTranslations();
		try {
			bundle.store(null);
		}
		catch (Exception e) {
			infoException(e);
		}
		isDirty = false;
	}

	private void clear() {
		setTitle(null);
		if (keyName != null) {
			keyName.setText("");
		}
		if (keynLab != null) {
			keynLab.setText("");
		}
		wasSelectedKey = null;
		textPanel.removeAll();
		langMenu.removeAll();
		if (tree.getRootNode() != null) {
			tree.remove(tree.getRootNode());
		}
		tree.repaint();
		textPanel.invalidate();
		validate();
		isDirty = false;
		bundle = new BundleManager();
		langStates = new ArrayList<>();

		closeMenu.disable();
		saveBundleMenu.disable();
		saveAsBundleMenu.disable();
		genMenu.disable();
		langMenu.disable();
	}

	private LangState getLangState(int idx) {
		return langStates.get(idx);
	}

	private int getVisLangCount() {
		return (int) IntStream.range(0, langStates.size()).mapToObj(this::getLangState).filter(ls -> !ls.hidden).count();
	}

	private void setAllIndicators() {
		for (int i = 0; i < langStates.size(); i++) {
			LangState ls = getLangState(i);
			ls.hidden = false;
			ls.box.setState(true);
		}
		// Fire it async as it take large time
		hideTransMenu.disable();
		(new Thread(this::setIndicatorsInit)).start();
	}

	private void setIndicatorsInit() {
		sbl2.setText(RC("tools.translator.progress.indicator"));
		sbl2.repaint();
		setIndicators(tree.getRootNode());
		hideTransMenu.enable();
		sbl2.setText("");
		sbl2.repaint();
	}

	private boolean setIndicators(TreeNode tn) {
		if (tn == null) {
			return false;
		}
		boolean res = setIndicators(tn.sibling);
		return setIndicator(tn, setIndicators(tn.child)) || res;
	}

	private boolean setIndicator(TreeNode tn, boolean childOn) {
		if (tn == null) {
			return false;
		}
		if (getVisLangCount() < 2) {
			tn.setIndicator(null);
			return false;
		}
		if (childOn) {
			tn.setIndicator(SYS_DIR + TranslatorConstants.WARN_IMAGE);
			return true;
		}

		BundleItem bi = bundle.getBundle().getItem(tn.getText());
		if (bi == null) {
			tn.setIndicator(null);
			return false;
		}
		boolean isPres = false;
		boolean isAbs = false;
		for (int i = 0; i < langStates.size(); i++) {
			LangState ls = getLangState(i);
			if (ls.hidden) {
				continue;
			}
			String ts = bi.getTranslation(ls.name);
			if ((ts != null) && (ts.trim().isEmpty())) {
				ts = null;
			}
			isAbs |= ts == null;
			isPres |= ts != null;
		}
		tn.setIndicator(null);
		if (isAbs && isPres) {
			tree.setIndicator(tn.getText(), SYS_DIR + TranslatorConstants.WARN_IMAGE);
			notCompletedCount++;
		}
		else {
			if (isAbs) {
				nullsCount++;
				if (showNullsMenu.getState()) {
					tn.setIndicator(SYS_DIR + TranslatorConstants.WARN_IMAGE);
					return true;
				}
			}
		}
		return isAbs && isPres;
	}

	private void adjustIndicator(TreeNode tn) {
		if (tn == null) {
			return;
		}
		setIndicator(tn, isSetInSiblings(tn.child));
		adjustIndicator(tn.parent);
	}

	private boolean isSetInSiblings(TreeNode tn) {
		if (tn == null) {
			return false;
		}
		if (tn.getIndicator() != null) {
			return true;
		}
		return isSetInSiblings(tn.sibling);
	}

	private void onSearch() {
		SearchDialog ed = new SearchDialog(this, RC("tools.translator.label.search.caption"), true, this);
		ed.setLabelCaption(RC("tools.translator.label.search.label"));
		ed.setButtonsCaption(RC("dialog.button.ok"), CLOSE_BUTTONS[2]);

		ed.setKVGroupLabels(RC("tools.translator.label.search.inkeys"), RC("tools.translator.label.search.invalues"));
		ed.setRMEGroupLabels(RC("tools.translator.label.search.regex"), RC("tools.translator.label.search.mask"),
			RC("tools.translator.label.search.exact"));
		ed.setCaseLabel(RC("tools.translator.label.search.case"));

		ed.doModal();
		String text = ed.getText();
		if ((text.length() <= 0) || !ed.isApply()) {
			return;
		}

		searchCriteria = text;
		searchRegex = ed.isRegexMatching();
		searchData = !ed.isKeyMatching();
		searchMask = ed.isMaskMatching();
		searchCase = ed.isCaseSensitive();
		replaceTo = null;
		if (!searchRegex && !searchMask && !searchCase) {
			searchCriteria = searchCriteria.toLowerCase();
		}

		lastKeyFound = null;
		onSearchAgain();
		ed.dispose();
	}

	private void onReplace() {
		ReplaceDialog ed = new ReplaceDialog(this, RC("tools.translator.label.replace.caption"), true, this);
		ed.setLabelCaption(RC("tools.translator.label.search.label"));
		ed.setButtonsCaption(RC("dialog.button.ok"), CLOSE_BUTTONS[2]);
		ed.setReplaceLabel(RC("tools.translator.label.replace.label"));

		ed.setRMGroupLabels(RC("tools.translator.label.search.regex"), RC("tools.translator.label.search.exact"));
		ed.setCPALabels(RC("tools.translator.label.search.case"), RC("tools.translator.label.replace.prompt"),
			RC("tools.translator.label.replace.all"));

		ed.doModal();
		String text = ed.getText();
		if ((text.length() <= 0) || !ed.isApply()) {
			return;
		}

		searchCriteria = text;
		searchRegex = ed.isRegexMatching();
		searchData = true;
		searchMask = false;
		searchCase = ed.isCaseSensitive();
		replacePrompt = ed.isPromptRequired();
		replaceAll = ed.isReplaceAll();
		replaceTo = ed.getReplaceTo();

		if (!searchRegex && !searchMask && !searchCase) {
			searchCriteria = searchCriteria.toLowerCase();
		}

		lastKeyFound = null;
		onSearchAgain();
	}

	private boolean isMatchedWith(String what) {
		if (what == null) {
			return false;
		}
		if (searchRegex) {
			return match_regex(searchCriteria, what, !searchCase);
		}
		else if (searchMask) {
			return match_mask(searchCriteria, what, !searchCase);
		}
		else {
			if (searchCase) {
				return what.contains(searchCriteria);
			}
			else {
				return what.toLowerCase().contains(searchCriteria);
			}
		}
	}

	private void makeReplaceImpl() {
		String lang = curLangForReplace.getLangId();
		String val = curItemForReplace.getTranslation(lang);
		if (searchRegex) {
			try {
				RE re = new RE(searchCriteria, searchCase ? RE.MATCH_NORMAL : RE.MATCH_CASEINDEPENDENT);
				val = re.subst(val, replaceTo, replaceAll ? RE.REPLACE_ALL : RE.REPLACE_FIRSTONLY);
			}
			catch (RESyntaxException e) {
				infoException(e);
				replaceTo = null;
			}
		}
		else {
			if (replaceAll) {
				val = bundle.replace(val, searchCriteria, replaceTo);
			}
			else {
				int j1 = val.indexOf(searchCriteria);
				if (j1 > 0) {
					val = val.substring(0, j1) + replaceTo + val.substring(j1 + searchCriteria.length());
				}
				else if (j1 == 0) {
					val = replaceTo + val.substring(searchCriteria.length());
				}
			}
		}
		curItemForReplace.setTranslation(lang, val);

		if (tree.getSelectedText().equals(curItemForReplace.getId())) {
			int k = bundle.getBundle().getLangIndex(lang);
			LangState ls = getLangState(k);
			if (ls != null) {
				ls.tf.setText(val);
				ls.tf.getControl().requestFocus();
			}
		}
		isDirty = true;
	}

	private void makeReplace(BundleItem bi, LangItem li) {
		curItemForReplace = bi;
		curLangForReplace = li;
		if (replacePrompt) {
			if (replaceAll) {
				setTranslations();
				tree.selectNode(curItemForReplace.getId());
				tree.openToNode(curItemForReplace.getId());
				setTranslations(curItemForReplace.getId());
				textPanel.invalidate();
				validate();
				tree.repaint();
			}
			repDialog.setModal(replaceAll);
			String mess = RC("tools.translator.message.found");
			mess = bundle.replace(mess, "[%found%]", searchCriteria);
			mess = bundle.replace(mess, "[%subst%]", replaceTo);
			repDialog.setText(mess);
			repDialog.show();
		}
		else {
			makeReplaceImpl();
		}
	}

	private void onSearchAgain() {
		if (searchCriteria == null) {
			onSearch();
			return;
		}
		boolean first = lastKeyFound == null;

		int j = 0;
		if (lastKeyFound != null) {
			j = bundle.getBundle().getItemIndex(lastKeyFound) + 1;
		}
		int i;
		if (!searchData) { // Key names
			for (i = j; i < bundle.getBundle().getItemCount(); ++i) {
				BundleItem bi = bundle.getBundle().getItem(i);
				String val = bi.getId();
				if (isMatchedWith(val)) {
					lastKeyFound = val;
					tree.selectNode(bi.getId());
					tree.openToNode(bi.getId());
					setTranslations(bi.getId());
					textPanel.invalidate();
					validate();
					tree.requestFocus();
					tree.repaint();
					return;
				}
			}
			lastKeyFound = null;
			if (first) {
				searchCriteria = null;
			}
			errDialog.setText(first ? RC("tools.translator.label.search.nokeys") : RC("tools.translator.label.search.nomorekeys"));
			errDialog.show();
			return;
		}

		int replacements = 0;
		for (i = j; i < bundle.getBundle().getItemCount(); ++i) {
			BundleItem bi = bundle.getBundle().getItem(i);
			for (int k = 0; k < bundle.getBundle().getLangCount(); ++k) {
				LangItem li = bundle.getBundle().getLanguage(k);
				String val = bi.getTranslation(li.getLangId());
				if (isMatchedWith(val)) {
					lastKeyFound = bi.getId();

					if ((replaceTo == null) || ((replaceTo != null) && !replaceAll)) {
						tree.selectNode(bi.getId());
						tree.openToNode(bi.getId());
						setTranslations(bi.getId());
						tree.repaint();

						if (replaceTo != null) {
							makeReplace(bi, li);
						}

						textPanel.invalidate();
						validate();
						if (replaceTo == null) {
							textPanel.requestFocus();
							LangState ls = getLangState(k);
							ls.tf.getControl().requestFocus();
						}
						return;
					}
					makeReplace(bi, li);
					++replacements;
					if (replaceTo == null) {
						break;
					}
				}
			}
		}
		lastKeyFound = null;
		if (first) {
			searchCriteria = null;
		}
		if (replacements > 0) {
			errDialog.setText(bundle.replace(RC("tools.translator.label.replaced.count"), "[%replaced%]", Integer.toString(replacements)));
		}
		else {
			errDialog.setText(first ? RC("tools.translator.label.search.nokeys") : RC("tools.translator.label.search.nomorekeys"));
		}
		errDialog.show();
	}

	private void onNewResource() {
		EditDialog ed = new EditDialog(this, RC("tools.translator.label.newrestitle"), true, this);
		ed.setLabelCaption(RC("tools.translator.label.filesuff"));
		ed.setButtonsCaption(RC("dialog.button.ok"), CLOSE_BUTTONS[2]);
		ed.doModal();
		String text = ed.getText();
		if ((text.length() <= 0) || !ed.isApply()) {
			return;
		}
		bundle.getBundle().addLanguage(text);
		syncLanguage(text);

		for (int i = 0; i < langStates.size(); i++) {
			LangState ls = getLangState(i);
			CheckboxMenuItem cmi = ls.box;
			boolean show = cmi.getState();
			ls.tf.setVisible(show);
			ls.hidden = !show;
			ls.label.setVisible(show);
		}
		setAllIndicators();
		textPanel.invalidate();
		validate();
	}

	private void onNewBundle() {
		clear();
		initControls();
		bundle.getBundle().addLanguage("en");
		bundle.getBundle().addKey("creationDate");
		bundle.getBundle().updateValue("creationDate", "en", (new Date()).toLocaleString());
		bundle.getBundle().resort();
		initData(false);
		setTitle(null);
		isDirty = false;
	}

	private SafeResourceBundle rcTable;

	private String RC(String key) {
		return rcTable.getString(key);
	}

	@Override
	public void setTitle(String filename) {
		String add = "";
		if (filename != null) {
			add = " [" + filename + "]";
		}
		super.setTitle("Zaval JRC Editor" + add);
		sbl2.setText(filename == null ? "" : filename);
	}

	private void join(BundleManager bundle2, boolean part) {
		if (part) {
			BundleSet set = bundle2.getBundle();
			int items = set.getItemCount();
			for (int i = 0; i < items; ++i) {
				BundleItem bi = set.getItem(i);
				BundleItem bi2 = bundle.getBundle().addKey(bi.getId());
				for (String lang : bi.getLanguages()) {
					bundle.getBundle().addLanguage(lang);
					bi2.setTranslation(lang, bi.getTranslation(lang));
				}
			}
			set.resort();
		}
		else {
			bundle = bundle2;
		}
	}

	private class Loader implements Runnable {
		private final String fileName;
		private final boolean part;

		private Loader(String fileName, boolean part) {
			this.fileName = fileName;
			this.part = part;
		}

		@Override
		public void run() {
			setCursor(Cursor.WAIT_CURSOR);
			sbl2.setText(RC("tools.translator.progress.loadfiles"));
			sbl2.repaint();
			try {
				BundleManager bundle2 = new BundleManager(fileName);
				join(bundle2, part);
			}
			catch (Exception e) {
				infoException(e);
			}
			sbl2.setText(RC("tools.translator.progress.maketree"));
			sbl2.repaint();

			if (!part) {
				initControls();
			}
			else {
				wasSelectedKey = null;
			}

			initData(part);
		}
	}

	private void readResources(String fileName, boolean part) {
		File f = new File(fileName);
		if (!f.canRead()) {
			errDialog.setText(fileName + ":" + RC("no.file.found"));
			errDialog.show();
			return;
		}

		(new Thread(new Loader(fileName, part))).start();
		setTitle(fileName);
		addToPickList(fileName);
	}

	private void initControls() {
		IELabel commLab = new IELabel(RC("tools.translator.label.comments"));
		constrain(textPanel, commLab, 0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0.0, 0.0, 10, 3, 0, 15);

		commField = new EmulatedTextField();
		commField.setBackground(Color.lightGray);
		constrain(textPanel, commField, 1, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 1.0, 1.0, 3, 3, 5, 15);

		dropComment = new Button(RC("tools.translator.label.dropcomment"));
		constrain(textPanel, dropComment, 2, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0.0, 0.0, 3, 3, 5, 15);

		keynLab = new IELabel("");
		constrain(textPanel, keynLab, 0, 1, 3, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1.0, 0.0, 10, 3, 0, 15);
		langMenu.enable();
	}

	private void syncLanguage(String lang) {
		LangItem lang2 = bundle.getBundle().getLanguage(lang);
		int i = bundle.getBundle().getLangIndex(lang);

		String langLab = lang2.getLangDescription();
		LangState ls = new LangState();
		ls.name = lang2.getLangId();
		ls.box = new CheckboxMenuItem(langLab, false);
		ls.box.setState(true);
		ls.label = new IELabel(langLab + ":", IELabel.LEFT);
		ls.tf = new TextAreaWrap();
		ls.tf.getControl().setBackground(Color.white);
		ls.tf.setLocale(new Locale(lang, ""));
		ls.tf.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent ke) {
				if (!ke.isActionKey() && (ke.getKeyChar() == '\n')) {
					invokeAutoFit();
				}
				checkForScrolling(ke.getComponent());
			}
		});

		langStates.add(ls);
		langMenu.add(ls.box);

		constrain(textPanel, ls.label, 0, i + 2, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0.0, 0.0, 10, 3, 0, 3);
		constrain(textPanel, ls.tf.getControl(), 1, i + 2, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 1.0, 1.0, 3,
			3, 5, 3);
	}

	private void addToTree(String s) {
		if (tree.getNode(s) != null) {
			return;
		}
		int ind = allowDot ? s.lastIndexOf(TranslatorConstants.KEY_SEPARATOR) : -1;
		int ind2 = allowUScore ? s.lastIndexOf(TranslatorConstants.KEY_SEPARATOR_2) : -1;
		if (ind2 > ind) {
			ind = ind2;
		}

		TreeNode tnew = new TreeNode(s, SYS_DIR + TranslatorConstants.OPEN_IMAGE, SYS_DIR + TranslatorConstants.CLOSE_IMAGE);
		if (ind < 0) {
			tnew.caption = s;
			tree.insertRoot(s);
			tnew = tree.getNode(s);
			tnew.setExpandedImage(SYS_DIR + TranslatorConstants.OPEN_IMAGE);
			tnew.setCollapsedImage(SYS_DIR + TranslatorConstants.CLOSE_IMAGE);
		}
		else {
			String tname = s.substring(0, ind);
			addToTree(tname);
			TreeNode ttpar = tree.getNode(tname);
			tnew.caption = s.substring(ind + 1);
			tree.insert(tnew, ttpar, LevelTree.CHILD);
			tnew = tree.getNode(s);
		}
		tnew.setContextMenu(1);
	}

	private String lookupFileForLoad(String mask) {
		FileDialog openFileDialog1 = new FileDialog(this, RC("tools.translator.label.opentitle"), FileDialog.LOAD);
		openFileDialog1.setDirectory(lastDirectory);
		openFileDialog1.setFile(mask);
		openFileDialog1.show();

		String filename = openFileDialog1.getFile();
		if (filename != null) {
			if (keepLastDir) {
				lastDirectory = openFileDialog1.getDirectory();
			}
			return openFileDialog1.getDirectory() + filename;
		}
		return null;
	}

	private String lookupFileForStore(String fileName) {
		FileDialog openFileDialog1 = new FileDialog(this, RC("tools.translator.label.saveastitle"), FileDialog.SAVE);
		openFileDialog1.setDirectory(lastDirectory);
		openFileDialog1.setFile(fileName);
		openFileDialog1.show();

		String filename = openFileDialog1.getFile();
		if ((filename != null) && keepLastDir) {
			lastDirectory = openFileDialog1.getDirectory();
			return openFileDialog1.getDirectory() + filename;
		}
		return filename;
	}

	private void onSaveAs() {
		String fn = bundle.getBundle().getLanguage(0).getLangFile();
		if (fn == null) {
			fn = "autosaved";
		}
		fn += TranslatorConstants.RES_EXTENSION;

		String filename = lookupFileForStore(fn);
		if (filename != null) {
			try {
				bundle.store(filename);
				addToPickList(filename);
				setTitle(filename);
				isDirty = false;
			}
			catch (Exception e) {
				infoException(e);
			}
		}
	}

	private void infoException(Exception e) {
		e.printStackTrace();
		try {
			ByteArrayOutputStream ba = new ByteArrayOutputStream();
			PrintStream p = new PrintStream(ba);
			e.printStackTrace(p);
			p.close();
			String msg = new String(ba.toByteArray(), 0);

			String hdr = e.getMessage() == null ? e.toString() : e.getMessage();
			hdr = hdr + "\n" + msg;
			errDialog.setText(hdr);
			errDialog.show();
		}
		catch (Exception z) {
		}
	}

	private void onAbout() {
		MessageBox2 aboutDialog = new MessageBox2(this);
		aboutDialog.setText(RC("tools.translator.copyright"));
		aboutDialog.setTitle(RC("dialog.title.info"));
		aboutDialog.setIcon(imgres.getImage(SYS_DIR + "ZavalCE.gif", aboutDialog));
		String[] OK_BUT = { RC("dialog.button.ok") };
		aboutDialog.setButtons(OK_BUT);
		aboutDialog.show();
	}

	private void onStatistics() {
		MessageBox2 sDialog = new MessageBox2(this);
		nullsCount = 0;
		notCompletedCount = 0;
		setIndicators(tree.getRootNode());
		String text = RC("tools.translator.label.statistics.lang") + bundle.getBundle().getLangCount() + "\n";
		text = text + RC("tools.translator.label.statistics.record") + bundle.getBundle().getItemCount() + "\n";
		text = text + RC("tools.translator.label.statistics.nulls") + nullsCount + "\n";
		text = text + RC("tools.translator.label.statistics.notcompleted") + notCompletedCount;
		sDialog.setText(text);
		sDialog.setTitle(RC("dialog.title.info"));
		String[] OK_BUT = { RC("dialog.button.ok") };
		sDialog.setButtons(OK_BUT);

		ResultField rf = sDialog.getTextContainer();
		TextAlignArea area = rf.getAlignArea();
		area.setAlign(AlignConstants.FIT | AlignConstants.TOP);
		sDialog.show();
	}

	private void onGenCode() {
		try {
			String fn = (bundle.getBundle().getLangCount() == 0) || (bundle.getBundle().getLanguage(0).getLangFile() == null)
				? "Sample"
				: bundle.baseName(bundle.getBundle().getLanguage(0).getLangFile());
			fn = fn.substring(0, 1).toUpperCase() + fn.substring(1);

			String filename = lookupFileForStore(fn + "ResourceMapped.java");
			if (filename != null) {
				SrcGenerator srcgen = new SrcGenerator(bundle.replace(filename, "\\", "/"));
				srcgen.perform(bundle.getBundle());
			}
		}
		catch (Exception e) {
			infoException(e);
		}
	}

	private void onParseCode() {
		try {
			String mask = "*.java";
			String filename = lookupFileForLoad(mask);
			if (filename != null) {
				filename = bundle.replace(filename, "\\", "/");
				JavaParser parser = new JavaParser(new FileInputStream(filename));
				Map<String, String> ask = parser.parse();
				if (ask.isEmpty()) {
					ask.put("empty", "");
				}

				clear();
				initControls();
				bundle.getBundle().addLanguage("en");
				String rlng = bundle.getBundle().getLanguage(0).getLangId();

				for (Map.Entry<String, String> stringStringEntry : ask.entrySet()) {
					BundleItem bi = bundle.getBundle().addKey(stringStringEntry.getKey());
					bi.setTranslation(rlng, stringStringEntry.getValue());
				}
				bundle.getBundle().resort();
				initData(false);
				setTitle(filename);
			}
		}
		catch (Exception e) {
			infoException(e);
		}
	}

	private void initData(boolean part) {
		/* Initialize language set */
		for (int i = 0; i < bundle.getBundle().getLangCount(); ++i) {
			LangItem lang2 = bundle.getBundle().getLanguage(i);
			syncLanguage(lang2.getLangId());
		}
		hideTransMenu.setState(false);

		/* Add all keys in tree view ... */
		BundleItem bi = bundle.getBundle().getItem(0);
		addToTree(bi.getId());

		for (int i = 1; i < bundle.getBundle().getItemCount(); ++i) {
			BundleItem bi2 = bundle.getBundle().getItem(i);
			addToTree(bi2.getId());
			if ((i % 250) == 0) {
				sbl2.setText("    " + i + " " + RC("tools.translator.progress.addkeys"));
				sbl2.repaint();
			}
		}
		setAllIndicators();
		sbl2.setText("");
		sbl2.repaint();
		setCursor(Cursor.DEFAULT_CURSOR);

		/* ... and make all keys closed by default */

		/* ... find first key, open it and select */
		if (bundle.getBundle().getItemCount() > 0) {
			String id = bundle.getBundle().getItem(0).getId();
			tree.selectNodeAndOpen(id);
			wasSelectedKey = null;
			setTranslations();
		}

		tree.requestFocus();

		closeMenu.enable();
		saveBundleMenu.enable();
		saveAsBundleMenu.enable();
		genMenu.enable();

		textPanel.invalidate();
		validate();
		repaint();
		syncToolbar();
		if (!part) {
			loadPickList();
		}
	}

	private void invokeAutoFit() {
		if (autoExpandTF) {
			textPanel.invalidate();
			validate();
		}
	}

	private void expand(TreeNode tn) {
		if (tn != null) {
			TreeNode[] children = tree.enumChild(tn);
			if (children != null) {
				for (TreeNode element : children) {
					expand(element);
				}
			}
			tree.openNode(tn.getText());
		}
	}

	private void collapse(TreeNode tn) {
		if (tn != null) {
			TreeNode[] children = tree.enumChild(tn);
			if (children != null) {
				for (TreeNode element : children) {
					collapse(element);
				}
			}
			tree.closeNode(tn.getText());
		}
	}

	private void syncToolbar() {
		for (int j = 0; j < tbar2menu.length; ++j) {
			tool.setEnabled(j, tbar2menu[j].isEnabled());
		}
	}

	private void linkPickList() {
		for (String aPickList : pickList) {
			String patz = stretchPath(aPickList);
			MenuItem item = new MenuItem(patz);
			fileMenu.add(item);
		}
		if (!pickList.isEmpty()) {
			fileMenu.addSeparator();
		}
		fileMenu.add(exitMenu);
	}

	private void removePickList() {
		if (pickList.isEmpty()) {
			return;
		}

		int j;
		String s1 = stretchPath(pickList.get(0));
		for (j = 0; j < fileMenu.countItems(); ++j) {
			String patz = fileMenu.getItem(j).getLabel();
			if (patz.equals(s1)) {
				break;
			}
		}
		pickList = new ArrayList<>();
		if (j >= fileMenu.countItems()) {
			return;
		}
		for (; j < fileMenu.countItems();) {
			fileMenu.remove(j);
		}
	}

	private String stretchPath(String name) {
		if (name.length() < MAX_PICK_LENGTH) {
			return name;
		}
		return name.substring(0, 4) + "..." + name.substring(name.length() - Math.min(name.length() - 7, MAX_PICK_LENGTH - 7));
	}

	private void loadPickList() {
		removePickList();
		try {
			String path = System.getProperty("user.home") + "/.jrc-editor.conf";
			InputIniFile ini = new InputIniFile(path);
			Map<String, String> tbl = ini.getItems();

			for (String key : tbl.keySet()) {
				String val = tbl.get(key);
				if (!key.startsWith("picklist.")) {
					continue;
				}
				try {
					key = key.substring(key.indexOf('.') + 1);
					int pickLevel = Integer.parseInt(key);
					while (pickList.size() <= pickLevel) {
						pickList.add(null);
					}
					pickList.set(pickLevel, val);
				}
				catch (Exception error) {
					error.printStackTrace();
				}
			}

			keepLastDir = (tbl.get("keepLastDir") == null) || "Y".equals(tbl.get("keepLastDir"));
			omitSpaces = (tbl.get("omitSpaces") == null) || "Y".equals(tbl.get("omitSpaces"));
			autoExpandTF = (tbl.get("autoExpandTF") == null) || "Y".equals(tbl.get("autoExpandTF"));
			allowDot = (tbl.get("allowDot") == null) || "Y".equals(tbl.get("allowDot"));
			allowUScore = (tbl.get("allowUScore") == null) || "Y".equals(tbl.get("allowUScore"));

			keepLastDirMenu.setState(keepLastDir);
			omitSpacesMenu.setState(omitSpaces);
			autoExpandTFMenu.setState(autoExpandTF);
			allowDotMenu.setState(allowDot);
			allowUScoreMenu.setState(allowUScore);
		}
		catch (Exception e1) {
		}

		for (int j = 0; j < pickList.size(); ++j) {
			Object obj = pickList.get(j);
			if (obj == null) {
				pickList.remove(j);
				--j;
			}
		}
		linkPickList();
	}

	private void addToPickList(String name) {
		if (name == null) {
			return;
		}
		for (int j = 0; j < pickList.size(); ++j) {
			String v1 = pickList.get(j);
			if (v1.equals(name)) {
				pickList.remove(j);
				--j;
			}
		}

		pickList.add(0, name);
		while (pickList.size() >= 8) {
			pickList.remove(7);
		}
		saveIni();
	}

	private void saveIni() {
		try {
			String path = System.getProperty("user.home") + "/.jrc-editor.conf";
			IniFile ini = new IniFile(path);
			for (int j = 0; j < pickList.size(); ++j) {
				ini.putString("picklist." + j, pickList.get(j));
			}

			ini.putString("keepLastDir", keepLastDir ? "Y" : "N");
			ini.putString("omitSpaces", omitSpaces ? "Y" : "N");
			ini.putString("autoExpandTF", autoExpandTF ? "Y" : "N");
			ini.putString("allowDot", allowDot ? "Y" : "N");
			ini.putString("allowUScore", keepLastDir ? "Y" : "N");
			ini.close();
		}
		catch (Exception e) {
		}
	}

	private String[] getLangSet() {
		LangDialog ed = new LangDialog(this, RC("tools.translator.label.choosetitle"), true, this);
		ed.setLabelCaption(RC("tools.translator.label.chooselabel"));
		ed.setButtonsCaption(RC("dialog.button.ok"), CLOSE_BUTTONS[2]);

		LangItem[] lset = new LangItem[bundle.getBundle().getLangCount()];
		Arrays.setAll(lset, i -> bundle.getBundle().getLanguage(i));
		ed.setList(lset);

		ed.doModal();
		String[] ask = ed.getList();
		if ((ask == null) || (ask.length <= 0) || !ed.isApply()) {
			return null;
		}
		for (int i = 0; i < ask.length; ++i) {
			if (ask[i].indexOf(':') > 0) {
				ask[i] = ask[i].substring(0, ask[i].indexOf(':')).trim();
			}
		}
		return ask;
	}

	private void onOpen(boolean part) {
		String mask = "*" + TranslatorConstants.RES_EXTENSION + ";" + "*" + TranslatorConstants.INI_EXTENSION;
		String filename = lookupFileForLoad(mask);
		if (filename != null) {
			if (!part) {
				clear();
			}
			readResources(filename, part);
		}
	}

	private void onSaveXml(boolean part) {
		String[] parts = part ? getLangSet() : null;
		if (part && ((parts == null) || (parts.length < 2))) {
			return;
		}
		String fn = bundle.getBundle().getLanguage(0).getLangFile();
		if (fn == null) {
			fn = "autosaved";
		}

		String filename = lookupFileForStore(bundle.baseName(fn) + ".xml");
		if (filename != null) {
			try {
				try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
					BundleSet set = bundle.getBundle();
					int items = set.getItemCount();
					out.writeChar((char) 0xFEFF);
					out.writeChars("<xml>\n");
					for (int i = 0; i < items; ++i) {
						BundleItem bi = set.getItem(i);
						out.writeChars("\t<key name=\"" + bi.getId() + "\">\n");
						for (String lang : bi.getLanguages()) {
							if (part && !inArray(parts, lang)) {
								continue;
							}
							out.writeChars("\t\t<value lang=\"" + lang + "\">" + bi.getTranslation(lang) + "</value>\n");
						}
						out.writeChars("\t</key>\n");
					}
					out.writeChars("</xml>\n");
				}
			}
			catch (Exception e) {
				infoException(e);
			}
		}
	}

	private void onSaveUtf(boolean part) {
		String[] parts = part ? getLangSet() : null;
		if (part && ((parts == null) || (parts.length < 2))) {
			return;
		}

		String fn = bundle.getBundle().getLanguage(0).getLangFile();
		if (fn == null) {
			fn = "autosaved";
		}

		String filename = lookupFileForStore(bundle.baseName(fn) + ".txt");
		if (filename != null) {
			try {
				try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
					BundleSet set = bundle.getBundle();
					int items = set.getItemCount();
					out.writeChar((char) 0xFEFF);
					out.writeChars("#JRC Editor: do not modify this line\r\n\r\n");
					for (int i = 0; i < items; ++i) {
						BundleItem bi = set.getItem(i);
						out.writeChars("KEY=\"" + bi.getId() + "\":\r\n");
						for (String lang : bi.getLanguages()) {
							if (part && !inArray(parts, lang)) {
								continue;
							}
							out.writeChars("\t\"" + lang + "\"=\"" + bi.getTranslation(lang) + "\"\r\n");
						}
						out.writeChars("\r\n");
					}
				}
			}
			catch (Exception e) {
				infoException(e);
			}
		}
	}

	private boolean inArray(String[] array, String lang) {
		return Arrays.stream(array).anyMatch(element -> element.equalsIgnoreCase(lang));
	}

	/**
	 * Reading unicode (UCS16) file stream into memory
	 */
	private String getBody(String file) throws IOException {
		StringBuilder buf = new StringBuilder();
		try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
			buf.ensureCapacity(in.available());

			try {
				in.readChar(); // skip UCS16 marker FEFF
				for (;;) {
					buf.append(in.readChar());
				}
			}
			catch (EOFException eof) {
			}
		}
		return buf.toString();
	}

	private void fillTable(Map<String, String> tbl) {
		for (Map.Entry<String, String> stringStringEntry : tbl.entrySet()) {
			StringTokenizer st = new StringTokenizer(stringStringEntry.getKey(), "!");
			String key = st.nextToken();
			if (!st.hasMoreTokens()) {
				continue;
			}
			String lang = st.nextToken();

			if (bundle.getBundle().getLanguage(lang) == null) {
				bundle.getBundle().addLanguage(lang);
			}

			bundle.getBundle().addKey(key);
			bundle.getBundle().updateValue(key, lang, stringStringEntry.getValue());
		}
		bundle.getBundle().resort();
	}

	private void onLoadXml(boolean part) {
		String mask = "*.xml";
		String filename = lookupFileForLoad(mask);
		if (filename != null) {
			if (!part) {
				clear();
			}
			if (!part) {
				initControls();
			}
			bundle.getBundle().addLanguage("en");

			try {
				XmlReader xml = new XmlReader(getBody(filename));
				fillTable(xml.flatten());
			}
			catch (Exception e) {
				infoException(e);
			}
			initData(part);
			setTitle(filename);
		}
	}

	private void onLoadUtf(boolean part) {
		String mask = "*.txt";
		String filename = lookupFileForLoad(mask);
		if (filename != null) {
			if (!part) {
				clear();
			}
			if (!part) {
				initControls();
			}
			bundle.getBundle().addLanguage("en");
			try {
				UtfParser parser = new UtfParser(new StringReader(getBody(filename)));
				Map<String, String> tbl = parser.parse();
				fillTable(tbl);
			}
			catch (Exception e) {
				infoException(e);
			}
			initData(part);
			setTitle(filename);
		}
	}

	private void onNewKey() {
		String title = RC("tools.translator.label.newkeytitle");
		String message = RC("tools.translator.label.insert");
		String text = JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE);
		if (null == text || text.isEmpty()) {
			return;
		}
		keyName.setText(text);
		onInsertKey();
	}

	private void moveFocus() {
		Component p = this;
		while ((p != null) && !(p instanceof Window)) {
			p = p.getParent();
		}
		if (p == null) {
			return;
		}
		Window wnd = (Window) p;

		Component focused = wnd.getFocusOwner();
		int idx = tabOrder.indexOf(focused);
		if (idx >= 0) {
			if ((idx + 1) < tabOrder.size()) {
				Component c = tabOrder.get(idx + 1);
				c.requestFocus();
				return;
			}
		}
		int i;
		for (i = 0; i < langStates.size(); i++) {
			LangState ls = getLangState(i);
			if (ls.hidden) {
				continue;
			}
			if (ls.tf.getControl() == focused) {
				break;
			}
		}

		if (i < langStates.size()) {
			for (++i; i < langStates.size(); ++i) {
				LangState ls = getLangState(i);
				if (ls.hidden) {
					continue;
				}
				ls.tf.requestFocus();
				return;
			}
			tree.requestFocus();
			return;
		}
		for (i = 0; i < langStates.size(); ++i) {
			LangState ls = getLangState(i);
			if (ls.hidden) {
				continue;
			}
			ls.tf.requestFocus();
			return;
		}
		tree.requestFocus();
	}

	private void removeLeafs(String key) {
		// Don't touch hier if key/childs are exists
		if (bundle.getBundle().getItem(key) != null) {
			return;
		}
		TreeNode tn = tree.getNode(key);
		if (tn != null) {
			if ((tree.enumChild(tn) != null) && (tree.enumChild(tn).length > 0)) {
				return;
			}
			tree.remove(key);
		}

		int j1 = allowDot ? key.lastIndexOf(TranslatorConstants.KEY_SEPARATOR) : -1;
		int j2 = allowUScore ? key.lastIndexOf(TranslatorConstants.KEY_SEPARATOR_2) : -1;
		j1 = Math.max(j1, j2);
		if (j1 <= 0) {
			return;
		}
		removeLeafs(key.substring(0, j1));
	}

	private void onRenameKey() {
		String oldKeyName = keyName.getText();
		if (oldKeyName.endsWith(".")) {
			oldKeyName = oldKeyName.substring(0, oldKeyName.length() - 1);
		}

		String title = RC("tools.translator.label.rename.caption");
		String message = RC("tools.translator.label.rename.label");
		String newKeyName = (String) JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE, null, null, oldKeyName);

		if (null == newKeyName || newKeyName.trim().isEmpty()) {
			return;
		}
		if (oldKeyName.equals(newKeyName)) {
			return;
		}

		BundleItem biOldAlone = bundle.getBundle().getItem(oldKeyName);
		List<BundleItem> en = bundle.getBundle().getKeysBeginningWith(oldKeyName);
		Map<String, String> oldValues = new HashMap<>();
		for (BundleItem biOld : en) {
			oldValues.clear();
			String newKey = newKeyName;
			if (biOldAlone == null) {
				newKey = newKeyName + biOld.getId().substring(oldKeyName.length());
			}

			int k = bundle.getBundle().getLangCount();
			if (bundle.getBundle().getItem(newKey) != null) {
				errDialog.setText(RC("tools.translator.label.rename.dup"));
				errDialog.show();
				return;
			}

			// Keep old values
			for (int j = 0; j < k; ++j) {
				String lang = bundle.getBundle().getLanguage(j).getLangId();
				String value = biOld.getTranslation(lang);
				if (value != null) {
					oldValues.put(lang, value);
				}
			}
			bundle.getBundle().removeKey(biOld.getId());

			// Add new key
			keyName.setText(newKey);
			addToTree(newKey);
			BundleItem biNew = bundle.getBundle().addKey(newKey);
			for (int j = 0; j < k; ++j) {
				String lang = bundle.getBundle().getLanguage(j).getLangId();
				String value = oldValues.get(lang);
				if (value != null) {
					biNew.setTranslation(lang, value);
				}
			}
		}
		isDirty = true;

		// Remove old key
		tree.remove(oldKeyName);
		removeLeafs(oldKeyName);
		bundle.getBundle().resort();

		tree.selectNodeAndOpen(newKeyName);
		tree.repaint();
		setTranslations();
		setIndicators(tree.getSelectedNode());
	}

	private void onLoadJar() {
		String mask = "*.jar";
		String filename = lookupFileForLoad(mask);
		if (filename != null) {
			clear();
			initControls();
			bundle.getBundle().addLanguage("en");
			try {
				try (ZipFile zip = new ZipFile(filename)) {
					Enumeration<? extends ZipEntry> en = zip.entries();
					while (en.hasMoreElements()) {
						ZipEntry ze = en.nextElement();
						if (ze.getName().endsWith(".properties")) {
							String lang = bundle.determineLanguage(ze.getName());
							InputStream in = zip.getInputStream(ze);
							bundle.appendResource(in, lang);
						}
					}
					initData(false);
					// Force new file name for storage
					bundle.getBundle().getLanguage(0).setLangFile(null);
					setTitle(filename);
				}
			}
			catch (Exception e) {
				infoException(e);
			}
		}
	}

	private void hideTranslated(boolean hide) {
		hideTranslated(tree.getRootNode(), hide);
		tree.invalidate();
		validate();
		tree.repaint();
	}

	private void hideTranslated(TreeNode tn, boolean hide) {
		while (tn != null) {
			if (tn.getIndicator() == null) {
				tn.setHide(hide);
			}
			hideTranslated(tn.child, hide);
			tn = tn.sibling;
		}
	}

	private void constrain(Container c, Component p, int x, int y, int width, int height, int anchor, int fill, double weightx,
		double weighty, int insetLeft, int insetTop, int insetRight, int insetBottom) {
		GridBagConstraints cc = new GridBagConstraints();

		cc.gridx = x;
		cc.gridy = y;
		cc.gridwidth = width;
		cc.gridheight = height;

		cc.fill = fill;
		cc.anchor = anchor;
		cc.weightx = weightx;
		cc.weighty = weighty;

		if ((insetTop + insetBottom + insetLeft + insetRight) > 0) {
			cc.insets = new Insets(insetTop, insetLeft, insetBottom, insetRight);
		}
		LayoutManager lm = c.getLayout();
		GridBagLayout gbl = (GridBagLayout) lm;
		gbl.setConstraints(p, cc);
		c.add(p);
	}

	private boolean match_regex(String mask, String val, boolean matchCase) {
		try {
			RE re = new RE(mask, matchCase ? RE.MATCH_NORMAL : RE.MATCH_CASEINDEPENDENT);
			return re.match(val);
		}
		catch (RESyntaxException e) {
			infoException(e);
		}
		return false;
	}

	private boolean match_mask(String mask, String val, boolean matchCase) {
		return match_mask(mask.toCharArray(), 0, val.toCharArray(), 0, matchCase);
	}

	private boolean match_mask(char[] s, int sp, char[] t, int tp, boolean matchCase) {

		if ((sp == s.length) && (tp == t.length)) {
			return true;
		}
		if ((tp == t.length) && (s[sp] == '*')) {
			return match_mask(s, sp + 1, t, tp, matchCase);
		}
		if ((tp == t.length) && (sp != s.length)) {
			return false;
		}
		if ((sp == s.length) && (tp != t.length)) {
			return false;
		}

		if (s[sp] == '?') {
			return match_mask(s, sp + 1, t, tp + 1, matchCase);
		}
		if (!matchCase && (Character.toLowerCase(s[sp]) == Character.toLowerCase(t[tp]))) {
			return match_mask(s, sp + 1, t, tp + 1, false);
		}
		if (matchCase && (s[sp] == t[tp])) {
			return match_mask(s, sp + 1, t, tp + 1, true);
		}

		if ((s[sp] != '?') && (s[sp] != '*')) {
			if (!matchCase && (Character.toLowerCase(s[sp]) != Character.toLowerCase(t[tp]))) {
				return false;
			}
			if (matchCase && (s[sp] != t[tp])) {
				return false;
			}
		}
		if ((s[sp] == '*') && (s.length == (sp + 1))) {
			return true;
		}
		return IntStream.range(tp, t.length).anyMatch(vp -> match_mask(s, sp + 1, t, vp, matchCase))
			|| match_mask(s, sp + 1, t, tp, matchCase);
	}

	private void checkForScrolling(Component what) {
		if (what instanceof EmulatedTextField) {
			Rectangle r1 = what.getBounds();
			Rectangle r2 = ((EmulatedTextField) what).getCursorShape();
			if ((r1 == null) || (r2 == null)) {
				return;
			}
			r1.x += r2.x;
			r1.y += r2.y;
			r1.width = r2.width + 25;
			r1.height = r2.height + 25;

			// Now R1 contains top/right cursor position
			Dimension s1 = scrPanel.size();
			s1.width -= scrPanel.getVScrollbar().isVisible() ? scrPanel.getVScrollbar().size().width : 0;
			s1.height -= scrPanel.getHScrollbar().isVisible() ? scrPanel.getHScrollbar().size().height : 0;
			// Now s1 contains view rect area

			int curHS = scrPanel.getHScrollbar().isVisible() ? scrPanel.getHScrollbar().getValue() : 0;
			int curVS = scrPanel.getVScrollbar().isVisible() ? scrPanel.getVScrollbar().getValue() : 0;

			if (((r1.x + r1.width) >= (s1.width + curHS))
				|| ((r1.y + r1.height) >= (s1.height + curVS))
				|| (r1.x < curHS)
				|| (r1.y < curVS)) {

				int newX = curHS;
				Dimension s2 = scrPanel.getScrollableObject().preferredSize();
				if ((r1.x + r1.width) >= s1.width) {
					newX = Math.min((r1.x + r1.width) - s1.width, s2.width - s1.width);
				}
				int newY = curVS;
				if ((r1.y + r1.height) >= s1.height) {
					newY = Math.min((r1.y + r1.height) - s1.height, s2.height - s1.height);
				}
				if (r1.x < curHS) {
					newX = r1.x;
				}
				if (r1.y < curVS) {
					newX = r1.y;
				}
				scrPanel.scroll(newX, newY);
			}
		}
	}
}
