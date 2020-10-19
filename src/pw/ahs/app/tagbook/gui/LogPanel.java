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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import pw.ahs.app.tagbook.core.utils.TBIOUtils;
import pw.ahs.app.tagbook.core.utils.TBLog;
import pw.ahs.app.tagbook.core.utils.TBText;
import pw.ahs.app.tagbook.gui.utils.TBGuiUtils;
import pw.ahs.app.tagbook.gui.utils.TBIcon;
import pw.ahs.app.tagbook.gui.utils.TBKeyStroke;
import pw.ahs.app.tagbook.gui.utils.TBMnemonic;

@SuppressWarnings("serial")
public class LogPanel extends JFrame implements TBLog {
	// gui components
	private JTextPane textPane;
	private Action saveAction;
	// data variables
	private SimpleAttributeSet timestampAttributes;
	private SimpleAttributeSet titleAttributes;
	private SimpleAttributeSet messageAttributes;
	private SimpleAttributeSet iconInfoAttribute;
	private SimpleAttributeSet iconErrorAttribute;
	private StyledDocument document;
	private static final int ERROR = 0;
	private static final int INFO = 1;

	public LogPanel() {
		initGui();
		initData();
	}

	private void initGui() {
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setTitle(TBText.get("logPanel.t"));
		setIconImage(TBIcon.get("log").getImage());
		getContentPane().setLayout(new GridBagLayout());

		initTextPane();
		initMenus();

		pack();

		setLocationRelativeTo(null);
	}

	private void initTextPane() {
		textPane = new JTextPane() {

			@Override
			public boolean getScrollableTracksViewportWidth() {
				return getUI().getPreferredSize(this).width <= getParent()
						.getSize().width;
			}
		};
		textPane.setEditable(false);
		textPane.setAutoscrolls(true);

		JScrollPane textPaneScroll = new JScrollPane(textPane);
		textPaneScroll.setPreferredSize(new Dimension(350, 150));

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(textPaneScroll, gridBagConstraints);

	}

	private void initMenus() {
		// create actions
		Action clearAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					document.remove(0, document.getLength());
				} catch (BadLocationException ignore) {
				}
			}
		};
		TBGuiUtils.initializeAction(clearAction, "Clear", "Clear",
				TBMnemonic.get("clear"), TBIcon.get("clear"),
				TBKeyStroke.get("clear"));

		Action copyAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				textPane.selectAll();
				textPane.copy();
				textPane.select(0, 0);
			}
		};
		TBGuiUtils.initializeAction(copyAction, "Copy", "Copy",
				TBMnemonic.get("copy"), TBIcon.get("copy"),
				TBKeyStroke.get("copy"));

		saveAction = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Choose file");
				FileFilter fileFilter = TBGuiUtils.txtFileFilter;
				fileChooser.setFileFilter(fileFilter);
				if (fileChooser.showSaveDialog(LogPanel.this) == JFileChooser.APPROVE_OPTION) {
					TBIOUtils.writeTextFile(textPane.getText(),
							fileChooser.getSelectedFile());
				}
			}
		};
		TBGuiUtils.initializeAction(saveAction, "Save", "Save",
				TBMnemonic.get("save"), TBIcon.get("saveas"),
				TBKeyStroke.get("save"));

		Action appendMessage = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object type = JOptionPane.showInputDialog(LogPanel.this,
						TBText.get("saveLog.chooseType"),
						TBText.get("saveLog.t"),
						JOptionPane.INFORMATION_MESSAGE, null, new String[] {
								"Error Message", "Info Message" },
						"Info Message");
				int itype = type.equals("Error Message") ? ERROR
						: INFO;
				String title = JOptionPane.showInputDialog(LogPanel.this,
						TBText.get("saveLog.chooseTitle"),
						TBText.get("saveLog.t"),
						JOptionPane.INFORMATION_MESSAGE);
				String text = JOptionPane.showInputDialog(LogPanel.this,
						TBText.get("saveLog.chooseText"),
						TBText.get("saveLog.t"),
						JOptionPane.INFORMATION_MESSAGE);
				if (!title.isEmpty() && !text.isEmpty()) {
					append(text, title, itype);
				}
			}
		};
		TBGuiUtils.initializeAction(appendMessage, "Append Message",
				"Manually Append Message", KeyEvent.VK_I, null, null);

		Action closeActoin = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		};
		TBGuiUtils.initializeAction(closeActoin, "Close", "Close",
				TBMnemonic.get("close"), TBIcon.get("close"),
				TBKeyStroke.get("cancel"));

		// create menubar
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu();
		menuFile.setText("File");
		menuFile.setMnemonic('f');
		menuFile.setIcon(TBIcon.get("file"));
		menuFile.add(copyAction);
		menuFile.add(clearAction);
		menuFile.add(saveAction);
		menuFile.addSeparator();
		menuFile.add(appendMessage);
		menuFile.addSeparator();
		menuFile.add(closeActoin);
		menuBar.add(menuFile);
		setJMenuBar(menuBar);

		// create popup menu
		JPopupMenu popupmenu = new JPopupMenu();
		popupmenu.add(copyAction);
		popupmenu.add(clearAction);
		popupmenu.add(saveAction);
		textPane.setComponentPopupMenu(popupmenu);
	}

	private void initData() {
		document = new DefaultStyledDocument();
		textPane.setDocument(document);

		timestampAttributes = new SimpleAttributeSet();
		titleAttributes = new SimpleAttributeSet();
		messageAttributes = new SimpleAttributeSet();
		iconInfoAttribute = new SimpleAttributeSet();
		iconErrorAttribute = new SimpleAttributeSet();

		StyleConstants.setFontSize(timestampAttributes, 14);
		StyleConstants.setItalic(timestampAttributes, true);

		StyleConstants.setFontSize(titleAttributes, 14);
		StyleConstants.setBold(titleAttributes, true);

		StyleConstants.setFontSize(messageAttributes, 12);

		StyleConstants.setIcon(iconErrorAttribute, TBIcon.get("error"));
		StyleConstants.setIcon(iconInfoAttribute, TBIcon.get("info"));
	}

	private void append(String message, String title, int messageType) {
		try {
			switch (messageType) {
			case ERROR:
				document.insertString(document.getLength(), "IconError",
						iconErrorAttribute);
				break;
			case INFO:
				document.insertString(document.getLength(), "IconInfo",
						iconInfoAttribute);
				break;
			}

			document.insertString(document.getLength(), " " + title + " | ",
					titleAttributes);
			document.insertString(document.getLength(),
					TBGuiUtils.formatDate(System.currentTimeMillis()) + "\n",
					timestampAttributes);
			document.insertString(document.getLength(), message + "\n\n",
					messageAttributes);
		} catch (BadLocationException ignored) {
		}
	}

	public void error(String message, String title) {
		append(message, title, ERROR);
	}

	public void error(String message) {
		error(message, "Error");
	}

	public void info(String message, String title) {
		append(message, title, INFO);
	}

	public void info(String message) {
		info(message, "Info");
	}

	public boolean hasLogged() {
		return !textPane.getText().isEmpty();
	}

	public void invokeSaveAction() {
		saveAction.actionPerformed(null);
	}

}
