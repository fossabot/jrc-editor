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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.MenuBar;
import java.awt.Rectangle;

public class ContextMenuBar extends MenuBar {
	final static int EV_MENU_REDRAW = 0xFFF0;
	final static int EV_MENU_EXIT = EV_MENU_REDRAW + 1;
	final static int EV_MENU_ENTER = EV_MENU_REDRAW + 2;
	final static int EV_MENU_REDRAWALL = EV_MENU_REDRAW + 3;

	private Container parent;
	private ContextMenu act_menu;
	private boolean first = true;

	public ContextMenuBar(Container a_parent) {
		parent = a_parent;
	}

	public void setParent(Container a_parent) {
		parent = a_parent;
	}

	public Dimension getParentSize() {
		if (parent == null) {
			return null;
		}
		else {
			return parent.size();
		}
	}

	void init(int index, int x, int y) {
		act_menu = get(index);
		if ((act_menu == null) || (!act_menu.isEnabled())) {
			act_menu = null;
			return;
		}
		act_menu.setPos(x, y);
		first = true;
	}

	void paint(Graphics gr) {
		if (act_menu == null) {
			return;
		}
		if (first) {
			first = false;
			act_menu.paint(gr);
		}
		else {
			act_menu.paintPart(gr);
		}
	}

	public boolean isActive() {
		return (parent != null) && (act_menu != null);
	}

	private ContextMenu get(int index) {
		return (ContextMenu) super.getMenu(index);
	}

	private void repaint() {
		if (parent == null) {
			return;
		}
		Graphics gr = parent.getGraphics();
		paint(gr);
		gr.dispose();
	}

	private void repaintAll() {
		first = true;
		if ((act_menu != null) && (parent != null)) {
			Rectangle r = act_menu.getBounds();
			parent.repaint(r.x, r.y, r.width, r.height);
		}
		repaint();
	}

	void sendEvent(Event evt) {
		if ((parent == null) || (act_menu == null)) {
			return;
		}
		switch (evt.id) {
			case ContextMenuBar.EV_MENU_REDRAW: {
				repaint();
			}
				break;

			case ContextMenuBar.EV_MENU_REDRAWALL: {
				repaintAll();
			}
				break;
			case ContextMenuBar.EV_MENU_EXIT: {
				repaintAll();
				act_menu = null;
			}
				break;
			case ContextMenuBar.EV_MENU_ENTER: {
				repaintAll();
				act_menu = null;
				evt.id = Event.ACTION_EVENT;
				parent.postEvent(evt);
			}
		}
	}

	boolean handleEvent(Event evt) {
		if ((act_menu == null) || (!act_menu.isEnabled())) {
			return false;
		}
		return act_menu.handleEvent(evt);
	}
}
