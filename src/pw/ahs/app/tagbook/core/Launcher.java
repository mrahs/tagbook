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

package pw.ahs.app.tagbook.core;

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import pw.ahs.app.tagbook.core.utils.TBError;
import pw.ahs.app.tagbook.gui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;

public class Launcher {
    public static void main(String[] args) {

        // Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        // CL argument
        final String path;
        if (args.length > 0) path = args[0];
        else path = null;

        // Try to catch uncaught exceptions
        Toolkit.getDefaultToolkit().getSystemEventQueue()
                .push(new EventQueue() {
                    @Override
                    protected void dispatchEvent(AWTEvent newEvent) {
                        try {
                            super.dispatchEvent(newEvent);
                        } catch (Throwable t) {
                            ErrorInfo info = new ErrorInfo(TBError
                                    .getMessage("fatal.t"), TBError
                                    .getMessage("fatal.m"), null, "unknown", t,
                                    Level.ALL, null);

                            JXErrorPane.showDialog(null, info);

                        }
                    }
                });

        // Run appliction
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow(path).setVisible(true);
            }
        });
    }
}

