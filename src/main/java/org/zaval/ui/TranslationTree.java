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

package org.zaval.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class TranslationTree implements TreeModelListener {
	private final Map<String, TranslationTreeNode> nodes = new HashMap<>();
	private final TranslationTreeNode rootNode = TranslationTreeNode.createRootNode();
	private final JTree tree;
	private final JScrollPane component;
	private final DefaultTreeModel treeModel;
	private final ImageIcon warningIcon;

	private TranslationTreeNode selectedNode; // highlighted node

	public TranslationTree(ImageIcon warningIcon) {
		this.warningIcon = warningIcon;
		nodes.put("", rootNode);
		tree = new JTree(rootNode);
		treeModel = (DefaultTreeModel) tree.getModel();
		rootNode.setModel(treeModel);
		component = new JScrollPane(tree);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setEditable(false);
		tree.setExpandsSelectedPaths(true);
		tree.setCellRenderer(new TranslationTreeCellRenderer(this, tree.getCellRenderer()));

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		addSelectionListener(this::treeSelectionChanged);
		treeModel.addTreeModelListener(this);
	}

	public ImageIcon getWarningIcon() {
		return warningIcon;
	}

	public TranslationTreeNode getRootNode() {
		return rootNode;
	}

	public JComponent getComponent() {
		return component;
	}

	public TranslationTreeNode getSelectedNode() {
		return selectedNode;
	}

	public void addSelectionListener(Consumer<TranslationTreeNode> callback) {
		tree.addTreeSelectionListener(e -> callback.accept((TranslationTreeNode) e.getPath().getLastPathComponent()));
	}

	public void addKeyListener(Consumer<KeyEvent> callback) {
		tree.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				// not forwarded
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// not forwarded
			}

			@Override
			public void keyPressed(KeyEvent e) {
				callback.accept(e);
			}
		});
	}

	public void setAllExpandedState(boolean expanded) {
		setAllExpandedState(new TreePath(rootNode), expanded);
	}

	@SuppressWarnings("rawtypes")
	private void setAllExpandedState(TreePath parent, boolean expanded) {
		TranslationTreeNode node = (TranslationTreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TranslationTreeNode n = (TranslationTreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				setAllExpandedState(path, expanded);
			}
		}
		expandNode(parent, expanded);
	}

	private void expandNode(TreePath path, boolean expanded) {
		if (expanded) {
			tree.expandPath(path);
		}
		else {
			tree.collapsePath(path);
		}
	}

	private void treeSelectionChanged(TranslationTreeNode newSelectedNode) {
		selectedNode = newSelectedNode;
	}

	public void requestFocusInWindow() {
		tree.requestFocusInWindow();
	}

	public String getSelectedText() {
		return null != selectedNode ? selectedNode.getText() : null;
	}

	public void selectNode(TranslationTreeNode node) {
		if (null != node) {
			tree.setSelectionPath(new TreePath(node.getPath()));
		}
	}

	public TranslationTreeNode getNode(String key) {
		return nodes.get(key);
	}

	public void selectNode(String key) {
		selectNode(getNode(key));
	}

	public void openToNode(String key) {
		TranslationTreeNode node = getNode(key);
		while (null != node) {
			expandNode(new TreePath(node.getPath()), true);
			node = node.getParent();
		}
	}

	public TranslationTreeNode[] enumChild(TranslationTreeNode tn) {
		int childCount = tn.getChildCount();
		return IntStream.range(0, childCount).mapToObj(tn::getChildAt).toArray(TranslationTreeNode[]::new);
	}

	public void remove(String key) {
		TranslationTreeNode node = nodes.remove(key);
		if (null != node) {
			treeModel.removeNodeFromParent(node);
		}
	}

	public void setComponentPopupMenu(JPopupMenu ctNodeMenu) {
		tree.setComponentPopupMenu(ctNodeMenu);
	}

	public void repaint() {
		//FIXME: Auto-generated method stub
		//FIXME: remove the _need_ for this method
	}

	public void invalidate() {
		//FIXME: Auto-generated method stub
		//FIXME: remove the _need_ for this method
	}

	public void openNode(String key) {
		TranslationTreeNode node = getNode(key);
		if (null != node) {
			expandNode(new TreePath(node.getPath()), true);
		}
	}

	public void closeNode(String key) {
		TranslationTreeNode node = getNode(key);
		if (null != node) {
			expandNode(new TreePath(node.getPath()), false);
		}
	}

	@Override
	public void treeNodesInserted(TreeModelEvent e) {
		if (1 == nodes.size()) {
			// auto expand the root node because JTree is such a smart useful thing that doesn't do this on it's own...
			// I mean JTrees with hidden collapsed root are so very useful... Am I right??
			SwingUtilities.invokeLater(() -> {
				expandNode(new TreePath(rootNode), true);
			});
		}
		for (Object n : e.getChildren()) {
			TranslationTreeNode node = (TranslationTreeNode) n;
			nodes.put(node.getText(), node);
		}
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e) {
		for (Object n : e.getChildren()) {
			nodes.remove(((TranslationTreeNode) n).getText());
		}
	}

	public void removeAll() {
		for (TranslationTreeNode child : enumChild(rootNode)) {
			treeModel.removeNodeFromParent(child);
			child.removeFromParent();
		}
	}

	@Override
	public void treeNodesChanged(TreeModelEvent e) {
		// not relevant
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {
		// not relevant
	}
}
