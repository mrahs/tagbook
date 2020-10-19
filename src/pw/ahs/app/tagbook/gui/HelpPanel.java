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
import java.awt.event.ActionListener;
import java.io.InputStream;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import pw.ahs.app.tagbook.core.utils.TBIOUtils;
import pw.ahs.app.tagbook.core.utils.TBText;
import pw.ahs.app.tagbook.gui.utils.TBIcon;
import pw.ahs.app.tagbook.gui.utils.TBKeyStroke;

@SuppressWarnings("serial")
public class HelpPanel extends JFrame {
	private JTextPane textPane;
	private JTree tree;

    public HelpPanel() {
		initGui();
	}

	private void initGui() {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle(TBText.get("helpPanel.t"));
		setIconImage(TBIcon.get("help").getImage());
		getContentPane().setLayout(new GridBagLayout());

		initTree();

		textPane = new JTextPane(){

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width <= getParent()
                        .getSize().width;
            }
        };
		textPane.setEditable(false);

		JScrollPane treeScroll = new JScrollPane(tree);
		treeScroll.setPreferredSize(new Dimension(120, 200));

		JScrollPane textPaneScroll = new JScrollPane(textPane);
		textPaneScroll.setPreferredSize(new Dimension(400, 300));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setLeftComponent(treeScroll);
		splitPane.setRightComponent(textPaneScroll);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(splitPane, gridBagConstraints);

		pack();

		setLocationRelativeTo(null);

		getRootPane().registerKeyboardAction(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		}, TBKeyStroke.get("cancel"),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	private void initTree() {
		String path = "/res/help/";
		InputStream in;

		final int nLeaf = 6;
		HelpTreeObject[] leafs = new HelpTreeObject[nLeaf];
		String[] leafName = new String[] { "What?", "Why?", "How?", "Examples",
				"Features", "Shortcuts" };
		String[] contentFile = new String[] { "what", "why", "how", "examples",
				"features", "shortcuts" };
		final DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[nLeaf + 1];
		nodes[0] = new DefaultMutableTreeNode(new HelpTreeObject("Help",
				"Help section is not complete yet!"));

		for (int i = 0; i < nLeaf; i++) {
			leafs[i] = new HelpTreeObject();
			leafs[i].setTitle(leafName[i]);

			in = getClass().getResourceAsStream(path + contentFile[i]);
			if (in != null)
				leafs[i].setContent(TBIOUtils.readTextFromStream(in));
			else
				leafs[i].setContent("help file not found!");

			nodes[0].add(new DefaultMutableTreeNode(leafs[i]));
		}

        DefaultTreeModel treeModel = new DefaultTreeModel(nodes[0]);
		tree = new JTree(treeModel);
		tree.getSelectionModel().addTreeSelectionListener(
				new TreeSelectionListener() {

					@Override
					public void valueChanged(TreeSelectionEvent e) {
						textPane.setText(((HelpTreeObject) ((DefaultMutableTreeNode) e
								.getPath().getLastPathComponent())
								.getUserObject()).getContent().toString());
					}
				});
	}

	public static void main(String[] args) {
		new HelpPanel().setVisible(true);
	}
}

class HelpTreeObject {

	private String title;
	private Object content;

	public HelpTreeObject() {
		this(null, null);
	}

	public HelpTreeObject(String title, Object content) {
		setTitle(title);
		setContent(content);
	}

	public String getTitle() {
		return title;
	}

	public final void setTitle(String title) {
		this.title = title == null ? "" : title;
	}

	public Object getContent() {
		return content;
	}

	public final void setContent(Object content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return title;
	}
}
