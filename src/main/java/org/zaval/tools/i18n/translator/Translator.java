/*
 * Copyright (C) 2001-2002  Zaval Creative Engineering Group (http://www.zaval.org)
 * Copyright (C) 2019 Christoph Obexer <cobexer@gmail.com>
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

import static org.zaval.ui.UiUtils.constrain;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.text.JTextComponent;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.zaval.awt.ToolkitResolver;
import org.zaval.io.IniFile;
import org.zaval.io.InputIniFile;
import org.zaval.tools.i18n.translator.generated.JavaParser;
import org.zaval.tools.i18n.translator.generated.UtfParser;
import org.zaval.ui.AboutDialog;
import org.zaval.ui.TranslationTree;
import org.zaval.ui.TranslationTreeNode;
import org.zaval.util.LambdaUtils;
import org.zaval.util.SafeResourceBundle;

@SuppressWarnings("serial")
class Translator extends JFrame {
	private JOptionPane repDialog;

	private JTextField keyName;
	private JLabel keynLab;
	private TranslationTree tree;
	private JPanel textPanel;
	private final Map<String, LangState> langStates = new LinkedHashMap<>();
	private String lastDirectory = ".";

	// Options
	private boolean keepLastDir = true; // Keep last directory
	private boolean omitSpaces = true; // remove spaces in keys
	private boolean autoExpandTF = true; // auto-expand text areas
	private boolean allowDot = true;
	private boolean allowUScore = true;

	private JMenuItem saveBundleMenu;
	private JMenuItem saveAsBundleMenu;
	private JMenuItem genMenu;
	private JMenuItem closeMenu;
	private JMenuItem exitMenu;
	private JMenu langMenu;
	private JMenu fileMenu;
	private JCheckBoxMenuItem hideTransMenu;
	private JCheckBoxMenuItem showNullsMenu;

	// Options
	private JCheckBoxMenuItem keepLastDirMenu;
	private JCheckBoxMenuItem omitSpacesMenu;
	private JCheckBoxMenuItem autoExpandTFMenu;
	private JCheckBoxMenuItem allowDotMenu;
	private JCheckBoxMenuItem allowUScoreMenu;

	private JTextField commField;
	private JLabel sbl1;
	private JLabel sbl2;

	private ToolkitResolver imgres;
	private boolean exitInitiated = true;
	private boolean isDirty;
	private String wasSelectedKey;
	private String SYS_DIR;
	private BundleManager bundle = new BundleManager();
	private final JPanel pane = new JPanel();

	private final String[] CLOSE_BUTTONS = new String[3];
	private final String[] REPLACE_BUTTONS = new String[3];

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
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
		});
		SYS_DIR = s;
		rcTable = res;

		CLOSE_BUTTONS[0] = RC("dialog.button.yes");
		CLOSE_BUTTONS[1] = RC("dialog.button.no");
		CLOSE_BUTTONS[2] = RC("dialog.button.cancel");

		REPLACE_BUTTONS[0] = RC("dialog.button.yes");
		REPLACE_BUTTONS[1] = RC("dialog.button.no");
		REPLACE_BUTTONS[2] = RC("dialog.button.cancel");

		imgres = new ToolkitResolver();
		this.setLayout(new BorderLayout(0, 0));
		add("Center", pane);

		JButton newBundleToolButton = createToolBarButton(this::onNewBundle, SYS_DIR + "new.gif", RC("tools.translator.menu.new.bundle"));
		JButton openBundleToolButton = createToolBarButton(this::onLoadBundle, SYS_DIR + "load.gif", RC("tools.translator.menu.open"));
		JButton saveBundleToolButton = createToolBarButton(this::onSave, SYS_DIR + "save.gif", RC("tools.translator.menu.save"));
		JButton saveAsToolButton = createToolBarButton(this::onSaveAs, SYS_DIR + "saveas.gif", RC("tools.translator.menu.saveas"));
		JButton genToolButton = createToolBarButton(this::onGenCode, SYS_DIR + "deploy.gif", RC("tools.translator.menu.generate"));
		JButton parseToolButton = createToolBarButton(this::onParseCode, SYS_DIR + "import.gif", RC("tools.translator.menu.parse"));
		JButton newLangToolButton = createToolBarButton(this::onNewResource, SYS_DIR + "newlang.gif", RC("tools.translator.menu.new.lang"));
		JButton delToolButton = createToolBarButton(this::onDeleteKey, SYS_DIR + "del.gif", RC("tools.translator.menu.delete"));
		JButton aboutToolButton = createToolBarButton(this::onAbout, SYS_DIR + "about.gif", RC("menu.about"));

		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.setRollover(true);
		tool.add(new JLabel(RC("menu.file") + ":"));
		tool.add(newBundleToolButton);
		tool.add(openBundleToolButton);
		tool.add(saveBundleToolButton);
		tool.add(saveAsToolButton);
		tool.addSeparator();
		tool.add(genToolButton);
		tool.add(parseToolButton);
		tool.addSeparator();
		tool.add(new JLabel(RC("menu.edit") + ":"));
		tool.add(newLangToolButton);
		tool.add(delToolButton);
		tool.addSeparator();
		tool.add(new JLabel(RC("menu.help") + ": "));
		tool.add(aboutToolButton);
		add("North", tool);

		setIconImage(imgres.getImage(SYS_DIR + "jrc-editor.gif"));

		JPanel panel3 = new JPanel();
		panel3.setPreferredSize(new Dimension(0, (int) (panel3.getFontMetrics(panel3.getFont()).getHeight() * 1.5f)));
		panel3.setLayout(new GridBagLayout());
		panel3.setBorder(new BevelBorder(BevelBorder.LOWERED));

		sbl1 = new JLabel();
		JPanel sbl1Panel = new JPanel();
		sbl1Panel.setPreferredSize(new Dimension(2, 0));
		sbl1Panel.setLayout(new GridLayout(1, 1));
		sbl1Panel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		sbl1Panel.add(sbl1);
		sbl2 = new JLabel();
		JPanel sbl2Panel = new JPanel();
		sbl2Panel.setPreferredSize(new Dimension(8, 0));
		sbl2Panel.setLayout(new GridLayout(1, 1));
		sbl2Panel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		sbl2Panel.add(sbl2);

		constrain(panel3, sbl1Panel, 0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 0.2, 1.0, 0, 0, 0, 0);
		constrain(panel3, sbl2Panel, 1, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 0.8, 1.0, 0, 0, 0, 0);
		add("South", panel3);

		tree = new TranslationTree(imgres.getImageIcon(SYS_DIR + TranslatorConstants.WARN_IMAGE));
		tree.addSelectionListener(this::onTreeSelectionchanged);
		tree.addKeyListener(this::onTreeKeyEvent);

		JPopupMenu ctNodeMenu = new JPopupMenu("");
		JMenuItem ctNewMenu = createMenuItem(this::onNewKey, RC("tools.translator.menu.insert"));
		ctNodeMenu.add(ctNewMenu);
		JMenuItem ctNodeExpandMenu = createMenuItem(this::onExpandTreeNode, RC("tools.translator.menu.expand"));
		ctNodeMenu.add(ctNodeExpandMenu);
		JMenuItem ctNodeCollapseMenu = createMenuItem(this::onCollapseTreeNode, RC("tools.translator.menu.collapse"));
		ctNodeMenu.add(ctNodeCollapseMenu);
		JMenuItem ctNodeDeleteMenu = createMenuItem(this::onDeleteKey, RC("tools.translator.menu.delete"));
		ctNodeMenu.add(ctNodeDeleteMenu);
		JMenuItem ctNodeRenameMenu = createMenuItem(this::onRenameKey, RC("tools.translator.menu.rename"));
		ctNodeMenu.add(ctNodeRenameMenu);
		tree.setComponentPopupMenu(ctNodeMenu);

		pane.setLayout(new BorderLayout());
		JSplitPane mainPanel = new JSplitPane();
		mainPanel.setDividerSize(3);
		mainPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		JPanel keyPanel = new JPanel(new GridBagLayout());

		JLabel keyLabel = new JLabel(RC("tools.translator.label.key"));
		constrain(keyPanel, keyLabel, 0, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE, 0.0, 0.0, 5, 5, 5, 5);

		keyName = new JTextField();
		constrain(keyPanel, keyName, 1, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 1.0, 1.0, 5, 5, 5, 5);

		JButton keyInsertButton = createButton(this::onInsertKey, RC("tools.translator.label.insert"));
		constrain(keyPanel, keyInsertButton, 2, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0.0, 0.0, 5, 5, 5, 5);

		JButton keyDeleteButton = createButton(this::onDeleteKey, RC("tools.translator.label.delete"));
		constrain(keyPanel, keyDeleteButton, 3, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0.0, 0.0, 5, 5, 5, 5);

		pane.add(keyPanel, "South");
		pane.add(mainPanel, "Center");
		textPanel = new JPanel();
		JScrollPane scrPane = new JScrollPane(textPanel);
		mainPanel.setLeftComponent(tree.getComponent());
		mainPanel.setRightComponent(scrPane);
		GridBagLayout textLayout = new GridBagLayout();
		textPanel.setLayout(textLayout);

		JMenuBar menuBar = new JMenuBar();
		fileMenu = new JMenu(RC("menu.file"));
		JMenuItem newBundleMenu = createMenuItem(this::onNewBundle, RC("tools.translator.menu.new.bundle"), KeyEvent.VK_N);
		JMenuItem openBundleMenu = createMenuItem(this::onLoadBundle, RC("tools.translator.menu.open"), KeyEvent.VK_O);
		saveBundleMenu = createMenuItem(this::onSave, RC("tools.translator.menu.save"), KeyEvent.VK_S);
		saveBundleMenu.addChangeListener(e -> saveBundleToolButton.setEnabled(saveBundleMenu.isEnabled()));
		saveBundleMenu.setEnabled(false);
		saveAsBundleMenu = createMenuItem(this::onSaveAs, RC("tools.translator.menu.saveas"));
		saveAsBundleMenu.addChangeListener(e -> saveAsToolButton.setEnabled(saveAsBundleMenu.isEnabled()));
		saveAsBundleMenu.setEnabled(false);
		closeMenu = createMenuItem(this::onCloseMenu, RC("tools.translator.menu.close"));
		closeMenu.setEnabled(false);
		exitMenu = createMenuItem(this::onClose, RC("menu.exit"));

		JMenu editMenu = new JMenu(RC("menu.edit"));

		JMenuItem editCopyMenu = createMenuItem(this::onCopy, RC("tools.translator.menu.edit.copy"));
		editCopyMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		JMenuItem editCutMenu = createMenuItem(this::onCut, RC("tools.translator.menu.edit.cut"));
		editCutMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
		JMenuItem editPasteMenu = createMenuItem(this::onPaste, RC("tools.translator.menu.edit.paste"));
		editPasteMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
		JMenuItem editDeleteMenu = createMenuItem(this::onDelete, RC("tools.translator.menu.edit.delete"));
		editDeleteMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		JMenuItem searchMenu = createMenuItem(this::onSearch, RC("menu.search"));
		searchMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
		JMenuItem searchAgainMenu = createMenuItem(this::onSearchAgain, RC("menu.searchagain"), KeyEvent.VK_F);
		searchAgainMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		JMenuItem replaceToMenu = createMenuItem(this::onReplace, RC("menu.replace"));

		JMenuItem newLangMenu = createMenuItem(this::onNewResource, RC("tools.translator.menu.new.lang"), KeyEvent.VK_L);
		JMenuItem delMenu = createMenuItem(this::onDeleteKey, RC("tools.translator.menu.delete"), KeyEvent.VK_D);
		JMenuItem insMenu = createMenuItem(this::onNewKey, RC("tools.translator.menu.insert"), KeyEvent.VK_I);
		JMenuItem renMenu = createMenuItem(this::onRenameKey, RC("tools.translator.menu.rename"), KeyEvent.VK_R);

		JMenu treeMenu = new JMenu(RC("menu.tree"));
		JMenuItem expandNodeMenu = createMenuItem(this::onExpandTreeNode, RC("tools.translator.menu.node.expand") /*, KeyEvent.VK_PLUS)*/);
		JMenuItem collapseNodeMenu = createMenuItem(this::onCollapseTreeNode,
			RC("tools.translator.menu.node.collapse") /*, KeyEvent.VK_MINUS)*/);
		JMenuItem expandTreeMenu = createMenuItem(this::onExpandAllTreeNodes, RC("tools.translator.menu.expand"));
		JMenuItem collapseTreeMenu = createMenuItem(this::onCollapseAllTreeNodes, RC("tools.translator.menu.collapse"));
		hideTransMenu = createCheckBoxMenuItem(this::onToggleHideTranslated, RC("tools.translator.menu.hide.completed"));

		JMenu viewMenu = new JMenu(RC("menu.options"));
		JMenuItem statisticsMenu = createMenuItem(this::onStatistics, RC("tools.translator.menu.statistics"));
		showNullsMenu = createCheckBoxMenuItem(this::onShowNulls, RC("tools.translator.menu.nulls"), false);
		langMenu = new JMenu(RC("tools.translator.menu.showres"));
		langMenu.setEnabled(false);
		JMenu optionsMenu = new JMenu(RC("tools.translator.menu.options"));
		keepLastDirMenu = createCheckBoxMenuItem(this::onToggleKeepLastDir, RC("tools.translator.menu.options.keeplastdir"), true);
		omitSpacesMenu = createCheckBoxMenuItem(this::onToggleOmitSpaces, RC("tools.translator.menu.options.omitspaces"), true);
		autoExpandTFMenu = createCheckBoxMenuItem(this::onToggleAutoExpandTF, RC("tools.translator.menu.options.autofit"), true);
		allowDotMenu = createCheckBoxMenuItem(this::onToggleAllowDot, RC("tools.translator.menu.options.allowdot"), true);
		allowUScoreMenu = createCheckBoxMenuItem(this::onToggleAllowUnderscore, RC("tools.translator.menu.options.allowuscore"), true);
		omitSpacesMenu.setEnabled(false);

		JMenu helpMenu = new JMenu(RC("menu.help"));
		JMenuItem aboutMenu = createMenuItem(this::onAbout, RC("menu.about"));

		JMenu toolMenu = new JMenu(RC("tools.translator.menu.tools"));
		genMenu = createMenuItem(this::onGenCode, RC("tools.translator.menu.generate"));
		genMenu.addChangeListener(e -> genToolButton.setEnabled(genMenu.isEnabled()));
		genMenu.setEnabled(false);
		JMenuItem parseMenu = createMenuItem(this::onParseCode, RC("tools.translator.menu.parse"));
		JMenuItem saveXmlBundleMenu = createMenuItem(() -> onSaveXml(false), RC("tools.translator.menu.save.xml"));
		JMenuItem saveUtfBundleMenu = createMenuItem(() -> onSaveUtf(false), RC("tools.translator.menu.save.utf"));
		JMenuItem loadXmlBundleMenu = createMenuItem(() -> onLoadXml(false), RC("tools.translator.menu.load.xml"));
		JMenuItem loadUtfBundleMenu = createMenuItem(() -> onLoadUtf(false), RC("tools.translator.menu.load.utf"));

		JMenuItem saveXmlBundleMenuP = createMenuItem(() -> onSaveXml(true), RC("tools.translator.menu.save.xml.part"));
		JMenuItem saveUtfBundleMenuP = createMenuItem(() -> onSaveUtf(true), RC("tools.translator.menu.save.utf.part"));
		JMenuItem loadXmlBundleMenuP = createMenuItem(() -> onLoadXml(true), RC("tools.translator.menu.load.xml.part"));
		JMenuItem loadUtfBundleMenuP = createMenuItem(() -> onLoadUtf(true), RC("tools.translator.menu.load.utf.part"));
		JMenuItem openBundleMenuP = createMenuItem(() -> onOpen(true), RC("tools.translator.menu.load.part"));

		JMenuItem loadJarMenu = createMenuItem(this::onLoadJar, RC("tools.translator.menu.load.jar"));

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
		setJMenuBar(menuBar);

		String title = RC("dialog.title.warning");
		repDialog = new JOptionPane("", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, REPLACE_BUTTONS,
			REPLACE_BUTTONS[0]);
		JDialog dlg = repDialog.createDialog(this, title);
		dlg.setModal(false);
		dlg.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				onReplaceDialogClosed();
			}
		});
	}

	private JButton createToolBarButton(Runnable callback, String icon, String tooltip) {
		JButton button = new JButton(imgres.getImageIcon(icon));
		button.addActionListener(e -> callback.run());
		button.setToolTipText(tooltip);
		return button;
	}

	private void onToggleAllowUnderscore() {
		allowUScore = allowUScoreMenu.getState();
	}

	private void onToggleAllowDot() {
		allowDot = allowDotMenu.getState();
	}

	private void onToggleAutoExpandTF() {
		autoExpandTF = autoExpandTFMenu.getState();
	}

	private void onToggleOmitSpaces() {
		omitSpaces = omitSpacesMenu.getState();
	}

	private void onToggleKeepLastDir() {
		keepLastDir = keepLastDirMenu.getState();
	}

	private void onShowNulls() {
		setIndicators(tree.getRootNode());
		tree.repaint();
	}

	private void onToggleHideTranslated() {
		hideTranslated(hideTransMenu.getState());
		updateStatusBar();
	}

	private void onCollapseAllTreeNodes() {
		tree.setAllExpandedState(false);
		tree.repaint();
	}

	private void onExpandAllTreeNodes() {
		tree.setAllExpandedState(true);
		tree.repaint();
	}

	private void onCloseMenu() {
		exitInitiated = false;
		onClose();
	}

	private void onCollapseTreeNode() {
		collapse(tree.getSelectedNode());
	}

	private void onExpandTreeNode() {
		expand(tree.getSelectedNode());
	}

	private JMenuItem createMenuItem(Runnable callback, String text, int mnemonic) {
		JMenuItem item = new JMenuItem(text, mnemonic);
		item.addActionListener(e -> callback.run());
		return item;
	}

	private JMenuItem createMenuItem(Runnable callback, String text) {
		return createMenuItem(callback, text, 0);
	}

	private JCheckBoxMenuItem createCheckBoxMenuItem(Runnable callback, String text, boolean defaultState) {
		JCheckBoxMenuItem item = new JCheckBoxMenuItem(text, defaultState);
		item.addActionListener(e -> callback.run());
		return item;
	}

	private JCheckBoxMenuItem createCheckBoxMenuItem(Runnable callback, String text) {
		return createCheckBoxMenuItem(callback, text, true);
	}

	private JButton createButton(Runnable callback, String text) {
		JButton item = new JButton(text);
		item.addActionListener(e -> callback.run());
		return item;
	}

	private void onTreeSelectionchanged(TranslationTreeNode newSelectedNode) {
		String newKey = null != newSelectedNode ? newSelectedNode.getText() : null;
		if (!Objects.equals(wasSelectedKey, newKey)) {
			setTranslations(newKey);
			invokeAutoFit();
		}
	}

	private void onTreeKeyEvent(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			onDeleteKey();
		}
	}

	private void onReplaceDialogClosed() {
		Object value = repDialog.getValue();
		if (Objects.equals(value, REPLACE_BUTTONS[0])) {
			makeReplaceImpl();
		}
		else if (Objects.equals(value, REPLACE_BUTTONS[2])) {
			replaceTo = null;
		}
		updateStatusBar();
	}

	private void updateStatusBar() {
		int visLangCount = getVisLangCount();
		int languageCount = bundle.getBundle().getLanguageCount();
		int itemCount = bundle.getBundle().getItemCount();
		sbl1.setText(MessageFormat.format(" {0}/{1}, {2} ", visLangCount, languageCount, itemCount));
	}

	private void onDelete() {
		Component ccur = getFocusOwner();
		if (ccur instanceof JTextComponent) {
			JTextComponent cur = (JTextComponent) ccur;
			if (cur.getSelectionStart() > 0) {
				cur.replaceSelection("");
			}
		}
	}

	private void onPaste() {
		Component ccur = getFocusOwner();
		if (ccur instanceof JTextComponent) {
			((JTextComponent) ccur).paste();
		}
	}

	private void onCut() {
		Component ccur = getFocusOwner();
		if (ccur instanceof JTextComponent) {
			((JTextComponent) ccur).cut();
		}
	}

	private void onCopy() {
		Component ccur = getFocusOwner();
		if (ccur instanceof JTextComponent) {
			((JTextComponent) ccur).copy();
		}
	}

	private void setTranslations() {
		String newKey = tree.getSelectedText();
		setTranslations(newKey);
	}

	private void setTranslations(String newKey) {
		if (wasSelectedKey != null) {
			for (LangState ls : langStates.values()) {
				if (ls.hidden) {
					continue;
				}
				String trans = ls.tf.getText();
				BundleItem bi = bundle.getBundle().getItem(wasSelectedKey);
				if (bi == null) {
					// do not add the translation implicitly - disable
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
		for (LangState ls : langStates.values()) {
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
		String text = bundle.replace(RC("tools.translator.message.illchar"), "[%illchar%]", illegalChar);
		JOptionPane.showMessageDialog(this, text, RC("dialog.title.warning"), JOptionPane.WARNING_MESSAGE);
		return null;
	}

	private void onInsertKey() {
		if (bundle.getBundle().hasLanguages()) {
			String key = getValidKey();
			if (key != null) {
				addToTree(key);
				bundle.getBundle().addKey(key);
				commField.setText("");
				isDirty = true;
				tree.selectNode(key);
				tree.repaint();
				setTranslations();
				saveBundleMenu.setEnabled(true);
				saveAsBundleMenu.setEnabled(true);
				genMenu.setEnabled(true);
				setIndicators(tree.getRootNode());
				isDirty = true;

				textPanel.invalidate();
				validate();
			}
			updateStatusBar();
		}
	}

	private void onDeleteKey() {
		String key = tree.getSelectedText();
		if (key == null) {
			return;
		}
		TranslationTreeNode tn = tree.getNode(key);
		if (tn == null) {
			return;
		}
		boolean hasChilds = tree.enumChild(tn).length != 0;
		BundleItem bi = bundle.getBundle().getItem(key);
		boolean doDeleteThis = false;
		boolean doDeleteAll = false;
		String title = RC("dialog.title.warning");
		String message = bundle.replace(RC("tools.translator.message.delkey"), "[%key%]", key);
		if ((bi != null) && hasChilds) {
			String[] options = { RC("dialog.button.delete.all"), RC("dialog.button.delete.this"), RC("dialog.button.cancel") };
			int selection = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			doDeleteAll = 0 == selection;
			doDeleteThis = 1 == selection;
		}
		else if ((bi != null) && !hasChilds) {
			String[] options = { RC("dialog.button.delete.this"), RC("dialog.button.cancel") };
			int selection = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
				options, options[0]);
			doDeleteThis = 0 == selection;
		}
		else if (bi == null) {
			String[] options = { RC("dialog.button.delete.all"), RC("dialog.button.cancel") };
			int selection = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
				options, options[0]);
			doDeleteAll = 0 == selection;
		}

		if (doDeleteAll) {
			isDirty = true;
			TranslationTreeNode parentNode = tn.getParent();
			TranslationTreeNode nextToSelect = tn.getPreviousSibling();
			if (null == nextToSelect) {
				nextToSelect = tn.getNextSibling();
			}
			if (null == nextToSelect) {
				nextToSelect = parentNode;
			}
			bundle.getBundle().removeKeysBeginningWith(key);

			tree.remove(key); // kill children
			removeLeafs(key); // clean leafs out of model
			adjustIndicator(parentNode);
			tree.selectNode(nextToSelect);
			tree.repaint();
			wasSelectedKey = null;
			setTranslations();
			updateStatusBar();
		}
		else if (doDeleteThis) {
			isDirty = true;
			// Not an leaf => don't touch tree but update model
			bundle.getBundle().removeKey(key);
			TranslationTreeNode parentNode = tn.getParent();
			TranslationTreeNode nextToSelect = tn;
			if (tree.enumChild(tn).length == 0) {
				nextToSelect = tn.getPreviousSibling();
				if (null == nextToSelect) {
					nextToSelect = tn.getNextSibling();
				}
				if (null == nextToSelect) {
					nextToSelect = parentNode;
				}
				tree.remove(key);
			}
			tree.selectNode(nextToSelect);

			adjustIndicator(parentNode);
			tree.repaint();

			wasSelectedKey = null;
			setTranslations();
			textPanel.invalidate();
			validate();
			updateStatusBar();
		}
	}

	private void onClose() {
		setTranslations();
		if (isDirty) {
			String message = RC("tools.translator.message.save");
			String title = RC("dialog.title.warning");
			int result = JOptionPane.showOptionDialog(this, message, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
				null, CLOSE_BUTTONS, CLOSE_BUTTONS[2]);
			if (0 == result) {
				onSave();
			}
			if (2 != result) {
				if (exitInitiated) {
					finish();
				}
				else {
					clear();
				}
				exitInitiated = true;
			}
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
		setVisible(false);
		saveIni();
		dispose();
	}

	private void onSave() {
		LangItem firstLanguage = bundle.getBundle().getFirstLanguage();
		if (null != firstLanguage) {
			String fn = firstLanguage.getFileName();
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
		tree.removeAll();
		tree.repaint();
		textPanel.invalidate();
		validate();
		isDirty = false;
		bundle = new BundleManager();
		langStates.clear();

		closeMenu.setEnabled(false);
		saveBundleMenu.setEnabled(false);
		saveAsBundleMenu.setEnabled(false);
		genMenu.setEnabled(false);
		langMenu.setEnabled(false);
	}

	private int getVisLangCount() {
		return (int) langStates.values().stream().filter(ls -> !ls.hidden).count();
	}

	private void setAllIndicators() {
		for (LangState ls : langStates.values()) {
			ls.hidden = false;
			ls.box.setState(true);
		}
		// Fire it async as it take large time
		hideTransMenu.setEnabled(false);
		(new Thread(this::setIndicatorsInit)).start();
	}

	private void setIndicatorsInit() {
		sbl2.setText(RC("tools.translator.progress.indicator"));
		sbl2.repaint();
		setIndicators(tree.getRootNode());
		hideTransMenu.setEnabled(true);
		sbl2.setText("");
		sbl2.repaint();
	}

	private boolean setIndicators(TranslationTreeNode tn) {
		if (tn == null) {
			return false;
		}
		boolean res = setIndicators(tn.getNextSibling());
		return setIndicator(tn, setIndicators(tn.getFirstChild())) || res;
	}

	private boolean setIndicator(TranslationTreeNode tn, boolean childOn) {
		if (tn == null) {
			return false;
		}
		if (getVisLangCount() < 2) {
			tn.setShowIndicator(false);
			return false;
		}
		if (childOn) {
			tn.setShowIndicator(true);
			return true;
		}

		BundleItem bi = bundle.getBundle().getItem(tn.getText());
		if (bi == null) {
			tn.setShowIndicator(false);
			return false;
		}
		boolean isPres = false;
		boolean isAbs = false;
		for (LangState ls : langStates.values()) {
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
		tn.setShowIndicator(false);
		if (isAbs && isPres) {
			tn.setShowIndicator(true);
			notCompletedCount++;
		}
		else {
			if (isAbs) {
				nullsCount++;
				if (showNullsMenu.getState()) {
					tn.setShowIndicator(true);
					return true;
				}
			}
		}
		return isAbs && isPres;
	}

	private void adjustIndicator(TranslationTreeNode tn) {
		if (tn == null) {
			return;
		}
		setIndicator(tn, isSetInSiblings(tn.getFirstChild()));
		adjustIndicator(tn.getParent());
	}

	private boolean isSetInSiblings(TranslationTreeNode tn) {
		if (tn == null) {
			return false;
		}
		if (tn.isShowIndicator()) {
			return true;
		}
		return isSetInSiblings(tn.getNextSibling());
	}

	private void onSearch() {
		SearchDialog ed = new SearchDialog(this, RC("tools.translator.label.search.caption"), true);
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
		updateStatusBar();
	}

	private void onReplace() {
		ReplaceDialog ed = new ReplaceDialog(this, RC("tools.translator.label.replace.caption"), true);
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
		String lang = curLangForReplace.getId();
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
			LangState ls = langStates.get(lang);
			if (ls != null) {
				ls.tf.setText(val);
				ls.tf.requestFocusInWindow();
			}
		}
		isDirty = true;
		updateStatusBar();
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
			JDialog dlg = ((JDialog) repDialog.getTopLevelAncestor());
			dlg.setModal(replaceAll);
			String mess = RC("tools.translator.message.found");
			mess = bundle.replace(mess, "[%found%]", searchCriteria);
			mess = bundle.replace(mess, "[%subst%]", replaceTo);
			repDialog.setMessage(mess);
			dlg.pack();
			dlg.setVisible(true);
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
		BundleSet set = bundle.getBundle();
		if (lastKeyFound != null) {
			j = set.getItemIndex(lastKeyFound) + 1;
		}
		if (!searchData) { // Key names
			Optional<BundleItem> nextMatch = set.getItems().skip(j).filter(it -> isMatchedWith(it.getId())).findFirst();
			if (nextMatch.isPresent()) {
				lastKeyFound = nextMatch.get().getId();
				tree.selectNode(lastKeyFound);
				tree.openToNode(lastKeyFound);
				setTranslations(lastKeyFound);
				textPanel.invalidate();
				validate();
				tree.requestFocusInWindow();
				tree.repaint();
				return;
			}
			lastKeyFound = null;
			if (first) {
				searchCriteria = null;
			}

			String title = RC("dialog.title.warning");
			String message = first ? RC("tools.translator.label.search.nokeys") : RC("tools.translator.label.search.nomorekeys");
			JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
			return;
		}

		int replacements = 0;
		BundleItem[] items = set.getItems().skip(j).toArray(BundleItem[]::new);
		for (BundleItem bi : items) {
			for (LangItem li : set.getLanguages()) {
				String val = bi.getTranslation(li.getId());
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
							textPanel.requestFocusInWindow();
							langStates.get(li.getId()).tf.requestFocusInWindow();
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
			String message = bundle.replace(RC("tools.translator.label.replaced.count"), "[%replaced%]", Integer.toString(replacements));
			String title = RC("dialog.title.warning");
			JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
		}
		else {
			String message = first ? RC("tools.translator.label.search.nokeys") : RC("tools.translator.label.search.nomorekeys");
			String title = RC("dialog.title.warning");
			JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
		}
		updateStatusBar();
	}

	private void onNewResource() {
		String title = RC("tools.translator.label.newrestitle");
		String message = RC("tools.translator.label.filesuff");
		String text = JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE);

		if ((null == text) || text.isEmpty()) {
			return;
		}
		bundle.getBundle().addLanguage(text);
		syncLanguage(text);

		for (LangState ls : langStates.values()) {
			JCheckBoxMenuItem cmi = ls.box;
			boolean show = cmi.getState();
			ls.tf.setVisible(show);
			ls.hidden = !show;
			ls.label.setVisible(show);
		}
		setAllIndicators();
		textPanel.invalidate();
		validate();
		updateStatusBar();
	}

	private void onNewBundle() {
		clear();
		initControls();
		bundle.getBundle().addLanguage("en");
		bundle.getBundle().addKey("creationDate");
		bundle.getBundle().updateValue("creationDate", "en", (new Date()).toString());
		initData(false);
		setTitle(null);
		isDirty = false;
		updateStatusBar();
	}

	private void onLoadBundle() {
		onOpen(false);
		updateStatusBar();
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
			set.getItems().forEachOrdered(bi -> {
				BundleItem bi2 = bundle.getBundle().addKey(bi.getId());
				for (String lang : bi.getLanguages()) {
					bundle.getBundle().addLanguage(lang);
					bi2.setTranslation(lang, bi.getTranslation(lang));
				}
			});
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
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
			String title = RC("dialog.title.warning");
			String message = fileName + ": " + RC("no.file.found");
			JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
			return;
		}

		(new Thread(new Loader(fileName, part))).start();
		setTitle(fileName);
		addToPickList(fileName);
		updateStatusBar();
	}

	private void initControls() {
		JLabel commLab = new JLabel(RC("tools.translator.label.comments"));
		constrain(textPanel, commLab, 0, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0.0, 0.0, 10, 3, 0, 15);

		commField = new JTextField();
		commField.setPreferredSize(new Dimension(0, commField.getFontMetrics(commField.getFont()).getHeight() * 3));
		constrain(textPanel, commField, 1, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1.0, 1.0, 3, 3, 5, 15);

		JButton dropComment = createButton(this::onDropComment, RC("tools.translator.label.dropcomment"));
		constrain(textPanel, dropComment, 2, 0, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.NONE, 0.0, 0.0, 3, 3, 5, 15);

		keynLab = new JLabel("");
		constrain(textPanel, keynLab, 0, 1, 3, 1, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, 1.0, 0.0, 10, 3, 0, 15);
		langMenu.setEnabled(true);
	}

	private void onDropComment() {
		commField.setText("");
		setTranslations();
	}

	private void syncLanguage(String lang) {
		LangItem lang2 = bundle.getBundle().getLanguage(lang);
		String langLab = lang2.getDescription();
		LangState ls = new LangState();
		ls.name = lang2.getId();
		ls.box = new JCheckBoxMenuItem(langLab, false);
		ls.box.addActionListener(e -> {
			ls.hidden = !ls.hidden;
			ls.tf.setVisible(!ls.hidden);
			ls.label.setVisible(!ls.hidden);
			setIndicators(tree.getRootNode());
			textPanel.invalidate();
			validate();
		});
		ls.box.setState(true);
		ls.label = new JLabel(langLab + ":");
		ls.tf = new JTextArea();
		ls.tf.setLocale(new Locale(lang, ""));
		ls.tf.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent ke) {
				if (!ke.isActionKey() && (ke.getKeyChar() == '\n')) {
					invokeAutoFit();
				}
				//checkForScrolling(ke.getComponent()); //TODO: scroll into view?
			}
		});

		langStates.put(lang, ls);
		langMenu.add(ls.box);

		int i = bundle.getBundle().getLanguageIndex(lang);

		constrain(textPanel, ls.label, 0, i + 2, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0.0, 0.0, 10, 3, 0, 3);
		constrain(textPanel, ls.tf, 1, i + 2, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 1.0, 1.0, 3, 3, 5, 3);
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

		if (ind < 0) {
			TranslationTreeNode tnew = tree.getRootNode().createChildNode(s);
			tnew.setCaption(s);
		}
		else {
			String tname = s.substring(0, ind);
			addToTree(tname);
			TranslationTreeNode ttpar = tree.getNode(tname);
			TranslationTreeNode tnew = ttpar.createChildNode(s);
			tnew.setCaption(s.substring(ind + 1));
		}
	}

	private String lookupFileForLoad(String mask) {
		FileDialog openFileDialog1 = new FileDialog(this, RC("tools.translator.label.opentitle"), FileDialog.LOAD);
		openFileDialog1.setDirectory(lastDirectory);
		openFileDialog1.setFile(mask);
		openFileDialog1.setVisible(true);

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
		openFileDialog1.setVisible(true);

		String filename = openFileDialog1.getFile();
		if ((filename != null) && keepLastDir) {
			lastDirectory = openFileDialog1.getDirectory();
			return openFileDialog1.getDirectory() + filename;
		}
		return filename;
	}

	private void onSaveAs() {
		String fn = bundle.getBundle().getFirstLanguage().getFileName();
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
		try (StringWriter writer = new StringWriter()) {
			e.printStackTrace(new PrintWriter(writer));
			String msg = writer.getBuffer().toString();

			String message = e.getMessage() == null ? e.toString() : e.getMessage();
			message = message + "\n" + msg;

			String title = RC("dialog.title.warning");
			JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
		}
		catch (Exception z) {
			z.printStackTrace();
		}
	}

	private void onAbout() {
		String title = RC("dialog.title.info");
		String zavalCopyright = "Copyright &copy; 2001-2002 <a href=\"http://www.zaval.org\">Zaval Creative Engineering Group (http://www.zaval.org)</a><br/>";
		String cobexerCopyright = "Copyright &copy 2018 <a href=\"mailto:cobexer+jrceditor@gmail.com?subject=JRC-Editor\"> Obexer Christoph &lt;cobexer+jrceditor@gmail.com&gt;</a>";
		String copyright = "<html>" + zavalCopyright + cobexerCopyright;
		String ok = RC("dialog.button.ok");
		ImageIcon image = imgres.getImageIcon(SYS_DIR + "ZavalCE.gif");
		AboutDialog aboutDialog = new AboutDialog(this, title, copyright, ok, image);
		aboutDialog.setVisible(true);
	}

	private void onStatistics() {
		nullsCount = 0;
		notCompletedCount = 0;
		setIndicators(tree.getRootNode());
		String text = RC("tools.translator.label.statistics.lang") + bundle.getBundle().getLanguageCount() + "\n";
		text = text + RC("tools.translator.label.statistics.record") + bundle.getBundle().getItemCount() + "\n";
		text = text + RC("tools.translator.label.statistics.nulls") + nullsCount + "\n";
		text = text + RC("tools.translator.label.statistics.notcompleted") + notCompletedCount;
		JOptionPane.showMessageDialog(this, text, RC("dialog.title.info"), JOptionPane.INFORMATION_MESSAGE);
		updateStatusBar();
	}

	private void onGenCode() {
		try {
			BundleSet b = bundle.getBundle();
			LangItem firstLanguage = b.getFirstLanguage();
			String fn = (null == firstLanguage) || (firstLanguage.getFileName() == null)
				? "Sample"
				: bundle.baseName(firstLanguage.getFileName());
			fn = fn.substring(0, 1).toUpperCase() + fn.substring(1);

			String filename = lookupFileForStore(fn + "ResourceMapped.java");
			if (filename != null) {
				SrcGenerator srcgen = new SrcGenerator(bundle.replace(filename, "\\", "/"));
				srcgen.perform(b);
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
				String rlng = bundle.getBundle().getFirstLanguage().getId();

				for (Map.Entry<String, String> stringStringEntry : ask.entrySet()) {
					BundleItem bi = bundle.getBundle().addKey(stringStringEntry.getKey());
					bi.setTranslation(rlng, stringStringEntry.getValue());
				}
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
		BundleSet set = bundle.getBundle();
		for (LangItem lang : set.getLanguages()) {
			syncLanguage(lang.getId());
		}
		hideTransMenu.setState(false);

		/* Add all keys in tree view ... */
		BundleItem bi = set.getItems().findFirst().orElse(null);
		addToTree(bi.getId());

		AtomicInteger counter = new AtomicInteger();
		set.getItems().forEachOrdered(bi2 -> {
			addToTree(bi2.getId());
			int i = counter.get();
			if ((i % 250) == 0) {
				sbl2.setText("    " + i + " " + RC("tools.translator.progress.addkeys"));
				sbl2.repaint();
			}
			counter.incrementAndGet();
		});
		setAllIndicators();
		sbl2.setText("");
		sbl2.repaint();
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		/* ... and make all keys closed by default */

		/* ... find first key, open it and select */
		if (null != bi) {
			String id = bi.getId();
			tree.selectNode(id);
			wasSelectedKey = null;
			setTranslations();
		}

		tree.requestFocusInWindow();

		closeMenu.setEnabled(true);
		saveBundleMenu.setEnabled(true);
		saveAsBundleMenu.setEnabled(true);
		genMenu.setEnabled(true);

		textPanel.invalidate();
		validate();
		repaint();
		if (!part) {
			loadPickList();
		}
		updateStatusBar();
	}

	private void invokeAutoFit() {
		if (autoExpandTF) {
			textPanel.invalidate();
			validate();
		}
	}

	private void expand(TranslationTreeNode tn) {
		if (tn != null) {
			for (TranslationTreeNode element : tree.enumChild(tn)) {
				expand(element);
			}
			tree.openNode(tn.getText());
		}
	}

	private void collapse(TranslationTreeNode tn) {
		if (tn != null) {
			for (TranslationTreeNode element : tree.enumChild(tn)) {
				collapse(element);
			}
			tree.closeNode(tn.getText());
		}
	}

	private void linkPickList() {
		for (String aPickList : pickList) {
			String patz = stretchPath(aPickList);
			JMenuItem item = new JMenuItem(patz);
			item.addActionListener(e -> {
				clear();
				readResources(aPickList, false);
			});
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
		for (j = 0; j < fileMenu.getItemCount(); ++j) {
			JMenuItem item = fileMenu.getItem(j);
			if (null != item) {
				String patz = item.getText();
				if (patz.equals(s1)) {
					break;
				}
			}
		}
		pickList = new ArrayList<>();
		if (j >= fileMenu.getItemCount()) {
			return;
		}
		for (; j < fileMenu.getItemCount();) {
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

	private List<LangItem> getLangSet() {
		LangDialog<LangItem> ed = new LangDialog<>(this, RC("tools.translator.label.choosetitle"), true,
			i -> String.format("%s: %s", i.getId(), i.getDescription()));
		ed.setLabelCaption(RC("tools.translator.label.chooselabel"));
		ed.setButtonsCaption(RC("dialog.button.ok"), CLOSE_BUTTONS[2]);

		ed.setList(bundle.getBundle().getLanguages());

		ed.doModal();
		return ed.isApply() ? ed.getList() : Collections.emptyList();
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
		updateStatusBar();
	}

	// FIXME: this needs to use a real XML api...
	private void onSaveXml(boolean part) {
		List<LangItem> parts = part ? getLangSet() : Collections.emptyList();
		if (part && (parts.size() < 2)) {
			return;
		}
		String fn = bundle.getBundle().getFirstLanguage().getFileName();
		if (fn == null) {
			fn = "autosaved";
		}

		String filename = lookupFileForStore(bundle.baseName(fn) + ".xml");
		if (filename != null) {
			try {
				try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
					BundleSet set = bundle.getBundle();
					out.writeChar((char) 0xFEFF);
					out.writeChars("<xml>\n");
					Set<String> write = parts.stream().map(LangItem::getId).collect(Collectors.toSet());
					set.getItems().forEachOrdered(LambdaUtils.unchecked(bi -> {
						out.writeChars("\t<key name=\"" + bi.getId() + "\">\n");
						for (String lang : bi.getLanguages()) {
							if (!part || write.contains(lang)) {
								out.writeChars("\t\t<value lang=\"" + lang + "\">" + bi.getTranslation(lang) + "</value>\n");
							}
						}
						out.writeChars("\t</key>\n");
					}));
					out.writeChars("</xml>\n");
				}
			}
			catch (Exception e) {
				infoException(e);
			}
		}
	}

	// FIXME: this needs to properly escape the strings for the file format
	private void onSaveUtf(boolean part) {
		List<LangItem> parts = part ? getLangSet() : Collections.emptyList();
		if (part && (parts.size() < 2)) {
			return;
		}

		BundleSet set = bundle.getBundle();
		String fn = set.getFirstLanguage().getFileName();
		if (fn == null) {
			fn = "autosaved";
		}

		String filename = lookupFileForStore(bundle.baseName(fn) + ".txt");
		if (filename != null) {
			try {
				try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
					out.writeChar((char) 0xFEFF);
					out.writeChars("#JRC Editor: do not modify this line\r\n\r\n");
					Set<String> write = parts.stream().map(LangItem::getId).collect(Collectors.toSet());
					set.getItems().forEachOrdered(LambdaUtils.unchecked(bi -> {
						out.writeChars("KEY=\"" + bi.getId() + "\":\r\n");
						for (String lang : bi.getLanguages()) {
							if (!part || write.contains(lang)) {
								out.writeChars("\t\"" + lang + "\"=\"" + bi.getTranslation(lang) + "\"\r\n");
							}
						}
						out.writeChars("\r\n");
					}));
				}
			}
			catch (Exception e) {
				infoException(e);
			}
		}
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
		updateStatusBar();
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
		updateStatusBar();
	}

	private void onNewKey() {
		String title = RC("tools.translator.label.newkeytitle");
		String message = RC("tools.translator.label.insert");
		String text = JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE);
		if ((null == text) || text.isEmpty()) {
			return;
		}
		keyName.setText(text);
		onInsertKey();
		updateStatusBar();
	}

	private void removeLeafs(String key) {
		// Don't touch hier if key/childs are exists
		if (bundle.getBundle().getItem(key) != null) {
			return;
		}
		TranslationTreeNode tn = tree.getNode(key);
		if (tn != null) {
			if (tree.enumChild(tn).length > 0) {
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
		String oldKeyName = keyName.getText().replaceFirst("\\.$", "");
		String title = RC("tools.translator.label.rename.caption");
		String message = RC("tools.translator.label.rename.label");
		String newKeyName = (String) JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE, null, null, oldKeyName);

		if ((null == newKeyName) || newKeyName.trim().isEmpty()) {
			return;
		}
		if (oldKeyName.equals(newKeyName)) {
			return;
		}

		BundleItem biOldAlone = bundle.getBundle().getItem(oldKeyName);
		bundle.getBundle().getKeysBeginningWith(oldKeyName).forEachOrdered(biOld -> {
			Map<String, String> oldValues = new HashMap<>();
			String newKey = newKeyName;
			if (biOldAlone == null) {
				newKey = newKeyName + biOld.getId().substring(oldKeyName.length());
			}

			if (bundle.getBundle().getItem(newKey) != null) {
				String errorTitle = RC("dialog.title.warning");
				String errorMessage = RC("tools.translator.label.rename.dup");
				JOptionPane.showMessageDialog(this, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
				return;
			}

			// Keep old values
			for (LangItem lang : bundle.getBundle().getLanguages()) {
				String value = biOld.getTranslation(lang.getId());
				if (value != null) {
					oldValues.put(lang.getId(), value);
				}
			}
			bundle.getBundle().removeKey(biOld.getId());

			// Add new key
			keyName.setText(newKey);
			addToTree(newKey);
			BundleItem biNew = bundle.getBundle().addKey(newKey);
			for (LangItem lang : bundle.getBundle().getLanguages()) {
				String value = oldValues.get(lang.getId());
				if (value != null) {
					biNew.setTranslation(lang.getId(), value);
				}
			}
		});
		isDirty = true;

		// Remove old key
		tree.remove(oldKeyName);
		removeLeafs(oldKeyName);

		tree.selectNode(newKeyName);
		tree.repaint();
		setTranslations();
		setIndicators(tree.getSelectedNode());
		updateStatusBar();
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
					bundle.getBundle().getFirstLanguage().setFileName(null);
					setTitle(filename);
				}
			}
			catch (Exception e) {
				infoException(e);
			}
		}
		updateStatusBar();
	}

	private void hideTranslated(boolean hide) {
		hideTranslated(tree.getRootNode(), hide);
		tree.invalidate();
		validate();
		tree.repaint();
	}

	private void hideTranslated(TranslationTreeNode tn, boolean hide) {
		while (tn != null) {
			if (!tn.isShowIndicator()) {
				tn.setVisible(!hide);
			}
			hideTranslated(tn.getFirstChild(), hide);
			tn = tn.getNextSibling();
		}
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
}
