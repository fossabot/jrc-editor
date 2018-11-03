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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import org.zaval.ui.UiUtils;

@SuppressWarnings("serial")
class LangDialog<ListItem> extends JDialog {
	private final JLabel label = new JLabel("");
	private final JList<ListItem> edit = new JList<>();
	private final JButton ok = new JButton("");
	private final JButton cancel = new JButton("");
	private boolean isApply;

	public LangDialog(JFrame owner, String title, boolean modal, Function<ListItem, String> itemMapper) {
		super(owner, title, modal);
		setLayout(new GridBagLayout());

		constrain(this, label, 0, 0, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.0, 0.0, 5, 5, 5, 5);
		edit.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		edit.setCellRenderer(new LangDialogCellRenderer<>(edit.getCellRenderer(), itemMapper));
		constrain(this, edit, 0, 1, 4, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1.0, 0.0, 5, 5, 5, 5);

		ok.addActionListener(this::onPerform);
		cancel.addActionListener(this::onCancel);

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 2, 2, 0));
		p.add(ok);
		p.add(cancel);

		constrain(this, p, 0, 2, 2, 1, GridBagConstraints.EAST, GridBagConstraints.NONE, 1.0, 0.0, 5, 5, 5, 5);

		pack();
	}

	private void onPerform(ActionEvent e) {
		isApply = true;
		dispose();
	}

	private void onCancel(ActionEvent e) {
		isApply = false;
		dispose();
	}

	public void setList(ListItem[] items) {
		edit.removeAll();
		edit.setListData(items);
		edit.setSelectedIndex(0); // first item (English?) is always selected
	}

	public List<ListItem> getList() {
		int[] selectedIndices = edit.getSelectedIndices();
		// make sure that the first item (English=) is always selected
		if (0 == selectedIndices.length) {
			edit.setSelectedIndex(0);
		}
		else if (0 != selectedIndices[0]) {
			int[] newSelectedIndices = new int[selectedIndices.length + 1];
			newSelectedIndices[0] = 0;
			System.arraycopy(selectedIndices, 0, newSelectedIndices, 1, selectedIndices.length);
			edit.setSelectedIndices(newSelectedIndices);
		}
		return edit.getSelectedValuesList();
	}

	public void setButtonsCaption(String o, String c) {
		ok.setText(o);
		cancel.setText(c);
	}

	public void setLabelCaption(String l) {
		label.setText(l);
	}

	public boolean isApply() {
		return isApply;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.width *= 2;
		return d;
	}

	public void doModal() {
		pack();
		UiUtils.toCenter(this);
		setVisible(true);
	}
}
