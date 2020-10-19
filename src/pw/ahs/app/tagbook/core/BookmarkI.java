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

import java.sql.Timestamp;
import java.util.Set;

/**
 * adds an {@code id} field
 */
public class BookmarkI extends Bookmark {
    public static final long NULL_ID = -1L;
    private Long id;

    /**
     * @param id           the id to set. must not be null. must meet the condition:           {@code id
     *                     >= 0}
     * @param name         the name to set
     * @param desc         the desc to set
     * @param address      the address to set
     * @param notes        the notes
     * @param tags         the tags
     * @param dateadded    the dateadd
     * @param datemodified the datemodified
     */
    public BookmarkI(Long id, String name, String desc, String address,
                     String notes, Set<Tag> tags, Timestamp dateadded,
                     Timestamp datemodified) {
        super(name, desc, address, notes, tags, dateadded, datemodified);
        setId(id);
    }

    /**
     * @param id      the id to set. must not be null. must meet the condition:           {@code id
     *                >= 0}
     * @param name    the name to set
     * @param address the address to set
     */
    public BookmarkI(Long id, String name, String address) {
        this(id, name, null, address, null, null, null, null);
    }

    /**
     * @param id        the id to set. must not be null. must meet the condition:           {@code id
     *                  >= 0}
     * @param name      the name to set
     * @param address   the address to set
     * @param dateadded the dateadd
     */
    public BookmarkI(Long id, String name, String address, Timestamp dateadded) {
        this(id, name, null, address, null, null, dateadded, null);
    }

    public BookmarkI(Bookmark orig, Long id) {
        super(orig.name, orig.desc, orig.address, orig.notes, orig.tags,
                orig.dateadded, orig.datemodified);
        setId(id);
    }

    public BookmarkI(Bookmark orig) {
        this(orig, NULL_ID);
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set. must not be null. must meet the condition:
     *           {@code id
     *           >= 0}
     * @throws IllegalArgumentException if {@code id} is null or less than 0
     */
    public final void setId(Long id) {
        if (id == null)
            throw new IllegalArgumentException("id cannot be null");
        if (id < 0 && id != NULL_ID)
            throw new IllegalArgumentException(
                    "id must be greater than 0, got(" + id + ")");
        this.id = id;
    }

    @Override
    public Object[] toArray(int addressType, int tagsType) {
        Object[] superArray = super.toArray(addressType, tagsType);
        return new Object[]{id, superArray[0], superArray[1], superArray[2],
                superArray[3], superArray[4], superArray[5], superArray[6]};
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + id.hashCode();
        return result;
    }

    /**
     * two objects of {@code BookmarkI} are considered equal if
     * {@code o1.id.equals(o2.id)}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        BookmarkI other = (BookmarkI) obj;
        return id.equals(other.id);
    }

}
