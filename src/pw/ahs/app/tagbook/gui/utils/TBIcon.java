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

import javax.swing.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TBIcon {
    private static final Map<String, ImageIcon> im = new HashMap<>();

    static {
        String iconsPath = "/res/icons/";
        String noIconFile = "puzzle-16";

        String[][] iconNameFilePair = new String[][]{
                {"bookmark", "bookmark-16"}, // Star-full
                {"app", "bookmark-24"}, // bookmark (bigger version)
                {"tray", "bookmark-16"}, // bookmark
                {"bookmark.add", "bookmark-add-16"}, // Star & Add (modified)
                {"bookmark.edit", "bookmark-edit-16"}, // Star & Edit
                // (modified)
                {"bookmark.delete", "bookmark-delete-16"}, // Star & Delete
                // (modified)
                {"tag.delete", "tag-delete-16"}, // Bookmark delete
                {"tag.replace", "replace-16"}, // Order
                {"tag.deepDelete", ""}, // not set
                {"tag.deleteOrphaned", ""}, // not set
                {"database", "db-16"}, // Basic data
                {"db.recent", "db-recent16"}, //
                {"db.new", "db-add-16"}, // Basic data & Add (modified)
                {"db.load", "db-open-16"}, // Basic data & Open-folder
                // (modified)
                {"db.backup", "db-backup-16"}, // Basic data & Backup-resoter
                // (modified)
                {"db.restore", "db-restore-16"}, // Basic data &
                // Backup-restore (modified)
                {"db.refresh", "db-refresh-16"}, // Basic data & Refresh
                // (modified)
                {"db.close", "db-close-16"}, // Basic data & Close (modified)
                {"readonly.true", "locked-16"}, // Locked
                {"readonly.false", "unlocked-16"}, // Unlocked
                {"search", "search-16"}, // Search
                {"filter", "filter"}, // from ??
                {"rename", "rename-16"}, // Rename
                {"show", "show-16"}, // Navigate up
                {"hide", "hide-16"}, // Navigate down
                {"copy", "copy16"}, // Copy
                {"cut", "cut16"}, // Cut
                {"paste", "paste-16"}, // Paste
                {"delete", "delete-16"}, // Delete2
                {"clear", "clear-16"}, // Clear
                {"undo", "undo-16"}, // Undo
                {"redo", "redo-16"}, // Undo (modified)
                {"save", "save-16"}, // Save
                {"saveas", "save-as-16"}, // Save as
                {"open", "open-file-16"}, // open-file
                {"cancel", "cancel-16"}, // Cancel
                {"close", "close-16"}, // Close
                {"stop", "stop12"}, // Stop
                {"exit", "logout-16"}, // logout
                {"error", "error-16"}, // Alert (modified)
                {"info", "info-16"}, // Info
                {"file", "file-16"}, // Documents
                {"browse", "browse-16"}, // File explorer
                {"view", "view-16"}, // Eye
                {"tagManager", "tag-16"}, // Bookmark
                {"stats", "stats-16"}, // Analysis
                {"log", "log-16"}, // File info
                {"settings", "options-16"}, // Options
                {"help", "help-16"}, // Faq
                {"import", "import-16"}, // Import
                {"export", "export-16"}, // Export
                {"sel", "table_select_big_16"}, // from FatCow
                {"sel.all", "table_select_all_16"}, // from FatCow
                {"sel.non", "table16"}, // from FatCow
                {"sel.inv", "table_select_row_16"} // from FatCow
        };

        URL imgUrl = TBIcon.class.getResource(iconsPath + noIconFile + ".png");
        ImageIcon noIcon = new ImageIcon(imgUrl, "");

        for (String[] anIconNameFilePair : iconNameFilePair) {
            if (anIconNameFilePair[1].isEmpty()) {
                im.put(anIconNameFilePair[0], noIcon);
                continue;
            }
            imgUrl = TBIcon.class.getResource(iconsPath
                    + anIconNameFilePair[1] + ".png");
            if (imgUrl != null) {
                im.put(anIconNameFilePair[0], new ImageIcon(imgUrl, ""));
            }
        }
    }

    public static ImageIcon get(String key) {
        return im.get(key);
    }
}
