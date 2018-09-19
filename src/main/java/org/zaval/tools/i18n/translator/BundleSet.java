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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class BundleSet {
	private final List<BundleItem> items = new ArrayList<>();
	private final List<LangItem> lng = new ArrayList<>();
	private final Map<String, BundleItem> nameCache = new HashMap<>();

	private void addLanguage(String slng, String desc) {
		if (getLanguage(slng) != null) {
			return;
		}
		LangItem newl = new LangItem(slng, desc);
		lng.add(newl);
		correctFileName(newl);
	}

	int getLangCount() {
		return lng.size();
	}

	LangItem getLanguage(int idx) {
		return lng.get(idx);
	}

	LangItem getLanguage(String lng) {
		int j = getLangIndex(lng);
		return j < 0 ? null : getLanguage(j);
	}

	int getLangIndex(String lng) {
		int k = getLangCount();
		for (int j = 0; j < k; ++j) {
			LangItem lx = getLanguage(j);
			if (lx.getLangId().equals(lng)) {
				return j;
			}
		}
		return -1;
	}

	int getItemCount() {
		return items.size();
	}

	BundleItem getItem(int idx) {
		return items.get(idx);
	}

	BundleItem getItem(String key) {
		return nameCache.get(key);
	}

	int getItemIndex(String key) {
		int k = getItemCount();
		for (int j = k - 1; j >= 0; --j) {
			BundleItem bi = getItem(j);
			if (bi.getId().equals(key)) {
				return j;
			}
		}
		return -1;
	}

	BundleItem addKey(String key) {
		BundleItem ask = getItem(key);
		if (ask == null) {
			ask = new BundleItem(key);
			items.add(ask);
			nameCache.put(key, ask);
		}
		return ask;
	}

	void removeKey(String key) {
		int j = getItemIndex(key);
		if (j >= 0) {
			items.remove(j);
		}
		nameCache.remove(key);
	}

	List<BundleItem> getKeysBeginningWith(String key) {
		return IntStream.range(0, getItemCount())
			.mapToObj(this::getItem)
			.filter(bi -> bi.getId().startsWith(key))
			.collect(Collectors.toList());
	}

	void removeKeysBeginningWith(String key) {
		for (int j = 0; j < getItemCount(); ++j) {
			BundleItem bi = getItem(j);
			if (bi.getId().startsWith(key)) {
				removeKey(bi.getId());
				--j;
			}
		}
	}

	void updateValue(String key, String lang, String value) {
		BundleItem bi = getItem(key);
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

		for (int j = 0; j < getItemCount(); ++j) {
			BundleItem bi = getItem(j);
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
		if (getLangCount() < 1) {
			return;
		}
		LangItem lan0 = getLanguage(0);
		if (lan0 == lang) {
			return;
		}
		if (lang.getLangFile() != null) {
			return;
		}
		if (lan0.getLangFile() == null) {
			return;
		}
		String base = lan0.getLangFile();
		int j = base.lastIndexOf('.');
		if (j < 0) {
			return;
		}
		base = base.substring(0, j) + "_" + lang.getLangId() + base.substring(j);
		lang.setLangFile(base);
	}

	public void resort() {
		items.sort(new BundleItemComparator());
	}

	private static class BundleItemComparator implements Comparator<BundleItem> {
		@Override
		public int compare(BundleItem o1, BundleItem o2) {
			return o1.getId().compareTo(o2.getId());
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}
	}
}
