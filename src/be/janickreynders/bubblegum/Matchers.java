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

import javax.servlet.http.HttpServletRequest;

public class Matchers {

    public static RequestMatcher method(final String httpMethod) {
        return new RequestMatcher() {
            @Override
            public boolean matches(HttpServletRequest req) {
                return httpMethod.equalsIgnoreCase(req.getMethod());
            }
        };
    }

    public static RequestMatcher header(final String headerName, final CharSequence value) {
        return new RequestMatcher() {
            @Override
            public boolean matches(HttpServletRequest req) {
                return req.getHeader(headerName).contains(value);
            }
        };
    }

    public static RequestMatcher accept(CharSequence mimeType) {
        return header("Accept", mimeType);
    }

    public static RequestMatcher contentType(CharSequence contentType) {
        return header("Content-Type", contentType);
    }

    public static RequestMatcher all(final RequestMatcher... matchers) {
        return new RequestMatcher() {
            @Override
            public boolean matches(HttpServletRequest req) {
                for (RequestMatcher matcher : matchers) {
                    if (!matcher.matches(req)) return false;
                }
                return true;
            }
        };
    }

    public static RequestMatcher any(final RequestMatcher... matchers) {
        return new RequestMatcher() {
            @Override
            public boolean matches(HttpServletRequest req) {
                for (RequestMatcher matcher : matchers) {
                    if (matcher.matches(req)) return true;
                }
                return false;
            }
        };
    }

    public static ExceptionMatcher ofType(final Class<? extends Exception> clazz) {
        return new ExceptionMatcher() {
            @Override
            public boolean matches(Exception e) {
                return clazz.isInstance(e);
            }
        };
    }

}
