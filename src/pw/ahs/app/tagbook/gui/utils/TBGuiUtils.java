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

package pw.ahs.app.tagbook.gui.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import pw.ahs.app.tagbook.core.utils.TBConsts;
import pw.ahs.app.tagbook.core.utils.TBError;
import pw.ahs.app.tagbook.core.utils.TBIOUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class TBGuiUtils {
    public static void initializeAction(Action action, String name,
                                        String shortDesc, Integer mnemonic, ImageIcon icon,
                                        KeyStroke keyStroke) {
        if (action != null) {
            if (name != null) {
                action.putValue(Action.NAME, name);
            }
            if (shortDesc != null) {
                action.putValue(Action.SHORT_DESCRIPTION, shortDesc);
            }
            if (mnemonic != null) {
                action.putValue(Action.MNEMONIC_KEY, mnemonic);
            }
            if (icon != null) {
                action.putValue(Action.SMALL_ICON, icon);
            }
            if (keyStroke != null) {
                action.putValue(Action.ACCELERATOR_KEY, keyStroke);
            }
        }
    }

    public static String formatDate(Date date) {
        return formatDate(date.getTime());
    }

    public static String formatDate(long time) {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                DateFormat.MEDIUM).format(time);
    }

    public static void removeFileChooserFileFilters(JFileChooser fc) {
        if (fc != null) {
            FileFilter[] filters = fc.getChoosableFileFilters();
            if (filters != null) {
                for (FileFilter filter : filters) {
                    fc.removeChoosableFileFilter(filter);
                }
            }
        }
    }

    public static final FileFilter csvFileFilter = new FileNameExtensionFilter(
            "Comma Separated Values (.csv)", "csv");
    public static final FileFilter xmlFileFilter = new FileNameExtensionFilter(
            "eXtensible Markup Language Files (.xml)", "xml");
    public static final FileFilter htmlFileFilter = new FileNameExtensionFilter(
            "Hyper Text Markup Language (.html, .htm)", "html", "html");
    public static final FileFilter txtFileFilter = new FileNameExtensionFilter(
            "Text Files (.txt)", "txt");
    public static final FileFilter tbFileFilter = new FileNameExtensionFilter(
            TBConsts.APP_TITLE + " Files (." + TBConsts.DB_FILE_EXT + ")",
            TBConsts.DB_FILE_EXT);
    public static final FileFilter tbbkFileFilter = new FileNameExtensionFilter(
            TBConsts.APP_TITLE + " Backup Files (."
                    + TBConsts.DB_BACKUP_FILE_EXT + ")",
            TBConsts.DB_BACKUP_FILE_EXT);

    /**
     * @param text the text
     * @return an image representation of the text
     * @throws IllegalArgumentException if {@code text} is null or empty
     */
    public static Image createImageFromText(String text) {
        if (StringUtils.isBlank(text))
            throw new IllegalArgumentException("text cannot be null or empty");
        Font font = imageFont;
        FontRenderContext frc = imageFrc;
        Rectangle2D bounds = font.getStringBounds(text, frc);
        BufferedImage txtImage = new BufferedImage((int) bounds.getWidth(),
                (int) bounds.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = txtImage.createGraphics();
        g.setColor(Color.black);
        g.setFont(font);
        g.drawString(text, (float) bounds.getX(), (float) -bounds.getY());
        g.dispose();
        return txtImage;
    }

    private static final Font imageFont = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    private static final FontRenderContext imageFrc = new FontRenderContext(null,
            true, true);

    @SuppressWarnings("serial")
    public static JFileChooser getNewFileChooser() {
        return new JFileChooser() {

            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                String path = f.getAbsolutePath();

                if (!getFileFilter().accept(f)) {
                    if (FilenameUtils.isExtension(path, "")) {
                        String[] exts = ((FileNameExtensionFilter) getFileFilter())
                                .getExtensions();
                        String ext;
                        if (exts.length > 0) {
                            ext = exts[0];
                            f = new File(TBIOUtils.setFileExt(path, ext));
                            if (!getFileFilter().accept(f)) {
                                JOptionPane.showMessageDialog(this,
                                        TBError.getMessage("badExt.m"),
                                        TBError.getMessage("badExt.t"),
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            } else { // extension is set
                                setSelectedFile(f);
                            }
                        } else { // could set extension
                            JOptionPane.showMessageDialog(this,
                                    "something is wrong!", "Wrong",
                                    JOptionPane.ERROR_MESSAGE);
                            cancelSelection();
                            return;
                        }
                    } else { // file was not accepted and has an extension
                        JOptionPane.showMessageDialog(this,
                                TBError.getMessage("badExt.m"),
                                TBError.getMessage("badExt.t"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } // file is accepted

                if (f.isFile() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(this,
                            TBError.getMessage("eFile.m"),
                            TBError.getMessage("eFile.t"),
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };
    }
}
