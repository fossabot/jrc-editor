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

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

class TranslationTreeCellRenderer implements TreeCellRenderer {
	private final ImageIcon warningIcon;
	private final TreeCellRenderer treeCellRenderer;

	TranslationTreeCellRenderer(TranslationTree tree, TreeCellRenderer treeCellRenderer) {
		this.warningIcon = tree.getWarningIcon();
		this.treeCellRenderer = treeCellRenderer;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row,
		boolean hasFocus) {
		TranslationTreeNode node = (TranslationTreeNode) value;
		JLabel c = (JLabel) treeCellRenderer.getTreeCellRendererComponent(tree, node.getCaption(), selected, expanded, leaf, row, hasFocus);

		if (node.isShowIndicator()) {
			c.setIcon(warningIcon);
		}
		else {
			c.setIcon(null);
		}
		return c;
	}
}
