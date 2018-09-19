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

package org.zaval.awt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Panel;

public class StatusBarElement extends Panel {
	private final int percent;
	private int type = 1;
	private final Insets ins = new Insets(1, 3, 1, 3);
	private final Dimension pref;

	StatusBarElement(Component c, int p, Dimension pref) {
		setLayout(new BorderLayout(1, 0));
		if (c != null) {
			add("Center", c);
		}
		percent = p;
		this.pref = pref;
	}

	public StatusBarElement(Component c, int p) {
		this(c, p, null);
	}

	public void setType(int t) {
		if (t == type) {
			return;
		}
		type = t;
		repaint();
	}

	public int getPercent() {
		return percent;
	}

	@Override
	public Insets insets() {
		return ins;
	}

	@Override
	public void paint(Graphics gr) {
		super.paint(gr);
		Dimension d = size();
		switch (type) {
			case 1:
				gr.setColor(Color.gray);
				gr.drawLine(0, 0, d.width - 1, 0);
				gr.drawLine(0, 0, 0, d.height - 1);

				gr.setColor(Color.white);
				gr.drawLine(d.width - 1, 0, d.width - 1, d.height - 1);
				gr.drawLine(d.width - 1, d.height - 1, 0, d.height - 1);
				break;
			case 2:
				gr.setColor(Color.white);
				gr.drawLine(0, 0, d.width - 1, 0);
				gr.drawLine(0, 0, 0, d.height - 1);

				gr.setColor(Color.gray);
				gr.drawLine(d.width - 1, 0, d.width - 1, d.height - 1);
				gr.drawLine(d.width - 1, d.height - 1, 0, d.height - 1);
		}
	}

	@Override
	public Dimension preferredSize() {
		if (pref != null) {
			return new Dimension(pref.width, pref.height);
		}
		return super.preferredSize();
	}
}
