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

/**
 * a tag with bookmarks count
 */
class CountedTag {
    private Tag tag;
    private long bcount;

    public CountedTag(Tag tag, long bcount) {
        setTag(tag);
        setBcount(bcount);
    }

    public Tag getTag() {
        return tag;
    }

    /**
     * @param tag the tag
     * @throws IllegalArgumentException if {@code tag} is null
     */
    final void setTag(Tag tag) {
        if (tag == null)
            throw new IllegalArgumentException("tag cannot be null");
        this.tag = tag;
    }

    /**
     * @return the bcount
     */
    public long getBcount() {
        return bcount;
    }

    /**
     * @param bcount the bcount to set. {@code bcount >= 0}
     * @throws IllegalArgumentException if {@code bcount < 0}
     */
    public final void setBcount(long bcount) {
        if (bcount < 0)
            throw new IllegalArgumentException("(" + bcount
                    + ") is less than zero");
        this.bcount = bcount;
    }

    public Object[] toArray() {
        if (tag instanceof TagI) {
            TagI ti = (TagI) tag;
            return new Object[]{ti.getId(), ti.getName(), bcount};
        } else
            return new Object[]{tag.getName(), bcount};
    }

    @Override
    public String toString() {
        return tag.getName() + " (" + bcount + ")";
    }
}
