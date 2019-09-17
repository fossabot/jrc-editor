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

package org.zaval.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class XmlReader {
	private final XmlElement xml = new XmlElement();

	public XmlReader(String body) throws IOException, XmlParseException {
		xml.parse(new StringReader(body));
	}

	public Map<String, String> flatten() {
		Map<String, String> ask = new HashMap<>();
		for (XmlElement child : xml.children()) {
			getTable(ask, child, "");
		}
		return ask;
	}

	private void getTable(Map<String, String> place, XmlElement root, String prefix) {
		String xmap = (String) root.getAttribute("lang");
		if (xmap == null) {
			xmap = (String) root.getAttribute("name");
		}
		if (xmap == null) {
			xmap = root.getName();
		}
		String name = prefix + xmap + "!";

		if (root.getContent() != null) {
			place.put(prefix + xmap, root.getContent());
		}

		for (XmlElement child : root.children()) {
			getTable(place, child, name);
		}
	}
}
