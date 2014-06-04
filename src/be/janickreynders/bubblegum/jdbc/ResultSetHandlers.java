/*
 * MIT license
 *
 * Copyright (c) 2012 Janick Reynders
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package be.janickreynders.bubblegum.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultSetHandlers {

    public static ResultSetHandler<List<Map<String, Object>>> LIST = rs -> {
        List<Map<String, Object>> result = new ArrayList<>();
        ResultSetMetaData metaData = rs.getMetaData();
        while (rs.next()) {
            result.add(readRow(rs, metaData));
        }

        return result;
    };

    public static ResultSetHandler<Map<String, Object>> MAP = rs -> {
        if (!rs.next()) return null;

        return readRow(rs, rs.getMetaData());
    };


    public static <T> ResultSetHandler<T> single(Class<T> clazz) {
        return rs -> {
            if (!rs.next()) return null;

            return clazz.cast(rs.getObject(1));
        };
    }

    public static <T> ResultSetHandler<List<T>> listOf(Class<T> clazz) {
        return rs -> {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(clazz.cast(rs.getObject(1)));
            }

            return result;
        };
    }

    private static Map<String, Object> readRow(ResultSet set, ResultSetMetaData metaData) throws SQLException {
        HashMap<String, Object> result = new HashMap<>();
        int cols = metaData.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            result.put(metaData.getColumnLabel(i), set.getObject(i));
        }

        return result;
    }
}
