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

import org.apache.commons.io.FilenameUtils;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.hyperlink.HyperlinkAction;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.HyperlinkProvider;
import org.jdesktop.swingx.table.ColumnControlButton;
import org.jdesktop.swingx.table.TableColumnExt;
import pw.ahs.app.tagbook.core.BookmarkI;
import pw.ahs.app.tagbook.core.DataAccessObject;
import pw.ahs.app.tagbook.core.OpenHistory;
import pw.ahs.app.tagbook.core.TagI;
import pw.ahs.app.tagbook.core.utils.TBConsts;
import pw.ahs.app.tagbook.core.utils.TBError;
import pw.ahs.app.tagbook.core.utils.TBIOUtils;
import pw.ahs.app.tagbook.core.utils.TBText;
import pw.ahs.app.tagbook.gui.utils.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

@SuppressWarnings("serial")
public class MainWindow extends JXFrame {
    private final LogPanel logPanel;
    private final TagManagerDialog tagManager;
    // gui components
    private JXTable table;
    private DefaultTableModel tableModel;
    private final DefaultTableModelController rowController;
    private JXSearchField txtSearchField;
    private JXSearchField txtFilterField;
    private JPanel panelFilter;
    private JFileChooser fileChooser;
    private JXBusyLabel busyLabel;
    private JLabel statusLabel;
    private JLabel countLabel;
//    private JSpinner tableFontSizeSpinner;
    //    private TableFont tableFontNormal;
//    private TableFont tableFontBig;
//    private TableFont tableFontBigger;
    private JMenu openHistoryMenu;
    private List<TableRowInfo> hiddenRows;
    // actions
    private Action actionNewBookmark;
    private Action actionEditBookmark;
    private Action actionDeleteBookmark;
    private Action actionTagBookmark;
    private Action actionHideBookmark;
    private Action actionUnhideBookmarks;
    private Action actionPasteAddress;
    private Action actionPasteSpecial;
    private Action actionCopyName;
    private Action actionCopyDesc;
    private Action actionCopyAddress;
    private Action actionCopyNotes;
    private Action actionCopyTags;
    private Action actionCopyDateadded;
    private Action actionCopyDatemodified;
    private Action actionSearch;
    private Action actionFilter;
    private Action actionExport;
    private Action actionImport;
    private Action actionDbNew;
    private Action actionDbLoad;
    private Action actionDbBackup;
    private Action actionDbRestore;
    private Action actionDbRefresh;
    private Action actionDbClose;
    private Action actionHide;
    private Action actionClose;
    //    private Action actionReadOnly;
    private Action actionViewStats;
    private Action actionViewTagManager;
    private Action actionClearOpenHistory;
    private Action actionAbort;
    // data variables
    private DataAccessObject dao;
    private boolean readOnly;
    private boolean readOnlyPreState; // used by setBusy
    private boolean dbLoaded;
    private boolean entriesSelected;
    private boolean entrySelected;
    private OpenHistory openHistory;
    // settings
    private boolean searchInstantly;
    private boolean miniToTray;
    private boolean useHyperlinks;
    private boolean confirmExit;

    public MainWindow(String arg) {
        logPanel = new LogPanel();
        initData(); // logPanel must be initialized

        // dao must be initialized
        tagManager = new TagManagerDialog(this, false, dao);
        initGui();

        // tableModel must be initialized
        rowController = new DefaultTableModelController(tableModel);
        rowController.getRowCounter().addPropertyChangeListener(
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        countLabel.setText(String.valueOf(evt
                                .getNewValue()));
                    }
                });
        loadConfigs(arg);

        txtSearchField.requestFocusInWindow();
    }

    /* Initializing Helper Methods { */
    private void initData() {
        try {
            dao = new DataAccessObject(logPanel);
        } catch (IllegalStateException e) {
            JXErrorPane.showDialog(
                    this,
                    new ErrorInfo(TBError.getMessage("driver.t"), TBError
                            .getMessage("driver.m"), null, "Fatal", e,
                            Level.SEVERE, null));
            System.exit(1);
        }

        setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferHandler.TransferSupport support) {
                return !(!support.isDataFlavorSupported(DataFlavor.stringFlavor)
                        || readOnly);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                if (support.isDataFlavorSupported(DataFlavor.stringFlavor))
                    try {
                        String text = (String) support.getTransferable()
                                .getTransferData(DataFlavor.stringFlavor);
                        showNewBookmarkDialog(null, text, null, null);
                        return true;
                    } catch (IOException | UnsupportedFlavorException
                            | ClassCastException e) {
                        logPanel.error(
                                TBError.getMessage("dnd.m") + "\n"
                                        + e.getMessage(),
                                TBError.getMessage("dnd.t"));
                        return false;
                    }
                else
                    return false;
            }
        });
    }

    private void initGui() {
        setTitle(TBConsts.APP_TITLE);
        setStartPosition(StartPosition.CenterInScreen);
        setIconImage(TBIcon.get("app").getImage());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setLayout(new GridBagLayout());

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (miniToTray)
                    setVisible(false);
                else
                    closeApp(true);
            }
        });

        openHistory = new OpenHistory();
        hiddenRows = new ArrayList<>();

        initActions();
        initTable(createTablePopupMenu());
        initSearchField();
        initFilterPanel();
        initFileChooser();

        setJMenuBar(createMenuBar());
        setStatusBar(createStatusBar());

        initTrayIcon(createTrayPopupMenu());

        pack();
        txtSearchField.requestFocusInWindow();
    }

    private void initActions() {
        actionNewBookmark = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showNewBookmarkDialog(null, null, null, null);
            }
        };
        TBGuiUtils.initializeAction(actionNewBookmark, "Add..",
                "Add New Bookmark", TBMnemonic.get("bookmark.add"),
                TBIcon.get("bookmark.add"), TBKeyStroke.get("bookmark.add"));

        actionEditBookmark = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                EditDialog editgui = new EditDialog(MainWindow.this, true, dao,
                        TBConsts.EDIT_UPDATE);
                editgui.setBookmark(dao
                        .getBookmark((Long) getTableSelectedRow()[0]));
                editgui.setVisible(true);
                BookmarkI bkmrk = editgui.getBookmark();
                if (bkmrk != null) {
                    if (dao.insertUpdateBookmark(bkmrk)) {
                        setStatusMessage(false, TBText.get("op.update"));
                        int rowId = getTableRow(bkmrk.getId());
                        rowController.updateRow(rowId, bkmrk.toArray(
                                TBConsts.ADDRESS_AS_URI,
                                TBConsts.TAGS_AS_STRING));
                        table.changeSelection(rowId, 0, false, false);
                    } else {
                        setStatusMessage(true, TBError.getMessage("op.update"));
                    }
                }
                editgui.dispose();
            }
        };
        TBGuiUtils.initializeAction(actionEditBookmark, "Edit..",
                "Edit Bookmark", TBMnemonic.get("bookmark.edit"),
                TBIcon.get("bookmark.edit"), TBKeyStroke.get("bookmark.edit"));

        actionDeleteBookmark = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(MainWindow.this, "Delete "
                        + table.getSelectedRowCount() + " bookmarks?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    setBusy(true, false, "Deleting..");
                    dao.tryResetAbort();
                    TBWorker worker = new TBWorker() {

                        @Override
                        protected void doneWithSuccess() {
                            setBusy(false, false, TBText.get("op.del"));
                        }

                        @Override
                        protected void doneWithError() {
                            setBusy(false, true, TBError.getMessage("op.del"));
                        }

                        @Override
                        protected void doneWithAbort() {
                            setBusy(false, false, TBText.get("op.del"));
                        }

                        @Override
                        protected void alwaysDoWhenDone() {
                            actionAbort.setEnabled(false);
                            actionDbRefresh.actionPerformed(null);
                        }

                        @Override
                        protected int doThis() {
                            int[] ids = table.getSelectedRows();
                            Long[] bids = new Long[ids.length];
                            for (int i = 0; i < ids.length; ++i) {
                                bids[i] = getBookmarkId(ids[i]);
                            }
                            return dao.deleteBookmarks(bids);
                            // boolean error = false;
                            // int i = table.getSelectedRow();
                            // while (i != -1) {
                            // if (dao.isAborted()) {
                            // return TBConsts.DAO_ABORT;
                            // }
                            // if (!dao.deleteBookmark((Long)
                            // getTableSelectedRow()[0])) {
                            // error = true;
                            // }
                            /*
                             * else { // this line causes exception due to
							 * rowController.removeRow(table
							 * .convertRowIndexToModel(i)); } this line is
							 * causing exceptions when JTable is trying to
							 * render the model which is being modified at the
							 * same time
							 */
                            // i = table.getSelectedRow();
                            // }
                            // if (error)
                            // return TBConsts.DAO_ERROR;
                            // else
                            // return TBConsts.DAO_SUCCESS;
                        }
                    };
                    actionAbort.setEnabled(true);
                    worker.execute();
                }
            }
        };
        TBGuiUtils.initializeAction(actionDeleteBookmark, "Delete",
                "Delete Bookmark", TBMnemonic.get("bookmark.delete"),
                TBIcon.get("bookmark.delete"), TBKeyStroke.get("delete"));

        actionTagBookmark = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setBusy(true, false, "Tagging..");
                TBWorker worker = new TBWorker() {

                    @Override
                    protected void doneWithSuccess() {
                    }

                    @Override
                    protected void doneWithError() {
                        JOptionPane.showMessageDialog(MainWindow.this,
                                "Something went wrong!", "Wrong!",
                                JOptionPane.ERROR_MESSAGE);
                    }

                    @Override
                    protected void doneWithAbort() {
                    }

                    @Override
                    protected void alwaysDoWhenDone() {
                        setBusy(false, false, "Ready!");
                        actionAbort.setEnabled(false);
                        actionDbRefresh.actionPerformed(null);
                    }

                    @Override
                    protected int doThis() {
                        int[] ids = table.getSelectedRows();
                        Long[] bids = new Long[ids.length];
                        for (int i = 0; i < ids.length; ++i) {
                            bids[i] = getBookmarkId(ids[i]);
                        }
                        Set<TagI> ts = dao.getTags(null);
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;

                        TagDialog tagdialog = new TagDialog(MainWindow.this,
                                ts != null ? ts.toArray() : null);
                        String[] tags = tagdialog.showTagDialog();
                        tagdialog.dispose();
                        if (tags != null) {
                            return dao.tagBookmarks(bids, tags);
                        }
                        return TBConsts.DAO_ABORT;
                    }
                };
                actionAbort.setEnabled(true);
                worker.execute();
            }
        };
        TBGuiUtils.initializeAction(actionTagBookmark, "Tag..",
                "Tag selected bookmark(s)", TBMnemonic.get("bookmark.tag"),
                TBIcon.get("tagManager"), TBKeyStroke.get("bookmark.tag"));

        actionHideBookmark = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int i = table.getSelectedRow();
                while (i != -1) {
                    hiddenRows.add(new TableRowInfo(getTableSelectedRow(), i));
                    rowController.removeRow(table.convertRowIndexToModel(i));
                    i = table.getSelectedRow();
                }
                actionUnhideBookmarks.setEnabled(true);
            }
        };
        TBGuiUtils.initializeAction(actionHideBookmark, "Hide Selected",
                "hide selected bookmark(s)", null, null, null);

        actionUnhideBookmarks = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (TableRowInfo ri : hiddenRows) {
                    rowController.insertRow(ri.getIndex(), ri.getData());
                }
                hiddenRows.clear();
                actionUnhideBookmarks.setEnabled(false);
            }
        };
        TBGuiUtils.initializeAction(actionUnhideBookmarks, "Unhide all",
                "unhide bookmark(s)", null, null, null);
        actionUnhideBookmarks.setEnabled(false);

        actionCopyName = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (TBIOUtils.copyTextToClipboard(getTableSelectedRow()[1]
                        .toString()))
                    setStatusMessage(false, TBText.get("op.copyName"));
                else
                    logPanel.error(TBError.getMessage("clipboard.m"),
                            TBError.getMessage("clipboard.t"));
            }
        };
        TBGuiUtils.initializeAction(actionCopyName, "Name",
                "Copy Bookmark Name", TBMnemonic.get("bookmark.copyName"),
                TBIcon.get("copy"), TBKeyStroke.get("bookmark.copyName"));

        actionCopyDesc = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (TBIOUtils.copyTextToClipboard(getTableSelectedRow()[2]
                        .toString()))
                    setStatusMessage(false, TBText.get("op.copyDesc"));
                else
                    logPanel.error(TBError.getMessage("clipboard.m"),
                            TBError.getMessage("clipboard.t"));
            }
        };
        TBGuiUtils.initializeAction(actionCopyDesc, "Description",
                "Copy Bookmark Description",
                TBMnemonic.get("bookmark.copyDesc"), TBIcon.get("copy"),
                TBKeyStroke.get("bookmark.copyDesc"));

        actionCopyAddress = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (TBIOUtils.copyTextToClipboard(getTableSelectedRow()[3]
                        .toString()))
                    setStatusMessage(false, TBText.get("op.copyAddress"));
                else
                    logPanel.error(TBError.getMessage("clipboard.m"),
                            TBError.getMessage("clipboard.t"));
            }
        };
        TBGuiUtils.initializeAction(actionCopyAddress, "Data",
                "Copy Bookmark Data",
                TBMnemonic.get("bookmark.copyAddress"), TBIcon.get("copy"),
                TBKeyStroke.get("bookmark.copyAddress"));

        actionCopyNotes = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (TBIOUtils.copyTextToClipboard(getTableSelectedRow()[4]
                        .toString()))
                    setStatusMessage(false, TBText.get("op.copyNotes"));
                else
                    logPanel.error(TBError.getMessage("clipboard.m"),
                            TBError.getMessage("clipboard.t"));
            }
        };
        TBGuiUtils.initializeAction(actionCopyNotes, "Notes",
                "Copy Bookmark Notes", TBMnemonic.get("bookmark.copyNotes"),
                TBIcon.get("copy"), TBKeyStroke.get("bookmark.copyNotes"));

        actionCopyTags = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (TBIOUtils.copyTextToClipboard(getTableSelectedRow()[5]
                        .toString()))
                    setStatusMessage(false, TBText.get("op.copyTags"));
                else
                    logPanel.error(TBError.getMessage("clipboard.m"),
                            TBError.getMessage("clipboard.t"));
            }
        };
        TBGuiUtils.initializeAction(actionCopyTags, "Tags",
                "Copy Bookmark Tags", TBMnemonic.get("bookmark.copyTags"),
                TBIcon.get("copy"), TBKeyStroke.get("bookmark.copyTags"));

        actionCopyDateadded = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (TBIOUtils.copyTextToClipboard(getTableSelectedRow()[6]
                        .toString()))
                    setStatusMessage(false, TBText.get("op.copyDateadded"));
                else
                    logPanel.error(TBError.getMessage("clipboard.m"),
                            TBError.getMessage("clipboard.t"));
            }
        };
        TBGuiUtils.initializeAction(actionCopyDateadded, "Date Added",
                "Copy Bookmark Date Added",
                TBMnemonic.get("bookmark.copyDateadded"), TBIcon.get("copy"),
                TBKeyStroke.get("bookmark.copyDateadded"));

        actionCopyDatemodified = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (TBIOUtils.copyTextToClipboard(getTableSelectedRow()[7]
                        .toString()))
                    setStatusMessage(false, TBText.get("op.copyDatemodified"));
                else
                    logPanel.error(TBError.getMessage("clipboard.m"),
                            TBError.getMessage("clipboard.t"));
            }
        };
        TBGuiUtils.initializeAction(actionCopyDatemodified, "Date Modified",
                "Copy Bookmark Date Modified",
                TBMnemonic.get("bookmark.copyDatemodified"),
                TBIcon.get("copy"),
                TBKeyStroke.get("bookmark.copyDatemodified"));

        actionPasteAddress = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String text = TBIOUtils.getTextFromClipboard();
                if (text != null) {
                    showNewBookmarkDialog(null, text, null, null);
                }
            }
        };
        TBGuiUtils.initializeAction(actionPasteAddress, "Paste Data..",
                "Add New Data", TBMnemonic.get("bookmark.pasteAddress"),
                TBIcon.get("paste"), TBKeyStroke.get("bookmark.pasteAddress"));

        actionPasteSpecial = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String text = TBIOUtils.getTextFromClipboard();
                if (text != null) {
                    String field = (String) JOptionPane.showInputDialog(
                            MainWindow.this, "Where to paste:\n" + text,
                            "Paste to", JOptionPane.QUESTION_MESSAGE, null,
                            new String[]{"Name", "Description", "Data",
                                    "Notes"}, "Name");
                    if (field != null)
                        switch (field) {
                            case "Name":
                                showNewBookmarkDialog(text, null, null, null);
                                break;
                            case "Description":
                                showNewBookmarkDialog(null, null, text, null);
                                break;
                            case "Data":
                                showNewBookmarkDialog(null, text, null, null);
                                break;
                            case "Notes":
                                showNewBookmarkDialog(null, null, null, text);
                                break;
                        }
                }
            }
        };
        TBGuiUtils.initializeAction(actionPasteSpecial, "Paste Special..",
                "Paste to field", TBMnemonic.get("bookmark.pasteSpecial"),
                TBIcon.get("paste"), TBKeyStroke.get("bookmark.pasteSpecial"));

        actionHide = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(!isVisible());
            }
        };
        TBGuiUtils.initializeAction(actionHide, "Hide", "Hide Window",
                TBMnemonic.get("hide"), TBIcon.get("hide"),
                TBKeyStroke.get("hide"));

        actionClose = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                closeApp(false);
            }
        };
        TBGuiUtils.initializeAction(actionClose, "Close", "Close Application",
                TBMnemonic.get("close"), TBIcon.get("exit"),
                TBKeyStroke.get("close"));

        actionSearch = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                txtSearchField.requestFocusInWindow();
            }
        };
        TBGuiUtils.initializeAction(actionSearch, "Search", "Search Bookmarks",
                TBMnemonic.get("bookmark.search"), TBIcon.get("search"),
                TBKeyStroke.get("searchFocus"));

        actionFilter = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                panelFilter.setVisible(true);
                txtFilterField.requestFocusInWindow();
            }
        };
        TBGuiUtils.initializeAction(actionFilter, "Filter", "Filter Bookmarks",
                TBMnemonic.get("bookmark.filter"), TBIcon.get("filter"),
                TBKeyStroke.get("filterFocus"));

        actionExport = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final ExportImportDialog eid = new ExportImportDialog(
                        MainWindow.this, TBConsts.EXPORT);
                setBusy(true, false, TBText.get("op.export.on"));
                final File f = eid.showExportDialog();
                if (f != null) {
                    final Long[] bids;
                    if (eid.getExportMethod() == TBConsts.EXPORT_SELECTED) {
                        int[] ids = table.getSelectedRows();
                        if (ids.length > 0) {
                            bids = new Long[ids.length];
                            for (int i = 0; i < ids.length; ++i) {
                                bids[i] = getBookmarkId(ids[i]);
                            }
                        } else
                            return;
                    } else
                        bids = null;
                    TBWorker worker = new TBWorker() {

                        @Override
                        protected void doneWithSuccess() {
                            setBusy(false, false, TBText.get("op.export"));
                        }

                        @Override
                        protected void doneWithError() {
                            setBusy(false, true,
                                    TBError.getMessage("op.export"));
                        }

                        @Override
                        protected void doneWithAbort() {
                            setBusy(false, false, TBText.get("op.export.abort"));
                        }

                        @Override
                        protected void alwaysDoWhenDone() {
                            actionAbort.setEnabled(false);
                        }

                        @Override
                        protected int doThis() {
                            return dao.exportBookmarks(f.getAbsolutePath(),
                                    eid.getFormat(), bids);
                        }
                    };
                    actionAbort.setEnabled(true);
                    worker.execute();
                } else {
                    setBusy(false, false, TBText.get("op.export.abort"));
                }
            }
        };
        TBGuiUtils.initializeAction(actionExport, "Export..",
                "Export Bookmarks", TBMnemonic.get("bookmark.export"),
                TBIcon.get("export"), TBKeyStroke.get("bookmark.export"));

        actionImport = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final ExportImportDialog eid = new ExportImportDialog(
                        MainWindow.this, TBConsts.IMPORT);
                setBusy(true, false, TBText.get("op.import.on"));
                final File f = eid.showImportDialog();
                if (f != null) {
                    TBWorker worker = new TBWorker() {
                        @Override
                        protected int doThis() {
                            return dao.importBookmarks(f.getAbsolutePath(),
                                    eid.getFormat(), eid.getConflictsMethod(),
                                    eid.getImportedTags(), rowController);
                        }

                        @Override
                        protected void alwaysDoWhenDone() {
                            actionAbort.setEnabled(false);
                        }

                        @Override
                        protected void doneWithAbort() {
                            setBusy(false, false, TBText.get("op.import.abort"));
                        }

                        @Override
                        protected void doneWithError() {
                            setBusy(false, true,
                                    TBError.getMessage("op.import"));
                        }

                        @Override
                        protected void doneWithSuccess() {
                            setBusy(false, false, TBText.get("op.import"));
                        }
                    };
                    actionAbort.setEnabled(true);
                    worker.execute();
                } else {
                    setBusy(false, false, TBText.get("op.import.abort"));
                }
            }
        };
        TBGuiUtils.initializeAction(actionImport, "Import..",
                "Import Bookmarks", TBMnemonic.get("bookmark.import"),
                TBIcon.get("import"), TBKeyStroke.get("bookmark.import"));

        actionDbNew = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setDialogTitle(TBText.get("op.newDb.fcTitle"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                TBGuiUtils.removeFileChooserFileFilters(fileChooser);
                fileChooser.setFileFilter(TBGuiUtils.tbFileFilter);
                setBusy(true, false, TBText.get("op.newDb.on"));
                if (fileChooser.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                    TBWorker worker = new TBWorker() {

                        @Override
                        protected void doneWithSuccess() {
                            dbLoaded = true;
                            updateActionsState();
                            openHistory.addPath(dao.getDbPath());
                            updateOpenHistoryMenu();
                            setBusy(false, false, TBText.get("op.newDb"));
                        }

                        @Override
                        protected void doneWithError() {
                            setBusy(false, true, TBError.getMessage("op.newDb"));
                        }

                        @Override
                        protected void doneWithAbort() {
                            setBusy(false, false, TBText.get("op.newDb.abort"));
                        }

                        @Override
                        protected void alwaysDoWhenDone() {
                            actionAbort.setEnabled(false);
                        }

                        @Override
                        protected int doThis() {
                            dao.close();
                            rowController.clearRows();
                            if (dao.createDb(fileChooser.getSelectedFile()
                                    .getAbsolutePath())) {
                                return TBConsts.DAO_SUCCESS;
                            } else
                                return TBConsts.DAO_ERROR;
                        }
                    };
                    actionAbort.setEnabled(true);
                    worker.execute();
                } else {
                    setBusy(false, false, TBText.get("op.newDb.abort"));
                }
            }
        };
        TBGuiUtils.initializeAction(actionDbNew, "New..",
                "Create New Database", TBMnemonic.get("db.new"),
                TBIcon.get("db.new"), TBKeyStroke.get("db.new"));

        actionDbLoad = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setDialogTitle(TBText.get("op.loadDb.fcTitle"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                TBGuiUtils.removeFileChooserFileFilters(fileChooser);
                fileChooser.setFileFilter(TBGuiUtils.tbFileFilter);
                setBusy(true, false, TBText.get("op.loadDb.on"));
                if (fileChooser.showOpenDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                    TBWorker worker = new TBWorker() {

                        @Override
                        protected void doneWithSuccess() {
                            setBusy(false, false, TBText.get("op.loadDb"));
                        }

                        @Override
                        protected void doneWithError() {
                            setBusy(false, true,
                                    TBError.getMessage("op.loadDb"));
                        }

                        @Override
                        protected void doneWithAbort() {
                            setBusy(false, false, TBText.get("op.loadDb.abort"));
                        }

                        @Override
                        protected void alwaysDoWhenDone() {
                            actionAbort.setEnabled(false);
                        }

                        @Override
                        protected int doThis() {
                            if (loadDb(fileChooser.getSelectedFile()
                                    .getAbsolutePath()))
                                return TBConsts.DAO_SUCCESS;
                            else
                                return TBConsts.DAO_ERROR;
                        }
                    };
                    actionAbort.setEnabled(true);
                    worker.execute();
                } else {
                    setBusy(false, false, TBText.get("op.loadDb.abort"));
                }
            }
        };
        TBGuiUtils.initializeAction(actionDbLoad, "Open..", "Open Book",
                TBMnemonic.get("db.load"), TBIcon.get("db.load"),
                TBKeyStroke.get("db.load"));

        actionDbBackup = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setDialogTitle(TBText.get("op.backupDb.fcTitle"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                TBGuiUtils.removeFileChooserFileFilters(fileChooser);
                fileChooser.setFileFilter(TBGuiUtils.tbbkFileFilter);
                setBusy(true, false, TBText.get("op.backupDb.on"));
                if (fileChooser.showSaveDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                    TBWorker worker = new TBWorker() {

                        @Override
                        protected void doneWithSuccess() {
                            dbLoaded = true;
                            updateActionsState();
                            setBusy(false, false, TBText.get("op.backupDb"));
                        }

                        @Override
                        protected void doneWithError() {
                            setBusy(false, true,
                                    TBError.getMessage("op.backup"));
                        }

                        @Override
                        protected void doneWithAbort() {
                            setBusy(false, false,
                                    TBText.get("op.backupDb.abort"));
                        }

                        @Override
                        protected void alwaysDoWhenDone() {
                            actionAbort.setEnabled(false);
                        }

                        @Override
                        protected int doThis() {
                            if (dao.backup(fileChooser.getSelectedFile()
                                    .getAbsolutePath()))
                                return TBConsts.DAO_SUCCESS;
                            else
                                return TBConsts.DAO_ERROR;
                        }
                    };
                    actionAbort.setEnabled(true);
                    worker.execute();
                } else {
                    setBusy(false, false, TBText.get("op.backupDb.abort"));
                }
            }
        };
        TBGuiUtils.initializeAction(actionDbBackup, "Backup..",
                "Backup Book", TBMnemonic.get("db.backup"),
                TBIcon.get("db.backup"), TBKeyStroke.get("db.backup"));

        actionDbRestore = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(MainWindow.this,
                        TBText.get("op.restoreDb.notfi.m"),
                        TBText.get("op.restoreDb.notif.t"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    fileChooser.setDialogTitle(TBText
                            .get("op.restoreDb.fcTitle"));
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    TBGuiUtils.removeFileChooserFileFilters(fileChooser);
                    fileChooser.setFileFilter(TBGuiUtils.tbbkFileFilter);
                    setBusy(true, false, TBText.get("op.restoreDb.on"));
                    if (fileChooser.showOpenDialog(MainWindow.this) == JFileChooser.APPROVE_OPTION) {
                        TBWorker worker = new TBWorker() {

                            @Override
                            protected void doneWithSuccess() {
                                dbLoaded = true;
                                updateActionsState();
                                setBusy(false, false,
                                        TBText.get("op.restoreDb"));
                            }

                            @Override
                            protected void doneWithError() {
                                setBusy(false, true,
                                        TBError.getMessage("op.restoreDb"));
                            }

                            @Override
                            protected void doneWithAbort() {
                                setBusy(false, false,
                                        TBText.get("op.restoreDb.abort"));
                            }

                            @Override
                            protected void alwaysDoWhenDone() {
                                actionAbort.setEnabled(false);
                            }

                            @Override
                            protected int doThis() {
                                if (dao.restore(fileChooser.getSelectedFile()
                                        .getAbsolutePath())) {
                                    dao.getBookmarks(rowController);
                                    if (dao.isAborted())
                                        return TBConsts.DAO_ABORT;
                                    return TBConsts.DAO_SUCCESS;
                                } else
                                    return TBConsts.DAO_ERROR;
                            }
                        };
                        actionAbort.setEnabled(true);
                        worker.execute();
                    } else {
                        setBusy(false, false, TBText.get("op.restoreDb.abort"));
                    }
                }
            }
        };
        TBGuiUtils.initializeAction(actionDbRestore, "Restore..",
                "Restore Book", TBMnemonic.get("db.restore"),
                TBIcon.get("db.restore"), TBKeyStroke.get("db.restore"));

        actionDbRefresh = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
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
                    protected void alwaysDoWhenDone() {
                        actionAbort.setEnabled(false);
                    }

                    @Override
                    protected int doThis() {
                        rowController.clearRows();
                        hiddenRows.clear();
                        actionUnhideBookmarks.setEnabled(false);
                        dao.getBookmarks(rowController);
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        else
                            return TBConsts.DAO_SUCCESS;
                    }
                };
                actionAbort.setEnabled(true);
                worker.execute();
            }
        };
        TBGuiUtils.initializeAction(actionDbRefresh, "Refresh",
                "Refresh Book", TBMnemonic.get("db.refresh"),
                TBIcon.get("db.refresh"), TBKeyStroke.get("db.refresh"));

        actionDbClose = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dao.close();
                rowController.clearRows();
                dbLoaded = false;
                updateActionsState();
                setStatusMessage(false, TBText.get("op.closeDb"));
            }
        };
        TBGuiUtils.initializeAction(actionDbClose, "Close", "Close Book",
                TBMnemonic.get("db.close"), TBIcon.get("db.close"),
                TBKeyStroke.get("db.close"));

//        actionReadOnly = new AbstractAction() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                boolean state = (boolean) getValue(Action.SELECTED_KEY);
//                setReadOnly(state);
//            }
//        };
//        TBGuiUtils.initializeAction(actionReadOnly, "Read Only",
//                "Block All Data Updates", TBMnemonic.get("readonly"),
//                TBIcon.get("readonly.true"), TBKeyStroke.get("readonly"));

        actionClearOpenHistory = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                openHistory.clear();
                updateOpenHistoryMenu();
            }
        };
        TBGuiUtils.initializeAction(actionClearOpenHistory, "Clear",
                "Clear History", TBMnemonic.get("clear"), TBIcon.get("clear"),
                null);

        actionAbort = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dao.abort();
                actionAbort.setEnabled(false);
            }
        };
        TBGuiUtils.initializeAction(actionAbort, null, "Stop current opertion",
                null, TBIcon.get("stop"), null);

        actionAbort.setEnabled(false);
    }

    private JMenuBar createMenuBar() {
        // Menu Bar
        JMenuBar menuBar = new JMenuBar();

        // bookmark menu
        JMenu bookmarkMenu = new JMenu("Bookmark");
        bookmarkMenu.setIcon(TBIcon.get("bookmark"));
        bookmarkMenu.setMnemonic('b');

        bookmarkMenu.add(actionEditBookmark);
        bookmarkMenu.add(actionDeleteBookmark);
        bookmarkMenu.add(actionTagBookmark);
        // bookmarkMenu.addSeparator();
        // bookmarkMenu.add(actionHideBookmark);
        // bookmarkMenu.add(actionUnhideBookmarks);
        bookmarkMenu.addSeparator();
        bookmarkMenu.add(actionNewBookmark);

        JMenu bookmarkMenuCopyMenu = new JMenu("Copy");
        bookmarkMenuCopyMenu.setIcon(TBIcon.get("copy"));
        bookmarkMenuCopyMenu.setMnemonic('c');

        bookmarkMenuCopyMenu.add(actionCopyAddress);
        bookmarkMenuCopyMenu.add(actionCopyName);
        bookmarkMenuCopyMenu.add(actionCopyTags);
        bookmarkMenuCopyMenu.add(actionCopyDesc);
        bookmarkMenuCopyMenu.add(actionCopyNotes);
        bookmarkMenuCopyMenu.add(actionCopyDateadded);
        bookmarkMenuCopyMenu.add(actionCopyDatemodified);

        bookmarkMenu.add(bookmarkMenuCopyMenu);

        JMenu bookmarkMenuPasteMenu = new JMenu("Paste");
        bookmarkMenuPasteMenu.setMnemonic('p');
        bookmarkMenuPasteMenu.setIcon(TBIcon.get("paste"));
        bookmarkMenuPasteMenu.add(actionPasteAddress);
        bookmarkMenuPasteMenu.add(actionPasteSpecial);

        bookmarkMenu.add(bookmarkMenuPasteMenu);

        bookmarkMenu.addSeparator();
        bookmarkMenu.add(actionSearch);
        bookmarkMenu.add(actionFilter);
        bookmarkMenu.addSeparator();
        bookmarkMenu.add(actionExport);
        bookmarkMenu.add(actionImport);
        bookmarkMenu.addSeparator();
        bookmarkMenu.add(actionHide);
        bookmarkMenu.add(actionClose);

        menuBar.add(bookmarkMenu);

        // db menu
        JMenu dbMenu = new JMenu("Book");
        dbMenu.setIcon(TBIcon.get("database"));
        dbMenu.setMnemonic('d');

        openHistoryMenu = new JMenu("Open Recent");
        openHistoryMenu.setIcon(TBIcon.get("db.recent"));

        dbMenu.add(openHistoryMenu);
        dbMenu.add(actionDbNew);
        dbMenu.add(actionDbLoad);
        dbMenu.add(actionDbBackup);
        dbMenu.add(actionDbRestore);
        dbMenu.add(actionDbRefresh);
        dbMenu.addSeparator();
        dbMenu.add(actionDbClose);

        menuBar.add(dbMenu);

        // View Menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setIcon(TBIcon.get("view"));
        viewMenu.setMnemonic('v');

        actionViewTagManager = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                tagManager.setVisible(true);
            }
        };
        TBGuiUtils.initializeAction(actionViewTagManager, "TagManager",
                "Show TagManager", TBMnemonic.get("view.tagManager"),
                TBIcon.get("tagManager"), TBKeyStroke.get("view.tagManager"));

        actionViewStats = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setBusy(true, false, "Loading statistics..");
                TBWorker worker = new TBWorker() {
                    final StatsDialog sd = new StatsDialog(MainWindow.this);

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
                    protected void alwaysDoWhenDone() {
                        sd.dispose();
                        actionAbort.setEnabled(false);
                    }

                    @Override
                    protected int doThis() {
                        dao.tryResetAbort();
                        sd.setCountBookmark(dao.getBookmarksCount());
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        sd.setCountTag(dao.getTagsCount());
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        sd.setCountUntagged(dao.getUntaggedBookmarksCount());
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        sd.setCountTagged(dao.getTaggedBookmarksCount());
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        sd.setCountOrph(dao.getOrphanedTagsCount());
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        sd.setCountLink(dao.getLinkBookmarksCount());
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        sd.setCountEmail(dao.getEmailBookmarksCount());
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        TagI mpt = dao.getMostPopularTag();
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        sd.setCountMostPopularTag(mpt == null ? 0 : dao
                                .getTagBookmarksCount(mpt.getId()));
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        sd.setMostPopularTagName(mpt == null ? null : mpt
                                .getName());
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        BookmarkI bkmrk = dao.getMostTaggedBookmark();
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        sd.setCountMostTagged(bkmrk == null ? 0 : dao
                                .getBookmarkTagsCount(bkmrk.getId()));
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        sd.setMostTaggedBookmark(bkmrk);
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;
                        sd.updateText();
                        setBusy(false, false, "Ready!");
                        sd.setVisible(true);
                        return TBConsts.DAO_SUCCESS;
                    }
                };
                actionAbort.setEnabled(true);
                worker.execute();
            }
        };
        TBGuiUtils.initializeAction(actionViewStats, "Statistics",
                "Show Statistics", TBMnemonic.get("view.stats"),
                TBIcon.get("stats"), TBKeyStroke.get("view.stats"));

        Action actionViewLog = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                logPanel.setVisible(true);
            }
        };
        TBGuiUtils.initializeAction(actionViewLog, "Log", "Show Log",
                TBMnemonic.get("view.log"), TBIcon.get("log"),
                TBKeyStroke.get("view.log"));

        Action actionViewSettings = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SettingsDialog settings = new SettingsDialog(MainWindow.this);
                settings.setDbPath(dao.getDbPath());
                settings.setSearchInstantly(searchInstantly);
                settings.setMiniToTray(miniToTray);
                settings.setUseHyperlinks(useHyperlinks);
                settings.setConfirmExit(confirmExit);
                settings.setVisible(true);

                if (settings.isAccepted()) {
                    setSearchInstantly(settings.getSearchIntantly());
                    miniToTray = settings.getMiniToTray();
                    useHyperlinks = settings.getUseHyperlinks();
                    confirmExit = settings.getConfirmExit();
                }
            }
        };
        TBGuiUtils.initializeAction(actionViewSettings, "Settings",
                "Show Settings", TBMnemonic.get("view.settings"),
                TBIcon.get("settings"), TBKeyStroke.get("view.settings"));

        Action actionViewHelp = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new HelpPanel().setVisible(true);
            }
        };
        TBGuiUtils.initializeAction(actionViewHelp, "Help", "Show Help",
                TBMnemonic.get("view.help"), TBIcon.get("help"),
                TBKeyStroke.get("view.help"));

        Action actionViewAbout = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new AboutDialog(MainWindow.this).setVisible(true);
            }
        };
        TBGuiUtils.initializeAction(actionViewAbout, "About", "Show About",
                TBMnemonic.get("view.about"), TBIcon.get("info"),
                TBKeyStroke.get("view.about"));

//        viewMenu.add(new JCheckBoxMenuItem(actionReadOnly));
//        viewMenu.addSeparator();
        viewMenu.add(actionViewTagManager);
        viewMenu.add(actionViewSettings);
        viewMenu.add(actionViewLog);
        viewMenu.add(actionViewStats);
        viewMenu.add(actionViewHelp);
        viewMenu.add(actionViewAbout);

        menuBar.add(viewMenu);

        // selection menu
        JMenu selMenu = new JMenu("Select");
        selMenu.setMnemonic('s');
        selMenu.setIcon(TBIcon.get("sel"));

        Action selAll = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                table.selectAll();
            }
        };
        TBGuiUtils.initializeAction(selAll, "All", "Select All",
                TBMnemonic.get("sel.all"), TBIcon.get("sel.all"),
                TBKeyStroke.get("sel.all"));

        Action selNone = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                table.clearSelection();
            }
        };
        TBGuiUtils.initializeAction(selNone, "None", "Select None",
                TBMnemonic.get("sel.non"), TBIcon.get("sel.non"),
                TBKeyStroke.get("sel.non"));

        Action selInvert = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selected = table.getSelectedRows();
                table.selectAll();
                if (selected.length > 0)
                    for (int aSelected : selected) {
                        table.getSelectionModel().removeSelectionInterval(
                                aSelected, aSelected);
                    }
            }
        };
        TBGuiUtils.initializeAction(selInvert, "Invert", "Invert Selection",
                TBMnemonic.get("sel.inv"), TBIcon.get("sel.inv"),
                TBKeyStroke.get("sel.inv"));

        selMenu.add(selAll);
        selMenu.add(selNone);
        selMenu.add(selInvert);

        menuBar.add(selMenu);

        menuBar.add(Box.createHorizontalGlue());

        // table font size
//        tableFontSizeSpinner = new JSpinner(new SpinnerListModel(
//                new TableFont[]{tableFontNormal, tableFontBig,
//                        tableFontBigger}));
//        tableFontSizeSpinner.setToolTipText("Font Size");
//        tableFontSizeSpinner.setMaximumSize(new Dimension(10, 30));
//        tableFontSizeSpinner.addChangeListener(new ChangeListener() {
//
//            @Override
//            public void stateChanged(ChangeEvent e) {
//                setTableFont((TableFont) ((JSpinner) e.getSource()).getValue());
//            }
//        });
//        tableFontSizeSpinner.setFocusable(false);
//        menuBar.add(tableFontSizeSpinner, new GridBagConstraints());

        // lock button
//        JToggleButton readOnlyButton = new JToggleButton(actionReadOnly);
//        readOnlyButton.setText(null);
//        menuBar.add(readOnlyButton);

        return menuBar;
    }

    private JPopupMenu createTablePopupMenu() {
        // Table PopupMenu
        JPopupMenu popupMenuTable = new JPopupMenu("Table Popup Menu");
        popupMenuTable.add(actionEditBookmark);
        popupMenuTable.add(actionDeleteBookmark);
        popupMenuTable.add(actionTagBookmark);
        // popupMenuTable.addSeparator();
        // popupMenuTable.add(actionHideBookmark);
        // popupMenuTable.add(actionUnhideBookmarks);
        popupMenuTable.addSeparator();
        popupMenuTable.add(actionNewBookmark);
        popupMenuTable.add(actionPasteAddress);
        popupMenuTable.add(actionPasteSpecial);

        JMenu popupMenuTableCopyMenu = new JMenu();
        popupMenuTableCopyMenu.setIcon(TBIcon.get("copy"));
        popupMenuTableCopyMenu.setText("Copy");
        popupMenuTableCopyMenu.setMnemonic('c');

        popupMenuTableCopyMenu.add(actionCopyAddress);
        popupMenuTableCopyMenu.add(actionCopyName);
        popupMenuTableCopyMenu.add(actionCopyTags);
        popupMenuTableCopyMenu.add(actionCopyDesc);
        popupMenuTableCopyMenu.add(actionCopyNotes);
        popupMenuTableCopyMenu.add(actionCopyDateadded);
        popupMenuTableCopyMenu.add(actionCopyDatemodified);

        popupMenuTable.add(popupMenuTableCopyMenu);

        return popupMenuTable;
    }

    private void initTable(JPopupMenu popupMenuTable) {
        // table fonts
//        tableFontNormal = new TableFont(1, new Font(Font.SANS_SERIF,
//                Font.PLAIN, 12), 21);
//        tableFontBig = new TableFont(2, new Font(Font.SANS_SERIF, Font.PLAIN,
//                13), 22);
//        tableFontBigger = new TableFont(3, new Font(Font.SANS_SERIF,
//                Font.PLAIN, 14), 23);

        // overriding
        tableModel = new DefaultTableModel() {

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Long.class;
                    default:
                        return Object.class;
                }
            }
        };
        tableModel.setColumnIdentifiers(new String[]{
                TBText.get("table.col.id"), TBText.get("table.col.name"),
                TBText.get("table.col.desc"), TBText.get("table.col.address"),
                TBText.get("table.col.notes"), TBText.get("table.col.tags"),
                TBText.get("table.col.dateadded"),
                TBText.get("table.col.datemodified")});
        table = new JXTable(tableModel) {

            private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table,
                                                               Object value, boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    super.getTableCellRendererComponent(table, value,
                            isSelected, hasFocus, row, column);

                    if (value == null)
                        return this;

                    if (value instanceof java.sql.Timestamp) {
                        this.setText(TBGuiUtils
                                .formatDate((java.sql.Timestamp) value));
                    }
                    this.setToolTipText(value.toString());
                    return this;
                }
            };

            private final DefaultTableRenderer hyperLinkRenderer = TBConsts.IsDesktopSupported ? new DefaultTableRenderer(
                    new HyperlinkProvider(new HyperlinkAction())) {

                @Override
                public Component getTableCellRendererComponent(JTable table,
                                                               Object value, boolean isSelected, boolean hasFocus,
                                                               int row, int column) {

                    Component c = super.getTableCellRendererComponent(table,
                            value, isSelected, hasFocus, row, column);

                    if (c instanceof JComponent && value != null) {
                        ((JComponent) c).setToolTipText(value.toString());
                    }
                    return c;
                }
            } : new DefaultTableRenderer();

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                Object value = getValueAt(row, column);
                if (value == null) {
                    return super.getCellRenderer(row, column);
                }

                if (value instanceof URI)
                    return hyperLinkRenderer;

                return defaultRenderer;
            }
        };

        // control properties
        table.setColumnControl(new ColumnControlButton(table) {

            @Override
            protected List<Action> getAdditionalActions() {
                List<Action> actions = super.getAdditionalActions();
                actions.add(new AbstractAction("Reset Sort Order") {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        table.resetSortOrder();
                    }
                });
                return actions;
            }
        });
        table.setColumnControlVisible(true);
        table.setComponentPopupMenu(popupMenuTable);
        table.setEditable(false);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // adjust keystrokes
        InputMap tableim = table
                .getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        tableim.put(KeyStroke.getKeyStroke("control C"), "none");
        tableim.put(KeyStroke.getKeyStroke("control V"), "none");
        tableim.put(KeyStroke.getKeyStroke("F2"), "none");
        tableim.put(KeyStroke.getKeyStroke("ctrl F"), "none");

        // visual properties
        table.setVisibleRowCount(10);
        table.setRowHeight(20);
        table.setShowGrid(false);
        table.setRowMargin(3);
        table.setColumnMargin(5);
        table.setRolloverEnabled(true);
        table.addHighlighter(HighlighterFactory.createAlternateStriping());
        table.addHighlighter(new ColorHighlighter(
                HighlightPredicate.ROLLOVER_ROW, new Color(0, 200, 0, 80), null));

        // DnD
        table.setDragEnabled(true);
        table.setDropMode(DropMode.INSERT_ROWS);
        table.setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferSupport support) {
                if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)
                        || readOnly) {
                    return false;
                }
                if (!(support.getDropLocation() instanceof JTable.DropLocation)) {
                    return false;
                }

                int colIndex = ((JTable.DropLocation) support.getDropLocation())
                        .getColumn();
                colIndex = table.convertColumnIndexToModel(colIndex);
                return !(colIndex == 0 // ID
                        || colIndex == 5 // Tags
                        || colIndex == 6 // Date Added
                        || colIndex == 7);

            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    String text = (String) support.getTransferable()
                            .getTransferData(DataFlavor.stringFlavor);
                    int colIndex = ((JTable.DropLocation) support
                            .getDropLocation()).getColumn();
                    colIndex = table.convertColumnIndexToModel(colIndex);

                    switch (colIndex) {
                        case 1: // name
                            showNewBookmarkDialog(text, null, null, null);
                            break;
                        case 2: // description
                            showNewBookmarkDialog(null, null, text, null);
                            break;
                        case 4: // notes
                            showNewBookmarkDialog(null, null, null, text);
                            break;
                        case 3: // address
                        default: // outside rows
                            showNewBookmarkDialog(null, text, null, null);
                            break;
                    }
                    return true;
                } catch (IOException | UnsupportedFlavorException
                        | ClassCastException e) {
                    logPanel.error(
                            TBError.getMessage("dnd.m") + "\n" + e.getMessage(),
                            TBError.getMessage("dnd.t"));
                    return false;
                }
            }

            @Override
            public int getSourceActions(JComponent c) {
                return COPY;
            }

            @Override
            protected Transferable createTransferable(JComponent c) {
                Object value = table.getValueAt(table.getSelectedRow(),
                        table.getSelectedColumn());
                if (value == null) {
                    return null;
                }
                return new StringSelection(value.toString());
            }

            @Override
            public Image getDragImage() {
                return TBGuiUtils.createImageFromText(table.getValueAt(
                        table.getSelectedRow(), table.getSelectedColumn())
                        .toString());
            }

            @Override
            public Point getDragImageOffset() {
                return new Point(0, 10);
            }
        });

        // actions
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && entrySelected) {
                    if (readOnly) {
                        EditDialog iud = new EditDialog(MainWindow.this, true,
                                dao, TBConsts.EDIT_UPDATE);
                        iud.setEditable(false);
                        iud.setBookmark(dao
                                .getBookmark((Long) getTableSelectedRow()[0]));
                        iud.setVisible(true);
                    } else {
                        actionEditBookmark.actionPerformed(null);
                    }
                }
            }
        });

        table.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_HOME) {
                    table.changeSelection(0, 0, false, false);
                } else if (e.getKeyCode() == KeyEvent.VK_END) {
                    table.changeSelection(table.getRowCount() - 1, 0, false,
                            false);
                }
            }
        });

        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        if (!e.getValueIsAdjusting()) {
                            updateActionsState(); // will call
                            // updateSelectionState()
                            countLabel.setText(table.getSelectedRowCount() + "");
                        }
                    }
                });

        // add to window
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(640, 480));
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1f;
        gridBagConstraints.weighty = 1f;
        getContentPane().add(tableScroll, gridBagConstraints);
    }

    private void initSearchField() {
        txtSearchField = new JXSearchField("Search");
        txtSearchField.addActionListener(new ActionListener() {

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
                        dao.tryResetAbort();
                        dao.searchBookmarks2(text == null || (text.isEmpty()) ? null
                                : text.split("\\s"), rowController);
                        if (dao.isAborted())
                            return TBConsts.DAO_ABORT;

                        return TBConsts.DAO_SUCCESS;
                    }
                };
                worker.execute();
            }
        });

        // add to window
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(txtSearchField, gbc);
    }

    private void initFilterPanel() {
        txtFilterField = new JXSearchField("Filter");
        txtFilterField.setSearchMode(JXSearchField.SearchMode.INSTANT);

        txtFilterField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final String text = e.getActionCommand();

                table.setRowFilter(new RowFilter<TableModel, Object>() {
                    @Override
                    public boolean include(
                            RowFilter.Entry<? extends TableModel, ?> entry) {
                        DefaultTableModel model = (DefaultTableModel) entry
                                .getModel();
                        int index = (Integer) entry.getIdentifier();
                        Pattern p = Pattern.compile(text + ".*",
                                Pattern.CASE_INSENSITIVE);

                        Object field;
                        boolean accept = false;
                        for (int i = 1; i < 6; ++i) {
                            field = model.getValueAt(index, i);
                            if (field != null)
                                if (p.matcher(field.toString()).matches())
                                    accept = true;
                        }
                        for (int i = 6; i <= 7; ++i) {
                            field = model.getValueAt(index, i);
                            if (field != null)
                                if (p.matcher(
                                        TBGuiUtils
                                                .formatDate(((Timestamp) field)))
                                        .matches())
                                    accept = true;
                        }
                        return accept;
                    }
                });
            }
        });

        final Action a = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                txtFilterField.setText(null);
                panelFilter.setVisible(false);
                table.requestFocusInWindow();
                /*
                 * font size spinner is taking the focus after hiding the
				 * panelFilter, so this should avoid that
				 */
            }
        };
        txtFilterField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    a.actionPerformed(null);
            }
        });
        JButton btnHideFiler = new JButton(a);
        btnHideFiler.setIcon(TBIcon.get("close"));

        panelFilter = new JPanel();
        panelFilter.setLayout(new GridBagLayout());
        panelFilter.setVisible(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0f;
        panelFilter.add(txtFilterField, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        panelFilter.add(btnHideFiler, gbc);

        // add to window
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        getContentPane().add(panelFilter, gbc);
    }

    private void initFileChooser() {
        fileChooser = TBGuiUtils.getNewFileChooser();
    }

    private JXStatusBar createStatusBar() {
        JXStatusBar statusBar = new JXStatusBar();
        statusBar.setResizeHandleEnabled(false);

        busyLabel = new JXBusyLabel(new Dimension(15, 15));
        busyLabel.setToolTipText("Busy Indicator");
        statusLabel = new JLabel("Ready!");
        statusLabel.setToolTipText("Status Message");
        countLabel = new JLabel("0");
        countLabel.setToolTipText("Number Of Rows");

        JButton btnAbort = new JButton(actionAbort);

        statusBar.add(busyLabel,
                new JXStatusBar.Constraint(busyLabel.getWidth()));
        statusBar.add(statusLabel, new JXStatusBar.Constraint(
                JXStatusBar.Constraint.ResizeBehavior.FILL));
        statusBar.add(btnAbort, new JXStatusBar.Constraint(30));
        statusBar.add(countLabel, new JXStatusBar.Constraint(30));

        return statusBar;
    }

    private PopupMenu createTrayPopupMenu() {
        // Tray PopupMenu
        PopupMenu popupMenuTray = new PopupMenu("Tray Popup Menu");
        MenuItem add = new MenuItem("Add");
        add.addActionListener(actionNewBookmark);
        MenuItem pasteAddress = new MenuItem("Paste Data");
        pasteAddress.addActionListener(actionPasteAddress);
        MenuItem pasteName = new MenuItem("Paste Name");
        pasteName.addActionListener(actionPasteSpecial);
        MenuItem hide = new MenuItem("Hide | Show");
        hide.addActionListener(actionHide);
        MenuItem close = new MenuItem("Close");
        close.addActionListener(actionClose);

        popupMenuTray.add(add);
        popupMenuTray.add(pasteAddress);
        popupMenuTray.add(pasteName);
        popupMenuTray.addSeparator();
        popupMenuTray.add(hide);
        popupMenuTray.add(close);

        return popupMenuTray;
    }

    private void initTrayIcon(PopupMenu popupMenuTray) {
        if (SystemTray.isSupported()) {
            TrayIcon trayIcon = new TrayIcon(TBIcon.get("tray").getImage(),
                    TBText.get("tray.tooltip"), popupMenuTray);
            trayIcon.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (isVisible()) {
                            setVisible(false);
                        } else {
                            setVisible(true);
                        }
                    }
                }
            });
            try {
                SystemTray.getSystemTray().add(trayIcon);
            } catch (HeadlessException | SecurityException | AWTException e) {
                logPanel.error(e.getMessage(), TBError.getMessage("tray.t"));
            }
        }
    }

	/* } Initializing Helper Methods */

	/* Table Helper Methods { */

    private Object[] getTableSelectedRow() {
        int i = table.getSelectedRow();
        return new Object[]{
                tableModel.getValueAt(table.convertRowIndexToModel(i), 0),
                tableModel.getValueAt(table.convertRowIndexToModel(i), 1),
                tableModel.getValueAt(table.convertRowIndexToModel(i), 2),
                tableModel.getValueAt(table.convertRowIndexToModel(i), 3),
                tableModel.getValueAt(table.convertRowIndexToModel(i), 4),
                tableModel.getValueAt(table.convertRowIndexToModel(i), 5),
                tableModel.getValueAt(table.convertRowIndexToModel(i), 6),
                tableModel.getValueAt(table.convertRowIndexToModel(i), 7)};
    }

    private int getTableRow(Long bookmarkId) {
        for (int i = 0; i < tableModel.getRowCount(); ++i) {
            if (tableModel.getValueAt(i, 0).equals(bookmarkId)) {
                return table.convertRowIndexToView(i);
            }
        }
        return -1;
    }

    private Long getBookmarkId(int tableRowIndex) {
        return (Long) tableModel.getValueAt(
                table.convertRowIndexToModel(tableRowIndex), 0);
    }

//    private void setTableFont(TableFont tf) {
//        table.setFont(tf.getFont());
//        table.setRowHeight(tf.getRowHeight());
//        tableFontSizeSpinner.getModel().setValue(tf);
//    }
//
//    private void setTableFont(Integer size) {
//        switch (size) {
//            case 2:
//                setTableFont(tableFontBig);
//                break;
//            case 3:
//                setTableFont(tableFontBigger);
//                break;
//            case 1:
//            default:
//                setTableFont(tableFontNormal);
//        }
//    }

	/* } Table Helper Methods */

	/* App State Helper Methods { */

    private void setStatusMessage(boolean error, String message) {
        if (error) {
            statusLabel.setForeground(Color.RED);
        } else {
            statusLabel.setForeground(Color.BLACK);
        }
        statusLabel.setText(message);
    }

    private void setBusy(boolean state, boolean error, String message) {
        txtSearchField.setEditable(!state);
        txtFilterField.setEditable(!state);
        actionSearch.setEnabled(state);
        actionFilter.setEnabled(state);
        if (state) {
            readOnlyPreState = readOnly;
            setReadOnly(true);
        } else {
            setReadOnly(readOnlyPreState);
        }
        busyLabel.setBusy(state);
        setStatusMessage(error, message);
    }

    private void setReadOnly(boolean state) {
        readOnly = state;
//        actionReadOnly.putValue(
//                Action.SMALL_ICON,
//                readOnly ? TBIcon.get("readonly.true") : TBIcon
//                        .get("readonly.false"));
//        actionReadOnly.putValue(Action.SELECTED_KEY, readOnly);

        tagManager.setReadOnly(readOnly);

        updateActionsState();
    }

    private void updateSelectionState() {
        int[] selected = table.getSelectedRows();
        if (selected.length == 0) {
            entriesSelected = false;
            entrySelected = false;
        } else if (selected.length > 1) {
            entriesSelected = true;
            entrySelected = true;
        } else {
            entriesSelected = false;
            entrySelected = true;
        }
    }

    private void updateActionsState() {

        boolean state = dbLoaded;
        actionDbBackup.setEnabled(state);
        actionDbRefresh.setEnabled(state);
        actionDbClose.setEnabled(state);

        actionExport.setEnabled(state);

        actionSearch.setEnabled(state);
        txtSearchField.setEnabled(state);
        actionFilter.setEnabled(state);
        txtFilterField.setEnabled(state);

        actionViewTagManager.setEnabled(state);
        actionViewStats.setEnabled(state);
//        actionReadOnly.setEnabled(state);

        state = dbLoaded && !readOnly;
        actionDbRestore.setEnabled(state);

        actionImport.setEnabled(state);

        actionNewBookmark.setEnabled(state);
        actionPasteSpecial.setEnabled(state);
        actionPasteAddress.setEnabled(state);

        updateSelectionState();

        state = entrySelected && !entriesSelected && !readOnly;
        actionEditBookmark.setEnabled(state);

        state = entrySelected && !entriesSelected;
        actionCopyName.setEnabled(state);
        actionCopyDesc.setEnabled(state);
        actionCopyAddress.setEnabled(state);
        actionCopyNotes.setEnabled(state);
        actionCopyTags.setEnabled(state);
        actionCopyDateadded.setEnabled(state);
        actionCopyDatemodified.setEnabled(state);

        state = entrySelected;
        actionHideBookmark.setEnabled(state);

        state = entrySelected && !readOnly;
        actionDeleteBookmark.setEnabled(state);
        actionTagBookmark.setEnabled(state);
    }

    private void updateOpenHistoryMenu() {
        openHistoryMenu.removeAll();
        if (openHistory.isEmpty()) {
            openHistoryMenu.add(new JMenuItem("empty!"));
        } else {
            for (final String path : openHistory.getPathsArrayReversed()) {
                Action a = new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        TBWorker worker = new TBWorker() {

                            @Override
                            protected void doneWithSuccess() {
                                setBusy(false, false, TBText.get("op.loadDb"));
                            }

                            @Override
                            protected void doneWithError() {
                                setBusy(false, true,
                                        TBError.getMessage("op.loadDb"));
                            }

                            @Override
                            protected void doneWithAbort() {
                                setBusy(false, false,
                                        TBText.get("op.loadDb.abort"));
                            }

                            @Override
                            protected void alwaysDoWhenDone() {
                                actionAbort.setEnabled(false);
                            }

                            @Override
                            protected int doThis() {
                                if (loadDb(path))
                                    return TBConsts.DAO_SUCCESS;
                                else
                                    return TBConsts.DAO_ERROR;
                            }
                        };
                        actionAbort.setEnabled(true);
                        worker.execute();
                    }
                };
                TBGuiUtils.initializeAction(a, FilenameUtils.getBaseName(path),
                        path, null, TBIcon.get("database"), null);
                openHistoryMenu.add(a);
            }
            openHistoryMenu.add(actionClearOpenHistory);
        }
    }

    private void closeApp(boolean confirm) {
        if (confirmExit
                && confirm
                && JOptionPane.showConfirmDialog(this, TBText.get("close.m"),
                TBText.get("close.t"), JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION
                || !confirm || !confirmExit) {
            // close log
            if (logPanel.hasLogged()
                    && JOptionPane.showConfirmDialog(this,
                    TBText.get("main.saveLog.m"),
                    TBText.get("main.saveLog.t"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                logPanel.invokeSaveAction();
            }
            logPanel.dispose();

            saveConfigs();

            // close dao
            dao.close();

            // exit
            System.exit(0);
        }
    }

	/* } App State Helper Methods */

    /* Configs Helper Methods { */
    private void saveConfigs() {
        Preferences prefs = Preferences.userRoot().node(TBText.get("prefs"));

        prefs.putBoolean("searchInstantly", searchInstantly);

        prefs.putBoolean("readonly", readOnly);

        prefs.putBoolean("miniToTray", miniToTray);

        prefs.putBoolean("useHyperlinks", useHyperlinks);

        prefs.putBoolean("confirmExit", confirmExit);

        prefs.put("openHistory", openHistory.toString());

        // font size
//        prefs.putInt("fontSize",
//                ((TableFont) tableFontSizeSpinner.getValue()).getId());

        // table columns
        TableColumnExt[] cols = new TableColumnExt[]{
                table.getColumnExt(TBText.get("table.col.id")),
                table.getColumnExt(TBText.get("table.col.name")),
                table.getColumnExt(TBText.get("table.col.desc")),
                table.getColumnExt(TBText.get("table.col.address")),
                table.getColumnExt(TBText.get("table.col.notes")),
                table.getColumnExt(TBText.get("table.col.tags")),
                table.getColumnExt(TBText.get("table.col.dateadded")),
                table.getColumnExt(TBText.get("table.col.datemodified"))};
        // table columns visibility
        prefs.putBoolean("idv", cols[0].isVisible());
        prefs.putBoolean("namev", cols[1].isVisible());
        prefs.putBoolean("descv", cols[2].isVisible());
        prefs.putBoolean("addressv", cols[3].isVisible());
        prefs.putBoolean("notesv", cols[4].isVisible());
        prefs.putBoolean("tagsv", cols[5].isVisible());
        prefs.putBoolean("dateaddedv", cols[6].isVisible());
        prefs.putBoolean("datemodifiedv", cols[7].isVisible());

        // table columns sequence
        // 1st all columns must be visible
        cols[0].setVisible(true);
        cols[1].setVisible(true);
        cols[2].setVisible(true);
        cols[3].setVisible(true);
        cols[4].setVisible(true);
        cols[5].setVisible(true);
        cols[6].setVisible(true);
        cols[7].setVisible(true);

        prefs.putInt("idi", table.convertColumnIndexToView(0));
        prefs.putInt("namei", table.convertColumnIndexToView(1));
        prefs.putInt("desci", table.convertColumnIndexToView(2));
        prefs.putInt("addressi", table.convertColumnIndexToView(3));
        prefs.putInt("notesi", table.convertColumnIndexToView(4));
        prefs.putInt("tagsi", table.convertColumnIndexToView(5));
        prefs.putInt("dateaddedi", table.convertColumnIndexToView(6));
        prefs.putInt("datemodifiedi", table.convertColumnIndexToView(7));

        try {
            prefs.flush();
        } catch (BackingStoreException ignored) {
        }
    }

    private void loadConfigs(String arg) {
        Preferences prefs = Preferences.userRoot().node(TBText.get("prefs"));

        if (arg != null)
            if (dao.loadDb(arg)) {
                dao.getBookmarks(rowController);
                dbLoaded = true;
            } else
                dbLoaded = false;
        else
            dbLoaded = false;
        updateActionsState();

        setSearchInstantly(prefs.getBoolean("searchInstantly", true));

        // read only state
        // readOnly depends on entry selection state
        // so, must initialize individual values first
        readOnly = prefs.getBoolean("readonly", true);
        entriesSelected = false;
        entrySelected = false;
        setReadOnly(readOnly);

        miniToTray = prefs.getBoolean("miniToTray", false);

        useHyperlinks = prefs.getBoolean("useHyperlinks",
                TBConsts.IsDesktopSupported);

        confirmExit = prefs.getBoolean("confirmExit", true);

        openHistory.addPaths(prefs.get("openHistory", "").split(";"));
        updateOpenHistoryMenu();

        // font size
//        setTableFont(prefs.getInt("fontSize", 1));

        // table columns
        TableColumnExt[] cols = new TableColumnExt[]{
                table.getColumnExt(TBText.get("table.col.id")),
                table.getColumnExt(TBText.get("table.col.name")),
                table.getColumnExt(TBText.get("table.col.desc")),
                table.getColumnExt(TBText.get("table.col.address")),
                table.getColumnExt(TBText.get("table.col.notes")),
                table.getColumnExt(TBText.get("table.col.tags")),
                table.getColumnExt(TBText.get("table.col.dateadded")),
                table.getColumnExt(TBText.get("table.col.datemodified"))};
        // table columns sequence
        // 1st all columns must be visible
        cols[0].setVisible(true);
        cols[1].setVisible(true);
        cols[2].setVisible(true);
        cols[3].setVisible(true);
        cols[4].setVisible(true);
        cols[5].setVisible(true);
        cols[6].setVisible(true);
        cols[7].setVisible(true);

        table.moveColumn(table.convertColumnIndexToView(0),
                prefs.getInt("idi", 0));
        table.moveColumn(table.convertColumnIndexToView(1),
                prefs.getInt("namei", 1));
        table.moveColumn(table.convertColumnIndexToView(2),
                prefs.getInt("desci", 2));
        table.moveColumn(table.convertColumnIndexToView(3),
                prefs.getInt("addressi", 3));
        table.moveColumn(table.convertColumnIndexToView(4),
                prefs.getInt("notesi", 4));
        table.moveColumn(table.convertColumnIndexToView(5),
                prefs.getInt("tagsi", 5));
        table.moveColumn(table.convertColumnIndexToView(6),
                prefs.getInt("dateaddedi", 6));
        table.moveColumn(table.convertColumnIndexToView(7),
                prefs.getInt("datemodifiedi", 7));

        // table columns visibility
        cols[0].setVisible(prefs.getBoolean("idv", false));
        cols[1].setVisible(prefs.getBoolean("namev", true));
        cols[2].setVisible(prefs.getBoolean("descv", true));
        cols[3].setVisible(prefs.getBoolean("addressv", true));
        cols[4].setVisible(prefs.getBoolean("notesv", false));
        cols[5].setVisible(prefs.getBoolean("tagsv", true));
        cols[6].setVisible(prefs.getBoolean("dateaddedv", false));
        cols[7].setVisible(prefs.getBoolean("datemodifiedv", false));
    }

    private void setSearchInstantly(boolean state) {
        searchInstantly = state;
        txtSearchField
                .setSearchMode(searchInstantly ? JXSearchField.SearchMode.INSTANT
                        : JXSearchField.SearchMode.REGULAR);
    }

	/* } Configs Helper Methosd */

    private void showNewBookmarkDialog(String name, String address,
                                       String description, String notes) {
        EditDialog editgui = new EditDialog(this, true, dao,
                TBConsts.EDIT_INSERT);
        editgui.setNameText(name);
        editgui.setAddressText(address);
        editgui.setDescriptionText(description);
        editgui.setNotesText(notes);
        editgui.setVisible(true);
        BookmarkI bkmrk = editgui.getBookmark();
        if (bkmrk != null) {
            if (dao.insertUpdateBookmark(bkmrk)) {
                int rowId = 0;
                switch (editgui.getMethodType()) {
                    case TBConsts.EDIT_UPDATE:
                        setStatusMessage(false, TBText.get("op.update"));
                        rowId = getTableRow(bkmrk.getId());
                        rowController.updateRow(rowId, bkmrk.toArray(
                                TBConsts.ADDRESS_AS_URI, TBConsts.TAGS_AS_STRING));
                        break;
                    case TBConsts.EDIT_INSERT:
                        setStatusMessage(false, TBText.get("op.insert"));
                        rowController.insertRow(
                                0,
                                dao.getBookmark(dao.getIdLastInsertUpdate())
                                        .toArray(TBConsts.ADDRESS_AS_URI,
                                                TBConsts.TAGS_AS_STRING));
                        break;
                }
                table.changeSelection(rowId, 0, false, false);
            } else {
                setStatusMessage(true, TBError.getMessage("op.insert"));
            }
        }
        editgui.dispose();
    }

    private boolean loadDb(String path) {
        dao.close();
        rowController.clearRows();
        if (dao.loadDb(path)) {
            dao.getBookmarks(rowController);
            dbLoaded = true;
            updateActionsState();
            openHistory.addPath(dao.getDbPath());
            updateOpenHistoryMenu();
            return true;
        } else {
            return false;
        }
    }
}

//class TableFont {
//
//    private Font font;
//    private int rowHeight;
//    private Integer id;
//    private static final List<Integer> ids = new ArrayList<>();
//
//    public TableFont(int id) {
//        this(id, null, 0);
//    }
//
//    public TableFont(Integer id, Font font, int rowHeight) {
//        setId(id);
//        this.font = font;
//        this.rowHeight = rowHeight;
//    }
//
//    public Integer getId() {
//        return id;
//    }
//
//    public Font getFont() {
//        return font;
//    }
//
//    public int getRowHeight() {
//        return rowHeight;
//    }
//
//    final void setId(Integer id) {
//        if (ids.contains(id)) {
//            throw new IllegalArgumentException("id '" + id
//                    + "' is already used");
//        }
//        this.id = id;
//        ids.add(id);
//    }
//
//    public void setFont(Font font) {
//        this.font = font;
//    }
//
//    public void setRowHeight(int rowHeight) {
//        this.rowHeight = rowHeight;
//    }
//
//    @Override
//    public String toString() {
//        return "" + id;
//    }
//}
