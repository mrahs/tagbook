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

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pw.ahs.app.tagbook.core.utils.*;

import javax.swing.*;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataAccessObject {
    // internal
    private Connection con;
    private AtomicBoolean abort;
    private AtomicBoolean working;
    private String dbUrl;

    // exposed
    private String dbPath;
    private Long idLastInsertUpdate;

    // external
    private TBLog log;

    /**
     * @param log the logger
     * @throws IllegalStateException if database driver could not be loaded
     */
    public DataAccessObject(TBLog log) {
        try {
            Class.forName(TBConsts.DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Database driver couldn't be loaded");
        }
        setLog(log);
        con = null;
        abort = new AtomicBoolean();
        working = new AtomicBoolean();
        dbUrl = null;
        dbPath = null;
        idLastInsertUpdate = -1L;
    }

    public TBLog getLog() {
        return log;
    }

    /**
     * @param log must not be null
     * @throws IllegalArgumentException if {@code log} is {@code null}
     */
    final void setLog(TBLog log) {
        if (log == null)
            throw new IllegalArgumentException("log cannot be null");
        this.log = log;
    }

    public String getDbPath() {
        return dbPath;
    }

    public Long getIdLastInsertUpdate() {
        return idLastInsertUpdate;
    }

    public void abort() {
        abort.set(true);
    }

    public void tryResetAbort() {
        if (!isWorking())
            abort.set(false);
    }

    public boolean isAborted() {
        return abort.get();
    }

    boolean isWorking() {
        return working.get();
    }

    private boolean isConnected() {
        if (con == null)
            return false;

        try {
            return !con.isClosed() && con.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean connect() {
        if (isConnected())
            return true;

        if (dbUrl == null) {
            log.error(TBError.getMessage("dao.badurl.m"),
                    TBError.getMessage("dao.badurl.t"));
            return false;
        }

        try {
            con = DriverManager.getConnection(dbUrl);
            con.setAutoCommit(false);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), TBError.getMessage("dao.con.t"));
            return false;
        }
    }

    private boolean disconnect() {
        if (!isConnected())
            return true;

        try {
            con.commit();
            con.close();
            return true;
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.discon.t"));
            return false;
        }
    }

    public boolean close() {
        if (!disconnect())
            return false;

        if (dbPath != null) { // we're connected to a database
            // check h2db file
            String h2dbPath = TBIOUtils.setFileExt(dbPath,
                    TBConsts.H2DB_FILE_EXT);
            if (TBIOUtils.isExistingFile(h2dbPath)) {
                String zippedPath = TBIOUtils.zipAfile(h2dbPath,
                        TBConsts.DB_FILE_EXT);
                if (zippedPath == null) {
                    log.error(TBError.getMessage("dao.file.m") + h2dbPath,
                            TBError.getMessage("dao.close.t"));
                    return false;
                }
                if (!TBIOUtils.deleteFile(h2dbPath)) {
                    log.error(TBError.getMessage("dao.file.m") + h2dbPath,
                            TBError.getMessage("dao.close.t"));
                }
            }
            dbPath = null;
            dbUrl = null;
        }
        return true;
    }

    public boolean createDb(String path) {
        if (isConnected())
            return false;

        // prepare file
        if (TBIOUtils.isVirtualFile(path))
            path = TBIOUtils.setFileExt(path, TBConsts.DB_FILE_EXT);

        if (!TBIOUtils.isTagBookFile(path, false)
                || !TBIOUtils.setFileWritable(path, false)) {
            log.error(TBError.getMessage("dao.file.m") + path,
                    TBError.getMessage("dao.createDb.t"));
            return false;
        }

        // file is ready
        dbPath = path;
        dbUrl = getDbUrlString(TBIOUtils.setFileExt(dbPath, ""), true);

        // connect to database
        if (!connect())
            return false;

        // initialize database
        try (PreparedStatement stmnt = con.prepareStatement(TBSql.INIT_DB)) {
            stmnt.execute();
            con.commit();
            return true;
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.createDb.t"));
            disconnect();
            deleteH2dbFile();
            return false;
        }
    }

    /**
     * @param path the database file path
     * @return true if successfully connected to database, false otherwise
     * @throws IllegalStateException if {@code iacDecision == TBConsts.IAC_PROMPT} and an iac is
     *                               detected.
     * @see TBConsts
     */
    public boolean loadDb(String path) {
        if (isConnected())
            return false;

        if (!TBIOUtils.isTagBookFile(path, true)) {
            log.error(TBError.getMessage("dao.file.m") + path,
                    TBError.getMessage("dao.loadDb.t"));
            return false;
        }

        String h2dbPath = TBIOUtils.setFileExt(path, TBConsts.H2DB_FILE_EXT);
        if (TBIOUtils.isExistingFile(h2dbPath)) {
            int i = JOptionPane.showOptionDialog(null,
                    TBError.getMessage("iac.m"), TBError.getMessage("iac.t"),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null, new String[]{
                    "Recover", "Overwrite", "Ignore"}, "Recover");
            switch (i) {
                case JOptionPane.YES_OPTION:
                    break;
                case JOptionPane.NO_OPTION:
                    h2dbPath = null;
                    break;
                case JOptionPane.CANCEL_OPTION:
                case JOptionPane.CLOSED_OPTION:
                    return false;
            }
        } else {
            h2dbPath = null;
        }

        if (h2dbPath == null) { // no iac detected || overwrite is requested
            h2dbPath = TBIOUtils.unzipAfile(path);
            if (h2dbPath == null) {
                log.error(TBError.getMessage("dao.file.m") + null,
                        TBError.getMessage("dao.loadDb.t"));
                return false;
            }
        } // else, we're ready to connect

        // create dbUrl
        dbPath = path;
        dbUrl = getDbUrlString(TBIOUtils.setFileExt(h2dbPath, ""), false);

        // connect
        if (!connect()) {
            TBIOUtils.deleteFile(h2dbPath);
            return false;
        }

        // validate db
        boolean tagTableExist = false, bookmarkTableExist = false, tagmapTableExist = false;
        try {
            DatabaseMetaData dbmd = con.getMetaData();
            ResultSet rs = dbmd.getTables(null, null, "%",
                    new String[]{"TABLE"});
            while (rs.next()) {
                switch (rs.getString("TABLE_NAME")) {
                    case "TAG":
                        tagTableExist = true;
                        break;
                    case "BOOKMARK":
                        bookmarkTableExist = true;
                        break;
                    case "TAGMAP":
                        tagmapTableExist = true;
                        break;
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.loadDb.t"));
            disconnect();
            TBIOUtils.deleteFile(h2dbPath);
            return false;
        }
        if (tagTableExist && bookmarkTableExist && tagmapTableExist) {
            return true;
        } else {
            log.error(TBError.getMessage("dao.loadDb.m"),
                    TBError.getMessage("dao.loadDb.t"));
            disconnect();
            TBIOUtils.deleteFile(h2dbPath);
            return false;
        }
    }

    public boolean backup(String path) {
        if (!isConnected())
            return false;

        // prepare file
        if (TBIOUtils.isVirtualFile(path))
            path = TBIOUtils.setFileExt(path, TBConsts.DB_BACKUP_FILE_EXT);
        if (!TBIOUtils.isTagBookBackupFile(path)
                || !TBIOUtils.setFileWritable(path, false)) {
            log.error(TBError.getMessage("dao.file.m") + path,
                    TBError.getMessage("dao.backup.t"));
            return false;
        }

        try (PreparedStatement stmnt = con.prepareStatement(TBSql.BACKUP_DB)) {
            stmnt.setString(1, path);
            return stmnt.execute();
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.backup.t"));
            return false;
        }
    }

    public boolean restore(String path) {
        if (!isConnected())
            return false;
        if (!TBIOUtils.isTagBookBackupFile(path)
                || !TBIOUtils.setFileReadable(path)) {
            log.error(TBError.getMessage("dao.file.m") + path,
                    TBError.getMessage("dao.restore.t"));
            return false;
        }
        try (PreparedStatement restoreDb = con
                .prepareStatement(TBSql.RESTORE_DB);
             PreparedStatement clearDb = con
                     .prepareStatement(TBSql.CLEAR_DB)) {
            clearDb.execute();
            restoreDb.setString(1, path);
            restoreDb.execute();
            con.commit();
            return true;
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.restore.t"));
            try {
                con.rollback();
            } catch (SQLException ex) {
                log.error(e.getMessage(), TBError.getMessage("dao.rollback.t"));
            }
            return false;
        }
    }

    @SuppressWarnings("resource")
    public int importBookmarks(String path, int format, int confMethod,
                               String[] importedTags, TBTable table) {
        if (!isConnected())
            return TBConsts.DAO_ERROR;
        if (!TBIOUtils.isTagBookImportExportFile(path)
                || !TBIOUtils.setFileReadable(path)) {
            log.error(TBError.getMessage("dao.file.m") + path,
                    TBError.getMessage("dao.import.t"));
            return TBConsts.DAO_ERROR;
        }

        CSVReader csvReader = null;
        String[] csvLine;
        XMLEventReader eventReader = null;
        Document doc;
        Elements dts;
        String[] data = new String[6];
        String[] tags = null;
        start();
        try {
            switch (format) {
                case TBConsts.FORMAT_CSV:
                    csvReader = new CSVReader(new InputStreamReader(
                            new FileInputStream(path), "UTF-8"));
                    csvLine = csvReader.readNext();
                    if (csvLine == null || csvLine.length < 7) {
                        done();
                        return TBConsts.DAO_ERROR;
                    }
                    while ((csvLine = csvReader.readNext()) != null) {
                        if (csvLine.length < 7)
                            continue;
                        if (csvLine[2].isEmpty())
                            continue; // no address

                        data[0] = csvLine[0].isEmpty() ? "Unnamed" : csvLine[0]; // name
                        data[1] = csvLine[1]; // desc
                        data[2] = csvLine[2]; // address
                        data[3] = csvLine[3]; // notes
                        data[4] = csvLine[5]; // dateadded
                        data[5] = csvLine[6]; // datemodified
                        tags = csvLine[4].isEmpty() ? null : csvLine[4]
                                .split("\\s");

                        importIt(data, tags, importedTags, confMethod, table);
                        if (abort.get()) {
                            done();
                            return TBConsts.DAO_ABORT;
                        }
                    }
                    done();
                    return TBConsts.DAO_SUCCESS;
                case TBConsts.FORMAT_XML:
                    eventReader = XMLInputFactory.newInstance()
                            .createXMLEventReader(new FileInputStream(path),
                                    "UTF-8");
                    for (int i = 0; i < data.length; i++)
                        data[i] = null;
                    tags = null;
                    while (eventReader.hasNext()) {
                        XMLEvent event = eventReader.nextEvent();
                        XMLEvent nextEvent = eventReader.nextEvent();
                        if (!nextEvent.isCharacters()) continue;
                        String text = nextEvent.asCharacters().getData();

                        if (event.isStartElement()) {
                            String eventName = event.asStartElement().getName()
                                    .getLocalPart();

                            switch (eventName) {
                                case "name":
                                    data[0] = text;
                                    break;
                                case "desc":
                                    data[1] = text;
                                    break;
                                case "data":
                                    data[2] = text;
                                    break;
                                case "notes":
                                    data[3] = text;
                                    break;
                                case "tags":
                                    if (StringUtils.isNotBlank(text))
                                        tags = text.split("\\s");
                                    break;
                                case "dateadded":
                                    data[4] = text;
                                    break;
                                case "datemodified":
                                    data[5] = text;
                                    break;
                            }

                        } else if (event.isEndElement()) {
                            if (event.asEndElement().getName().getLocalPart()
                                    .equals("bookmark")
                                    && data[2] != null) {
                                if (data[0] == null) data[0] = "Unnamed";
                                importIt(data, tags, importedTags, confMethod,
                                        table);

                                for (int i = 0; i < data.length; i++)
                                    data[i] = null;
                                tags = null;
                            }
                        }
                        if (abort.get()) {
                            done();
                            return TBConsts.DAO_ABORT;
                        }
                    }
                    done();
                    return TBConsts.DAO_SUCCESS;
                case TBConsts.FORMAT_DIIGO:
                    csvReader = new CSVReader(new InputStreamReader(
                            new FileInputStream(path), "UTF-8"));
                    csvLine = csvReader.readNext();
                    if (csvLine == null || csvLine.length < 4) {
                        done();
                        return TBConsts.DAO_ERROR;
                    }
                    while ((csvLine = csvReader.readNext()) != null) {
                        if (csvLine.length < 4)
                            continue;

                        if (csvLine[1].isEmpty())
                            continue; // no address

                        data[0] = csvLine[0].isEmpty() ? "Unnamed" : csvLine[0]; // name
                        data[1] = csvLine[3]; // desc
                        data[2] = csvLine[1]; // address

                        tags = csvLine[4].isEmpty() ? null : csvLine[4]
                                .split("\\s");
                        importIt(data, tags, importedTags, confMethod, table);
                        if (abort.get()) {
                            done();
                            return TBConsts.DAO_ABORT;
                        }
                    }
                    done();
                    return TBConsts.DAO_SUCCESS;
                case TBConsts.FORMAT_FIREFOX:
                case TBConsts.FORMAT_DELICIOUS:
                case TBConsts.FORMAT_IE:
                    doc = Jsoup.parse(new File(path), "UTF-8", "");
                    dts = doc.select("dt");
                    if (!dts.isEmpty()) {
                        for (Element dt : dts) {
                            Element link = dt.select("a[href]").first();
                            if (link == null)
                                continue;
                            Element dd = dt.nextElementSibling();
                            data[0] = link.text(); // name
                            data[2] = link.attr("href"); // address
                            if (data[2].isEmpty())
                                continue; // no address

                            if (data[0].isEmpty()) data[0] = "Unnamed";

                            if (dd != null && dd.tagName().equals("dd")) {
                                data[1] = dd.text(); // desc
                            } else {
                                data[1] = "";
                            }
                            String ts = link.attr("tags");
                            if (StringUtils.isNotBlank(ts))
                                tags = ts.split(",");

                            importIt(data, tags, importedTags, confMethod, table);
                            if (abort.get()) {
                                done();
                                return TBConsts.DAO_ABORT;
                            }
                        }
                    }
                    done();
                    return TBConsts.DAO_SUCCESS;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), TBError.getMessage("dao.import.t"));
            done();
            return TBConsts.DAO_ERROR;
        } finally {
            try {
                if (csvReader != null)
                    csvReader.close();
                if (eventReader != null)
                    eventReader.close();
            } catch (Exception e) {
                log.error(e.getMessage(), TBError.getMessage("dao.import.t"));
            }
        }
        return TBConsts.DAO_ERROR;
    }

    private void importIt(String[] data, String[] tags, String[] importedTags,
                          int confMethod, TBTable table) {
        BookmarkI b = new BookmarkI(BookmarkI.NULL_ID, data[0], data[2]);
        Timestamp addDate;
        Timestamp modiDate;
        try {
            addDate = new Timestamp(Long.parseLong(data[4]));
        } catch (NumberFormatException e) {
            addDate = null;
        }
        try {
            modiDate = new Timestamp(Long.parseLong(data[5]));
        } catch (NumberFormatException e) {
            modiDate = null;
        }

        if (StringUtils.isNotBlank(data[1]))
            b.setDesc(data[1]);
        if (StringUtils.isNotBlank(data[3]))
            b.setNotes(data[3]);
        b.setDateadded(addDate);
        b.setDatemodified(modiDate);

        if (tags != null)
            for (String t : tags)
                b.addTag(new Tag(t));
        if (importedTags != null)
            for (String t : importedTags)
                b.addTag(new Tag(t));

        if (bookmarkExists(b.getAddress())) {
            BookmarkI oldBookmark = getBookmark(b.getAddress());
            switch (confMethod) {
                case TBConsts.CONFL_MERGE:
                    oldBookmark.setDatemodified(new Timestamp(System
                            .currentTimeMillis()));
                    oldBookmark.addTags(b.getTags());
                    oldBookmark.appendNote(b.getNotes());
                    insertUpdateBookmark(oldBookmark);
                    break;
                case TBConsts.CONFL_REPLACE:
                    b.setDatemodified(new Timestamp(System.currentTimeMillis()));
                    insertUpdateBookmark(new BookmarkI(b, oldBookmark.getId()));
                    break;
                case TBConsts.CONFL_MNR:
                    oldBookmark.setDatemodified(new Timestamp(System
                            .currentTimeMillis()));
                    oldBookmark.setName(b.getName());
                    oldBookmark.setDesc(b.getDesc());
                    oldBookmark.addTags(b.getTags());
                    oldBookmark.appendNote(b.getNotes());
                    insertUpdateBookmark(oldBookmark);
                    break;
                case TBConsts.CONFL_NONE:
                    break;
            }
        } else {
            insertUpdateBookmark(b);
            b.setId(getIdLastInsertUpdate());
            if (table != null) {
                table.addRow(b.toArray(TBConsts.ADDRESS_AS_URI,
                        TBConsts.TAGS_AS_STRING));
            }
        }
    }

    @SuppressWarnings("resource")
    public int exportBookmarks(String path, int format, Long[] ids) {
        if (!isConnected())
            return TBConsts.DAO_ERROR;
        if (TBIOUtils.isVirtualFile(path)) {
            String ext;
            switch (format) {
                case TBConsts.FORMAT_CSV:
                case TBConsts.FORMAT_DIIGO:
                    ext = "csv";
                    break;
                case TBConsts.FORMAT_XML:
                    ext = "xml";
                    break;
                case TBConsts.FORMAT_DELICIOUS:
                case TBConsts.FORMAT_FIREFOX:
                case TBConsts.FORMAT_IE:
                    ext = "html";
                    break;
                default:
                    ext = "txt";
            }
            path = TBIOUtils.setFileExt(path, ext);
        }
        if (!TBIOUtils.isTagBookImportExportFile(path)
                || !TBIOUtils.setFileWritable(path, false)) {
            log.error(TBError.getMessage("dao.file.m") + path,
                    TBError.getMessage("dao.export.t"));
            return TBConsts.DAO_ERROR;
        }

        start();
        List<Bookmark> bookmarks = new ArrayList<>();
        if (ids == null) {
            List<BookmarkI> tmp = getBookmarks(null);
            if (tmp != null)
                bookmarks.addAll(tmp);
        } else {
            bookmarks = new ArrayList<>(ids.length);
            for (Long id : ids) {
                Bookmark bkmrk = getBookmark(id);
                if (bkmrk != null) {
                    bookmarks.add(bkmrk);
                }
            }
        }

        String htmlHeader = null;
        if (FilenameUtils.isExtension(path, new String[]{"html", "html"}))
            htmlHeader = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\n" + "<!-- This is an automatically generated file.\n" + "\tIt will be read and overwritten.\n" + "\tDO NOT EDIT! -->\n" + "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n" + "<TITLE>Bookmarks</TITLE>\n";

        CSVWriter writer = null;
        XMLEventWriter eventWriter = null;
        UrlValidator urlValidator = UrlValidator.getInstance();
        DomainValidator domainvalidator = DomainValidator.getInstance();
        StringBuilder docString;
        boolean state;
        try {
            switch (format) {
                case TBConsts.FORMAT_CSV:
                    writer = new CSVWriter(new OutputStreamWriter(
                            new FileOutputStream(path), "UTF-8"));
                    writer.writeNext(new String[]{"Name", "Description",
                            "Data", "Notes", "Tags", "Date Added",
                            "Date Modified"});
                    for (Bookmark bookmark : bookmarks) {
                        writer.writeNext(new String[]{
                                bookmark.getName(),
                                bookmark.getDesc() == null ? "" : bookmark
                                        .getDesc(),
                                bookmark.getAddress(),
                                bookmark.getNotes() == null ? "" : bookmark
                                        .getNotes(),
                                bookmark.getTagsString(" "),
                                String.valueOf(bookmark.getDateadded().getTime()),
                                String.valueOf(bookmark.getDatemodified().getTime())});
                        if (abort.get()) {
                            done();
                            return TBConsts.DAO_ABORT;
                        }
                    }
                    writer.close();
                    done();
                    return TBConsts.DAO_SUCCESS;
                case TBConsts.FORMAT_XML:
                    eventWriter = XMLOutputFactory.newInstance()
                            .createXMLEventWriter(new FileOutputStream(path),
                                    "UTF-8");
                    XMLEventFactory eventFactory = XMLEventFactory.newInstance();
                    XMLEvent endline = eventFactory.createDTD("\n");
                    XMLEvent tab = eventFactory.createDTD("\t");

                    eventWriter.add(eventFactory.createStartDocument("UTF-8",
                            "1.0", true));
                    eventWriter.add(endline);

                    eventWriter.add(eventFactory.createStartElement("", "",
                            "bookmarks"));
                    eventWriter.add(endline);

                    long i = 1L;
                    for (Bookmark bookmark : bookmarks) {
                        eventWriter.add(tab);
                        eventWriter.add(eventFactory.createStartElement(
                                "",
                                "",
                                "bookmark",
                                Arrays.asList(
                                        eventFactory.createAttribute("index",
                                                String.valueOf(i))).iterator(),
                                null));
                        eventWriter.add(endline);
                        ++i;

                        eventWriter.add(tab);
                        eventWriter.add(tab);
                        eventWriter.add(eventFactory.createStartElement("", "",
                                "name"));
                        eventWriter.add(eventFactory.createCharacters(bookmark
                                .getName()));
                        eventWriter.add(eventFactory.createEndElement("", "",
                                "name"));
                        eventWriter.add(endline);

                        String desc = bookmark.getDesc();
                        if (desc != null) {
                            eventWriter.add(tab);
                            eventWriter.add(tab);
                            eventWriter.add(eventFactory.createStartElement("", "",
                                    "desc"));
                            eventWriter.add(eventFactory.createCharacters(desc));
                            eventWriter.add(eventFactory.createEndElement("", "",
                                    "desc"));
                            eventWriter.add(endline);
                        }

                        eventWriter.add(tab);
                        eventWriter.add(tab);
                        eventWriter.add(eventFactory.createStartElement("", "",
                                "data"));
                        eventWriter.add(eventFactory.createCharacters(bookmark
                                .getAddress()));
                        eventWriter.add(eventFactory.createEndElement("", "",
                                "data"));
                        eventWriter.add(endline);

                        String notes = bookmark.getNotes();
                        if (notes != null) {
                            eventWriter.add(tab);
                            eventWriter.add(tab);
                            eventWriter.add(eventFactory.createStartElement("", "",
                                    "notes"));
                            eventWriter.add(eventFactory.createCharacters(notes));
                            eventWriter.add(eventFactory.createEndElement("", "",
                                    "notes"));
                            eventWriter.add(endline);
                        }

                        String tags = bookmark.getTagsString(" ");
                        if (!tags.isEmpty()) {
                            eventWriter.add(tab);
                            eventWriter.add(tab);
                            eventWriter.add(eventFactory.createStartElement("", "",
                                    "tags"));
                            eventWriter.add(eventFactory.createCharacters(tags));
                            eventWriter.add(eventFactory.createEndElement("", "",
                                    "tags"));
                            eventWriter.add(endline);
                        }

                        eventWriter.add(tab);
                        eventWriter.add(tab);
                        eventWriter.add(eventFactory.createStartElement("", "",
                                "dateadded"));
                        eventWriter.add(eventFactory.createCharacters(String
                                .valueOf(bookmark.getDateadded().getTime())));
                        eventWriter.add(eventFactory.createEndElement("", "",
                                "dateadded"));
                        eventWriter.add(endline);

                        eventWriter.add(tab);
                        eventWriter.add(tab);
                        eventWriter.add(eventFactory.createStartElement("", "",
                                "datemodified"));
                        eventWriter.add(eventFactory.createCharacters(String
                                .valueOf(bookmark.getDatemodified().getTime())));
                        eventWriter.add(eventFactory.createEndElement("", "",
                                "datemodified"));
                        eventWriter.add(endline);

                        eventWriter.add(tab);
                        eventWriter.add(eventFactory.createEndElement("", "",
                                "bookmark"));
                        eventWriter.add(endline);
                        if (abort.get()) {
                            eventWriter.add(eventFactory.createEndElement("", "",
                                    "bookmarks"));
                            eventWriter.add(eventFactory.createEndDocument());
                            done();
                            return TBConsts.DAO_ABORT;
                        }
                    }
                    eventWriter.add(eventFactory.createEndElement("", "",
                            "bookmarks"));
                    eventWriter.add(eventFactory.createEndDocument());
                    done();
                    eventWriter.close();
                    return TBConsts.DAO_SUCCESS;
                case TBConsts.FORMAT_DIIGO:
                    writer = new CSVWriter(new OutputStreamWriter(
                            new FileOutputStream(path), "UTF-8"));
                    writer.writeNext(new String[]{"title", "url", "tags",
                            "comments", "annotations"});
                    for (Bookmark bookmark : bookmarks) {
                        writer.writeNext(new String[]{
                                bookmark.getName(),
                                bookmark.getAddress(),
                                bookmark.getTagsString(" "),
                                (bookmark.getDesc() == null ? "" : bookmark
                                        .getDesc())
                                        + (bookmark.getNotes() == null ? ""
                                        : bookmark.getNotes()), ""});
                        if (abort.get()) {
                            done();
                            return TBConsts.DAO_ABORT;
                        }
                    }
                    writer.close();
                    done();
                    return TBConsts.DAO_SUCCESS;
                case TBConsts.FORMAT_FIREFOX:
                    docString = new StringBuilder(htmlHeader);
                    docString
                            .append("<DL><p>\n")
                            .append("\t<DT><H3 ADD_DATE=\"\" LAST_MODIFIED=\"\">Bookmark Bookmarks</H3>\n")
                            .append("\t<DD>Boomarks from the Bookmark Application\n")
                            .append("\t<DL><p>\n");
                    for (Bookmark bookmark : bookmarks) {
                        if (urlValidator.isValid(bookmark.getAddress())
                                || domainvalidator.isValid(bookmark.getAddress())) {
                            docString.append("\t<DT><A HREF=\"")
                                    .append(bookmark.getAddress()).append("\"")
                                    .append(" ADD_DATE=\"\" LAST_MODIFIED=\"\">")
                                    .append(bookmark.getName()).append("</A>\n");
                            if (bookmark.getDesc() != null) {
                                docString.append("\t<DD>")
                                        .append(bookmark.getDesc()).append("\n");
                            }
                        }
                        if (abort.get()) {
                            docString.append("\t</DL><p>\n</DL><p>\n");
                            state = TBIOUtils.writeTextFile(docString.toString(),
                                    path);
                            done();
                            return state ? TBConsts.DAO_ABORT : TBConsts.DAO_ERROR;
                        }
                    }
                    docString.append("\t</DL><p>\n</DL><p>\n");
                    state = TBIOUtils.writeTextFile(docString.toString(), path);
                    done();
                    return state ? TBConsts.DAO_SUCCESS : TBConsts.DAO_ERROR;
                case TBConsts.FORMAT_DELICIOUS:
                    docString = new StringBuilder(htmlHeader);
                    docString.append("<DL><p>\n");
                    for (Bookmark bookmark : bookmarks) {
                        if (urlValidator.isValid(bookmark.getAddress())
                                || domainvalidator.isValid(bookmark.getAddress())) {
                            docString.append("\t\t<DT><A HREF=\"")
                                    .append(bookmark.getAddress()).append("\"")
                                    .append(" LAST_VISIT=\"\"")
                                    .append(" ADD_DATE=\"\" PRIVATE=\"0\"")
                                    .append(" TAGS=\"")
                                    .append(bookmark.getTagsString(","))
                                    .append("\">").append(bookmark.getName())
                                    .append("</A>\n");
                            if (bookmark.getDesc() != null) {
                                docString.append("\t\t<DD>")
                                        .append(bookmark.getDesc()).append("\n");
                            }
                        }
                        if (abort.get()) {
                            docString.append("</DL><p>\n");
                            state = TBIOUtils.writeTextFile(docString.toString(),
                                    path);
                            done();
                            return state ? TBConsts.DAO_ABORT : TBConsts.DAO_ERROR;
                        }
                    }
                    docString.append("</DL><p>\n");
                    state = TBIOUtils.writeTextFile(docString.toString(), path);
                    done();
                    return state ? TBConsts.DAO_ABORT : TBConsts.DAO_ERROR;
                case TBConsts.FORMAT_IE:
                    docString = new StringBuilder(htmlHeader);
                    docString
                            .append("<DL><p>\n")
                            .append("\t<DT><H3 FOLDED ADD_DATE=\"\">Bookmark Bookmarks</H3>\n")
                            .append("\t<DL><p>\n");
                    for (Bookmark bookmark : bookmarks) {
                        if (urlValidator.isValid(bookmark.getAddress())
                                || domainvalidator.isValid(bookmark.getAddress())) {
                            docString.append("\t\t<DT><A HREF=\"")
                                    .append(bookmark.getAddress()).append("\"")
                                    .append(" ADD_DATE=\"")
                                    .append(bookmark.getDateadded().getTime())
                                    .append("\" LAST_VISIT=\"\"")
                                    .append("LAST_MODIFIED=\"")
                                    .append(bookmark.getDatemodified().getTime())
                                    .append("\">").append(bookmark.getName())
                                    .append("</A>\n");
                        }
                        if (abort.get()) {
                            docString.append("\t</DL><p>\n</DL><p>\n");
                            state = TBIOUtils.writeTextFile(docString.toString(),
                                    path);
                            done();
                            return state ? TBConsts.DAO_ABORT : TBConsts.DAO_ERROR;
                        }
                    }
                    docString.append("\t</DL><p>\n</DL><p>\n");
                    state = TBIOUtils.writeTextFile(docString.toString(), path);
                    done();
                    return state ? TBConsts.DAO_ABORT : TBConsts.DAO_ERROR;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), TBError.getMessage("dao.export.t"));
            TBIOUtils.deleteFile(path);
            return TBConsts.DAO_ERROR;
        } finally {
            try {
                if (writer != null)
                    writer.close();
                if (eventWriter != null)
                    eventWriter.close();
            } catch (IOException | XMLStreamException e) {
                log.error(e.getMessage(), TBError.getMessage("dao.export.t"));
                TBIOUtils.deleteFile(path);
            }
        }
        return TBConsts.DAO_ERROR;
    }

    public BookmarkI getBookmark(Long id) {
        if (!isConnected())
            return null;

        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.SELECT_BOOKMARK_BY_ID)) {
            stmnt.setLong(1, id);
            List<BookmarkI> res = executeSelectBookmarks(stmnt, null);
            if (res == null)
                return null;
            return res.get(0);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    public BookmarkI getBookmark(String address) {
        if (!isConnected())
            return null;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.SELECT_BOOKMARK_BY_ADDRESS)) {
            stmnt.setString(1, address);
            List<BookmarkI> res = executeSelectBookmarks(stmnt, null);
            if (res == null)
                return null;
            return res.get(0);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    public BookmarkI getMostTaggedBookmark() {
        if (!isConnected())
            return null;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.SELECT_MOST_TAGGED_BOOKMARK)) {
            List<BookmarkI> res = executeSelectBookmarks(stmnt, null);
            if (res == null)
                return null;
            return res.get(0);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    public List<BookmarkI> getBookmarks(TBTable table) {
        if (!isConnected())
            return null;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.SELECT_BOOKMARKS)) {
            return executeSelectBookmarks(stmnt, table);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    List<BookmarkI> getUntaggedBookmarks(TBTable table) {
        if (!isConnected())
            return null;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.SELECT_UNTAG_BOOKMARKS)) {
            return executeSelectBookmarks(stmnt, table);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    public Set<TagI> getTags(TBTable table) {
        if (!isConnected())
            return null;
        try (PreparedStatement stmnt = con.prepareStatement(TBSql.SELECT_TAGS)) {
            return executeSelectTags(stmnt, table);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    public Set<TagI> getOrphanedTags(TBTable table) {
        if (!isConnected())
            return null;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.SELECT_ORPHANED_TAGS)) {
            return executeSelectTags(stmnt, table);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    public Set<CountedTag> getTagsWithCounts(TBTable table) {
        if (!isConnected())
            return null;
        try (PreparedStatement stmnt = con.prepareStatement(TBSql.SELECT_TAGS)) {
            return executeSelectCountedTags(stmnt, table);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    Set<CountedTag> getOrphanedTagsWithCounts(TBTable table) {
        if (!isConnected())
            return null;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.SELECT_ORPHANED_TAGS)) {
            return executeSelectCountedTags(stmnt, table);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    Set<TagI> getBookmarkTags(Long id) {
        if (!isConnected())
            return null;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.SELECT_BOOKMARK_TAGS)) {
            stmnt.setLong(1, id);
            return executeSelectTags(stmnt, null);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    public TagI getMostPopularTag() {
        if (!isConnected())
            return null;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.SELECT_MOST_POPULAR_TAG)) {
            ResultSet rs = stmnt.executeQuery();
            if (!rs.isBeforeFirst())
                return null;

            rs.next();
            return new TagI(rs.getLong("id"), rs.getString("name"));
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    public long getBookmarksCount() {
        return executeCount(TBSql.COUNT_BOOKMARKS);
    }

    public long getTagsCount() {
        return executeCount(TBSql.COUNT_TAGS);
    }

    public int getTagBookmarksCount(Long id) {
        if (!isConnected())
            return 0;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.COUNT_TAG_BOOKMARKS)) {
            stmnt.setLong(1, id);
            return (int) executeCount(stmnt);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return 0;
        }
    }

    public int getBookmarkTagsCount(Long id) {
        if (!isConnected())
            return 0;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.COUNT_BOOKMARK_TAGS)) {
            stmnt.setLong(1, id);
            return (int) executeCount(stmnt);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return 0;
        }
    }

    public long getOrphanedTagsCount() {
        return executeCount(TBSql.COUNT_ORPHANED_TAGS);
    }

    public long getUntaggedBookmarksCount() {
        return executeCount(TBSql.COUNT_UNTAG_BOOKMARKS);
    }

    public long getTaggedBookmarksCount() {
        return executeCount(TBSql.COUNT_TAGGED_BOOKMARKS);
    }

    public long getLinkBookmarksCount() {
        if (!isConnected())
            return 0;
        long count = 0;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.SELECT_BOOKMARKS)) {
            ResultSet rs = stmnt.executeQuery();
            if (!rs.isBeforeFirst())
                return 0;
            while (rs.next()) {
                if (Bookmark.isLink(rs.getString("address"), true)) {
                    ++count;
                }
            }
            return count;
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return 0;
        }
    }

    public long getEmailBookmarksCount() {
        if (!isConnected())
            return 0;
        long count = 0;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.SELECT_BOOKMARKS)) {
            ResultSet rs = stmnt.executeQuery();
            if (!rs.isBeforeFirst())
                return 0;
            while (rs.next()) {
                if (Bookmark.isEmail(rs.getString("address"))) {
                    ++count;
                }
            }
            return count;
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return 0;
        }
    }

    public boolean bookmarkExists(String address) {
        if (!isConnected())
            return false;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.SELECT_BOOKMARK_BY_ADDRESS)) {
            stmnt.setString(1, address);
            ResultSet rs = stmnt.executeQuery();
            return rs.isBeforeFirst();
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return false;
        }
    }

    public boolean insertUpdateBookmark(BookmarkI bookmark) {
        if (!isConnected())
            return false;
        Long id = bookmark.getId();
        try (PreparedStatement insertUpdateStmnt = con.prepareStatement(
                id == BookmarkI.NULL_ID ? TBSql.INSERT_BOOKMARK
                        : TBSql.UPDATE_BOOKMARK,
                PreparedStatement.RETURN_GENERATED_KEYS)) {

            // insert|update bookmark without tags
            insertUpdateStmnt.setString(1, bookmark.getName());
            insertUpdateStmnt.setString(2, bookmark.getDesc());
            insertUpdateStmnt.setString(3, bookmark.getAddress());
            insertUpdateStmnt.setString(4, bookmark.getNotes());
            insertUpdateStmnt.setTimestamp(5, bookmark.getDateadded());
            insertUpdateStmnt.setTimestamp(6, bookmark.getDatemodified());
            if (id != BookmarkI.NULL_ID) {
                insertUpdateStmnt.setLong(7, id);
                try (PreparedStatement delTags = con
                        .prepareStatement(TBSql.DELETE_BOOKMARK_TAGS)) {
                    delTags.setLong(1, id);
                    delTags.execute();
                }
            } else {
                insertUpdateStmnt.setString(7, bookmark.getAddress());
            }
            insertUpdateStmnt.execute();

            // retrieve last insert|update id
            if (id == BookmarkI.NULL_ID) {
                ResultSet rs = insertUpdateStmnt.getGeneratedKeys();
                if (!rs.isBeforeFirst()) { // address already exists, nothing
                    // inserted
                    return true;
                }
                rs.next();
                id = rs.getLong(1);
            }

            // insert bookmark tags
            String[] tags = bookmark.getTagsString(" ").split("\\s");
            try (PreparedStatement insertBookmarkTagsStmnt = con
                    .prepareStatement(
                            TBSql.getInsertBookmarkTags(tags.length),
                            PreparedStatement.RETURN_GENERATED_KEYS);
                 PreparedStatement insertBookmarkTagsMapStmnt = con
                         .prepareStatement(TBSql
                                 .getInsertBookmarkTagsMap(tags.length))) {

                for (int i = 0; i < tags.length * 2; ++i) {
                    insertBookmarkTagsStmnt.setString(i + 1, tags[i
                            % tags.length]);
                }

                insertBookmarkTagsMapStmnt.setLong(1, id);
                for (int i = 0; i < tags.length; ++i) {
                    insertBookmarkTagsMapStmnt.setString(i + 2, tags[i]);
                }

                insertBookmarkTagsStmnt.execute();
                insertBookmarkTagsMapStmnt.execute();
            }

            con.commit();
            idLastInsertUpdate = id;
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), TBError.getMessage("dao.mod.t"));
            try {
                con.rollback();
            } catch (Exception ex) {
                log.error(e.getMessage(), TBError.getMessage("dao.rollback.t"));
            }
            return false;
        }
    }

    public int tagBookmarks(Long[] ids, String[] tags) {
        if (!isConnected())
            return TBConsts.DAO_ERROR;
        boolean state = true;
        start();
        try (PreparedStatement insertTagsStmnt = con.prepareStatement(TBSql
                .getInsertBookmarkTags(tags.length));
             PreparedStatement insertTagsMapStmnt = con
                     .prepareStatement(TBSql
                             .getInsertBookmarkTagsMap(tags.length))) {

            for (int i = 0; i < tags.length * 2; ++i) {
                insertTagsStmnt.setString(i + 1, tags[i % tags.length]);
            }

            insertTagsStmnt.execute();

            for (int i = 0; i < tags.length; ++i) {
                insertTagsMapStmnt.setString(i + 2, tags[i]);
            }

            for (Long id : ids) {
                insertTagsMapStmnt.setLong(1, id);
                insertTagsMapStmnt.execute();
                if (abort.get()) {
                    done();
                    state = false;
                    break;
                }
            }
            if (state) {
                con.commit();
                return TBConsts.DAO_SUCCESS;
            } else {
                con.rollback();
                return TBConsts.DAO_ABORT;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.mod.t"));
            try {
                con.rollback();
            } catch (Exception ex) {
                log.error(e.getMessage(), TBError.getMessage("dao.rollback.t"));
            }
            return TBConsts.DAO_ERROR;
        }
    }

    public boolean deleteBookmark(Long id) {
        if (!isConnected())
            return false;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.DELETE_BOOKMARK)) {
            stmnt.setLong(1, id);
            if (stmnt.executeUpdate() > 0) {
                con.commit();
                return true;
            } else {
                con.rollback();
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), TBError.getMessage("dao.mod.t"));
            try {
                con.rollback();
            } catch (Exception ex) {
                log.error(e.getMessage(), TBError.getMessage("dao.rollback.t"));
            }
            return false;
        }
    }

    public int deleteBookmarks(Long[] ids) {
        if (!isConnected())
            return TBConsts.DAO_ERROR;
        start();
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.DELETE_BOOKMARK)) {
            for (Long id : ids) {
                stmnt.setLong(1, id);
                stmnt.executeUpdate();
                if (abort.get()) {
                    done();
                    return TBConsts.DAO_ABORT;
                }
            }
            done();
            return TBConsts.DAO_SUCCESS;
        } catch (Exception e) {
            log.error(e.getMessage(), TBError.getMessage("dao.mod.t"));
            try {
                con.rollback();
            } catch (Exception ex) {
                log.error(e.getMessage(), TBError.getMessage("dao.rollback.t"));
            }
            return TBConsts.DAO_ERROR;
        }
    }

    public boolean updateTag(Long id, String newName) {
        if (!isConnected())
            return false;
        try (PreparedStatement stmnt = con.prepareStatement(TBSql.UPDATE_TAG)) {
            stmnt.setString(1, newName);
            stmnt.setLong(2, id);

            if (stmnt.executeUpdate() > 0) {
                con.commit();
                return true;
            } else {
                con.rollback();
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), TBError.getMessage("dao.mod.t"));
            try {
                con.rollback();
            } catch (Exception ex) {
                log.error(e.getMessage(), TBError.getMessage("dao.rollback.t"));
            }
            return false;
        }
    }

    public boolean deleteTag(Long id) {
        if (!isConnected())
            return false;
        try (PreparedStatement stmnt = con.prepareStatement(TBSql.DELETE_TAG)) {
            stmnt.setLong(1, id);

            if (stmnt.executeUpdate() > 0) {
                con.commit();
                return true;
            } else {
                con.rollback();
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), TBError.getMessage("dao.mod.t"));
            try {
                con.rollback();
            } catch (Exception ex) {
                log.error(e.getMessage(), TBError.getMessage("dao.rollback.t"));
            }
            return false;
        }
    }

    public boolean deepDeleteTag(Long id) {
        if (!isConnected())
            return false;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.DELETE_TAG_BOOKMARKS);
             PreparedStatement stmnt2 = con
                     .prepareStatement(TBSql.DELETE_TAG)) {
            stmnt.setLong(1, id);
            stmnt2.setLong(1, id);
            stmnt.execute();
            stmnt2.execute();
            con.commit();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), TBError.getMessage("dao.mod.t"));
            try {
                con.rollback();
            } catch (Exception ex) {
                log.error(e.getMessage(), TBError.getMessage("dao.rollback.t"));
            }
            return false;
        }
    }

    public boolean deleteOrphanedTags() {
        if (!isConnected())
            return false;
        try (PreparedStatement stmnt = con
                .prepareStatement(TBSql.DELETE_ORPHANED_TAGS)) {
            if (stmnt.executeUpdate() > 0) {
                con.commit();
                return true;
            } else {
                con.rollback();
                return false;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), TBError.getMessage("dao.mod.t"));
            try {
                con.rollback();
            } catch (Exception ex) {
                log.error(e.getMessage(), TBError.getMessage("dao.rollback.t"));
            }
            return false;
        }
    }

    public boolean replaceTag(Long toBeReplaced, Long toReplace) {
        if (!isConnected())
            return false;

        try (PreparedStatement replaceStmnt = con
                .prepareStatement(TBSql.REPLACE_TAG);
             PreparedStatement deleteStmnt = con
                     .prepareStatement(TBSql.DELETE_TAG)) {
            replaceStmnt.setLong(1, toReplace);
            replaceStmnt.setLong(2, toBeReplaced);
            deleteStmnt.setLong(1, toBeReplaced);
            replaceStmnt.execute();
            deleteStmnt.execute();
            con.commit();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), TBError.getMessage("dao.mod.t"));
            try {
                con.rollback();
            } catch (Exception ex) {
                log.error(e.getMessage(), TBError.getMessage("dao.rollback.t"));
            }
            return false;
        }
    }

    @SuppressWarnings({"unused", "null"})
    public Set<CountedTag> searchTags(String keyword, TBTable table) {
        if (!isConnected())
            return null;

        table.clearRows();

        if (StringUtils.isBlank(keyword))
            return getTagsWithCounts(table);

        if (keyword.equals("orph")) {
            return getOrphanedTagsWithCounts(table);
        }

        Set<CountedTag> tags = null;
        try (PreparedStatement stmnt = con.prepareStatement(TBSql.SEARCH_TAGS)) {
            stmnt.setString(1, "%" + keyword.trim() + "%");
            ResultSet rs = stmnt.executeQuery();
            if (!rs.isBeforeFirst())
                return null;

            if (table == null)
                tags = new HashSet<>();

            while (rs.next()) {
                TagI tag = new TagI(rs.getLong("id"), rs.getString("name"));
                CountedTag ctag = new CountedTag(tag, 0);
                ctag.setBcount(getTagBookmarksCount(tag.getId()));
                if (table == null)
                    tags.add(ctag);
                else
                    table.addRow(ctag.toArray());
            }
            return tags;
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    @SuppressWarnings("unused")
    public List<BookmarkI> searchBookmarks(String[] keywords, TBTable table) {
        if (!isConnected())
            return null;

        table.clearRows();

        if (keywords == null || (keywords.length == 0)) {
            return getBookmarks(table);
        }

        boolean noName, noTag, searchDesc, searchAddress;
        noName = noTag = searchDesc = searchAddress = false;
        String keywordString = null;
        List<String> included = new ArrayList<>();
        List<String> excluded = new ArrayList<>();

        for (String keyword1 : keywords) {
            String keyword = keyword1.trim().toLowerCase();
            if (keyword.isEmpty()) {
                continue;
            }
            if (keyword.charAt(0) == '-') {
                excluded.add(keyword.substring(1));
                continue;
            }
            switch (keyword) {
                case "notag":
                    noTag = true;
                    break;
                case "nn":
                    noName = true;
                    break;
                case "d":
                    searchDesc = true;
                    break;
                case "a":
                    searchAddress = true;
                    break;
                default:
                    included.add(keyword);
            }
        }

        if (included.isEmpty() && excluded.isEmpty()) {
            // no keywords
            if (noTag) {
                return getUntaggedBookmarks(table);
            } else {
                return getBookmarks(table);
            }
        } else if (!included.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String s : included)
                sb.append(s).append(' ');
            keywordString = '%' + sb.toString() + '%';
        } else {
            noName = true;
            searchDesc = searchAddress = false;
        }

        Set<BookmarkI> result = new HashSet<>();
        List<BookmarkI> tmpList;
        start();
        // search by name
        if (!noName) {
            try (PreparedStatement stmnt = con
                    .prepareStatement(TBSql.SEARCH_BOOMARKS_NAME)) {
                stmnt.setString(1, keywordString);
                tmpList = executeSelectBookmarks(stmnt, table);
                if (tmpList != null)
                    result.addAll(tmpList);
                if (abort.get()) {
                    return finishSearch(result);
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
                return null;
            }
        }

        // search by description
        if (searchDesc) {
            try (PreparedStatement stmnt = con
                    .prepareStatement(TBSql.SEARCH_BOOKMARKS_DESCRIPTION)) {
                stmnt.setString(1, keywordString);
                tmpList = executeSelectBookmarks(stmnt, table);
                if (tmpList != null)
                    result.addAll(tmpList);
                if (abort.get()) {
                    return finishSearch(result);
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
                return null;
            }
        }
        // search by address
        if (searchAddress) {
            try (PreparedStatement stmnt = con
                    .prepareStatement(TBSql.SEARCH_BOOKMARKS_ADDRESS)) {
                stmnt.setString(1, keywordString);
                tmpList = executeSelectBookmarks(stmnt, table);
                if (tmpList != null)
                    result.addAll(tmpList);
                if (abort.get()) {
                    return finishSearch(result);
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
                return null;
            }
        }

        // search by tag
        if (noTag) {
            if (table == null)
                result.addAll(getUntaggedBookmarks(null));
            else
                getUntaggedBookmarks(table);
        }
        if (abort.get()) {
            return finishSearch(result);
        }
        try (PreparedStatement stmnt = con.prepareStatement(TBSql
                .getSearchBookmarkByTag(included.size(), excluded.size()))) {
            int stmntIndex, listIndex;
            for (stmntIndex = 1, listIndex = 0; listIndex < included.size(); ++stmntIndex, ++listIndex) {
                stmnt.setString(stmntIndex, keywords[listIndex]);
            }
            for (listIndex = 0; listIndex < excluded.size(); ++stmntIndex, ++listIndex) {
                stmnt.setString(stmntIndex, excluded.get(listIndex));
            }

            tmpList = executeSelectBookmarks(stmnt, table);
            if (tmpList != null)
                result.addAll(tmpList);
            if (abort.get()) {
                return finishSearch(result);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }

        return finishSearch(result);
    }

    private List<BookmarkI> finishSearch(Set<BookmarkI> data) {
        done();
        if (data.isEmpty())
            return null;
        return new ArrayList<>(data);
    }

    public List<BookmarkI> searchBookmarks2(String[] keywords, TBTable table) {
        if (!isConnected())
            return null;

        table.clearRows();

        if (ArrayUtils.isEmpty(keywords))
            return getBookmarks(table);

        int searchMask = 0;
        boolean noName = false;
        boolean noTag = false;
        List<String> included = new ArrayList<>();
        List<String> excluded = new ArrayList<>();
        String keywordString = null;
        List<BookmarkI> res = new ArrayList<>();

        for (String keyword1 : keywords) {
            String keyword = keyword1.trim().toLowerCase();
            if (keyword.isEmpty())
                continue;

            if (keyword.charAt(0) == '-') {
                excluded.add(keyword.substring(1));
                continue;
            }
            switch (keyword) {
                case "nt":
                    noTag = true;
                    break;
                case "nn":
                    noName = true;
                    break;
                case "d":
                    searchMask |= TBConsts.SEARCH_DESC_MASK;
                    break;
                case "a":
                    searchMask |= TBConsts.SEARCH_ADDRESS_MASK;
                    break;
                case "n":
                    searchMask |= TBConsts.SEARCH_NOTES_MASK;
                    break;
                case "notag":
                    searchMask |= TBConsts.SEARCH_NOTAG_MASK;
                    break;
                default:
                    included.add(keyword);
            }
        }
        if (!noName)
            searchMask |= TBConsts.SEARCH_NAME_MASK;
        if (!noTag)
            searchMask |= TBConsts.SEARCH_TAG_MASK;

        if (!included.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String s : included)
                sb.append(s).append(' ');
            keywordString = '%' + sb.toString().trim() + '%';
        }

        // adjust searchMask
        if (keywordString == null) {
            int nameDescAddressNotesMask = TBConsts.SEARCH_NAME_MASK
                    | TBConsts.SEARCH_DESC_MASK | TBConsts.SEARCH_ADDRESS_MASK
                    | TBConsts.SEARCH_NOTES_MASK;
            searchMask &= ~nameDescAddressNotesMask;
        }

        start();
        try (PreparedStatement stmnt = con.prepareStatement(TBSql
                .getSearchBookmarks(included.size(), excluded.size(),
                        searchMask))) {
            int stmntIndex = 1;
            if ((searchMask & TBConsts.SEARCH_TAG_MASK) != 0) {
                int listIndex;
                for (listIndex = 0; listIndex < included.size(); ++stmntIndex, ++listIndex) {
                    stmnt.setString(stmntIndex, included.get(listIndex));
                }
                for (listIndex = 0; listIndex < excluded.size(); ++stmntIndex, ++listIndex) {
                    stmnt.setString(stmntIndex, excluded.get(listIndex));
                }
            }

            if (keywordString != null) {
                int count = 0;
                if ((searchMask & TBConsts.SEARCH_NAME_MASK) != 0)
                    ++count;
                if ((searchMask & TBConsts.SEARCH_DESC_MASK) != 0)
                    ++count;
                if ((searchMask & TBConsts.SEARCH_ADDRESS_MASK) != 0)
                    ++count;
                if ((searchMask & TBConsts.SEARCH_NOTES_MASK) != 0)
                    ++count;
                for (int i = 0; i < count; ++i, ++stmntIndex)
                    stmnt.setString(stmntIndex, keywordString);
            }
            if (abort.get())
                return finishSearch(res);
            res = executeSelectBookmarks(stmnt, table);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
        }

        return finishSearch(res);
    }

    private List<BookmarkI> finishSearch(List<BookmarkI> data) {
        done();
        if (data.isEmpty())
            return null;
        return data;
    }

    private List<BookmarkI> executeSelectBookmarks(PreparedStatement stmnt,
                                                   TBTable table) {
        List<BookmarkI> bkmrks = null;
        start();
        try {
            ResultSet rs = stmnt.executeQuery();
            if (!rs.isBeforeFirst())
                return null;
            if (table == null)
                bkmrks = new ArrayList<>();
            while (rs.next()) {
                BookmarkI bkmrk = new BookmarkI(rs.getLong("id"),
                        rs.getString("name"), rs.getString("description"),
                        rs.getString("address"), rs.getString("notes"), null,
                        rs.getTimestamp("dateadded"),
                        rs.getTimestamp("datemodified"));
                Set<TagI> tags = getBookmarkTags(bkmrk.getId());
                bkmrk.setTags(tags);
                if (table == null)
                    bkmrks.add(bkmrk);
                else
                    table.addRow(bkmrk.toArray(TBConsts.ADDRESS_AS_URI,
                            TBConsts.TAGS_AS_STRING));
                if (abort.get()) {
                    done();
                    break;
                }
            }
            return bkmrks;
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    private Set<TagI> executeSelectTags(PreparedStatement stmnt, TBTable table) {
        Set<TagI> tags = null;
        start();
        try {
            ResultSet rs = stmnt.executeQuery();
            if (!rs.isBeforeFirst())
                return null;
            if (table == null)
                tags = new HashSet<>();
            while (rs.next()) {
                TagI tag = new TagI(rs.getLong("id"), rs.getString("name"));
                if (table == null)
                    tags.add(tag);
                else
                    table.addRow(tag.toArray());
                if (abort.get()) {
                    done();
                    break;
                }
            }
            return tags;
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    private Set<CountedTag> executeSelectCountedTags(PreparedStatement stmnt,
                                                     TBTable table) {
        Set<CountedTag> cts = null;
        start();
        try {
            ResultSet rs = stmnt.executeQuery();
            if (!rs.isBeforeFirst())
                return null;
            if (table == null)
                cts = new HashSet<>();
            while (rs.next()) {
                TagI t = new TagI(rs.getLong("id"), rs.getString("name"));
                CountedTag ct = new CountedTag(t, 0);
                ct.setBcount(getTagBookmarksCount(t.getId()));
                if (table == null)
                    cts.add(ct);
                else
                    table.addRow(ct.toArray());
                if (abort.get()) {
                    done();
                    break;
                }
            }
            return cts;
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return null;
        }
    }

    private long executeCount(PreparedStatement stmnt) {
        try {
            ResultSet rs = stmnt.executeQuery();
            if (!rs.isBeforeFirst())
                return 0;
            rs.next();
            return rs.getLong("TOTAL");
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return 0;
        }
    }

    private long executeCount(String sqlStmnt) {
        if (!isConnected())
            return 0;
        try (PreparedStatement stmnt = con.prepareStatement(sqlStmnt)) {
            return executeCount(stmnt);
        } catch (SQLException e) {
            log.error(e.getMessage(), TBError.getMessage("dao.sel.t"));
            return 0;
        }
    }

    private String getDbUrlString(String path, boolean create) {
        String url = "jdbc:h2:" + path + ";TRACE_LEVEL_FILE=0";
        if (create) {
            return url;
        }
        return url + ";IFEXISTS=TRUE";
    }

    private void deleteH2dbFile() {
        TBIOUtils.deleteFile(TBIOUtils.setFileExt(dbPath,
                TBConsts.H2DB_FILE_EXT));
    }

    private void start() {
        abort.set(false);
        working.set(true);
    }

    private void done() {
        working.set(false);
    }
}
