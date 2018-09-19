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
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Rectangle;

public class BorderedPanel extends Panel {
	private static final int NONE = 0;
	private static final int RAISED = 1;
	private static final int SUNKEN = 2;
	private static final int ETCHED = 3;
	public static final int RAISED2 = 4;

	private final int type;
	private final Insets insets = new Insets(2, 2, 2, 2);
	private static final Color[] colors = { Color.white, Color.gray };

	public BorderedPanel() {
		this(ETCHED);
	}

	public BorderedPanel(int type) {
		this.type = type;
	}

	@Override
	public Insets getInsets() {
		return insets;
	}

	@Override
	public void paint(Graphics g) {
		if (type == NONE) {
			Rectangle r = bounds();
			g.setColor(getParent().getBackground());
			g.fillRect(0, 0, r.width, r.height);
			return;
		}
		super.paint(g);

		Dimension d = size();
		int x = 0;
		int y = 0;
		int x2 = d.width - 1;
		int y2 = d.height - 1;

		leftLine(g, x, y, y2);
		rightLine(g, y, x2, y2);

		topLine(g, x, y, x2);
		bottomLine(g, x, x2, y2);
	}

	private void leftLine(Graphics g, int x, int y, int y2) {
		g.setColor(colors[1]);
		switch (type) {
			case ETCHED: {
				g.setColor(colors[1]);
				g.drawLine(x, y, x, y2 - 1); // Topleft to bottomleft
				g.setColor(colors[0]);
				g.drawLine(x + 1, y + 1, x + 1, y2 - 2); // Topleft to bottomleft
			}
				break;
			case RAISED2: {
				g.setColor(colors[0]);
				g.drawLine(x, y, x, y2 + 2); // Topleft to bottomleft
			}
				break;
			case RAISED: {
				g.setColor(colors[0]);

				g.drawLine(x, y, x, y2 - 1); // Topleft to bottomleft
			}
				break;
			case SUNKEN: {
				g.setColor(colors[1]);
				g.drawLine(x, y, x, y2 - 1); // Topleft to bottomleft
				g.setColor(Color.black);
				g.drawLine(x + 1, y + 1, x + 1, y2 - 2); // Topleft to bottomleft
			}
		}
	}

	private void rightLine(Graphics g, int y, int x2, int y2) {
		g.setColor(colors[1]);
		switch (type) {
			case ETCHED: {
				g.setColor(colors[1]);
				g.drawLine(x2 - 1, y + 1, x2 - 1, y2); // Topright to bottomright
				g.setColor(colors[0]);
				g.drawLine(x2, y, x2, y2); // Topright to bottomright
			}
				break;
			case RAISED: {
				g.setColor(colors[1]);
				g.drawLine(x2, y, x2, y2); // Topright to bottomright
				g.setColor(colors[1]);
				g.drawLine(x2 - 1, y + 1, x2 - 1, y2 - 1); // Topright to bottomright
			}
				break;
			case SUNKEN: {
				g.setColor(colors[0]);
				g.drawLine(x2, y, x2, y2 - 1); // Topright to bottomright
			}
				break;
			case RAISED2:
				g.setColor(colors[1]);
				g.drawLine(x2, y, x2, y2); // Topright to bottomright
				break;
		}
	}

	private void topLine(Graphics g, int x, int y, int x2) {
		g.setColor(colors[1]);
		switch (type) {
			case ETCHED: {
				g.setColor(colors[1]);
				g.drawLine(x, y, x2 - 1, y); // Topleft to topright
				g.setColor(colors[0]);
				g.drawLine(x + 1, y + 1, x2 - 2, y + 1); // Topleft to topright
			}
				break;
			case RAISED2: {
				g.setColor(colors[0]);
				g.drawLine(x, y, x2 + 2, y); // Topleft to topright
			}
				break;
			case RAISED: {
				g.setColor(colors[0]);
				g.drawLine(x, y, x2 - 1, y); // Topleft to topright*/
			}
				break;
			case SUNKEN: {
				g.setColor(Color.black);
				g.drawLine(x + 1, y + 1, x2 - 2, y + 1); // Topleft to topright
				g.setColor(colors[1]);
				g.drawLine(x, y, x2 - 1, y); // Topleft to topright
			}
		}
	}

	private void bottomLine(Graphics g, int x, int x2, int y2) {
		g.setColor(colors[1]);
		switch (type) {
			case ETCHED: {
				g.setColor(colors[1]);
				g.drawLine(x, y2 - 1, x2 - 1, y2 - 1); // Bottomleft to bottomright
				g.setColor(colors[0]);
				g.drawLine(x, y2, x2, y2); // Bottomleft to bottomright
			}
				break;
			case RAISED: {
				g.setColor(colors[1]);
				g.drawLine(x, y2, x2 - 1, y2); // Bottomleft to bottomright
				g.setColor(colors[1]);
				g.drawLine(x + 1, y2 - 1, x2 - 2, y2 - 1); // Bottomleft to bottomright
			}
				break;
			case SUNKEN: {
				g.setColor(colors[0]);
				g.drawLine(x, y2, x2, y2); // Bottomleft to bottomright
				g.drawLine(x + 1, y2 - 1, x2 - 1, y2 - 1); // Bottomleft to bottomright
			}
				break;
			case RAISED2:
				g.setColor(colors[1]);
				g.drawLine(x + 1, y2, x2, y2); // Bottomleft to bottomright
				break;
		}
	}

	// X11 fixes to avoid wrong repaints
	@Override
	public boolean mouseMove(Event e, int x, int y) {
		return true;
	}

	@Override
	public boolean mouseExit(Event e, int x, int y) {
		return true;
	}

	@Override
	public boolean mouseEnter(Event e, int x, int y) {
		return true;
	}
}
