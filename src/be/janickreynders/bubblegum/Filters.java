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

package be.janickreynders.bubblegum;

import java.text.SimpleDateFormat;
import java.util.*;

public class Filters {

    public static Filter handler(final Handler handler) {
        return new Filter() {
            @Override
            public void handle(Request req, Response resp, Chain chain) throws Exception {
                handler.handle(req, resp);
            }
        };
    }

    public static Filter catchAndHandle(final Class<? extends Exception> clazz, final Handler handler) {
        return new Filter() {
            @Override
            public void handle(Request req, Response resp, Chain chain) throws Exception {
                try{
                    chain.handle(req, resp);
                } catch (Exception e) {
                    if (clazz.isInstance(e))
                        handler.handle(req, resp);
                    else
                        throw e;
                }
            }
        };
    }

    public static Filter header(final String name, final String value) {
        return new Filter() {
            @Override
            public void handle(Request req, Response resp, Chain chain) throws Exception {
                resp.header(name, value);
                chain.handle(req, resp);
            }
        };
    }

    public static Filter cacheNeverExpires() {
        return new Filter() {
            @Override
            public void handle(Request req, Response resp, Chain chain) throws Exception {
                resp.header("Expires", neverExpiresAsSpecifiedByHttp1Dot1());
                resp.header("Cache-Control", "max-age=" + (60 * 60 * 24 * 365));
                chain.handle(req, resp);
            }

            private String neverExpiresAsSpecifiedByHttp1Dot1() {
                return format(nextYear());
            }

            private Date nextYear() {
                final GregorianCalendar calendar = new GregorianCalendar();
                calendar.add(Calendar.YEAR, 1);
                return calendar.getTime();
            }

            private String format(Date date) {
                final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                format.setTimeZone(TimeZone.getTimeZone("GMT"));
                return format.format(date);
            }
        };
    }

    public static Filter vary(final String requestHeaders) {
        return new Filter() {
            @Override
            public void handle(Request req, Response resp, Chain chain) throws Exception {
                resp.vary(requestHeaders);
                chain.handle(req, resp);
            }
        };
    }

}
