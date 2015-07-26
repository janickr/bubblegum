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

public class Handlers {
    public static Handler status(final int httpStatusCode) {
        return new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                resp.status(httpStatusCode);
            }

            @Override
            public String toString() {
                return "http status " + httpStatusCode;
            }
        };
    }

    public static Handler forward(final String url) {
        return new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                req.forward(url, resp);
            }

            @Override
            public String toString() {
                return "forward to " + url;
            }
        };
    }

    public static Handler redirect(final String url) {
        return new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                resp.redirect(url);
            }

            @Override
            public String toString() {
                return "redirect to " + url;
            }
        };
    }
}
