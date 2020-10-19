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
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class TBKeyStroke {
    private static final Map<String, KeyStroke> ksm = new HashMap<>();

    static {
        int menuShortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        ksm.put("edit.PasteName",
                KeyStroke.getKeyStroke(KeyEvent.VK_1, menuShortcut));
        ksm.put("edit.PasteDesc",
                KeyStroke.getKeyStroke(KeyEvent.VK_2, menuShortcut));
        ksm.put("edit.PasteAddress",
                KeyStroke.getKeyStroke(KeyEvent.VK_3, menuShortcut));
        ksm.put("edit.PasteNotes",
                KeyStroke.getKeyStroke(KeyEvent.VK_4, menuShortcut));
        ksm.put("save", KeyStroke.getKeyStroke(KeyEvent.VK_S, menuShortcut));
        ksm.put("ok", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        ksm.put("cancel", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        ksm.put("undo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, menuShortcut));
        ksm.put("redo", KeyStroke.getKeyStroke(KeyEvent.VK_Y, menuShortcut));
        ksm.put("copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, menuShortcut));
        ksm.put("paste", KeyStroke.getKeyStroke(KeyEvent.VK_V, menuShortcut));
        ksm.put("cut", KeyStroke.getKeyStroke(KeyEvent.VK_X, menuShortcut));
        ksm.put("clear", KeyStroke.getKeyStroke(KeyEvent.VK_R, menuShortcut));
        ksm.put("delete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        ksm.put("rename", KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        ksm.put("replace", KeyStroke.getKeyStroke(KeyEvent.VK_L, menuShortcut));
        ksm.put("tag.deepDelete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,
                InputEvent.SHIFT_MASK));
        ksm.put("tag.deleteOrphaned",
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, menuShortcut));
        ksm.put("close", KeyStroke.getKeyStroke(KeyEvent.VK_W, menuShortcut));
        ksm.put("hide", KeyStroke.getKeyStroke(KeyEvent.VK_H, menuShortcut));
        ksm.put("bookmark.add",
                KeyStroke.getKeyStroke(KeyEvent.VK_N, menuShortcut));
        ksm.put("bookmark.edit",
                KeyStroke.getKeyStroke(KeyEvent.VK_E, menuShortcut));
        ksm.put("bookmark.tag",
                KeyStroke.getKeyStroke(KeyEvent.VK_T, menuShortcut));
        ksm.put("bookmark.pasteAddress",
                KeyStroke.getKeyStroke(KeyEvent.VK_V, menuShortcut));
        ksm.put("bookmark.pasteSpecial",
                KeyStroke.getKeyStroke(KeyEvent.VK_V, menuShortcut
                        | InputEvent.SHIFT_MASK));
        ksm.put("bookmark.copyName",
                KeyStroke.getKeyStroke(KeyEvent.VK_C, menuShortcut
                        | InputEvent.SHIFT_MASK));
        ksm.put("bookmark.copyDesc", null);
        ksm.put("bookmark.copyAddress",
                KeyStroke.getKeyStroke(KeyEvent.VK_C, menuShortcut));
        ksm.put("bookmark.copyNotes", null);
        ksm.put("bookmark.copyTags",
                KeyStroke.getKeyStroke(KeyEvent.VK_C, menuShortcut
                        | InputEvent.ALT_MASK));
        ksm.put("bookmark.copyDateadded", null);
        ksm.put("bookmark.copyDatemodified", null);
        ksm.put("bookmark.export",
                KeyStroke.getKeyStroke(KeyEvent.VK_X, menuShortcut));
        ksm.put("bookmark.import",
                KeyStroke.getKeyStroke(KeyEvent.VK_I, menuShortcut));
        ksm.put("db.new", null);
        ksm.put("db.load", KeyStroke.getKeyStroke(KeyEvent.VK_O, menuShortcut));
        ksm.put("db.backup", null);
        ksm.put("db.restore", null);
        ksm.put("db.refresh", KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        ksm.put("db.close", null);
        ksm.put("view.tagManager", KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        ksm.put("view.stats", KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
        ksm.put("view.log", KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        ksm.put("view.settings", KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        ksm.put("view.help", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        ksm.put("view.about", null);
        ksm.put("searchFocus",
                KeyStroke.getKeyStroke(KeyEvent.VK_F, menuShortcut));
        ksm.put("filterFocus",
                KeyStroke.getKeyStroke(KeyEvent.VK_F, menuShortcut
                        | InputEvent.SHIFT_MASK));
        ksm.put("readonly", KeyStroke.getKeyStroke(KeyEvent.VK_L, menuShortcut));
        ksm.put("sel.all", KeyStroke.getKeyStroke(KeyEvent.VK_A, menuShortcut));
        ksm.put("sel.non", KeyStroke.getKeyStroke(KeyEvent.VK_A, menuShortcut | InputEvent.SHIFT_MASK));
        ksm.put("sel.inv", KeyStroke.getKeyStroke(KeyEvent.VK_A, menuShortcut | InputEvent.ALT_MASK));

    }

    public static KeyStroke get(String key) {
        return ksm.get(key);
    }
}
