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

package org.zaval.awt.peer;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import org.zaval.awt.ImageResolver;

public class TreeNode {
	private ImageResolver imgres;

	public TreeNode sibling;
	public TreeNode child;
	public TreeNode parent;
	public final String text;
	private String nameCollImage;
	private String nameExpImage;
	private Image collapsedImage;
	private Image expandedImage;
	public int depth = -1;
	private boolean isExpanded;
	public int numberOfChildren;
	private int contextMenu = -1;
	private final Map<String, Object> property = new HashMap<>();
	public boolean hidden;
	public String caption;
	private Image indicator;

	public void setResolver(ImageResolver imgres) {
		this.imgres = imgres;
	}

	public TreeNode(String text) {
		this(text, null, null);
	}

	public TreeNode(String text, String nameCollImage, String nameExpImage) {
		this.text = text;
		this.sibling = null;
		this.child = null;
		this.nameCollImage = nameCollImage;
		this.nameExpImage = nameExpImage;

		numberOfChildren = 0;
		caption = null;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return depth;
	}

	public boolean isExpanded() {
		return !hidden && isExpanded;
	}

	public boolean isExpandable() {
		return !hidden && (child != null);
	}

	public void expand() {
		if (isExpandable()) {
			isExpanded = true;
		}
	}

	public void collapse() {
		isExpanded = false;
	}

	public void toggle() {
		if (isExpanded) {
			collapse();
		}
		else if (isExpandable()) {
			expand();
		}
	}

	public Image getImage() {
		return ((isExpanded && (expandedImage != null)) ? expandedImage : collapsedImage);
	}

	public Image getExpandedImage() {
		loadImages();
		return (expandedImage != null) ? expandedImage : collapsedImage;
	}

	public Image getCollapsedImage() {
		loadImages();
		return collapsedImage;
	}

	private void loadImages() {
		if (imgres == null) {
			return;
		}
		if ((nameCollImage != null) && (collapsedImage == null)) {
			collapsedImage = imgres.getImage(nameCollImage);
		}
		if ((nameExpImage != null) && (expandedImage == null)) {
			expandedImage = imgres.getImage(nameExpImage);
		}
	}

	public void setExpandedImage(String image) {
		this.nameCollImage = image;
		if ((image != null) && (imgres != null)) {
			this.collapsedImage = imgres.getImage(nameCollImage);
		}
	}

	public void setCollapsedImage(String image) {
		this.nameExpImage = image;
		if ((image != null) && (imgres != null)) {
			this.expandedImage = imgres.getImage(nameExpImage);
		}
	}

	public String getText() {
		return text;
	}

	public void setContextMenu(int index) {
		contextMenu = index;
	}

	public int getContextMenu() {
		return contextMenu;
	}

	private Object getProperty(String name) {
		return property.get(name);
	}

	public String getStringProperty(String name) {
		return (String) getProperty(name);
	}

	private void setProperty(String name, Object value) {
		property.put(name, value);
	}

	public void setStringProperty(String name, String value) {
		setProperty(name, value);
	}

	public void setHide(boolean b) {
		hidden = b;
	}

	public boolean getHide() {
		return hidden;
	}

	public void setIndicator(String name) {
		if ((name == null) || (imgres == null)) {
			indicator = null;
		}
		else {
			indicator = imgres.getImage(name);
		}
	}

	public Image getIndicator() {
		return indicator;
	}
}
