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

package org.zaval.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class InputIniFile {
	private final Map<String, String> hash = new HashMap<>();
	private String postponed;

	public Map<String, String> getItems() {
		return hash;
	}

	public InputIniFile(String path) throws IOException {
		load(path);
	}

	private String readLine(BufferedReader in) throws IOException {
		String x = postponed == null ? in.readLine() : postponed;
		postponed = null;
		if (x == null) {
			return null;
		}
		x = x.trim();

		while (x.endsWith("\\")) {
			String v = in.readLine();
			if (v == null) {
				break;
			}
			if (!v.startsWith(" ")) {
				postponed = v;
				break;
			}
			int i = v.indexOf('#');
			if (i > 0) {
				if (v.endsWith("\\")) {
					v = v.substring(0, i) + " \\";
				}
				else {
					v = v.substring(0, i);
				}
			}
			x = x.substring(0, x.length() - 1) + v.trim();
		}

		int j = x.indexOf('#');
		if (j == 0) {
			return "";
		}
		if (j > 0) {
			x = x.substring(0, j);
		}
		return x;
	}

	private void load(String path) throws IOException {
		try (BufferedReader in = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = readLine(in)) != null) {
				StringTokenizer st = new StringTokenizer(line, "=", true); // key = value
				if (st.countTokens() < 3) {
					continue; // syntax error, ignored
				}
				String key = st.nextToken().trim();
				st.nextToken(); // '='
				String value = st.nextToken("").trim();
				hash.put(key, value);
			}
		}
	}
}
