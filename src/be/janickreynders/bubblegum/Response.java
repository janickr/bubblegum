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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Response {
    private HttpServletResponse resp;

    public Response(HttpServletResponse resp) {
        this.resp = resp;
    }

    public HttpServletResponse raw() {
        return resp;
    }

    public void ok(CharSequence c) throws IOException {
        resp.getWriter().append(c);
    }

    public void status(int code) throws IOException {
        resp.sendError(code);
    }

    public void contentType(String contentType) {
        resp.setContentType(contentType);
    }

    public void vary(String requestHeaders) {
        header("Vary", requestHeaders);
    }

    public void header(String name, String value) {
        resp.setHeader(name, value);
    }

    public void redirect(String url) throws IOException {
        resp.sendRedirect(url);
    }

    public void removeCookie(String name) {
        addCookie(name, "", 0);
    }

    public void removeCookie(String name, String path) {
        addCookie(name, "", 0, path);
    }

    public void addNonPersistentCookie(String name, String value) {
        addCookie(name, value, -1);
    }

    public void addCookie(String name, String value, int maxAge) {
        addCookie(name, value, maxAge, null);
    }

    public void addCookie(String name, String value, int maxAge, String path) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);
        resp.addCookie(cookie);
    }

}
