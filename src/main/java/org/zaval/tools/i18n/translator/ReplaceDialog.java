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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import org.zaval.awt.dialog.EditDialog;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

class ReplaceDialog extends EditDialog {
	private final JRadioButton regex;
	private final JRadioButton exact;

	private final JCheckBox cases;
	private final JCheckBox prompt;
	private final JCheckBox all;
	private final JTextField replaceTo;
	private final JLabel label;

	public void setReplaceLabel(String s) {
		label.setText(s);
	}

	public void setRMGroupLabels(String s1, String s3) {
		regex.setText(s1);
		exact.setText(s3);
	}

	public void setCPALabels(String s1, String s2, String s3) {
		cases.setText(s1);
		prompt.setText(s2);
		all.setText(s3);
	}

	public boolean isRegexMatching() {
		return regex.isSelected();
	}

	public boolean isCaseSensitive() {
		return cases.isSelected();
	}

	public boolean isPromptRequired() {
		return prompt.isSelected();
	}

	public boolean isReplaceAll() {
		return all.isSelected();
	}

	public String getReplaceTo() {
		return replaceTo.getText();
	}

	public ReplaceDialog(JFrame f, String s, boolean b, Component l) {
		super(f, s, b, l);

		regex = new JRadioButton("", false);
		exact = new JRadioButton("", true);
		ButtonGroup matchType = new ButtonGroup();
		matchType.add(regex);
		matchType.add(exact);
		cases = new JCheckBox("", true);
		prompt = new JCheckBox("", true);
		all = new JCheckBox("", false);

		label = new JLabel("To:");
		replaceTo = new JTextField(20);
		replaceTo.addActionListener(this);
		replaceTo.setActionCommand(COMMAND_OK);
		constrain(this, label, 0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0, 0, 5, 5, 5);
		constrain(this, replaceTo, 1, 1, 1, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, 1.0, 0.0, 0, 5, 5, 0);

		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());

		JPanel p2 = new JPanel();
		p2.setBorder(new LineBorder(null));
		p2.setLayout(new GridBagLayout());
		constrain(p2, exact, 1, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 5, 5, 0);
		constrain(p2, regex, 1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 5, 5, 0);
		constrain(p, p2, 0, 1, 2, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 1.0, 5, 0, 0, 0);

		constrain(p, cases, 0, 2, 2, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 5, 5, 5, 0);
		constrain(p, prompt, 0, 3, 2, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 5, 5, 0);
		constrain(p, all, 0, 4, 2, 1, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, 0.0, 0.0, 0, 5, 5, 5);

		constrain(this, p, 0, 2, 2, 1, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, 1.0, 1.0, 5, 5, 5, 5);
		renderDialogFooter();
	}
}
