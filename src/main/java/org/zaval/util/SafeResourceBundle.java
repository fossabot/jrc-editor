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

package org.zaval.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class SafeResourceBundle {
	private ResourceBundle rb;

	private static final String FAILURE_STRING = "?????";

	public SafeResourceBundle(String resName, Locale loc) {
		try {
			if (loc == null) {
				Locale.setDefault(new Locale("en", "US"));
				rb = ResourceBundle.getBundle(resName);
				Locale saved = Locale.getDefault();
				Locale.setDefault(saved);
			}
			else {
				rb = ResourceBundle.getBundle(resName, loc);
			}
		}
		catch (Exception e) {
			System.err.println(resName + ": resource not found");
		}
	}

	public String getString(String k) {
		if (rb == null) {
			return FAILURE_STRING;
		}
		String res = null;
		try {
			res = rb.getString(k);
		}
		catch (Exception e) {
			System.err.println(k + ": resource not found");
		}
		if (res != null) {
			return res;
		}
		return FAILURE_STRING;
	}
}
