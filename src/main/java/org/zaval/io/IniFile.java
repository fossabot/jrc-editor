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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class IniFile {
	private final List<String> keys = new ArrayList<>();
	private final List<Object> vals = new ArrayList<>();
	private boolean dirty;

	private final File file;

	public IniFile(String name) throws IOException {
		file = new File(name);
		if (file.canRead()) {
			loadFile();
		}
	}

	private void saveFile() throws IOException {
		if (dirty) {
			dirty = false;
			try (PrintStream pr = new PrintStream(new FileOutputStream(file))) {
				for (int j = 0; j < keys.size(); ++j) {
					pr.print(keys.get(j));
					pr.print("=");
					pr.println(vals.get(j));
				}
			}
		}
	}

	private void loadFile() throws IOException {
		try (BufferedReader in = new BufferedReader(new FileReader(file))) {

			// [ \t]* ({symbol}+ '=' [ \t]* {symbol}*

			String line;
			char ch = ' ';
			while ((line = in.readLine()) != null) {
				int k = line.length();
				if (k <= 0) {
					continue;
				}
				int j = 0;
				for (; j < k; ++j) {
					ch = line.charAt(j);
					if ((ch != '\t') && (ch != ' ')) {
						break;
					}
				}
				if ((ch == '#') || (ch == '\n') || (ch == '\r')) {
					continue;
				}
				int i;
				for (i = j; j < k; ++j) {
					ch = line.charAt(j);
					if ((ch == '\t') || (ch == ' ') || (ch == '\n') || (ch == '\r') || (ch == '=') || (ch == '#')) {
						break;
					}
				}
				if (j != i) {
					keys.add(line.substring(i, j));
				}
				for (; j < k; ++j) {
					ch = line.charAt(j);
					if ((ch == '=') || (ch == '\n') || (ch == '\r') || (ch == '#')) {
						break;
					}
				}
				if ((ch == '\n') || (ch == '\r') || (ch == '#')) {
					vals.add("");
					continue;
				}
				for (++j; j < k; ++j) {
					ch = line.charAt(j);
					if ((ch != ' ') && (ch != '\t')) {
						break;
					}
				}
				if ((ch == '\n') || (ch == '\r') || (ch == '#')) {
					vals.add("");
					continue;
				}
				for (i = j; j < k; ++j) {
					ch = line.charAt(j);
					if ((ch == '\n') || (ch == '\r') || (ch == '#')) {
						break;
					}
				}
				vals.add(j != i ? line.substring(i, j).trim() : "");
			}
		}
	}

	public synchronized void putString(String key, String value) throws IOException {
		int j = keys.indexOf(key);
		if (j < 0) {
			keys.add(key);
			vals.add(value);
		}
		else {
			vals.set(j, value);
		}
		dirty = true;
		saveFile();
	}

	public synchronized void close() throws IOException {
		saveFile();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[IniFile = ").append(file.toString()).append("]={");
		for (int j = 0; j < keys.size(); ++j) {
			sb.append("\n\t").append(keys.get(j)).append("=").append(vals.get(j));
		}
		sb.append("}");
		return sb.toString();
	}

}
