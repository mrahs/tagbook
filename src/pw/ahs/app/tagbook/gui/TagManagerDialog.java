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

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXSearchField;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.ColumnControlButton;
import pw.ahs.app.tagbook.core.DataAccessObject;
import pw.ahs.app.tagbook.core.Tag;
import pw.ahs.app.tagbook.core.TagI;
import pw.ahs.app.tagbook.core.utils.TBConsts;
import pw.ahs.app.tagbook.core.utils.TBText;
import pw.ahs.app.tagbook.gui.utils.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
class TagManagerDialog extends JDialog {
    // gui components
    private org.jdesktop.swingx.JXSearchField txtFilter;
    private DefaultTableModel tableModel;
    private JXTable tableData;
    private JXBusyLabel busyLabel;
    private JLabel statusLabel;
    private JLabel countLabel;
    // actions
    private Action renameAction;
    private Action replaceAction;
    private Action deleteAction;
    private Action deepDeleteAction;
    private Action deleteOrphanedAction;
    private Action closeAction;
    // data variables
    private DataAccessObject dao;
    private DefaultTableModelController table;
    private boolean readOnly;
    private boolean readOnlyPreState; // used by setBusy

    public TagManagerDialog(Frame parent, boolean modal, DataAccessObject dao) {
        super(parent, modal);
        if (dao == null) {
            throw new IllegalArgumentException(
                    "Data Access Object cannot be null");
        }
        initGui();

        // initialize data
        this.dao = dao;
        this.readOnly = true;
        this.table = new DefaultTableModelController(tableModel);
        this.table.getRowCounter().addPropertyChangeListener(
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        countLabel.setText(String.valueOf(evt
                                .getNewValue()));
                    }
                });
        dao.getTagsWithCounts(table);

        // center panel
        setLocationRelativeTo(parent);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        txtFilter.getFindAction().actionPerformed(new ActionEvent(txtFilter, ActionEvent.ACTION_PERFORMED, ""));
    }

    private void initGui() {
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setTitle(TBText.get("tagManager.t"));
        setIconImage(TBIcon.get("tagManager").getImage());
        getContentPane().setLayout(new GridBagLayout());

        getRootPane().registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                txtFilter.requestFocusInWindow();
            }
        }, TBKeyStroke.get("searchFocus"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        initActions();
        initTable();
        initSearchField();
        initMenus();

        initStatusBar();
        pack();

        setPerEntryActionsEnabled(false);
    }

    private void initActions() {
        renameAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Long id = getSelectedTagId();
                String name = getSelectedTagName();
                Object newName = JOptionPane.showInputDialog(
                        TagManagerDialog.this, "New Name:", "Rename Tag",
                        JOptionPane.INFORMATION_MESSAGE, null, null, name);
                if (newName != null) {
                    if (!newName.toString().isEmpty()) {
                        dao.updateTag(id, newName.toString());
                        tableModel.setValueAt(newName.toString(), tableData
                                .convertRowIndexToModel(tableData
                                        .getSelectedRow()), 1);
                        statusLabel.setText(TBText.get("op.tag.rename"));
                    }
                }
            }
        };
        TBGuiUtils.initializeAction(renameAction, "Rename", "Rename",
                TBMnemonic.get("rename"), TBIcon.get("rename"),
                TBKeyStroke.get("rename"));

        replaceAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setBusy(true, "replacing tag..");
                TBWorker worker = new TBWorker() {

                    @Override
                    protected void doneWithSuccess() {
                        statusLabel.setText(TBText.get("op.tag.replace"));
                    }

                    @Override
                    protected void doneWithError() {

                    }

                    @Override
                    protected void doneWithAbort() {

                    }

                    @Override
                    protected void alwaysDoWhenDone() {
                        setBusy(false, "");
                    }

                    @Override
                    protected int doThis() {
                        String selectedTagName = getSelectedTagName();
                        List<TagI> tags = new ArrayList<>(dao.getTags(null));
                        int toRemove = -1;
                        for (int i = 0; i < tags.size(); ++i) {
                            if (tags.get(i).getName().equals(selectedTagName)) {
                                toRemove = i;
                                break;
                            }
                        }
                        if (toRemove != -1) {
                            tags.remove(toRemove);
                        }
                        Collections.sort(tags);
                        Object tag = JOptionPane.showInputDialog(
                                TagManagerDialog.this,
                                "Choose tag to replace '" + selectedTagName
                                        + "':", "Replace Tag",
                                JOptionPane.INFORMATION_MESSAGE, null,
                                tags.toArray(), null);
                        if (tag instanceof Tag) {
                            dao.replaceTag(getSelectedTagId(),
                                    ((TagI) tag).getId());
                            table.clearRows();
                            dao.getTags(table);
                            return TBConsts.DAO_SUCCESS;
                        }
                        return TBConsts.DAO_ABORT;
                    }
                };
                worker.execute();
            }
        };
        TBGuiUtils.initializeAction(replaceAction, "Replace", "Repalce",
                TBMnemonic.get("replace"), TBIcon.get("tag.replace"),
                TBKeyStroke.get("replace"));

        deleteAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(TagManagerDialog.this,
                        "Delete tag '" + getSelectedTagName() + "'?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    dao.deleteTag(getSelectedTagId());
                    tableModel
                            .removeRow(tableData
                                    .convertRowIndexToModel(tableData
                                            .getSelectedRow()));
                    statusLabel.setText(TBText.get("op.tag.del"));
                }
            }
        };
        TBGuiUtils.initializeAction(deleteAction, "Delete", "Delete",
                TBMnemonic.get("delete"), TBIcon.get("delete"),
                TBKeyStroke.get("delete"));

        deepDeleteAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(TagManagerDialog.this,
                        "DEEP Delete tag '" + getSelectedTagName() + "'?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    dao.deepDeleteTag(getSelectedTagId());
                    tableModel
                            .removeRow(tableData
                                    .convertRowIndexToModel(tableData
                                            .getSelectedRow()));
                    statusLabel.setText(TBText.get("op.tag.deepDelete"));
                }
            }
        };
        TBGuiUtils.initializeAction(deepDeleteAction, "Deep Delete",
                "Delete Along With Tagged Items",
                TBMnemonic.get("tag.deepDelete"), TBIcon.get("tag.deepDelete"),
                TBKeyStroke.get("tag.deepDelete"));

        deleteOrphanedAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setBusy(true, "Deleting..");
                TBWorker worker = new TBWorker() {

                    @Override
                    protected void doneWithSuccess() {
                        statusLabel.setText(TBText.get("op.tag.delOrph"));
                    }

                    @Override
                    protected void doneWithError() {
                    }

                    @Override
                    protected void doneWithAbort() {
                        statusLabel.setText("aborted");
                    }

                    @Override
                    protected void alwaysDoWhenDone() {
                        setBusy(false, "");
                    }

                    @Override
                    protected int doThis() {
                        long count = dao.getOrphanedTagsCount();
                        if (JOptionPane.showConfirmDialog(
                                TagManagerDialog.this, "Delete " + count
                                + " orphaned tags?", "Confirm Delete",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                            dao.deleteOrphanedTags();
                            table.clearRows();
                            dao.getTags(table);
                            return TBConsts.DAO_SUCCESS;
                        }
                        return TBConsts.DAO_ABORT;
                    }
                };
                worker.execute();
            }
        };
        TBGuiUtils.initializeAction(deleteOrphanedAction,
                "Delete Orphaned Tags", "Delete Tags With No Bookmarks",
                TBMnemonic.get("tag.deleteOrphaned"),
                TBIcon.get("tag.deleteOrphaned"),
                TBKeyStroke.get("tag.deleteOrphaned"));

        closeAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        };
        TBGuiUtils.initializeAction(closeAction, "Close", "Close",
                TBMnemonic.get("close"), TBIcon.get("close"),
                TBKeyStroke.get("close"));
    }

    private void initTable() {
        // overriding
        tableModel = new DefaultTableModel(new Object[][]{}, new String[]{
                "ID", "Name", "Hits"}) {

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                    case 2:
                        return Long.class;
                    case 1:
                    default:
                        return String.class;
                }
            }
        };

        tableData = new org.jdesktop.swingx.JXTable(tableModel) {

            @Override
            public Component prepareRenderer(TableCellRenderer renderer,
                                             int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JComponent) {
                    Object value = getValueAt(row, column);
                    if (value != null) {
                        ((JComponent) c).setToolTipText(value.toString());
                    }
                }
                return c;
            }
        };

        // control properties
        tableData.setColumnControl(new ColumnControlButton(tableData) {

            @Override
            protected List<Action> getAdditionalActions() {
                List<Action> actions = super.getAdditionalActions();
                actions.add(new AbstractAction("Reset Sort Order") {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tableData.resetSortOrder();
                    }
                });
                return actions;
            }
        });

        tableData.setEditable(false);
        tableData.setColumnControlVisible(true);
        tableData.setAutoCreateRowSorter(false);
        tableData.getColumnExt("ID").setVisible(false);
        tableData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // adjust keystrokes
        InputMap tableim = tableData
                .getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        tableim.put(KeyStroke.getKeyStroke("F2"), "none");
        tableim.put(KeyStroke.getKeyStroke("alt F"),
                tableim.get(KeyStroke.getKeyStroke("ctrl F")));
        tableim.put(KeyStroke.getKeyStroke("ctrl F"), "none");

        // visual properties
        tableData.setColumnMargin(5);
        tableData.setRowMargin(3);
        tableData.setShowGrid(false);
        tableData.setRolloverEnabled(true);
        tableData.addHighlighter(HighlighterFactory.createAlternateStriping());
        tableData
                .addHighlighter(new ColorHighlighter(
                        HighlightPredicate.ROLLOVER_ROW, new Color(0, 200, 0,
                        80), null));

        tableData.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("F2"), "");

        // actions
        tableData.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !tableData.getSelectionModel().isSelectionEmpty()) {
                    renameAction.actionPerformed(null);
                }
            }
        });

        tableData.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        if (!e.getValueIsAdjusting()) {
                            int index = tableData.getSelectedRow();
                            if (index == -1) {
                                setPerEntryActionsEnabled(false);
                            } else {
                                setPerEntryActionsEnabled(true);
                            }
                        }
                    }
                });

        // DnD
        tableData.setDragEnabled(true);
        tableData.setTransferHandler(new TransferHandler(null) {

            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }

            @Override
            protected Transferable createTransferable(JComponent c) {
                Object value = tableData.getValueAt(tableData.getSelectedRow(),
                        tableData.convertColumnIndexToView(1));
                if (value == null) {
                    return null;
                }
                return new StringSelection(value.toString());
            }

            @Override
            public Image getDragImage() {
                return TBGuiUtils.createImageFromText(tableData.getValueAt(
                        tableData.getSelectedRow(),
                        tableData.getSelectedColumn()).toString());
            }

            @Override
            public Point getDragImageOffset() {
                return new Point(0, 10);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JScrollPane tableScrollPane = new JScrollPane(tableData);
        tableScrollPane.setPreferredSize(new Dimension(300, 200));
        getContentPane().add(tableScrollPane, gbc);
    }

    private void initSearchField() {
        txtFilter = new org.jdesktop.swingx.JXSearchField();
        txtFilter.setToolTipText("Filter tags");
        txtFilter.setPrompt("Filter");
        txtFilter.setSearchMode(JXSearchField.SearchMode.INSTANT);
        txtFilter.setUseNativeSearchFieldIfPossible(true);
        txtFilter.setFindAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String text = e.getActionCommand();
                dao.abort();
                TBWorker worker = new TBWorker() {

                    @Override
                    protected void doneWithSuccess() {

                    }

                    @Override
                    protected void doneWithError() {

                    }

                    @Override
                    protected void doneWithAbort() {

                    }

                    @Override
                    protected int doThis() {
                        dao.searchTags(text, table);
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        else
                            return TBConsts.DAO_SUCCESS;
                    }
                };
                worker.execute();
            }
        });

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(txtFilter, gridBagConstraints);
    }

    private void initMenus() {
        JMenu menu = new JMenu("Tag");
        menu.setMnemonic('t');
        menu.setIcon(TBIcon.get("tagManager"));
        menu.add(renameAction);
        menu.add(replaceAction);
        menu.addSeparator();
        menu.add(deleteAction);
        menu.add(deepDeleteAction);
        menu.add(deleteOrphanedAction);
        menu.addSeparator();
        menu.add(closeAction);

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(renameAction);
        popupMenu.add(replaceAction);
        popupMenu.addSeparator();
        popupMenu.add(deleteAction);
        popupMenu.add(deepDeleteAction);
        popupMenu.add(deleteOrphanedAction);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        setJMenuBar(menuBar);

        tableData.setComponentPopupMenu(popupMenu);
    }

    private void initStatusBar() {
        JXStatusBar statusBar = new JXStatusBar();
        statusBar.setResizeHandleEnabled(false);

        busyLabel = new JXBusyLabel(new Dimension(10, 10));
        busyLabel.setToolTipText("Busy Indicator");
        statusLabel = new JLabel("Ready!");
        statusLabel.setToolTipText("Status Message");
        countLabel = new JLabel("0");
        countLabel.setToolTipText("Number Of Rows");

        statusBar.add(busyLabel,
                new JXStatusBar.Constraint(busyLabel.getWidth()));
        statusBar.add(statusLabel, new JXStatusBar.Constraint(
                JXStatusBar.Constraint.ResizeBehavior.FILL));
        statusBar.add(countLabel, new JXStatusBar.Constraint(20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(statusBar, gbc);
    }

    public void setReadOnly(boolean state) {
        readOnly = state;
        tableData.getSelectionModel().clearSelection(); //
    }

    private Long getSelectedTagId() {
        return (Long) tableModel
                .getValueAt(tableData.convertRowIndexToModel(tableData
                        .getSelectedRow()), 0);
    }

    private String getSelectedTagName() {
        return tableModel
                .getValueAt(
                        tableData.convertRowIndexToModel(tableData
                                .getSelectedRow()), 1).toString();
    }

    private void setPerEntryActionsEnabled(boolean state) {
        state = !readOnly && state;
        renameAction.setEnabled(state);
        replaceAction.setEnabled(state);
        deleteAction.setEnabled(state);
        deepDeleteAction.setEnabled(state);
    }

    private void setBusy(boolean state, String message) {
        txtFilter.setEditable(!state);
        if (state) {
            readOnlyPreState = readOnly;
            setReadOnly(true);
        } else {
            setReadOnly(readOnlyPreState);
        }
        busyLabel.setBusy(state);
        statusLabel.setText(message);
    }
}
