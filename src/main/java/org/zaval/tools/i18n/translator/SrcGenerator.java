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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

class SrcGenerator {
	private final PrintStream out;
	private final String filename;

	SrcGenerator(String filename) throws IOException {
		FileOutputStream fop = new FileOutputStream(filename);
		this.out = new PrintStream(fop);
		this.filename = filename;
	}

	void perform(BundleSet set) {
		out.println("import java.util.*;\n\npublic class " + baseName(filename) + "\n{");
		int j;
		int k = set.getItemCount();
		for (j = 0; j < k; ++j) {
			BundleItem bi = set.getItem(j);
			out.println("\tprivate String " + makeVarName(bi) + ";");
		}
		out.println();
		for (j = 0; j < k; ++j) {
			BundleItem bi = set.getItem(j);
			out.println("\tpublic final String get" + makeFunName(bi) + "()\t{ return " + makeVarName(bi) + ";}");
		}
		out.println();
		for (j = 0; j < k; ++j) {
			BundleItem bi = set.getItem(j);
			out.println("\tpublic final void set" + makeFunName(bi) + "(String what)\t{ this." + makeVarName(bi) + " = what;}");
		}
		out.println();
		out.println("\tpublic void loadFromResource(ResourceBundle rs)\n\t{");
		for (j = 0; j < k; ++j) {
			BundleItem bi = set.getItem(j);
			out.println("\t\ttry{ set"
				+ makeFunName(bi)
				+ "(rs.getString(\""
				+ bi.getId()
				+ "\")); } catch(Exception error){ reportNoRc(\""
				+ bi.getId()
				+ "\", error); }");
		}
		out.println("\t}\n");
		out.println("\tprivate void reportNoRc(String what, Exception details)\n\t{\n"
			+ "\t\tSystem.err.println(what + \": unknown resource\");\n"
			+ "\t\tdetails.printStackTrace();\n\t}\n");
		out.println("}");
		out.close();
	}

	private String makeVarName(BundleItem bi) {
		String ask = bi.getTranslation("__var");
		if (ask != null) {
			return ask;
		}
		ask = makeVarName(bi.getId());
		bi.setTranslation("__var", ask);
		return ask;
	}

	private String makeFunName(BundleItem bi) {
		String ask = bi.getTranslation("__varF");
		if (ask != null) {
			return ask;
		}
		ask = capitalize(makeVarName(bi.getId()));
		bi.setTranslation("__varF", ask);
		return ask;
	}

	private String makeVarName(String key) {
		String s = key.toLowerCase();
		int j1 = s.lastIndexOf('.');
		if (j1 < 0) {
			return s;
		}
		int j2 = s.lastIndexOf('.', j1 - 1);
		s = s.substring(j2 + 1, j1) + capitalize(s.substring(j1 + 1));
		return s;
	}

	private String capitalize(String s) {
		if (s.length() < 2) {
			return s.toUpperCase();
		}
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	private String baseName(String fn) {
		int ind = fn.lastIndexOf('/');
		fn = ind >= 0 ? fn.substring(ind + 1) : fn;
		ind = fn.lastIndexOf('.');
		return ind >= 0 ? fn.substring(0, ind) : fn;
	}
}
