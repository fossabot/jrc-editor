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

package org.zaval.awt.dialog;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.zaval.awt.AlignConstants;
import org.zaval.awt.ResultField;
import org.zaval.awt.StaticImage;
import org.zaval.awt.TextAlignArea;

public class MessageBox2 extends Dialog implements LayoutManager {
	private StaticImage icon;
	private List<Button> buttons;
	private final ResultField text;
	private Color storedColor;

	private final List<Component> listeners = new ArrayList<>();

	private static final int INDENT = 10;
	private static final int ICON_TEXT_GAP = 5;
	private static final int w = 4;
	private static final int h = 3;
	private static final int maxlinecount = 15;

	public MessageBox2(Frame parent) {
		super(parent, false);
		setLayout(this);
		setTitle("Message");
		text = new ResultField();
		TextAlignArea area = text.getAlignArea();
		if (area != null) {
			area.setMultiLine(true);
			area.setAlign(AlignConstants.TLEFT);
			area.setInsets(new Insets(10, 0, 10, 0));
		}
		add(text);
	}

	@Override
	public void show() {
		pack();
		Dimension d1 = preferredSize();
		Dimension d2 = Toolkit.getDefaultToolkit().getScreenSize();
		resize(d1.width, d1.height);
		move((d2.width - d1.width) / 2, (d2.height - d1.height) / 2);
		setResizable(false);
		super.show();
	}

	@Override
	public void setBackground(Color c) {
		if (storedColor != null) {
			c = storedColor;
		}
		storedColor = c;
		if (text != null) {
			text.setBackground(c);
		}
		if (icon != null) {
			icon.setBackground(c);
		}
		for (int i = 0; (buttons != null) && (i < buttons.size()); i++) {
			buttons.get(i).setBackground(c);
		}
		super.setBackground(c);
	}

	@Override
	public void setForeground(Color c) {
		super.setForeground(c);
		if (text != null) {
			text.setForeground(c);
		}
		if (icon != null) {
			icon.setForeground(c);
		}
		for (Button button : buttons) {
			button.setForeground(c);
		}
	}

	private void setButtons(List<Button> b) {
		if (buttons != null) {
			for (Button button : buttons) {
				remove(button);
			}
		}
		buttons = b;
		for (Button button : buttons) {
			add(button);
		}
		invalidate();
		validate();
	}

	public void setButtons(String... sa) {
		List<Button> z = Arrays.stream(sa).map(Button::new).collect(Collectors.toCollection(() -> new ArrayList<>(sa.length)));
		setButtons(z);
	}

	public void setButtons(String one) {
		List<Button> z = new ArrayList<>();
		z.add(new Button(one));
		setButtons(z);
	}

	public void setIcon(Image icon1) {
		if (icon != null) {
			remove(icon);
		}
		if (icon1 == null) {
			return;
		}
		icon = new StaticImage(icon1);
		add(icon);
	}

	public void setText(String str) {
		text.setText(str);
	}

	public void addListener(Component ls) {
		listeners.add(ls);
	}

	@Override
	public boolean action(Event e, Object o) {
		if (e.target instanceof Button) {
			for (Component listener : listeners) {
				listener.postEvent(new Event(this, Event.ACTION_EVENT, e.target));
			}
			hide();
			return true;
		}
		return super.action(e, o);
	}

	private void setDefFontMetrics() {
		if ((text != null) && (text.getAlignArea().getFontMetrics() == null)) {
			Font font = getFont();
			if (font == null) {
				return;
			}
			FontMetrics fm = getFontMetrics(font);
			if (fm == null) {
				return;
			}
			text.getAlignArea().setFontMetrics(fm);
		}
	}

	private Dimension getIconSize() {
		if (icon == null) {
			return new Dimension(0, 0);
		}
		return icon.preferredSize();
	}

	private Dimension getTextSize(int maxwidth) {
		if (text == null) {
			return new Dimension(0, 0);
		}
		setDefFontMetrics();

		int prefwidth = 0;
		int prefheight = 0;
		TextAlignArea align = text.getAlignArea();
		if ((align != null) && (align.getFontMetrics() != null)) {
			FontMetrics fm = align.getFontMetrics();
			String[] str = TextAlignArea.breakString(align.getText(), fm, maxwidth);
			for (String element : str) {
				prefwidth = Math.max(fm.stringWidth(element), prefwidth);
			}
			prefheight = fm.getHeight() * Math.min(maxlinecount, str.length);
			Insets ins = align.getInsets();
			prefwidth += ins.left + ins.right;
			prefheight += ins.top + ins.bottom;
		}
		return new Dimension(prefwidth, prefheight);
	}

	private Button getButton(int z) {
		return buttons.get(z);
	}

	private Dimension getButtonsSize() {
		if ((buttons == null) || (buttons.isEmpty())) {
			return new Dimension(0, 0);
		}
		int maxH = 23; // minimum height and width
		int maxW = 48;
		for (Button button : buttons) {
			Dimension d = button.preferredSize();
			maxH = Math.max(d.height, maxH);
			maxW = Math.max(d.width, maxW);
		}
		return new Dimension(maxW, maxH);
	}

	private Rectangle getPosition(Rectangle area, Dimension size) {
		int w = Math.max(0, (area.width - size.width) / 2);
		int h = Math.max(0, (area.height - size.height) / 2);
		return new Rectangle(area.x + w, area.y + h, area.width - (w * 2), area.height - (h * 2));
	}

// LayoutManager implementation
	@Override
	public void addLayoutComponent(String s, Component component) {
	}

	@Override
	public void removeLayoutComponent(Component component) {
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		Insets ins = insets();
		Dimension d2 = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension d = getTextSize(d2.width / 2);
		int prefwidth = d.width;
		int prefheight = d.height;

		d = getIconSize();
		if (d.width != 0) {
			d.width += ICON_TEXT_GAP;
		}
		prefwidth += d.width;
		prefheight = Math.max(d.height, prefheight);

		d = getButtonsSize();
		prefwidth = Math.max(prefwidth, ((d.width + w) * buttons.size()) + w);
		prefheight += d.height + (h * 2) + INDENT;
		if (prefwidth < (d2.width / 4)) {
			prefwidth = d2.width / 4;
		}
		return new Dimension(prefwidth + ins.left + ins.right + (2 * INDENT), prefheight + ins.top + ins.bottom + (2 * INDENT));

	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	@Override
	public void layoutContainer(Container parent) {
		Dimension size = parent.size();
		Insets inss = insets();
		int insl = inss.left + INDENT;
		int inst = inss.top + INDENT;
		int width = size.width - (insl + inss.right + INDENT);
		int height = size.height - (inst + inss.bottom + INDENT);
		Dimension isize = getIconSize();
		Dimension tsize = getTextSize(width - isize.width - ICON_TEXT_GAP);
		Dimension bsize = getButtonsSize();
		height -= bsize.height + (h * 2);
		height = Math.max(height, Math.max(isize.height, tsize.height));
		if (icon != null) {
			Dimension d = icon.preferredSize();
			Rectangle r = getPosition(new Rectangle(insl, inst, isize.width, height), d);
			icon.move(r.x, r.y);
			icon.resize(r.width, r.height);
		}
		if (text != null) {
			int addIc = isize.width;
			if (isize.width > 0) {
				addIc += ICON_TEXT_GAP;
			}
			if ((width - tsize.width) > (3 * addIc)) {
				addIc = 0;
			}
			else {
				addIc -= (width - tsize.width - addIc) / 2;
			}
			Rectangle r = getPosition(new Rectangle(insl + addIc, inst, width - addIc, height), tsize);
			text.move(r.x, r.y);
			text.resize(r.width, r.height);
		}
		int y = height + inst + h;
		int x = ((width - ((bsize.width + w) * buttons.size()) - w) / 2) + w + insl;
		for (int i = 0; i < buttons.size(); i++) {
			getButton(i).move(x, y);
			getButton(i).resize(bsize);
			x += bsize.width + w;
		}
	}

	@Override
	public boolean handleEvent(Event e) {
		if (e.id == Event.WINDOW_DESTROY) {
			hide();
			return true;
		}
		return super.handleEvent(e);
	}

	@Override
	public boolean keyDown(Event e, int key) {
		if ((e.target instanceof Button) && (key == Event.ENTER)) {
			Component p = this;
			while ((p != null) && !(p instanceof Window)) {
				p = p.getParent();
			}
			if (p == null) {
				return false;
			}
			p.action(e, null);
			return true;
		}
		return false;
	}
}
