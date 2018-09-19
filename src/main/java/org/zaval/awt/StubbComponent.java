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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;

class StubbComponent extends Canvas {
	private static final int size = 16;

	@Override
	public Dimension preferredSize() {
		return new Dimension(size, size);
	}

	public StubbComponent() {
		setBackground(Color.lightGray);
	}

	@Override
	public void paint(Graphics g) {
		draw(g);
	}

	private void draw(Graphics g) {
		Dimension d = size();
		g.setColor(Color.lightGray);
		g.fillRect(0, 0, d.width, d.height);

		int stap = size / 3;
		int x = stap;
		for (int y = stap; x <= (d.width * 2); x += stap, y += stap) {
			g.setColor(Color.white);
			g.drawLine(x, 0, 0, y);

			g.setColor(Color.gray);
			g.drawLine(x + 1, 0, 0, y + 1);
		}
	}

	@Override
	public boolean mouseEnter(Event e, int x, int y) {
		return true;
	}

	@Override
	public boolean mouseExit(Event e, int x, int y) {
		return true;
	}
}
