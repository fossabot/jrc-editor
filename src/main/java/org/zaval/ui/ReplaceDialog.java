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

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

@SuppressWarnings("serial")
public class ReplaceDialog extends EditDialog {
	private final JRadioButton regex = new JRadioButton("", false);
	private final JRadioButton exact = new JRadioButton("", true);

	private final JCheckBox cases = new JCheckBox("", true);
	private final JCheckBox prompt = new JCheckBox("", true);
	private final JCheckBox all = new JCheckBox("", false);
	private final JTextField replaceTo = new JTextField(20);
	private final JLabel label = new JLabel("");

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

	public ReplaceDialog(JFrame f, String s, boolean b) {
		super(f, s, b);
		ButtonGroup matchType = new ButtonGroup();
		matchType.add(regex);
		matchType.add(exact);
		replaceTo.addActionListener(this::onPerform);
		constrain(this, label, 0, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.NONE, 0.0, 0.0, 0, 5, 5, 5);
		constrain(this, replaceTo, 1, 1, 1, 1, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, 1.0, 0.0, 0, 5, 0, 5);

		JPanel p2 = new JPanel();
		p2.setBorder(new LineBorder(null));
		p2.setLayout(new GridBagLayout());
		constrain(p2, exact, 1, 0, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0.0, 0.0, 0, 5, 0, 5);
		constrain(p2, regex, 1, 1, 1, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0.0, 0.0, 0, 5, 0, 5);

		JPanel p = new JPanel(new GridBagLayout());
		constrain(p, p2, 0, 1, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1.0, 1.0, 5, 0, 0, 0);
		constrain(p, cases, 0, 2, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0.0, 0.0, 5, 5, 0, 5);
		constrain(p, prompt, 0, 3, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0.0, 0.0, 0, 5, 0, 5);
		constrain(p, all, 0, 4, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 0.0, 0.0, 0, 5, 5, 5);

		constrain(this, p, 0, 2, 2, 1, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 1.0, 1.0, 5, 5, 5, 5);
		renderDialogFooter();
	}
}
