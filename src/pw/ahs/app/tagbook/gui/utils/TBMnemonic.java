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

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class TBMnemonic {
    private static final Map<String, Integer> mm = new HashMap<>();

    static {
        // general
        mm.put("copy", KeyEvent.VK_Y);
        mm.put("cut", KeyEvent.VK_T);
        mm.put("paste", KeyEvent.VK_P);
        mm.put("delete", KeyEvent.VK_E);
        mm.put("clear", KeyEvent.VK_R);
        mm.put("rename", KeyEvent.VK_N);
        mm.put("replace", KeyEvent.VK_L);
        mm.put("cancel", KeyEvent.VK_C);
        mm.put("close", mm.get("cancel"));
        mm.put("hide", KeyEvent.VK_H);
        mm.put("undo", KeyEvent.VK_U);
        mm.put("redo", KeyEvent.VK_D);
        mm.put("save", KeyEvent.VK_S);
        mm.put("copy", KeyEvent.VK_Y);

        // tag menus
        mm.put("tag.deepDelete", KeyEvent.VK_E);
        mm.put("tag.deleteOrphaned", KeyEvent.VK_O);

        // bookmark menus
        mm.put("bookmark.add", KeyEvent.VK_A);
        mm.put("bookmark.edit", KeyEvent.VK_E);
        mm.put("bookmark.delete", KeyEvent.VK_D);
        mm.put("bookmark.tag", KeyEvent.VK_T);
        mm.put("bookmark.pasteAddress", KeyEvent.VK_A);
        mm.put("bookmark.pasteSpecial", KeyEvent.VK_P);
        mm.put("bookmark.copyName", KeyEvent.VK_N);
        mm.put("bookmark.copyDesc", KeyEvent.VK_D);
        mm.put("bookmark.copyAddress", KeyEvent.VK_A);
        mm.put("bookmark.copyNotes", KeyEvent.VK_O);
        mm.put("bookmark.copyTags", KeyEvent.VK_T);
        mm.put("bookmark.copyDateadded", KeyEvent.VK_C);
        mm.put("bookmark.copyDatemodified", KeyEvent.VK_C);
        mm.put("bookmark.search", KeyEvent.VK_S);
        mm.put("bookmark.filter", KeyEvent.VK_F);
        mm.put("bookmark.export", KeyEvent.VK_R);
        mm.put("bookmark.import", KeyEvent.VK_I);

        // db menus
        mm.put("db.new", KeyEvent.VK_N);
        mm.put("db.load", KeyEvent.VK_L);
        mm.put("db.backup", KeyEvent.VK_B);
        mm.put("db.restore", KeyEvent.VK_R);
        mm.put("db.refresh", KeyEvent.VK_F);
        mm.put("db.close", mm.get("cancel"));

        // view menu
        mm.put("view.tagManager", KeyEvent.VK_T);
        mm.put("view.stats", KeyEvent.VK_S);
        mm.put("view.log", KeyEvent.VK_L);
        mm.put("view.settings", KeyEvent.VK_G);
        mm.put("view.help", KeyEvent.VK_H);
        mm.put("view.about", KeyEvent.VK_B);

        // select menu
        mm.put("sel.all", KeyEvent.VK_A);
        mm.put("sel.non", KeyEvent.VK_N);
        mm.put("sel.inv", KeyEvent.VK_I);

        mm.put("readonly", KeyEvent.VK_R);
    }

    public static int get(String key) {
        return mm.get(key);
    }
}
