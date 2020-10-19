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

import pw.ahs.app.tagbook.core.utils.TBConsts;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Bookmark {
    public static boolean isLink(String address, boolean acceptDomain) {
        return UrlValidator.getInstance().isValid(address) || acceptDomain && DomainValidator.getInstance().isValid(address);
    }

    public static boolean isEmail(String address) {
        return EmailValidator.getInstance().isValid(address);
    }

    String name;
    String desc;
    String address;
    String notes;
    Timestamp dateadded;
    Timestamp datemodified;
    SortedSet<Tag> tags;

    /**
     * Constructs a new Bookmark object.
     *
     * @param name the name to set. must not be null nor empty.
     * @param desc the desc to set
     * @param address the address to set. must not be null nor empty
     * @param notes the notes to set
     * @param tags the tags
     * @param dateadded the dateadded to set. if {@code null} the date is set to now.
     * @param datemodified the datemodified to set. if {@code null} the date is set to {@code dateadded}.
     * @see #setName(String)
     * @see #setDesc(String)
     * @see #setAddress(String)
     * @see #setNotes(String)
     * @see #setDateadded(Timestamp)
     * @see #setDatemodified(Timestamp)
     * @see #setTags(Set)
     */
    Bookmark(String name, String desc, String address, String notes,
             Set<Tag> tags, Timestamp dateadded, Timestamp datemodified) {
        setName(name);
        setDesc(desc);
        setAddress(address);
        setNotes(notes);
        setDateadded(dateadded); // must precede setDatemodified
        setDatemodified(datemodified);
        setTags(tags);
    }

    /**
     * @param name the name to set. must not be null nor empty.
     * @param address the address to set. must not be null nor empty
     */
    public Bookmark(String name, String address) {
        this(name, null, address, null, null, null, null);
    }

    /**
     * @param name the name to set. must not be null nor empty.
     * @param address the address to set. must not be null nor empty
     * @param dateadded the dateadded to set. if {@code null} the date is set to now.
     */
    public Bookmark(String name, String address, Timestamp dateadded) {
        this(name, null, address, null, null, dateadded, null);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @return the dateadded
     */
    public Timestamp getDateadded() {
        return dateadded;
    }

    /**
     * @return the datemodified
     */
    public Timestamp getDatemodified() {
        return datemodified;
    }

    /**
     * @return the tags
     */
    public Set<Tag> getTags() {
        return tags;
    }

    /**
     * @param name the name to set. must not be null nor empty.
     * @throws IllegalArgumentException if {@code name} is an empty string or is {@code null}.
     */
    public final void setName(String name) {
        if (StringUtils.isBlank(name))
            throw new IllegalArgumentException("(" + name
                    + ") is null or empty");
        this.name = name;
    }

    /**
     * @param desc the desc to set
     */
    public final void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * @param address the address to set. must not be null nor empty
     * @throws IllegalArgumentException if {@code address} is an empty string or is {@code null}.
     */
    public final void setAddress(String address) {
        if (StringUtils.isBlank(address))
            throw new IllegalArgumentException("(" + name
                    + ") is null or empty");
        this.address = address;
    }

    /**
     * @param notes the notes to set
     */
    public final void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @param dateadded the dateadded to set. if {@code null} the date is set to now.
     */
    public final void setDateadded(Timestamp dateadded) {
        this.dateadded = dateadded != null ? dateadded : new Timestamp(
                System.currentTimeMillis());
    }

    /**
     * @param datemodified the datemodified to set. if {@code null} the date is set to
     *                     {@code dateadded}.
     */
    public final void setDatemodified(Timestamp datemodified) {
        this.datemodified = datemodified != null ? datemodified : dateadded;
    }

    /**
     * @param tags the tags to set
     */
    public final void setTags(Set<? extends Tag> tags) {
        if ((tags != null && tags.isEmpty()) || tags == null)
            this.tags = null;
        else
            this.tags = new TreeSet<>(tags);
    }

    // public final void setTags()

    /**
     * @return a {@code URI} object representing the {@code address} field. if
     * the address cannot be represented as {@code URI}, {@code null} is
     * returned.
     */
    URI getUri() {
        if (isLink()) {
            try {
                return new URI(address);
            } catch (URISyntaxException ignore) {
            }
        } else if (isEmail()) {
            try {
                return new URI("mailto:" + address);
            } catch (URISyntaxException ignore) {
            }
        }
        return null;
    }

    /**
     * @param del the delimiter to use
     * @return a string representation of {@code tags} delimited using
     * {@code del}, or an empty string if there are no tags
     */
    public String getTagsString(String del) {
        if (tags == null) {
            return "";
        }
        if (del == null) {
            del = " ";
        }
        StringBuilder tagsText = new StringBuilder();
        for (Tag t : tags) {
            tagsText.append(t.getName()).append(del);
        }
        tagsText.delete(tagsText.lastIndexOf(del), tagsText.length());
        return tagsText.toString();
    }

    public void addTag(Tag t) {
        if (t != null) {
            if (this.tags == null) {
                this.tags = new TreeSet<>();
            }
            tags.add(t);
        }
    }

    public void addTags(Set<Tag> ts) {
        if (ts != null && !ts.isEmpty()) {
            tags.addAll(ts);
        }
    }

    public void removeTag(Tag t) {
        if (tags != null && t != null) {
            tags.remove(t);
        }
    }

    public void appendNote(String n) {
        if (StringUtils.isNotBlank(n)) {
            if (notes == null)
                notes = n;
            else
                notes += "\n" + n;
        }
    }

    public void prependNote(String n) {
        if (StringUtils.isNotBlank(n)) {
            if (notes == null)
                notes = n;
            else
                notes = n + "\n" + notes;
        }
    }

    /**
     * @param addressType must be one of TBConsts.ADDRESS_AS_*, otherwise,
     *                    {@code address} will be null in the array
     * @param tagsType    must be one of TBConsts.TAGS_AS_*, otherwise, {@code tags}
     *                    will be null in the array
     * @return an array of type {@code Object} contains the attributes
     */
    Object[] toArray(int addressType, int tagsType) {
        URI uri = getUri();
        Object addressObj = null;
        Object tagsObj = null;

        switch (tagsType) {
            case TBConsts.TAGS_AS_ARRAY:
                tagsObj = tags.toArray();
                break;
            case TBConsts.TAGS_AS_STRING:
                tagsObj = getTagsString(" ");
                break;
        }
        switch (addressType) {
            case TBConsts.ADDRESS_AS_STRING:
                addressObj = address;
                break;
            case TBConsts.ADDRESS_AS_URI:
                addressObj = uri == null ? address : uri;
                break;
        }
        return new Object[]{name, desc, addressObj, notes, tagsObj,
                dateadded, datemodified};
    }

    boolean isLink() {
        return isLink(address, true);
    }

    boolean isEmail() {
        return isEmail(address);
    }

    @Override
    public String toString() {
        return "Bookmark\n" + "\tName:\t" + name + "\n" + "\tDescription:\t" + (desc == null ? "" : desc) + "\n" + "\tData:\t" + address + "\n" + "\tNotes:\t" + (notes == null ? "" : notes) + "\n" + "\tTags:\t" + getTagsString(", ") + "\n" + "\tDate Added:\t" + dateadded + "\n" + "\tDate modified:\t" + datemodified + "\n";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + address.hashCode();
        return result;
    }

    /**
     * two objects of {@code Bookmark} are considered equal if
     * {@code o1.address.equals(o2.address)}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Bookmark other = (Bookmark) obj;
        return address.equals(other.address);
    }

}
