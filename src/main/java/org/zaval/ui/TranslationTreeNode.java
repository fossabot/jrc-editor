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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

@SuppressWarnings("serial")
public class TranslationTreeNode extends DefaultMutableTreeNode {
	private final String fullPath;
	private DefaultTreeModel treeModel;
	private String caption;
	private boolean showIndicator;
	private boolean visible = true;

	private TranslationTreeNode(DefaultTreeModel treeModel, String fullPath) {
		this.treeModel = treeModel;
		this.fullPath = fullPath;
	}

	static TranslationTreeNode createRootNode() {
		return new TranslationTreeNode(null, "/");
	}

	public TranslationTreeNode createChildNode(String fullPath) {
		TranslationTreeNode nn = new TranslationTreeNode(treeModel, fullPath);
		int childrenCount = getChildCount();
		int i = 0;
		for (; i < childrenCount; ++i) {
			if (getChildAt(i).fullPath.compareToIgnoreCase(fullPath) > 0) {
				i += 1;
				break;
			}
		}
		insert(nn, i);
		treeModel.nodesWereInserted(this, new int[] { i });
		return nn;
	}

	public void removeRecursive() {
		for (int i = getChildCount(); 0 != i; --i) {
			TranslationTreeNode child = getChildAt(i - 1);
			child.removeRecursive();
		}
		treeModel.removeNodeFromParent(this);
	}

	public void setModel(DefaultTreeModel treeModel) {
		this.treeModel = treeModel;
	}

	private String getCaption() {
		return null != caption ? caption : fullPath;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	@Override
	public TranslationTreeNode getParent() {
		return (TranslationTreeNode) super.getParent();
	}

	@Override
	public TranslationTreeNode getFirstChild() {
		TranslationTreeNode firstChild = null;
		if (getChildCount() > 0) {
			firstChild = (TranslationTreeNode) super.getFirstChild();
		}
		return firstChild;
	}

	@Override
	public TranslationTreeNode getPreviousSibling() {
		return (TranslationTreeNode) super.getPreviousSibling();
	}

	@Override
	public TranslationTreeNode getNextSibling() {
		return (TranslationTreeNode) super.getNextSibling();
	}

	@Override
	public TranslationTreeNode getChildAt(int index) {
		return (TranslationTreeNode) super.getChildAt(index);
	}

	public void setShowIndicator(boolean showIndicator) {
		if (this.showIndicator != showIndicator) {
			this.showIndicator = showIndicator;
			treeModel.nodeChanged(this);
		}
	}

	public boolean isShowIndicator() {
		return showIndicator;
	}

	public String getText() {
		return fullPath;
	}

	public void setVisible(boolean visible) {
		if (this.visible != visible) {
			this.visible = visible;
			treeModel.nodeChanged(this);
			//TODO: toggle node visibility
		}
	}

	@Override
	public String toString() {
		return getCaption();
	}
}
