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

public class StaticImage extends Canvas {
	private Image image;
	private int width;
	private int height;

	public StaticImage(Image img) {
		width = img.getWidth(this);
		height = img.getHeight(this);
		setImage(img);
	}

	private void setImage(Image img, int width, int height) {
		this.image = img;
		this.width = width;
		this.height = height;
		repaint();
	}

	private void setImage(Image img) {
		setImage(img, width, height);
	}

	@Override
	public void paint(Graphics gr) {
		if (image == null) {
			return;
		}
		Dimension sz = size();
		gr.drawImage(image, 0, 0, sz.width, sz.height, this);
	}

	@Override
	public Dimension preferredSize() {
		return new Dimension(width, height);
	}

	@Override
	public Dimension minimumSize() {
		return preferredSize();
	}
}
