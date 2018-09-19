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

import java.awt.Dimension;
import java.awt.Scrollbar;

import org.zaval.awt.util.Metrics;

class ScrollController extends Metrics {
	static final int SCROLL_SIZE = 16;

	private ScrollArea area;
	private ScrollObject sobj;

	public ScrollController(ScrollArea a, ScrollObject o) {
		setScrollArea(a);
		setScrollObject(o);
	}

	public int getMaxHorScroll() {
		Scrollbar hBar = area.getHBar();
		Scrollbar vBar = area.getVBar();
		if (hBar == null) {
			return -1;
		}
		Dimension r = sobj.getSOSize();
		Dimension d = area.getSASize();
		int wx = d.width;
		int wy = d.height;
		boolean needVBar = false;
		boolean b1 = (r.width > wx);

		if (vBar != null) {
			if ((r.height > wy) || (b1 && (r.height > (wy - SCROLL_SIZE)))) {
				needVBar = true;
				wx -= SCROLL_SIZE;
			}
		}

		boolean b2 = (needVBar && (r.width > wx));
		if (b1 || b2) {
			int max = (r.width - wx);
			return max + 1;
		}

		return -1;
	}

	public int getMaxVerScroll() {
		Scrollbar hBar = area.getHBar();
		Scrollbar vBar = area.getVBar();
		if (vBar == null) {
			return -1;
		}
		Dimension r = sobj.getSOSize();
		Dimension d = area.getSASize();
		int wx = d.width;
		int wy = d.height;
		boolean needHBar = false;

		boolean b1 = (r.height > wy);
		if (hBar != null) {
			if ((r.width > wx) || (b1 && (r.width > (wx - SCROLL_SIZE)))) {
				wy -= SCROLL_SIZE;
				needHBar = true;
			}
		}

		boolean b2 = (needHBar && (r.height > wy));
		if (b1 || b2) {
			int max = (r.height - wy);
			return max + 1;
		}

		return -1;
	}

	private void setScrollArea(ScrollArea a) {
		area = a;
		invalidate();
	}

	private void setScrollObject(ScrollObject o) {
		sobj = o;
		invalidate();
	}

}
