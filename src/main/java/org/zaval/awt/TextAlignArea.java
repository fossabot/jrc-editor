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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class TextAlignArea extends AlignArea {
	private static final int STRING_GAP = 0;

	private String text;
	private String[] strs = new String[0];
	private FontMetrics fontMetrics;
	private boolean isMulti;

	public TextAlignArea() {
		setAlign(AlignConstants.LEFT);
	}

	@Override
	protected int getWidth(int s) {
		if ((fontMetrics == null) || (text == null)) {
			return -1;
		}

		if (!isMulti) {
			return fontMetrics.stringWidth(text);
		}

		int max = -1;
		for (String str : strs) {
			int len = fontMetrics.stringWidth(str);
			if (max < len) {
				max = len;
			}
		}
		return max;
	}

	@Override
	protected int getHeight(int s) {
		if ((fontMetrics == null) || (text == null)) {
			return -1;
		}
		if (!isMulti) {
			return fontMetrics.getHeight();
		}
		return (fontMetrics.getHeight() * strs.length) + ((strs.length - 1) * STRING_GAP);
	}

	@Override
	protected void recalc() {
		Dimension d = getSize();
		if (fontMetrics == null) {
			strs = null;
		}
		else {
			strs = breakString(text, fontMetrics, d.width);
		}
	}

	public void setMultiLine(boolean b) {
		if (isMulti == b) {
			return;
		}
		isMulti = b;
		invalidate();
	}

	public void setFontMetrics(FontMetrics f) {
		fontMetrics = f;
		invalidate();
	}

	public void setText(String t) {
		if ((t != null) && t.equals(text)) {
			return;
		}
		text = t;
		invalidate();
	}

	public String getText() {
		return text;
	}

	public FontMetrics getFontMetrics() {
		return fontMetrics;
	}

	void draw(Graphics g, Color col) {
		draw(g, 0, 0, col);
	}

	private void drawText(Graphics gr, String text, int xs, int ys, int max) {
		StringTokenizer st = new StringTokenizer(text, ":");
		String left = st.nextToken() + ":";
		String right = st.nextToken();

		FontMetrics fm = fontMetrics;
		fm.stringWidth(left);
		int commr = 0;
		commr += fm.stringWidth(right);

		gr.drawString(left, xs, ys);
		gr.drawString(right, (xs + max) - commr, ys);
	}

	private void drawJText(Graphics gr, String text, int xs, int ys, int max) {
		int j = 0;
		int sizes = 0;
		StringTokenizer st = new StringTokenizer(text, "\t ");
		String[] words = new String[st.countTokens()];
		FontMetrics fm = fontMetrics;

		for (; st.hasMoreTokens(); ++j) {
			words[j] = st.nextToken();
			sizes += fm.stringWidth(words[j]);
		}
		double space = (max - sizes) / (words.length - 1);
		for (j = 0; j < words.length; ++j) {
			gr.drawString(words[j], xs, ys);
			xs += space;
		}
	}

	void draw(Graphics g, int offx, int offy, Color col) {
		Dimension d = getSize();
		Insets ins = getInsets();
		Rectangle r = getAlignRectangle();

		int h = fontMetrics.getHeight();
		int y = (r.y + fontMetrics.getHeight() + offy) - fontMetrics.getDescent();
		g.setColor(col);

		int x = r.x + offx;
		if (isMulti) {
			for (int i = 0; i < strs.length; i++) {
				if ((getAlign() & AlignConstants.RIGHT) > 0) {
					int len = fontMetrics.stringWidth(strs[i]);
					if (len > d.width) {
						x = ins.left + offx;
					}
					else {
						x = (r.x + offx + r.width) - len;
					}
				}
				if ((getAlign() & AlignConstants.FIT) > 0) {
					drawText(g, strs[i], x, y, d.width);
				}
				else if (((i + 1) != strs.length) && ((getAlign() & AlignConstants.JUSTIFY) > 0)) {
					drawJText(g, strs[i], x, y, d.width);
				}
				else {
					g.drawString(strs[i], x, y);
				}
				y += (h + STRING_GAP);
			}
		}
		else {
			g.drawString(text, r.x, y);
		}
	}

	private static void next(String s, String[] res) {
		int index = s.indexOf(' ');
		if (index < 0) {
			res[0] = s;
			res[1] = null;
			return;
		}

		if (index == 0) {
			index++;
		}
		res[0] = s.substring(index);
		res[1] = s.substring(0, index);
	}

	public static String[] breakString(String t, FontMetrics fm, int width) {
		if (t != null) {
			StringTokenizer st = new StringTokenizer(t, "\n");
			List<String> vv = new ArrayList<>();

			while (st.hasMoreTokens()) {
				vv.add(st.nextToken());
			}

			List<String> vvv = new ArrayList<>();
			String[] ps = new String[2];

			for (String ss : vv) {
				StringBuilder tk = new StringBuilder();
				int c = 0;

				for (int tw = 0;;) {
					next(ss, ps);

					String token;
					if (ps[1] != null) {
						token = ps[1];
					}
					else {
						token = ps[0];
					}
					ss = ps[0];

					int len = fm.stringWidth(token);
					if ((tw + len) > width) {
						if (tk.length() > 0) {
							vvv.add(tk.toString());
							tk = new StringBuilder(token);
							tw = len;
						}
						else {
							vvv.add(token);
							tk = new StringBuilder();
							tw = 0;
						}
					}
					else {
						c++;
						tk.append(token);
						tw += len;
					}

					if (ps[1] == null) {
						break;
					}
				}

				if (c > 0) {
					vvv.add(tk.toString());
				}

			}

			return vvv.stream().toArray(String[]::new);
		}
		return null;
	}
}
