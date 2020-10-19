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

import org.apache.commons.lang3.StringUtils;
import pw.ahs.app.tagbook.core.utils.TBConsts;
import pw.ahs.app.tagbook.core.utils.TBUtils;

import java.util.HashSet;
import java.util.Set;

public class Tag implements Comparable<Object> {
    /**
     * @param address the address to suggest tags for
     * @return an array of {@code Tag}. if no suggestions were made,
     * {@code null} is returned.
     */
    public static Tag[] suggestTags(String address) {
        if (StringUtils.isBlank(address))
            return null;
        Set<Tag> tags = new HashSet<>();
        if (Bookmark.isLink(address, true))
            tags.add(new Tag("link"));
        else if (Bookmark.isEmail(address))
            tags.add(new Tag("email"));
        else if (TBUtils.tbNumberPattern.matcher(address).matches())
            tags.add(new Tag("number"));
        return tags.isEmpty() ? null : tags.toArray(new Tag[tags.size()]);
    }

    /**
     * @param name the name
     * @return false if {@code name} is null, is an empty string or contains
     * whitespace or one of the invalid characters specified by
     * {@code TBConsts.TAGS_INVALID_CHARS}, true otherwise
     * @see TBConsts#TAG_INVALID_CHARS
     */
    public static boolean isValidTagName(String name) {
        return !(StringUtils.containsWhitespace(name)
                || StringUtils.containsAny(name, TBConsts.TAG_INVALID_CHARS));
    }

    private String name;

    /**
     * @param name the name
     * @see #setName(String)
     */
    public Tag(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    /**
     * @param name the name
     * @throws IllegalArgumentException if {@code name} is invalid
     * @see Tag#isValidTagName(String)
     */
    final void setName(String name) {

        this.name = name;
    }

    public Object[] toArray() {
        return new Object[]{name};
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        return result;
    }

    /**
     * two objects of {@code Tag} are considered equal if
     * {@code o1.name.equlas(o2.name)}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Tag other = (Tag) obj;
        return name.equals(other.name);
    }

    /**
     * comparison is based on {@code name} field
     */
    @Override
    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        }
        if (o == this) {
            return 0;
        }
        if (o.getClass() != getClass()) {
            return -1;
        }
        Tag rhs = (Tag) o;
        return name.compareTo(rhs.name);
    }
}
