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
import java.awt.Insets;
import java.awt.Rectangle;

class AlignArea extends Align {
	private Dimension size = new Dimension(0, 0);
	private Insets insets = new Insets(0, 0, 0, 0);
	private boolean isValid;
	private Rectangle rect;

	@Override
	public void setAlign(int a) {
		if (getAlign() == a) {
			return;
		}
		super.setAlign(a);
		invalidate();
	}

	public void setSize(Dimension d) {
		if ((d != null) && (size != null)) {
			if ((d.width == size.width) && (d.height == size.height)) {
				return;
			}
		}

		invalidate();
		if (d == null) {
			size = null;
		}
		else {
			size = new Dimension(d.width, d.height);
		}
	}

	Dimension getSize() {
		if (size == null) {
			return null;
		}
		return new Dimension(size.width, size.height);
	}

	public void setInsets(Insets i) {
		if ((i != null) && (i.top == insets.top) && (i.left == insets.left) && (i.right == insets.right) && (i.bottom == insets.bottom)) {
			return;
		}

		invalidate();
		if (i == null) {
			insets = null;
		}
		else {
			insets = new Insets(i.top, i.left, i.bottom, i.right);
		}
	}

	public Insets getInsets() {
		return insets;
	}

	public Rectangle getAlignRectangle() {
		if (isValid) {
			if (rect == null) {
				return null;
			}
			return new Rectangle(rect.x, rect.y, rect.width, rect.height);
		}

		recalc();
		Dimension s = getSize();

		s.width -= (insets.left + insets.right);
		s.height -= (insets.top + insets.bottom);

		int sx = 0;
		int wx = getWidth(sx);
		int sy = 0;
		int wy = getHeight(sy);
		int xx = size.width - wx;
		int yy = size.height - wy;
		int a = getAlign();
		Rectangle r = new Rectangle(xx / 2, yy / 2, wx, wy);

		if ((a & AlignConstants.LEFT) > 0) {
			r.x = insets.left;
		}
		else if ((a & AlignConstants.RIGHT) > 0) {
			r.x = xx - insets.right;
		}

		if ((a & AlignConstants.TOP) > 0) {
			r.y = insets.top;
		}
		else if ((a & AlignConstants.BOTTOM) > 0) {
			r.y = yy - insets.bottom;
		}

		rect = r;
		validate();
		return r;
	}

	int getWidth(int s) {
		return s;
	}

	int getHeight(int s) {
		return s;
	}

	private void validate() {
		isValid = true;
	}

	void recalc() {
	}

	public void invalidate() {
		isValid = false;
	}

	public boolean isValid() {
		return isValid;
	}
}
