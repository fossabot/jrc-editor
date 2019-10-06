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

package org.zaval.ui;

import static org.zaval.ui.UiUtils.constrain;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;

@SuppressWarnings("serial")
public class SearchDialog extends EditDialog {
	private final JRadioButton inKeys = new JRadioButton("", false);
	private final JRadioButton inVals = new JRadioButton("", true);

	private final JRadioButton regex = new JRadioButton("", false);
	private final JRadioButton mask = new JRadioButton("", false);
	private final JRadioButton exact = new JRadioButton("", true);

	private final JCheckBox cases = new JCheckBox("", true);

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

	public SearchDialog(JFrame f, String s, boolean b) {
		super(f, s, b);
		ButtonGroup keysOrValues = new ButtonGroup();
		keysOrValues.add(inKeys);
		keysOrValues.add(inVals);

		ButtonGroup matchTypeGroup = new ButtonGroup();
		matchTypeGroup.add(regex);
		matchTypeGroup.add(mask);
		matchTypeGroup.add(exact);

		JPanel p1 = new JPanel(new GridLayout(2, 1));
		p1.setBorder(new LineBorder(null));
		p1.add(inVals);
		p1.add(inKeys);

		JPanel p2 = new JPanel(new GridLayout(3, 1));
		p2.setBorder(new LineBorder(null));
		p2.add(exact);
		p2.add(mask);
		p2.add(regex);

		JPanel p = new JPanel(new GridBagLayout());
		constrain(p, p1, 0, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1.0, 1.0, 5, 0, 0, 5);
		constrain(p, p2, 1, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1.0, 1.0, 5, 0, 0, 0);
		constrain(p, cases, 0, 3, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0.0, 0.0, 5, 5, 5, 5);

		constrain(this, p, 0, 1, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1.0, 1.0, 5, 5, 5, 5);
		renderDialogFooter();
	}
}
