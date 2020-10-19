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

import pw.ahs.app.tagbook.core.utils.TBTable;

import javax.swing.table.DefaultTableModel;

public class DefaultTableModelController implements TBTable {
    private DefaultTableModel model;
    private final RowCounter rowCounter;

    public DefaultTableModelController(DefaultTableModel model) {
        setModel(model);
        rowCounter = new RowCounter();
    }

    public DefaultTableModel getModel() {
        return model;
    }

    /**
     * @param model the table model
     * @throws IllegalArgumentException if model is null
     */
    void setModel(DefaultTableModel model) {
        if (model == null) {
            throw new IllegalArgumentException("wrapped model cannot be null");
        }
        this.model = model;
    }

    public RowCounter getRowCounter() {
        return rowCounter;
    }

    @Override
    public void addRow(Object[] data) {
        model.addRow(data);
        rowCounter.inc();
    }

    @Override
    public void addRows(Object[][] data) {
        if (data == null) {
            addRow(null);
        } else {
            for (Object[] row : data) {
                addRow(row);
            }
        }
    }

    @Override
    public void insertRow(int index, Object[] data) {
        if (index < 0) {
            throw new IllegalArgumentException("index cannot be less then zero");
        }
        if (index >= model.getRowCount()) {
            model.addRow(data);
        } else {
            model.insertRow(index, data);
            rowCounter.inc();
        }
    }

    @Override
    public void updateRow(int index, Object[] data) {
        if (index < 0) {
            throw new IllegalArgumentException("index cannot be less then zero");
        }
        if (index >= model.getRowCount()) {
            throw new IllegalArgumentException("too large index value");
        }
        if (data.length > model.getColumnCount()) {
            throw new IllegalArgumentException("too much data");
        }

        int i = 0;
        for (; i < data.length; ++i) {
            model.setValueAt(data[i], index, i);
        }
        for (; i < model.getColumnCount(); ++i) {
            model.setValueAt(null, index, i);
        }
    }

    @Override
    public void removeRow(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index cannot be less then zero");
        }
        if (index >= model.getRowCount()) {
            model.removeRow(model.getRowCount() - 1);
        } else {
            model.removeRow(index);
        }
        rowCounter.dec();
    }

    @Override
    public void clearRows() {
        model.setRowCount(0);
        rowCounter.clear();
    }
}
