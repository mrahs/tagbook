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

import pw.ahs.app.tagbook.core.utils.TBConsts;

import javax.swing.*;

public abstract class TBWorker extends SwingWorker<Void, Void> {
    private int flag = TBConsts.DAO_ERROR;

    @Override
    protected Void doInBackground() {
        flag = doThis();
        return null;
    }

    abstract protected int doThis();

    @Override
    protected void done() {
        alwaysDoWhenDone();
        switch (flag) {
            case TBConsts.DAO_ABORT:
                doneWithAbort();
                break;
            case TBConsts.DAO_ERROR:
                doneWithError();
                break;
            case TBConsts.DAO_SUCCESS:
                doneWithSuccess();
                break;
        }
    }

    protected void alwaysDoWhenDone() {

    }

    abstract protected void doneWithSuccess();

    abstract protected void doneWithError();

    abstract protected void doneWithAbort();
}