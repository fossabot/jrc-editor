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

import java.util.Optional;

public interface TranslationTreeListener {
	/**
	 * Called in response to changing the selected node.
	 * @param newSelectedNode the node that receives the selection (if any).
	 */
	void onTreeSelectionChanged(Optional<TranslationTreeNode> newSelectedNode);

	/**
	 * Called in response to pressing the delete key.
	 * @param selectedNode the currently selected node (if any).
	 */
	void onDeleteTreeNode(Optional<TranslationTreeNode> selectedNode);
}
