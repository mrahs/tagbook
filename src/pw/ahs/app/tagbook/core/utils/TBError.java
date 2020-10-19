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

public class TBError {
    private static final Map<String, String> em = new HashMap<>();

    static {
        em.put("dao.badurl.t", "Bad Database URL");
        em.put("dao.badurl.m", "the database url is invalid");
        em.put("dao.con.t", "Error Connecting To Database");
        em.put("dao.discon.t", "Error Disconnecting From Database");
        em.put("dao.close.t", "Error Closing Database");
        em.put("dao.file.m", "something wrong with a file: ");
        em.put("dao.createDb.t", "Error Creating Database");
        em.put("dao.loadDb.t", "Error Loading Database");
        em.put("dao.loadDb.m", "database is invalid");
        em.put("dao.backup.t", "Backup Error");
        em.put("dao.restore.t", "Restore Error");
        em.put("dao.rollback.t", "Error In Rollback");
        em.put("dao.import.t", "Import Error");
        em.put("dao.export.t", "Export Error");
        em.put("dao.sel.t", "Error Retrieving Data");
        em.put("dao.mod.t", "Error Updating Data");
        em.put("eAddress.m", "Data already exists!\n"
                + "1 -> Merge tags and notes.\n"
                + "2 -> Merge tags and Replace other fields.\n"
                + "3 -> Replace all fields.");
        em.put("eAddress.t", "Data Already Exists!");
        em.put("eTag.m",
                "Tag must not contain whitespaces\n or any of the following characters:\n"
                        + TBConsts.TAG_INVALID_CHARS);
        em.put("eTag.t", "Invalid Tag Name");
        em.put("eFile.t", "File Already Exists!");
        em.put("eFile.m", "Overwrite file?");
        em.put("badExt.t", "Wrong File Type!");
        em.put("badExt.m", "File extension is incorrect!");
        em.put("stats.noBookmark", "No bookmark found!");
        em.put("stats.noTag", "No tag found!");
        em.put("fatal.t", "Something went wrong!");
        em.put("fatal.m",
                "An error has occured!\n"
                        + "Please send these information to the developer to fix the bug in future releases.");
        em.put("driver.t", "Fatal Error!");
        em.put("driver.m",
                "Database driver could not be loaded!\nPlease make sure the package is not corrupted.");
        em.put("tray.t", "Error Adding Tray Icon");
        em.put("dnd.t", "DnD Error");
        em.put("dnd.m", "Error in DnD!");
        em.put("clipboard.m", "Error trying to access clipboard!");
        em.put("clipboard.t", "Clipboard Error!");
        em.put("op.insert", "Insert Error");
        em.put("op.update", "Update Error");
        em.put("op.del", "Delete Error");
        em.put("op.export", "Export Error");
        em.put("op.import", "Import Error");
        em.put("op.newDb", "Error creating database");
        em.put("op.loadDb", "Error loading database");
        em.put("op.backupDb", "Backup Error");
        em.put("op.restoreDb", "Restore Error");
        em.put("iac.m", "The selected database was not closed properly.");
        em.put("iac.t", "Inapproperiate Close Detected!");
    }

    public static String getMessage(String key) {
        String val = em.get(key);
        if (val == null) {
            val = "no message";
        }
        return val;
    }
}
