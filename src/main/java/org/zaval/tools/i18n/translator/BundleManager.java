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

package org.zaval.tools.i18n.translator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

class BundleManager {
	private final BundleSet set = new BundleSet();

	public BundleManager() {
	}

	BundleManager(String baseFileName) throws IOException {
		readResources(baseFileName);
	}

	void appendResource(InputStream stream, String lang) throws IOException {
		readResource(stream, lang);
	}

	BundleSet getBundle() {
		return set;
	}

	private String dirName(String fn) {
		fn = replace(fn, "\\", "/");
		int ind = fn.lastIndexOf('/');
		return ind >= 0 ? fn.substring(0, ind + 1) : "./";
	}

	String baseName(String fn) {
		fn = replace(fn, "\\", "/");
		int ind = fn.lastIndexOf('/');
		fn = ind >= 0 ? fn.substring(ind + 1) : fn;
		ind = fn.lastIndexOf('.');
		return ind >= 0 ? fn.substring(0, ind) : fn;
	}

	private String extName(String fn) {
		fn = replace(fn, "\\", "/");
		int ind = fn.lastIndexOf('.');
		return ind >= 0 ? fn.substring(ind) : "";
	}

	private String purifyFileName(String fn) {
		fn = baseName(fn);
		int ind = fn.lastIndexOf('_');
		int in2 = ind > 0 ? fn.lastIndexOf('_', ind - 1) : -1;
		if ((in2 < 0) && (ind > 0)) {
			in2 = ind;
		}
		return in2 >= 0 ? fn.substring(0, in2) : fn;
	}

	private List<String> getResFiles(String dir, String baseFileName, String defExt) {
		File f = new File(dir);
		String bpn = purifyFileName(baseFileName);
		String[] fs = f.list();
		if (fs.length == 0) {
			return null;
		}
		List<String> res = new ArrayList<>();
		for (String f1 : fs) {
			if (!f1.startsWith(bpn)) {
				continue;
			}
			String bfn = purifyFileName(f1);
			if (!bfn.equals(bpn)) {
				continue;
			}
			if (!extName(f1).equals(defExt)) {
				continue;
			}
			File f2 = new File(dir + f1);
			if (!f2.isDirectory()) {
				res.add(f1);
			}
		}
		return res;
	}

	String determineLanguage(String fn) {
		fn = baseName(fn);
		int ind = fn.lastIndexOf('_');
		int in2 = ind > 0 ? fn.lastIndexOf('_', ind - 1) : -1;
		if ((in2 < 0) && (ind > 0)) {
			in2 = ind;
		}
		return in2 >= 0 ? fn.substring(in2 + 1) : "en";
	}

	private void readResources(String baseFileName) throws IOException {
		String dir = dirName(baseFileName);
		String ext = extName(baseFileName);
		baseFileName = baseName(baseFileName);

		for (String fn : getResFiles(dir, baseFileName, ext)) {
			readResource(dir + fn, determineLanguage(fn));
		}
	}

	private void readResource(String fullName, String lang) throws IOException {
		List<String> lines = getLines(fullName);
		proceedLines(lines, lang, fullName);
	}

	private void readResource(InputStream in, String lang) throws IOException {
		List<String> lines = getLines(in);
		proceedLines(lines, lang, null);
	}

	private void proceedLines(List<String> lines, String lang, String fullName) {
		fullName = fullName != null ? fullName : "tmp_" + lang;
		set.addLanguage(lang);
		set.getLanguage(lang).setFileName(fullName);
		String lastComment = null;
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			if (line.startsWith("#")) {
				lastComment = line.substring(1);
				continue;
			}

			StringTokenizer st = new StringTokenizer(line, "=", true); // key = value
			if (st.countTokens() < 2) {
				continue; // syntax error, ignored
			}
			String dname = st.nextToken().trim();
			st.nextToken(); // '='
			String value = "";
			if (st.hasMoreTokens()) {
				value = st.nextToken("");
			}

			BundleItem bi = set.getItem(dname);
			if (bi == null) {
				bi = set.addKey(dname);
			}
			bi.setTranslation(lang, value);
			bi.setComment(lastComment);
			lastComment = null;
		}
	}

	private List<String> getLines(String fileName) throws IOException {
		List<String> res = new ArrayList<>();
		if (fileName.endsWith(TranslatorConstants.RES_EXTENSION)) {
			try (BufferedReader in = new BufferedReader(new FileReader(fileName))) {
				String line;
				while ((line = in.readLine()) != null) {
					for (;;) {
						line = line.trim();
						if (line.endsWith("\\")) {
							String line2 = in.readLine();
							if (line2 != null) {
								line = line.substring(0, line.length() - 1) + line2;
							}
							else {
								break;
							}
						}
						else {
							break;
						}
					}
					res.add(fromEscape(line));
				}
			}
		}
		else {
			try (RandomAccessFile in = new RandomAccessFile(fileName, "r")) {
				StringBuilder sb = new StringBuilder();
				int factor1 = 1;
				for (int factor2 = 256;;) {
					if ((in.length() - in.getFilePointer()) == 0) {
						break;
					}
					int i = (in.readUnsignedByte() * factor1) + (in.readUnsignedByte() * factor2);
					if (i == 0xFFFE) {
						factor1 = 256;
						factor2 = 1;
					}
					if ((i != 0x0D) && (i != 0xFFFE) && (i != 0xFEFF) && (i != 0xFFFF)) {
						if (i != 0x0A) {
							sb.append((char) i);
						}
						else {
							res.add(fromEscape(sb.toString()));
							sb.setLength(0);
						}
					}
				}
			}
		}
		return res;
	}

	private List<String> getLines(InputStream xin) throws IOException {
		List<String> res = new ArrayList<>();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(xin))) {
			String line;
			while ((line = in.readLine()) != null) {
				for (;;) {
					line = line.trim();
					if (line.endsWith("\\")) {
						String line2 = in.readLine();
						if (line2 != null) {
							line = line.substring(0, line.length() - 1) + line2;
						}
						else {
							break;
						}
					}
					else {
						break;
					}
				}
				res.add(fromEscape(line));
			}
		}
		return res;
	}

	private static String toEscape(String s) {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (ch == '\r') {
				continue;
			}
			if ((ch < 128) && (ch != '\n') && (ch != '\\')) {
				res.append(ch);
			}
			else {
				res.append("\\u");
				String hex = Integer.toHexString(ch);
				for (int j = 0; j < (4 - hex.length()); j++) {
					res.append("0");
				}
				res.append(hex);
			}
		}
		return res.toString();
	}

	private static String fromEscape(String s) {
		StringBuilder res = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if ((ch == '\\') && ((i + 1) >= s.length())) {
				res.append(ch);
				break;
			}
			if (ch != '\\') {
				res.append(ch);
			}
			else {
				switch (s.charAt(i + 1)) {
					case 'u':
						res.append((char) Integer.parseInt(s.substring(i + 2, i + 6), 16));
						i += 5;
						break;
					case 'n':
						res.append('\n');
						i++;
						break;
					case 't':
						res.append('\t');
						i++;
						break;
					case 'r':
						res.append('\r');
						i++;
						break;
					default:
						break;
				}
			}
		}
		return res.toString();
	}

	String replace(String line, String from, String to) {
		StringBuilder res = new StringBuilder(line.length());
		int ind = -1;
		int lastind = 0;

		while ((ind = line.indexOf(from, ind + 1)) != -1) {
			if (lastind < ind) {
				String tmpstr = line.substring(lastind, ind);
				res.append(tmpstr);
			}
			res.append(to);
			lastind = ind + from.length();
			ind += from.length() - 1;
		}
		if (lastind == 0) {
			return line;
		}
		res.append(line.substring(lastind));
		return res.toString();
	}

	void store(String fileName) throws IOException {
		for (LangItem lang : set.getLanguages()) {
			store(lang.getId(), fileName);
		}
	}

	private void store(String lng, String fn) throws IOException {
		LangItem lang = set.getLanguage(lng);
		if (fn == null) {
			fn = lang.getFileName();
		}
		else {
			String tmpFn = fn;
			tmpFn = dirName(tmpFn) + purifyFileName(tmpFn);
			LangItem firstLanguage = set.getFirstLanguage();
			if (firstLanguage != lang) {
				tmpFn += "_" + lang.getId();
			}
			tmpFn += TranslatorConstants.RES_EXTENSION;
			fn = tmpFn;
			lang.setFileName(fn);
		}

		if (fn == null) {
			store(lng, "autosaved.properties");
			return;
		}

		List<String> lines = set.store(lang.getId());
		if (fn.endsWith(TranslatorConstants.RES_EXTENSION)) {
			try (PrintStream f = new PrintStream(new FileOutputStream(fn))) {
				for (String line : lines) {
					f.print(toEscape(line) + System.getProperty("line.separator"));
				}
			}
		}
		else {
			try (FileOutputStream f = new FileOutputStream(fn)) {
				f.write(0xFF);
				f.write(0xFE);
				for (String s : lines) {
					s = replace(s, "\n", toEscape("\n"));
					for (int k = 0; k < s.length(); k++) {
						char ch = s.charAt(k);
						f.write((ch) & 255);
						f.write((ch) >> 8);
					}
					f.write(0x0D);
					f.write(0x00);
					f.write(0x0A);
					f.write(0x00);
				}
			}
		}
	}
}
