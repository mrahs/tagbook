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
import java.awt.event.ActionEvent;

public class TextDialog {
    private final JDialog dialog;
    private final JTextArea text;

    @SuppressWarnings("serial")
    public TextDialog(JDialog parent, boolean modal) {
        text = new JTextArea();
        text.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        text.setEditable(false);
        text.setWrapStyleWord(false);
        text.setSize(100, 100);
        dialog = new JDialog(parent, modal);
        dialog.add(text);
        setText(null);
        dialog.getRootPane().registerKeyboardAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        }, KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public void setVisible(boolean b) {
        dialog.setVisible(b);
    }

    public void setText(String text) {
        this.text.setText(text);
        dialog.pack();
        dialog.setLocationRelativeTo(dialog.getParent());
    }

    public void dispose() {
        dialog.dispose();
    }
}
