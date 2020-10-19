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

/**
 *
 *
 */
public class TBSql {
    public static final String INIT_DB = "CREATE TABLE tag(id IDENTITY NOT NULL UNIQUE, name VARCHAR NOT NULL UNIQUE);\n"
            + "CREATE TABLE bookmark(id IDENTITY NOT NULL UNIQUE, name VARCHAR NOT NULL, description VARCHAR, address VARCHAR NOT NULL UNIQUE, notes VARCHAR, dateadded TIMESTAMP NOT NULL, datemodified TIMESTAMP NOT NULL);\n"
            + "CREATE TABLE tagmap(bookmark_id BIGINT, tag_id BIGINT, FOREIGN KEY (bookmark_id) REFERENCES bookmark(id) ON DELETE CASCADE ON UPDATE CASCADE, FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE ON UPDATE CASCADE);";
    public static final String CLEAR_DB = "DROP ALL OBJECTS;";
    public static final String BACKUP_DB = "SCRIPT TO ? COMPRESSION DEFLATE CIPHER AES PASSWORD 'tb8ackUp' CHARSET 'UTF-8';";
    public static final String RESTORE_DB = "RUNSCRIPT FROM ? COMPRESSION DEFLATE CIPHER AES PASSWORD 'tb8ackUp' CHARSET 'UTF-8';";
    public static final String INSERT_BOOKMARK = "INSERT INTO bookmark(name, description, address, notes, dateadded, datemodified)\n"
            + "\tSELECT * FROM (\n"
            + "\t\tSELECT TRIM(BOTH FROM ?) AS name,\n"
            + "\t\t\tTRIM(BOTH FROM ?) AS description,\n"
            + "\t\t\tTRIM(BOTH FROM ?) AS  address,\n"
            + "\t\t\tTRIM(BOTH FROM ?) AS notes,\n"
            + "\t\t\tTRIM(BOTH FROM ?) AS dateadded,\n"
            + "\t\t\tTRIM(BOTH FROM ?) AS datemodified\n"
            + "\t\t) WHERE NOT EXISTS (SELECT id FROM bookmark WHERE address = ?);";

    public static String getInsertBookmarkTags(int n) {
        // Example:
        // INSERT INTO tag(name)
        // SELECT * FROM (
        // SELECT LOWER(TRIM(BOTH FROM ?)) AS name
        // UNION SELECT LOWER(TRIM(BOTH FROM ?)) AS name
        // UNION SELECT LOWER(TRIM(BOTH FROM ?)) AS name
        // ) AS TAGS
        // WHERE TAGS.name NOT IN (SELECT name FROM tag WHERE tag.name IN
        // (LOWER(TRIM(BOTH FROM ?)), LOWER(TRIM(BOTH FROM ?)), LOWER(TRIM(BOTH
        // FROM ?))));
        StringBuilder tags = new StringBuilder();
        StringBuilder tagsSelect = new StringBuilder();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            tags.append("LOWER(TRIM(BOTH FROM ?)), ");
            tagsSelect
                    .append("\t\tUNION SELECT LOWER(TRIM(BOTH FROM ?)) AS name\n");
        }
        tags.delete(tags.length() - 2, tags.length());
        tagsSelect.delete(2, 8);

        res.append("INSERT INTO tag(name)\n\tSELECT * FROM (\n")
                .append(tagsSelect)
                .append("\t\t) AS TAGS \n\tWHERE TAGS.name NOT IN (SELECT name FROM tag WHERE tag.name IN (")
                .append(tags).append("));\n");
        return res.toString();
    }

    public static String getInsertBookmarkTagsMap(int n) {
        // Example:
        // INSERT INTO tagmap(bookmark_id, tag_id)
        // SELECT * FROM
        // (SELECT LOWER(?)), (SELECT id FROM tag WHERE name IN (LOWER(TRIM(BOTH
        // FROM ?)),
        // LOWER(TRIM(BOTH FROM ?)), LOWER(TRIM(BOTH FROM ?))));
        StringBuilder tags = new StringBuilder();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            tags.append("LOWER(TRIM(BOTH FROM ?)), ");
        }
        tags.delete(tags.length() - 2, tags.length());

        res.append("INSERT INTO tagmap(bookmark_id, tag_id)\n")
                .append("\tSELECT * FROM\n\t\t(SELECT LOWER(?)), (SELECT id FROM tag WHERE name IN (")
                .append(tags).append("));");
        return res.toString();
    }

    public static final String UPDATE_BOOKMARK = "UPDATE bookmark SET name = TRIM(BOTH FROM ?), description = TRIM(BOTH FROM ?), address = TRIM(BOTH FROM ?), notes = TRIM(BOTH FROM ?), dateadded = ?, datemodified = ? WHERE id = ?;";
    public static final String DELETE_BOOKMARK = "DELETE FROM bookmark WHERE id = ?;";

    public static String getDeleteBookmarks(int n) {
        StringBuilder idPart = new StringBuilder();
        for (int i = 0; i < n; i++) {
            idPart.append("?, ");
        }
        idPart.delete(idPart.length() - 2, idPart.length());

        return "DELETE FROM bookmark WHERE id IN(" + idPart + ')';
    }

    public static final String DELETE_BOOKMARK_TAGS = "DELETE FROM tagmap WHERE bookmark_id = ?";
    public static final String SELECT_BOOKMARK_BY_ID = "SELECT * FROM bookmark WHERE id = ?;";
    public static final String SELECT_BOOKMARK_BY_ADDRESS = "SELECT * FROM bookmark WHERE address = ?";
    public static final String SELECT_BOOKMARKS = "SELECT * FROM bookmark;";
    public static final String SELECT_BOOKMARK_TAGS = "SELECT * FROM tag WHERE id IN (SELECT tag_id FROM tagmap WHERE bookmark_id = ?);";
    public static final String SELECT_UNTAG_BOOKMARKS = "SELECT * FROM bookmark WHERE id NOT IN (SELECT DISTINCT bookmark_id FROM tagmap);";
    public static final String SELECT_TAGS = "SELECT * FROM tag;";
    public static final String SELECT_ORPHANED_TAGS = "SELECT * FROM tag WHERE id NOT IN (SELECT DISTINCT tag_id FROM tagmap);";
    public static final String SELECT_MOST_POPULAR_TAG = "SELECT * FROM tag WHERE id IN(\n"
            + "\tSELECT tag_id FROM tagmap\n"
            + "\tGROUP BY tag_id\n"
            + "\tORDER BY COUNT(tag_id) DESC\n" + "\tLIMIT 1\n" + ");";
    public static final String SELECT_MOST_TAGGED_BOOKMARK = "SELECT * FROM bookmark WHERE id IN(\n"
            + "\tSELECT bookmark_id FROM tagmap\n"
            + "\tGROUP BY bookmark_id\n"
            + "\tORDER BY COUNT(bookmark_id) DESC\n" + "\tLIMIT 1\n" + ");";
    public static final String UPDATE_TAG = "UPDATE tag SET name = LOWER(TRIM(BOTH FROM ?)) WHERE id = ?;";
    public static final String REPLACE_TAG = "UPDATE tagmap SET tag_id = ? WHERE tag_id = ?";
    public static final String DELETE_TAG = "DELETE FROM tag WHERE id = ?;";
    public static final String DELETE_TAG_BOOKMARKS = "DELETE FROM bookmark WHERE id IN (SELECT bookmark_id FROM tagmap WHERE tag_id = ?);";
    public static final String DELETE_ORPHANED_TAGS = "DELETE FROM tag WHERE id NOT IN (SELECT DISTINCT tag_id FROM tagmap);";
    public static final String COUNT_BOOKMARKS = "SELECT COUNT(id) AS TOTAL FROM bookmark;";
    public static final String COUNT_UNTAG_BOOKMARKS = "SELECT COUNT(id) AS TOTAL FROM bookmark WHERE id NOT IN (SELECT DISTINCT bookmark_id FROM tagmap);";
    public static final String COUNT_TAGGED_BOOKMARKS = "SELECT COUNT(id) AS TOTAL FROM bookmark WHERE id IN (SELECT DISTINCT bookmark_id FROM tagmap);";
    public static final String COUNT_TAGS = "SELECT COUNT(id) AS TOTAL FROM tag;";
    public static final String COUNT_TAG_BOOKMARKS = "SELECT COUNT(bookmark_id) AS TOTAL FROM tagmap WHERE tag_id = ?";
    public static final String COUNT_BOOKMARK_TAGS = "SELECT COUNT(tag_id) AS TOTAL FROM tagmap WHERE bookmark_id = ?";
    public static final String COUNT_ORPHANED_TAGS = "SELECT COUNT(id) AS TOTAL FROM tag WHERE id NOT IN (SELECT DISTINCT tag_id FROM tagmap)";
    public static final String SEARCH_TAGS = "SELECT * FROM tag WHERE name LIKE ?";
    public static final String SEARCH_BOOMARKS_NAME = "SELECT * FROM bookmark WHERE LOWER(name) LIKE LOWER(?)";
    public static final String SEARCH_BOOKMARKS_DESCRIPTION = "SELECT * FROM bookmark WHERE LOWER(description) LIKE LOWER(?)";
    public static final String SEARCH_BOOKMARKS_ADDRESS = "SELECT * FROM bookmark WHERE address LIKE ?";

    public static String getSearchBookmarkByTag(int inTags, int outTags) {
        // SELECT * FROM bookmark
        // WHERE id IN (
        // SELECT bookmark_id FROM tagmap WHERE tag_id IN (
        // SELECT id FROM tag WHERE name in (?, ?, ?, ?)
        // )
        // GROUP BY bookmark_id
        // HAVING COUNT(bookmark_id) = 4
        // ) AND id NOT IN (
        // SELECT DISTINCT bookmark_id FROM tagmap WHERE tag_id IN (
        // SELECT id FROM tag WHERE name in (?, ?)
        // )
        // );
        StringBuilder inKeywords = new StringBuilder();
        StringBuilder outKeywords = new StringBuilder();

        StringBuilder inPart = new StringBuilder();
        StringBuilder outPart = new StringBuilder();

        StringBuilder res = new StringBuilder();

        if (inTags > 0) {
            for (int i = 0; i < inTags; ++i) {
                inKeywords.append("?, ");
            }
            inKeywords.delete(inKeywords.length() - 2, inKeywords.length());
            inPart.append("id IN (\n")
                    .append("\tSELECT bookmark_id FROM tagmap WHERE tag_id IN (\n")
                    .append("\t\tSELECT id FROM tag WHERE name in (")
                    .append(inKeywords)
                    .append(")\n\t)\n\tGROUP BY bookmark_id\n")
                    .append("\tHAVING COUNT(bookmark_id) = ").append(inTags)
                    .append("\n)");
        }

        if (outTags > 0) {
            for (int i = 0; i < outTags; ++i) {
                outKeywords.append("?, ");
            }
            outKeywords.delete(outKeywords.length() - 2, outKeywords.length());
            outPart.append("id NOT IN (\n")
                    .append("\tSELECT DISTINCT bookmark_id FROM tagmap WHERE tag_id IN (\n")
                    .append("\t\tSELECT id FROM tag WHERE name in (")
                    .append(outKeywords).append(")\n\t)\n)");
        }

        if (inTags == 0)
            if (outTags == 0)
                return SELECT_BOOKMARKS;
            else
                return res.append("SELECT * FROM bookmark\nWHERE ")
                        .append(outPart).append(';').toString();
        else if (outTags == 0)
            return res.append("SELECT * FROM bookmark\nWHERE ").append(inPart)
                    .append(';').toString();
        else
            return res.append("SELECT * FROM bookmark\nWHERE ").append(inPart)
                    .append(" AND ").append(outPart).append(';').toString();
    }

    public static String getSearchBookmarks(int inTags, int outTags,
                                            int searchMask) {
        /*
         * SELECT * FROM bookmark WHERE (id IN ( SELECT bookmark_id FROM tagmap
		 * WHERE tag_id IN ( SELECT id FROM tag WHERE name in (?, ?, ?, ?) )
		 * GROUP BY bookmark_id HAVING COUNT(bookmark_id) = 4 ) AND id NOT IN (
		 * SELECT DISTINCT bookmark_id FROM tagmap WHERE tag_id IN ( SELECT id
		 * FROM tag WHERE name in (?, ?) ) )) OR id NOT IN (SELECT DISTINCT
		 * bookmark_id FROM tagmap) OR LOWER(name) LIKE LOWER(?) OR
		 * LOWER(description) LIKE LOWER(?) OR address LIKE ? OR LOWER(notes)
		 * LIKE LOWER(?) ;
		 */
        String head = "SELECT * FROM bookmark WHERE\n";
        String or = "";

        StringBuilder inKeywords = new StringBuilder();
        StringBuilder outKeywords = new StringBuilder();

        StringBuilder inPart = new StringBuilder();
        StringBuilder outPart = new StringBuilder();

        StringBuilder res = new StringBuilder();

        if (inTags > 0) {
            for (int i = 0; i < inTags; ++i) {
                inKeywords.append("?, ");
            }
            inKeywords.delete(inKeywords.length() - 2, inKeywords.length());
            inPart.append("id IN (\n")
                    .append("\tSELECT bookmark_id FROM tagmap WHERE tag_id IN (\n")
                    .append("\t\tSELECT id FROM tag WHERE name in (")
                    .append(inKeywords)
                    .append(")\n\t)\n\tGROUP BY bookmark_id\n")
                    .append("\tHAVING COUNT(bookmark_id) = ").append(inTags)
                    .append("\n)");
        }

        if (outTags > 0) {
            for (int i = 0; i < outTags; ++i) {
                outKeywords.append("?, ");
            }
            outKeywords.delete(outKeywords.length() - 2, outKeywords.length());
            outPart.append("id NOT IN (\n")
                    .append("\tSELECT DISTINCT bookmark_id FROM tagmap WHERE tag_id IN (\n")
                    .append("\t\tSELECT id FROM tag WHERE name in (")
                    .append(outKeywords).append(")\n\t)\n)");
        }

        if (searchMask == 0) {
            return SELECT_BOOKMARKS;
        }
        if ((searchMask & TBConsts.SEARCH_TAG_MASK) == TBConsts.SEARCH_TAG_MASK) {
            if (inTags == 0) {
                if (outTags != 0) res.append(outPart);
            } else if (outTags == 0) {
                res.append(inPart);
            } else {
                res.append('(').append(inPart).append(" AND ").append(outPart)
                        .append(")\n");
            }
            or = "OR ";
        }
        if ((searchMask & TBConsts.SEARCH_NOTAG_MASK) == TBConsts.SEARCH_NOTAG_MASK) {
            res.append(or).append(
                    "id NOT IN (SELECT DISTINCT bookmark_id FROM tagmap)\n");
            or = "OR ";
        }
        if ((searchMask & TBConsts.SEARCH_NAME_MASK) == TBConsts.SEARCH_NAME_MASK) {
            res.append(or).append("LOWER(name) LIKE LOWER(?)\n");
            or = "OR ";
        }
        if ((searchMask & TBConsts.SEARCH_DESC_MASK) == TBConsts.SEARCH_DESC_MASK) {
            res.append(or).append("LOWER(description) LIKE LOWER(?)\n");
            or = "OR ";
        }
        if ((searchMask & TBConsts.SEARCH_ADDRESS_MASK) == TBConsts.SEARCH_ADDRESS_MASK) {
            res.append(or).append("address LIKE ?\n");
            or = "OR ";
        }
        if ((searchMask & TBConsts.SEARCH_NOTES_MASK) == TBConsts.SEARCH_NOTES_MASK) {
            res.append(or).append("LOWER(notes) LIKE LOWER(?)\n");
        }
        return res.length() == 0 ? SELECT_BOOKMARKS : res.insert(0, head)
                .append(';').toString();
    }

    public static void main(String[] args) {
        System.out.println("Testing SQL.java:");
        System.out
                .println(getSearchBookmarks(4, 2, TBConsts.SEARCH_ADDRESS_MASK
                        | TBConsts.SEARCH_DESC_MASK | TBConsts.SEARCH_NAME_MASK
                        | TBConsts.SEARCH_NOTAG_MASK
                        | TBConsts.SEARCH_NOTES_MASK | TBConsts.SEARCH_TAG_MASK));
    }
}
