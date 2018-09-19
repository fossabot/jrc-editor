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
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class EmulatedTextField extends Canvas implements KeyListener, MouseListener, FocusListener, ActionListener {
	private static EmulatedTextField cursorOwner;

	StringBuffer buffer = new StringBuffer();
	final Insets insets = new Insets(2, 5, 2, 5);
	final Point textLocation = new Point(0, 0);
	final Point cursorLocation = new Point(0, 0);
	final Dimension textSize = new Dimension(0, 0);
	private final Dimension cursorSize = new Dimension(0, 0);
	final Point shift = new Point(0, 0);
	private final Color cursorColor = Color.black;
	int cursorPos;
	private int selPos;
	private int selWidth;
	private int startSel;
	private final PopupMenu menu;
	private final int minSize;

	public EmulatedTextField() {
		this(0);
	}

	public EmulatedTextField(int size) {
		enableInputMethods(true);
		addKeyListener(this);
		addMouseListener(this);
		addFocusListener(this);

		menu = new PopupMenu();
		add(menu);
		MenuItem menuItemCut = new MenuItem("Cut");
		menu.add(menuItemCut);
		menuItemCut.addActionListener(this);
		menuItemCut.setActionCommand("Cut");
		MenuItem menuItemCopy = new MenuItem("Copy");
		menu.add(menuItemCopy);
		menuItemCopy.addActionListener(this);
		menuItemCopy.setActionCommand("Copy");
		MenuItem menuItemPaste = new MenuItem("Paste");
		menu.add(menuItemPaste);
		menuItemPaste.addActionListener(this);
		menuItemPaste.setActionCommand("Paste");
		menu.addActionListener(this);
		minSize = size;
	}

	void setSelPos(int selPos) {
		this.selPos = selPos;
	}

	void setSelWidth(int selWidth) {
		this.selWidth = selWidth;
	}

	int getSelPos() {
		return selPos;
	}

	int getSelWidth() {
		return selWidth;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			case "Cut":
				blCopy();
				removeBlock();
				break;
			case "Copy":
				blCopy();
				break;
			case "Paste":
				blPaste();
				break;
		}
	}

	public void setText(String s) {
		buffer = new StringBuffer(s);
		setPos(0);
	}

	public String getText() {
		return buffer.toString();
	}

	@Override
	public boolean hasFocus() {
		return (this == cursorOwner);
	}

	@Override
	public void keyTyped(KeyEvent ke) {
		int key = ke.getKeyCode();
		ke.getModifiers();
		boolean isShift = (ke.getModifiers() & InputEvent.SHIFT_MASK) != 0;
		if (controlKey(key, isShift)) {
			return;
		}

		if (ke.isActionKey()) {
			return;
		}
		char c = ke.getKeyChar();
		if (c == KeyEvent.CHAR_UNDEFINED) {
			return;
		}

		if ((c == 0x7F) || (c == 0x1B)) {
			return;
		}
		if (ke.isControlDown()) {
			return;
		}
		if ((c != '\b') && (c != '\t')) {
			removeBlock();
			if (write(c)) {
				seek(1, false);
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		int key = ke.getKeyCode();
		boolean isCtrl = (ke.getModifiers() & InputEvent.CTRL_MASK) != 0;
		boolean isShift = (ke.getModifiers() & InputEvent.SHIFT_MASK) != 0;
		if (blockKey(key, isCtrl, isShift)) {
			return;
		}
		if (controlKey(key, isShift)) {
			return;
		}
		if (key == KeyEvent.VK_F12) {
			menu.show(this, 0, 0);
		}
	}

	@Override
	public void keyReleased(KeyEvent ke) {
	}

	private boolean blockKey(int key, boolean isCtrlDown, boolean isShiftDown) {
		if (isCtrlDown && ((key == KeyEvent.VK_INSERT) || (key == KeyEvent.VK_C))) {
			blCopy();
		}
		else if ((isShiftDown && (key == KeyEvent.VK_INSERT)) || (isCtrlDown && (key == KeyEvent.VK_V))) {
			blPaste();
		}
		else if ((isShiftDown && (key == KeyEvent.VK_DELETE)) || (isCtrlDown && (key == KeyEvent.VK_X))) {
			blDelete();
		}
		else if (isCtrlDown && (key == KeyEvent.VK_RIGHT)) {
			seek(nextSpace() - cursorPos, isShiftDown);
		}
		else if (isCtrlDown && (key == KeyEvent.VK_LEFT)) {
			seek(prevSpace() - cursorPos, isShiftDown);
		}
		else {
			return isCtrlDown;
		}
		return true;
	}

	private int prevSpace() {
		int j = cursorPos;
		if (j >= buffer.length()) {
			j = buffer.length() - 1;
		}
		for (; (j > 0) && Character.isSpaceChar(buffer.charAt(j)); --j) {
			// skip
		}
		for (; (j > 0) && !Character.isSpaceChar(buffer.charAt(j)); --j) {
			// skip
		}
		return j;
	}

	private int nextSpace() {
		int j = cursorPos;
		int k = buffer.length();
		if (j >= k) {
			return cursorPos;
		}
		for (; (j < k) && Character.isSpaceChar(buffer.charAt(j)); ++j) {
			// skip
		}
		for (; (j < k) && !Character.isSpaceChar(buffer.charAt(j)); ++j) {
			// skip
		}
		return j;
	}

	boolean controlKey(int key, boolean shift) {
		switch (key) {
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_RIGHT:
				seek(1, shift);
				break;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_LEFT:
				seek(-1, shift);
				break;
			case KeyEvent.VK_END:
				seek2end(shift);
				break;
			case KeyEvent.VK_HOME:
				seek2beg(shift);
				break;
			case KeyEvent.VK_DELETE:
				if (!isSelected()) {
					remove(cursorPos, 1);
				}
				else {
					removeBlock();
				}
				break;
			case KeyEvent.VK_BACK_SPACE:
				if (!isSelected()) {
					if (cursorPos > 0) {
						seek(-1, shift);
						remove(cursorPos, 1);
					}
				}
				else {
					removeBlock();
				}
				break;
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_PAGE_UP:
			case KeyEvent.VK_PAGE_DOWN:
				break;
			case KeyEvent.VK_INSERT:
				return false;
			case KeyEvent.VK_TAB:
				return true;
			default:
				return false;
		}
		if (!shift) {
			clear();
		}
		return true;
	}

	public void blPaste() {
		String s = filterSymbols(readFromClipboard());
		removeBlock();
		insert(cursorPos, s);
		setPos(cursorPos + s.length());
	}

	String filterSymbols(String s) {
		return s;
	}

	public void blCopy() {
		if (!isSelected()) {
			return;
		}
		writeToClipboard(buffer.toString().substring(selPos, selPos + selWidth));
	}

	public void blDelete() {
		if (!isSelected()) {
			return;
		}
		writeToClipboard(buffer.toString().substring(selPos, selPos + selWidth));
		removeBlock();
	}

	private void removeBlock() {
		if (isSelected()) {
			remove(selPos, selWidth);
			setPos(selPos);
			clear();
		}
	}

	@Override
	public void paint(Graphics g) {
		recalc();
		drawBorder(g);
		drawCursor(g);
		drawText(g);
		drawBlock(g);
	}

	Insets insets() {
		return insets;
	}

	void drawBlock(Graphics g) {
		if (!isSelected()) {
			return;
		}
		String s = buffer.toString();
		FontMetrics fm = getFontMetrics(getFont());
		int beg = fm.stringWidth(s.substring(0, selPos));
		int end = fm.stringWidth(s.substring(0, selPos + selWidth));
		g.setColor(Color.blue);
		g.fillRect(textLocation.x + shift.x + beg, cursorLocation.y + shift.y, end - beg, textSize.height);
		g.setColor(Color.white);
		g.drawString(s.substring(selPos, selPos + selWidth), textLocation.x + shift.x + beg, textLocation.y + shift.y);
	}

	void drawText(Graphics g) {
		Dimension d = size();
		g.clipRect(insets.left, insets.top, d.width - insets.left - insets.right, d.height - insets.top - insets.bottom);
		g.setColor(getForeground());
		g.drawString(buffer.toString(), textLocation.x + shift.x, textLocation.y + shift.y);
	}

	void drawCursor(Graphics g) {
		if (cursorOwner != this) {
			return;
		}
		g.setColor(cursorColor);
		g.fillRect(cursorLocation.x + shift.x, cursorLocation.y + shift.y, cursorSize.width, cursorSize.height);
	}

	public Rectangle getCursorShape() {
		return new Rectangle(cursorLocation.x + shift.x, cursorLocation.y + shift.y, cursorSize.width, cursorSize.height);
	}

	void drawBorder(Graphics g) {
		Dimension d = size();
		g.setColor(Color.gray);
		g.drawLine(0, 0, d.width - 1, 0);
		g.drawLine(0, 0, 0, d.height - 1);
		g.setColor(Color.black);
		g.drawLine(1, 1, d.width - 3, 1);
		g.drawLine(1, 1, 1, d.height - 3);
		g.setColor(Color.white);
		g.drawLine(0, d.height - 1, d.width - 1, d.height - 1);
		g.drawLine(d.width - 1, 0, d.width - 1, d.height - 1);
	}

	boolean seek(int shift, boolean b) {
		int len = buffer.length();
		int npos = getValidPos(shift);

		if ((npos > len) || (npos < 0)) {
			return false;
		}

		if (!isSelected() && b) {
			startSel = cursorPos;
		}

		setPos(npos);

		if (b) {
			if (cursorPos < startSel) {
				select(cursorPos, startSel - cursorPos);
			}
			else {
				select(startSel, cursorPos - startSel);
			}
		}

		return true;
	}

	private int getValidPos(int shift) {
		return cursorPos + shift;
	}

	private void seek2end(boolean b) {
		seek(buffer.length() - cursorPos, b);
	}

	private void seek2beg(boolean b) {
		seek(-cursorPos, b);
	}

	boolean write(char key) {
		buffer.insert(cursorPos, key);
		return true;
	}

	void remove(int pos, int size) {
		if ((pos > buffer.length()) || (pos < 0)) {
			return;
		}
		if ((pos + size) > buffer.length()) {
			size = buffer.length() - pos;
		}
		String s = buffer.toString();
		s = s.substring(0, pos) + s.substring(pos + size);
		buffer = new StringBuffer(s);
		repaintPart();
	}

	void insert(int pos, String str) {
		if ((pos > buffer.length()) || (pos < 0)) {
			return;
		}
		String s = buffer.toString();
		s = s.substring(0, pos) + str + s.substring(pos);
		buffer = new StringBuffer(s);
		repaintPart();
	}

	private long clickTime;

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();

		if (cursorOwner != this) {
			requestFocus();
		}
		int pos = calcTextPos(x, y);
		if ((pos >= 0) && (pos != cursorPos)) {
			setPos(pos);
		}

		if (e.isPopupTrigger() || e.isShiftDown()) {
			menu.show(this, x, y);
			return;
		}
		else if (isSelected() && !e.isShiftDown()) {
			clear();
		}

		long t = System.currentTimeMillis();
		if ((t - clickTime) < 300) {
			select(0, buffer.length());
		}
		clickTime = t;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	int calcTextPos(int x, int y) {
		if (buffer.length() == 0) {
			return 0;
		}

		if (x > (shift.x + textSize.width + textLocation.x)) {
			return buffer.length();
		}

		if ((shift.x + textLocation.x) > x) {
			return 0;
		}

		int w = x - shift.x;
		int p = (w * 100) / textSize.width;
		int l = buffer.length();
		int s = (l * p) / 100;

		FontMetrics fm = getFontMetrics(getFont());
		String ss = buffer.toString();
		for (int i = s, j = s + 1, k = i; (i >= 0) || (j < l);) {
			if ((k >= 0) && (k < l)) {
				char ch = buffer.charAt(k);
				String sx = ss.substring(0, k);
				int sl = fm.stringWidth(sx) + shift.x + textLocation.x;
				int cl = fm.charWidth(ch);
				if ((x >= sl) && (x < (sl + cl))) {
					if (x > (sl + (cl / 2))) {
						return k + 1;
					}
					else {
						return k;
						//return 1;
					}
				}
			}

			if (k == j) {
				i--;
				j++;
				k = i;
			}
			else {
				k = j;
			}
		}

		return -1;
	}

	void setPos(int p) {
		cursorPos = p;
		repaintPart();
	}

	private Dimension calcSize() {
		Font f = getFont();
		if (f == null) {
			return new Dimension(0, 25);
		}
		FontMetrics m = getFontMetrics(f);
		if (m == null) {
			Toolkit k = Toolkit.getDefaultToolkit();
			m = k.getFontMetrics(f);
			if (m == null) {
				return new Dimension(0, 25);
			}
		}
		Insets i = insets();
		String t = buffer.toString();
		return new Dimension(i.left + i.right + Math.max(minSize * m.stringWidth("W"), m.stringWidth(t)),
			i.top + i.bottom + Math.max(m.getHeight(), 17));
	}

	boolean recalc() {
		Dimension d = size();
		if ((d.width == 0) || (d.height == 0)) {
			return false;
		}

		Insets i = insets();
		FontMetrics m = getFontMetrics(getFont());
		if (m == null) {
			return false;
		}

		String s = buffer.toString();
		String sub = s.substring(0, cursorPos);
		int sl = m.stringWidth(sub);
		int rh = d.height - i.top - i.bottom;

		textSize.height = m.getHeight();
		textSize.width = m.stringWidth(s);
		textLocation.y = (i.top + ((rh + textSize.height) / 2)) - m.getDescent();

		cursorLocation.x = sl + i.left;
		cursorLocation.y = (textLocation.y - textSize.height) + m.getDescent();
		if (cursorLocation.y < i.top) {
			cursorLocation.y = i.top;
		}

		cursorSize.width = 1;
		cursorSize.height = textSize.height;

		if ((cursorLocation.y + cursorSize.height) >= (d.height - i.bottom)) {
			cursorSize.height = d.height - cursorLocation.y - i.bottom;
		}

		textLocation.x = i.left;
		cursorLocation.x = sl + i.left;

		if ((cursorLocation.x + shift.x) < i.left) {
			shift.x = i.left - cursorLocation.x;
		}
		else {
			int w = d.width - i.right;
			if ((cursorLocation.x + shift.x) > w) {
				shift.x = w - cursorLocation.x;
			}
		}

		return true;
	}

	@Override
	public void resize(int w, int h) {
		shift.x = 0;
		super.resize(w, h);
	}

	private void otdaiFocusTvojuMat() {
		cursorOwner = null;
		repaint();
	}

	@Override
	public void focusGained(FocusEvent e) {
		if (cursorOwner != null) {
			cursorOwner.otdaiFocusTvojuMat();
		}
		cursorOwner = this;
		if (buffer != null) {
			setPos(buffer.length());
			select(0, buffer.length());
		}
		repaint();
	}

	@Override
	public void focusLost(FocusEvent e) {
		if (cursorOwner == this) {
			cursorOwner = null;
			clear();
			repaint();
		}
	}

	void repaintPart() {
		Insets i = insets();
		Dimension d = size();
		repaint(i.left, i.top, (d.width - i.right - i.left) + 1, (d.height - i.bottom - i.top) + 1);
	}

	@Override
	public Dimension preferredSize() {
		return calcSize();
	}

	void select(int pos, int w) {
		if ((selPos == pos) && (w == selWidth)) {
			return;
		}
		selPos = pos;
		selWidth = w;
		repaintPart();
	}

	boolean isSelected() {
		int len = buffer.length();
		return (selPos >= 0) && (selPos < len) && ((selPos + selWidth) <= len) && (selWidth != 0);
	}

	void clear() {
		selWidth = 0;
		repaintPart();
	}

	@Override
	public boolean mouseDrag(Event e, int x, int y) {
		int pos = calcTextPos(x, y);
		if (pos >= 0) {
			if (pos < cursorPos) {
				select(pos, cursorPos - pos);
			}
			else {
				select(cursorPos, pos - cursorPos);
			}
		}
		return super.mouseDrag(e, x, y);
	}

	/**
	 * Contributed by <a href="mailto:morten@bilpriser.dk">Morten Raahede Knudsen</a>.
	 */
	private static synchronized void writeToClipboard(String s) {
		Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection s2 = new StringSelection(s);
		c.setContents(s2, s2);
	}

	private static synchronized String readFromClipboard() {
		Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable t = c.getContents("e");

		if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				return (String) t.getTransferData(DataFlavor.stringFlavor);
			}
			catch (Exception ex) {
			}
		}
		return "";
	}
}
