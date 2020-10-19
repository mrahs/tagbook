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

import org.jdesktop.swingx.JXHyperlink;
import pw.ahs.app.tagbook.core.utils.TBConsts;
import pw.ahs.app.tagbook.core.utils.TBIOUtils;
import pw.ahs.app.tagbook.core.utils.TBText;
import pw.ahs.app.tagbook.gui.utils.TBIcon;
import pw.ahs.app.tagbook.gui.utils.TBKeyStroke;
import pw.ahs.app.tagbook.gui.utils.TextDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class AboutDialog extends JDialog {

    private static final long serialVersionUID = -172475364576340937L;

    private JXHyperlink linkDeveloper;
    private JXHyperlink linkH2db;
    private JXHyperlink linkSwingx;
    private JXHyperlink linkJsoup;
    private JXHyperlink linkOpencsv;
    private JXHyperlink linkCommonsValidator;
    private JXHyperlink linkCommonsLang;
    private JXHyperlink linkCommonsIO;
    private JXHyperlink linkIcons;
    private JXHyperlink linkLicense;
    private JXHyperlink linkHome;

    public AboutDialog(Frame parent) {
        super(parent, true);
        initDialog();
        initLinks();
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initDialog() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(TBText.get("about.title"));
        setIconImage(TBIcon.get("info").getImage());
        setResizable(false);

        getRootPane().registerKeyboardAction(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dispatchEvent(new WindowEvent(AboutDialog.this,
                                WindowEvent.WINDOW_CLOSING));
                    }
                }, TBKeyStroke.get("cancel"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    @SuppressWarnings("serial")
    private void initLinks() {
        linkDeveloper = new JXHyperlink();
        linkH2db = new JXHyperlink();
        linkSwingx = new JXHyperlink();
        linkJsoup = new JXHyperlink();
        linkOpencsv = new JXHyperlink();
        linkCommonsValidator = new JXHyperlink();
        linkCommonsIO = new JXHyperlink();
        linkCommonsLang = new JXHyperlink();
        linkIcons = new JXHyperlink();
        linkHome = new JXHyperlink();
        final String swingxUrl = "http://java.net/projects/swingx/";
        final String h2dbUrl = "http://www.h2database.com/";
        final String jsoupUrl = "http://jsoup.org/";
        final String opencsvUrl = "http://opencsv.sourceforge.net/";
        final String commonsValidatorUrl = "https://commons.apache.org/validator/";
        final String commonsIoUrl = "https://commons.apache.org/io/";
        final String commonsLangUrl = "https://commons.apache.org/lang/";
        final String iconsUrl = "http://www.customicondesign.com/free-icons/pretty-office-icon-set";
        if (TBConsts.IsDesktopSupported)
            try {
                linkDeveloper.setURI(new URI(TBConsts.DEV_HOME));
                linkSwingx.setURI(new URI(swingxUrl));
                linkH2db.setURI(new URI(h2dbUrl));
                linkJsoup.setURI(new URI(jsoupUrl));
                linkOpencsv.setURI(new URI(opencsvUrl));
                linkCommonsValidator.setURI(new URI(commonsValidatorUrl));
                linkCommonsIO.setURI(new URI(commonsIoUrl));
                linkCommonsLang.setURI(new URI(commonsLangUrl));
                linkIcons.setURI(new URI(iconsUrl));
                linkHome.setURI(new URI(TBConsts.APP_HOME));
            } catch (URISyntaxException ignored) {
            }
        else {
            Action a = new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String url = null;
                    if (e.getSource() == linkDeveloper) {
                        url = TBConsts.DEV_HOME;
                    } else if (e.getSource() == linkSwingx) {
                        url = swingxUrl;
                    } else if (e.getSource() == linkH2db) {
                        url = h2dbUrl;
                    } else if (e.getSource() == linkJsoup) {
                        url = jsoupUrl;
                    } else if (e.getSource() == linkOpencsv) {
                        url = opencsvUrl;
                    } else if (e.getSource() == linkCommonsIO) {
                        url = commonsIoUrl;
                    } else if (e.getSource() == linkCommonsLang) {
                        url = commonsLangUrl;
                    } else if (e.getSource() == linkCommonsValidator) {
                        url = commonsValidatorUrl;
                    } else if (e.getSource() == linkIcons) {
                        url = iconsUrl;
                    } else if (e.getSource() == linkHome) {
                        url = TBConsts.APP_HOME;
                    }

                    if (url != null) {
                        String message;
                        if (TBIOUtils.copyTextToClipboard(url))
                            message = "link copied to clipboard";
                        else
                            message = "could not copy link to clipboard";

                        JOptionPane
                                .showMessageDialog(AboutDialog.this, message);
                    }
                }
            };
            linkDeveloper.setAction(a);
            linkSwingx.setAction(a);
            linkH2db.setAction(a);
            linkJsoup.setAction(a);
            linkOpencsv.setAction(a);
            linkCommonsIO.setAction(a);
            linkCommonsLang.setAction(a);
            linkCommonsValidator.setAction(a);
            linkIcons.setAction(a);
            linkHome.setAction(a);
        }

        linkDeveloper.setText("Anas H. Sulaiman");
        linkDeveloper.setToolTipText(TBConsts.DEV_HOME);

        linkH2db.setText("H2 Database");
        linkH2db.setToolTipText(h2dbUrl);

        linkSwingx.setText("SwingX");
        linkSwingx.setToolTipText(swingxUrl);

        linkJsoup.setText("Jsoup");
        linkJsoup.setToolTipText(jsoupUrl);

        linkOpencsv.setText("OpenCSV");
        linkOpencsv.setToolTipText(opencsvUrl);

        linkCommonsValidator.setText("Commons Validator");
        linkCommonsValidator.setToolTipText(commonsValidatorUrl);

        linkCommonsIO.setText("Commons IO");
        linkCommonsIO.setToolTipText(commonsIoUrl);

        linkCommonsLang.setText("Commons Lang");
        linkCommonsLang.setToolTipText(commonsLangUrl);

        linkIcons.setText("CustomOfficeDesign");
        linkIcons.setToolTipText(iconsUrl);

        linkHome.setText(TBConsts.APP_TITLE);
        linkHome.setToolTipText(TBConsts.APP_HOME);

        linkLicense = new JXHyperlink(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                InputStream licenseStream = getClass().getResourceAsStream(
                        "/res/license");
                if (licenseStream != null) {
                    TextDialog td = new TextDialog(AboutDialog.this, true);
                    td.setText(TBIOUtils.readTextFromStream(licenseStream));
                    td.setVisible(true);
                    td.dispose();
                } else
                    JOptionPane.showMessageDialog(AboutDialog.this,
                            "license file was not found", "404!",
                            JOptionPane.ERROR_MESSAGE);
            }
        });
        linkLicense.setText("GPLv3");
    }

    private void initComponents() {
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        Insets leftRightMargin = new Insets(0, 10, 0, 5);
        Insets topMargin = new Insets(10, 0, 0, 0);
        Insets bottomMargin = new Insets(0, 0, 10, 0);

        Font fontTitle = new Font(Font.SANS_SERIF, Font.BOLD, 13);
        Font fontVal = new Font(Font.DIALOG, Font.PLAIN, 13);

        JLabel lblHeader = new JLabel();
        JLabel lblVersionTitle = new JLabel();
        JLabel lblVersionValue = new JLabel();
        JLabel lblJavaVersionTitle = new JLabel();
        JLabel lblJavaVersionValue = new JLabel();
        JLabel lblSystemTitle = new JLabel();
        JLabel lblSystemValue = new JLabel();
        JLabel lblDeveloperTitle = new JLabel();
        JLabel lblIconsBy = new JLabel();
        JLabel lblLicense = new JLabel();
        JLabel lblHome = new JLabel();
        JLabel lblUsing = new JLabel();

        lblHeader.setFont(new Font(Font.SERIF, Font.BOLD, 18));
        lblHeader.setText(TBText.get("about.header"));

        lblVersionTitle.setFont(fontTitle);
        lblVersionTitle.setText("Version:");
        lblVersionTitle.setLabelFor(lblVersionValue);

        lblVersionValue.setFont(fontVal);
        lblVersionValue.setText(TBConsts.APP_VERSION);

        lblJavaVersionTitle.setFont(fontTitle);
        lblJavaVersionTitle.setText("Java:");
        lblJavaVersionTitle.setLabelFor(lblJavaVersionValue);

        lblJavaVersionValue.setFont(fontVal);
        lblJavaVersionValue.setText(System.getProperty("java.version") + " by "
                + System.getProperty("java.vendor"));

        lblSystemTitle.setFont(fontTitle);
        lblSystemTitle.setText("System:");
        lblSystemTitle.setLabelFor(lblSystemValue);

        lblSystemValue.setFont(fontVal);
        lblSystemValue.setText(System.getProperty("os.name") + " version "
                + System.getProperty("os.version") + " running on "
                + System.getProperty("os.arch"));

        lblDeveloperTitle.setFont(fontTitle);
        lblDeveloperTitle.setText("Developed By:");
        lblDeveloperTitle.setLabelFor(linkDeveloper);

        lblUsing.setFont(fontTitle);
        lblUsing.setText("Using:");
        lblUsing.setLabelFor(linkSwingx);

        lblIconsBy.setFont(fontTitle);
        lblIconsBy.setText("Icons by:");
        lblIconsBy.setLabelFor(linkIcons);

        lblLicense.setFont(fontTitle);
        lblLicense.setText("License:");
        lblLicense.setLabelFor(linkLicense);

        lblHome.setFont(fontTitle);
        lblHome.setText("Home page:");
        lblHome.setLabelFor(linkHome);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1f;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.insets = new Insets(topMargin.top, 0, bottomMargin.bottom, 0);
        getContentPane().add(lblHeader, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = leftRightMargin;
        getContentPane().add(lblVersionTitle, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = leftRightMargin;
        getContentPane().add(lblVersionValue, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = leftRightMargin;
        getContentPane().add(lblJavaVersionTitle, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = leftRightMargin;
        getContentPane().add(lblJavaVersionValue, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = leftRightMargin;
        getContentPane().add(lblSystemTitle, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = leftRightMargin;
        getContentPane().add(lblSystemValue, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = leftRightMargin;
        getContentPane().add(lblDeveloperTitle, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = leftRightMargin;
        getContentPane().add(linkDeveloper, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = leftRightMargin;
        getContentPane().add(lblIconsBy, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = leftRightMargin;
        getContentPane().add(linkIcons, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = leftRightMargin;
        getContentPane().add(lblLicense, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = leftRightMargin;
        getContentPane().add(linkLicense, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = leftRightMargin;
        getContentPane().add(lblHome, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = leftRightMargin;
        getContentPane().add(linkHome, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = leftRightMargin;
        getContentPane().add(lblUsing, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = leftRightMargin;
        getContentPane().add(linkSwingx, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = leftRightMargin;
        getContentPane().add(linkH2db, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = leftRightMargin;
        getContentPane().add(linkJsoup, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = leftRightMargin;
        getContentPane().add(linkOpencsv, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 12;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = leftRightMargin;
        getContentPane().add(linkCommonsLang, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 13;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = leftRightMargin;
        getContentPane().add(linkCommonsIO, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 14;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(0, leftRightMargin.left, bottomMargin.bottom,
                leftRightMargin.right);
        getContentPane().add(linkCommonsValidator, gbc);
    }

    public static void main(String[] args) {
        new AboutDialog(null).setVisible(true);
    }
}
