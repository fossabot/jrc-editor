/*
 * Copyright (C) 2001-2002  Zaval Creative Engineering Group (http://www.zaval.org)
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
import java.awt.Graphics;

public class IECheckbox extends BaseCheckbox {

	public IECheckbox(String label) {
		setLabel(label);
		setState(false);
		setAlign();
	}

	@Override
	public void paint(Graphics g, int x, int y, int width, int height) {
		int yy = posY + height;
		int xx = posX + width;

		if (isEnabled()) {
			if (mouse_down) {
				g.setColor(Color.lightGray);
			}
			else {
				g.setColor(Color.white);
			}
		}
		else {
			g.setColor(Color.lightGray);
		}

		g.fillRect(x, y, width, height);

		g.setColor(Color.gray);
		g.drawLine(x, y, x, yy);
		g.drawLine(x, y, xx, y);
		g.setColor(Color.white);
		g.drawLine(xx, y, xx, yy);
		g.drawLine(xx, yy, x, yy);
		g.setColor(Color.black);
		g.drawLine(x + 1, y + 1, x + 1, yy - 2);
		g.drawLine(x + 1, y + 1, xx - 2, y + 1);
		g.setColor(Color.lightGray);
		g.drawLine(x + 1, yy - 1, xx - 1, yy - 1);
		g.drawLine(xx - 1, y + 1, xx - 1, yy - 1);

		if (state) {
			if (isEnabled()) {
				g.setColor(Color.black);
			}
			else {
				g.setColor(Color.gray);
			}

			g.drawLine(x + 3, y + 5, x + 3, y + 7);
			g.drawLine(x + 4, y + 6, x + 4, y + 8);
			g.drawLine(x + 5, y + 7, x + 5, y + 9);
			g.drawLine(x + 6, y + 6, x + 6, y + 8);
			g.drawLine(x + 7, y + 5, x + 7, y + 7);
			g.drawLine(x + 8, y + 4, x + 8, y + 6);
			g.drawLine(x + 9, y + 3, x + 9, y + 5);
		}
	}
}
