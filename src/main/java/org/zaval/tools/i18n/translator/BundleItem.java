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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class BundleItem {
	private final String identifier;
	private final Map<String, String> translations = new HashMap<>();
	private String comment;

	BundleItem(String id) {
		identifier = id;
	}

	String getId() {
		return identifier;
	}

	String getTranslation(String lng) {
		return translations.get(lng);
	}

	String getComment() {
		return comment;
	}

	void setComment(String s) {
		comment = s;
	}

	void setTranslation(String lng, String txt) {
		translations.put(lng, txt);
	}

	Set<String> getLanguages() {
		return translations.keySet();
	}
}
