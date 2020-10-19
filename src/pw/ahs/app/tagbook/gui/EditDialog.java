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

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;
import pw.ahs.app.tagbook.core.*;
import pw.ahs.app.tagbook.core.utils.TBConsts;
import pw.ahs.app.tagbook.core.utils.TBError;
import pw.ahs.app.tagbook.core.utils.TBText;
import pw.ahs.app.tagbook.gui.utils.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Set;

@SuppressWarnings({"serial", "rawtypes", "unchecked"})
class EditDialog extends JDialog {
    // gui components
    private Color txtFieldDefaultBackground;
    private JTextField txtName;
    private JTextField txtDescription;
    private JTextField txtAddress;
    private JTextArea txtNotes;
    private JXComboBox comboTag;
    private JXList lstTags;
    private JList<String> lstTagsSuggested;
    private DefaultListModel<String> lstModelTags;
    private DefaultListModel<String> lstModelSuggestedTags;
    private JTextField txtDateadded;
    private JTextField txtDatemodified;
    private JPopupMenu popupmenu;
    private Action saveAction;
    private Action cancelAction;
    private UndoManager[] undoManagers;
    // data variables
    private DataAccessObject dao;
    private int methodType;
    private BookmarkI bookmark;
    private boolean accept;

    public EditDialog(Frame parent, boolean modal, DataAccessObject dao,
                      int methodType) {
        super(parent, modal);
        if (dao == null) {
            throw new IllegalArgumentException(
                    "Data Access Object cannot be null");
        }
        initGui();

        // initialize data
        this.dao = dao;
        this.bookmark = null;
        this.accept = false;
        setMethodType(methodType);

        Set<TagI> tags = dao.getTags(null);
        if (tags != null) {
            Object[] ts = tags.toArray();
            Arrays.sort(ts);
            comboTag.setModel(new DefaultComboBoxModel(ts));
        }

        // center dialog
        setLocationRelativeTo(parent);
    }

    private void initGui() {
        // initialize dialog
        getContentPane().setLayout(new GridBagLayout());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(TBText.get("edit.title"));
        setIconImage(TBIcon.get("bookmark.edit").getImage());

        // initialize things
        initPopupMenu();
        initFields();
        initButtons();

        // pack things
        pack();

        // initialize connections
        initKeyboardActioins();
        initUndoManagers();
        initListsModels();
    }

    private void initPopupMenu() {
        // allocate
        popupmenu = new JPopupMenu();

        // initialize menu items and actions
        Action copyAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ((JTextComponent) popupmenu.getInvoker()).copy();
            }
        };
        TBGuiUtils.initializeAction(copyAction, "Copy", "Copy",
                TBMnemonic.get("copy"), TBIcon.get("copy"), null);

        Action pasteAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ((JTextComponent) popupmenu.getInvoker()).paste();
            }
        };
        TBGuiUtils.initializeAction(pasteAction, "Paste", "Paste",
                TBMnemonic.get("paste"), TBIcon.get("paste"), null);

        Action cutAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ((JTextComponent) popupmenu.getInvoker()).cut();
            }
        };
        TBGuiUtils.initializeAction(cutAction, "Cut", "Cut",
                TBMnemonic.get("cut"), TBIcon.get("cut"), null);

        Action clearAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ((JTextComponent) popupmenu.getInvoker()).setText(null);
            }
        };
        TBGuiUtils.initializeAction(clearAction, "Clear", "Clear",
                TBMnemonic.get("clear"), TBIcon.get("delete"), null);

        Action undoAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                undoRedoActionPerformed(popupmenu.getInvoker(), 0);
            }
        };
        TBGuiUtils.initializeAction(undoAction, "Undo", "Undo",
                TBMnemonic.get("undo"), TBIcon.get("undo"), null);

        Action redoAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                undoRedoActionPerformed(popupmenu.getInvoker(), 1);
            }
        };
        TBGuiUtils.initializeAction(redoAction, "Redo", "Redo",
                TBMnemonic.get("redo"), TBIcon.get("redo"), null);

        // add items to menu
        popupmenu.add(copyAction);
        popupmenu.add(pasteAction);
        popupmenu.add(cutAction);
        popupmenu.add(clearAction);
        popupmenu.add(undoAction);
        popupmenu.add(redoAction);

        // initialize listener
        popupmenu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (e.getSource() instanceof JPopupMenu) {
                    JPopupMenu src = (JPopupMenu) e.getSource();
                    if (src.getInvoker() instanceof JTextComponent) {
                        if (!((JTextComponent) src.getInvoker()).isEditable()) {
                            popupmenu.getComponent(1).setEnabled(false);
                            popupmenu.getComponent(2).setEnabled(false);
                            popupmenu.getComponent(3).setEnabled(false);
                            popupmenu.getComponent(4).setEnabled(false);
                            popupmenu.getComponent(5).setEnabled(false);
                        }
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
    }

    private void initFields() {
        // allocate
        txtName = new JTextField();
        txtDescription = new JTextField();
        txtAddress = new JTextField();
        txtNotes = new JTextArea();
        txtDateadded = new JTextField();
        txtDatemodified = new JTextField();
        lstTags = new JXList();
        comboTag = new JXComboBox();
        lstTagsSuggested = new JList<>();
        JButton btnAddSTags = new JButton(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Enumeration<String> en = lstModelSuggestedTags.elements();
                while (en.hasMoreElements()) {
                    lstModelTags.addElement(en.nextElement());
                }
            }
        });
        btnAddSTags.setText("Add all");

        // initialize input fields
        DocumentListener validateInputCaller = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateInput();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validateInput();
            }
        };
        txtFieldDefaultBackground = txtName.getBackground();

        txtName.setToolTipText("enter name");
        txtName.setComponentPopupMenu(popupmenu);
        txtName.setDragEnabled(true);
        txtName.getDocument().addDocumentListener(validateInputCaller);

        txtDescription.setToolTipText("enter description");
        txtDescription.setComponentPopupMenu(popupmenu);
        txtDescription.setDragEnabled(true);

        txtAddress.setToolTipText("enter data");
        txtAddress.setComponentPopupMenu(popupmenu);
        txtAddress.setDragEnabled(true);
        txtAddress.getDocument().addDocumentListener(validateInputCaller);
        txtAddress.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent evt) {
                txtAddress.postActionEvent();
            }
        });
        txtAddress.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (txtAddress.isEditable()) {
                    Tag[] suggestedTags = Tag.suggestTags(txtAddress.getText());
                    if (suggestedTags == null) {
                        lstModelSuggestedTags.clear();
                    } else {
                        for (Tag tag : suggestedTags) {
                            lstModelSuggestedTags.addElement(tag.toString());
                        }
                    }
                }
            }
        });

        txtNotes.setFont(new Font(Font.DIALOG, 0, 11)); // NOI18N
        txtNotes.setRows(3);
        txtNotes.setToolTipText("enter notes");
        txtNotes.setComponentPopupMenu(popupmenu);
        txtNotes.setDragEnabled(true);
        JScrollPane scrlTxtNotes = new JScrollPane(txtNotes);

        lstTags.setToolTipText("Tags");
        lstTags.setAutoCreateRowSorter(true);
        lstTags.setDragEnabled(true);
        lstTags.setDropMode(DropMode.INSERT);
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

        lstTagsSuggested.setToolTipText("Sugessted Tags");
        lstTagsSuggested.setDragEnabled(true);
        lstTagsSuggested.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    String selected = lstTagsSuggested.getSelectedValue();
                    lstModelTags.addElement(selected);
                }
            }
        });
        JScrollPane scrlLstTagsSuggested = new JScrollPane(lstTagsSuggested);
        scrlLstTagsSuggested.setPreferredSize(new Dimension(100, 80));

        comboTag.setEditable(true);
        comboTag.setToolTipText("enter tag");
        comboTag.setPreferredSize(new Dimension(100, 20));
        comboTag.getEditor().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String tag = e.getActionCommand();
                if (!tag.isEmpty()) {
                    if (!Tag.isValidTagName(tag)) {
                        JOptionPane.showMessageDialog(EditDialog.this,
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

        txtDateadded.setEditable(false);
        txtDateadded.setToolTipText("Date Added");

        txtDatemodified.setEditable(false);
        txtDatemodified.setToolTipText("Date Modified");

        // initialize labels
        JLabel lblName = new JLabel("Name 1");
        lblName.setDisplayedMnemonic('1');
        lblName.setLabelFor(txtName);

        JLabel lblDescription = new JLabel("Description 2");
        lblDescription.setDisplayedMnemonic('2');
        lblDescription.setLabelFor(txtDescription);

        JLabel lblAddress = new JLabel("Data 3");
        lblAddress.setDisplayedMnemonic('3');
        lblAddress.setLabelFor(txtAddress);
        lblAddress.setToolTipText("hit enter to suggest tags");

        JLabel lblNotes = new JLabel("Notes 4");
        lblNotes.setDisplayedMnemonic('4');
        lblNotes.setLabelFor(txtNotes);

        JLabel lblAddTags = new JLabel("Add Tags 5");
        lblAddTags.setDisplayedMnemonic('5');
        lblAddTags.setLabelFor(comboTag);

        JLabel lblTags = new JLabel("Tags 6");
        lblTags.setDisplayedMnemonic('6');
        lblTags.setLabelFor(lstTags);

        JLabel lblDateadded = new JLabel("Date Added");
        lblDateadded.setLabelFor(txtDateadded);

        JLabel lblDatemodified = new JLabel("Date Modified");
        lblDatemodified.setLabelFor(txtDatemodified);

        // initialize layout
        GridBagConstraints gbc;

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 5);
        getContentPane().add(lblName, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        getContentPane().add(txtName, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 5);
        getContentPane().add(lblDescription, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        getContentPane().add(txtDescription, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 5);
        getContentPane().add(lblAddress, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        getContentPane().add(txtAddress, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 5);
        getContentPane().add(lblNotes, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 1f;
        gbc.weighty = 1f;
        getContentPane().add(scrlTxtNotes, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 5);
        getContentPane().add(lblAddTags, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridheight = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.8f;
        gbc.weighty = 1f;
        getContentPane().add(scrlLstTags, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.weightx = 0.2f;
        gbc.weighty = 0f;
        getContentPane().add(comboTag, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 5);
        getContentPane().add(lblTags, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.2f;
        gbc.weighty = 1f;
        getContentPane().add(scrlLstTagsSuggested, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(btnAddSTags, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 5);
        getContentPane().add(lblDateadded, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        getContentPane().add(txtDateadded, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(0, 5, 0, 5);
        getContentPane().add(lblDatemodified, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.LINE_START;
        getContentPane().add(txtDatemodified, gbc);
    }

    private void initButtons() {
        saveAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // if (update and address has changed and it exists)
                // or if (insert and address exists)
                if ((methodType == TBConsts.EDIT_UPDATE
                        && !bookmark.getAddress().equals(txtAddress.getText()) && dao
                        .bookmarkExists(txtAddress.getText()))
                        || (methodType == TBConsts.EDIT_INSERT && dao
                        .bookmarkExists(txtAddress.getText()))) {
                    resolveConflict();
                } else {
                    prepareBookmark();
                    accept = true;
                    dispose();
                }
            }
        };
        TBGuiUtils.initializeAction(saveAction, "Save", "Save",
                TBMnemonic.get("save"), TBIcon.get("save"), null);

        cancelAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                accept = false;
                dispose();
            }
        };
        TBGuiUtils.initializeAction(cancelAction, "Cancel", "Cancel",
                TBMnemonic.get("cancel"), TBIcon.get("cancel"), null);

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelButtons.add(new JButton(saveAction));
        panelButtons.add(new JButton(cancelAction));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(panelButtons, gridBagConstraints);
    }

    private void initKeyboardActioins() {
        // paste text to name field
        getRootPane().registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                txtName.setText(null);
                txtName.paste();
            }
        }, TBKeyStroke.get("edit.PasteName"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // paste text to description field
        getRootPane().registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                txtDescription.setText(null);
                txtDescription.paste();
            }
        }, TBKeyStroke.get("edit.PasteDesc"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // paste text to address field
        getRootPane().registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                txtAddress.setText(null);
                txtAddress.paste();
            }
        }, TBKeyStroke.get("edit.PasteAddress"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // psate text to notes field
        getRootPane().registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                txtNotes.setText(null);
                txtNotes.paste();
            }
        }, TBKeyStroke.get("edit.PasteNotes"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // close window
        getRootPane().registerKeyboardAction(cancelAction,
                TBKeyStroke.get("cancel"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // save
        getRootPane().registerKeyboardAction(saveAction,
                TBKeyStroke.get("save"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initUndoManagers() {
        undoManagers = new UndoManager[4];
        for (int i = 0; i < 4; ++i) {
            undoManagers[i] = new UndoManager();
        }
        KeyAdapter undoKeyListener = new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent evt) {
                int todo = -1;
                if (KeyStroke
                        .getKeyStroke(evt.getKeyCode(), evt.getModifiers()) == TBKeyStroke
                        .get("undo")) {
                    todo = 0;
                } else if (KeyStroke.getKeyStroke(evt.getKeyCode(),
                        evt.getModifiers()) == TBKeyStroke.get("redo")) {
                    todo = 1;
                }
                undoRedoActionPerformed(evt.getSource(), todo);
            }
        };
        txtName.getDocument().addUndoableEditListener(undoManagers[0]);
        txtDescription.getDocument().addUndoableEditListener(undoManagers[1]);
        txtAddress.getDocument().addUndoableEditListener(undoManagers[2]);
        txtNotes.getDocument().addUndoableEditListener(undoManagers[3]);

        txtName.addKeyListener(undoKeyListener);
        txtDescription.addKeyListener(undoKeyListener);
        txtAddress.addKeyListener(undoKeyListener);
        txtNotes.addKeyListener(undoKeyListener);
    }

    private void initListsModels() {
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

        lstModelSuggestedTags = new DefaultListModel<String>() {
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
        lstTagsSuggested.setModel(lstModelSuggestedTags);
        lstTags.setTransferHandler(new TransferHandler(null) {

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    String text = (String) support.getTransferable()
                            .getTransferData(DataFlavor.stringFlavor);
                    String[] tags = text.split("\\s");
                    for (String tag : tags) {
                        lstModelTags.addElement(tag);
                    }
                    return true;
                } catch (UnsupportedFlavorException | IOException ex) {
                    return false;
                }
            }
        });
    }

    public int getMethodType() {
        return methodType;
    }

    public BookmarkI getBookmark() {
        if (accept) {
            return bookmark;
        }
        return null;
    }

    final void setMethodType(int methodType) {
        if (methodType == TBConsts.EDIT_INSERT
                || methodType == TBConsts.EDIT_UPDATE) {
            this.methodType = methodType;
        } else {
            this.methodType = TBConsts.EDIT_INSERT;
        }
    }

    public void setBookmark(BookmarkI bookmark) {
        this.bookmark = bookmark;
    }

    public void setEditable(boolean state) {
        txtName.setEditable(state);
        txtDescription.setEditable(state);
        txtAddress.setEditable(state);
        txtNotes.setEditable(state);
        comboTag.setEditable(state);
        saveAction.setEnabled(state);
    }

    public void setNameText(String text) {
        txtName.setText(text);
        validateInput();
    }

    public void setDescriptionText(String text) {
        txtDescription.setText(text);
    }

    public void setAddressText(String text) {
        txtAddress.setText(text);
    }

    public void setNotesText(String text) {
        txtNotes.setText(text);
    }

    public Bookmark showInsertDialog() {
        methodType = TBConsts.EDIT_INSERT;
        setVisible(true);
        return getBookmark();
    }

    public Bookmark showUpdateDialog() {
        methodType = TBConsts.EDIT_UPDATE;
        setVisible(true);
        return getBookmark();
    }

    @Override
    public void setVisible(boolean b) {
        if (b && configureToMethodType()) {
            super.setVisible(true);
        } else {
            super.setVisible(false);
        }
    }

    private String[] getTags() {
        if (lstModelTags.getSize() == 0) {
            return null;
        }

        String[] tags = new String[lstModelTags.size()];
        lstModelTags.copyInto(tags);
        return tags;
    }

    private void undoRedoActionPerformed(Object source, int whattodo) {
        int indx = 0; // undoManager index
        if (source instanceof JTextComponent) {
            if (source == txtName) {
                indx = 0;
            } else if (source == txtDescription) {
                indx = 1;
            } else if (source == txtAddress) {
                indx = 2;
            } else if (source == txtNotes) {
                indx = 3;
            }

            if (whattodo == 0) {
                if (undoManagers[indx].canUndo()) {
                    undoManagers[indx].undo();
                }
            } else if (whattodo == 1) {
                if (undoManagers[indx].canRedo()) {
                    undoManagers[indx].redo();
                }
            }
        }
    }

    private boolean configureToMethodType() {
        switch (methodType) {
            case TBConsts.EDIT_INSERT:
                bookmark = new BookmarkI(BookmarkI.NULL_ID, "dummy", "dummy");
                txtDateadded
                        .setText(TBGuiUtils.formatDate(bookmark.getDateadded()));
                txtDatemodified.setText(txtDateadded.getText());
                break;
            case TBConsts.EDIT_UPDATE:
                if (bookmark == null) {
                    return false;
                }
                bookmark.setDatemodified(new Timestamp(System.currentTimeMillis()));
                txtName.setText(bookmark.getName());
                txtDescription.setText(bookmark.getDesc());
                txtAddress.setText(bookmark.getAddress());
                txtNotes.setText(bookmark.getNotes());
                String ts = bookmark.getTagsString(" ");
                if (ts != null) {
                    String[] tags = ts.split("\\s");
                    for (String tag : tags) {
                        lstModelTags.addElement(tag);
                    }
                }
                txtDateadded
                        .setText(TBGuiUtils.formatDate(bookmark.getDateadded()));
                txtDatemodified.setText(TBGuiUtils.formatDate(bookmark
                        .getDatemodified()));
                break;
            default:
                return false;
        }
        validateInput();
        return true;
    }

    private void validateInput() {
        if (txtName.isEditable()) {
            boolean nameState = !txtName.getText().trim().isEmpty();
            boolean addressState = !txtAddress.getText().trim().isEmpty();

            if (nameState) {
                txtName.setBackground(txtFieldDefaultBackground);
            } else {
                txtName.setBackground(Color.RED);
            }
            if (addressState) {
                txtAddress.setBackground(txtFieldDefaultBackground);
            } else {
                txtAddress.setBackground(Color.RED);
            }

            saveAction.setEnabled(nameState && addressState);
        }
    }

    private void resolveConflict() {
        final BookmarkI oldBookmark = dao.getBookmark(txtAddress.getText());

        String[] options = new String[]{"1", "2", "3", "Cancel"};
        JButton btnShowOldBookmark = new JButton(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                TextDialog td = new TextDialog(EditDialog.this, true);
                td.setText(oldBookmark.toString());
                td.setVisible(true);
                td.dispose();
            }
        });
        btnShowOldBookmark.setText("Show Existing");

        int choice = JOptionPane.showOptionDialog(this,
                TBError.getMessage("eAddress.m"),
                TBError.getMessage("eAddress.t"),
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE,
                null, new Object[]{options[0], options[1], options[2],
                options[3], btnShowOldBookmark}, options[0]);

        switch (choice) {
            case 0: // merge
                txtName.setText(oldBookmark.getName());
                txtDescription.setText(oldBookmark.getDesc());
            case 1: // merge & replace, continue merge
                String notes = oldBookmark.getNotes();
                if (notes != null && !notes.isEmpty()) {
                    txtNotes.setText(notes + "\n" + txtNotes.getText());
                }
                Set<Tag> ts = oldBookmark.getTags();
                if (ts != null) {
                    for (Tag tag : ts) {
                        lstModelTags.addElement(tag.toString());
                    }
                }
            case 2: // replace, continue merge, continue merge & replace
                bookmark.setId(oldBookmark.getId());
                bookmark.setDateadded(oldBookmark.getDateadded());
                txtDateadded
                        .setText(TBGuiUtils.formatDate(bookmark.getDateadded()));
                bookmark.setAddress(txtAddress.getText());
                methodType = TBConsts.EDIT_UPDATE;
                break;
        }
    }

    private void prepareBookmark() {
        bookmark.setName(txtName.getText());
        String desc = txtDescription.getText();
        if (!desc.isEmpty())
            bookmark.setDesc(desc);
        bookmark.setAddress(txtAddress.getText());
        String notes = txtNotes.getText();
        if (!notes.isEmpty())
            bookmark.setNotes(notes);
        bookmark.setTags(null);
        String[] ts = getTags();
        if (ts != null)
            for (String t : getTags())
                bookmark.addTag(new Tag(t));
    }

}
