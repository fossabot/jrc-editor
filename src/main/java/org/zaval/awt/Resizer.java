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
import java.awt.Component;
import java.awt.Container;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Resizer extends Canvas {
	private int startx;
	private int oldrg;

	private boolean drag;
	private boolean state;
	private boolean _enable;

	public Resizer() {
		super();
		startx = 0;
		oldrg = 0;
		_enable = isEnabled();
	}

	@Override
	public void enable() {
		enable(true);
	}

	@Override
	public void disable() {
		enable(false);
	}

	@Override
	public void enable(boolean e) {
		if (_enable == e) {
			return;
		}
		_enable = e;
		super.enable(_enable);
		if (!_enable) {
			drag = false;
			startx = 0;
			oldrg = 0;
		}
		repaint();
	}

	@Override
	public void paint(Graphics gr) {
		if (_enable) {
			Rectangle r = bounds();
			int x = 0;
			int y = 0;
			int w = r.width - (2 * x) - 1;
			int h = r.height - (2 * y) - 1;
			gr.setColor(Color.lightGray);
			gr.fillRect(x, y, x + w, y + h);
			gr.setColor(Color.white);
			gr.drawLine(x, y, x + w, y);
			gr.drawLine(x, y, x, y + h);
			gr.setColor(Color.black);
			gr.drawLine(x, y + h, x + w, y + h);
			gr.drawLine(x + w, y, x + w, y + h);
		}
	}

	private void paintLine(Component c, int x) {
		for (int i = 0; i < bounds().width; i++, x++) {
			drawVLineOnComponent(c, bounds().y, bounds().height + bounds().y, x, Color.darkGray);
		}
	}

	private static void drawVLineOnComponent(Component c, int y1, int y2, int x, Color col) {
		if (c == null) {
			return;
		}
		Rectangle d = c.bounds();
		if (d.height <= y2) {
			y2 = d.height - 1;
		}

		Component lc = getNextBottomChild(c, y1 + 1, x);
		while ((lc != null) && (y1 < y2)) {
			Rectangle lr = lc.bounds();
			int yy1 = y1 - lr.y;
			if (yy1 < 0) {
				yy1 = 0;
			}

			int yy2 = y2 - lr.y;
			if (yy2 >= lr.height) {
				yy2 = lr.height - 1;
			}

			int xx = x - lr.x;
			if (yy2 <= yy1) {
				break;
			}

			if (lr.y > y1) {
				Graphics g = c.getGraphics();
				Color clr = g.getColor();
				g.setColor(col);
				g.setXORMode(c.getBackground());
				g.drawLine(x, y1, x, lr.y);
				g.setColor(clr);
				g.setPaintMode();
				g.dispose();
			}

			drawVLineOnComponent(lc, yy1, yy2, xx, col);
			y1 = (lr.y + lr.height);
			lc = getNextBottomChild(c, y1, x);
		}

		if (y1 < y2) {
			Graphics g = c.getGraphics();
			Color clr = g.getColor();
			g.setColor(col);
			g.setXORMode(c.getBackground());
			g.drawLine(x, y1, x, y2);
			g.setColor(clr);
			g.setPaintMode();
			g.dispose();
		}
	}

	private static Component getNextBottomChild(Component parent, int y, int x) {
		if (!(parent instanceof Container)) {
			return null;
		}

		Component c = getComponentAtFix((Container) parent, x, y); //parent.getComponentAt(x, y);
		if ((c != null) && (c != parent)) {
			return c;
		}

		Component[] comps = ((Container) parent).getComponents();
		Component find = null;
		int fy = Integer.MAX_VALUE;
		for (Component comp : comps) {
			Rectangle r = comp.bounds();
			if ((x < r.x) || (x > (r.x + r.width))) {
				continue;
			}

			if ((r.y < fy) && (r.y > y)) {
				fy = r.y;
				find = comp;
			}
		}
		return find;
	}

	private static Component getComponentAtFix(Container top, int x, int y) {
		Component[] c = top.getComponents();
		List<Component> v = new ArrayList<>();
		for (Component aC : c) {
			if (!aC.isVisible()) {
				continue;
			}
			Rectangle b = aC.bounds();
			if (b.inside(x, y)) {
				v.add(aC);
			}
		}

		if (!v.isEmpty()) {
			return v.get(0);
		}
		return null;
	}

	private void setCursor0(int c) {
		Component f = this;
		while ((f != null) && !(f instanceof Frame)) {
			f = f.getParent();
		}
		if (f != null) {
			((Frame) f).setCursor(c);
		}
	}

	private void resizeme(int x) {
		Rectangle r = bounds();
		ResizeLayout rl = (ResizeLayout) getParent().getLayout();
		Rectangle rp = getParent().bounds();

		int pos = (r.x + x) - startx - r.width;
		int right = (rp.width - (2 * r.width)) + rp.x;

		if (pos > right) {
			pos = right;
		}
		int left = 0;
		if (pos < left) {
			pos = left;
		}

		rl.setSeparator(pos, getParent());

		oldrg = 0;
		startx = 0;
	}

	@Override
	public boolean mouseEnter(Event ev, int x, int y) {
		if (_enable) {
			setCursor0(Frame.E_RESIZE_CURSOR);
			return true;
		}
		return super.mouseEnter(ev, x, y);
	}

	@Override
	public boolean mouseExit(Event ev, int x, int y) {
		if (_enable) {
			if (drag) {
				paintLine(getParent(), (oldrg + bounds().x) - startx);
				drag = false;
			}
			setCursor0(Frame.DEFAULT_CURSOR);
			return true;
		}
		return super.mouseExit(ev, x, y);
	}

	@Override
	public boolean mouseDown(Event ev, int x, int y) {
		if (_enable && inside(x, y) && (ev.modifiers == 0)) {
			if (drag) {
				return super.mouseDown(ev, x, y);
			}
			startx = x;
			oldrg = x;
			state = true;
			drag = true;
			paintLine(getParent(), (oldrg + bounds().x) - startx);
			setCursor0(Frame.E_RESIZE_CURSOR);
			return true;
		}
		return super.mouseDown(ev, x, y);
	}

	@Override
	public boolean mouseUp(Event ev, int x, int y) {
		if (_enable) {
			if (state) {
				paintLine(getParent(), (oldrg + bounds().x) - startx);
				resizeme(x);
				state = false;
				drag = false;
			}
			return true;
		}
		return super.mouseUp(ev, x, y);
	}

	@Override
	public boolean mouseDrag(Event ev, int x, int y) {
		if (_enable) {
			if (state) {
				if (oldrg == x) {
					return super.mouseDrag(ev, x, y);
				}
				if (drag) {
					paintLine(getParent(), (oldrg + bounds().x) - startx);
				}
				else {
					drag = true;
				}
				paintLine(getParent(), (x + bounds().x) - startx);
				oldrg = x;
			}
			return true;
		}
		return super.mouseDrag(ev, x, y);
	}

}
