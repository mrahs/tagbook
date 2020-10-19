/*******************************************************************************
 Copyright (c) 2014 - Anas H. Sulaiman (ahs.pw)

 	This file is part of TagBook.

     TagBook is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     TagBook is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with TagBook.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package pw.ahs.app.tagbook.gui;

import pw.ahs.app.tagbook.core.utils.TBConsts;
import pw.ahs.app.tagbook.core.utils.TBText;
import pw.ahs.app.tagbook.gui.utils.TBGuiUtils;
import pw.ahs.app.tagbook.gui.utils.TBIcon;
import pw.ahs.app.tagbook.gui.utils.TBKeyStroke;
import pw.ahs.app.tagbook.gui.utils.TBMnemonic;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class SettingsDialog extends JDialog {
	private JTextField txtDbPath;
	private JCheckBox cbSearchMethod;
	private JCheckBox cbUseHyper;
	private JCheckBox cbMiniToTray;
	private JCheckBox cbConfirmExit;
	private boolean accepted;

	public SettingsDialog(Frame parent) {
		super(parent, true);
		accepted = false;
		initGui();

		// center dialog
		setLocationRelativeTo(parent);
	}

	private void initGui() {
		// initialize dialog
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(TBText.get("settingsDialog.t"));
		setIconImage(TBIcon.get("settings").getImage());
		setPreferredSize(new Dimension(300, 200));
		getContentPane().setLayout(new GridBagLayout());

		Action saveAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				accepted = true;
				dispose();
			}
		};
		TBGuiUtils.initializeAction(saveAction, "Save", "Save",
				TBMnemonic.get("save"), TBIcon.get("save"),
				TBKeyStroke.get("save"));

		Action cancelAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				accepted = false;
				dispose();
			}
		};
		TBGuiUtils.initializeAction(cancelAction, "Cancel", "Cancel",
				TBMnemonic.get("cancel"), TBIcon.get("cancel"),
				TBKeyStroke.get("cancel"));

		getRootPane().registerKeyboardAction(cancelAction,
				TBKeyStroke.get("cancel"),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		getRootPane().registerKeyboardAction(saveAction, TBKeyStroke.get("ok"),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

		// initialize dbPath
		txtDbPath = new JTextField();
		txtDbPath.setEditable(false);
		JLabel lblDbPath = new JLabel("book:");
		lblDbPath.setLabelFor(txtDbPath);

		// initialize search instantly checkbox
		cbSearchMethod = new JCheckBox();
		cbSearchMethod.setText("Search Instantly");
		cbSearchMethod.setMnemonic('i');

		// initialize useHyper checkbox
		cbUseHyper = new JCheckBox();
		cbUseHyper.setText("Use Hyperlinks");
		if (!TBConsts.IsDesktopSupported) {
			cbUseHyper
					.setToolTipText("This option is not supported on your platform");
			cbUseHyper.setSelected(false);
			cbUseHyper.setEnabled(false);
		}
		cbUseHyper.setMnemonic('h');

		// initialize miniToTray checkbox
		cbMiniToTray = new JCheckBox();
		cbMiniToTray.setText("X button minimize to tray");
		cbMiniToTray.setMnemonic('x');

		// initialize autoAddSTags
		cbConfirmExit = new JCheckBox();
		cbConfirmExit.setText("Confirm exit using the X button");
		cbConfirmExit.setMnemonic('c');

		// add to layout
		GridBagConstraints gbc;

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_END;
		getContentPane().add(lblDbPath, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		getContentPane().add(txtDbPath, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.LINE_START;
		getContentPane().add(cbSearchMethod, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.LINE_START;
		getContentPane().add(cbUseHyper, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.LINE_START;
		getContentPane().add(cbMiniToTray, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.LINE_START;
		getContentPane().add(cbConfirmExit, gbc);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(new JButton(saveAction));
		buttonPanel.add(new JButton(cancelAction));

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		getContentPane().add(buttonPanel, gbc);

		pack();
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setDbPath(String path) {
		txtDbPath.setText(path);
	}

	public boolean getSearchIntantly() {
		return cbSearchMethod.isSelected();
	}

	public void setSearchInstantly(boolean state) {
		cbSearchMethod.setSelected(state);
	}

	public boolean getUseHyperlinks() {
		return cbUseHyper.isSelected();
	}

	public void setUseHyperlinks(boolean state) {
		if (cbUseHyper.isEnabled())
			cbUseHyper.setSelected(state);
	}

	public boolean getMiniToTray() {
		return cbMiniToTray.isSelected();
	}

	public void setMiniToTray(boolean state) {
		cbMiniToTray.setSelected(state);
	}

	public boolean getConfirmExit() {
		return cbConfirmExit.isSelected();
	}

	public void setConfirmExit(boolean state) {
		cbConfirmExit.setSelected(state);
	}

	public static void main(String[] args) {
		new SettingsDialog(null).setVisible(true);
	}
}
