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

import java.awt.Component;
import java.util.function.Function;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class LangDialogCellRenderer<ListItem> implements ListCellRenderer<ListItem> {
	private final ListCellRenderer<? super ListItem> cellRenderer;
	private final Function<ListItem, String> itemMapper;

	LangDialogCellRenderer(ListCellRenderer<? super ListItem> cellRenderer, Function<ListItem, String> itemMapper) {
		this.cellRenderer = cellRenderer;
		this.itemMapper = itemMapper;
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends ListItem> list, ListItem value, int index, boolean isSelected,
		boolean cellHasFocus) {
		JLabel l = (JLabel) cellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		l.setText(itemMapper.apply(value));
		return l;
	}
}
