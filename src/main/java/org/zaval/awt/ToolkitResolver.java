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
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;

public class ToolkitResolver implements ImageResolver {
	private final Toolkit kit;

	public ToolkitResolver() {
		kit = Toolkit.getDefaultToolkit();
	}

	public Image getImage(String str, Component listener) {
		Image i = kit.getImage(str);
		MediaTracker track = new MediaTracker(listener);
		try {
			track.addImage(i, 0);
			track.waitForID(0);
		}
		catch (Exception e) {
			i = null;
		}
		return i;
	}

	@Override
	public Image getImage(String str) {
		return kit.getImage(str);
	}
}
