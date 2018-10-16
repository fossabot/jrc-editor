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

package org.zaval.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;

public class UiUtils {

	private UiUtils() {
		// prevent instantiation
	}

	public static void toCenter(Window w) {
		Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension d = w.getSize();
		w.setLocation((s.width - d.width) / 2, (s.height - d.height) / 2);
	}

	public static void constrain(Container c, Component p, int x, int y, int w, int h, int a, int f, double wx, double wy, int t, int l,
		int b, int r) {
		GridBagConstraints cc = new GridBagConstraints();

		cc.gridx = x;
		cc.gridy = y;
		cc.gridwidth = w;
		cc.gridheight = h;

		cc.anchor = a;
		cc.fill = f;
		cc.weightx = wx;
		cc.weighty = wy;

		if ((t + b + l + r) > 0) {
			cc.insets = new Insets(t, l, b, r);
		}
		c.add(p, cc);
	}
}
