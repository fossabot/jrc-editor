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

package org.zaval.awt.dialog;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EditDialog extends JDialog implements ActionListener {
	protected static final String COMMAND_OK = "ok";
	protected static final String COMMAND_CANCEL = "cancel";
	private final JTextField edit;
	private final JButton ok;
	private final JButton cancel;
	private boolean isApply;
	private final Component listener;
	private final JLabel label;

	public EditDialog(JFrame f, String s, boolean b, Component l) {
		super(f, s, b);
		setLayout(new GridBagLayout());

		label = new JLabel("Name");

		constrain(this, label, 0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0, 5, 5, 5, 5);
		edit = new JTextField(20);
		edit.addActionListener(this);
		edit.setActionCommand(COMMAND_OK);
		constrain(this, edit, 1, 0, 4, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0, 5, 5, 5, 5);

		listener = l;
		edit.requestFocus();
		ok = new JButton("Ok");
		cancel = new JButton("Cancel");
	}

	protected void renderDialogFooter() {
		ok.setActionCommand(COMMAND_OK);
		ok.addActionListener(this);
		cancel.addActionListener(this);
		cancel.setActionCommand(COMMAND_CANCEL);

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 2, 2, 0));
		p.add(ok);
		p.add(cancel);

		constrain(this, p, 0, 10, 2, 1, GridBagConstraints.NONE, GridBagConstraints.EAST, 1.0, 0.0, 5, 5, 5, 5);
	}

	public void setText(String t) {
		edit.setText(null == t ? "" : t);
	}

	public String getText() {
		return edit.getText();
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

	private void toCenter() {
		Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension d = getSize();
		setLocation((s.width - d.width) / 2, (s.height - d.height) / 2);
	}

	protected static void constrain(Container c, Component p, int x, int y, int w, int h, int f, int a, double wx, double wy, int t, int l,
		int r, int b) {
		GridBagConstraints cc = new GridBagConstraints();

		cc.gridx = x;
		cc.gridy = y;
		cc.gridwidth = w;
		cc.gridheight = h;

		cc.fill = f;
		cc.anchor = a;
		cc.weightx = wx;
		cc.weighty = wy;

		if ((t + b + l + r) > 0) {
			cc.insets = new Insets(t, l, b, r);
		}
		c.add(p, cc);
	}

	public void doModal() {
		pack();
		toCenter();
		edit.requestFocus();
		show();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case COMMAND_OK:
				isApply = true;
				listener.postEvent(new Event(this, Event.ACTION_EVENT, edit.getText()));
				dispose();
				break;
			case COMMAND_CANCEL:
				isApply = false;
				dispose();
				break;
		}
	}
}
