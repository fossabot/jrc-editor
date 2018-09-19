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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.util.Arrays;

public class StatusBar extends Panel implements LayoutManager {
	private static final int FULL = 1;

	private final Insets insets = new Insets(2, 1, 0, -1);
	private static final int hgap = 2;

	public StatusBar() {
		setLayout(this);
	}

	private int getFill() {
		return 0;
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
	}

	@Override
	public void layoutContainer(Container parent) {
		parent.size();
		Component[] cc = parent.getComponents();
		int height = getActualHeight(parent);
		int[] widths = getActualWidths(parent);

		int x = insets.left;
		int y = insets.top;
		for (int j = 0; j < cc.length; ++j) {
			cc[j].move(x, y);
			cc[j].resize(widths[j], height);
			x += (widths[j] + hgap);
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int[] widths = getActualWidths(parent);
		int height = getActualHeight(parent) + insets.top + insets.bottom;
		int width = insets.left + insets.right + (hgap * (widths.length - 1));
		width += Arrays.stream(widths).sum();
		return new Dimension(width, height);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
	}

	private int getActualHeight(Container parent) {
		Component[] cc = parent.getComponents();
		int ah = 0;
		for (Component element : cc) {
			Dimension d = element.preferredSize();
			if (ah < d.height) {
				ah = d.height;
			}
		}
		return ah;
	}

	private int[] getActualWidths(Container parent) {
		Dimension ds = parent.size();
		Component[] cc = parent.getComponents();
		int[] widths = new int[cc.length];
		int xx = insets.left + (hgap * (cc.length - 1));

		ds.width -= (insets.left + insets.right + (hgap * (cc.length - 1)));
		ds.height -= (insets.top + insets.bottom);

		int j;
		int aw = 0;
		for (j = 0; j < cc.length; ++j) {
			Dimension d = cc[j].preferredSize();

			if (!(cc[j] instanceof StatusBarElement)) {
				widths[j] = d.width;
				aw += d.width;
				continue;
			}

			if (cc[j] instanceof StatusBarElement) {
				StatusBarElement e = (StatusBarElement) cc[j];
				if (e.getPercent() == 0) {
					aw += d.width;
					widths[j] = d.width;
				}
			}
		}

		ds.width -= aw;
		for (j = 0; j < cc.length; ++j) {
			xx += widths[j];
			if (cc[j] instanceof StatusBarElement) {
				StatusBarElement e = (StatusBarElement) cc[j];
				int perc = e.getPercent();
				if (perc == 0) {
					continue;
				}
				widths[j] = (ds.width * perc) / 100;
			}
		}

		if (getFill() == FULL) {
			xx -= widths[cc.length - 1];
			widths[cc.length - 1] = ds.width - xx - insets.right;
		}
		return widths;
	}
}
