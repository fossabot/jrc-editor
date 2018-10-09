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
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;

import org.zaval.awt.image.BoxButtonFilter;
import org.zaval.awt.image.ButtonImageFilter;

@SuppressWarnings("serial")
public class SpeedButton extends Canvas implements MouseListener {
	private static final int FREE = 1;
	private static final int UP = 2;
	private static final int DOWN = 3;

	private final Runnable callback;

	private Image normImg;
	private Image upImg;
	private Image disImg;
	private Image downImg;

	private int w;
	private int h;
	private int state = FREE;

	public SpeedButton(Runnable callback, Image src) {
		this.callback = callback;
		ButtonImageFilter filt = new BoxButtonFilter();
		w = src.getWidth(this);
		h = src.getHeight(this);
		int border = -5;
		border = -border;
		border = (w * border) / 100;
		if (border < 2) {
			border = 2;
		}
		int light = 30;
		init(src, getFilter(src, filt, light, border, false), getFilter(src, filt, -light, border, true),
			getFilter(src, filt, -Math.abs(light), 0, false));
		addMouseListener(this);
	}

	private void init(Image src, Image up, Image down, Image dis) {
		normImg = src;
		upImg = up;
		disImg = dis;
		downImg = down;
	}

	private Image getFilter(Image src, ButtonImageFilter filt, int light, int border, boolean b) {
		filt = filt.clone();
		filt.setup(light, border, w, h, b);

		ImageProducer prod = src.getSource();
		ImageProducer ip = new FilteredImageSource(prod, filt);
		return createImage(ip);
	}

	private void setState(int s) {
		state = s;
		repaint();
	}

	@Override
	public Dimension preferredSize() {
		if (w < 0) {
			w = 0;
		}
		if (h < 0) {
			h = 0;
		}
		return new Dimension(w, h);
	}

	@Override
	public void paint(Graphics gr) {
		Dimension d = getSize();
		int ww = d.width;
		int hh = d.height;

		if ((disImg != null) && !isEnabled()) {
			gr.drawImage(disImg, 0, 0, ww, hh, this);
		}
		else if ((state == FREE) && (normImg != null)) {
			gr.drawImage(normImg, 0, 0, ww, hh, this);
		}
		else if ((state == DOWN) && (downImg != null)) {
			gr.drawImage(downImg, 0, 0, ww, hh, this);
		}
		else if ((state == UP) && (upImg != null)) {
			gr.drawImage(upImg, 0, 0, ww, hh, this);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (isEnabled() && (state != FREE)) {
			setState(UP);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (isEnabled() && (state != FREE)) {
			callback.run();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (isEnabled()) {
			setState(DOWN);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		setState(UP);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		setState(FREE);
	}
}
