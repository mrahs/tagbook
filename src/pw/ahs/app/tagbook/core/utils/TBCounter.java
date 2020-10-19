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

public interface TBCounter {
    /**
     * @param val the value
     * @throws IllegalArgumentException if {@code val} less than zero
     */
    public void setValue(long val);

    public long getValue();

    public void inc();

    /**
     * @throws IllegalArgumentException if {@code val} is less than zero
     */
    public void inc(int val);

    public void dec();

    /**
     * @throws IllegalArgumentException if {@code val} is less than zero
     */
    public void dec(int val);

    public void clear();
}
