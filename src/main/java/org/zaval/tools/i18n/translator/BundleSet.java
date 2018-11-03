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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

class BundleSet {
	private final Map<String, BundleItem> items = new TreeMap<>();
	private final Map<String, LangItem> lng = new LinkedHashMap<>();

	private void addLanguage(String id, String desc) {
		lng.computeIfAbsent(id, k -> {
			LangItem lang = new LangItem(k, desc);
			correctFileName(lang);
			return lang;
		});
	}

	boolean hasLanguages() {
		return !lng.isEmpty();
	}

	int getLanguageCount() {
		return lng.size();
	}

	LangItem getFirstLanguage() {
		return lng.isEmpty() ? null : lng.entrySet().iterator().next().getValue();
	}

	LangItem[] getLanguages() {
		return lng.values().toArray(new LangItem[0]);
	}

	LangItem getLanguage(String id) {
		return lng.get(id);
	}

	int getLanguageIndex(String id) {
		int j = 0;
		for (LangItem lx : lng.values()) {
			if (lx.getId().equals(id)) {
				return j;
			}
			j++;
		}
		return -1;
	}

	int getItemCount() {
		return items.size();
	}

	BundleItem getItem(String key) {
		return items.get(key);
	}

	Stream<BundleItem> getItems() {
		return items.entrySet().stream().map(Map.Entry<String, BundleItem>::getValue);
	}

	int getItemIndex(String key) {
		int i = -1;
		for (BundleItem bi : items.values()) {
			if (bi.getId().equals(key)) {
				return i;
			}
			++i;
		}
		return -1;
	}

	BundleItem addKey(String key) {
		return items.computeIfAbsent(key, k -> new BundleItem(k));
	}

	void removeKey(String key) {
		items.remove(key);
	}

	Stream<BundleItem> getKeysBeginningWith(String key) {
		return items.entrySet().stream().filter(bi -> bi.getKey().startsWith(key)).map(it -> it.getValue());
	}

	void removeKeysBeginningWith(String key) {
		items.entrySet().removeIf(it -> it.getKey().startsWith(key));
	}

	void updateValue(String key, String lang, String value) {
		BundleItem bi = items.get(key);
		if (bi != null) {
			bi.setTranslation(lang, value);
		}
	}

	private Locale parseLanguage(String suffix) {
		if ((suffix == null) || (suffix.isEmpty())) {
			return null;
		}
		int undInd = suffix.indexOf('_');
		String sl = suffix;
		String sc = "";
		if (undInd > 0) {
			sl = suffix.substring(0, undInd);
			sc = suffix.substring(undInd + 1);
		}
		return new Locale(sl, sc);
	}

	void addLanguage(String lng) {
		Locale loc = parseLanguage(lng);
		if (loc != null) {
			String desc = loc.getDisplayLanguage();
			String sCountry = loc.getDisplayCountry();
			if ((sCountry != null) && (!sCountry.isEmpty())) {
				desc += " (" + sCountry + ")";
			}
			addLanguage(lng, desc);
		}
	}

	private String makeLine(String key, String val) {
		if (val == null) {
			return null;
		}
		return key + '=' + val;
	}

	List<String> store(String lng) {
		getLanguage(lng);
		List<String> lines = new ArrayList<>();

		for (BundleItem bi : items.values()) {
			if (bi.getComment() != null) {
				lines.add("#" + bi.getComment());
			}
			if (bi.getTranslation(lng) == null) {
				continue;
			}
			lines.add(makeLine(bi.getId(), bi.getTranslation(lng)));
		}
		return lines;
	}

	private void correctFileName(LangItem lang) {
		if (lng.isEmpty()) {
			return;
		}
		LangItem lan0 = lng.values().iterator().next();
		if (lan0 == lang) {
			return;
		}
		if (lang.getFileName() != null) {
			return;
		}
		if (lan0.getFileName() == null) {
			return;
		}
		String base = lan0.getFileName();
		int j = base.lastIndexOf('.');
		if (j < 0) {
			return;
		}
		base = base.substring(0, j) + "_" + lang.getId() + base.substring(j);
		lang.setFileName(base);
	}
}
