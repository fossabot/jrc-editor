/*
 * Copyright (C) 2001-2002  Zaval Creative Engineering Group (http://www.zaval.org)
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

package org.zaval.tools.i18n.translator;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.Locale;

import org.zaval.util.SafeResourceBundle;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main { // NO_UCD (unused code)
	public static void main(String arg[]) {
		String altDir = System.getProperty("my.root.dir");
		File f = new File(altDir != null ? altDir : ".");
		String path = f.getAbsolutePath();
		if (path.endsWith(".")) {
			path = path.substring(0, path.length() - 1);
		}
		path += "/images/";

		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		}
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
		}

		Translator t;
		if (arg.length > 0) {
			t = new Translator(path, new SafeResourceBundle("jrc-editor", Locale.getDefault()), arg[0]);
		}
		else {
			t = new Translator(path, new SafeResourceBundle("jrc-editor", Locale.getDefault()));
		}

		Dimension gdz = Toolkit.getDefaultToolkit().getScreenSize();
		int optimalX = (gdz.width / 4) * 3;
		int optimalY = (gdz.height / 4) * 3;
		t.move((gdz.width - optimalX) / 2, (gdz.height - optimalY) / 2);
		t.resize(optimalX, optimalY);
		t.show();
	}
}
