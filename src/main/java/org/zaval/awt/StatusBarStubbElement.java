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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

public class StatusBarStubbElement extends StatusBarElement {
	public StatusBarStubbElement(Component c, int p, Dimension pref) {
		super(c, p, pref);
	}

	@Override
	public void paint(Graphics gr) {
		super.paint(gr);
		Dimension d = size();

		int h = (d.height * 2) / 3;
		int c = h / 5;
		int xx = d.width - 2;
		int yy = d.height - 2;
		if ((c % 5) > 0) {
			c++;
		}

		int x = xx - 2;
		int y = yy - 2;
		for (int i = 0; i < c; i++) {
			gr.setColor(Color.gray);
			gr.drawLine(x, yy, xx, y);
			x -= 2;
			y -= 2;
			gr.setColor(Color.white);
			gr.drawLine(x, yy, xx, y);
			x -= 2;
			y -= 2;
		}

		gr.setColor(getBackground());
		gr.drawLine(x + 2, yy + 1, xx, yy + 1);
		gr.drawLine(xx + 1, y + 2, xx + 1, yy + 1);
	}
}
