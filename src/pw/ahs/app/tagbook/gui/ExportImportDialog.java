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
import pw.ahs.app.tagbook.core.utils.TBIOUtils;
import pw.ahs.app.tagbook.core.utils.TBText;
import pw.ahs.app.tagbook.gui.utils.TBGuiUtils;
import pw.ahs.app.tagbook.gui.utils.TBIcon;
import pw.ahs.app.tagbook.gui.utils.TBKeyStroke;
import pw.ahs.app.tagbook.gui.utils.TBMnemonic;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

@SuppressWarnings("serial")
public class ExportImportDialog extends JDialog {
    private JLabel lblTitle;
    private JPanel panelExport;
    private JPanel panelImport;
    private JRadioButton rbCsv;
    private JRadioButton rbXml;
    private JRadioButton rbDelicious;
    private JRadioButton rbDiigo;
    private JRadioButton rbFirefox;
    private JRadioButton rbIe;
    private JRadioButton rbEverything;
    private JRadioButton rbSelected;
    private JRadioButton rbMerge;
    private JRadioButton rbReplace;
    private JRadioButton rbMnR;
    private JRadioButton rbNone;
    private JTextField txtPath;
    private JFileChooser fileChooser;
    private TagDialog tagdialog;
    private Action saveAction;
    private Action cancelAction;
    // data variables
    private int methodType;
    private boolean accept;

    public ExportImportDialog(Frame parent, int methodType) {
        super(parent, true);
        initGui();

        // initialize data
        setMethodType(methodType);
        this.accept = false;

        // center dialog
        setLocationRelativeTo(parent);
    }

    private void initGui() {
        // initialize dialog
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(TBText.get("exportImportDialog.t"));
        getContentPane().setLayout(new GridBagLayout());

        // intialize title
        lblTitle = new JLabel();
        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14)); // NOI18N
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        getContentPane().add(lblTitle, gridBagConstraints);

        tagdialog = new TagDialog(this);

        // initialize thigs
        initActions();
        initRadioButtons();
        initPathComponents();
        initDiaolgButtons();
        initKeyboardShortcuts();

        // pack things
        pack();
    }

    private void initActions() {
        saveAction = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                accept = true;
                dispose();
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
    }

    private void initRadioButtons() {
        // create radio buttons action listener
        ActionListener radioButtonsActionListenr = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                updateSaveActionState();
            }
        };
        // initialize export method group
        ButtonGroup bgExport = new ButtonGroup();
        rbEverything = new JRadioButton();
        rbSelected = new JRadioButton();

        rbEverything.setText("Everything");
        rbEverything.setMnemonic('e');
        rbEverything.addActionListener(radioButtonsActionListenr);
        rbEverything.setSelected(true);

        rbSelected.setText("Selected");
        rbSelected.setMnemonic('s');
        rbSelected.addActionListener(radioButtonsActionListenr);

        bgExport.add(rbEverything);
        bgExport.add(rbSelected);

        // initialize import group
        ButtonGroup bgImport = new ButtonGroup();
        rbMerge = new JRadioButton();
        rbReplace = new JRadioButton();
        rbMnR = new JRadioButton();
        rbNone = new JRadioButton();

        rbMerge.setText("Merge tags and notes");
        rbMerge.setMnemonic('m');

        rbReplace.setText("Replace all fields");
        rbReplace.setMnemonic('r');

        rbMnR.setText("Merge tags and replace other fields");
        rbMnR.setMnemonic('e');

        rbNone.setText("Do not import");
        rbNone.setMnemonic('n');
        rbNone.setSelected(true);

        bgImport.add(rbMerge);
        bgImport.add(rbReplace);
        bgImport.add(rbMnR);
        bgImport.add(rbNone);

        // initialize format group
        ButtonGroup bgFormat = new ButtonGroup();
        rbCsv = new JRadioButton();
        rbXml = new JRadioButton();
        rbFirefox = new JRadioButton();
        rbIe = new JRadioButton();
        rbDelicious = new JRadioButton();
        rbDiigo = new JRadioButton();

        rbCsv.setText("CSV");
        rbCsv.setMnemonic('c');
        rbCsv.addActionListener(radioButtonsActionListenr);
        rbCsv.setSelected(true);

        rbXml.setText("XML");
        rbXml.setMnemonic('x');
        rbXml.addActionListener(radioButtonsActionListenr);

        rbFirefox.setText("Firefox");
        rbFirefox.setMnemonic('f');
        rbFirefox.addActionListener(radioButtonsActionListenr);

        rbIe.setText("IE");
        rbIe.setMnemonic('i');
        rbIe.addActionListener(radioButtonsActionListenr);

        rbDelicious.setText("Delicious");
        rbDelicious.setMnemonic('d');
        rbDelicious.addActionListener(radioButtonsActionListenr);

        rbDiigo.setText("Diigo");
        rbDiigo.setMnemonic('g');
        rbDiigo.addActionListener(radioButtonsActionListenr);

        bgFormat.add(rbCsv);
        bgFormat.add(rbXml);
        bgFormat.add(rbFirefox);
        bgFormat.add(rbIe);
        bgFormat.add(rbDelicious);
        bgFormat.add(rbDiigo);

        // add to layout
        GridBagConstraints gbc;

        // export group
        panelExport = new JPanel();
        panelExport.setBorder(BorderFactory.createTitledBorder("Export"));
        panelExport.setLayout(new GridBagLayout());

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        panelExport.add(rbEverything, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        panelExport.add(rbSelected, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        getContentPane().add(panelExport, gbc);

        // import group
        panelImport = new JPanel();
        panelImport.setBorder(BorderFactory
                .createTitledBorder("If one already exists"));
        panelImport.setLayout(new GridBagLayout());

        JButton btnImportedTags = new JButton(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                tagdialog.setVisible(true);
            }
        });
        btnImportedTags.setText("Tag imported with");
        btnImportedTags.setMnemonic('t');

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        panelImport.add(rbNone, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        panelImport.add(rbMerge, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        panelImport.add(rbReplace, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_START;
        panelImport.add(rbMnR, gbc);


        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        panelImport.add(btnImportedTags, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        getContentPane().add(panelImport, gbc);

        // format group
        JPanel panelFormat = new JPanel();
        panelFormat.setBorder(BorderFactory.createTitledBorder("Format"));
        panelFormat.setLayout(new GridBagLayout());

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        panelFormat.add(rbCsv, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        panelFormat.add(rbXml, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        panelFormat.add(rbFirefox, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        panelFormat.add(rbIe, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        panelFormat.add(rbDelicious, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        panelFormat.add(rbDiigo, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        getContentPane().add(panelFormat, gbc);
    }

    private void initPathComponents() {
        // allocate
        txtPath = new JTextField();
        JButton buttonBrowse = new JButton();
        fileChooser = TBGuiUtils.getNewFileChooser();

        // initialize
        txtPath.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSaveActionState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSaveActionState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSaveActionState();
            }
        });
        txtPath.setText(FileSystemView.getFileSystemView()
                .getDefaultDirectory().getAbsolutePath());

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Choose File");

        buttonBrowse.setText("Browse");
        buttonBrowse.setMnemonic('r');
        buttonBrowse.setIcon(TBIcon.get("browse"));
        buttonBrowse.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                fileChooser.setCurrentDirectory(new File(txtPath.getText()));
                configureFileFilters();
                int i;
                if (methodType == TBConsts.EXPORT) {
                    i = fileChooser.showSaveDialog(ExportImportDialog.this);
                } else {
                    i = fileChooser.showOpenDialog(ExportImportDialog.this);
                }
                if (i == JFileChooser.APPROVE_OPTION) {
                    txtPath.setText(fileChooser.getSelectedFile()
                            .getAbsolutePath());
                }
            }
        });

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(txtPath, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        getContentPane().add(buttonBrowse, gridBagConstraints);
    }

    private void initDiaolgButtons() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(new JButton(saveAction));
        buttonsPanel.add(new JButton(cancelAction));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        getContentPane().add(buttonsPanel, gridBagConstraints);
    }

    private void initKeyboardShortcuts() {
        getRootPane().registerKeyboardAction(saveAction,
                TBKeyStroke.get("save"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        getRootPane().registerKeyboardAction(cancelAction,
                TBKeyStroke.get("cancel"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public int getMethodType() {
        return methodType;
    }

    public int getFormat() {
        if (rbCsv.isSelected())
            return TBConsts.FORMAT_CSV;

        if (rbXml.isSelected())
            return TBConsts.FORMAT_XML;

        if (rbFirefox.isSelected())
            return TBConsts.FORMAT_FIREFOX;

        if (rbDelicious.isSelected())
            return TBConsts.FORMAT_DELICIOUS;

        if (rbDiigo.isSelected())
            return TBConsts.FORMAT_DIIGO;

        if (rbIe.isSelected())
            return TBConsts.FORMAT_IE;

        return -1;
    }

    public int getConflictsMethod() {
        if (rbMerge.isSelected())
            return TBConsts.CONFL_MERGE;
        if (rbReplace.isSelected())
            return TBConsts.CONFL_REPLACE;
        if (rbMnR.isSelected())
            return TBConsts.CONFL_MNR;
        if (rbNone.isSelected())
            return TBConsts.CONFL_NONE;
        return -1;
    }

    public int getExportMethod() {
        if (rbEverything.isSelected()) {
            return TBConsts.EXPORT_ALL;
        }
        if (rbSelected.isSelected()) {
            return TBConsts.EXPORT_SELECTED;
        }
        return -1;
    }

    public String[] getImportedTags() {
        if (methodType != TBConsts.IMPORT)
            return null;
        return tagdialog.getTags();
    }

    File getSelectedFile() {
        if (accept) {
            return new File(txtPath.getText());
        } else {
            return null;
        }
    }

    final void setMethodType(int methodType) {
        if (methodType == TBConsts.IMPORT || methodType == TBConsts.EXPORT) {
            this.methodType = methodType;
        } else {
            this.methodType = TBConsts.EXPORT;
        }
    }

    public File showExportDialog() {
        methodType = TBConsts.EXPORT;
        setVisible(true);
        return getSelectedFile();
    }

    public File showImportDialog() {
        methodType = TBConsts.IMPORT;
        setVisible(true);
        return getSelectedFile();
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            configureToMethodType();
        }
        super.setVisible(b);
    }

    private void configureFileFilters() {
        TBGuiUtils.removeFileChooserFileFilters(fileChooser);
        switch (getFormat()) {
            case TBConsts.FORMAT_CSV:
            case TBConsts.FORMAT_DIIGO:
                fileChooser.setFileFilter(TBGuiUtils.csvFileFilter);
                break;
            case TBConsts.FORMAT_XML:
                fileChooser.setFileFilter(TBGuiUtils.xmlFileFilter);
                break;
            case TBConsts.FORMAT_DELICIOUS:
            case TBConsts.FORMAT_FIREFOX:
            case TBConsts.FORMAT_IE:
                fileChooser.setFileFilter(TBGuiUtils.htmlFileFilter);
                break;
        }
    }

    private void updateSaveActionState() {
        File selectedFile = new File(txtPath.getText());
        boolean state = false;
        switch (methodType) {
            case TBConsts.IMPORT:
                state = TBIOUtils.setFileReadable(selectedFile);
                break;
            case TBConsts.EXPORT:
                state = TBIOUtils.setFileWritable(selectedFile, false);
                break;
        }
        state = state
                && FilenameUtils.isExtension(txtPath.getText(),
                TBConsts.EXPORT_IMPORT_FORMATS.split(";"));
        saveAction.setEnabled(state);
    }

    private void configureToMethodType() {
        switch (methodType) {
            case TBConsts.EXPORT:
                lblTitle.setText(TBText.get("exportDialog.t"));
                panelExport.setVisible(true);
                panelImport.setVisible(false);
                saveAction.putValue(Action.NAME, "Save");
                saveAction.putValue(Action.SHORT_DESCRIPTION, "Save");
                saveAction.putValue((Action.SMALL_ICON), TBIcon.get("save"));
                setIconImage(TBIcon.get("export").getImage());
                break;
            case TBConsts.IMPORT:
                lblTitle.setText(TBText.get("importDialog.t"));
                panelExport.setVisible(false);
                panelImport.setVisible(true);
                saveAction.putValue(Action.NAME, "Open");
                saveAction.putValue(Action.SHORT_DESCRIPTION, "Open");
                saveAction.putValue((Action.SMALL_ICON), TBIcon.get("open"));
                setIconImage(TBIcon.get("import").getImage());
                break;
        }
        updateSaveActionState();
        pack();
    }

    public static void main(String[] args) {
        new ExportImportDialog(null, TBConsts.IMPORT).setVisible(true);
    }
}
