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

import java.awt.*;

public class TBConsts {
    public static final boolean IsDesktopSupported = Desktop
            .isDesktopSupported();

    // meta data
    public static final String APP_TITLE = "TagBook";
    public static final String APP_VERSION = "0.3b";
    public static final String APP_DEV = "Anas H. Sulaiman";
    public static final String APP_HOME = "http://ahs.pw/app/tagbook/";
    public static final String DEV_HOME = "http://ahs.pw/";

    // dao consts
    public static final String DB_DRIVER = "org.h2.Driver";
    public static final String DB_FILE_EXT = "tb";
    public static final String H2DB_FILE_EXT = "h2.db";
    public static final String DB_BACKUP_FILE_EXT = "tbbk";
    public static final String EXPORT_IMPORT_FORMATS = "html;htm;csv;xml";

    // tags representation inside Bookmark#toArray
    public static final int TAGS_AS_STRING = 0;
    public static final int TAGS_AS_ARRAY = 1;

    // address representation inside Bookmark#toArray
    public static final int ADDRESS_AS_URI = 2;
    public static final int ADDRESS_AS_STRING = 3;

    // TBLog message types
    public static final int MESSAGE_ERROR = 4;
    public static final int MESSAGE_INFO = 5;

    // DAO inappropriate close decisions
    public static final int IAC_RECOVER = 6;
    public static final int IAC_OVERWRITE = 7;
    public static final int IAC_CANCEL = 8;
    public static final int IAC_PROMPT = 9;

    // import export file formats
    public static final int FORMAT_CSV = 10;
    public static final int FORMAT_XML = 11;
    public static final int FORMAT_DIIGO = 12;
    public static final int FORMAT_FIREFOX = 13;
    public static final int FORMAT_DELICIOUS = 14;
    public static final int FORMAT_IE = 15;
    public static final int EXPORT = 16;
    public static final int IMPORT = 17;
    public static final int EXPORT_ALL = 18;
    public static final int EXPORT_SELECTED = 19;

    // gui constants
    public static final int EDIT_INSERT = 20;
    public static final int EDIT_UPDATE = 21;

    // import export conflict methods
    public static final int CONFL_MERGE = 22;
    public static final int CONFL_REPLACE = 23;
    public static final int CONFL_MNR = 24;
    public static final int CONFL_NONE = 25;

    // dao flags
    public static final int DAO_SUCCESS = 26;
    public static final int DAO_ERROR = 27;
    public static final int DAO_ABORT = 28;

    // search flags
    public static final int SEARCH_TAG_MASK = 1 << 1;
    public static final int SEARCH_NAME_MASK = 1 << 2;
    public static final int SEARCH_DESC_MASK = 1 << 3;
    public static final int SEARCH_ADDRESS_MASK = 1 << 4;
    public static final int SEARCH_NOTES_MASK = 1 << 5;
    public static final int SEARCH_NOTAG_MASK = 1 << 6;

    // public static final int SERACH_TAG = 2;
    // public static final int SEARCH_NAME = 4;
    // public static final int SEARCH_DESC = 8;
    // public static final int SEARCH_ADDRESS = 16;
    // public static final int SEARCH_NOTES = 32;
    // public static final int SEARCH_NOTAG = 64;

    /**
     * comma ',', colon ':', semicolon ';'
     */
    public static final String TAG_INVALID_CHARS = ",:;";
}
