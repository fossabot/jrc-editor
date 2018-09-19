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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.StringTokenizer;

public class IELabel extends Canvas {
	public static final int LEFT = 0;
	private static final int CENTER = 1;
	private static final int RIGHT = 2;
	private static final int FIT = 3;

	private int wsize;
	private int hsize;

	private int ascent;

	private String name;
	private final int align;

	public IELabel() {
		align = LEFT;
		name = "";
	}

	public IELabel(String name) {
		align = LEFT;
		this.name = name;
	}

	public IELabel(String name, int align) {
		this.align = align;
		this.name = name;
	}

	public void setText(String s) {
		name = s;
		measure();
	}

	@Override
	public String toString() {
		return "IELabel[text=" + name + ",align=" + (align == LEFT ? "LEFT" : align == CENTER ? "CENTER" : "RIGHT") + "]";
	}

	@Override
	public void setFont(Font s) {
		super.setFont(s);
		measure();
	}

	@Override
	public void paint(Graphics gr) {
		try {
			measure();
			Dimension b = size();
			int xs;
			int ys = ascent + ((b.height - hsize) / 2);
			if (RIGHT == align) {
				xs = b.width - wsize;
			}
			else if (CENTER == align) {
				xs = (b.width - wsize) / 2;
			}
			else {
				xs = 1;
			}

			gr.setColor(getBackground());

			gr.fillRect(0, 0, b.width - 1, b.height - 1);

			gr.setColor(getForeground());
			if ((align != FIT) || (b.width <= wsize)) {
				gr.drawString(name, xs, ys);
			}
			else {
				drawText(gr, name, xs, ys, b.width);
			}
		}
		catch (Exception eee) {
			eee.printStackTrace();
		}
	}

	private void drawText(Graphics gr, String text, int xs, int ys, int max) {
		StringTokenizer st = new StringTokenizer(text, " \t");
		if (st.countTokens() == 0) {
			return;
		}
		String[] words = new String[st.countTokens()];

		FontMetrics fm = getFontMetrics(getFont());
		int j = 0;
		int comml = 0;
		while (st.hasMoreTokens()) {
			words[j] = st.nextToken();
			comml += fm.stringWidth(words[j]);
			++j;
		}
		int spc = (max - comml) / words.length;
		int spcd = (max - comml) % words.length;

		int k = spcd;
		for (j = 0; j < words.length; ++j) {
			gr.drawString(words[j], xs, ys);
			int v = k / words.length;
			if (v > 0) {
				k = k % words.length;
			}
			k += spcd;
			xs += fm.stringWidth(words[j]) + spc + v;
		}
	}

	@Override
	public Dimension preferredSize() {
		measure();
		return new Dimension(wsize, hsize);
	}

	@Override
	public Dimension minimumSize() {
		return preferredSize();
	}

	private void measure() {
		if (name == null) {
			return;
		}
		Font x = getFont();
		if (x == null) {
			return;
		}
		FontMetrics fm = getFontMetrics(x);
		if (fm == null) {
			Toolkit k = Toolkit.getDefaultToolkit();
			fm = k.getFontMetrics(x);
			if (fm == null) {
				return;
			}
		}
		hsize = fm.getHeight();
		ascent = fm.getAscent();
		wsize = fm.stringWidth(name) /*+10*/ ; // for bad fonts
	}
}
