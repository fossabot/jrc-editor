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
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Scrollbar;

import org.zaval.awt.peer.TreeNode;

class SymTree extends Panel implements ScrollArea, ScrollObject {

	private TreeNode selectedNode; // highlighted node

	private final Scrollbar sbV; // vertical scrollbar
	private final Scrollbar sbH; // vertical scrollbar

	private Color bgHighlightColor = Color.gray; // selection bg color
	private final Color fgHighlightColor = Color.white; // selection fg color
	private int viewHeight = 300;
	private int viewWidth = 300; // pixel size of tree display

	private static final int cellSize = 16; // size of node image
	private static final int clickSize = 8; // size of mouse toggle (plus or minus)
	private static final int textInset = 6; // left margin for text
	private FontMetrics fm; // current font metrics
	private final ScrollController sm;

	private Image im1; // offscreen image
	private Graphics g1; // offscreen graphics context
	boolean noChoice;

	private int posx;
	private int posy;
	private final Dimension scrollInsets = new Dimension(10, 0);
	private final LevelTree ltree;
	private final ScrollLayout sl = new ScrollLayout();

	private static final int DELETE = 127;
	private static final int INSERT = 1025;

	SymTree() {
		super.setLayout(sl);

		sbV = new Scrollbar(Scrollbar.VERTICAL);
		add("East", sbV);
		sbH = new Scrollbar(Scrollbar.HORIZONTAL);
		add("South", sbH);
		add("Stubb", new StubbComponent());

		sbV.hide();
		sbH.hide();
		sbV.setBackground(Color.lightGray);
		sbH.setBackground(Color.lightGray);
		sm = new ScrollController(this, this);
		ltree = new LevelTree();
	}

	@Override
	public void setBackground(Color c) {
		super.setBackground(c);
		invalidate();
	}

	@Override
	public void setForeground(Color c) {
		super.setForeground(c);
		invalidate();
	}

	// Insert a new node relative to a node in the tree.
	// position = CHILD inserts the new node as a child of the node
	// position = NEXT inserts the new node as the next sibling
	// position = PREVIOUS inserts the new node as the previous sibling
	public void insert(TreeNode newNode, TreeNode relativeNode, int position) {
		ltree.insert(newNode, relativeNode, position);
	}

	public TreeNode getRootNode() {
		return ltree.getRootNode();
	}

	private void resetVector() {
		ltree.resetVector();
	}

// This functions will be added on caf

	public TreeNode getNode(String name) {
		return ltree.getNode(name);
	}

	public void insertRoot(String addname) {
		ltree.insertRoot(addname);
		validate2();
	}

	public boolean selectNode(String name) {
		if (getNode(name) == null) {
			return false;
		}
		TreeNode f = getNode(name);
		if (isHidden(name)) {
			return false;
		}
		return selectNode(f);
	}

	public boolean selectNode(TreeNode tn) {
		selectedNode = tn;
		int viewCount = getViewCount();
		int index = getIndex(selectedNode);
		if (index == -1) {
			index = ltree.e.indexOf(selectedNode);
		}
		if (index > (viewCount - 1)) {
			index = viewCount - 1;
		}
		checkSelection(index);
		return true;
	}

	public void selectNodeAndOpen(String name) {
		if (!selectNode(name)) {
			return;
		}
		TreeNode x = selectedNode.parent;
		while (x != null) {
			x.expand();
			x = x.parent;
		}
		validate2();
	}

	// end add

	public void remove(String s) {
		remove(getNode(s));
	}

	public void remove(TreeNode node) {
		int viewCount = getViewCount();
		if (node == selectedNode) {
			int index = getIndex(selectedNode);

			if (index == -1) {
				index = ltree.e.indexOf(selectedNode);
			}

			if (index > (viewCount - 1)) {
				index = viewCount - 1;
			}

			if (index > 0) {
				changeSelection(ltree.v.get(index - 1), index - 1);
			}
			else if (viewCount > 0) {
				try {
					changeSelection(ltree.v.get(1), 1);
				}
				catch (Exception e) {
					changeSelection(ltree.v.get(0), 0);
				}
			}
		}
		ltree.remove(node);
	}

	private boolean checkScrolls() {
		if (!isVisible()) {
			return false;
		}

		int viewCount = getViewCount();
		Dimension d = getSASize();
		boolean b = false;

		int hh = sm.getMaxHorScroll();
		int hv = sm.getMaxVerScroll();

		int w = d.width - (hv > 0 ? ScrollController.SCROLL_SIZE : 0);
		int h = d.height - (hh > 0 ? ScrollController.SCROLL_SIZE : 0);

		if (hv > 0) {
			int hl = (viewCount - (h / cellSize)) + (((h % cellSize) > 0) ? 1 : 0);
			hl += scrollInsets.height;
			if (hl <= 1) {
				hl = 2;
			}
			int v = sbV.getValue();
			sbV.setValues(v, 0, 0, hl);
			if (!sbV.isVisible()) {
				b = true;
			}
			sbV.show();
		}
		else {
			if (sbV.isVisible()) {
				sbV.hide();
				sbV.setValue(0);
				posy = 0;
				b = true;
			}
		}

		if (sm.getMaxHorScroll() > 0) {
			int h1 = (getMaxWidth() - w) + scrollInsets.width;
			int v = sbH.getValue();

			if (h1 <= 1) {
				h1 = 2;
			}
			sbH.setValues(v, 0, 0, h1);
			if (!sbH.isVisible()) {
				b = true;
			}
			sbH.show();
		}
		else {
			if (sbH.isVisible()) {
				sbH.setValue(0);
				sbH.hide();
				posx = 0;
				b = true;
			}
		}
		return b;
	}

	@Override
	public void reshape(int x, int y, int w, int h) {
		super.reshape(x, y, w, h);
		validate2();
	}

	@Override
	public boolean handleEvent(Event event) {
		if (scroll(event)) {
			return true;
		}

		if ((event.key > 0) && (selectedNode != null)) {
			if ((event.key == 45) && (selectedNode.isExpanded())) {
				toggleEvent(selectedNode, 0);
				return true;
			}

			if ((event.key == 43) && isExpandable(selectedNode) && (!selectedNode.isExpanded())) {
				toggleEvent(selectedNode, 1);
				return true;
			}
		}
		return (super.handleEvent(event));
	}

	private long time;

	@Override
	public boolean mouseUp(Event event, int x, int y) {
		if (time == 0) {
			time = System.currentTimeMillis();
		}
		long tt = System.currentTimeMillis();
		long dt = tt - time;
		time = tt;
		if ((event.clickCount == 0) && (dt < 300)) {
			event.clickCount = 2;
		}

		if (event.modifiers == 4) {
			return super.mouseUp(event, x, y);
		}
		requestFocus();

		boolean[] flags = new boolean[1];
		changeSelection(event, x, y, true, flags);
		if (!flags[0]) {
			time -= 400;
		}

		if (flags[0] && (event.clickCount == 2)) {
			Event e = new Event(event.target, 9999, event.arg);
			getParent().postEvent(e);
		}
		return super.mouseUp(event, x, y);
	}

	@Override
	public boolean keyDown(Event event, int key) {
		requestFocus();
		if (selectedNode == null) {
			return super.keyDown(event, key);
		}
		int index = getIndex(selectedNode);
		int viewCount = getViewCount();
		TreeNode f;
		switch (key) {
			case 10:
				sendActionEvent(event);
				break;
			case Event.UP:
				if (index > 0) {
					index--;
					changeSelection(ltree.v.get(index), index);
					sendActionEvent(event);
				}
				break;
			case Event.DOWN:
				if (index < (viewCount - 1)) {
					index++;
					changeSelection(ltree.v.get(index), index);
					sendActionEvent(event);
				}
				break;
			case Event.RIGHT: {
				if (!selectedNode.isExpanded()) {
					toggleEvent(selectedNode, 1);
				}
				else if (selectedNode.child != null) {
					f = selectedNode.child;
					while ((f != null) && f.hidden) {
						f = f.sibling;
					}
					if (f != null) {
						changeSelection(f, index);
						sendActionEvent(event);
					}
				}
			}
				break;
			case Event.LEFT: {
				if (selectedNode.isExpanded()) {
					toggleEvent(selectedNode, 0);
				}
				else if ((selectedNode.parent != null) && (selectedNode.parent != getRootNode()) && !selectedNode.parent.hidden) {
					changeSelection(selectedNode.parent, index);
					sendActionEvent(event);
				}
			}
				break;
			case Event.PGUP: {
				scrollPages(-1);
				sendActionEvent(event);
			}
				break;
			case Event.PGDN: {
				scrollPages(1);
				sendActionEvent(event);
			}
				break;
			case Event.HOME: {
				f = ltree.v.get(0);
				changeSelection(f, 0);
				sendActionEvent(event);
			}
				break;
			case Event.END: {
				f = ltree.v.get(ltree.v.size() - 1);
				changeSelection(f, ltree.v.size() - 1);
				sendActionEvent(event);
			}
				break;
			case INSERT: {
				Event e = new Event(event.target, 9999, event.arg);
				getParent().postEvent(e);
			}
				break;
			case DELETE: {
				Event e = new Event(event.target, 9991, event.arg);
				getParent().postEvent(e);
			}
				break;
		}
		return super.keyDown(event, key);
	}

	private void sendActionEvent(Event event) {
		int id = event.id;
		Object arg = event.arg;
		event.id = Event.ACTION_EVENT;
		event.arg = selectedNode.getText();
		postEvent(event);
		event.id = id;
		event.arg = arg;
		repaint();
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public String getSelectedText() {
		if (selectedNode == null) {
			return null;
		}
		return selectedNode.getText();
	}

	void changeSelection(Event evt, int x, int y, boolean isToggle, boolean... flags) {
		requestFocus();

		Dimension d = size();
		flags[0] = false;

		int viewCount = d.height / cellSize;
		int index = ((Math.abs(posy) + y) / cellSize);
		noChoice = false;

		int index2 = y / cellSize;
		if (index2 > (viewCount - 1)) {
			noChoice = true;
			return; //clicked below the last node
		}

		for (int i = 0; (i <= index) && (i < ltree.v.size()); ++i) {
			TreeNode tmpNode = ltree.v.get(i);
			if (tmpNode.getHide()) {
				++index;
			}
		}

		if (index >= ltree.v.size()) {
			return;
		}

		TreeNode oldNode = selectedNode;
		TreeNode newNode = ltree.v.get(index);
		int newDepth = newNode.getDepth();

		// check click in place plus/minus

		if (isExpandable(newNode)) {
			Rectangle rec = new Rectangle(posx + (cellSize * (newDepth - 1)) + (cellSize / 4),
				posy + (getIndex(newNode) * cellSize) + (clickSize / 2), clickSize, clickSize);

			if ((rec.inside(x, y)) && (isToggle)) {
				toggleEvent(newNode, newNode.isExpanded() ? 1 : 0);
				return;
			}
		}

		// check max right position

		String text = (newNode.caption == null) ? newNode.text : newNode.caption;
		int x1 = posx + ((newDepth - 1) * (cellSize)) + cellSize + textInset;
		int x2 = x1 + fm.stringWidth(text) + 4;
		FontMetrics fm = g1.getFontMetrics();
		if (newNode.getImage() != null) {
			x2 = x2 + fm.getHeight();
		}

		if (newNode.getIndicator() != null) {
			x2 = x2 + fm.getHeight();
		}

		if ((x > x2) || (x < x1)) {
			noChoice = true;
			return;
		}

		if (newNode == oldNode) {
			flags[0] = true;
		}
		changeSelection(newNode, index);

		// check for toggle box click
		// TODO: make it a bit bigger
		Rectangle toggleBox = new Rectangle(posx + (cellSize * newDepth) + (cellSize / 4), posy + (index * cellSize) + (clickSize / 2),
			clickSize, clickSize);

		if ((evt.modifiers != 4) && (isToggle) && (newNode == oldNode)) {
			toggleEvent(newNode, newNode.isExpanded() ? 0 : 1);
			return;
		}

		if (newNode.getImage() != null) {
			toggleBox.x -= fm.getHeight();
		}
		if (newNode.getIndicator() != null) {
			toggleBox.x -= fm.getHeight();
		}

		if (!toggleBox.inside(x, y)) {
			sendActionEvent(evt);
		}

	}

	private void validate2() {
		resetVector();
		if (checkScrolls()) {
			invalidate();
		}
		validate();
		repaint();
	}

	private int getIndex(TreeNode node) {
		return ltree.v.indexOf(node);
	}

	private void changeSelection(TreeNode node, int index) {
		if (selectedNode == null) {
			if (node != null) {
				selectedNode = node;
			}
			else {
				return;
			}
		}

		TreeNode oldNode = selectedNode;
		selectedNode = node;

		int y = index * cellSize;
		drawNodeText(oldNode, y, true);
		drawNodeText(node, y, true);

		checkSelection(index);
	}

	private void checkSelection(int index) {
		if (!sbV.isVisible() || (index < 0)) {
			return;
		}
		int y = index * cellSize;

		if ((posy != 0) && (y < Math.abs(posy))) {
			int maxIndex = Math.abs(posy) / cellSize;
			int decIndex = maxIndex - index;
			vscroll(-decIndex);
			return;
		}

		y += (posy + cellSize);
		int k = viewHeight;
		if (sbH.isVisible()) {
			k -= ScrollController.SCROLL_SIZE;
		}
		if (y > k) {
			int dy = (y - k);
			int incIndex = dy / cellSize;
			if ((dy % cellSize) > 0) {
				incIndex++;
			}
			vscroll(incIndex);
		}
	}

	@Override
	public void paint(Graphics g) {
		redraw();
		if (im1 != null) {
			g.drawImage(im1, 0, 0, this);
		}

		g.setColor(Color.gray);
		Dimension d = size();
		g.drawRect(0, 0, d.width - 1, d.height - 1);
	}

	private void redraw() {
		resetVector();
		drawTree();
	}

	private void drawTree() {
		Dimension d = size();
		if ((d.width != viewWidth) || (d.height != viewHeight) || (g1 == null)) {
			if ((d.width * d.height) <= 0) {
				return;
			}

			im1 = createImage(d.width, d.height);
			if (g1 != null) {
				g1.dispose();
				g1 = null;
			}
			g1 = im1.getGraphics();
			viewWidth = d.width;
			viewHeight = d.height;
		}

		Font f = getFont(); // unix version might not provide a default font

		//Make certain there is a font
		if (f == null) {
			f = new Font("TimesRoman", Font.PLAIN, 14);
			g1.setFont(f);
			setFont(f);
		}

		//Make certain the graphics object has a font (Mac doesn't seem to)
		if (g1.getFont() == null) {
			g1.setFont(f);
		}

		sbV.isVisible();
		sbH.isVisible();

		fm = g1.getFontMetrics();
		g1.setColor(getBackground());
		g1.fillRect(0, 0, viewWidth, viewHeight); // clear image

		int lastOne = ltree.v.size();
		getViewCount();
		int skipCount = 0;
		for (int i = 0; i < lastOne; ++i) {
			TreeNode node = null;
			// This block is better than synchronization for every call to LevelTree
			try {
				node = ltree.v.get(i);
			}
			catch (Exception e) {
			}

			if (node == null) {
				g1.dispose();
				g1 = null;
				break;
			}

			int x = posx + (cellSize * (node.depth - 1));
			int y = posy + ((i - skipCount) * cellSize);

			// draw lines
			g1.setColor(getForeground());

			// draw vertical sibling line
			TreeNode sb = getSibling(node);
			if (sb != null) {
				int k = getIndex(sb) - getIndex(node);
				drawDotLine(x + (cellSize / 2), y + (cellSize / 2), x + (cellSize / 2), y + (cellSize / 2) + (k * cellSize));
			}

			// draw vertical child lines
			if (node.isExpanded()) {
				int xx = x + cellSize + (cellSize / 2);
				drawDotLine(xx, (y + cellSize) - 2, xx, y + cellSize + (cellSize / 2));
			}

			// draw node horizontal line
			g1.setColor(getForeground());
			int xxx = x + (cellSize / 2);
			drawDotLine(xxx, y + (cellSize / 2), xxx + (cellSize / 2) + 10, y + (cellSize / 2));

			// draw toggle box
			if (isExpandable(node)) {

				g1.setColor(getBackground());
				int xx = x + (clickSize / 2);
				g1.fillRect(xx, y + (clickSize / 2), clickSize, clickSize);
				g1.setColor(getForeground());
				g1.drawRect(xx, y + (clickSize / 2), clickSize, clickSize);
				// cross hair
				g1.drawLine(xx + 2, y + (cellSize / 2), (xx + clickSize) - 2, y + (cellSize / 2));

				if (!(isExpanded(node))) {
					g1.drawLine(xx + (clickSize / 2), y + (clickSize / 2) + 2, xx + (clickSize / 2), (y + (clickSize / 2) + clickSize) - 2);
				}
			}

			// draw node image
			Image nodeImage = isExpanded(node) ? node.getExpandedImage() : node.getCollapsedImage();
			if (nodeImage != null) {
				g1.drawImage(nodeImage, x + cellSize, y, this);
			}

			// draw node indicator
			Image nodeInd = node.getIndicator();
			if (nodeInd != null) {
				int dx = ((nodeImage == null) ? cellSize : 2 * cellSize) + 2;
				int dy = ((cellSize - nodeInd.getHeight(this)) / 2) + 1;

				g1.drawImage(nodeInd, x + dx, y + dy, this);
			}

			// draw node text
			if (node.text != null) {
				drawNodeText(node, y, node == selectedNode);
			}
		}
	}

	private TreeNode getSibling(TreeNode node) {
		TreeNode tn = node.sibling;
		while ((tn != null) && tn.hidden) {
			tn = tn.sibling;
		}
		return tn;
	}

	private int getMaxWidth() {
		int max = 0;
		for (int i = 0; i < ltree.v.size(); i++) {
			TreeNode node = ltree.v.get(i);
			if (node.getHide()) {
				continue;
			}
			String text = node.caption == null ? node.text : node.caption;
			int depth = node.depth;

			int stringWidth = ((((depth - 1) * cellSize) + cellSize + textInset) - 1) + ((fm == null) ? 0 : fm.stringWidth(text));
			if ((node.getImage() != null) && (fm != null)) {
				stringWidth += fm.getHeight();
			}
			if ((node.getIndicator() != null) && (fm != null)) {
				stringWidth += fm.getHeight();
			}
			if (stringWidth > max) {
				max = stringWidth;
			}
		}
		return max;
	}

	private void drawNodeText(TreeNode node, int yPosition, boolean eraseBackground) {
		String text = node.caption == null ? node.text : node.caption;
		Color fg;
		Color bg;
		int textOffset = getTextPos(node);

		if (node == selectedNode) {
			fg = fgHighlightColor;
			bg = bgHighlightColor;
		}
		else {
			fg = getForeground();
			bg = getBackground();
		}

		fm.stringWidth(text);
		if (eraseBackground) {
			g1.setColor(bg);
			g1.fillRect(textOffset - 1, yPosition + 1, fm.stringWidth(text) + 4, cellSize - 1);
		}
		g1.setColor(fg);

		// position of font baseline from bottom of cell
		int textBaseLine = 3;
		g1.drawString(text, textOffset, (yPosition + cellSize) - textBaseLine);
	}

	private void drawDotLine(int x0, int y0, int x1, int y1) {
		if (y0 == y1) {
			for (int i = x0; i < x1; i += 2) {
				g1.drawLine(i, y0, i, y1);
			}
		}
		else {
			for (int i = y0; i < y1; i += 2) {
				g1.drawLine(x0, i, x1, i);
			}
		}
	}

	@Override
	public Dimension preferredSize() {
		FontMetrics fm = getFontMetrics(getFont());
		return new Dimension(175, Math.min(ltree.v.size() * fm.getHeight(), 500));
	}

	@Override
	public Dimension minimumSize() {
		return preferredSize();
	}

	@Override
	public void setLayout(LayoutManager lm) {
	}

	public void setResolver(ImageResolver imgres) {
		ltree.setResolver(imgres);
	}

	private void correctSelect(TreeNode n) {
		resetVector();
		if ((selectedNode != null) && (!ltree.v.contains(selectedNode))) {
			changeSelection(n, ltree.v.indexOf(n));
			Event event = new Event(this, 0, null);
			sendActionEvent(event);
		}
	}

	private boolean isExpandable(TreeNode node) {
		if (!node.isExpandable()) {
			return false;
		}
		if (node.child == null) {
			return false;
		}
		node = node.child;

		if (!node.getHide()) {
			return true;
		}
		while (node.sibling != null) {
			node = node.sibling;
			if (!node.getHide()) {
				return true;
			}
		}
		return false;
	}

	public void openToNode(String name) {
		TreeNode t = getNode(name);
		for (; t != null; t = t.parent) {
			if (t.isExpandable() && !t.isExpanded()) {
				t.toggle();
			}
		}
		validate2();
	}

	public void openNode(String name) {
		TreeNode t = getNode(name);
		if ((t == null) || t.isExpanded()) {
			return;
		}
		if (t.isExpandable() && !t.isExpanded()) {
			t.toggle();
		}
		validate2();
	}

	public void closeNode(String name) {
		TreeNode t = getNode(name);
		if (t == null) {
			return;
		}
		if (t.isExpandable() && t.isExpanded()) {
			t.toggle();
			correctSelect(t);
		}
		validate2();
	}

	private boolean isHidden(String name) {
		TreeNode t = getNode(name);
		if (t == null) {
			return false;
		}
		while (t.parent != null) {
			if (t.getHide()) {
				return true;
			}
			else {
				t = t.parent;
			}
		}
		return false;
	}

	public void expandAll() {
		ltree.expandAll();
		validate2();
	}

	public void collapseAll() {
		ltree.collapseAll();
		validate2();
	}

	public TreeNode[] enumChild(TreeNode tn) {
		return ltree.enumChild(tn);
	}

	private boolean isExpanded(TreeNode node) {
		if (!node.isExpanded()) {
			return false;
		}
		TreeNode c = node.child;
		while (c != null) {
			if (!c.getHide()) {
				return true;
			}
			c = c.sibling;
		}
		return false;
	}

	private int getViewCount() {
		return ltree.getViewCount();
	}

	@Override
	public Dimension getSASize() {
		return sl.getAreaSize(this);
	}

	@Override
	public Scrollbar getVBar() {
		return sbV;
	}

	@Override
	public Scrollbar getHBar() {
		return sbH;
	}

	@Override
	public Dimension getSOSize() {
		int v = getViewCount();
		Dimension r = new Dimension(getMaxWidth(), 0);
		r.height = v * cellSize;
		return r;
	}

	@Override
	public boolean gotFocus(Event e, Object o) {
		bgHighlightColor = Color.blue;
		repaint();
		return true;
	}

	public void setIndicator(String name, String image) {
		TreeNode t = getNode(name);
		if (t == null) {
			return;
		}
		t.setIndicator(image);
	}

	@Override
	public boolean lostFocus(Event e, Object o) {
		bgHighlightColor = Color.gray;
		repaint();
		return super.lostFocus(e, o);
	}

	private void toggleEvent(TreeNode n, int i) {
		if (!isExpandable(n)) {
			return;
		}
		getParent().postEvent(new Event(this, 0, 8888, 0, 0, i, 0, n.getText()));
		n.toggle();
		correctSelect(n);
		validate2();
	}

	private void scrollPages(int pages) {
		int index = getIndex(selectedNode);
		int height = viewHeight - ((sbH.isVisible()) ? ScrollController.SCROLL_SIZE : 0);
		int lines = height / cellSize;
		if (pages < 0) {
			lines = -lines;
		}

		int pg = lines + index;
		int max = ltree.v.size();
		if (pg >= max) {
			pg = max - 1;
		}
		if (pg < 0) {
			pg = 0;
		}
		changeSelection(ltree.v.get(pg), pg);
		repaint();
		vscroll(lines);
	}

	private void vscroll(int lines) {
		sbV.setValue(sbV.getValue() + lines);
		int id = (lines < 0) ? Event.SCROLL_LINE_DOWN : Event.SCROLL_LINE_UP;
		Event e = new Event(sbV, id, null);
		scroll(e);
	}

	private boolean scroll(Event e) {
		boolean b = false;
		if (e.target == sbV) {
			if (sbV.isVisible()) {
				posy = (-sbV.getValue() * cellSize);
				b = true;
			}
		}

		if (e.target == sbH) {
			if (sbH.isVisible()) {
				posx = -sbH.getValue();
				b = true;
			}
		}

		if (b) {
			validate2();
		}
		return b;
	}

	private int getTextPos(TreeNode node) {
		int depth = node.depth;
		int textOffset = ((depth - 1) * (cellSize)) + cellSize + textInset + posx;
		if (node.getImage() != null) {
			textOffset = textOffset + fm.getHeight();
		}
		if (node.getIndicator() != null) {
			textOffset += cellSize;
		}
		return textOffset;
	}
}
