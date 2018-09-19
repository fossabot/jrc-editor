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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.StringTokenizer;

import org.zaval.tools.i18n.translator.generated.JavaParser;
import org.zaval.tools.i18n.translator.generated.UtfParser;

public class Split { // NO_UCD (unused code)
	private BundleManager bundle = new BundleManager();

	private Split(String srcName) {
		try {
			readResources(srcName, false);
		}
		catch (Exception e) {
		}
	}

	private void join(BundleManager bundle2, boolean part) {
		if (part) {
			BundleSet set = bundle2.getBundle();
			int items = set.getItemCount();
			for (int i = 0; i < items; ++i) {
				BundleItem bi = set.getItem(i);
				bundle.getBundle().addKey(bi.getId());
				for (String lang : bi.getLanguages()) {
					bundle.getBundle().addLanguage(lang);
					bundle.getBundle().updateValue(bi.getId(), lang, bi.getTranslation(lang));
				}
			}
		}
		else {
			bundle = bundle2;
		}
	}

	private void readResources(String fileName, boolean part) throws Exception {
		try {
			BundleManager bundle2 = new BundleManager(fileName);
			join(bundle2, part);
		}
		catch (Exception e) {
			infoException(fileName, e);
			throw e;
		}
	}

	private void onSaveAs(String fileName) {
		if (fileName != null) {
			try {
				bundle.store(fileName);
			}
			catch (Exception e) {
				infoException(fileName, e);
			}
		}
	}

	private void infoException(String fileName, Exception e) {
		System.err.println(fileName + ":" + e.getMessage());
	}

	private void onGenCode(String fileName) {
		try {
			if (fileName != null) {
				SrcGenerator srcgen = new SrcGenerator(bundle.replace(fileName, "\\", "/"));
				srcgen.perform(bundle.getBundle());
			}
		}
		catch (Exception e) {
			infoException(fileName, e);
		}
	}

	private void onParseCode(String fileName) throws Exception {
		if (fileName != null) {
			fileName = bundle.replace(fileName, "\\", "/");
			JavaParser parser = new JavaParser(new FileInputStream(fileName));
			Map<String, String> ask = parser.parse();

			bundle.getBundle().addLanguage("en");
			String rlng = bundle.getBundle().getLanguage(0).getLangId();

			for (Map.Entry<String, String> stringStringEntry : ask.entrySet()) {
				bundle.getBundle().addKey(stringStringEntry.getKey());
				bundle.getBundle().updateValue(stringStringEntry.getKey(), rlng, stringStringEntry.getValue());
			}
		}
	}

	private void onSaveXml(String fileName, String... parts) {
		if (fileName != null) {
			try {
				try (DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName))) {
					BundleSet set = bundle.getBundle();
					int items = set.getItemCount();
					out.writeChar((char) 0xFEFF);
					out.writeChars("<xml>\n");
					for (int i = 0; i < items; ++i) {
						BundleItem bi = set.getItem(i);
						out.writeChars("\t<key name=\"" + bi.getId() + "\">\n");
						for (String lang : bi.getLanguages()) {
							if (!inArray(parts, lang)) {
								continue;
							}
							out.writeChars("\t\t<value lang=\"" + lang + "\">" + bi.getTranslation(lang) + "</value>\n");
						}
						out.writeChars("\t</key>\n");
					}
					out.writeChars("</xml>\n");
				}
			}
			catch (Exception e) {
				infoException(fileName, e);
			}
		}
	}

	private void onSaveUtf(String fileName, String... parts) {
		if (fileName != null) {
			try {
				try (DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName))) {
					BundleSet set = bundle.getBundle();
					int items = set.getItemCount();
					out.writeChar((char) 0xFEFF);
					out.writeChars("#JRC Editor 2.0: do not modify this line\r\n\r\n");
					for (int i = 0; i < items; ++i) {
						BundleItem bi = set.getItem(i);
						out.writeChars("KEY=\"" + bi.getId() + "\":\r\n");
						for (String lang : bi.getLanguages()) {
							if (!inArray(parts, lang)) {
								continue;
							}
							out.writeChars("\t\"" + lang + "\"=\"" + bi.getTranslation(lang) + "\"\r\n");
						}
						out.writeChars("\r\n");
					}
				}
			}
			catch (Exception e) {
				infoException(fileName, e);
			}
		}
	}

	private boolean inArray(String[] array, String lang) {
		for (int j = 0; (array != null) && (j < array.length); ++j) {
			if ((array[j] != null) && array[j].equalsIgnoreCase(lang)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Reading unicode (UCS16) file stream into memory
	 */
	private String getBody(String file) throws IOException {
		try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
			StringBuilder buf = new StringBuilder(in.available());
			try {
				in.readChar(); // skip UCS16 marker FEFF
				for (;;) {
					char ch = in.readChar();
					buf.append(ch);
				}
			}
			catch (EOFException eof) {
			}
			return buf.toString();
		}
	}

	private void fillTable(Map<String, String> tbl) {
		for (Map.Entry<String, String> stringStringEntry : tbl.entrySet()) {
			StringTokenizer st = new StringTokenizer(stringStringEntry.getKey(), "!");
			String key = st.nextToken();
			if (!st.hasMoreTokens()) {
				continue;
			}
			String lang = st.nextToken();

			if (bundle.getBundle().getLanguage(lang) == null) {
				bundle.getBundle().addLanguage(lang);
			}

			bundle.getBundle().addKey(key);
			bundle.getBundle().updateValue(key, lang, stringStringEntry.getValue());
		}
	}

	private void onLoadXml(String fileName) throws Exception {
		if (fileName != null) {
			bundle.getBundle().addLanguage("en");

			XmlReader xml = new XmlReader(getBody(fileName));
			fillTable(xml.flatten());
		}
	}

	private void onLoadUtf(String fileName) throws Exception {
		if (fileName != null) {
			bundle.getBundle().addLanguage("en");
			UtfParser parser = new UtfParser(new StringReader(getBody(fileName)));
			Map<String, String> tbl = parser.parse();
			fillTable(tbl);
		}
	}

	private void tryToLoad(String fileName) throws IOException {
		try {
			onParseCode(fileName);
			return;
		}
		catch (Exception e) {
		}
		try {
			onLoadXml(fileName);
			return;
		}
		catch (Exception e) {
		}
		try {
			onLoadUtf(fileName);
			return;
		}
		catch (Exception e) {
		}
		try {
			readResources(fileName, true);
			return;
		}
		catch (Exception e) {
		}
		throw new IOException(fileName + ": wrong file format or file unavailable");
	}

	public static void main(String[] args) {
		try {
			String command = args[0];
			String fileName = args[1];
			String[] options = new String[args.length - 2];
			System.arraycopy(args, 2, options, 0, options.length);
			Split obj = new Split(fileName);
			switch (command) {
				case "join":
					for (String option : options) {
						obj.tryToLoad(option);
					}
					obj.onSaveAs(fileName);
					break;
				case "split":
					String dstFile = options[0];
					options[0] = null;
					if (dstFile.endsWith(".txt")) {
						obj.onSaveUtf(dstFile, options);
					}
					else if (dstFile.endsWith(".xml")) {
						obj.onSaveXml(dstFile, options);
					}
					else if (dstFile.endsWith(".java")) {
						obj.onGenCode(dstFile);
					}
					else {
						throw new IOException(dstFile + ": wrong file format or I/O error");
					}
					break;
				default:
					throw new Exception();
			}
		}
		catch (IOException eio) {
			System.err.println(eio.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Usage:\n"
				+ "\tjrc-split join srcFile ... addFile\n"
				+ "\tjrc-split split srcFile dstFile [lang ...]\n"
				+ "Where:\n"
				+ "\taddFile\t- XML, Java, other bundle set or UCS16 text file\n"
				+ "\tsrcFile\t- a root file of properties bundle set\n"
				+ "\tdstFile\t- XML, Java, other bundle set or UCS16 text file\n"
				+ "\tlang\t- locale abbreviation (suffix of slave properties files)\n");
		}
	}
}
