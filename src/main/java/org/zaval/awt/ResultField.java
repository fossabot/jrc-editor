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
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

public class ResultField extends Canvas {
	private final TextAlignArea alignArea = new TextAlignArea();

	public ResultField() {
		alignArea.setInsets(new Insets(0, 2, 0, 0));
	}

	public TextAlignArea getAlignArea() {
		return alignArea;
	}

	public void setText(String text) {
		alignArea.setText((text));
		repaint();
	}

	@Override
	public void setFont(Font f) {
		super.setFont(f);
		alignArea.invalidate();
	}

	@Override
	public void reshape(int x, int y, int w, int h) {
		super.reshape(x, y, w, h);
		alignArea.setSize(new Dimension(w, h));
	}

	@Override
	public void resize(int w, int h) {
		super.resize(w, h);
		alignArea.setSize(new Dimension(w, h));
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (alignArea.getFontMetrics() == null) {
			alignArea.setFontMetrics(getFontMetrics(getFont()));
		}
		alignArea.draw(g, getForeground());
	}

	@Override
	public Dimension preferredSize() {
		if (!isValid()) {
			validate();
		}
		Rectangle r = alignArea.getAlignRectangle();
		return new Dimension(r.width, r.height);
	}

	@Override
	public boolean mouseDown(Event e, int x, int y) {
		requestFocus();
		return true;
	}

	@Override
	public boolean mouseUp(Event e, int x, int y) {
		return true;
	}

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
