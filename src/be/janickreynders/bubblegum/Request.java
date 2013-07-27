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

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public class Request {
    private final HttpServletRequest req;
    private final Map<String, String> params;

    public Request(HttpServletRequest req, Map<String, String> params) {
        this.req = req;
        this.params = params;
    }

    public Request(HttpServletRequest req) {
        this(req, new HashMap<String, String>());
    }

    public HttpServletRequest raw() {
        return req;
    }

    public String body() throws IOException {
        BufferedReader reader = req.getReader();
        StringWriter writer = new StringWriter();
        char[] buffer = new char[1024];

        int n = reader.read(buffer);
        while (n != -1) {
            writer.write(buffer, 0, n);
            n = reader.read(buffer);
        }
        return writer.toString();
    }

    public String param(String name) {
        return params.get(name);
    }

    public Object attribute(String name) {
        return req.getAttribute(name);
    }

    public void attribute(String name, Object val) {
        req.setAttribute(name, val);
    }

    public void forward(String url, Response response) throws IOException, ServletException {
        req.setAttribute("bubblegumParams", params);
        req.getRequestDispatcher(url).forward(req, response.raw());
    }

    public Set<String> queryParams() {
        return new HashSet<String>(req.getParameterMap().keySet());
    }

    public String queryParam(String name) {
        return req.getParameter(name);
    }

    public List<String> queryParams(String name) {
        return Arrays.asList(req.getParameterValues(name));
    }

    public Cookie cookie(String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) return cookie;
        }
        return null;
    }

    public String getPath() {
        return raw().getRequestURI().substring(raw().getContextPath().length());
    }

    public String method() {
        return req.getMethod();
    }

    public String header(String headerName) {
        return req.getHeader(headerName);
    }
}
