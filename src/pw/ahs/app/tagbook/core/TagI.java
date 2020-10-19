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
 * adds an {@code id} field
 */
public class TagI extends Tag {
    private Long id;

    /**
     * @param name the name
     * @see #setId(Long)
     */
    public TagI(Long id, String name) {
        super(name);
        setId(id);
    }

    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set. must not be null. must meet the condition:
     *           {@code id
     *           >= 0}
     * @throws NullPointerException     if {@code id} is null
     * @throws IllegalArgumentException if {@code id} is less than 0
     */

    final void setId(Long id) {
        if (id == null)
            throw new NullPointerException();
        if (id < 0)
            throw new IllegalArgumentException(
                    "id must be greater than 0, got(" + id + ")");
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + id.hashCode();
        return result;
    }

    /**
     * two objects of {@code TagI} are considered equal if
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
        TagI other = (TagI) obj;
        return id.equals(other.id);
    }

}
