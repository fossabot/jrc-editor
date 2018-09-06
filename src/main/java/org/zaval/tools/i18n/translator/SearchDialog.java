/*
 * Copyright (C) 2001-2002  Zaval Creative Engineering Group (http://www.zaval.org)
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

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;

import org.zaval.awt.BorderedPanel;
import org.zaval.awt.dialog.EditDialog;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

class SearchDialog extends EditDialog {
	private final JRadioButton inKeys;
	private final JRadioButton inVals;

	private final JRadioButton regex;
	private final JRadioButton mask;
	private final JRadioButton exact;

	private final JCheckBox cases;

	public void setKVGroupLabels(String s1, String s2) {
		inKeys.setText(s1);
		inVals.setText(s2);
	}

	public void setRMEGroupLabels(String s1, String s2, String s3) {
		regex.setText(s1);
		mask.setText(s2);
		exact.setText(s3);
	}

	public void setCaseLabel(String s1) {
		cases.setText(s1);
	}

	public boolean isKeyMatching() {
		return inKeys.isSelected();
	}

	public boolean isMaskMatching() {
		return mask.isSelected();
	}

	public boolean isRegexMatching() {
		return regex.isSelected();
	}

	public boolean isCaseSensitive() {
		return cases.isSelected();
	}

	public SearchDialog(Frame f, String s, boolean b, Component l) {
		super(f, s, b, l);

		inKeys = new JRadioButton("", false);
		inVals = new JRadioButton("", true);
		ButtonGroup keysOrValues = new ButtonGroup();
		keysOrValues.add(inKeys);
		keysOrValues.add(inVals);

		regex = new JRadioButton("", false);
		mask = new JRadioButton("", false);
		exact = new JRadioButton("", true);
		ButtonGroup matchTypeGroup = new ButtonGroup();
		matchTypeGroup.add(regex);
		matchTypeGroup.add(mask);
		matchTypeGroup.add(exact);

		cases = new JCheckBox("", true);

		Panel p = new Panel();
		p.setLayout(new GridBagLayout());

		Panel p1 = new BorderedPanel();
		p1.setLayout(new GridBagLayout());
		Panel p2 = new BorderedPanel();
		p2.setLayout(new GridBagLayout());

		constrain(p1, inVals, 0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 5, 5, 0);
		constrain(p1, inKeys, 0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 5, 5, 0);

		constrain(p2, exact, 1, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 5, 5, 0);
		constrain(p2, mask, 1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 5, 5, 0);
		constrain(p2, regex, 1, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 5, 5, 0);

		constrain(p, p1, 0, 0, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 1.0, 5, 0, 5, 0);
		constrain(p, p2, 1, 0, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 1.0, 5, 0, 0, 0);
		constrain(p, cases, 0, 3, 2, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 5, 5, 5, 5);

		constrain(this, p, 0, 1, 2, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 1.0, 5, 5, 5, 5);
	}
}
