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

import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.List;
import java.awt.Toolkit;

import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
class LangDialog extends Dialog {
	private final List edit;
	private final Button ok;
	private final Button cancel;
	private boolean isApply;
	private final Component listener;
	private final JLabel label;

	public LangDialog(Frame f, String s, boolean b, Component l) {
		super(f, s, b);
		setLayout(new GridBagLayout());

		label = new JLabel("List of languages");
		constrain(this, label, 0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0, 5, 5, 5, 5);
		edit = new List(10, true);
		constrain(this, edit, 0, 1, 4, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0, 5, 5, 5, 5);

		ok = new Button("Ok");
		cancel = new Button("Cancel");

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(1, 2, 2, 0));
		p.add(ok);
		p.add(cancel);

		constrain(this, p, 0, 2, 2, 1, GridBagConstraints.NONE, GridBagConstraints.EAST, 1.0, 0.0, 5, 5, 5, 5);

		listener = l;
		pack();
	}

	public void setList(LangItem... t) {
		edit.removeAll();
		if (t == null) {
			return;
		}
		for (LangItem element : t) {
			String s = element.getLangId() + ": " + element.getLangDescription();
			edit.add(s);
		}
		edit.select(0); // english is always selected
	}

	public String[] getList() {
		edit.select(0); // english is always selected
		return edit.getSelectedItems();
	}

	public void setButtonsCaption(String o, String c) {
		ok.setLabel(o);
		cancel.setLabel(c);
	}

	public void setLabelCaption(String l) {
		label.setText(l);
	}

	@Override
	public boolean handleEvent(Event e) {
		if ((e.id == Event.WINDOW_DESTROY) || ((e.target == cancel) && (e.id == Event.ACTION_EVENT))) {
			isApply = false;
			dispose();
		}

		if ((e.target == ok) && (e.id == Event.ACTION_EVENT)) {
			isApply = true;
			listener.postEvent(new Event(this, Event.ACTION_EVENT, null));
			dispose();
		}
		return super.handleEvent(e);
	}

	public boolean isApply() {
		return isApply;
	}

	private void toCenter() {
		Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension d = getSize();
		move((s.width - d.width) / 2, (s.height - d.height) / 2);
	}

	static private void constrain(Container c, Component p, int x, int y, int w, int h, int f, int a, double wx, double wy, int t, int l,
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
		LayoutManager lm = c.getLayout();
		if (lm instanceof GridBagLayout) {
			GridBagLayout gbl = (GridBagLayout) lm;
			gbl.setConstraints(p, cc);
		}
		c.add(p);
	}

	@Override
	public boolean keyDown(Event e, int key) {
		if (((e.target == ok) && (key == Event.ENTER)) || ((e.target == edit) && (key == Event.ENTER))) {
			isApply = true;
			listener.postEvent(new Event(this, Event.ACTION_EVENT, null));
			dispose();
			return true;
		}
		if ((e.target == cancel) && (key == Event.ENTER)) {
			isApply = false;
			dispose();
			return true;
		}
		return false;
	}

	@Override
	public Dimension preferredSize() {
		Dimension d = super.preferredSize();
		d.width *= 2;
		return d;
	}

	public void doModal() {
		pack();
		toCenter();
		show();
	}
}
