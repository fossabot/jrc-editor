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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.IntStream;

public class EmulatedTextArea extends EmulatedTextField implements ScrollObject {
	private boolean wordWrap;
	private int[] lineStart = new int[LINE_INCR];
	private final List<String> lineText = new ArrayList<>();
	private int maxTextWidth;
	private int upRowNum;
	private int baseTextIndent;
	private int viewWidth;
	private int viewHeight;
	private Image internImg;
	private int textHeight;
	private int lastVisLine;
	private boolean addLineFeed = true;
	private boolean noFontMetric;

	private static final int LINE_INCR = 20;

	private EmulatedTextArea() {
		super();
		lineText.add("");
	}

	public EmulatedTextArea(boolean ww, boolean lf) {
		this();
		wordWrap = ww;
		addLineFeed = lf;
	}

	@Override
	public void setText(String s) {
		super.setText(s);
		recalcLines(0);
	}

	@Override
	public void focusGained(FocusEvent e) {
		int wasCP = cursorPos;
		super.focusGained(e);
		setSelPos(0);
		setSelWidth(0);
		cursorPos = wasCP;
	}

	@Override
	protected boolean controlKey(int key, boolean shift) {
		switch (key) {
			case KeyEvent.VK_DOWN:
				seek(vertPosShift(cursorPos, 1) - cursorPos, shift);
				break;
			case KeyEvent.VK_UP:
				seek(vertPosShift(cursorPos, -1) - cursorPos, shift);
				break;
			case KeyEvent.VK_HOME:
				seek(lineStart[lineFromPos(cursorPos)] - cursorPos, shift);
				break;
			case KeyEvent.VK_END:
				int ln = lineFromPos(cursorPos);
				int newPos = buffer.toString().length();
				if (ln < (lineText.size() - 1)) {
					newPos = adjustPos(lineStart[ln + 1] - 1, false);
				}
				seek(newPos - cursorPos, shift);
				break;
			case KeyEvent.VK_ENTER:
				return false;
			case KeyEvent.VK_PAGE_UP:
				upRowNum -= lastVisLine;
				if (upRowNum < 0) {
					upRowNum = 0;
				}
				seek(vertPosShift(cursorPos, -lastVisLine) - cursorPos, shift);
				break;
			case KeyEvent.VK_PAGE_DOWN:
				upRowNum += lastVisLine;
				if ((upRowNum + lastVisLine) >= lineText.size()) {
					upRowNum = lineText.size() - lastVisLine - 1;
				}
				seek(vertPosShift(cursorPos, lastVisLine) - cursorPos, shift);
				break;
			default:
				return super.controlKey(key, shift);
		}
		if (!shift) {
			clear();
		}
		return true;
	}

	@Override
	protected boolean write(char key) {
		super.write(key);
		if (addLineFeed && (key == '\n')) {
			super.write('\r');
		}
		recalcLines(cursorPos);
		return true;
	}

	@Override
	protected String filterSymbols(String s) {
		return s;
	}

	@Override
	protected void repaintPart() {
		repaint();
	}

	@Override
	protected void remove(int pos, int size) {
		if ((pos + size) > buffer.length()) {
			size = buffer.length() - pos;
		}
		if ((pos > buffer.length()) || (size <= 0)) {
			return;
		}
		if ((pos > 0) && (buffer.charAt(pos) == '\n') && (buffer.charAt(pos - 1) == '\r')) {
			pos--;
			size++;
		}
		if (((pos + size) < buffer.length()) && (buffer.charAt((pos + size) - 1) == '\r') && (buffer.charAt(pos + size) == '\n')) {
			size++;
		}
		super.remove(pos, size);
		recalcLines(pos);
	}

	@Override
	public void insert(int pos, String str) {
		super.insert(pos, str);
		recalcLines(pos);
	}

	@Override
	public void update(Graphics g) {
		paint(g);
	}

	@Override
	public void paint(Graphics g) {
		Dimension d = size();
		if ((d.width != viewWidth) || (d.height != viewHeight) || (internImg == null)) {
			if ((d.width * d.height) <= 0) {
				return;
			}
			internImg = createImage(d.width, d.height);
			viewWidth = d.width;
			viewHeight = d.height;
		}
		Graphics internGr = internImg.getGraphics();
		recalc();
		internGr.clearRect(0, 0, d.width, d.height);
		drawBorder(internGr);
		internGr.clipRect(insets.left, insets.top, (d.width - insets.left - insets.right) + 1, d.height - insets.top - insets.bottom);
		drawCursor(internGr);
		drawText(internGr);
		drawBlock(internGr);
		g.drawImage(internImg, 0, 0, this);
		internGr.dispose();
	}

	@Override
	void drawBlock(Graphics g) {
		if (!isSelected()) {
			return;
		}
		FontMetrics fm = getFontMetrics(getFont());
		int l1 = lineFromPos(getSelPos());
		int l2 = lineFromPos(getSelPos() + getSelWidth());
		for (int i = l1; i <= l2; i++) {
			String s = lineText.get(i);
			int beg = 0;
			int begPos = 0;
			if (i == l1) {
				begPos = getSelPos() - lineStart[i];
				beg = fm.stringWidth(s.substring(0, begPos));
			}
			int end = fm.stringWidth(s);
			int endPos = s.length();
			if (i == l2) {
				endPos = (getSelPos() + getSelWidth()) - lineStart[i];
				end = fm.stringWidth(s.substring(0, endPos));
			}
			g.setColor(Color.blue);
			g.fillRect(textLocation.x + shift.x + beg, insets.top + ((i - upRowNum) * fm.getHeight()), end - beg, textSize.height);
			g.setColor(Color.white);
			g.drawString(s.substring(begPos, endPos), insets.left + beg + shift.x,
				insets.top + baseTextIndent + ((i - upRowNum) * fm.getHeight()));
		}
	}

	@Override
	void drawText(Graphics g) {
		size();
		g.setColor(getForeground());
		for (int i = upRowNum; i < lineText.size(); i++) {
			g.drawString(lineText.get(i), insets.left + shift.x, insets.top + baseTextIndent + ((i - upRowNum) * textSize.height));
		}
	}

	@Override
	public Dimension preferredSize() {

		Font f = getFont();
		if (f == null) {
			return new Dimension(0, 0);
		}
		FontMetrics m = getFontMetrics(f);
		if (m == null) {
			Toolkit k = Toolkit.getDefaultToolkit();
			m = k.getFontMetrics(f);
			if (m == null) {
				return new Dimension(0, 0);
			}
		}

		String text = getText();
		int rows = 1;
		rows += IntStream.range(0, text.length()).filter(j -> text.charAt(j) == '\n').count();
		StringTokenizer st = new StringTokenizer(text, "\n");
		int h = m.getHeight() * (rows + 1);
		int w = 0;
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			w = Math.max(w, m.stringWidth(s));
		}
		Insets i = insets();
		return new Dimension(i.left + i.right + w, i.top + i.bottom + h);
	}

	@Override
	public Dimension getSOSize() {
		return new Dimension(maxTextWidth + insets.left + insets.right, (lineText.size() * textSize.height) + insets.top + insets.bottom);
	}

	private void setLineStart(int pos, int value) {
		if (pos >= lineStart.length) {
			int[] nls = new int[((pos / LINE_INCR) + 1) * LINE_INCR];
			System.arraycopy(lineStart, 0, nls, 0, lineStart.length);
			lineStart = nls;
		}
		lineStart[pos] = value;
	}

	private int indexOfBlank(String s, int i) {
		int i1 = s.indexOf(' ', i);
		if (i1 < 0) {
			i1 = s.length() - 1;
		}
		int i2 = s.indexOf('\t', i);
		if (i2 < 0) {
			i2 = s.length() - 1;
		}
		return Math.min(i1, i2);
	}

	private void recalcLines(int position) {
		int rowNum = lineFromPos(position);
		lineText.size();
		Dimension d = size();
		Insets i = insets();
		int textWidth = d.width - i.left - i.right;
		textHeight = d.height - i.top - i.bottom;
		if ((textWidth <= 0) || (textHeight <= 0)) {
			return;
		}
		noFontMetric = true;
		Font f = getFont();
		if (f == null) {
			return;
		}
		FontMetrics m = getFontMetrics(f);
		if (m == null) {
			return;
		}
		noFontMetric = false;
		if (rowNum > lineText.size()) {
			return;
		}
		String allText = buffer.toString();
		int currLine = rowNum;
		if (lineText.size() > currLine) {
			lineText.subList(currLine, lineText.size()).clear();
		}
		setLineStart(0, 0);
		int currPos = lineStart[currLine];
		int ind;
		do {
			ind = allText.indexOf('\n', currPos);
			int startNext = ind + 1;
			if (ind < 0) {
				ind = allText.length();
			}
			if ((ind > 0) && (allText.charAt(ind - 1) == '\r')) {
				ind--;
			}
			String sl = allText.substring(currPos, ind);
			if (wordWrap && (m.stringWidth(sl) > textWidth)) {
				int blankInd = indexOfBlank(allText, currPos);
				if (blankInd < ind) {
					ind = blankInd + 1;
					sl = allText.substring(currPos, ind);
					String tempSl = sl;
					while (m.stringWidth(tempSl) < textWidth) {
						sl = tempSl;
						ind = blankInd + 1;
						blankInd = indexOfBlank(allText, ind);
						tempSl = allText.substring(currPos, blankInd + 1);
					}
					startNext = ind;
				}
			}
			lineText.add(sl);
			setLineStart(currLine, currPos);
			currPos = startNext;
			currLine++;
		} while (ind < allText.length());
		maxTextWidth = 0;
		for (String aLineText : lineText) {
			int len = m.stringWidth(aLineText);
			if (maxTextWidth < len) {
				maxTextWidth = len;
			}
		}
	}

	@Override
	protected void setPos(int p) {
		super.setPos(adjustPos(p, true));
	}

	@Override
	public void select(int pos, int w) {
		int ap = adjustPos(pos, true);
		int aw = adjustPos(pos + w, true) - ap;
		super.select(ap, aw);
	}

	@Override
	boolean seek(int shift, boolean b) {
		return super.seek(adjustPos(cursorPos + shift, shift > 0) - cursorPos, b);
	}

	private int adjustPos(int pos, boolean incr) {
		int l = lineFromPos(pos);
		int sl = lineText.get(l).length();
		if ((l < (lineText.size() - 1)) && ((pos - lineStart[l]) > sl)) {
			if (incr) {
				return lineStart[l + 1];
			}
			else {
				return lineStart[l] + sl;
			}
		}
		return pos;
	}

	@Override
	boolean recalc() {
		int wasShiftX = shift.x;
		if (noFontMetric) {
			recalcLines(0);
		}
		boolean res = super.recalc();
		shift.x = wasShiftX;
		int l = lineFromPos(cursorPos);
		String s = lineText.get(l);
		s = s.substring(0, cursorPos - lineStart[l]);
		FontMetrics m = getFontMetrics(getFont());
		shift.y = 0;
		baseTextIndent = m.getHeight() - m.getDescent();
		int cursLine = lineFromPos(cursorPos);
		if (cursLine < upRowNum) {
			upRowNum = cursLine;
		}
		lastVisLine = (textHeight / m.getHeight()) - 1;
		if (lastVisLine < 0) {
			lastVisLine = 0;
		}
		if (cursLine > (lastVisLine + upRowNum)) {
			upRowNum = cursLine - lastVisLine;
		}
		cursorLocation.x = insets.left + m.stringWidth(s);
		cursorLocation.y = insets.top + ((l - upRowNum) * textSize.height);
		if ((cursorLocation.x + shift.x) < insets.left) {
			shift.x = insets.left - cursorLocation.x;
		}
		else {
			int w = size().width - insets.right;
			if ((cursorLocation.x + shift.x) > w) {
				shift.x = w - cursorLocation.x;
			}
		}
		return res;
	}

	private int getLinePos(int ln, FontMetrics fm, int pix) {
		if (ln < 0) {
			ln = 0;
		}
		if (ln >= lineText.size()) {
			ln = lineText.size() - 1;
		}
		String s = lineText.get(ln);
		for (int i = 0; i < s.length(); i++) {
			if (fm.stringWidth(s.substring(0, i)) > pix) {
				return (lineStart[ln] + i) - 1;
			}
		}
		int res = lineStart[ln] + s.length();
		if ((pix > 0) && (ln < (lineText.size() - 1)) && (buffer.charAt(lineStart[ln + 1] - 1) != '\n')) {
			res--;
		}
		return res;
	}

	@Override
	protected int calcTextPos(int x, int y) {
		FontMetrics fm = getFontMetrics(getFont());
		return getLinePos(((y - insets.top) / fm.getHeight()) + upRowNum, fm, x - insets.left - shift.x);
	}

	private int vertPosShift(int currPos, int vertShift) {
		int currLine = lineFromPos(currPos);
		FontMetrics fm = getFontMetrics(getFont());
		int pixW = fm.stringWidth(lineText.get(currLine).substring(0, currPos - lineStart[currLine]));
		return getLinePos(currLine + vertShift, fm, pixW);
	}

	private int lineFromPos(int pos) {
		for (int i = lineText.size() - 1; i >= 0; i--) {
			if (lineStart[i] <= pos) {
				return i;
			}
		}
		return 0;
	}

	@Override
	public void resize(int w, int h) {
		super.resize(w, h);
		recalcLines(0);
	}

	@Override
	public void reshape(int x, int y, int w, int h) {
		Dimension d = size();
		super.reshape(x, y, w, h);
		if (d.width != w) {
			recalcLines(0);
		}
	}
}
