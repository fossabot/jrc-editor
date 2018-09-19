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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.util.Arrays;

public class SimpleScrollPanel extends Panel implements LayoutManager {
	private final Scrollbar hor;
	private final Scrollbar ver;
	private final Component comp;
	private final Panel panel;

	public Component getScrollableObject() {
		return comp;
	}

	public SimpleScrollPanel(Component c) {
		comp = c;
		hor = new Scrollbar(Scrollbar.HORIZONTAL);
		ver = new Scrollbar(Scrollbar.VERTICAL);
		setLayout(this);

		panel = new Panel();
		panel.setLayout(null);

		panel.add(comp);
		ver.hide();
		hor.hide();

		add(panel);
		add(hor);
		add(ver);

		setBackground(getBackground());
		hor.setBackground(getBackground());
		ver.setBackground(getBackground());
		panel.setBackground(getBackground());
	}

	@Override
	public void setBackground(Color c) {
		super.setBackground(c);
		hor.setBackground(c);
		ver.setBackground(c);
		panel.setBackground(c);
	}

	private static final int[] table = {
		Event.SCROLL_ABSOLUTE,
		Event.SCROLL_LINE_DOWN,
		Event.SCROLL_LINE_UP,
		Event.SCROLL_PAGE_DOWN,
		Event.SCROLL_PAGE_UP };

	private boolean drop(Event e) {
		if (!(e.target instanceof Scrollbar)) {
			return true;
		}
		return Arrays.stream(table).noneMatch(element -> e.id == element);
	}

	@Override
	public boolean handleEvent(Event evt) {
		if (drop(evt)) {
			return super.handleEvent(evt);
		}
		if (evt.target == ver) {
			comp.move(comp.bounds().x, -ver.getValue());
			checkValid();
			return true;
		}

		if (evt.target == hor) {
			comp.move(-hor.getValue(), comp.bounds().y);
			checkValid();
			return true;
		}
		return super.handleEvent(evt);
	}

	private void checkValid() {
		if (!hor.isEnabled()) {
			hor.hide();
			hor.enable();
			((Component) this).invalidate();
			validate();
		}
		if (!ver.isEnabled()) {
			ver.hide();
			ver.enable();
			((Component) this).invalidate();
			validate();
		}
		comp.repaint();
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		Dimension zet = comp.preferredSize();
		if ((zet.width == 0) || (zet.height == 0)) {
			zet = comp.size();
		}
		zet.width += ver.preferredSize().width;
		zet.height += hor.preferredSize().height;
		return zet;
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		Dimension zet = comp.minimumSize();
		if ((zet.width == 0) || (zet.height == 0)) {
			zet = comp.size();
		}
		zet.width += ver.preferredSize().width;
		zet.height += hor.preferredSize().height;
		return zet;
	}

	@Override
	public void layoutContainer(Container parent) {
		Dimension x = comp.preferredSize();
		if ((x.width == 0) || (x.height == 0)) {
			x = comp.size();
		}
		comp.resize(x.width, x.height);
		Rectangle r = comp.bounds();
		if ((r.x < 0) || (r.y < 0)) {
			comp.move(0, 0);
		}
		checkForSVH();
	}

	private void checkForSVH() {
		Dimension x = comp.preferredSize();
		if ((x.width == 0) || (x.height == 0)) {
			x = comp.size();
		}
		Rectangle r = bounds();
		int hor_h = hor.preferredSize().height;
		int ver_w = ver.preferredSize().width;
		int wx = r.width;
		int wy = r.height;

		boolean seth = (x.width > wx) || ((x.height > wy) && (x.width > (wx - ver_w)));
		boolean setv = (x.height > wy) || ((x.width > wx) && (x.height > (wy - hor_h)));

		if (!seth) {
			if (hor.isVisible()) {
				hor.hide();
			}
			comp.move(0, 0);
		}
		else {
			hor.move(0, r.height - hor_h);
			hor.resize(r.width - (setv ? ver_w : 0), hor_h);
			hor.show();
			wy -= hor_h;
			hor.setValues(0, wx - (setv ? ver_w : 0), 0, x.width);
			hor.setPageIncrement(wx / 2);
		}

		if (!setv) {
			if (ver.isVisible()) {
				ver.hide();
			}
			comp.move(0, 0);
		}
		else {
			ver.move(r.width - ver_w, 0);
			ver.resize(ver_w, r.height - (seth ? hor_h : 0));
			ver.show();
			wx -= ver_w;
			ver.setValues(0, wy, 0, x.height);
			ver.setPageIncrement(wy / 2);
		}
		panel.move(0, 0);
		panel.resize(wx, wy);
		if (x.width < wx) {
			x.width = wx; // To fit it horizontally
			comp.resize(x.width, x.height);
		}
	}

	public Scrollbar getVScrollbar() {
		return ver;
	}

	public Scrollbar getHScrollbar() {
		return hor;
	}

	public void scroll(int newX, int newY) {
		comp.move(-newX, -newY);
		hor.setValue(newX);
		ver.setValue(newY);
	}
}
