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

import java.util.HashMap;
import java.util.Map;

public class TBText {
    private static final Map<String, String> tm = new HashMap<>();

    static {
        tm.put("about.title", "About - " + TBConsts.APP_TITLE);
        tm.put("about.header", TBConsts.APP_TITLE
                + " - The Bookmarking Application");
        tm.put("edit.title", "Edit - " + TBConsts.APP_TITLE);
        tm.put("exportImportDialog.t", "Export | Import - "
                + TBConsts.APP_TITLE);
        tm.put("exportDialog.t", "Export - " + TBConsts.APP_TITLE);
        tm.put("importDialog.t", "Import - " + TBConsts.APP_TITLE);
        tm.put("fileFilter.csv", "Comma Separated Values (.csv)");
        tm.put("helpPanel.t", "Help - " + TBConsts.APP_TITLE);
        tm.put("logPanel.t", "Log - " + TBConsts.APP_TITLE);
        tm.put("saveLog.chooseType", "Choose Message Type");
        tm.put("saveLog.chooseTitle", "Choose Message Title");
        tm.put("saveLog.chooseText", "Choose Message Text");
        tm.put("saveLog.t", "Choose Message Text");
        tm.put("settingsDialog.t", "Settings - " + TBConsts.APP_TITLE);
        tm.put("stats.t", "Statistics - " + TBConsts.APP_TITLE);
        tm.put("stats.header", "Statistics");
        tm.put("tagManager.t", "Tag Manager - " + TBConsts.APP_TITLE);
        tm.put("tagDialog.t", "Tag Bookmarks - " + TBConsts.APP_TITLE);
        tm.put("dao.dbFileName", "bookmark.h2.db");
        tm.put("dao.backup.fileExt", "bkba");
        tm.put("dao.export.fileName", "bookmarks");
        tm.put("table.col.id", "ID");
        tm.put("table.col.name", "Name");
        tm.put("table.col.desc", "Description");
        tm.put("table.col.address", "Data");
        tm.put("table.col.notes", "Notes");
        tm.put("table.col.tags", "Tags");
        tm.put("table.col.dateadded", "Date Added");
        tm.put("table.col.datemodified", "Date Modified");
        tm.put("app.title", "TagBook");
        tm.put("tray.tooltip", "TagBook - The Bookmarking Application");
        tm.put("close.m", "TagBook will be closed.\nExit?");
        tm.put("close.t", "Close TagBook?");
        tm.put("main.saveLog.m",
                "Some information were logged.\nWould you like to save the data?");
        tm.put("main.saveLog.t", "Logged Information");
        tm.put("prefs", "TagBook_Preferences");
        tm.put("op.copyName", "Copied 'Name' to Clipboard");
        tm.put("op.copyDesc", "Copied 'Description' to Clipboard");
        tm.put("op.copyAddress", "Copied 'Data' to Clipboard");
        tm.put("op.copyNotes", "Copied 'Notes' to Clipboard");
        tm.put("op.copyTags", "Copied 'Tags' to Clipboard");
        tm.put("op.copyDateadded", "Copied 'Date Added' to Clipboard");
        tm.put("op.copyDatemodified", "Copied 'Date Modified' to Clipboard");
        tm.put("op.closeDb", "Book closed");
        tm.put("op.insert", "New Bookmark Inserted");
        tm.put("op.update", "Bookmark updated");
        tm.put("op.del", "Bookmark(s) deleted");
        tm.put("op.export.on", "Exporting Bookmarks..");
        tm.put("op.export", "Bookmarks Exported");
        tm.put("op.export.abort", "Export Aborted");
        tm.put("op.import.on", "Importing Bookmarks..");
        tm.put("op.import", "Bookmarks Imported");
        tm.put("op.import.abort", "Import Aborted");
        tm.put("op.newDb.on", "Creating book");
        tm.put("op.newDb", "Book created");
        tm.put("op.newDb.abort", "No book created");
        tm.put("op.loadDb.on", "Loading book");
        tm.put("op.loadDb", "Book loaded");
        tm.put("op.loadDb.abort", "No book loaded");
        tm.put("op.backupDb.on", "Backing-up Book");
        tm.put("op.backupDb", "Book Backed-up");
        tm.put("op.backupDb.abort", "No backup created");
        tm.put("op.restoreDb.on", "Restoring book");
        tm.put("op.restoreDb", "Book Restored");
        tm.put("op.restoreDb.abort", "Restore canceled");
        tm.put("op.restoreDb.notfi.m",
                "This is a sensitive operation.\n"
                        + "If an error occurs, you might loose ALL your data!\n"
                        + "It is highly recommended to backup your current data first.\n"
                        + "Continue?");
        tm.put("op.restoreDb.notif.t", "WARNING!");
        tm.put("op.tag.rename", "Tag renamed");
        tm.put("op.tag.replace", "Tag replaced");
        tm.put("op.tag.del", "Tag deleted");
        tm.put("op.tag.deepDel", "Tag deleted with its associated bookmarks");
        tm.put("op.tag.delOrph", "Orphaned tags deleted");
        tm.put("op.newDb.fcTitle", "Choose a file");
        tm.put("op.loadDb.fcTitle", "Choose a file");
        tm.put("op.backupDb.fcTitle", "Choose a file");
        tm.put("op.restoreDb.fcTitle", "Choose a file");
    }

    public static String get(String key) {
        String val = tm.get(key);
        if (val == null) {
            val = "no text";
        }
        return val;
    }
}
