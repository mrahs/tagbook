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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class OpenHistory {
    private List<String> paths;
    private int limit;

    public OpenHistory() {
        this(5);
    }

    private OpenHistory(int limit) {
        if (limit < 1)
            throw new IllegalArgumentException("(" + limit + ") is less than 1");
        this.limit = limit;
        paths = new ArrayList<>(limit);
    }

    public List<String> getPaths() {
        return paths;
    }

    String[] getPathsArray() {
        return paths.toArray(new String[paths.size()]);
    }

    public String[] getPathsArrayReversed() {
        String[] res = getPathsArray();
        ArrayUtils.reverse(res);
        return res;
    }

    public void addPath(String path) {
        if (StringUtils.isNotBlank(path))
            if (paths.contains(path)) {
                paths.remove(path);
                paths.add(path);
            } else if (paths.size() == limit) {
                paths.remove(0);
                paths.add(path);
            } else
                paths.add(path);
    }

    public void addPaths(String[] paths) {
        if (paths != null && paths.length > 0)
            for (String p : paths) {
                addPath(p);
            }
    }

    public void removePath(String path) {
        if (StringUtils.isNotBlank(path))
            paths.remove(path);
    }

    public boolean isEmpty() {
        return paths.isEmpty();
    }

    public void clear() {
        paths.clear();
    }

    @Override
    public String toString() {
        if (paths.isEmpty())
            return "";
        StringBuilder res = new StringBuilder();
        for (String s : paths) {
            res.append(s).append(';');
        }
        return res.toString();
    }
}
