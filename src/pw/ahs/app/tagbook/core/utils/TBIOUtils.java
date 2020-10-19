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

package pw.ahs.app.tagbook.core.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.*;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class TBIOUtils {
    /**
     * @param path the file path
     * @return true if the path describes a non-existing file, false otherwise.
     * a virtual file is a path that does not end with a file separator
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty
     */
    public static boolean isVirtualFile(String path) {
        if (StringUtils.isBlank(path))
            throw new IllegalArgumentException("(" + path + ") is not valid");
        File f = new File(path);
        return !f.exists() && !path.endsWith(File.separator);
    }

    /**
     * @param path the file path
     * @return true if {@code path} describes an existing file, false othrewise
     */
    public static boolean isExistingFile(String path) {
        if (StringUtils.isBlank(path))
            throw new IllegalArgumentException("(" + path + ") is not valid");
        File f = new File(path);
        return f.isFile();
    }

    public static boolean isNoExtension(String path) {
        return FilenameUtils.isExtension(path, "");
    }

    public static boolean isTagBookFile(String path, boolean checkContent) {
        if (StringUtils.isBlank(path))
            return false;
        if (!FilenameUtils.isExtension(path, TBConsts.DB_FILE_EXT))
            return false;
        if (!checkContent)
            return true;
        File f = new File(path);
        if (!f.exists())
            return false;
        try (ZipFile zf = new ZipFile(f)) {
            String dbFileName = setFileExt(f.getName(), TBConsts.H2DB_FILE_EXT);
            ZipEntry ze = zf.getEntry(dbFileName);
            return ze != null;
        } catch (IOException | IllegalStateException ignore) {
            return false;
        }
    }

    public static boolean isTagBookBackupFile(String path) {
        return !StringUtils.isBlank(path) && FilenameUtils.isExtension(path, TBConsts.DB_BACKUP_FILE_EXT);
    }

    public static boolean isTagBookImportExportFile(String path) {
        return !StringUtils.isBlank(path) && FilenameUtils.isExtension(path, new String[]{"csv", "html", "htm", "xml"});
    }

    /**
     * @param path the file path
     * @param ext the extension
     * @return a path with the new extension. if {@code ext} is null or empty,
     * the extension is removed
     * @throws IllegalArgumentException if {@code path} is {@code null} or empty
     */
    public static String setFileExt(String path, String ext) {
        if (StringUtils.isBlank(path))
            throw new IllegalArgumentException("(" + path + ") is not valid");
        if (path.endsWith(".h2.db"))
            path = path.substring(0, path.length() - 6);
        path = FilenameUtils.removeExtension(path);
        if (StringUtils.isBlank(ext))
            return path;
        return FilenameUtils.removeExtension(path) + '.' + (ext == null ? "" : ext);
    }

    public static boolean setFileReadable(String path) {
        return !StringUtils.isBlank(path) && setFileReadable(new File(path));
    }

    public static boolean setFileReadable(File f) {
        return f != null && f.isFile() && (f.canRead() || f.setReadable(true));
    }

    public static boolean setFileWritable(String path, boolean create) {
        return !StringUtils.isBlank(path) && setFileWritable(new File(path), create);
    }

    public static boolean setFileWritable(File f, boolean create) {
        if (f == null)
            return false;

        if (f.exists()) {
            return f.isFile() && (f.canWrite() || f.setWritable(true));

        }

        if (!create) {
            return f.getParentFile() != null && f.getParentFile().canWrite();
        }

        try {
            FileUtils.touch(f);
        } catch (IOException ignore) {
            return false;
        }
        return f.setWritable(true);
    }

    /**
     * @param path the file path
     * @return true if the file was successfully deleted, false otherwise
     * @throws IllegalArgumentException if {@code path} is null or empty
     */
    public static boolean deleteFile(String path) {
        if (StringUtils.isBlank(path))
            throw new IllegalArgumentException("(" + path + ") is not valid");
        File f = new File(path);
        return FileUtils.deleteQuietly(f);
    }

    public static String zipAfile(String path, String ext) {
        if (!setFileReadable(path))
            return null;
        if (StringUtils.isBlank(ext))
            ext = "zip";
        File f = new File(path);
        File zippedFile = new File(TBIOUtils.setFileExt(path, ext));
        if (zippedFile.exists())
            zippedFile.delete();

        int buffer = 2048;
        try (BufferedInputStream input = new BufferedInputStream(
                new FileInputStream(f), buffer);
             ZipOutputStream zipout = new ZipOutputStream(
                     new BufferedOutputStream(new FileOutputStream(
                             zippedFile)))) {
            ZipEntry ze = new ZipEntry(f.getName());
            byte[] data = new byte[buffer];
            zipout.setMethod(ZipOutputStream.DEFLATED);
            zipout.putNextEntry(ze);
            int len;
            while ((len = input.read(data, 0, buffer)) != -1) {
                zipout.write(data, 0, len);
            }
            return zippedFile.getAbsolutePath();
        } catch (IOException ignored) {
            return null;
        }
    }

    public static String unzipAfile(String path) {
        if (!setFileReadable(path))
            return null;

        ZipFile zf;
        try {
            zf = new ZipFile(path);
        } catch (IOException e) {
            return null;
        }

        ZipEntry ze = zf.entries().nextElement();
        String unzipped = FilenameUtils.getFullPath(path) + ze.getName();

        int buffer = 2048;
        try (BufferedInputStream input = new BufferedInputStream(
                zf.getInputStream(ze));
             BufferedOutputStream output = new BufferedOutputStream(
                     new FileOutputStream(unzipped), buffer)) {
            byte data[] = new byte[buffer];

            int len;
            while ((len = input.read(data, 0, buffer)) != -1) {
                output.write(data, 0, len);
            }
        } catch (IOException e) {
            return null;
        }

        try {
            zf.close();
        } catch (IOException ignored) {
        }

        return unzipped;
    }

    /**
     * @param text the text to write
     * @param path the file path
     * @return true if {@code text} was successfully written to {@code path},
     * false otherwise
     * @throws IllegalArgumentException if {@code path} or {@code text} is null or empty.
     */
    public static boolean writeTextFile(String text, String path) {
        if (StringUtils.isBlank(text))
            throw new IllegalArgumentException("(" + text + ") is invalid");
        if (StringUtils.isBlank(path))
            throw new IllegalArgumentException("(" + path + ") is invalid");
        return writeTextFile(text, new File(path));
    }

    /**
     * @param text the text to write
     * @param f the file path
     * @return true if {@code text} was successfully written to {@code f}, false
     * otherwise
     * @throws IllegalArgumentException if {@code f} is null or {@code text} is null or empty.
     */
    public static boolean writeTextFile(String text, File f) {
        if (StringUtils.isBlank(text))
            throw new IllegalArgumentException("(" + text + ") is invalid");
        if (f == null)
            throw new IllegalArgumentException("file cannot be null");
        try {
            FileUtils.write(f, text, "UTF-8");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * @param path the file path
     * @return the file specified by {@code path} as a string. If something goes
     * wrong, an empty string is returned
     * @throws IllegalArgumentException if {@code path} is null or is an empty
     */
    public static String readTextFile(String path) {
        if (StringUtils.isBlank(path))
            throw new IllegalArgumentException("(" + path + ") is invalid");
        try {
            return FileUtils.readFileToString(new File(path));
        } catch (IOException e) {
            return "";
        }
    }

    public static String readTextFromStream(InputStream in) {
        if (in == null)
            throw new IllegalArgumentException("input stream is null");
        Scanner sc = new Scanner(in);
        sc.useDelimiter("\\A");
        String res = "";

        if (sc.hasNext())
            res = sc.next();
        sc.close();
        return res;
    }

    public static boolean copyTextToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            return true;
        } catch (AWTError | HeadlessException | IllegalStateException e) {
            return false;
        }
    }

    public static String getTextFromClipboard() {
        String text = null;
        try {
            Transferable clipboardContents = Toolkit.getDefaultToolkit()
                    .getSystemClipboard().getContents(null);
            if (clipboardContents != null) {
                if (clipboardContents
                        .isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    text = clipboardContents.getTransferData(
                            DataFlavor.stringFlavor).toString();
                }
            }
        } catch (AWTError | HeadlessException | IOException
                | UnsupportedFlavorException e) {
            text = null;
        }
        return text;
    }

}
