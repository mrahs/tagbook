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

import pw.ahs.app.tagbook.core.utils.TBCounter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class RowCounter implements TBCounter {
    private Long value;
    private final PropertyChangeSupport changeSupp;

    public RowCounter() {
        this(0);
    }

    private RowCounter(long val) {
        changeSupp = new PropertyChangeSupport(val);
        setValue(val);
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changeSupp.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupp.removePropertyChangeListener(l);
    }

    @Override
    public final void setValue(long val) {
        if (val < 0)
            throw new IllegalArgumentException("(" + val
                    + ") is less than zero");
        Long oldValue = this.value;
        this.value = val;
        changeSupp.firePropertyChange("value", oldValue, this.value);
    }

    @Override
    public long getValue() {
        return value;
    }

    @Override
    public void inc() {
        setValue(this.value + 1);
    }

    @Override
    public void inc(int val) {
        if (val < 0)
            throw new IllegalArgumentException("(" + val
                    + ") is less than zero");
        setValue(this.value + val);
    }

    @Override
    public void dec() {
        setValue(this.value - 1);
    }

    @Override
    public void dec(int val) {
        if (val < 0)
            throw new IllegalArgumentException("(" + val
                    + ") is less than zero");
        setValue(this.value - val);
    }

    @Override
    public void clear() {
        setValue(0);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
