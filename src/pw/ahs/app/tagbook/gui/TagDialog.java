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

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SortOrder;
import javax.swing.WindowConstants;

import pw.ahs.app.tagbook.core.Tag;
import pw.ahs.app.tagbook.core.utils.TBError;
import pw.ahs.app.tagbook.core.utils.TBText;
import pw.ahs.app.tagbook.gui.utils.TBIcon;
import pw.ahs.app.tagbook.gui.utils.TBKeyStroke;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

@SuppressWarnings("serial")
public class TagDialog extends JDialog {
	private JXComboBox comboTag;
	private JXList lstTags;
	private DefaultListModel<String> lstModelTags;

	public TagDialog(Frame owner, Object[] availTags) {
		super(owner, true);
		init(availTags);
		setLocationRelativeTo(owner);
	}

	private TagDialog(Frame owner) {
		this(owner, null);
	}

	private TagDialog(Dialog owner, Object[] availTags) {
		super(owner, true);
		init(availTags);
		setLocationRelativeTo(owner);
	}

	public TagDialog(Dialog owner) {
		this(owner, null);
	}

	private void init(Object[] availTags) {
		getContentPane().setLayout(new GridBagLayout());
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle(TBText.get("tagDialog.t"));
		setIconImage(TBIcon.get("tagManager").getImage());

		comboTag = new JXComboBox();
		comboTag.setEditable(true);
		comboTag.setToolTipText("enter tag");
		comboTag.setPreferredSize(new Dimension(100, 20));
		comboTag.getEditor().addActionListener(new ActionListener() {

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void actionPerformed(ActionEvent e) {
				String tag = e.getActionCommand();
				if (!tag.isEmpty()) {
					if (!Tag.isValidTagName(tag)) {
						JOptionPane.showMessageDialog(TagDialog.this,
								TBError.getMessage("eTag.m"),
								TBError.getMessage("eTag.t"),
								JOptionPane.ERROR_MESSAGE);
					} else {
						lstModelTags.addElement(tag);
						if (((DefaultComboBoxModel) comboTag.getModel())
								.getIndexOf(tag) == -1) {
							comboTag.addItem(tag);
						}
					}
				}
			}
		});
		AutoCompleteDecorator.decorate(comboTag,
				ObjectToStringConverter.DEFAULT_IMPLEMENTATION);

		lstTags = new JXList();
		lstTags.setToolTipText("Tags");
		lstTags.setAutoCreateRowSorter(true);
		lstTags.setSortOrder(SortOrder.ASCENDING);
		lstTags.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent evt) {
				if (KeyStroke.getKeyStroke(evt.getKeyCode(), evt.getModifiers()) == TBKeyStroke
						.get("delete")) {
					int i = lstTags.getSelectedIndex();
					while (i != -1) {
						lstModelTags.remove(lstTags.convertIndexToModel(i));
						i = lstTags.getSelectedIndex();
					}
				}
			}
		});
		JScrollPane scrlLstTags = new JScrollPane(lstTags);
		scrlLstTags.setPreferredSize(new Dimension(100, 100));

		lstModelTags = new DefaultListModel<String>() {

			@Override
			public void add(int index, String element) {
				if (!contains(element)) {
					super.add(index, element);
				}
			}

			@Override
			public void addElement(String element) {
				if (!contains(element)) {
					super.addElement(element);
				}
			}
		};
		lstTags.setModel(lstModelTags);

		JButton okButton = new JButton();
		okButton.setText("Done");
		okButton.setMnemonic('d');
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				TagDialog.this.setVisible(false);
			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.weightx = 0.3;
		getContentPane().add(comboTag, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.7;
		gbc.weighty = 1.0;
		getContentPane().add(scrlLstTags, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		getContentPane().add(okButton, gbc);

		pack();

		if (availTags != null) {
			setAvailableTags(availTags);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
    void setAvailableTags(Object[] tags) {
		Arrays.sort(tags);
		comboTag.setModel(new DefaultComboBoxModel(tags));
	}

	public String[] getTags() {
		if (lstModelTags.getSize() == 0) {
			return null;
		}

		String[] tags = new String[lstModelTags.size()];
		lstModelTags.copyInto(tags);
		return tags;
	}

	public String[] showTagDialog() {
		setVisible(true);
		return getTags();
	}

	public static void main(String[] args) {
		new TagDialog((Frame)null).setVisible(true);
	}
}
