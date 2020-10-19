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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import pw.ahs.app.tagbook.core.Bookmark;
import pw.ahs.app.tagbook.core.utils.TBError;
import pw.ahs.app.tagbook.core.utils.TBText;
import pw.ahs.app.tagbook.gui.utils.TBIcon;
import pw.ahs.app.tagbook.gui.utils.TBKeyStroke;

import org.jdesktop.swingx.JXHyperlink;

@SuppressWarnings("serial")
class StatsDialog extends JDialog {
	private long countB;
	private long countT;
	private long countUntagged;
	private long countTagged;
	private long countOrph;
	private long countLinks;
	private long countEmails;
	private int countMostPopularTag;
	private int countMostTaggedBookmark;
	private Bookmark mostTaggedBookmark;
	private String mostPopularTag;
	private JXHyperlink linkMostTaggedBookmark;
	private CustomDialog cdMostTaggedBookmark;
	private JLabel lblBCountValue;
	private JLabel lblTCountValue;
	private JLabel lblOrphValue;
	private JLabel lblLinkValue;
	private JLabel lblEmailValue;
	private JLabel lblUntaggedValue;
	private JLabel lblTaggedValue;
	private JLabel lblMostPopularTagValue;
	private JLabel lblMostPopularTagName;
	private JLabel lblMostTaggedBookmarkValue;

	public StatsDialog(Frame parent) {
		super(parent, true);
		initDialog();
		initLink();
		initDialogMTP();
		initComponents();
		initData();
	}

	private void initDialog() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(TBText.get("stats.t"));
		setIconImage(TBIcon.get("stats").getImage());
		setResizable(false);

		getRootPane().registerKeyboardAction(
				new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						dispatchEvent(new WindowEvent(StatsDialog.this,
								WindowEvent.WINDOW_CLOSING));
					}
				}, TBKeyStroke.get("cancel"),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	private void initLink() {
		linkMostTaggedBookmark = new JXHyperlink(new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				cdMostTaggedBookmark
						.setText(mostTaggedBookmark == null ? TBError
								.getMessage("stats.noBookmark")
								: mostTaggedBookmark.toString());
				cdMostTaggedBookmark.setLocationRelativeTo(StatsDialog.this);
				cdMostTaggedBookmark.setVisible(true);
			}
		});
		linkMostTaggedBookmark.setText(TBError.getMessage("stats.noBookmark"));
		linkMostTaggedBookmark.setToolTipText("Show Details");
	}

	private void initDialogMTP() {
		cdMostTaggedBookmark = new CustomDialog(this);
		cdMostTaggedBookmark.setTitle("Most Tagged Bookmark");
	}

	private void initComponents() {
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints gbc;
		Insets leftRightMargin = new Insets(0, 10, 0, 5);
		Insets topMargin = new Insets(10, 0, 0, 0);
		Insets bottomMargin = new Insets(0, 0, 10, 0);

		JLabel lblHeader = new JLabel();
		JSeparator separatorHeader = new JSeparator(SwingConstants.HORIZONTAL);
		JLabel lblBCountTitle = new JLabel();
		lblBCountValue = new JLabel();
		JLabel lblTCountTitle = new JLabel();
		lblTCountValue = new JLabel();
		JLabel lblOrphTitle = new JLabel();
		lblOrphValue = new JLabel();
		JLabel lblLinkTitle = new JLabel();
		lblLinkValue = new JLabel();
		JLabel lblEmailTitle = new JLabel();
		lblEmailValue = new JLabel();
		JLabel lblUntaggedTitle = new JLabel();
		lblUntaggedValue = new JLabel();
		JLabel lblTaggedTitle = new JLabel();
		lblTaggedValue = new JLabel();
		JLabel lblMostPopularTagTitle = new JLabel();
		lblMostPopularTagValue = new JLabel();
		lblMostPopularTagName = new JLabel();
		JLabel lblMostTaggedBookmarkTitle = new JLabel();
		lblMostTaggedBookmarkValue = new JLabel();

		Font fontTitle = new Font(Font.SANS_SERIF, Font.BOLD, 13);
		Font fontValue = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

		lblHeader.setFont(new Font(Font.SERIF, Font.BOLD, 18));
		lblHeader.setText(TBText.get("stats.header"));

		lblBCountTitle.setFont(fontTitle);
		lblBCountTitle.setText("Bookmarks:");
		lblBCountTitle.setLabelFor(lblBCountValue);

		lblBCountValue.setFont(fontValue);
		lblBCountValue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBCountValue.setText(null);

		lblTCountTitle.setFont(fontTitle);
		lblTCountTitle.setText("Tags:");
		lblTCountTitle.setLabelFor(lblTCountValue);

		lblTCountValue.setFont(fontValue);
		lblTCountValue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTCountValue.setText(null);

		lblOrphTitle.setFont(fontTitle);
		lblOrphTitle.setText("Orphaned Tags:");
		lblOrphTitle.setLabelFor(lblOrphValue);

		lblOrphValue.setFont(fontValue);
		lblOrphValue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOrphValue.setText(null);

		lblLinkTitle.setFont(fontTitle);
		lblLinkTitle.setText("Link Bookmarks:");
		lblLinkTitle.setLabelFor(lblLinkValue);

		lblLinkValue.setFont(fontValue);
		lblLinkValue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLinkValue.setText(null);

		lblEmailTitle.setFont(fontTitle);
		lblEmailTitle.setText("Email Bookmarks:");
		lblEmailTitle.setLabelFor(lblEmailValue);

		lblEmailValue.setFont(fontValue);
		lblEmailValue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblEmailValue.setText(null);

		lblUntaggedTitle.setFont(fontTitle);
		lblUntaggedTitle.setText("Untagged Bookmarks:");
		lblUntaggedTitle.setLabelFor(lblUntaggedValue);

		lblUntaggedValue.setFont(fontValue);
		lblUntaggedValue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUntaggedValue.setText(null);

		lblTaggedTitle.setFont(fontTitle);
		lblTaggedTitle.setText("Tagged Bookmarks:");
		lblTaggedTitle.setLabelFor(lblTaggedValue);

		lblTaggedValue.setFont(fontValue);
		lblTaggedValue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTaggedValue.setText(null);

		lblMostPopularTagTitle.setFont(fontTitle);
		lblMostPopularTagTitle.setText("Most Popular Tag:");
		lblMostPopularTagTitle.setLabelFor(lblMostPopularTagValue);

		lblMostPopularTagValue.setFont(fontValue);
		lblMostPopularTagValue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMostPopularTagValue.setText(null);

		lblMostPopularTagName.setFont(fontValue);
		lblMostPopularTagName.setText(null);

		lblMostTaggedBookmarkTitle.setFont(fontTitle);
		lblMostTaggedBookmarkTitle.setText("Most Tagged Bookmark:");
		lblMostTaggedBookmarkTitle.setLabelFor(lblMostTaggedBookmarkValue);

		lblMostTaggedBookmarkValue.setFont(fontValue);
		lblMostTaggedBookmarkValue.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMostTaggedBookmarkValue.setText(null);

		// add to layout
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridwidth = 3;
		gbc.weightx = 1f;
		gbc.insets = new Insets(topMargin.top, 0, bottomMargin.bottom, 0);
		getContentPane().add(lblHeader, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 3;
		gbc.weightx = 1f;
		gbc.insets = new Insets(topMargin.top,
				lblHeader.getPreferredSize().width + 5, 0, 0);
		getContentPane().add(separatorHeader, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblBCountTitle, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblBCountValue, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblTCountTitle, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblTCountValue, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblOrphTitle, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblOrphValue, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblLinkTitle, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblLinkValue, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblEmailTitle, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblEmailValue, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblUntaggedTitle, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblUntaggedValue, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblTaggedTitle, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 7;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblTaggedValue, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 8;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblMostPopularTagTitle, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 8;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblMostPopularTagValue, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 8;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblMostPopularTagName, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 9;
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblMostTaggedBookmarkTitle, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 9;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = leftRightMargin;
		getContentPane().add(lblMostTaggedBookmarkValue, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 2;
		gbc.gridy = 9;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.insets = new Insets(topMargin.top, leftRightMargin.left,
				bottomMargin.bottom, leftRightMargin.right);
		getContentPane().add(linkMostTaggedBookmark, gbc);
	}

	private void initData() {
		countB = 0;
		countT = 0;
		countUntagged = 0;
		countTagged = 0;
		countOrph = 0;
		countLinks = 0;
		countEmails = 0;
		countMostPopularTag = 0;
		countMostTaggedBookmark = 0;
		mostPopularTag = TBError.getMessage("stats.noTag");
		mostTaggedBookmark = null;
	}

	public void setCountBookmark(long count) {
		this.countB = count;
	}

	public void setCountTag(long count) {
		this.countT = count;
	}

	public void setCountUntagged(long count) {
		this.countUntagged = count;
	}

	public void setCountTagged(long count) {
		this.countTagged = count;
	}

	public void setCountOrph(long count) {
		this.countOrph = count;
	}

	public void setCountLink(long count) {
		this.countLinks = count;
	}

	public void setCountEmail(long count) {
		this.countEmails = count;
	}

	public void setCountMostPopularTag(int count) {
		this.countMostPopularTag = count;
	}

	public void setMostPopularTagName(String name) {
		this.mostPopularTag = name == null ? TBError.getMessage("stats.noTag")
				: name;
	}

	public void setCountMostTagged(int count) {
		this.countMostTaggedBookmark = count;
	}

	public void setMostTaggedBookmark(Bookmark bookmark) {
		this.mostTaggedBookmark = bookmark;
		linkMostTaggedBookmark.setText(bookmark == null ? TBError
				.getMessage("stats.noBookmark") : bookmark.getName());
	}

	public void updateText() {
		// set text
		lblBCountValue.setText("" + countB);
		lblTCountValue.setText("" + countT);
		lblOrphValue.setText("" + countOrph);
		lblLinkValue.setText("" + countLinks);
		lblEmailValue.setText("" + countEmails);
		lblUntaggedValue.setText("" + countUntagged);
		lblTaggedValue.setText("" + countTagged);
		lblMostPopularTagValue.setText("" + countMostPopularTag);
		lblMostPopularTagName.setText(mostPopularTag);
		lblMostTaggedBookmarkValue.setText("" + countMostTaggedBookmark);
		pack();
		setLocationRelativeTo(this.getParent());
	}

}

class CustomDialog {

	private final JDialog dialog;
	private final JTextArea textArea;

	public CustomDialog(JDialog parent) {
		dialog = new JDialog(parent);
		dialog.setIconImage(null);
		textArea = new JTextArea();
		textArea.setEditable(false);
		// textArea.setBackground(dialog.getBackground());
		textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		textArea.setLineWrap(true);
		textArea.setColumns(50);
		textArea.setTabSize(10);
		dialog.setLayout(new BorderLayout());
		dialog.add(textArea, BorderLayout.CENTER);
		dialog.getRootPane().registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		}, KeyStroke.getKeyStroke("ESCAPE"),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	public void setTitle(String title) {
		dialog.setTitle(title);
	}

	public void setText(String text) {
		textArea.setText(text);
		dialog.pack();
	}

	public void setLocationRelativeTo(Component c) {
		dialog.setLocationRelativeTo(c);
	}

	public void setVisible(boolean visible) {
		dialog.setVisible(visible);
	}

	public void dispose() {
		dialog.dispose();
	}
}
